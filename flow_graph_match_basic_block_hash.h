#ifndef FLOW_GRAPH_MATCH_BASIC_BLOCK_HASH_H_
#define FLOW_GRAPH_MATCH_BASIC_BLOCK_HASH_H_

#include "third_party/zynamics/bindiff/flow_graph_match.h"

#include "third_party/absl/strings/str_cat.h"

namespace security {
namespace bindiff {

// Matches basic blocks based on a binary hash of their raw bytes. Only used on
// basic blocks with a minimum number of specified instructions.
class MatchingStepHashBasicBlock : public MatchingStepFlowGraph {
 public:
  explicit MatchingStepHashBasicBlock(int min_instructions)
      : MatchingStepFlowGraph(absl::StrCat("basicBlock: hash matching (",
                                           min_instructions,
                                           " instructions minimum)")),
        min_instructions_(min_instructions) {}

  bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                       const VertexSet& vertices1, const VertexSet& vertices2,
                       FixedPoint* fixed_point, MatchingContext* context,
                       MatchingStepsFlowGraph* matching_steps) override {
    VertexIntMap vertex_map_1;
    VertexIntMap vertex_map_2;
    GetUnmatchedBasicBlocksByHash(primary, vertices1, &vertex_map_1);
    GetUnmatchedBasicBlocksByHash(secondary, vertices2, &vertex_map_2);
    return FindFixedPointsBasicBlockInternal(primary, secondary, &vertex_map_1,
                                             &vertex_map_2, fixed_point,
                                             context, matching_steps);
  }

 private:
  void GetUnmatchedBasicBlocksByHash(const FlowGraph* flow_graph,
                                     const VertexSet& vertices,
                                     VertexIntMap* basic_blocks_map) {
    basic_blocks_map->clear();
    for (auto vertex : vertices) {
      if (!flow_graph->GetFixedPoint(vertex) &&
          flow_graph->GetInstructionCount(vertex) >= min_instructions_) {
        basic_blocks_map->emplace(flow_graph->GetHash(vertex), vertex);
      }
    }
  }

  int min_instructions_;
};

}  // namespace bindiff
}  // namespace security

#endif  // FLOW_GRAPH_MATCH_BASIC_BLOCK_HASH_H_
