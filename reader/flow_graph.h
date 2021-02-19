// Copyright 2011-2021 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// A class to convert and store a BinExport::Flowgraph protocol buffer in a
// Boost graph.

#ifndef READER_FLOW_GRAPH_H_
#define READER_FLOW_GRAPH_H_

#include <boost/graph/adjacency_list.hpp>               //NOLINT
#include <boost/graph/compressed_sparse_row_graph.hpp>  // NOLINT
#include <cstdint>
#include <memory>
#include <utility>
#include <vector>

#include "base/macros.h"
#include "third_party/absl/types/optional.h"
#include "third_party/zynamics/binexport/architectures.h"
#include "third_party/zynamics/binexport/binexport2.pb.h"
#include "third_party/zynamics/binexport/reader/instruction.h"
#include "third_party/zynamics/binexport/types.h"

namespace security::binexport {

class FlowGraph {
 public:
  FlowGraph();

  FlowGraph(const FlowGraph&) = delete;
  FlowGraph& operator=(const FlowGraph&) = delete;

  // TODO(cblichmann): Future extension point: This should be a template
  //                   argument. BinDiff has VertexInfo with different fields.
  struct VertexProperty {
    VertexProperty() = default;

    // Start index of instructions in instruction vector.
    uint32_t instruction_start = std::numeric_limits<uint32_t>::max();
  };

  enum EdgeType {
    kEdgeUnconditional = 1 << 0,  // unconditional edge.
    kEdgeTrue = 1 << 1,           // conditional jump true case.
    kEdgeFalse = 1 << 2,          // conditional jump false case.
    kEdgeSwitch = 1 << 3,         // switch jump edge.
    kEdgeLoop = 1 << 4,           // loop back edge (as in Lengauer-Tarjan).
  };

  struct EdgeProperty {
    EdgeProperty() = default;

    uint32_t flags = 0;
  };

  using Graph = boost::compressed_sparse_row_graph<
      boost::bidirectionalS,  // Iterate in and out edges.
      VertexProperty,         // The information per vertex.
      EdgeProperty,           // The information per edge.
      boost::no_property,     // Use no graph properties.
      uint32_t,               // Index type for vertices.
      uint32_t>;              // Index type for edges.

  using Vertex = boost::graph_traits<Graph>::vertex_descriptor;
  using VertexIterator = boost::graph_traits<Graph>::vertex_iterator;
  using Edge = boost::graph_traits<Graph>::edge_descriptor;
  using EdgeIterator = boost::graph_traits<Graph>::edge_iterator;
  using OutEdgeIterator = boost::graph_traits<Graph>::out_edge_iterator;
  using InEdgeIterator = boost::graph_traits<Graph>::in_edge_iterator;
  using AdjacencyIterator = boost::graph_traits<Graph>::adjacency_iterator;

  using UndirectedGraph =
      boost::adjacency_list<boost::vecS, boost::vecS, boost::undirectedS,
                            VertexProperty, EdgeProperty>;

  // Factory method to read and initialize a flow graph (BinExport2).
  static std::unique_ptr<FlowGraph> FromBinExport2Proto(
      const BinExport2& proto, const BinExport2::FlowGraph& flow_graph_proto,
      const std::vector<uint64_t>& instruction_addresses);

  // Returns the graph of this flow graph.
  const Graph& graph() const { return graph_; }

  // Returns the entry point address of the flow graph.
  const Address& entry_point_address() const { return entry_point_address_; }

  // Returns the start address of the given vertex.
  Address GetAddress(Vertex vertex) const;

  // Returns the instructions for the given vertex.
  std::pair<Instructions::const_iterator, Instructions::const_iterator>
  GetInstructions(Vertex vertex) const;

  // Computes the call targets for the given vertex.
  template <typename OutputIterator>
  void GetCallTargets(Vertex vertex, OutputIterator call_targets) const;

  // Returns true if vertex is an exit node from a function. This usually
  // happens when block doesn't have any outgoing flow edges, but there are
  // exceptions - like when block ends with unrecognised jump to register.
  bool IsExitNode(Vertex vertex) const;

  // Returns the number of vertices in this flow graph.
  size_t GetVertexCount() const;

  // Returns the number of edges of this flow graph.
  size_t GetEdgeCount() const;

  // Returns the number of instruction in this flow graph.
  size_t GetInstructionCount() const;

  // Returns the instructions in this flow graph.
  const Instructions& instructions() const { return instructions_; }

  // Returns the vertex with the given address.
  Vertex GetVertex(Address address) const;

 private:
  // Boost graph representing the structure of the flow graph.
  Graph graph_;

  // Entry point address for this flow graph. Each flow graph only has a single
  // entry point.
  Address entry_point_address_;

  // Instructions of this flow graph. Instructions are stored for the whole flow
  // graph rather than keeping a list for each graph vertex. To access
  // instructions for a single vertex use the FlowGraph::GetInstructions(Vertex
  // vertex) function.
  Instructions instructions_;

  // Architecture of instructions in this flow graph.
  absl::optional<Architecture> architecture_;
};

template <typename OutputIterator>
void FlowGraph::GetCallTargets(Vertex vertex,
                               OutputIterator call_targets) const {
  Instructions::const_iterator it;
  Instructions::const_iterator end;
  for (std::tie(it, end) = GetInstructions(vertex); it != end; ++it) {
    for (const auto& target : it->call_targets()) {
      *call_targets++ = target;
    }
  }
}

}  // namespace security::binexport

#endif  // READER_FLOW_GRAPH_H_
