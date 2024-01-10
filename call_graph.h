// Copyright 2011-2024 Google LLC
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

#ifndef CALL_GRAPH_H_
#define CALL_GRAPH_H_

#include <boost/graph/compressed_sparse_row_graph.hpp>  // NOLINT
#include <cstdint>
#include <limits>
#include <string>
#include <utility>

#include "third_party/absl/status/status.h"
#include "third_party/zynamics/bindiff/comment.h"
#include "third_party/zynamics/bindiff/graph_util.h"
#include "third_party/zynamics/binexport/binexport2.pb.h"
#include "third_party/zynamics/binexport/util/types.h"

namespace security::bindiff {

class FlowGraph;

class CallGraph {
 public:
  struct VertexInfo {
    Address address_ = 0;         // Function address
    std::string name_;            // Function name
    std::string demangled_name_;  // Only set iff different from name_
    uint32_t bfs_top_down_ = 0;   // Breadth-first-search level top down
    uint32_t bfs_bottom_up_ = 0;  // Breadth-first-search level bottom up
    uint32_t flags_ = 0;
    FlowGraph* flow_graph_ = nullptr;  // Flow graph (if loaded and attached)
  };

  struct EdgeInfo {
    uint32_t flags_ = 0;
    double md_index_proximity_ = -1.0;  // MD index proximity
    double md_index_top_down_ = 0.0;    // MD index top down
    double md_index_bottom_up_ = 0.0;   // MD index bottom up
  };

  using Graph = boost::compressed_sparse_row_graph<
      boost::bidirectionalS,  // we need to be able to iterate in/out edges
      VertexInfo,             // vertex properties
      EdgeInfo,               // edge properties
      boost::no_property,     // graph properties
      uint32_t,               // index type for vertices
      uint32_t>;              // index type for edges

  using Vertex = boost::graph_traits<Graph>::vertex_descriptor;
  using VertexIterator = boost::graph_traits<Graph>::vertex_iterator;
  using Edge = boost::graph_traits<Graph>::edge_descriptor;
  using EdgeIterator = boost::graph_traits<Graph>::edge_iterator;
  using OutEdgeIterator = boost::graph_traits<Graph>::out_edge_iterator;
  using InEdgeIterator = boost::graph_traits<Graph>::in_edge_iterator;

  enum {
    // Edge flags
    EDGE_DUPLICATE = 1
  };
  enum {
    // Vertex flags
    VERTEX_LIBRARY = 1 << 0,         // This is a library function.
    VERTEX_STUB = 1 << 1,            // Stub function, i.e. single jmp.
    VERTEX_NAME = 1 << 2,            // Function has a non-auto-generated name.
    VERTEX_DEMANGLED_NAME = 1 << 3,  // C++ demangled name.
  };

  // A constant denoting a non-existent vertex.
  static constexpr Vertex kInvalidVertex = std::numeric_limits<Vertex>::max();

  // Constructs an empty call graph.
  CallGraph() = default;

  virtual ~CallGraph() = default;

  // Reads and initializes the call graph from "proto". "filename" is passed in
  // and remembered for informational purposes only (we want to be able to
  // construct default save filenames with it for example).
  absl::Status Read(const BinExport2& proto, const std::string& filename);

  // Gets just the filename part (without path or extension) passed into Read().
  std::string GetFilename() const;

  // Gets the fully specified filename including path and extension as passed
  // into read().
  std::string GetFilePath() const { return filename_; }

  // Frees all associated data and resets to an initial (empty) state.
  void Reset();

  // Searches all vertices to return the one with "address" using Binary Search.
  // Returns kInvalidVertex if address couldn't be found.
  // Complexity: O(log n)
  Vertex GetVertex(Address address) const;

  // Returns the vertex' address. The argument needs to be a valid vertex.
  // Complexity: O(1)
  Address GetAddress(Vertex vertex) const;

  // Calculates and returns the edge's MD index.
  // Complexity: O(1)
  double GetMdIndex(const Edge& edge) const;

  // Return the (cached) MD index for the whole graph.
  // Complexity: O(1)
  double GetMdIndex() const;

  // Set the cached MD index for the whole graph.
  // Complexity: O(1)
  void SetMdIndex(double index);

  // Returns the MD index for a given vertex. Inverted MD index uses the bottom
  // up breadth first node level for calculating MD indices. This method is
  // quite expensive: it iterates it's in and out edges and calculates and sums
  // the edge MD indices.
  double GetMdIndex(Vertex vertex) const;
  double GetMdIndexInverted(Vertex vertex) const;

  // Performs two breadth first searches over the graph. Stores the resulting
  // BFS indices in vertices.
  // Complexity: O(|V| + |E|)
  void CalculateTopology();

  // Accesses the internal boost graph structure.
  Graph& GetGraph() { return graph_; }
  const Graph& GetGraph() const { return graph_; }

  // Associates the given flow graph with the corresponding call graph vertex.
  // The call graph will _not_ take ownership of the flow graph!
  void AttachFlowGraph(FlowGraph* flow_graph);
  void DetachFlowGraph(FlowGraph* flow_graph);
  // TODO(cblichmann): Remove!!!
  FlowGraph* GetFlowGraph(Address address) const;
  inline FlowGraph* GetFlowGraph(Vertex vertex) const {
    return graph_[vertex].flow_graph_;
  }

  // Returns true iff the edge is a circular/recursive edge, i.e. source and
  // target vertex are the same.
  bool IsCircular(const Edge& edge) const;

  // Returns true if this edge is parallel to another one. This happens quite
  // often in a call graph when one function calls another one multiple times.
  // We often don't care about all but the first call.
  void SetDuplicate(const Edge& edge, bool duplicate);
  inline bool IsDuplicate(const Edge& edge) const {
    return graph_[edge].flags_ & EDGE_DUPLICATE;
  }

  // Is this vertex a library function?
  bool IsLibrary(Vertex vertex) const;
  void SetLibrary(Vertex vertex, bool library);

  // Is this vertex a stub function? TODO(soerenme) define stub
  bool IsStub(Vertex vertex) const;
  void SetStub(Vertex vertex, bool stub);

  // Does this vertex have a real, i.e. user supplied, name? Binaries without
  // symbols often have auto generated names like "sub_BAADF00D".
  bool HasRealName(Vertex vertex) const;

  // Returns the pair<edge,true> if the edge could be found or
  // pair<Edge(),false> if it doesn't exist in the graph. Search is very
  // efficient with the source lookup happening in O(1) time and edge lookup
  // in O(num_of_outedges(source)).
  std::pair<Edge, bool> FindEdge(Vertex source, Vertex target) const;

  // Returns the name of the function "vertex". This is actually the only place
  // where this information is stored. The flow graphs themselves don't know
  // their own names.
  const std::string& GetName(Vertex vertex) const;
  void SetName(Vertex vertex, std::string name);
  const std::string& GetDemangledName(Vertex vertex) const;
  void SetDemangledName(Vertex vertex, std::string name);
  // Returns the demangled name if available, raw name otherwise.
  const std::string& GetGoodName(Vertex vertex) const;

  // Returns the name of the original input binary if known, else empty
  // std::string.
  const std::string& GetExeFilename() const { return exe_filename_; }
  void SetExeFilename(std::string name) { exe_filename_ = std::move(name); }

  // Returns the hex encoded hash (SHA256, SHA1 or obsolete MD5) of the original
  // input binary.
  const std::string& GetExeHash() const { return exe_hash_; }
  void SetExeHash(std::string hash);

  // Accesses comments. The call graph stores these globally even for operands
  // because we don't want to store them multiple times for shared basic blocks.
  CommentsByOperatorId& GetComments() { return comments_; }
  const CommentsByOperatorId& GetComments() const { return comments_; }

  // Reduces the graph to the immediate vicinity of "edge" and recalculates MD
  // indices on that subgraph. The idea is to become resilient against non-local
  // changes to the call graph.
  // Note: This function is potentially very computationally expensive.
  double GetProximityMdIndex(const Edge& edge);

  // Deletes the vertexes from the specified address range (inclusive!).
  // Expensive and buggy function (see notes in implementation). Try to avoid.
  void DeleteVertices(Address from, Address to);

 protected:
  void Init();
  double CalculateProximityMdIndex(Edge edge);

  Graph graph_;
  double md_index_ = 0.0;
  std::string exe_filename_;
  std::string exe_hash_;
  CommentsByOperatorId comments_;
  std::string filename_;
};

}  // namespace security::bindiff

#endif  // CALL_GRAPH_H_
