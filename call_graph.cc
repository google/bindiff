// Copyright 2011-2023 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#include "third_party/zynamics/bindiff/call_graph.h"

#include <cassert>

#include "third_party/absl/log/log.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/binexport/util/filesystem.h"
#include "third_party/zynamics/binexport/util/format.h"

namespace security::bindiff {

using binexport::FormatAddress;
using binexport::FormatFunctionName;

namespace {

bool SortEdgeByMdIndex(const std::pair<CallGraph::Edge, double>& one,
                       const std::pair<CallGraph::Edge, double>& two) {
  return one.second < two.second;
}

}  // namespace

std::string CallGraph::GetFilename() const {
  absl::string_view basename = filename_;
  if (auto last_slash = basename.find_last_of(R"(\/)");
      last_slash != absl::string_view::npos) {
    basename.remove_prefix(last_slash + 1);
  }
  return ReplaceFileExtension(basename, /*new_extension=*/"");
}

void CallGraph::Reset() {
  graph_.clear();
  md_index_ = 0.0;
  exe_filename_ = "";
  exe_hash_ = "";
  comments_.clear();
  filename_ = "";
}

void CallGraph::Init() {
  for (auto [it, end] = boost::edges(graph_); it != end; ++it) {
    if (IsDuplicate(*it)) {
      continue;
    }

    const Vertex source = boost::source(*it, graph_);
    const Vertex target = boost::target(*it, graph_);
    for (auto [edge_it, edge_end] = boost::out_edges(source, graph_);
         edge_it != edge_end; ++edge_it) {
      if (*edge_it != *it && boost::target(*edge_it, graph_) == target) {
        SetDuplicate(*edge_it, true);
      }
    }
  }

  CalculateTopology();
  SetMdIndex(CalculateMdIndex(*this));
  CalculateMdIndex(*this, true);  // Needs to be here to update edge properties.
}

void CallGraph::SetExeHash(std::string hash) {
  // The executable hash is used for display purposes only, so we do not check
  // it for validity here.
  exe_hash_ = std::move(hash);
}

absl::Status CallGraph::Read(const BinExport2& proto,
                             const std::string& filename) {
  filename_ = filename;
  std::replace(filename_.begin(), filename_.end(), '\\', '/');

  const auto& meta = proto.meta_information();
  exe_hash_ = meta.executable_id();
  exe_filename_ = meta.executable_name();

  const auto& call_graph = proto.call_graph();
  std::vector<VertexInfo> temp_vertices(call_graph.vertex_size());
  std::vector<Address> temp_addresses(call_graph.vertex_size());
  Address last_address = 0;
  for (int i = 0; i < call_graph.vertex_size(); ++i) {
    const auto& proto_vertex = call_graph.vertex(i);
    VertexInfo& vertex = temp_vertices[i];
    vertex.address_ = proto_vertex.address();

    if (vertex.address_ < last_address) {
      return absl::FailedPreconditionError(absl::StrCat(
          "Call graph nodes not sorted: ", FormatAddress(vertex.address_),
          " >= ", FormatAddress(last_address)));
    }
    last_address = vertex.address_;

    temp_addresses[i] = vertex.address_;
    if (proto_vertex.has_mangled_name()) {
      vertex.flags_ |= VERTEX_NAME;
      vertex.name_ = proto_vertex.mangled_name();
    }
    if (proto_vertex.has_demangled_name()) {
      assert(proto_vertex.has_mangled_name());
      vertex.flags_ |= VERTEX_NAME;
      vertex.flags_ |= VERTEX_DEMANGLED_NAME;
      vertex.demangled_name_ = proto_vertex.demangled_name();
    }
    if (!(vertex.flags_ & VERTEX_NAME)) {
      // Provide a dummy name for display.
      vertex.name_ = FormatFunctionName(vertex.address_);
    }

    if (proto_vertex.type() == BinExport2::CallGraph::Vertex::LIBRARY) {
      vertex.flags_ |= VERTEX_LIBRARY;
    } else if (proto_vertex.type() == BinExport2::CallGraph::Vertex::THUNK) {
      vertex.flags_ |= VERTEX_STUB;
    }
  }

  std::vector<std::pair<Graph::edges_size_type, Graph::edges_size_type>> edges;
  edges.reserve(call_graph.edge_size());
  std::vector<EdgeInfo> properties(call_graph.edge_size());
  for (int i = 0; i < call_graph.edge_size(); ++i) {
    const Address source_address =
        call_graph.vertex(call_graph.edge(i).source_vertex_index()).address();
    const Address target_address =
        call_graph.vertex(call_graph.edge(i).target_vertex_index()).address();
    const auto source = std::lower_bound(temp_addresses.begin(),
                                         temp_addresses.end(), source_address);
    const auto target = std::lower_bound(temp_addresses.begin(),
                                         temp_addresses.end(), target_address);
    if (source != temp_addresses.end() && target != temp_addresses.end() &&
        *source == source_address && *target == target_address) {
      edges.push_back(std::make_pair(source - temp_addresses.begin(),
                                     target - temp_addresses.begin()));
    }
  }

  Graph temp_graph(boost::edges_are_unsorted_multi_pass, edges.begin(),
                   edges.end(), properties.begin(), call_graph.vertex_size());
  std::swap(graph_, temp_graph);

  int j = 0;
  for (auto [it, end] = boost::vertices(graph_); it != end; ++it, ++j) {
    graph_[*it] = temp_vertices[j];
  }
  Init();
  return absl::OkStatus();
}

void CallGraph::AttachFlowGraph(FlowGraph* flow_graph) {
  if (!flow_graph) {
    throw std::runtime_error(
        "AttachFlowGraph: invalid flow graph (null pointer)");
  }

  auto entry_point_address = flow_graph->GetEntryPointAddress();
  auto vertex = GetVertex(entry_point_address);
  if (vertex == kInvalidVertex) {
    throw std::runtime_error(absl::StrCat(
        "AttachFlowGraph: couldn't find call graph node for flow graph ",
        FormatAddress(entry_point_address)));
  }

  if (graph_[vertex].flow_graph_ != nullptr) {
    throw std::runtime_error(
        absl::StrCat("AttachFlowGraph: flow graph already attached ",
                     FormatAddress(entry_point_address)));
  }

  graph_[vertex].flow_graph_ = flow_graph;
  flow_graph->SetCallGraph(this);
}

void CallGraph::DetachFlowGraph(FlowGraph* flow_graph) {
  if (!flow_graph || flow_graph->GetCallGraph() != this) {
    throw std::runtime_error("DetachFlowGraph: invalid graph");
  }

  auto entry_point_address = flow_graph->GetEntryPointAddress();
  auto vertex = GetVertex(entry_point_address);
  if (vertex == kInvalidVertex) {
    LOG(INFO) << absl::StrCat(
        "DetachFlowGraph: couldn't find call graph node for flow graph ",
        FormatAddress(entry_point_address));
  } else {
    graph_[vertex].flow_graph_ = nullptr;
  }
  flow_graph->SetCallGraph(nullptr);
}

CallGraph::Vertex CallGraph::GetVertex(Address address) const {
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

  if ((first != last) && (GetAddress(first) == address)) {
    return first;
  }
  return kInvalidVertex;
}

Address CallGraph::GetAddress(Vertex vertex) const {
  return graph_[vertex].address_;
}

void CallGraph::SetMdIndex(double index) { md_index_ = index; }

double CallGraph::GetMdIndex() const { return md_index_; }

double CallGraph::GetMdIndex(const Edge& edge) const {
  const Vertex source = boost::source(edge, graph_);
  const double in_degree_source = boost::in_degree(source, graph_);
  const double out_degree_source = boost::out_degree(source, graph_);
  const double level_source = graph_[source].bfs_top_down_;
  const Vertex target = boost::target(edge, graph_);
  const double in_degree_target = boost::in_degree(target, graph_);
  const double out_degree_target = boost::out_degree(target, graph_);
  const double level_target = graph_[target].bfs_top_down_;
  const double md_index =
      sqrt(2.0) * in_degree_source + sqrt(3.0) * out_degree_source +
      sqrt(5.0) * in_degree_target + sqrt(7.0) * out_degree_target +
      sqrt(11.0) * level_source + sqrt(13.0) * level_target;
  return md_index ? 1.0 / md_index : 0.0;
}

FlowGraph* CallGraph::GetFlowGraph(Address address) const {
  return GetFlowGraph(GetVertex(address));
}

bool CallGraph::IsLibrary(Vertex vertex) const {
  return graph_[vertex].flags_ & VERTEX_LIBRARY;
}

void CallGraph::SetLibrary(Vertex vertex, bool library) {
  uint32_t flags = graph_[vertex].flags_;
  if (library) {
    flags |= VERTEX_LIBRARY;
  } else {
    flags &= ~VERTEX_LIBRARY;
  }
  graph_[vertex].flags_ = flags;
}

bool CallGraph::HasRealName(Vertex vertex) const {
  return (graph_[vertex].flags_ & VERTEX_NAME) == VERTEX_NAME;
}

bool CallGraph::IsStub(Vertex vertex) const {
  return graph_[vertex].flags_ & VERTEX_STUB;
}

void CallGraph::SetStub(Vertex vertex, bool stub) {
  uint32_t flags = graph_[vertex].flags_;
  if (stub) {
    flags |= VERTEX_STUB;
  } else {
    flags &= ~VERTEX_STUB;
  }
  graph_[vertex].flags_ = flags;
}

void CallGraph::SetDuplicate(const Edge& edge, bool duplicate) {
  uint32_t flags = graph_[edge].flags_;
  if (duplicate) {
    flags |= EDGE_DUPLICATE;
  } else {
    flags &= ~EDGE_DUPLICATE;
  }
  graph_[edge].flags_ = flags;
}

std::pair<CallGraph::Edge, bool> CallGraph::FindEdge(Vertex source,
                                                     Vertex target) const {
  for (auto [edge_it, edge_it_end] = boost::out_edges(source, graph_);
       edge_it != edge_it_end; ++edge_it) {
    if (boost::target(*edge_it, graph_) == target) {
      return std::make_pair(*edge_it, true);
    }
  }
  return std::make_pair(Edge(), false);
}

void CallGraph::CalculateTopology() {
  BreadthFirstSearch(&graph_);
  InvertedBreadthFirstSearch(&graph_);
}

const std::string& CallGraph::GetName(Vertex vertex) const {
  return graph_[vertex].name_;
}

void CallGraph::SetName(Vertex vertex, std::string name) {
  graph_[vertex].name_ = std::move(name);
}

const std::string& CallGraph::GetDemangledName(Vertex vertex) const {
  return graph_[vertex].demangled_name_;
}

void CallGraph::SetDemangledName(Vertex vertex, std::string name) {
  if (name.empty()) {
    graph_[vertex].flags_ &= ~VERTEX_DEMANGLED_NAME;
  } else {
    graph_[vertex].flags_ |= VERTEX_DEMANGLED_NAME;
  }
  graph_[vertex].demangled_name_ = std::move(name);
}

const std::string& CallGraph::GetGoodName(Vertex vertex) const {
  if (graph_[vertex].flags_ & VERTEX_DEMANGLED_NAME) {
    return graph_[vertex].demangled_name_;
  }
  return graph_[vertex].name_;
}

bool CallGraph::IsCircular(const Edge& edge) const {
  return boost::source(edge, graph_) == boost::target(edge, graph_);
}

// MD index for a vertex is defined as the sum of all edge MD indices
// for that vertex.
double CallGraph::GetMdIndex(Vertex vertex) const {
  const Graph& graph = GetGraph();
  std::vector<double> md_indices(boost::in_degree(vertex, graph) +
                                 boost::out_degree(vertex, graph));
  size_t index = 0;
  for (auto [it, end] = boost::in_edges(vertex, graph); it != end;
       ++it, ++index) {
    md_indices[index] = graph_[*it].md_index_top_down_;
  }
  for (auto [it, end] = boost::out_edges(vertex, graph); it != end;
       ++it, ++index) {
    md_indices[index] = graph_[*it].md_index_top_down_;
  }

  // Summation is not commutative for doubles.
  std::sort(md_indices.begin(), md_indices.end());
  return std::accumulate(md_indices.begin(), md_indices.end(), 0.0);
}

double CallGraph::GetMdIndexInverted(Vertex vertex) const {
  const Graph& graph = GetGraph();
  std::vector<double> md_indices(boost::in_degree(vertex, graph) +
                                 boost::out_degree(vertex, graph));
  size_t index = 0;
  for (auto [it, end] = boost::in_edges(vertex, graph); it != end;
       ++it, ++index) {
    md_indices[index] = graph_[*it].md_index_bottom_up_;
  }
  for (auto [it, end] = boost::out_edges(vertex, graph); it != end;
       ++it, ++index) {
    md_indices[index] = graph_[*it].md_index_bottom_up_;
  }

  // Summation is not commutative for doubles.
  std::sort(md_indices.begin(), md_indices.end());
  return std::accumulate(md_indices.begin(), md_indices.end(), 0.0);
}

struct NeighborInfo {
  explicit NeighborInfo(CallGraph::Vertex vertex)
      : vertex_(vertex), in_degree_(0), out_degree_(0) {}

  bool operator==(const NeighborInfo& rhs) const {
    return vertex_ == rhs.vertex_;
  }

  CallGraph::Vertex vertex_;
  size_t in_degree_;
  size_t out_degree_;
};

bool operator<(const NeighborInfo& one, const NeighborInfo& two) {
  return one.vertex_ < two.vertex_;
}

// TODO(soerenme): Very bad worst case behavior in high connectivity graphs!
double CallGraph::CalculateProximityMdIndex(Edge edge) {
  std::vector<NeighborInfo> neighbors;
  // Collect all nodes with a distance less than or equal to one.
  const Vertex source = boost::source(edge, graph_);
  const Vertex target = boost::target(edge, graph_);
  {
    for (auto [it, end] = boost::in_edges(source, graph_); it != end; ++it) {
      neighbors.emplace_back(boost::source(*it, graph_));
    }
    for (auto [it, end] = boost::in_edges(target, graph_); it != end; ++it) {
      neighbors.emplace_back(boost::source(*it, graph_));
    }
    for (auto [it, end] = boost::out_edges(source, graph_); it != end; ++it) {
      neighbors.push_back(NeighborInfo(boost::target(*it, graph_)));
    }
    for (auto [it, end] = boost::out_edges(target, graph_); it != end; ++it) {
      neighbors.push_back(NeighborInfo(boost::target(*it, graph_)));
    }
  }
  std::sort(neighbors.begin(), neighbors.end());
  neighbors.erase(std::unique(neighbors.begin(), neighbors.end()),
                  neighbors.end());

  // Compute truncated in-degree and out-degree for each neighbor.
  std::vector<std::pair<Edge, double>> edges;
  for (auto& neighbor : neighbors) {
    {
      InEdgeIterator i, end;
      for (auto [i, end] = boost::in_edges(neighbor.vertex_, graph_); i != end;
           ++i) {
        const Vertex source = boost::source(*i, graph_);
        if (std::binary_search(neighbors.begin(), neighbors.end(),
                               NeighborInfo(source))) {
          ++neighbor.in_degree_;
          if (!IsDuplicate(*i)) {
            edges.push_back(std::make_pair(*i, 0.0));
          }
        }
      }
    }
    {
      OutEdgeIterator i, end;
      for (auto [i, end] = boost::out_edges(neighbor.vertex_, graph_); i != end;
           ++i) {
        const Vertex target = boost::target(*i, graph_);
        if (std::binary_search(neighbors.begin(), neighbors.end(),
                               NeighborInfo(target))) {
          ++neighbor.out_degree_;
          if (!IsDuplicate(*i)) {
            edges.push_back(std::make_pair(*i, 0.0));
          }
        }
      }
    }
  }
  std::sort(edges.begin(), edges.end());
  edges.erase(std::unique(edges.begin(), edges.end()), edges.end());

  // Collect all truncated MD indices.
  for (auto& edge : edges) {
    const auto source =
        std::lower_bound(neighbors.begin(), neighbors.end(),
                         NeighborInfo(boost::source(edge.first, graph_)));
    const auto target =
        std::lower_bound(neighbors.begin(), neighbors.end(),
                         NeighborInfo(boost::target(edge.first, graph_)));
    edge.second =
        sqrt(2.0) * source->in_degree_ + sqrt(3.0) * source->out_degree_ +
        sqrt(5.0) * target->in_degree_ + sqrt(7.0) * target->out_degree_;
    edge.second = edge.second ? 1.0 / edge.second : 0.0;
  }

  std::sort(edges.begin(), edges.end(), &SortEdgeByMdIndex);
  double md_index = 0;
  for (const auto& edge : edges) {
    md_index += edge.second;
  }
  return md_index;
}

// Proxy MD index is lazy: we don't calculate it until we call this function
// -> thus it may potentially be quite expensive.
double CallGraph::GetProximityMdIndex(const Edge& edge) {
  double index = graph_[edge].md_index_proximity_;
  if (index < 0) {
    index = CalculateProximityMdIndex(edge);
    graph_[edge].md_index_proximity_ = index;
  }
  return index;
}

void CallGraph::DeleteVertices(Address from, Address to) {
  if (boost::num_vertices(graph_) == 0 ||
      (GetAddress(0) >= from &&
       GetAddress(boost::num_vertices(graph_) - 1) <= to)) {
    // nothing to do
    return;
  }

  std::vector<VertexInfo> temp_vertices;
  std::vector<Address> temp_addresses;
  for (auto [it, end] = boost::vertices(graph_); it != end; ++it) {
    const Address address = GetAddress(*it);
    if (address >= from && address <= to) {
      // The BFS and MD indices aren't correct after copying - however, the
      // original indices may still be what we want for correct matching?
      temp_vertices.push_back(graph_[*it]);
      temp_addresses.push_back(address);
    }
  }

  std::vector<std::pair<Graph::edges_size_type, Graph::edges_size_type>> edges;
  std::vector<EdgeInfo> properties;
  for (auto [it, end] = boost::edges(graph_); it != end; ++it) {
    const Address source_address = GetAddress(boost::source(*it, graph_));
    const Address target_address = GetAddress(boost::target(*it, graph_));
    const auto source = std::lower_bound(temp_addresses.begin(),
                                         temp_addresses.end(), source_address);
    const auto target = std::lower_bound(temp_addresses.begin(),
                                         temp_addresses.end(), target_address);
    if (source != temp_addresses.end() && *source == source_address &&
        target != temp_addresses.end() && *target == target_address) {
      edges.push_back(std::make_pair(source - temp_addresses.begin(),
                                     target - temp_addresses.begin()));
      properties.push_back(graph_[*it]);
    }
  }

  Graph newGraph(boost::edges_are_unsorted_multi_pass, edges.begin(),
                 edges.end(), properties.begin(), temp_addresses.size());
  std::swap(graph_, newGraph);

  int j = 0;
  for (auto [it, end] = boost::vertices(graph_); it != end; ++it, ++j) {
    graph_[*it] = temp_vertices[j];
  }

  // TODO(cblichmann): Bug: comments_ contains orphans now.
  // TODO(cblichmann): Bug: IsDuplicate may contain wrong information for edges.
}

}  // namespace security::bindiff
