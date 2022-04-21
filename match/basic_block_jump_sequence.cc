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

#include "third_party/zynamics/bindiff/match/basic_block_jump_sequence.h"

#include <cstdint>

#include "third_party/zynamics/bindiff/match/flow_graph.h"

namespace security::bindiff {

bool MatchingStepJumpSequence ::FindFixedPoints(
    FlowGraph* primary, FlowGraph* secondary, const VertexSet& vertices1,
    const VertexSet& vertices2, FixedPoint* fixed_point,
    MatchingContext* context, MatchingStepsFlowGraph* matching_steps) {
  VertexIntMap vertex_map_1;
  VertexIntMap vertex_map_2;
  GetUnmatchedBasicBlocksByJumpSequence(primary, vertices1, &vertex_map_1);
  GetUnmatchedBasicBlocksByJumpSequence(secondary, vertices2, &vertex_map_2);
  return FindFixedPointsBasicBlockInternal(primary, secondary, &vertex_map_1,
                                           &vertex_map_2, fixed_point, context,
                                           matching_steps);
}

void MatchingStepJumpSequence ::GetUnmatchedBasicBlocksByJumpSequence(
    const FlowGraph* flow_graph, const VertexSet& vertices,
    VertexIntMap* basic_blocks_map) {
  basic_blocks_map->clear();
  std::map<uint64_t, uint64_t> md_count;
  for (auto vertex : vertices) {
    if (!flow_graph->GetFixedPoint(vertex)) {
      const auto int_md_index = static_cast<uint64_t>(
          flow_graph->GetMdIndex(vertex) * 1000000000000000000ULL);
      basic_blocks_map->emplace(md_count[int_md_index]++ + int_md_index,
                                vertex);
    }
  }
}

}  // namespace security::bindiff
