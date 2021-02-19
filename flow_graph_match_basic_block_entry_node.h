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

#ifndef FLOW_GRAPH_MATCH_BASIC_BLOCK_ENTRY_NODE_H_
#define FLOW_GRAPH_MATCH_BASIC_BLOCK_ENTRY_NODE_H_

#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security::bindiff {

// Matches the entry/exit point basic blocks. The entry point is uniquely
// identified by the function and usually has an indegree of 0. Exit points are
// vertices with outdegree 0.
class MatchingStepEntryNodes : public MatchingStepFlowGraph {
 public:
  explicit MatchingStepEntryNodes(Direction direction)
      : MatchingStepFlowGraph(
            absl::StrCat(
                "basicBlock: ", direction == kTopDown ? "entry" : "exit",
                " point matching"),
            absl::StrCat("Basic Block: ",
                         direction == kTopDown ? "Entry Point" : "Exit Point")),
        direction_(direction) {}

  bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                       const VertexSet& vertices1, const VertexSet& vertices2,
                       FixedPoint* fixed_point, MatchingContext* context,
                       MatchingStepsFlowGraph* matching_steps) override;

 private:
  void GetUnmatchedBasicBlocksEntryPoint(const FlowGraph* flow_graph,
                                         const VertexSet& vertices,
                                         VertexIntMap* basic_blocks_map);

  Direction direction_;
};

}  // namespace security::bindiff

#endif  // FLOW_GRAPH_MATCH_BASIC_BLOCK_ENTRY_NODE_H_
