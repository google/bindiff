#include "third_party/zynamics/bindiff/call_graph_match_function_name_hash.h"

#include "third_party/zynamics/binexport/hash.h"

namespace security {
namespace bindiff {

bool MatchingStepName::FindFixedPoints(
    const FlowGraph* primary_parent, const FlowGraph* secondary_parent,
    FlowGraphs& flow_graphs_1, FlowGraphs& flow_graphs_2,
    MatchingContext& context, MatchingSteps& matching_steps,
    const MatchingStepsFlowGraph& default_steps) {
  FlowGraphIntMap flow_graphs_map_1;
  FlowGraphIntMap flow_graphs_map_2;
  GetUnmatchedFlowGraphsByNameHash(flow_graphs_1, flow_graphs_map_1);
  GetUnmatchedFlowGraphsByNameHash(flow_graphs_2, flow_graphs_map_2);
  return ::security::bindiff::FindFixedPoints(
      primary_parent, secondary_parent, flow_graphs_map_1, flow_graphs_map_2,
      &context, matching_steps, default_steps);
}

void MatchingStepName::GetUnmatchedFlowGraphsByNameHash(
    const FlowGraphs& flow_graphs, FlowGraphIntMap& flow_graphs_map) {
  flow_graphs_map.clear();
  for (FlowGraph* graph : flow_graphs) {
    // Don't call this, we need special logic here:
    // isValidCandidate(graph))
    // Match based on demangled name if that is available, fall back on
    // raw name if not. We use the demangled name because that should be
    // the same even if the code has been compiled with different compilers
    // and thus different mangling schemes.
    if (!graph->GetFixedPoint() && graph->HasRealName()) {
      const string& name = graph->GetGoodName();
      flow_graphs_map.emplace(GetSdbmHash(name), graph);
    }
  }
}

}  // namespace bindiff
}  // namespace security
