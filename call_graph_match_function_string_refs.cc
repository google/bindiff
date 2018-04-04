#include "third_party/zynamics/bindiff/call_graph_match_function_string_refs.h"

namespace security {
namespace bindiff {

bool MatchingStepFunctionStringReferences ::FindFixedPoints(
    const FlowGraph* primary_parent, const FlowGraph* secondary_parent,
    FlowGraphs& flow_graphs_1, FlowGraphs& flow_graphs_2,
    MatchingContext& context, MatchingSteps& matching_steps,
    const MatchingStepsFlowGraph& default_steps) {
  FlowGraphIntMap flow_graphs_map_1;
  FlowGraphIntMap flow_graphs_map_2;
  GetUnmatchedFlowGraphsByStringReferences(flow_graphs_1, flow_graphs_map_1);
  GetUnmatchedFlowGraphsByStringReferences(flow_graphs_2, flow_graphs_map_2);
  return ::FindFixedPoints(primary_parent, secondary_parent, flow_graphs_map_1,
                           flow_graphs_map_2, &context, matching_steps,
                           default_steps);
}

void MatchingStepFunctionStringReferences ::
    GetUnmatchedFlowGraphsByStringReferences(const FlowGraphs& flow_graphs,
                                             FlowGraphIntMap& flow_graphs_map) {
  flow_graphs_map.clear();
  for (FlowGraph* graph : flow_graphs) {
    const uint32_t hash = graph->GetStringReferences();
    if (hash > 1 && IsValidCandidate(graph)) {
      flow_graphs_map.emplace(hash, graph);
    }
  }
}

}  // namespace bindiff
}  // namespace security
