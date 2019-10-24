#ifndef FLOW_GRAPH_MATCH_BASIC_BLOCK_STRING_REFS_H_
#define FLOW_GRAPH_MATCH_BASIC_BLOCK_STRING_REFS_H_

#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security::bindiff {

// Matches basic blocks if they reference at least one string and that string is
// the same in both binaries.
class MatchingStepStringReferences : public MatchingStepFlowGraph {
 public:
  MatchingStepStringReferences()
      : MatchingStepFlowGraph("basicBlock: string references matching",
                              "Basic Block: String References") {}

  bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                       const VertexSet& vertices1, const VertexSet& vertices2,
                       FixedPoint* fixed_point, MatchingContext* context,
                       MatchingStepsFlowGraph* matching_steps) override;

 private:
  void GetUnmatchedBasicBlocksStringReferences(const FlowGraph* flow_graph,
                                               const VertexSet& vertices,
                                               VertexIntMap* basic_blocks_map);
};

}  // namespace security::bindiff

#endif  // FLOW_GRAPH_MATCH_BASIC_BLOCK_STRING_REFS_H_
