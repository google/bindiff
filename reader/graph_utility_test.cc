// Copyright 2011-2020 Google LLC
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

#include "third_party/zynamics/binexport/reader/graph_utility.h"

#include <utility>
#include <vector>

#include <boost/graph/adjacency_list.hpp>  // NOLINT

#include "gmock/gmock.h"
#include "gtest/gtest.h"

namespace security::binexport {

class GraphUtilityTest : public testing::Test {
 protected:
  const std::vector<Address> addresses_{0,  1,  2,  3,  4,  5,  6,  7,  8,  9,
                                        10, 11, 12, 13, 14, 15, 16, 16, 18, 19};

  typedef std::pair<uint32_t, uint32_t> edge;
  const std::vector<edge> edges_{
      {0, 1},   {1, 2},   {2, 3},   {3, 4},   {4, 5},   {5, 6},   {5, 7},
      {5, 8},   {5, 9},   {6, 10},  {7, 10},  {8, 10},  {9, 10},  {10, 11},
      {11, 12}, {12, 13}, {13, 14}, {14, 15}, {14, 15}, {15, 16}, {16, 17},
      {17, 18}, {18, 19}, {1, 18},  {17, 2}};

  //    [0]->[1]->[2]->[3]->[4]->[5]->[6]->[10]->[11]->[12]->[13]->[14]->[15]
  //          ^    ^              +-->[7]----^                       +----^|
  //          |    |              +-->[8]----^                             |
  //          |    |              +-->[9]----^                             |
  //          |    +---------------------------------------------+         |
  //          +-------------------------------------------+      |         |
  //                                               [19]<-[18]<-[17]<-[16]<-+
  // [20] <- alone to test if the graph hashing works if a vertex is not
  // connected by any edge.

  struct VertexInfo {
    Address address;
  };

 public:
  struct EdgeProperty {};

  typedef boost::compressed_sparse_row_graph<boost::bidirectionalS, VertexInfo,
                                             EdgeProperty, boost::no_property,
                                             uint32_t, uint32_t>
      Graph;

  Graph graph_;

  typedef boost::graph_traits<Graph>::vertex_descriptor Vertex;
  typedef boost::graph_traits<Graph>::edge_descriptor Edge;
  typedef boost::graph_traits<Graph>::edge_iterator EdgeIterator;
  typedef boost::graph_traits<Graph>::out_edge_iterator OutEdgeIterator;
  typedef boost::graph_traits<Graph>::in_edge_iterator InEdgeIterator;
  typedef boost::graph_traits<Graph>::adjacency_iterator AdjacencyIterator;

  typedef boost::property_map<Graph, boost::vertex_index_t>::type
      VertexIndexMap;

  typedef boost::adjacency_list<boost::vecS, boost::vecS, boost::undirectedS,
                                VertexInfo, EdgeProperty> UndirectedGraph;

 protected:
  void SetUp() override {
    graph_ = Graph(boost::edges_are_unsorted_multi_pass, edges_.begin(),
                   edges_.end(), addresses_.size());
    typedef boost::graph_traits<Graph>::vertex_iterator VertexIterator;
    VertexIterator vertices_it, vertices_end;
    int j = 0;
    for (boost::tie(vertices_it, vertices_end) = boost::vertices(graph_);
         vertices_it != vertices_end; ++vertices_it, ++j) {
      graph_[j].address = addresses_.at(j);
    }
  }
};

TEST_F(GraphUtilityTest, GetEdgeVector) {
  const std::pair<Edge, bool> edge_pair(
      boost::edge(Vertex(5), Vertex(6), graph_));
  const EdgeDegrees edge_vector =
      GetEdgeDegrees<GraphUtilityTest>(graph_, edge_pair.first);
  EXPECT_EQ(edge_vector.source_in_degree, 1);
  EXPECT_EQ(edge_vector.source_out_degree, 4);
  EXPECT_EQ(edge_vector.target_in_degree, 1);
  EXPECT_EQ(edge_vector.target_out_degree, 1);
}

}  // namespace security::binexport
