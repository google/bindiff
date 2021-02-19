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

#ifndef FLOW_GRAPH_MATCH_BASIC_BLOCK_EDGES_PRIME_H_
#define FLOW_GRAPH_MATCH_BASIC_BLOCK_EDGES_PRIME_H_

#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security::bindiff {

// Matches flow graph edges if source and target basic block instruction prime
// products match. Thus both basic blocks contain identical instructions,
// potentially ordered differently.
class MatchingStepEdgesPrimeProduct : public MatchingStepFlowGraph {
 public:
  MatchingStepEdgesPrimeProduct()
      : MatchingStepFlowGraph("basicBlock: edges prime product",
                              "Basic Block: Edges Prime Product") {
    edge_matching_ = true;
  }

  bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                       const VertexSet& vertices1, const VertexSet& vertices2,
                       FixedPoint* fixed_point, MatchingContext* context,
                       MatchingStepsFlowGraph* matching_steps) override;

 private:
  void GetUnmatchedEdgesPrimeProduct(const FlowGraph& flow_graph,
                                     const VertexSet& vertices,
                                     EdgeIntMap* edges);
};

}  // namespace security::bindiff

#endif  // FLOW_GRAPH_MATCH_BASIC_BLOCK_EDGES_PRIME_H_
