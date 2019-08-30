#ifndef FLOW_GRAPH_MATCH_BASIC_BLOCK_ENTRY_NODE_H_
#define FLOW_GRAPH_MATCH_BASIC_BLOCK_ENTRY_NODE_H_

#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security {
namespace bindiff {

// Matches the entry/exit point basic blocks. The entry point is uniquely
// identified by the function and usually has an indegree of 0. Exit points are
// vertices with outdegree 0.
class MatchingStepEntryNodes : public MatchingStepFlowGraph {
 public:
  explicit MatchingStepEntryNodes(Direction direction)
      : MatchingStepFlowGraph(
            absl::StrCat(
                "basicBlock: ", direction == kTopDown ? "entry" : "exit",
                " point matching"),
            absl::StrCat("Basic Block: ",
                         direction == kTopDown ? "Entry Point" : "Exit Point")),
        direction_(direction) {}

  bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                       const VertexSet& vertices1, const VertexSet& vertices2,
                       FixedPoint* fixed_point, MatchingContext* context,
                       MatchingStepsFlowGraph* matching_steps) override;

 private:
  void GetUnmatchedBasicBlocksEntryPoint(const FlowGraph* flow_graph,
                                         const VertexSet& vertices,
                                         VertexIntMap* basic_blocks_map);

  Direction direction_;
};

}  // namespace bindiff
}  // namespace security

#endif  // FLOW_GRAPH_MATCH_BASIC_BLOCK_ENTRY_NODE_H_
