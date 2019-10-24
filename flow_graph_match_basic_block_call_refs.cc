#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_call_refs.h"

#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security::bindiff {

bool MatchingStepCallReferences::FindFixedPoints(
    FlowGraph* primary, FlowGraph* secondary, const VertexSet& vertices1,
    const VertexSet& vertices2, FixedPoint* fixed_point,
    MatchingContext* context, MatchingStepsFlowGraph* matching_steps) {
  VertexIntMap vertex_map_1;
  VertexIntMap vertex_map_2;
  GetUnmatchedBasicBlocksByCallReference(kPrimary, primary, vertices1,
                                         &vertex_map_1, context);
  GetUnmatchedBasicBlocksByCallReference(kSecondary, secondary, vertices2,
                                         &vertex_map_2, context);
  return FindFixedPointsBasicBlockInternal(primary, secondary, &vertex_map_1,
                                           &vertex_map_2, fixed_point, context,
                                           matching_steps);
}

void MatchingStepCallReferences::GetUnmatchedBasicBlocksByCallReference(
    FlowGraphType type, const FlowGraph* flow_graph, const VertexSet& vertices,
    VertexIntMap* basic_blocks_map, MatchingContext* context) {
  basic_blocks_map->clear();
  for (auto vertex : vertices) {
    if (flow_graph->GetFixedPoint(vertex)) {
      continue;
    }

    auto calls = flow_graph->GetCallTargets(vertex);
    if (calls.first == calls.second) {
      continue;
    }

    uint64_t index = 1;
    uint64_t address_feature = 0;
    for (; calls.first != calls.second; ++calls.first, ++index) {
      FixedPoint* fixed_point =
          type == kPrimary ? context->FixedPointByPrimary(*calls.first)
                           : context->FixedPointBySecondary(*calls.first);
      if (!fixed_point) {
        // If we couldn't match all vertices, clear basic block.
        address_feature = 0;
        break;
      }
      address_feature =
          index * (fixed_point->GetPrimary()->GetEntryPointAddress() +
                   fixed_point->GetSecondary()->GetEntryPointAddress());
    }
    if (address_feature) {
      basic_blocks_map->emplace(address_feature, vertex);
    }
  }
}

}  // namespace security::bindiff
