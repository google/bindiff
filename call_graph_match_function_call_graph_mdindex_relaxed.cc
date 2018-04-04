#include "third_party/zynamics/bindiff/call_graph_match_function_call_graph_mdindex_relaxed.h"

namespace security {
namespace bindiff {

bool MatchingStepCallGraphMdIndexRelaxed::FindFixedPoints(
    const FlowGraph* primary_parent, const FlowGraph* secondary_parent,
    FlowGraphs& flow_graphs_1, FlowGraphs& flow_graphs_2,
    MatchingContext& context, MatchingSteps& matching_steps,
    const MatchingStepsFlowGraph& default_steps) {
  FlowGraphDoubleMap flow_graphs_map_1;
  FlowGraphDoubleMap flow_graphs_map_2;
  GetUnmatchedFlowGraphsByMdindexRelaxed(flow_graphs_1, flow_graphs_map_1);
  GetUnmatchedFlowGraphsByMdindexRelaxed(flow_graphs_2, flow_graphs_map_2);
  return ::FindFixedPoints(primary_parent, secondary_parent, flow_graphs_map_1,
                           flow_graphs_map_2, &context, matching_steps,
                           default_steps);
}

void MatchingStepCallGraphMdIndexRelaxed::
    GetUnmatchedFlowGraphsByMdindexRelaxed(
        const FlowGraphs& flow_graphs, FlowGraphDoubleMap& flow_graphs_map) {
  flow_graphs_map.clear();
  for (FlowGraph* graph : flow_graphs) {
    if (IsValidCandidate(graph)) {
      flow_graphs_map.emplace(
          CalculateMdIndexNode(*graph->GetCallGraph(),
                               graph->GetCallGraphVertex(), vertex_bfs_index),
          graph);
    }
  }
}

}  // namespace bindiff
}  // namespace security
