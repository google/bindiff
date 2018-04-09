#ifndef FLOW_GRAPH_MATCH_BASIC_BLOCK_LOOP_ENTRY_H_
#define FLOW_GRAPH_MATCH_BASIC_BLOCK_LOOP_ENTRY_H_

#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security {
namespace bindiff {

// Matches basic blocks that are loop anchors, i.e. targets of a back edge.
class MatchingStepLoopEntry : public MatchingStepFlowGraph {
 public:
  MatchingStepLoopEntry()
      : MatchingStepFlowGraph("basicBlock: loop entry matching") {}

  bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                       const VertexSet& vertices1, const VertexSet& vertices2,
                       FixedPoint* fixed_point, MatchingContext* context,
                       MatchingStepsFlowGraph* matching_steps) override {
    VertexIntMap vertex_map_1;
    VertexIntMap vertex_map_2;
    GetUnmatchedBasicBlocksLoopEntry(primary, vertices1, &vertex_map_1);
    GetUnmatchedBasicBlocksLoopEntry(secondary, vertices2, &vertex_map_2);
    return FindFixedPointsBasicBlockInternal(primary, secondary, &vertex_map_1,
                                             &vertex_map_2, fixed_point,
                                             context, matching_steps);
  }

 private:
  void GetUnmatchedBasicBlocksLoopEntry(const FlowGraph* flow_graph,
                                        const VertexSet& vertices,
                                        VertexIntMap* basic_blocks_map) {
    basic_blocks_map->clear();
    uint64_t loop_index = 0;
    for (auto vertex : vertices) {
      if (flow_graph->GetFixedPoint(vertex)) {
        continue;
      }

      if (flow_graph->IsLoopEntry(vertex)) {
        basic_blocks_map->emplace(loop_index++, vertex);
      }
    }
  }
};

}  // namespace bindiff
}  // namespace security

#endif  // FLOW_GRAPH_MATCH_BASIC_BLOCK_LOOP_ENTRY_H_
