#ifndef FLOW_GRAPH_MATCH_BASIC_BLOCK_ENTRY_NODE_H_
#define FLOW_GRAPH_MATCH_BASIC_BLOCK_ENTRY_NODE_H_

#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security {
namespace bindiff {

// Matches the entry/exit point basic blocks. The entry point is uniquely
// identified by the function and usually has an indegree of 0. Exit points are
// vertices with outdegree 0.
class MatchingStepEntryNodes : public MatchingStepFlowGraph {
 public:
  explicit MatchingStepEntryNodes(Direction direction)
      : MatchingStepFlowGraph(direction == kTopDown
                                  ? "basicBlock: entry point matching"
                                  : "basicBlock: exit point matching"),
        direction_(direction) {}

  bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                       const VertexSet& vertices1, const VertexSet& vertices2,
                       FixedPoint* fixed_point, MatchingContext* context,
                       MatchingStepsFlowGraph* matching_steps) override {
    VertexIntMap vertex_map_1;
    VertexIntMap vertex_map_2;
    GetUnmatchedBasicBlocksEntryPoint(primary, vertices1, &vertex_map_1);
    GetUnmatchedBasicBlocksEntryPoint(secondary, vertices2, &vertex_map_2);
    return FindFixedPointsBasicBlockInternal(primary, secondary, &vertex_map_1,
                                             &vertex_map_2, fixed_point,
                                             context, matching_steps);
  }

 private:
  void GetUnmatchedBasicBlocksEntryPoint(const FlowGraph* flow_graph,
                                         const VertexSet& vertices,
                                         VertexIntMap* basic_blocks_map) {
    basic_blocks_map->clear();
    for (auto vertex : vertices) {
      if (!flow_graph->GetFixedPoint(vertex)) {
        if ((direction_ == kTopDown &&
             boost::in_degree(vertex, flow_graph->GetGraph()) == 0) ||
            (direction_ == kBottomUp &&
             boost::out_degree(vertex, flow_graph->GetGraph()) == 0)) {
          basic_blocks_map->emplace(1, vertex);
        }
      }
    }
  }

  Direction direction_;
};

}  // namespace bindiff
}  // namespace security

#endif  // FLOW_GRAPH_MATCH_BASIC_BLOCK_ENTRY_NODE_H_
