#ifndef GRAPH_UTIL_H_
#define GRAPH_UTIL_H_

#include <queue>

#include <boost/graph/adjacency_list.hpp>        // NOLINT(readability/boost)
#include <boost/graph/breadth_first_search.hpp>  // NOLINT(readability/boost)
#include <boost/graph/compressed_sparse_row_graph.hpp>  // NOLINT(readability/boost)

#include "third_party/zynamics/bindiff/utility.h"

class FlowGraph;
class CallGraph;

enum vertex_flags_t { vertex_flags };
enum vertex_bfs_index_t { vertex_bfs_index };
enum vertex_bfs_index_inverted_t { vertex_bfs_index_inverted };
enum edge_flags_t { edge_flags };
enum edge_md_index_t { edge_md_index };
enum edge_md_index_inverted_t { edge_md_index_inverted };
namespace boost {
BOOST_INSTALL_PROPERTY(vertex, bfs_index);
BOOST_INSTALL_PROPERTY(vertex, bfs_index_inverted);
BOOST_INSTALL_PROPERTY(vertex, flags);
BOOST_INSTALL_PROPERTY(edge, flags);
BOOST_INSTALL_PROPERTY(edge, md_index);
BOOST_INSTALL_PROPERTY(edge, md_index_inverted);
}

// Calculate the MD index for a given edge in the graph. Use regular breadth
// first index topology if inverted == false, bottom up breadth first index
// for inverted == true. Weights should be a 6 element array of prime numbers
// giving the multiplicative weights for the following features (in order):
// 0) in-degree of source vertex
// 1) out-degree of source vertex
// 2) in-degree of target vertex
// 3) out-degree of target vertex
// 4) topological level of source vertex
// 5) topological level of target vertex
template <typename Graph, typename Edge>
double CalculateMdIndexInternal(const Graph& graph, const Edge& edge,
                                bool inverted, const double weights[6]) {
  const typename boost::graph_traits<Graph>::vertex_descriptor source =
      boost::source(edge, graph);
  const double in_degree_source = boost::in_degree(source, graph);
  const double out_degree_source = boost::out_degree(source, graph);
  const double level_source =
      inverted ? graph[source].bfs_bottom_up_ : graph[source].bfs_top_down_;
  const typename boost::graph_traits<Graph>::vertex_descriptor target =
      boost::target(edge, graph);
  const double in_degree_target = boost::in_degree(target, graph);
  const double out_degree_target = boost::out_degree(target, graph);
  const double level_target =
      inverted ? graph[target].bfs_bottom_up_ : graph[target].bfs_top_down_;
  const double md_index =
      (sqrt(weights[0]) * in_degree_source +
       sqrt(weights[1]) * out_degree_source +
       sqrt(weights[2]) * in_degree_target +
       sqrt(weights[3]) * out_degree_target + sqrt(weights[4]) * level_source +
       sqrt(weights[5]) * level_target);
  assert(md_index);
  return 1.0 / md_index;
}

// Default weights for the MD index features of a vertex. See
// CalculateMdIndexInternal for an explanation of these.
static const double kDefaultWeightsNode[] = {2.0, 3.0, 5.0, 7.0, 0.0, 0.0};

// Calculate the MD index for the given vertex in parent_graph. The MD index
// for a vertex is defined as the sum of MD indices of all in- and out-edges
// of the vertex. See CalculateMdIndexInternal for the edge MD index.
template <typename GraphType>
double CalculateMdIndexNode(const GraphType& parent_graph,
                            typename GraphType::Vertex vertex,
                            bool inverted = false,
                            const double weights[] = kDefaultWeightsNode) {
  const typename GraphType::Graph& graph = parent_graph.GetGraph();
  std::vector<double> md_indices(boost::in_degree(vertex, graph) +
                                 boost::out_degree(vertex, graph));
  size_t index = 0;
  {
    typename GraphType::InEdgeIterator i, end;
    for (boost::tie(i, end) = boost::in_edges(vertex, graph); i != end;
         ++i, ++index) {
      md_indices[index] = CalculateMdIndexInternal(parent_graph.GetGraph(), *i,
                                                   inverted, weights);
    }
  }
  {
    typename GraphType::OutEdgeIterator i, end;
    for (boost::tie(i, end) = boost::out_edges(vertex, graph); i != end;
         ++i, ++index) {
      md_indices[index] = CalculateMdIndexInternal(parent_graph.GetGraph(), *i,
                                                   inverted, weights);
    }
  }

  // Sorting the summands before adding them together is important because
  // of rounding errors. Summation is _not_ commutative for doubles!
  double md_index = 0.0;
  std::sort(md_indices.begin(), md_indices.end());
  for (size_t i = 0; i < md_indices.size(); ++i) {
    md_index += md_indices[i];
  }

  return md_index;
}

// Default weights for the MD index features of a full graph. See
// CalculateMdIndexInternal for an explanation of these.
static const double kDefaultWeights[] = {2.0, 3.0, 5.0, 7.0, 11.0, 13.0};

// Calculate the MD index for the full graph. It is defined as the sum of MD
// indices of all the edges in the graph. See CalculateMdIndexInternal for the
// edge MD index.
// Precondition: CalculateTopology has been called on the graph.
template <typename GraphType>
double CalculateMdIndex(GraphType& parent_graph, bool inverted = false,
                        const double weights[] = kDefaultWeights) {
  typename GraphType::Graph& graph = parent_graph.GetGraph();
  std::vector<double> md_indices(boost::num_edges(graph));
  std::vector<double> md_indices_inverted(boost::num_edges(graph));
  size_t index = 0;
  typename GraphType::EdgeIterator i, end;
  if (inverted) {
    for (boost::tie(i, end) = boost::edges(graph); i != end; ++i, ++index) {
      md_indices[index] = CalculateMdIndexInternal(parent_graph.GetGraph(), *i,
                                                   inverted, weights);
      graph[*i].md_index_bottom_up_ = md_indices[index];
    }
  } else {
    for (boost::tie(i, end) = boost::edges(graph); i != end; ++i, ++index) {
      md_indices[index] = CalculateMdIndexInternal(parent_graph.GetGraph(), *i,
                                                   inverted, weights);
      graph[*i].md_index_top_down_ = md_indices[index];
    }
  }

  // Sorting the summands before adding them together is important because
  // of rounding errors. Summation is _not_ commutative for doubles!
  double md_index = 0.0;
  std::sort(md_indices.begin(), md_indices.end());
  for (size_t j = 0; j < md_indices.size(); ++j) {
    md_index += md_indices[j];
  }

  return md_index;
}

// Perform a breadth first search through the graph starting from all vertices
// with zero in-degree. Update bfs_top_down_ data member of all vertices
// encountered with the iteration index (topological order).
// TODO(soerenme) Verify initialization to 0 of all vertices. Otherwise think
//     about what happens to a disjoint vertex with a self-loop? In-degree is
//     > 0 so it'll not be an initial vertex. It's also disconnected from the
//     rest of the graph and will never be iterated.
template <typename Graph>
void BreadthFirstSearch(Graph* graph) {
  std::queue<typename boost::graph_traits<Graph>::vertex_descriptor> next;
  typename boost::graph_traits<Graph>::vertex_iterator i, end;
  for (boost::tie(i, end) = boost::vertices(*graph); i != end; ++i) {
    (*graph)[*i].bfs_top_down_ = 0;
    if (in_degree(*i, *graph) == 0) {
      next.push(*i);
    }
  }

  while (!next.empty()) {
    typename boost::graph_traits<Graph>::vertex_descriptor vertex =
        next.front();
    next.pop();

    typename boost::graph_traits<Graph>::out_edge_iterator i, end;
    for (boost::tie(i, end) = out_edges(vertex, *graph); i != end; ++i) {
      if ((*graph)[boost::target(*i, *graph)].bfs_top_down_) {
        continue;
      }

      next.push(boost::target(*i, *graph));
      (*graph)[boost::target(*i, *graph)].bfs_top_down_ =
          (*graph)[vertex].bfs_top_down_ + 1;
    }
  }
}

// Like BreadthFirstSearch but starting from vertices with zero out-degree and
// iterating parents instead of children.
// I couldn't figure out how to invert all edges of a graph or how to apply
// boost breadth_first_search to in_edges instead of out_edges so I wrote my
// own...
template <typename Graph>
void InvertedBreadthFirstSearch(Graph* graph) {
  std::queue<typename boost::graph_traits<Graph>::vertex_descriptor> next;
  typename boost::graph_traits<Graph>::vertex_iterator i, end;
  for (boost::tie(i, end) = boost::vertices(*graph); i != end; ++i) {
    (*graph)[*i].bfs_bottom_up_ = 0;
    if (out_degree(*i, *graph) == 0) {
      next.push(*i);
    }
  }

  while (!next.empty()) {
    typename boost::graph_traits<Graph>::vertex_descriptor vertex =
        next.front();
    next.pop();

    typename boost::graph_traits<Graph>::in_edge_iterator i, end;
    for (boost::tie(i, end) = in_edges(vertex, *graph); i != end; ++i) {
      if ((*graph)[boost::source(*i, *graph)].bfs_bottom_up_) {
        continue;
      }

      next.push(boost::source(*i, *graph));
      (*graph)[boost::source(*i, *graph)].bfs_bottom_up_ =
          (*graph)[vertex].bfs_bottom_up_ + 1;
    }
  }
}

#endif  // GRAPH_UTIL_H_
