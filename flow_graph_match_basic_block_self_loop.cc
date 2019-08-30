#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_self_loop.h"

#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security {
namespace bindiff {

bool MatchingStepSelfLoops::FindFixedPoints(
    FlowGraph* primary, FlowGraph* secondary, const VertexSet& vertices1,
    const VertexSet& vertices2, FixedPoint* fixed_point,
    MatchingContext* context, MatchingStepsFlowGraph* matching_steps) {
  VertexIntMap vertex_map_1, vertex_map_2;
  GetUnmatchedBasicBlocksSelfLoops(primary, vertices1, &vertex_map_1);
  GetUnmatchedBasicBlocksSelfLoops(secondary, vertices2, &vertex_map_2);
  return FindFixedPointsBasicBlockInternal(primary, secondary, &vertex_map_1,
                                           &vertex_map_2, fixed_point, context,
                                           matching_steps);
}

void MatchingStepSelfLoops::GetUnmatchedBasicBlocksSelfLoops(
    const FlowGraph* flow_graph, const VertexSet& vertices,
    VertexIntMap* basic_blocks_map) {
  basic_blocks_map->clear();
  for (auto vertex : vertices) {
    if (flow_graph->GetFixedPoint(vertex)) {
      continue;
    }

    size_t count = 0;
    FlowGraph::OutEdgeIterator j, end;
    for (boost::tie(j, end) = boost::out_edges(vertex, flow_graph->GetGraph());
         j != end; ++j) {
      count += boost::source(*j, flow_graph->GetGraph()) ==
               boost::target(*j, flow_graph->GetGraph());
    }
    if (count) {
      basic_blocks_map->emplace(count, vertex);
    }
  }
}

}  // namespace bindiff
}  // namespace security
