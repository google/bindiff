#ifndef FLOW_GRAPH_MATCH_BASIC_BLOCK_LOOP_ENTRY_H_
#define FLOW_GRAPH_MATCH_BASIC_BLOCK_LOOP_ENTRY_H_

#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security::bindiff {

// Matches basic blocks that are loop anchors, i.e. targets of a back edge.
class MatchingStepLoopEntry : public MatchingStepFlowGraph {
 public:
  MatchingStepLoopEntry()
      : MatchingStepFlowGraph("basicBlock: loop entry matching",
                              "Basic Block: Loop Entry") {}

  bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                       const VertexSet& vertices1, const VertexSet& vertices2,
                       FixedPoint* fixed_point, MatchingContext* context,
                       MatchingStepsFlowGraph* matching_steps) override;

 private:
  void GetUnmatchedBasicBlocksLoopEntry(const FlowGraph* flow_graph,
                                        const VertexSet& vertices,
                                        VertexIntMap* basic_blocks_map);
};

}  // namespace security::bindiff

#endif  // FLOW_GRAPH_MATCH_BASIC_BLOCK_LOOP_ENTRY_H_
