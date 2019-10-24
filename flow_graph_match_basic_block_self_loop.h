#ifndef FLOW_GRAPH_MATCH_BASIC_BLOCK_SELF_LOOP_H_
#define FLOW_GRAPH_MATCH_BASIC_BLOCK_SELF_LOOP_H_

#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security::bindiff {

// Matches basic blocks that have self loops.
class MatchingStepSelfLoops : public MatchingStepFlowGraph {
 public:
  MatchingStepSelfLoops()
      : MatchingStepFlowGraph("basicBlock: self loop matching",
                              "Basic Block: Self Loop") {}

  bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                       const VertexSet& vertices1, const VertexSet& vertices2,
                       FixedPoint* fixed_point, MatchingContext* context,
                       MatchingStepsFlowGraph* matching_steps) override;

 private:
  void GetUnmatchedBasicBlocksSelfLoops(const FlowGraph* flow_graph,
                                        const VertexSet& vertices,
                                        VertexIntMap* basic_blocks_map);
};

}  // namespace security::bindiff

#endif  // FLOW_GRAPH_MATCH_BASIC_BLOCK_SELF_LOOP_H_
