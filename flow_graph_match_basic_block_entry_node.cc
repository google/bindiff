#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_entry_node.h"

#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security::bindiff {

bool MatchingStepEntryNodes::FindFixedPoints(
    FlowGraph* primary, FlowGraph* secondary, const VertexSet& vertices1,
    const VertexSet& vertices2, FixedPoint* fixed_point,
    MatchingContext* context, MatchingStepsFlowGraph* matching_steps) {
  VertexIntMap vertex_map_1;
  VertexIntMap vertex_map_2;
  GetUnmatchedBasicBlocksEntryPoint(primary, vertices1, &vertex_map_1);
  GetUnmatchedBasicBlocksEntryPoint(secondary, vertices2, &vertex_map_2);
  return FindFixedPointsBasicBlockInternal(primary, secondary, &vertex_map_1,
                                           &vertex_map_2, fixed_point, context,
                                           matching_steps);
}

void MatchingStepEntryNodes::GetUnmatchedBasicBlocksEntryPoint(
    const FlowGraph* flow_graph, const VertexSet& vertices,
    VertexIntMap* basic_blocks_map) {
  basic_blocks_map->clear();
  for (auto vertex : vertices) {
    if (!flow_graph->GetFixedPoint(vertex)) {
      if ((direction_ == kTopDown &&
           boost::in_degree(vertex, flow_graph->GetGraph()) == 0) ||
          (direction_ == kBottomUp &&
           boost::out_degree(vertex, flow_graph->GetGraph()) == 0)) {
        basic_blocks_map->emplace(1, vertex);
      }
    }
  }
}

}  // namespace security::bindiff
