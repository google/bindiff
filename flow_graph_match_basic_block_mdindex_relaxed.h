#ifndef FLOW_GRAPH_MATCH_BASIC_BLOCK_MDINDEX_RELAXED_H_
#define FLOW_GRAPH_MATCH_BASIC_BLOCK_MDINDEX_RELAXED_H_

#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security {
namespace bindiff {

// Matches basic blocks based on their position in the flow graph.
class MatchingStepMdIndexRelaxed : public MatchingStepFlowGraph {
 public:
  MatchingStepMdIndexRelaxed()
      : MatchingStepFlowGraph("basicBlock: relaxed MD index matching",
                              "Basic Block: Relaxed MD index") {}

  bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                       const VertexSet& vertices1, const VertexSet& vertices2,
                       FixedPoint* fixed_point, MatchingContext* context,
                       MatchingStepsFlowGraph* matching_steps) override {
    VertexDoubleMap vertex_map_1;
    VertexDoubleMap vertex_map_2;
    GetUnmatchedBasicBlocksByMdIndexRelaxed(primary, vertices1, &vertex_map_1);
    GetUnmatchedBasicBlocksByMdIndexRelaxed(secondary, vertices2,
                                            &vertex_map_2);
    return FindFixedPointsBasicBlockInternal(primary, secondary, &vertex_map_1,
                                             &vertex_map_2, fixed_point,
                                             context, matching_steps);
  }

 private:
  void GetUnmatchedBasicBlocksByMdIndexRelaxed(
      const FlowGraph* flow_graph, const VertexSet& vertices,
      VertexDoubleMap* basic_blocks_map) {
    basic_blocks_map->clear();
    for (auto vertex : vertices) {
      if (!flow_graph->GetFixedPoint(vertex)) {
        basic_blocks_map->emplace(CalculateMdIndexNode(*flow_graph, vertex),
                                  vertex);
      }
    }
  }
};

}  // namespace bindiff
}  // namespace security

#endif  // FLOW_GRAPH_MATCH_BASIC_BLOCK_MDINDEX_RELAXED_H_
