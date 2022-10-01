// Copyright 2011-2022 Google LLC
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

#include "third_party/zynamics/binexport/reader/flow_graph.h"

#include <algorithm>
#include <boost/graph/graph_traits.hpp>  // NOLINT
#include <cstdint>
#include <string>
#include <vector>

#include "third_party/absl/log/check.h"
#include "third_party/absl/log/log.h"
#include "third_party/absl/memory/memory.h"
#include "third_party/zynamics/binexport/reader/graph_utility.h"
#include "third_party/zynamics/binexport/util/types.h"

namespace security::binexport {
namespace {

absl::optional<Architecture> GetSupportedArchitecture(const BinExport2& proto) {
  const std::string& architecture =
      proto.meta_information().architecture_name();
  if (architecture == "arm") {
    return Architecture::kArm;
  }
  if (architecture == "aarch64") {
    return Architecture::kAArch64;
  }
  if (architecture == "dex") {
    return Architecture::kDex;
  }
  if (architecture == "msil") {
    return Architecture::kMsil;
  }
  if (architecture == "x86-32") {
    return Architecture::kX86Arch32;
  }
  if (architecture == "x86-64") {
    return Architecture::kX86Arch64;
  }
  return absl::nullopt;
}

uint32_t GetEdgeTypeFromProto(BinExport2::FlowGraph::Edge::Type type) {
  switch (type) {
    case BinExport2::FlowGraph::Edge::CONDITION_TRUE:
      return FlowGraph::kEdgeTrue;
    case BinExport2::FlowGraph::Edge::CONDITION_FALSE:
      return FlowGraph::kEdgeFalse;
    case BinExport2::FlowGraph::Edge::UNCONDITIONAL:
      return FlowGraph::kEdgeUnconditional;
    case BinExport2::FlowGraph::Edge::SWITCH:
      return FlowGraph::kEdgeSwitch;
    default:
      LOG(QFATAL) << "Invalid edge type: " << type;
      return FlowGraph::kEdgeUnconditional;  // Not reached
  }
}

void EdgesFromEdgeProto(
    const BinExport2& proto, const BinExport2::FlowGraph& flow_graph_proto,
    const std::vector<Address>& addresses,
    const std::vector<uint64_t>& instruction_addresses,
    std::vector<std::pair<FlowGraph::Graph::edges_size_type,
                          FlowGraph::Graph::edges_size_type>>* edges,
    std::vector<FlowGraph::EdgeProperty>* edge_properties) {
  for (const auto& edge : flow_graph_proto.edge()) {
    const Address source_address =
        instruction_addresses[proto.basic_block(edge.source_basic_block_index())
                                  .instruction_index(0)
                                  .begin_index()];
    const Address target_address =
        instruction_addresses[proto.basic_block(edge.target_basic_block_index())
                                  .instruction_index(0)
                                  .begin_index()];
    const auto source =
        std::lower_bound(addresses.begin(), addresses.end(), source_address);
    const auto target =
        std::lower_bound(addresses.begin(), addresses.end(), target_address);
    if (source != addresses.end() && target != addresses.end()) {
      edges->emplace_back(source - addresses.begin(),
                          target - addresses.begin());
      FlowGraph::EdgeProperty edge_property;
      edge_property.flags |= GetEdgeTypeFromProto(edge.type());
      if (edge.is_back_edge()) {
        edge_property.flags |= FlowGraph::kEdgeLoop;
      }
      edge_properties->push_back(edge_property);
    }
  }
}

void AssignVertexProperties(
    const std::vector<FlowGraph::VertexProperty>& vertex_properties,
    FlowGraph::Graph* graph) {
  FlowGraph::VertexIterator vertices_it;
  FlowGraph::VertexIterator vertices_end;
  FlowGraph::Graph& graph_ref = *graph;
  int j = 0;
  for (boost::tie(vertices_it, vertices_end) = boost::vertices(*graph);
       vertices_it != vertices_end; ++vertices_it, ++j) {
    graph_ref[*vertices_it] = vertex_properties[j];
  }
}

void AssignEdgeProperties(
    const std::vector<FlowGraph::EdgeProperty>& edge_properties,
    FlowGraph::Graph* graph) {
  FlowGraph::EdgeIterator edges_it;
  FlowGraph::EdgeIterator edges_end;
  FlowGraph::Graph& graph_ref = *graph;
  int j = 0;
  for (boost::tie(edges_it, edges_end) = boost::edges(*graph);
       edges_it != edges_end; ++edges_it, ++j) {
    graph_ref[*edges_it] = edge_properties[j];
  }
}

}  // namespace

FlowGraph::FlowGraph() : entry_point_address_(0) {}

FlowGraph::Vertex FlowGraph::GetVertex(Address address) const {
  return security::binexport::GetVertex(*this, address);
}

std::unique_ptr<FlowGraph> FlowGraph::FromBinExport2Proto(
    const BinExport2& proto, const BinExport2::FlowGraph& flow_graph_proto,
    const std::vector<Address>& instruction_addresses) {
  auto flow_graph = absl::make_unique<FlowGraph>();
  int entry_instruction_index =
      proto.basic_block(flow_graph_proto.entry_basic_block_index())
          .instruction_index(0)
          .begin_index();
  flow_graph->entry_point_address_ =
      instruction_addresses[entry_instruction_index];

  std::vector<VertexProperty> vertices;
  vertices.reserve(flow_graph_proto.basic_block_index_size());
  std::vector<Address> addresses;
  addresses.reserve(flow_graph_proto.basic_block_index_size());

  for (int basic_block_index : flow_graph_proto.basic_block_index()) {
    const BinExport2::BasicBlock& basic_block_proto(
        proto.basic_block(basic_block_index));
    VertexProperty vertex_property;
    vertex_property.instruction_start = flow_graph->instructions_.size();
    QCHECK(basic_block_proto.instruction_index_size());
    for (const auto& instruction_interval :
         basic_block_proto.instruction_index()) {
      const int instruction_end_index(instruction_interval.has_end_index()
                                          ? instruction_interval.end_index()
                                          : instruction_interval.begin_index() +
                                                1);
      for (int instruction_index = instruction_interval.begin_index();
           instruction_index < instruction_end_index; ++instruction_index) {
        const auto& instruction_proto(proto.instruction(instruction_index));
        Address instruction_address = instruction_addresses[instruction_index];
        const std::string& mnemonic(
            proto.mnemonic(instruction_proto.mnemonic_index()).name());
        flow_graph->instructions_.emplace_back(instruction_address, mnemonic);

        auto& instruction = flow_graph->instructions_.back();
        instruction.set_index(instruction_index);
        instruction.set_operands({instruction_proto.operand_index().begin(),
                                  instruction_proto.operand_index().end()});
        instruction.set_call_targets({instruction_proto.call_target().begin(),
                                      instruction_proto.call_target().end()});
        instruction.set_comment_indices(
            {instruction_proto.comment_index().begin(),
             instruction_proto.comment_index().end()});
      }
    }
    addresses.push_back(
        flow_graph->instructions_[vertex_property.instruction_start].address());
    vertices.push_back(vertex_property);
  }

  CHECK(std::is_sorted(addresses.begin(), addresses.end()))
      << "Flow graph nodes not sorted by address.";

  std::vector<std::pair<Graph::edges_size_type, Graph::edges_size_type>> edges;
  edges.reserve(flow_graph_proto.edge_size());
  std::vector<EdgeProperty> edge_properties;
  edge_properties.reserve(flow_graph_proto.edge_size());
  EdgesFromEdgeProto(proto, flow_graph_proto, addresses, instruction_addresses,
                     &edges, &edge_properties);
  flow_graph->graph_ = Graph(boost::edges_are_unsorted_multi_pass,
                             edges.begin(), edges.end(), addresses.size());
  flow_graph->architecture_ = GetSupportedArchitecture(proto);
  AssignVertexProperties(vertices, &flow_graph->graph_);
  AssignEdgeProperties(edge_properties, &flow_graph->graph_);
  return flow_graph;
}

size_t FlowGraph::GetVertexCount() const { return num_vertices(graph_); }
size_t FlowGraph::GetEdgeCount() const { return num_edges(graph_); }
size_t FlowGraph::GetInstructionCount() const { return instructions_.size(); }

Address FlowGraph::GetAddress(Vertex vertex) const {
  return GetInstructions(vertex).first->address();
}

std::pair<Instructions::const_iterator, Instructions::const_iterator>
FlowGraph::GetInstructions(Vertex vertex) const {
  return {instructions_.begin() + graph_[vertex].instruction_start,
          GetVertexCount() != vertex + 1
              ? instructions_.begin() + graph_[vertex + 1].instruction_start
              : instructions_.end()};
}

bool FlowGraph::IsExitNode(Vertex vertex) const {
  if (boost::out_degree(vertex, graph_)) {
    // Basic block has outgoing flow edges - it cannot possibly be an exit node.
    return false;
  }

  Instructions::const_iterator instructions_it, instructions_end;
  boost::tie(instructions_it, instructions_end) = GetInstructions(vertex);
  if (instructions_it == instructions_end) {
    // Basic block doesn't have any instructions.
    return true;
  }

  // TODO(b/114701180) - if last instruction is call to a known function, this
  // is an exit node (optimized tail call pattern).

  // If last instruction is a jump to unknown location, this is probably
  // unrecognized switch pattern (or similar) - return false. Otherwise true.
  const Instruction& last_instruction = *(--instructions_end);
  return !IsJumpInstruction(last_instruction, architecture_);
}

}  // namespace security::binexport
