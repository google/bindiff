// Copyright 2011-2024 Google LLC
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

#include "third_party/zynamics/bindiff/match/basic_block_self_loop.h"

#include "third_party/zynamics/bindiff/match/flow_graph.h"

namespace security::bindiff {

bool MatchingStepSelfLoops::FindFixedPoints(
    FlowGraph* primary, FlowGraph* secondary, const VertexSet& vertices1,
    const VertexSet& vertices2, FixedPoint* fixed_point,
    MatchingContext* context, MatchingStepsFlowGraph* matching_steps) {
  VertexIntMap vertex_map_1;
  VertexIntMap vertex_map_2;
  GetUnmatchedBasicBlocksSelfLoops(primary, vertices1, &vertex_map_1);
  GetUnmatchedBasicBlocksSelfLoops(secondary, vertices2, &vertex_map_2);
  return FindFixedPointsBasicBlockInternal(primary, secondary, &vertex_map_1,
                                           &vertex_map_2, fixed_point, context,
                                           matching_steps);
}

void MatchingStepSelfLoops::GetUnmatchedBasicBlocksSelfLoops(
    const FlowGraph* flow_graph, const VertexSet& vertices,
    VertexIntMap* basic_blocks_map) {
  basic_blocks_map->clear();
  for (const auto& vertex : vertices) {
    if (flow_graph->GetFixedPoint(vertex)) {
      continue;
    }

    size_t count = 0;
    for (auto [it, end] = boost::out_edges(vertex, flow_graph->GetGraph());
         it != end; ++it) {
      count += boost::source(*it, flow_graph->GetGraph()) ==
               boost::target(*it, flow_graph->GetGraph());
    }
    if (count) {
      basic_blocks_map->emplace(count, vertex);
    }
  }
}

}  // namespace security::bindiff
