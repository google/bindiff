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

#ifndef MATCH_BASIC_BLOCK_HASH_H_
#define MATCH_BASIC_BLOCK_HASH_H_

#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/bindiff/match/flow_graph.h"

namespace security::bindiff {

// Matches basic blocks based on a binary hash of their raw bytes. Only used on
// basic blocks with a minimum number of specified instructions.
class MatchingStepHashBasicBlock : public MatchingStepFlowGraph {
 public:
  explicit MatchingStepHashBasicBlock(int min_instructions)
      : MatchingStepFlowGraph(
            absl::StrCat("basicBlock: hash matching (", min_instructions,
                         " instructions minimum)"),
            absl::StrCat("Basic Block: Hash (", min_instructions,
                         " Instructions Minimum)")),
        min_instructions_(min_instructions) {}

  bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                       const VertexSet& vertices1, const VertexSet& vertices2,
                       FixedPoint* fixed_point, MatchingContext* context,
                       MatchingStepsFlowGraph* matching_steps) override;

 private:
  void GetUnmatchedBasicBlocksByHash(const FlowGraph* flow_graph,
                                     const VertexSet& vertices,
                                     VertexIntMap* basic_blocks_map);

  int min_instructions_;
};

}  // namespace security::bindiff

#endif  // MATCH_BASIC_BLOCK_HASH_H_
