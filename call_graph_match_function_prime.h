#ifndef CALL_GRAPH_MATCH_FUNCTION_PRIME_H_
#define CALL_GRAPH_MATCH_FUNCTION_PRIME_H_

#include "third_party/zynamics/bindiff/call_graph_match.h"

namespace security::bindiff {

// Matches functions based on their instruction prime products. Each mnemonic
// gets assigned a unique small prime number. These primes are multiplied for
// all instructions of the function. This yields a structurally invariant,
// instruction order independent, product to be subsequently used as a matching
// attribute.
class MatchingStepPrime : public MatchingStep {
 public:
  MatchingStepPrime()
      : MatchingStep("function: prime signature matching",
                     "Function: Prime Signature") {}

  bool FindFixedPoints(const FlowGraph* primary_parent,
                       const FlowGraph* secondary_parent,
                       FlowGraphs& flow_graphs_1, FlowGraphs& flow_graphs_2,
                       MatchingContext& context, MatchingSteps& matching_steps,
                       const MatchingStepsFlowGraph& default_steps) override;

 private:
  void GetUnmatchedFlowGraphsByPrimeSignature(const FlowGraphs& flow_graphs,
                                              FlowGraphIntMap& flow_graphs_map);
};

}  // namespace security::bindiff

#endif  // CALL_GRAPH_MATCH_FUNCTION_PRIME_H_
