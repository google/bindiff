#ifndef CALL_GRAPH_MATCH_FUNCTION_CALL_GRAPH_EDGES_PROXIMITY_MDINDEX_H_
#define CALL_GRAPH_MATCH_FUNCTION_CALL_GRAPH_EDGES_PROXIMITY_MDINDEX_H_

#include "third_party/zynamics/bindiff/call_graph_match.h"

namespace security::bindiff {

// Matches functions based on their local call graph neighborhoods. Calls and
// callees are only followed two levels deep as seen from the function in
// question.
class MatchingStepEdgesProximityMdIndex : public MatchingStep {
 public:
  MatchingStepEdgesProximityMdIndex()
      : MatchingStep("function: edges proximity MD index",
                     "Function: Edges Proximity MD Index") {}

  bool FindFixedPoints(const FlowGraph* primary_parent,
                       const FlowGraph* secondary_parent,
                       FlowGraphs& /*flow_graphs1*/,
                       FlowGraphs& /*flow_graphs2*/, MatchingContext& context,
                       MatchingSteps& matching_steps,
                       const MatchingStepsFlowGraph& default_steps) override;

 private:
  void GetUnmatchedEdgesProximityMdIndex(CallGraph* call_graph,
                                         EdgeFeatures* edges);
};

}  // namespace security::bindiff

#endif  // CALL_GRAPH_MATCH_FUNCTION_CALL_GRAPH_EDGES_PROXIMITY_MDINDEX_H_
