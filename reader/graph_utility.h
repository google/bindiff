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

// Utility class for graph related algorithms.

#ifndef READER_GRAPH_UTILITY_H_
#define READER_GRAPH_UTILITY_H_

#include <boost/graph/compressed_sparse_row_graph.hpp>  // NOLINT
#include <cstdint>
#include <limits>

#include "third_party/zynamics/binexport/types.h"

namespace security::binexport {

struct EdgeDegrees {
  uint32_t source_in_degree;
  uint32_t source_out_degree;
  uint32_t target_in_degree;
  uint32_t target_out_degree;
};

template <typename T>
struct VertexTypeTraits {};

// This is the specialization for the types FlowGraph::Vertex and
// CallGraph::Vertex. Both of them are just a typedef and therefore no real type
// in the type system. They are currently of type uint32. This is safe as when
// underlying code changes there is no specialization for this type in place.
template <>
struct VertexTypeTraits<uint32_t> {
  static constexpr auto kInvalidVertex = std::numeric_limits<uint32_t>::max();
};

template <typename Vertex>
bool IsValidVertex(const Vertex& vertex) {
  return vertex != VertexTypeTraits<Vertex>::kInvalidVertex;
}

// Gets a Graph::Vertex by its address. Complexity of the search is O(log(n)).
// This code is heavily borrowed from std::lower_bound. But using it would
// require a comparator which keeps state.
template <typename Graph>
typename Graph::Vertex GetVertex(const Graph& graph, Address address) {
  typename Graph::Vertex first = 0;
  typename Graph::Vertex last = boost::num_vertices(graph.graph());
  typename Graph::Vertex count = last;
  while (count > 0) {
    typename Graph::Vertex count2 = count / 2;
    typename Graph::Vertex mid = first + count2;
    if (graph.GetAddress(mid) < address) {
      first = ++mid;
      count -= count2 + 1;
    } else {
      count = count2;
    }
  }

  if ((first != last) && (graph.GetAddress(first) == address)) {
    return first;
  }
  return VertexTypeTraits<typename Graph::Vertex>::kInvalidVertex;
}

// Collects the in and out degree of the edges source and target vertex, and
// passes them as EdgeDegree struct to the caller.
template <typename Graph>
EdgeDegrees GetEdgeDegrees(const typename Graph::Graph& graph,
                           const typename Graph::Edge& edge) {
  typename Graph::Vertex source = boost::source(edge, graph);
  typename Graph::Vertex target = boost::target(edge, graph);
  EdgeDegrees edge_vector{
      boost::in_degree(source, graph), boost::out_degree(source, graph),
      boost::in_degree(target, graph), boost::out_degree(target, graph)};
  return edge_vector;
}

}  // namespace security::binexport

#endif  // READER_GRAPH_UTILITY_H_
