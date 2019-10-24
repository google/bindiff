#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_prime.h"

#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security::bindiff {

bool MatchingStepPrimeBasicBlock::FindFixedPoints(
    FlowGraph* primary, FlowGraph* secondary, const VertexSet& vertices1,
    const VertexSet& vertices2, FixedPoint* fixed_point,
    MatchingContext* context, MatchingStepsFlowGraph* matching_steps) {
  VertexIntMap vertex_map_1;
  VertexIntMap vertex_map_2;
  GetUnmatchedBasicBlocksByPrime(primary, vertices1, &vertex_map_1);
  GetUnmatchedBasicBlocksByPrime(secondary, vertices2, &vertex_map_2);
  return FindFixedPointsBasicBlockInternal(primary, secondary, &vertex_map_1,
                                           &vertex_map_2, fixed_point, context,
                                           matching_steps);
}

void MatchingStepPrimeBasicBlock ::GetUnmatchedBasicBlocksByPrime(
    const FlowGraph* flow_graph, const VertexSet& vertices,
    VertexIntMap* basic_blocks_map) {
  basic_blocks_map->clear();
  for (auto vertex : vertices) {
    if (!flow_graph->GetFixedPoint(vertex) &&
        flow_graph->GetInstructionCount(vertex) >= min_instructions_) {
      basic_blocks_map->emplace(flow_graph->GetPrime(vertex), vertex);
    }
  }
}

}  // namespace security::bindiff
