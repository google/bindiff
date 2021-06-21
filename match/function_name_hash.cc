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

#include "third_party/zynamics/bindiff/match/function_name_hash.h"

#include "third_party/zynamics/binexport/hash.h"

namespace security::bindiff {

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
      const std::string& name = graph->GetGoodName();
      flow_graphs_map.emplace(GetSdbmHash(name), graph);
    }
  }
}

}  // namespace security::bindiff
