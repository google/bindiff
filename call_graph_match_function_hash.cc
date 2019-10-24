#include "third_party/zynamics/bindiff/call_graph_match_function_hash.h"

#include "third_party/zynamics/binexport/hash.h"

namespace security::bindiff {

bool MatchingStepHash::FindFixedPoints(
    const FlowGraph* primary_parent, const FlowGraph* secondary_parent,
    FlowGraphs& flow_graphs_1, FlowGraphs& flow_graphs_2,
    MatchingContext& context, MatchingSteps& matching_steps,
    const MatchingStepsFlowGraph& default_steps) {
  FlowGraphIntMap flow_graphs_map_1, flow_graphs_map_2;
  GetUnmatchedFlowGraphsByHash(flow_graphs_1, flow_graphs_map_1);
  GetUnmatchedFlowGraphsByHash(flow_graphs_2, flow_graphs_map_2);
  return ::security::bindiff::FindFixedPoints(
      primary_parent, secondary_parent, flow_graphs_map_1, flow_graphs_map_2,
      &context, matching_steps, default_steps);
}

void MatchingStepHash::GetUnmatchedFlowGraphsByHash(
    const FlowGraphs& flow_graphs, FlowGraphIntMap& flow_graphs_map) {
  flow_graphs_map.clear();
  for (FlowGraph* graph : flow_graphs) {
    if (!graph->IsTrivial() && IsValidCandidate(graph)) {
      flow_graphs_map.emplace(graph->GetHash(), graph);
    }
  }
}

}  // namespace security::bindiff
