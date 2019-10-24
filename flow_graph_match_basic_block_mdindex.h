#ifndef FLOW_GRAPH_MATCH_BASIC_BLOCK_MDINDEX_H_
#define FLOW_GRAPH_MATCH_BASIC_BLOCK_MDINDEX_H_

#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security::bindiff {

// Matches basic blocks based on their position in the flow graph.
class MatchingStepMdIndex : public MatchingStepFlowGraph {
 public:
  explicit MatchingStepMdIndex(Direction direction)
      : MatchingStepFlowGraph(
            absl::StrCat("basicBlock: MD index matching (",
                         direction == kTopDown ? "top down)" : "bottom up)"),
            absl::StrCat("Basic Block: MD Index (",
                         direction == kTopDown ? "Top Down)" : "Bottom Up)")),
        direction_(direction) {}

  bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                       const VertexSet& vertices1, const VertexSet& vertices2,
                       FixedPoint* fixed_point, MatchingContext* context,
                       MatchingStepsFlowGraph* matching_steps) override;

 private:
  void GetUnmatchedBasicBlocksByMdIndex(const FlowGraph* flow_graph,
                                        const VertexSet& vertices,
                                        VertexDoubleMap* basic_blocks_map);

  Direction direction_;
};

}  // namespace security::bindiff

#endif  // FLOW_GRAPH_MATCH_BASIC_BLOCK_MDINDEX_H_
