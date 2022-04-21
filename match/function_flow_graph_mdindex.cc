// Copyright 2011-2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#include "third_party/zynamics/bindiff/match/function_flow_graph_mdindex.h"

namespace security::bindiff {

bool MatchingStepFlowGraphMdIndex::FindFixedPoints(
    const FlowGraph* primary_parent, const FlowGraph* secondary_parent,
    FlowGraphs& flow_graphs_1, FlowGraphs& flow_graphs_2,
    MatchingContext& context, MatchingSteps& matching_steps,
    const MatchingStepsFlowGraph& default_steps) {
  FlowGraphDoubleMap flow_graphs_map_1;
  FlowGraphDoubleMap flow_graphs_map_2;
  GetUnmatchedFlowGraphsByMdIndex(flow_graphs_1, flow_graphs_map_1);
  GetUnmatchedFlowGraphsByMdIndex(flow_graphs_2, flow_graphs_map_2);
  return ::security::bindiff::FindFixedPoints(
      primary_parent, secondary_parent, flow_graphs_map_1, flow_graphs_map_2,
      &context, matching_steps, default_steps);
}

void MatchingStepFlowGraphMdIndex::GetUnmatchedFlowGraphsByMdIndex(
    const FlowGraphs& flow_graphs, FlowGraphDoubleMap& flow_graphs_map) {
  flow_graphs_map.clear();
  for (FlowGraph* graph : flow_graphs) {
    if (IsValidCandidate(graph)) {
      flow_graphs_map.emplace(direction_ == kTopDown
                                  ? graph->GetMdIndex()
                                  : graph->GetMdIndexInverted(),
                              graph);
    }
  }
}

}  // namespace security::bindiff
