#ifndef FLOW_GRAPH_MATCH_BASIC_BLOCK_MDINDEX_H_
#define FLOW_GRAPH_MATCH_BASIC_BLOCK_MDINDEX_H_

#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security {
namespace bindiff {

// Matches basic blocks based on their position in the flow graph.
class MatchingStepMdIndex : public MatchingStepFlowGraph {
 public:
  explicit MatchingStepMdIndex(Direction direction)
      : MatchingStepFlowGraph(
            direction == kTopDown
                ? "basicBlock: MD index matching (top down)"
                : "basicBlock: MD index matching (bottom up)"),
        direction_(direction) {}

  bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                       const VertexSet& vertices1, const VertexSet& vertices2,
                       FixedPoint* fixed_point, MatchingContext* context,
                       MatchingStepsFlowGraph* matching_steps) override {
    VertexDoubleMap vertex_map_1;
    VertexDoubleMap vertex_map_2;
    GetUnmatchedBasicBlocksByMdIndex(primary, vertices1, &vertex_map_1);
    GetUnmatchedBasicBlocksByMdIndex(secondary, vertices2, &vertex_map_2);
    return FindFixedPointsBasicBlockInternal(primary, secondary, &vertex_map_1,
                                             &vertex_map_2, fixed_point,
                                             context, matching_steps);
  }

 private:
  void GetUnmatchedBasicBlocksByMdIndex(const FlowGraph* flow_graph,
                                        const VertexSet& vertices,
                                        VertexDoubleMap* basic_blocks_map) {
    basic_blocks_map->clear();
    for (auto vertex : vertices) {
      if (!flow_graph->GetFixedPoint(vertex)) {
        basic_blocks_map->emplace(direction_ == kTopDown
                                      ? flow_graph->GetMdIndex(vertex)
                                      : flow_graph->GetMdIndexInverted(vertex),
                                  vertex);
      }
    }
  }

  Direction direction_;
};

}  // namespace bindiff
}  // namespace security

#endif  // FLOW_GRAPH_MATCH_BASIC_BLOCK_MDINDEX_H_
