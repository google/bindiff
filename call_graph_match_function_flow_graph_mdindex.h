#ifndef CALL_GRAPH_MATCH_FUNCTION_FLOW_GRAPH_MDINDEX_H_
#define CALL_GRAPH_MATCH_FUNCTION_FLOW_GRAPH_MDINDEX_H_

#include "third_party/zynamics/bindiff/call_graph_match.h"

namespace security {
namespace bindiff {

// Matches functions based on their structure using the MD index. Since the MD
// index takes a topological graph ordering as one of it's inputs we can
// parametrize it by whether we sort the graph vertices into levels following
// calls from the entrypoint (top down) or callers from the exit points (bottom
// up).
class MatchingStepFlowGraphMdIndex : public MatchingStep {
 public:
  explicit MatchingStepFlowGraphMdIndex(Direction direction)
      : MatchingStep(
            direction == kTopDown
                ? "function: MD index matching (flowgraph MD index, top down)"
                : "function: MD index matching (flowgraph MD index, bottom "
                  "up)"),
        direction_(direction) {}

  bool FindFixedPoints(const FlowGraph* primary_parent,
                       const FlowGraph* secondary_parent,
                       FlowGraphs& flow_graphs_1, FlowGraphs& flow_graphs_2,
                       MatchingContext& context, MatchingSteps& matching_steps,
                       const MatchingStepsFlowGraph& default_steps) override;

 private:
  void GetUnmatchedFlowGraphsByMdIndex(const FlowGraphs& flow_graphs,
                                       FlowGraphDoubleMap& flow_graphs_map);
  Direction direction_;
};

}  // namespace bindiff
}  // namespace security

#endif  // CALL_GRAPH_MATCH_FUNCTION_FLOW_GRAPH_MDINDEX_H_
