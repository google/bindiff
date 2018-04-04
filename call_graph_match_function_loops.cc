#include "third_party/zynamics/bindiff/call_graph_match_function_loops.h"

#include "third_party/zynamics/binexport/hash.h"

namespace security {
namespace bindiff {

bool MatchingStepLoops::FindFixedPoints(
    const FlowGraph* primary_parent, const FlowGraph* secondary_parent,
    FlowGraphs& flow_graphs_1, FlowGraphs& flow_graphs_2,
    MatchingContext& context, MatchingSteps& matching_steps,
    const MatchingStepsFlowGraph& default_steps) {
  FlowGraphIntMap flow_graphs_map_1;
  FlowGraphIntMap flow_graphs_map_2;
  GetUnmatchedFlowGraphsByLoopCount(flow_graphs_1, flow_graphs_map_1);
  GetUnmatchedFlowGraphsByLoopCount(flow_graphs_2, flow_graphs_map_2);
  return ::FindFixedPoints(primary_parent, secondary_parent, flow_graphs_map_1,
                           flow_graphs_map_2, &context, matching_steps,
                           default_steps);
}

void MatchingStepLoops::GetUnmatchedFlowGraphsByLoopCount(
    const FlowGraphs& flow_graphs, FlowGraphIntMap& flow_graphs_map) {
  flow_graphs_map.clear();
  for (FlowGraph* graph : flow_graphs) {
    if (graph->GetLoopCount() && IsValidCandidate(graph)) {
      flow_graphs_map.emplace(graph->GetLoopCount(), graph);
    }
  }
}

}  // namespace bindiff
}  // namespace security
