#include "third_party/zynamics/bindiff/call_graph_match_function_call_graph_edges_proximity_mdindex.h"

namespace security::bindiff {

bool MatchingStepEdgesProximityMdIndex::FindFixedPoints(
    const FlowGraph* primary_parent, const FlowGraph* secondary_parent,
    FlowGraphs& /*flow_graphs_1*/, FlowGraphs& /*flow_graphs_2*/,
    MatchingContext& context, MatchingSteps& matching_steps,
    const MatchingStepsFlowGraph& default_steps) {
  EdgeFeatures edges1;
  EdgeFeatures edges2;
  GetUnmatchedEdgesProximityMdIndex(&context.primary_call_graph_, &edges1);
  GetUnmatchedEdgesProximityMdIndex(&context.secondary_call_graph_, &edges2);
  return ::security::bindiff::FindFixedPointsEdge(
      primary_parent, secondary_parent, &edges1, &edges2, &context,
      &matching_steps, default_steps);
}

void MatchingStepEdgesProximityMdIndex::GetUnmatchedEdgesProximityMdIndex(
    CallGraph* call_graph, EdgeFeatures* edges) {
  edges->clear();
  // TODO(cblichmann): Very bad worst case behavior with high connectivity
  //                   graphs!
  for (auto [edge, end] = boost::edges(call_graph->GetGraph()); edge != end;
       ++edge) {
    if (call_graph->IsDuplicate(*edge) || call_graph->IsCircular(*edge)) {
      continue;
    }

    FlowGraph* source =
        call_graph->GetFlowGraph(boost::source(*edge, call_graph->GetGraph()));
    if (!source || source->GetMdIndex() == 0.0) {
      continue;
    }

    FlowGraph* target =
        call_graph->GetFlowGraph(boost::target(*edge, call_graph->GetGraph()));
    if (!target || target->GetMdIndex() == 0.0) {
      continue;
    }

    // Already a fixed point, no need to evaluate again.
    if (source->GetFixedPoint() != 0 && target->GetFixedPoint() != 0) {
      continue;
    }

    edges->push_back({*edge, call_graph->GetProximityMdIndex(*edge), 0.0});
  }
}

}  // namespace security::bindiff
