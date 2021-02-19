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

#ifndef FLOW_GRAPH_MATCH_BASIC_BLOCK_INSTRUCTION_COUNT_H_
#define FLOW_GRAPH_MATCH_BASIC_BLOCK_INSTRUCTION_COUNT_H_

#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security::bindiff {

// Basic blocks are matched according to their number of instructions.
class MatchingStepInstructionCount : public MatchingStepFlowGraph {
 public:
  MatchingStepInstructionCount()
      : MatchingStepFlowGraph("basicBlock: instruction count matching",
                              "Basic Block: Instruction Count") {}

  bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                       const VertexSet& vertices1, const VertexSet& vertices2,
                       FixedPoint* fixed_point, MatchingContext* context,
                       MatchingStepsFlowGraph* matching_steps) override;

 private:
  void GetUnmatchedBasicBlocksByInstructionCount(
      const FlowGraph* flow_graph, const VertexSet& vertices,
      VertexIntMap* basic_blocks_map);
};

}  // namespace security::bindiff

#endif  // FLOW_GRAPH_MATCH_BASIC_BLOCK_INSTRUCTION_COUNT_H_
