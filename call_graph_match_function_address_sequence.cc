#include "third_party/zynamics/bindiff/call_graph_match_function_address_sequence.h"

#include "third_party/zynamics/bindiff/differ.h"

namespace security::bindiff {

bool MatchingStepSequence::FindFixedPoints(
    const FlowGraph* primary_parent, const FlowGraph* secondary_parent,
    FlowGraphs& flow_graphs_1, FlowGraphs& flow_graphs_2,
    MatchingContext& context, MatchingSteps& matching_steps,
    const MatchingStepsFlowGraph& default_steps) {
  FlowGraphIntMap flow_graphs_map_1;
  FlowGraphIntMap flow_graphs_map_2;
  GetUnmatchedFlowGraphsByAddress(flow_graphs_1, flow_graphs_map_1);
  GetUnmatchedFlowGraphsByAddress(flow_graphs_2, flow_graphs_map_2);
  return ::security::bindiff::FindFixedPoints(
      primary_parent, secondary_parent, flow_graphs_map_1, flow_graphs_map_2,
      &context, matching_steps, default_steps);
}

void MatchingStepSequence::GetUnmatchedFlowGraphsByAddress(
    const FlowGraphs& flow_graphs, FlowGraphIntMap& flow_graphs_map) {
  flow_graphs_map.clear();
  FlowGraphIntMap sorted_by_size;
  uint64_t sequence = 0;
  for (FlowGraph* flow_graph : flow_graphs) {
    // TODO(cblichmann): kLCS for most similar sequence of raw bytes?
    if (flow_graph->GetBasicBlockCount() && IsValidCandidate(flow_graph)) {
      Counts counts;
      Count(*flow_graph, &counts);
      // This should sort by number of instructions first and address second.
      const auto feature =
          1000 * static_cast<uint64_t>(counts["instructions (library)"] +
                                     counts["instructions (non-library)"]) +
          sequence;
      sorted_by_size.emplace(feature, flow_graph);
    }
    ++sequence;
  }

  sequence = 0;
  for (FlowGraphIntMap::const_reverse_iterator i = sorted_by_size.rbegin(),
                                               end = sorted_by_size.rend();
       i != end; ++i, ++sequence) {
    flow_graphs_map.emplace(sequence, i->second);
  }
}

}  // namespace security::bindiff
