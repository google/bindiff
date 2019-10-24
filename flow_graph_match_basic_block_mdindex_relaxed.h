#ifndef FLOW_GRAPH_MATCH_BASIC_BLOCK_MDINDEX_RELAXED_H_
#define FLOW_GRAPH_MATCH_BASIC_BLOCK_MDINDEX_RELAXED_H_

#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security::bindiff {

// Matches basic blocks based on their position in the flow graph.
class MatchingStepMdIndexRelaxed : public MatchingStepFlowGraph {
 public:
  MatchingStepMdIndexRelaxed()
      : MatchingStepFlowGraph("basicBlock: relaxed MD index matching",
                              "Basic Block: Relaxed MD index") {}

  bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                       const VertexSet& vertices1, const VertexSet& vertices2,
                       FixedPoint* fixed_point, MatchingContext* context,
                       MatchingStepsFlowGraph* matching_steps) override;

 private:
  void GetUnmatchedBasicBlocksByMdIndexRelaxed(
      const FlowGraph* flow_graph, const VertexSet& vertices,
      VertexDoubleMap* basic_blocks_map);
};

}  // namespace security::bindiff

#endif  // FLOW_GRAPH_MATCH_BASIC_BLOCK_MDINDEX_RELAXED_H_
