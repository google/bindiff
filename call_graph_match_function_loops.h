#ifndef CALL_GRAPH_MATCH_FUNCTION_LOOPS_H_
#define CALL_GRAPH_MATCH_FUNCTION_LOOPS_H_

#include "third_party/zynamics/bindiff/call_graph_match.h"

namespace security::bindiff {

// Matches functions based on the number of loops. Only applied if at least one
// loop is present.
class MatchingStepLoops : public MatchingStep {
 public:
  MatchingStepLoops()
      : MatchingStep("function: loop count matching", "Function: Loop Count") {}

  bool FindFixedPoints(const FlowGraph* primary_parent,
                       const FlowGraph* secondary_parent,
                       FlowGraphs& flow_graphs_1, FlowGraphs& flow_graphs_2,
                       MatchingContext& context, MatchingSteps& matching_steps,
                       const MatchingStepsFlowGraph& default_steps) override;

 private:
  void GetUnmatchedFlowGraphsByLoopCount(const FlowGraphs& flow_graphs,
                                         FlowGraphIntMap& flow_graphs_map);
};

}  // namespace security::bindiff

#endif  // CALL_GRAPH_MATCH_FUNCTION_LOOPS_H_
