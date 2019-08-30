#ifndef FLOW_GRAPH_MATCH_BASIC_BLOCK_EDGES_MDINDEX_H_
#define FLOW_GRAPH_MATCH_BASIC_BLOCK_EDGES_MDINDEX_H_

#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/bindiff/flow_graph_match.h"

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
                       MatchingStepsFlowGraph* matching_steps) override;

 private:
  void GetUnmatchedEdgesMdIndex(const FlowGraph& flow_graph,
                                const VertexSet& vertices,
                                EdgeDoubleMap* edges);

  Direction direction_;
};

}  // namespace bindiff
}  // namespace security

#endif  // FLOW_GRAPH_MATCH_BASIC_BLOCK_EDGES_MDINDEX_H_
