#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_string_refs.h"

#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security::bindiff {

bool MatchingStepStringReferences::FindFixedPoints(
    FlowGraph* primary, FlowGraph* secondary, const VertexSet& vertices1,
    const VertexSet& vertices2, FixedPoint* fixed_point,
    MatchingContext* context, MatchingStepsFlowGraph* matching_steps) {
  VertexIntMap vertex_map_1;
  VertexIntMap vertex_map_2;
  GetUnmatchedBasicBlocksStringReferences(primary, vertices1, &vertex_map_1);
  GetUnmatchedBasicBlocksStringReferences(secondary, vertices2, &vertex_map_2);
  return FindFixedPointsBasicBlockInternal(primary, secondary, &vertex_map_1,
                                           &vertex_map_2, fixed_point, context,
                                           matching_steps);
}

void MatchingStepStringReferences ::GetUnmatchedBasicBlocksStringReferences(
    const FlowGraph* flow_graph, const VertexSet& vertices,
    VertexIntMap* basic_blocks_map) {
  basic_blocks_map->clear();
  for (auto vertex : vertices) {
    if (flow_graph->GetFixedPoint(vertex)) {
      continue;
    }
    const uint32_t hash = flow_graph->GetStringReferences(vertex);
    if (hash > 1) {
      basic_blocks_map->emplace(hash, vertex);
    }
  }
}

}  // namespace security::bindiff
