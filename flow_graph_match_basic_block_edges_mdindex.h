#ifndef FLOW_GRAPH_MATCH_BASIC_BLOCK_EDGES_MDINDEX_H_
#define FLOW_GRAPH_MATCH_BASIC_BLOCK_EDGES_MDINDEX_H_

#include "third_party/zynamics/bindiff/flow_graph_match.h"

#include "third_party/absl/strings/str_cat.h"

namespace security {
namespace bindiff {

// Basic blocks are matched based on their position in the flow graph.
class MatchingStepEdgesMdIndex : public MatchingStepFlowGraph {
 public:
  explicit MatchingStepEdgesMdIndex(Direction direction)
      : MatchingStepFlowGraph(
            absl::StrCat("basicBlock: edges MD index (",
                         direction == kTopDown ? "top down)" : "bottom up)"),
            absl::StrCat("Basic Block: Edges MD Index (",
                         direction == kTopDown ? "Top Down)" : "Bottom Up)")),
        direction_(direction) {
    edge_matching_ = true;
  }

  bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                       const VertexSet& vertices1, const VertexSet& vertices2,
                       FixedPoint* fixed_point, MatchingContext* context,
                       MatchingStepsFlowGraph* matching_steps) override {
    EdgeDoubleMap primary_edges;
    EdgeDoubleMap secondary_edges;
    GetUnmatchedEdgesMdIndex(*primary, vertices1, &primary_edges);
    GetUnmatchedEdgesMdIndex(*secondary, vertices2, &secondary_edges);
    return FindFixedPointsBasicBlockEdgeInternal(
        &primary_edges, &secondary_edges, primary, secondary, fixed_point,
        context, matching_steps);
  }

 private:
  void GetUnmatchedEdgesMdIndex(const FlowGraph& flow_graph,
                                const VertexSet& vertices,
                                EdgeDoubleMap* edges) {
    edges->clear();
    FlowGraph::EdgeIterator edge, end;
    for (boost::tie(edge, end) = boost::edges(flow_graph.GetGraph());
         edge != end; ++edge) {
      if (flow_graph.IsCircular(*edge)) {
        continue;
      }

      const auto source = boost::source(*edge, flow_graph.GetGraph());
      const auto target = boost::target(*edge, flow_graph.GetGraph());
      if ((flow_graph.GetFixedPoint(source) == nullptr ||
           flow_graph.GetFixedPoint(target) == nullptr) &&
          (vertices.find(source) != vertices.end() ||
           vertices.find(target) != vertices.end())) {
        edges->emplace(direction_ == kTopDown
                           ? flow_graph.GetMdIndex(*edge)
                           : flow_graph.GetMdIndexInverted(*edge),
                       *edge);
      }
    }
  }

  Direction direction_;
};

}  // namespace bindiff
}  // namespace security

#endif  // FLOW_GRAPH_MATCH_BASIC_BLOCK_EDGES_MDINDEX_H_
