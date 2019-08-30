#ifndef FLOW_GRAPH_MATCH_BASIC_BLOCK_INSTRUCTION_COUNT_H_
#define FLOW_GRAPH_MATCH_BASIC_BLOCK_INSTRUCTION_COUNT_H_

#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security {
namespace bindiff {

// Basic blocks are matched according to their number of instructions.
class MatchingStepInstructionCount : public MatchingStepFlowGraph {
 public:
  MatchingStepInstructionCount()
      : MatchingStepFlowGraph("basicBlock: instruction count matching",
                              "Basic Block: Instruction Count") {}

  bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                       const VertexSet& vertices1, const VertexSet& vertices2,
                       FixedPoint* fixed_point, MatchingContext* context,
                       MatchingStepsFlowGraph* matching_steps) override;

 private:
  void GetUnmatchedBasicBlocksByInstructionCount(
      const FlowGraph* flow_graph, const VertexSet& vertices,
      VertexIntMap* basic_blocks_map);
};

}  // namespace bindiff
}  // namespace security

#endif  // FLOW_GRAPH_MATCH_BASIC_BLOCK_INSTRUCTION_COUNT_H_
