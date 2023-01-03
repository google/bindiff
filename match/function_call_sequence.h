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

#ifndef MATCH_FUNCTION_CALL_SEQUENCE_H_
#define MATCH_FUNCTION_CALL_SEQUENCE_H_

#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/bindiff/match/call_graph.h"

namespace security::bindiff {

// Special algorithm that is only used for functions with matched parents
// (callers). The point of the call at the call site is determined as a tuple:
// topological basic block level, instruction number in the basic block,
// address. The child function (callee) is matched if basic block level and
// instruction number match (exact), if only the basic block level match
// (topology) or simply ordered by call site address (sequence). This produces
// very weak matches in general, but may be a good strategy if the parent
// function was matched correctly. In that case it is not unlikely that it will
// call functions in the same order in both binaries.
class MatchingStepCallSequence : public MatchingStep {
 public:
  enum Accuracy { EXACT = 0, TOPOLOGY, SEQUENCE };

  explicit MatchingStepCallSequence(Accuracy accuracy)
      : MatchingStep(absl::StrCat("function: call sequence matching(",
                                  accuracy == EXACT      ? "exact)"
                                  : accuracy == TOPOLOGY ? "topology)"
                                                         : "sequence)"),
                     absl::StrCat("Function: Call Sequence (",
                                  accuracy == EXACT      ? "Exact)"
                                  : accuracy == TOPOLOGY ? "Topology)"
                                                         : "Sequence)")),
        accuracy_(accuracy) {}

  bool FindFixedPoints(const FlowGraph* primary_parent,
                       const FlowGraph* secondary_parent,
                       FlowGraphs& flow_graphs_1, FlowGraphs& flow_graphs_2,
                       MatchingContext& context, MatchingSteps& matching_steps,
                       const MatchingStepsFlowGraph& default_steps) override;

 private:
  void GetUnmatchedFlowGraphsByCallLevel(
      const FlowGraph* parent, const FlowGraphs& flow_graphs,
      FlowGraphIntMap& flow_graphs_map,
      MatchingStepCallSequence::Accuracy accuracy);

  Accuracy accuracy_;
};

}  // namespace security::bindiff

#endif  // MATCH_FUNCTION_CALL_SEQUENCE_H_
