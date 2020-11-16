// Copyright 2011-2020 Google LLC
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

#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_edges_prime.h"

#include <cstdint>

#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security::bindiff {

bool MatchingStepEdgesPrimeProduct::FindFixedPoints(
    FlowGraph* primary, FlowGraph* secondary, const VertexSet& vertices1,
    const VertexSet& vertices2, FixedPoint* fixed_point,
    MatchingContext* context, MatchingStepsFlowGraph* matching_steps) {
  EdgeIntMap primary_edges, secondary_edges;
  GetUnmatchedEdgesPrimeProduct(*primary, vertices1, &primary_edges);
  GetUnmatchedEdgesPrimeProduct(*secondary, vertices2, &secondary_edges);
  return FindFixedPointsBasicBlockEdgeInternal(&primary_edges, &secondary_edges,
                                               primary, secondary, fixed_point,
                                               context, matching_steps);
}

void MatchingStepEdgesPrimeProduct::GetUnmatchedEdgesPrimeProduct(
    const FlowGraph& flow_graph, const VertexSet& vertices, EdgeIntMap* edges) {
  edges->clear();
  for (auto [edge, end] = boost::edges(flow_graph.GetGraph()); edge != end;
       ++edge) {
    if (flow_graph.IsCircular(*edge)) {
      continue;
    }
    const auto source = boost::source(*edge, flow_graph.GetGraph());
    const auto target = boost::target(*edge, flow_graph.GetGraph());
    if ((flow_graph.GetFixedPoint(source) == nullptr ||
         flow_graph.GetFixedPoint(target) == nullptr) &&
        (vertices.find(source) != vertices.end() ||
         vertices.find(target) != vertices.end())) {
      const uint64_t prime =
          flow_graph.GetPrime(source) + flow_graph.GetPrime(target) + 1;
      edges->emplace(prime, *edge);
    }
  }
}

}  // namespace security::bindiff
