#ifndef FLOW_GRAPH_MATCH_BASIC_BLOCK_CALL_REFS_H_
#define FLOW_GRAPH_MATCH_BASIC_BLOCK_CALL_REFS_H_

#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security::bindiff {

// Matches basic blocks if they call at least one function and all called
// functions have been matched.
class MatchingStepCallReferences : public MatchingStepFlowGraph {
 public:
  MatchingStepCallReferences()
      : MatchingStepFlowGraph("basicBlock: call reference matching",
                              "Basic Block: Call Reference") {}

  bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                       const VertexSet& vertices1, const VertexSet& vertices2,
                       FixedPoint* fixed_point, MatchingContext* context,
                       MatchingStepsFlowGraph* matching_steps) override;

 private:
  enum FlowGraphType { kPrimary, kSecondary };

  void GetUnmatchedBasicBlocksByCallReference(FlowGraphType type,
                                              const FlowGraph* flow_graph,
                                              const VertexSet& vertices,
                                              VertexIntMap* basic_blocks_map,
                                              MatchingContext* context);
};

}  // namespace security::bindiff

#endif  // FLOW_GRAPH_MATCH_BASIC_BLOCK_CALL_REFS_H_
