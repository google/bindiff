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

#ifndef MATCH_BASIC_BLOCK_EDGES_LENGAUER_TARJAN_H_
#define MATCH_BASIC_BLOCK_EDGES_LENGAUER_TARJAN_H_

#include "third_party/absl/container/flat_hash_map.h"
#include "third_party/zynamics/bindiff/match/flow_graph.h"

namespace security::bindiff {

// Matches the back edges of loops which have been determined using the
// Lengauer-Tarjan algorithm.
class MatchingStepEdgesLoop : public MatchingStepFlowGraph {
 public:
  MatchingStepEdgesLoop()
      : MatchingStepFlowGraph("basicBlock: edges Lengauer Tarjan dominated",
                              "Basic Block: Edges Lengauer Tarjan Dominated") {
    edge_matching_ = true;
  }

  bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                       const VertexSet& vertices1, const VertexSet& vertices2,
                       FixedPoint* fixed_point, MatchingContext* context,
                       MatchingStepsFlowGraph* matching_steps) override;

 private:
  using EdgeFeatures = std::vector<int>;
  using EdgesByFlowGraph = absl::flat_hash_map<const FlowGraph*, EdgeFeatures>;

  enum {
    kIsCircular = 1 << 0,
    kIsEdgeDominated = 1 << 1,
  };

  static void FeatureDestructor(EdgesByFlowGraph* features) { delete features; }

  void GetUnmatchedEdgesLoop(MatchingContext* context,
                             const FlowGraph& flow_graph,
                             const VertexSet& vertices, EdgeIntMap* edges);
};

}  // namespace security::bindiff

#endif  // MATCH_BASIC_BLOCK_EDGES_LENGAUER_TARJAN_H_
