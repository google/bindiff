#ifndef CALL_GRAPH_MATCH_FUNCTION_HASH_H_
#define CALL_GRAPH_MATCH_FUNCTION_HASH_H_

#include "third_party/zynamics/bindiff/call_graph_match.h"

namespace security::bindiff {

// Matches functions based on a hash of the original raw function bytes. Thus
// two functions matched by this algorithm should be identical on the byte
// level.
class MatchingStepHash : public MatchingStep {
 public:
  MatchingStepHash()
      : MatchingStep("function: hash matching", "Function: Hash") {}

  bool FindFixedPoints(const FlowGraph* primary_parent,
                       const FlowGraph* secondary_parent,
                       FlowGraphs& flow_graphs_1, FlowGraphs& flow_graphs_2,
                       MatchingContext& context, MatchingSteps& matching_steps,
                       const MatchingStepsFlowGraph& default_steps) override;

 private:
  void GetUnmatchedFlowGraphsByHash(const FlowGraphs& flow_graphs,
                                    FlowGraphIntMap& flow_graphs_map);
};

}  // namespace security::bindiff

#endif  // CALL_GRAPH_MATCH_FUNCTION_HASH_H_
