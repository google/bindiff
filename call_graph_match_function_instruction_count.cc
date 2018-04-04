#include "third_party/zynamics/bindiff/call_graph_match_function_instruction_count.h"

#include "third_party/zynamics/bindiff/differ.h"

namespace security {
namespace bindiff {

bool MatchingStepFunctionInstructionCount::FindFixedPoints(
    const FlowGraph* primary_parent, const FlowGraph* secondary_parent,
    FlowGraphs& flow_graphs_1, FlowGraphs& flow_graphs_2,
    MatchingContext& context, MatchingSteps& matching_steps,
    const MatchingStepsFlowGraph& default_steps) {
  FlowGraphIntMap flow_graphs_map_1;
  FlowGraphIntMap flow_graphs_map_2;
  GetUnmatchedFlowGraphsByInstructionCount(flow_graphs_1, flow_graphs_map_1);
  GetUnmatchedFlowGraphsByInstructionCount(flow_graphs_2, flow_graphs_map_2);
  return ::FindFixedPoints(primary_parent, secondary_parent, flow_graphs_map_1,
                           flow_graphs_map_2, &context, matching_steps,
                           default_steps);
}

void MatchingStepFunctionInstructionCount::
    GetUnmatchedFlowGraphsByInstructionCount(const FlowGraphs& flow_graphs,
                                             FlowGraphIntMap& flow_graphs_map) {
  for (FlowGraph* graph : flow_graphs) {
    if (IsValidCandidate(graph) && boost::num_vertices(graph->GetGraph())) {
      Counts counts;
      Count(*graph, &counts);
      const uint64_t instruction_count = counts["instructions (library)"] +
                                       counts["instructions (non-library)"];
      flow_graphs_map.emplace(instruction_count, graph);
    }
  }
}

}  // namespace bindiff
}  // namespace security
