#ifndef FLOW_GRAPH_MATCH_BASIC_BLOCK_EDGES_PRIME_H_
#define FLOW_GRAPH_MATCH_BASIC_BLOCK_EDGES_PRIME_H_

#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security::bindiff {

// Matches flow graph edges if source and target basic block instruction prime
// products match. Thus both basic blocks contain identical instructions,
// potentially ordered differently.
class MatchingStepEdgesPrimeProduct : public MatchingStepFlowGraph {
 public:
  MatchingStepEdgesPrimeProduct()
      : MatchingStepFlowGraph("basicBlock: edges prime product",
                              "Basic Block: Edges Prime Product") {
    edge_matching_ = true;
  }

  bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                       const VertexSet& vertices1, const VertexSet& vertices2,
                       FixedPoint* fixed_point, MatchingContext* context,
                       MatchingStepsFlowGraph* matching_steps) override;

 private:
  void GetUnmatchedEdgesPrimeProduct(const FlowGraph& flow_graph,
                                     const VertexSet& vertices,
                                     EdgeIntMap* edges);
};

}  // namespace security::bindiff

#endif  // FLOW_GRAPH_MATCH_BASIC_BLOCK_EDGES_PRIME_H_
