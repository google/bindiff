#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_mdindex.h"

#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security {
namespace bindiff {

bool MatchingStepMdIndex::FindFixedPoints(
    FlowGraph* primary, FlowGraph* secondary, const VertexSet& vertices1,
    const VertexSet& vertices2, FixedPoint* fixed_point,
    MatchingContext* context, MatchingStepsFlowGraph* matching_steps) {
  VertexDoubleMap vertex_map_1;
  VertexDoubleMap vertex_map_2;
  GetUnmatchedBasicBlocksByMdIndex(primary, vertices1, &vertex_map_1);
  GetUnmatchedBasicBlocksByMdIndex(secondary, vertices2, &vertex_map_2);
  return FindFixedPointsBasicBlockInternal(primary, secondary, &vertex_map_1,
                                           &vertex_map_2, fixed_point, context,
                                           matching_steps);
}

void MatchingStepMdIndex::GetUnmatchedBasicBlocksByMdIndex(
    const FlowGraph* flow_graph, const VertexSet& vertices,
    VertexDoubleMap* basic_blocks_map) {
  basic_blocks_map->clear();
  for (auto vertex : vertices) {
    if (!flow_graph->GetFixedPoint(vertex)) {
      basic_blocks_map->emplace(direction_ == kTopDown
                                    ? flow_graph->GetMdIndex(vertex)
                                    : flow_graph->GetMdIndexInverted(vertex),
                                vertex);
    }
  }
}

}  // namespace bindiff
}  // namespace security
