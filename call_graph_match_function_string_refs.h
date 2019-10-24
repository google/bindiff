#ifndef CALL_GRAPH_MATCH_FUNCTION_STRING_REFS_H_
#define CALL_GRAPH_MATCH_FUNCTION_STRING_REFS_H_

#include "third_party/zynamics/bindiff/call_graph_match.h"

namespace security::bindiff {

// Matches functions based on their referenced string data. All strings
// referenced from the functions in question are put into a combined hash which
// is subsequently used as the matching attribute if at least one string is
// referenced. This is a good algorithm for matching error handling code which
// often has very little structure (and thus won't get matched by the stronger
// algorithms) but lots of references to error message strings.
class MatchingStepFunctionStringReferences : public MatchingStep {
 public:
  MatchingStepFunctionStringReferences()
      : MatchingStep("function: string references",
                     "Function: String References") {}

  bool FindFixedPoints(const FlowGraph* primary_parent,
                       const FlowGraph* secondary_parent,
                       FlowGraphs& flow_graphs_1, FlowGraphs& flow_graphs_2,
                       MatchingContext& context, MatchingSteps& matching_steps,
                       const MatchingStepsFlowGraph& default_steps) override;

 private:
  void GetUnmatchedFlowGraphsByStringReferences(
      const FlowGraphs& flow_graphs, FlowGraphIntMap& flow_graphs_map);
};

}  // namespace security::bindiff

#endif  // CALL_GRAPH_MATCH_FUNCTION_STRING_REFS_H_
