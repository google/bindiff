#ifndef CALL_GRAPH_MATCH_FUNCTION_CALL_GRAPH_MDINDEX_H_
#define CALL_GRAPH_MATCH_FUNCTION_CALL_GRAPH_MDINDEX_H_

#include "third_party/zynamics/bindiff/call_graph_match.h"

namespace security {
namespace bindiff {

// Matches functions based on their position in the callgraph. The call graph
// leading up to that function must be structurally identical as viewed from the
// program entrypoint (top down) or its exits (bottom up).
class MatchingStepCallGraphMdIndex : public MatchingStep {
 public:
  explicit MatchingStepCallGraphMdIndex(Direction direction)
      : MatchingStep(
            direction == kTopDown
                ? "function: MD index matching (callGraph MD index, top down)"
                : "function: MD index matching (callGraph MD index, bottom "
                  "up)"),
        direction_(direction) {}

  bool FindFixedPoints(const FlowGraph* primary_parent,
                       const FlowGraph* secondary_parent,
                       FlowGraphs& flow_graphs_1, FlowGraphs& flow_graphs_2,
                       MatchingContext& context, MatchingSteps& matching_steps,
                       const MatchingStepsFlowGraph& default_steps) override;

 private:
  void GetUnmatchedFlowGraphsByCallGraphMDIndex(
      const FlowGraphs& flow_graphs, FlowGraphDoubleMap& flow_graphs_map);

  Direction direction_;
};

}  // namespace bindiff
}  // namespace security

#endif  // CALL_GRAPH_MATCH_FUNCTION_CALL_GRAPH_MDINDEX_H_
