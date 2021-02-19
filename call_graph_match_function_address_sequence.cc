// Copyright 2011-2021 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
      const uint64_t feature =
          1000 * (counts[Counts::kInstructionsLibrary] +
                  counts[Counts::kInstructionsNonLibrary]) +
          sequence;
      sorted_by_size.emplace(feature, flow_graph);
    }
    ++sequence;
  }

  sequence = 0;
  for (auto it = sorted_by_size.rbegin(), end = sorted_by_size.rend();
       it != end; ++it, ++sequence) {
    flow_graphs_map.emplace(sequence, it->second);
  }
}

}  // namespace security::bindiff
