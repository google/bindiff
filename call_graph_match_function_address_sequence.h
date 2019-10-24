#ifndef CALL_GRAPH_MATCH_FUNCTION_ADDRESS_SEQUENCE_H_
#define CALL_GRAPH_MATCH_FUNCTION_ADDRESS_SEQUENCE_H_

#include "third_party/zynamics/bindiff/call_graph_match.h"

namespace security::bindiff {

// Matches functions in order based on their entry point addresses. This is a
// special matching step that is especially useful during drill downs. Since it
// would indiscriminately match all functions if not further constrained there
// are two additional requirements: first the functions in question must already
// be equivalent according to the relaxed MD index and the flow graph MD index.
// Second, the two sets of equivalent functions in both binaries must be of
// equal size.
class MatchingStepSequence : public MatchingStep {
 public:
  MatchingStepSequence()
      : MatchingStep("function: address sequence",
                     "Function: Address Sequence") {
    strict_equivalence_ = true;
  }

  bool FindFixedPoints(const FlowGraph* primary_parent,
                       const FlowGraph* secondary_parent,
                       FlowGraphs& flow_graphs_1, FlowGraphs& flow_graphs_2,
                       MatchingContext& context, MatchingSteps& matching_steps,
                       const MatchingStepsFlowGraph& default_steps) override;

 private:
  void GetUnmatchedFlowGraphsByAddress(const FlowGraphs& flow_graphs,
                                       FlowGraphIntMap& flow_graphs_map);
};

}  // namespace security::bindiff

#endif  // CALL_GRAPH_MATCH_FUNCTION_ADDRESS_SEQUENCE_H_
