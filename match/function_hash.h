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

#ifndef MATCH_FUNCTION_HASH_H_
#define MATCH_FUNCTION_HASH_H_

#include "third_party/zynamics/bindiff/match/call_graph.h"

namespace security::bindiff {

// Matches functions based on a hash of the original raw function bytes. Thus
// two functions matched by this algorithm should be identical on the byte
// level.
class MatchingStepHash : public MatchingStep {
 public:
  MatchingStepHash()
      : MatchingStep("function: hash matching", "Function: Hash") {}

  bool FindFixedPoints(const FlowGraph* primary_parent,
                       const FlowGraph* secondary_parent,
                       FlowGraphs& flow_graphs_1, FlowGraphs& flow_graphs_2,
                       MatchingContext& context, MatchingSteps& matching_steps,
                       const MatchingStepsFlowGraph& default_steps) override;

 private:
  void GetUnmatchedFlowGraphsByHash(const FlowGraphs& flow_graphs,
                                    FlowGraphIntMap& flow_graphs_map);
};

}  // namespace security::bindiff

#endif  // MATCH_FUNCTION_HASH_H_
