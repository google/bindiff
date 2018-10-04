#include "third_party/zynamics/bindiff/flow_graph.h"

#include <cinttypes>
#include <sstream>

#include "base/logging.h"
#include "third_party/absl/strings/str_cat.h"
#ifdef GOOGLE
#include "third_party/boost/allowed/graph/breadth_first_search.hpp"
#include "third_party/boost/allowed/graph/dominator_tree.hpp"
#include "third_party/boost/allowed/graph/filtered_graph.hpp"
#include "third_party/boost/allowed/graph/iteration_macros.hpp"
#else
#include <boost/graph/breadth_first_search.hpp>  // NOLINT
#include <boost/graph/dominator_tree.hpp>        // NOLINT
#include <boost/graph/filtered_graph.hpp>        // NOLINT
#include <boost/graph/iteration_macros.hpp>      // NOLINT
#endif  // GOOGLE
#include "third_party/zynamics/bindiff/call_graph.h"
#include "third_party/zynamics/bindiff/comments.h"
#include "third_party/zynamics/bindiff/fixed_points.h"
#include "third_party/zynamics/bindiff/graph_util.h"
#include "third_party/zynamics/bindiff/prime_signature.h"
#include "third_party/zynamics/binexport/binexport.h"
#include "third_party/zynamics/binexport/format_util.h"
#include "third_party/zynamics/binexport/hash.h"

namespace security {

using binexport::FormatAddress;
using binexport::GetInstructionAddress;

namespace bindiff {
namespace {

// Maximum number of basic blocks/edges/instructions we want to allow for a
// single function. If a function has more than this, we simply discard it as
// invalid.
enum {
  kMaxFunctionBasicBlocks = 5000,
  kMaxFunctionEdges = 5000,
  kMaxFunctionInstructions = 10000
};

uint8_t TranslateEdge(int edge_type) {
  switch (edge_type) {
    case 1:
      return FlowGraph::EDGE_TRUE;
    case 2:
      return FlowGraph::EDGE_FALSE;
    case 3:
      return FlowGraph::EDGE_UNCONDITIONAL;
    case 4:
      return FlowGraph::EDGE_SWITCH;
    default:
      throw std::runtime_error("invalid edge type");
  }
}

// Translates from BinExport2 protocol buffer edge type to the one used
// internally by the Differ.
uint8_t ProtoToFlowGraphEdgeType(
    BinExport2::FlowGraph::Edge::Type type) {
  switch (type) {
    case BinExport2::FlowGraph::Edge::CONDITION_TRUE:
      return FlowGraph::EDGE_TRUE;
    case BinExport2::FlowGraph::Edge::CONDITION_FALSE:
      return FlowGraph::EDGE_FALSE;
    case BinExport2::FlowGraph::Edge::UNCONDITIONAL:
      return FlowGraph::EDGE_UNCONDITIONAL;
    case BinExport2::FlowGraph::Edge::SWITCH:
      return FlowGraph::EDGE_SWITCH;
    default:
      throw std::runtime_error("invalid edge type");
  }
}

bool SortByAddressLevel(const std::pair<Address, FlowGraph::Level>& one,
                        const std::pair<Address, FlowGraph::Level>& two) {
  return one.first < two.first;
}

}  // namespace

// Returns true iff the addresses are in strictly increasing order.
bool IsSorted(const std::vector<Address>& addresses) {
  if (addresses.empty()) {
    return true;
  }

  std::vector<Address>::const_iterator i = addresses.begin();
  std::vector<Address>::const_iterator j = i;
  ++j;
  const std::vector<Address>::const_iterator end = addresses.end();
  for (; j != end; ++i, ++j) {
    if (*i >= *j) {
      return false;
    }
  }
  return true;
}

// Returns the index of address in addresses.
FlowGraph::Vertex FindVertex(const std::vector<Address>& addresses,
                             Address address) {
  auto it = std::lower_bound(addresses.begin(), addresses.end(), address);
  if (it == addresses.end() || *it != address) {
    LOG(ERROR) << absl::StrCat("Could not find basic block: ",
                               FormatAddress(address));
    it = addresses.begin();
  }
  return FlowGraph::Vertex(std::distance(addresses.begin(), it));
}

FlowGraph::FlowGraph(CallGraph* call_graph, Address entry_point)
    : graph_(),
      level_for_call_(),
      call_graph_(call_graph),
      call_graph_vertex_(call_graph->GetVertex(entry_point)),
      md_index_(0),
      md_index_inverted_(0),
      entry_point_address_(entry_point),
      fixed_point_(0),
      prime_(0),
      byte_hash_(1),
      string_references_(1),
      instructions_(),
      call_targets_(),
      num_loops_(0) {
  call_graph_->AttachFlowGraph(this);
}

// instruction_cache needs to be passed in (it used to be a static member)
// because otherwise flow graphs wouldn't be thread safe. The cache has to
// be a thread local object.
FlowGraph::FlowGraph()
    : graph_(),
      level_for_call_(),
      call_graph_(),
      call_graph_vertex_(0),
      md_index_(0),
      md_index_inverted_(0),
      entry_point_address_(0),
      fixed_point_(0),
      prime_(0),
      byte_hash_(1),
      string_references_(1),
      instructions_(),
      call_targets_(),
      num_loops_(0) {}

FlowGraph::~FlowGraph() {
  if (call_graph_) {
    call_graph_->DetachFlowGraph(this);
  }
}

FlowGraph::Graph& FlowGraph::GetGraph() { return graph_; }

const FlowGraph::Graph& FlowGraph::GetGraph() const { return graph_; }

const string& FlowGraph::GetName() const {
  return call_graph_->GetName(call_graph_vertex_);
}

const string& FlowGraph::GetDemangledName() const {
  return call_graph_->GetDemangledName(call_graph_vertex_);
}

const string& FlowGraph::GetGoodName() const {
  return call_graph_->GetGoodName(call_graph_vertex_);
}

bool FlowGraph::HasRealName() const {
  return call_graph_->HasRealName(call_graph_vertex_);
}

void FlowGraph::Read(const BinExport2& proto,
                     const BinExport2::FlowGraph& proto_flow_graph,
                     CallGraph* call_graph,
                     Instruction::Cache* instruction_cache) {
  entry_point_address_ =
      proto.instruction(
               proto.basic_block(proto_flow_graph.entry_basic_block_index())
                   .instruction_index(0)
                   .begin_index())
          .address();
  call_graph_ = call_graph;
  call_graph_->AttachFlowGraph(this);
  call_graph_vertex_ = call_graph_->GetVertex(entry_point_address_);

  prime_ = 0;  // Sum of basic block primes.

  // TODO(soerenme) We don't export string references yet (BinDetego doesn't
  //     have them, only the IDA plugin).
  string_references_ = 1;

  Address computed_instruction_address = 0;
  int last_instruction_index = 0;
  string function_bytes;
  std::vector<VertexInfo> temp_vertices(
      proto_flow_graph.basic_block_index_size());
  std::vector<Address> temp_addresses(temp_vertices.size());
  for (int basic_block_index = 0;
       basic_block_index < proto_flow_graph.basic_block_index_size();
       ++basic_block_index) {
    const BinExport2::BasicBlock& proto_basic_block(proto.basic_block(
        proto_flow_graph.basic_block_index(basic_block_index)));
    string basic_block_bytes;

    VertexInfo& vertex_info(temp_vertices[basic_block_index]);
    vertex_info.instruction_start_ = instructions_.size();
    vertex_info.prime_ = 0;        // Sum of instruction primes.
    vertex_info.fixed_point_ = 0;  // Not a fixed point yet...

    // Flags will be set by analysis steps later on. Currently the only flag
    // used is VERTEX_LOOPENTRY.
    vertex_info.flags_ = 0;

    // TODO(soerenme) We don't export string references yet (BinDetego doesn't
    //     have them, only the IDA plugin).
    vertex_info.string_hash_ = 0;
    vertex_info.call_target_start_ = std::numeric_limits<uint32_t>::max();
    CHECK(proto_basic_block.instruction_index_size());
    for (int interval_index = 0;
         interval_index < proto_basic_block.instruction_index_size();
         ++interval_index) {
      const BinExport2::BasicBlock::IndexRange& instruction_interval(
          proto_basic_block.instruction_index(interval_index));
      const int instruction_end_index(instruction_interval.has_end_index()
                                          ? instruction_interval.end_index()
                                          : instruction_interval.begin_index() +
                                                1);
      for (int instruction_index = instruction_interval.begin_index();
           instruction_index < instruction_end_index; ++instruction_index) {
        const BinExport2::Instruction& proto_instruction(
            proto.instruction(instruction_index));
        Address instruction_address = 0;
        if (last_instruction_index == instruction_index - 1 &&
            !proto_instruction.has_address()) {
          instruction_address = computed_instruction_address;
        } else {
          instruction_address = GetInstructionAddress(proto, instruction_index);
        }
        computed_instruction_address =
            instruction_address + proto_instruction.raw_bytes().size();
        last_instruction_index = instruction_index;
        const string& mnemonic(
            proto.mnemonic(proto_instruction.mnemonic_index()).name());
        const uint32_t instruction_prime = bindiff::GetPrime(mnemonic);
        vertex_info.prime_ += instruction_prime;
        instructions_.emplace_back(instruction_cache, instruction_address,
                                   mnemonic, instruction_prime);
        basic_block_bytes += proto_instruction.raw_bytes();

        if (proto_instruction.call_target_size() > 0 &&
            vertex_info.call_target_start_ ==
                std::numeric_limits<uint32_t>::max()) {
          vertex_info.call_target_start_ = call_targets_.size();
        }
        for (int i = 0; i < proto_instruction.call_target_size(); ++i) {
          call_targets_.push_back(proto_instruction.call_target(i));
        }
      }
    }

    temp_addresses[basic_block_index] =
        instructions_[vertex_info.instruction_start_].GetAddress();
    prime_ += vertex_info.prime_;
    vertex_info.basic_block_hash_ = GetSdbmHash(basic_block_bytes);
    function_bytes += basic_block_bytes;
  }

  byte_hash_ = GetSdbmHash(function_bytes);

  if (!IsSorted(temp_addresses)) {
    throw std::runtime_error("Basic blocks not sorted by address!");
  }

  typedef std::pair<Graph::edges_size_type, Graph::edges_size_type>
      EdgeDescriptor;
  std::vector<EdgeDescriptor> edges(proto_flow_graph.edge_size());
  std::vector<EdgeInfo> edge_properties(proto_flow_graph.edge_size());
  for (int i = 0; i < proto_flow_graph.edge_size(); ++i) {
    const BinExport2::FlowGraph::Edge& proto_edge =
        proto_flow_graph.edge(i);
    const Address source_address = GetInstructionAddress(
        proto, proto.basic_block(proto_edge.source_basic_block_index())
                   .instruction_index(0)
                   .begin_index());
    const Address target_address = GetInstructionAddress(
        proto, proto.basic_block(proto_edge.target_basic_block_index())
                   .instruction_index(0)
                   .begin_index());
    edges[i] = EdgeDescriptor(FindVertex(temp_addresses, source_address),
                              FindVertex(temp_addresses, target_address));
    edge_properties[i].flags_ = ProtoToFlowGraphEdgeType(proto_edge.type());
  }

  // This leaves prime, byte hash etc unaffected. It's debatable whether that is
  // good or bad. It doesn't reflect the current reality of the loaded graph
  // after truncation, but it does reflect the actual disassembly.
  if (instructions_.size() >= kMaxFunctionInstructions ||
      edges.size() >= kMaxFunctionEdges ||
      temp_addresses.size() >= kMaxFunctionBasicBlocks) {
    LOG(WARNING) << absl::StrCat(
        "Function ", FormatAddress(entry_point_address_),
        " is excessively large: ", temp_addresses.size(), " basic blocks, ",
        edges.size(), " edges, ", instructions_.size(),
        " instructions. Discarding.");
  } else {
    Graph temp_graph(boost::edges_are_unsorted_multi_pass, edges.begin(),
                     edges.end(), edge_properties.begin(),
                     temp_addresses.size());
    std::swap(graph_, temp_graph);

    {
      VertexIterator i, end;
      int j = 0;
      for (boost::tie(i, end) = boost::vertices(graph_); i != end; ++i, ++j)
        graph_[*i] = temp_vertices[j];
    }
  }

  Init();
}

void FlowGraph::Init() {
  instructions_.shrink_to_fit();  // shrink to fit
  call_targets_.shrink_to_fit();

  // Adjust end iterators to one-past-the-end of instructions array instead of
  // std::numeric_limits::max.
  VertexIterator i, end;
  for (boost::tie(i, end) = boost::vertices(graph_); i != end; ++i) {
    graph_[*i].instruction_start_ =
        std::min(graph_[*i].instruction_start_,
                 static_cast<uint32_t>(instructions_.size()));
    graph_[*i].call_target_start_ =
        std::min(graph_[*i].call_target_start_,
                 static_cast<uint32_t>(call_targets_.size()));
  }

  CalculateTopology();
  SetMdIndex(CalculateMdIndex(*this));
  SetMdIndexInverted(CalculateMdIndex(*this, true));
  CalculateCallLevels();
  MarkLoops();
}

uint8_t FlowGraph::GetFlags(const Edge& edge) const {
  return graph_[edge].flags_;
}

void FlowGraph::SetFlags(const Edge& edge, uint8_t flags) {
  graph_[edge].flags_ = flags;
}

uint32_t FlowGraph::GetFlags(Vertex vertex) const {
  return graph_[vertex].flags_;
}

void FlowGraph::SetFlags(Vertex vertex, uint32_t flags) {
  graph_[vertex].flags_ = flags;
}

Address FlowGraph::GetAddress(Vertex vertex) const {
  return GetInstructions(vertex).first->GetAddress();
}

void FlowGraph::MarkLoops() {
  std::vector<Vertex> dominator_nodes(
      num_vertices(graph_), boost::graph_traits<Graph>::null_vertex());
  boost::lengauer_tarjan_dominator_tree(
      // Need boost::filtered_graph<> to satisfy Boost.Graph concept.
      boost::make_filtered_graph(graph_, boost::keep_all()),
      boost::vertex(0, graph_),
      make_iterator_property_map(dominator_nodes.begin(),
                                 get(boost::vertex_index, graph_)));

  EdgeIterator i, end;
  for (boost::tie(i, end) = boost::edges(graph_); i != end; ++i) {
    const Vertex target = boost::target(*i, graph_);
    const Vertex source = boost::source(*i, graph_);
    if (dominator_nodes[source] != boost::graph_traits<Graph>::null_vertex()) {
      for (Vertex node = dominator_nodes[source];
           node != boost::graph_traits<Graph>::null_vertex();
           node = dominator_nodes[node]) {
        if (node == target) {
          SetFlags(*i, GetFlags(*i) | EDGE_DOMINATED);
          graph_[target].flags_ |= VERTEX_LOOPENTRY;
          ++num_loops_;
          break;
        }
      }
    }
  }
}

uint16_t FlowGraph::GetLoopCount() const { return num_loops_; }

bool FlowGraph::IsLoopEntry(Vertex vertex) const {
  return (GetFlags(vertex) & VERTEX_LOOPENTRY) != 0;
}

// Precondition: calculateTopology has been called already.
void FlowGraph::CalculateCallLevels() {
  level_for_call_.clear();
  VertexIterator i, end;
  for (boost::tie(i, end) = boost::vertices(graph_); i != end; ++i) {
    auto calls = GetCallTargets(*i);
    if (calls.first == calls.second) {
      continue;
    }

    const size_t level = GetTopologyLevel(*i);
    size_t sequence = 0;
    for (; calls.first != calls.second; ++calls.first, ++sequence) {
      level_for_call_.emplace_back(*calls.first,
                                   std::make_pair(level, sequence));
    }
  }
  level_for_call_.shrink_to_fit();
  // Warning: Stable sort is required here as call levels are not necessarily
  //          unique. I actually encountered differing diff results between
  //          Linux and Windows versions because of this.
  // TODO(soerenme) Check whether we should be sorting by level in addition to
  //     source address (what we are currently doing).
  std::stable_sort(level_for_call_.begin(), level_for_call_.end(),
                   &SortByAddressLevel);
}

void FlowGraph::CalculateTopology() {
  BreadthFirstSearch(&graph_);
  InvertedBreadthFirstSearch(&graph_);
}

size_t FlowGraph::GetTopologyLevel(Vertex vertex) const {
  return graph_[vertex].bfs_top_down_;
}

size_t FlowGraph::GetTopologyLevelInverted(Vertex vertex) const {
  return graph_[vertex].bfs_bottom_up_;
}

// MD index for a vertex is defined as the sum of all edge MD indices for
// that vertex.
double FlowGraph::GetMdIndex(Vertex vertex) const {
  const Graph& graph = GetGraph();
  std::vector<double> md_indices(in_degree(vertex, graph) +
                                 out_degree(vertex, graph));

  size_t index = 0;
  {
    InEdgeIterator i, end;
    for (boost::tie(i, end) = in_edges(vertex, graph); i != end; ++i, ++index) {
      md_indices[index] = GetMdIndex(*i);
    }
  }
  {
    OutEdgeIterator i, end;
    for (boost::tie(i, end) = out_edges(vertex, graph); i != end;
         ++i, ++index) {
      md_indices[index] = GetMdIndex(*i);
    }
  }

  double md_index = 0.0;
  std::sort(md_indices.begin(), md_indices.end());
  for (size_t i = 0; i < md_indices.size(); ++i) {
    md_index += md_indices[i];
  }

  return md_index;
}

double FlowGraph::GetMdIndexInverted(Vertex vertex) const {
  const Graph& graph = GetGraph();
  std::vector<double> md_indices(boost::in_degree(vertex, graph) +
                                 boost::out_degree(vertex, graph));

  size_t index = 0;
  {
    InEdgeIterator i, end;
    for (boost::tie(i, end) = boost::in_edges(vertex, graph); i != end;
         ++i, ++index) {
      md_indices[index] = GetMdIndexInverted(*i);
    }
  }
  {
    OutEdgeIterator i, end;
    for (boost::tie(i, end) = boost::out_edges(vertex, graph); i != end;
         ++i, ++index) {
      md_indices[index] = GetMdIndexInverted(*i);
    }
  }

  double md_index = 0.0;
  std::sort(md_indices.begin(), md_indices.end());
  for (size_t i = 0; i < md_indices.size(); ++i) {
    md_index += md_indices[i];
  }

  return md_index;
}

bool FlowGraph::IsCircular(const Edge& edge) const {
  return boost::source(edge, graph_) == boost::target(edge, graph_);
}

double FlowGraph::GetMdIndex(const Edge& edge) const {
  return graph_[edge].md_index_top_down_;
}

double FlowGraph::GetMdIndexInverted(const Edge& edge) const {
  return graph_[edge].md_index_bottom_up_;
}

void FlowGraph::SetMdIndex(double index) { md_index_ = index; }

void FlowGraph::SetMdIndexInverted(double index) { md_index_inverted_ = index; }

uint64_t FlowGraph::GetPrime(Vertex vertex) const {
  return graph_[vertex].prime_;
}

uint32_t FlowGraph::GetHash(Vertex vertex) const {
  return graph_[vertex].basic_block_hash_;
}

uint64_t FlowGraph::GetPrime() const { return prime_; }

std::pair<FlowGraph::CallTargets::const_iterator,
          FlowGraph::CallTargets::const_iterator>
FlowGraph::GetCallTargets(Vertex vertex) const {
  std::pair<CallTargets::const_iterator, CallTargets::const_iterator> pair;
  pair.first = call_targets_.begin() + graph_[vertex].call_target_start_;
  pair.second =
      vertex + 1 != GetBasicBlockCount() && pair.first != call_targets_.end()
          ? call_targets_.begin() + graph_[vertex + 1].call_target_start_
          : call_targets_.end();
  return pair;
}

int FlowGraph::GetCallCount(Vertex vertex) const {
  const std::pair<CallTargets::const_iterator, CallTargets::const_iterator>
      pair(GetCallTargets(vertex));
  return pair.second - pair.first;
}

std::pair<Instructions::const_iterator, Instructions::const_iterator>
FlowGraph::GetInstructions(Vertex vertex) const {
  std::pair<Instructions::const_iterator, Instructions::const_iterator> pair;
  pair.first = instructions_.begin() + graph_[vertex].instruction_start_;
  pair.second =
      vertex + 1 != GetBasicBlockCount()
          ? instructions_.begin() + graph_[vertex + 1].instruction_start_
          : instructions_.end();
  return pair;
}

int FlowGraph::GetInstructionCount(Vertex vertex) const {
  const auto instructions(GetInstructions(vertex));
  return std::distance(instructions.first, instructions.second);
}

int FlowGraph::GetInstructionCount() const {
  int count = 0;
  for (auto vertices = boost::vertices(graph_);
       vertices.first != vertices.second; ++vertices.first) {
    count += GetInstructionCount(*vertices.first);
  }
  return count;
}

size_t FlowGraph::GetBasicBlockCount() const { return num_vertices(graph_); }

BasicBlockFixedPoint* FlowGraph::GetFixedPoint(Vertex vertex) const {
  return graph_[vertex].fixed_point_;
}

void FlowGraph::SetFixedPoint(FixedPoint* fixed_point) {
  fixed_point_ = fixed_point;
}

void FlowGraph::SetFixedPoint(Vertex vertex,
                              BasicBlockFixedPoint* fixed_point) {
  graph_[vertex].fixed_point_ = fixed_point;
}

FlowGraph::Vertex FlowGraph::GetVertex(Address address) const {
  Vertex first = 0;
  Vertex last = boost::num_vertices(graph_);
  Vertex count = last;
  while (count > 0) {
    Vertex count2 = count / 2;
    Vertex mid = first + count2;
    if (GetAddress(mid) < address) {
      first = ++mid;
      count -= count2 + 1;
    } else {
      count = count2;
    }
  }

  CHECK(first != last && GetAddress(first) == address)
#ifdef GOOGLE
      << "Invalid flow graph address " << FormatAddress(address);
#endif
  ;
  return first;
}

FlowGraph::Level FlowGraph::GetLevelForCallAddress(Address address) const {
  Level min_level =
      std::make_pair(std::numeric_limits<Level::first_type>::max(),
                     std::numeric_limits<Level::second_type>::max());

  AddressToLevelMap::const_iterator it = std::lower_bound(
      level_for_call_.begin(), level_for_call_.end(),
      std::make_pair(address, std::make_pair(uint16_t(0), uint16_t(0))),
      &SortByAddressLevel);
  // TODO(soerenme) Investigate this. The assert fails for the libssl fixture.
  //     This implies that the call graph and flow graphs somehow disagree, i.e.
  //     there is a child of this flow graph node in the call graph but we
  //     cannot find the corresponding call in the flow graph's instructions.
  //  assert(i != level_for_call_.end());
  for (; it != level_for_call_.end() && it->first == address; ++it) {
    if (it->second.first <= min_level.first ||
        (it->second.first == min_level.first &&
         it->second.second <= min_level.second)) {
      min_level = it->second;
    }
  }

  return min_level;
}

uint32_t FlowGraph::GetStringReferences(Vertex vertex) const {
  return graph_[vertex].string_hash_;
}

CallGraph* FlowGraph::GetCallGraph() const { return call_graph_; }

CallGraph::Vertex FlowGraph::GetCallGraphVertex() const {
  return call_graph_vertex_;
}

void FlowGraph::SetCallGraph(CallGraph* graph) {
  call_graph_ = graph;
  if (call_graph_) {
    call_graph_vertex_ = call_graph_->GetVertex(entry_point_address_);
  }
}

bool FlowGraph::IsLibrary() const {
  return call_graph_->IsLibrary(call_graph_vertex_);
}

void FlowGraph::ResetMatches() {
  VertexIterator i, end;
  for (boost::tie(i, end) = boost::vertices(graph_); i != end; ++i) {
    graph_[*i].fixed_point_ = 0;
  }

  SetFixedPoint(0);
}

uint32_t FlowGraph::GetHash() const { return byte_hash_; }

bool FlowGraph::IsTrivial() const { return GetMdIndex() == 0; }

uint32_t FlowGraph::GetStringReferences() const { return string_references_; }

}  // namespace bindiff
}  // namespace security
