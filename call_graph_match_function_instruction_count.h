#ifndef CALL_GRAPH_MATCH_FUNCTION_INSTRUCTION_COUNT_H_
#define CALL_GRAPH_MATCH_FUNCTION_INSTRUCTION_COUNT_H_

#include "third_party/zynamics/bindiff/call_graph_match.h"

namespace security {
namespace bindiff {

class MatchingStepFunctionInstructionCount : public MatchingStep {
 public:
  MatchingStepFunctionInstructionCount()
      : MatchingStep("function: instruction count") {
    strict_equivalence_ = true;
  }

  bool FindFixedPoints(const FlowGraph* primary_parent,
                       const FlowGraph* secondary_parent,
                       FlowGraphs& flow_graphs_1, FlowGraphs& flow_graphs_2,
                       MatchingContext& context, MatchingSteps& matching_steps,
                       const MatchingStepsFlowGraph& default_steps);

 private:
  void GetUnmatchedFlowGraphsByInstructionCount(
      const FlowGraphs& flow_graphs, FlowGraphIntMap& flow_graphs_map);
};

}  // namespace bindiff
}  // namespace security

#endif  // CALL_GRAPH_MATCH_FUNCTION_INSTRUCTION_COUNT_H_
