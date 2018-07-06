#ifndef FLOW_GRAPH_MATCH_BASIC_BLOCK_JUMP_SEQUENCE_H_
#define FLOW_GRAPH_MATCH_BASIC_BLOCK_JUMP_SEQUENCE_H_

#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security {
namespace bindiff {

class MatchingStepJumpSequence : public MatchingStepFlowGraph {
 public:
  MatchingStepJumpSequence()
      : MatchingStepFlowGraph("basicBlock: jump sequence matching",
                              "Basic Block: Jump Sequence") {}

  bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                       const VertexSet& vertices1, const VertexSet& vertices2,
                       FixedPoint* fixed_point, MatchingContext* context,
                       MatchingStepsFlowGraph* matching_steps) override {
    VertexIntMap vertex_map_1;
    VertexIntMap vertex_map_2;
    GetUnmatchedBasicBlocksByJumpSequence(primary, vertices1, &vertex_map_1);
    GetUnmatchedBasicBlocksByJumpSequence(secondary, vertices2, &vertex_map_2);
    return FindFixedPointsBasicBlockInternal(primary, secondary, &vertex_map_1,
                                             &vertex_map_2, fixed_point,
                                             context, matching_steps);
  }

 private:
  void GetUnmatchedBasicBlocksByJumpSequence(const FlowGraph* flow_graph,
                                             const VertexSet& vertices,
                                             VertexIntMap* basic_blocks_map) {
    basic_blocks_map->clear();
    std::map<uint64_t, uint64_t> md_count;
    for (auto vertex : vertices) {
      if (!flow_graph->GetFixedPoint(vertex)) {
        const auto int_md_index = static_cast<uint64_t>(
            flow_graph->GetMdIndex(vertex) * 1000000000000000000ULL);
        basic_blocks_map->emplace(md_count[int_md_index]++ + int_md_index,
                                  vertex);
      }
    }
  }
};

}  // namespace bindiff
}  // namespace security

#endif  // FLOW_GRAPH_MATCH_BASIC_BLOCK_JUMP_SEQUENCE_H_
