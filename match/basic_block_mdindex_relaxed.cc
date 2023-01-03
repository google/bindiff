// Copyright 2011-2023 Google LLC
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

#include "third_party/zynamics/bindiff/match/basic_block_mdindex_relaxed.h"

#include "third_party/zynamics/bindiff/match/flow_graph.h"

namespace security::bindiff {

bool MatchingStepMdIndexRelaxed ::FindFixedPoints(
    FlowGraph* primary, FlowGraph* secondary, const VertexSet& vertices1,
    const VertexSet& vertices2, FixedPoint* fixed_point,
    MatchingContext* context, MatchingStepsFlowGraph* matching_steps) {
  VertexDoubleMap vertex_map_1;
  VertexDoubleMap vertex_map_2;
  GetUnmatchedBasicBlocksByMdIndexRelaxed(primary, vertices1, &vertex_map_1);
  GetUnmatchedBasicBlocksByMdIndexRelaxed(secondary, vertices2, &vertex_map_2);
  return FindFixedPointsBasicBlockInternal(primary, secondary, &vertex_map_1,
                                           &vertex_map_2, fixed_point, context,
                                           matching_steps);
}

void MatchingStepMdIndexRelaxed ::GetUnmatchedBasicBlocksByMdIndexRelaxed(
    const FlowGraph* flow_graph, const VertexSet& vertices,
    VertexDoubleMap* basic_blocks_map) {
  basic_blocks_map->clear();
  for (auto vertex : vertices) {
    if (!flow_graph->GetFixedPoint(vertex)) {
      basic_blocks_map->emplace(CalculateMdIndexNode(*flow_graph, vertex),
                                vertex);
    }
  }
}

}  // namespace security::bindiff
