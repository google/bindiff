#ifndef CALL_GRAPH_MATCH_FUNCTION_CALL_SEQUENCE_H_
#define CALL_GRAPH_MATCH_FUNCTION_CALL_SEQUENCE_H_

#include "third_party/zynamics/bindiff/call_graph_match.h"

namespace security {
namespace bindiff {

// Special algorithm that is only used for functions with matched parents
// (callers). The point of the call at the call site is determined as a tuple:
// topological basic block level, instruction number in the basic block,
// address. The child function (callee) is matched if basic block level and
// instruction number match (exact), if only the basic block level match
// (topology) or simply ordered by call site address (sequence). This produces
// very weak matches in general, but may be a good strategy if the parent
// function was matched correctly. In that case it is not unlikely that it will
// call functions in the same order in both binaries.
class MatchingStepCallSequence : public MatchingStep {
 public:
  enum Accuracy { EXACT = 0, TOPOLOGY, SEQUENCE };

  explicit MatchingStepCallSequence(Accuracy accuracy)
      : MatchingStep(accuracy == EXACT
                         ? "function: call sequence matching(exact)"
                         : accuracy == TOPOLOGY
                               ? "function: call sequence matching(topology)"
                               : "function: call sequence matching(sequence)"),
        accuracy_(accuracy) {}

  bool FindFixedPoints(const FlowGraph* primary_parent,
                       const FlowGraph* secondary_parent,
                       FlowGraphs& flow_graphs_1, FlowGraphs& flow_graphs_2,
                       MatchingContext& context, MatchingSteps& matching_steps,
                       const MatchingStepsFlowGraph& default_steps) override;

 private:
  void GetUnmatchedFlowGraphsByCallLevel(
      const FlowGraph* parent, const FlowGraphs& flow_graphs,
      FlowGraphIntMap& flow_graphs_map,
      MatchingStepCallSequence::Accuracy accuracy);

  Accuracy accuracy_;
};

}  // namespace bindiff
}  // namespace security

#endif  // CALL_GRAPH_MATCH_FUNCTION_CALL_SEQUENCE_H_
