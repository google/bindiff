#ifndef FLOW_GRAPH_MATCH_BASIC_BLOCK_SELF_LOOP_H_
#define FLOW_GRAPH_MATCH_BASIC_BLOCK_SELF_LOOP_H_

#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security {
namespace bindiff {

// Matches basic blocks that have self loops.
class MatchingStepSelfLoops : public MatchingStepFlowGraph {
 public:
  MatchingStepSelfLoops()
      : MatchingStepFlowGraph("basicBlock: self loop matching",
                              "Basic Block: Self Loop") {}

  virtual bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                               const VertexSet& vertices1,
                               const VertexSet& vertices2,
                               FixedPoint* fixed_point,
                               MatchingContext* context,
                               MatchingStepsFlowGraph* matching_steps) {
    VertexIntMap vertex_map_1, vertex_map_2;
    GetUnmatchedBasicBlocksSelfLoops(primary, vertices1, &vertex_map_1);
    GetUnmatchedBasicBlocksSelfLoops(secondary, vertices2, &vertex_map_2);
    return FindFixedPointsBasicBlockInternal(primary, secondary, &vertex_map_1,
                                             &vertex_map_2, fixed_point,
                                             context, matching_steps);
  }

 private:
  void GetUnmatchedBasicBlocksSelfLoops(const FlowGraph* flow_graph,
                                        const VertexSet& vertices,
                                        VertexIntMap* basic_blocks_map) {
    basic_blocks_map->clear();
    for (auto vertex : vertices) {
      if (flow_graph->GetFixedPoint(vertex)) {
        continue;
      }

      size_t count = 0;
      FlowGraph::OutEdgeIterator j, end;
      for (boost::tie(j, end) =
               boost::out_edges(vertex, flow_graph->GetGraph());
           j != end; ++j) {
        count += boost::source(*j, flow_graph->GetGraph()) ==
                 boost::target(*j, flow_graph->GetGraph());
      }
      if (count) {
        basic_blocks_map->emplace(count, vertex);
      }
    }
  }
};

}  // namespace bindiff
}  // namespace security

#endif  // FLOW_GRAPH_MATCH_BASIC_BLOCK_SELF_LOOP_H_
