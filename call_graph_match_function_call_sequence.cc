#include "third_party/zynamics/bindiff/call_graph_match_function_call_sequence.h"

namespace security {
namespace bindiff {

bool MatchingStepCallSequence::FindFixedPoints(
    const FlowGraph* primary_parent, const FlowGraph* secondary_parent,
    FlowGraphs& flow_graphs_1, FlowGraphs& flow_graphs_2,
    MatchingContext& context, MatchingSteps& matching_steps,
    const MatchingStepsFlowGraph& default_steps) {
  if (!primary_parent || !secondary_parent) {
    return false;
  }

  FlowGraphIntMap flow_graphs_map_1;
  FlowGraphIntMap flow_graphs_map_2;
  GetUnmatchedFlowGraphsByCallLevel(primary_parent, flow_graphs_1,
                                    flow_graphs_map_1, accuracy_);
  GetUnmatchedFlowGraphsByCallLevel(secondary_parent, flow_graphs_2,
                                    flow_graphs_map_2, accuracy_);
  return ::security::bindiff::FindFixedPoints(
      primary_parent, secondary_parent, flow_graphs_map_1, flow_graphs_map_2,
      &context, matching_steps, default_steps);
}

void MatchingStepCallSequence::GetUnmatchedFlowGraphsByCallLevel(
    const FlowGraph* parent, const FlowGraphs& flow_graphs,
    FlowGraphIntMap& flow_graphs_map,
    MatchingStepCallSequence::Accuracy accuracy) {
  flow_graphs_map.clear();
  if (accuracy == MatchingStepCallSequence::SEQUENCE) {
    FlowGraphIntMap exact_map;
    GetUnmatchedFlowGraphsByCallLevel(parent, flow_graphs, exact_map,
                                      MatchingStepCallSequence::EXACT);

    FlowGraphIntMap::key_type index = 0;
    for (FlowGraphIntMap::const_iterator i = exact_map.begin();
         i != exact_map.end(); ++i, ++index) {
      flow_graphs_map.emplace(index, i->second);
    }
  } else {
    for (FlowGraph* graph : flow_graphs) {
      if (IsValidCandidate(graph)) {
        const FlowGraph::Level level =
            parent->GetLevelForCallAddress(graph->GetEntryPointAddress());
        FlowGraphIntMap::key_type index = 0;
        if (accuracy == MatchingStepCallSequence::EXACT) {
          index = (level.first << 16) + level.second;
        } else if (accuracy == MatchingStepCallSequence::TOPOLOGY) {
          index = level.first;
        }
        flow_graphs_map.emplace(index, graph);
      }
    }
  }
}

}  // namespace bindiff
}  // namespace security
