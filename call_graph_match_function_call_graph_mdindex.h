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

#ifndef CALL_GRAPH_MATCH_FUNCTION_CALL_GRAPH_MDINDEX_H_
#define CALL_GRAPH_MATCH_FUNCTION_CALL_GRAPH_MDINDEX_H_

#include "third_party/zynamics/bindiff/call_graph_match.h"

#include "third_party/absl/strings/str_cat.h"

namespace security::bindiff {

// Matches functions based on their position in the callgraph. The call graph
// leading up to that function must be structurally identical as viewed from the
// program entrypoint (top down) or its exits (bottom up).
class MatchingStepCallGraphMdIndex : public MatchingStep {
 public:
  explicit MatchingStepCallGraphMdIndex(Direction direction)
      : MatchingStep(
            absl::StrCat("function: MD index matching (callGraph MD index, ",
                         direction == kTopDown ? "top down)" : "bottom up)"),
            absl::StrCat("Function: MD Index (Call Graph, ",
                         direction == kTopDown ? "Top Down)" : "Bottom Up)")),
        direction_(direction) {}

  bool FindFixedPoints(const FlowGraph* primary_parent,
                       const FlowGraph* secondary_parent,
                       FlowGraphs& flow_graphs_1, FlowGraphs& flow_graphs_2,
                       MatchingContext& context, MatchingSteps& matching_steps,
                       const MatchingStepsFlowGraph& default_steps) override;

 private:
  void GetUnmatchedFlowGraphsByCallGraphMDIndex(
      const FlowGraphs& flow_graphs, FlowGraphDoubleMap& flow_graphs_map);

  Direction direction_;
};

}  // namespace security::bindiff

#endif  // CALL_GRAPH_MATCH_FUNCTION_CALL_GRAPH_MDINDEX_H_
