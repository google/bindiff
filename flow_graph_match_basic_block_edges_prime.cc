#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_edges_prime.h"

#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security {
namespace bindiff {

bool MatchingStepEdgesPrimeProduct::FindFixedPoints(
    FlowGraph* primary, FlowGraph* secondary, const VertexSet& vertices1,
    const VertexSet& vertices2, FixedPoint* fixed_point,
    MatchingContext* context, MatchingStepsFlowGraph* matching_steps) {
  EdgeIntMap primary_edges, secondary_edges;
  GetUnmatchedEdgesPrimeProduct(*primary, vertices1, &primary_edges);
  GetUnmatchedEdgesPrimeProduct(*secondary, vertices2, &secondary_edges);
  return FindFixedPointsBasicBlockEdgeInternal(&primary_edges, &secondary_edges,
                                               primary, secondary, fixed_point,
                                               context, matching_steps);
}

void MatchingStepEdgesPrimeProduct::GetUnmatchedEdgesPrimeProduct(
    const FlowGraph& flow_graph, const VertexSet& vertices, EdgeIntMap* edges) {
  edges->clear();
  FlowGraph::EdgeIterator edge, end;
  for (boost::tie(edge, end) = boost::edges(flow_graph.GetGraph()); edge != end;
       ++edge) {
    if (flow_graph.IsCircular(*edge)) {
      continue;
    }
    const auto source = boost::source(*edge, flow_graph.GetGraph());
    const auto target = boost::target(*edge, flow_graph.GetGraph());
    if ((flow_graph.GetFixedPoint(source) == nullptr ||
         flow_graph.GetFixedPoint(target) == nullptr) &&
        (vertices.find(source) != vertices.end() ||
         vertices.find(target) != vertices.end())) {
      const uint64_t prime =
          flow_graph.GetPrime(source) + flow_graph.GetPrime(target) + 1;
      edges->emplace(prime, *edge);
    }
  }
}

}  // namespace bindiff
}  // namespace security
