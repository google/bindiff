#ifndef FLOW_GRAPH_MATCH_BASIC_BLOCK_STRING_REFS_H_
#define FLOW_GRAPH_MATCH_BASIC_BLOCK_STRING_REFS_H_

#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security {
namespace bindiff {

// Matches basic blocks if they reference at least one string and that string is
// the same in both binaries.
class MatchingStepStringReferences : public MatchingStepFlowGraph {
 public:
  MatchingStepStringReferences()
      : MatchingStepFlowGraph("basicBlock: string references matching") {}

  bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                       const VertexSet& vertices1, const VertexSet& vertices2,
                       FixedPoint* fixed_point, MatchingContext* context,
                       MatchingStepsFlowGraph* matching_steps) override {
    VertexIntMap vertex_map_1;
    VertexIntMap vertex_map_2;
    GetUnmatchedBasicBlocksStringReferences(primary, vertices1, &vertex_map_1);
    GetUnmatchedBasicBlocksStringReferences(secondary, vertices2,
                                            &vertex_map_2);
    return FindFixedPointsBasicBlockInternal(primary, secondary, &vertex_map_1,
                                             &vertex_map_2, fixed_point,
                                             context, matching_steps);
  }

 private:
  void GetUnmatchedBasicBlocksStringReferences(const FlowGraph* flow_graph,
                                               const VertexSet& vertices,
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
};

}  // namespace bindiff
}  // namespace security

#endif  // FLOW_GRAPH_MATCH_BASIC_BLOCK_STRING_REFS_H_
