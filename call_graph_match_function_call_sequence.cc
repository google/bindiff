// Copyright 2011-2020 Google LLC
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

#include "third_party/zynamics/bindiff/call_graph_match_function_call_sequence.h"

namespace security::bindiff {

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

}  // namespace security::bindiff
