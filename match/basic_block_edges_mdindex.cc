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

#include "third_party/zynamics/bindiff/match/basic_block_edges_mdindex.h"

#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/bindiff/match/flow_graph.h"

namespace security::bindiff {

bool MatchingStepEdgesMdIndex ::FindFixedPoints(
    FlowGraph* primary, FlowGraph* secondary, const VertexSet& vertices1,
    const VertexSet& vertices2, FixedPoint* fixed_point,
    MatchingContext* context, MatchingStepsFlowGraph* matching_steps) {
  EdgeDoubleMap primary_edges;
  EdgeDoubleMap secondary_edges;
  GetUnmatchedEdgesMdIndex(*primary, vertices1, &primary_edges);
  GetUnmatchedEdgesMdIndex(*secondary, vertices2, &secondary_edges);
  return FindFixedPointsBasicBlockEdgeInternal(&primary_edges, &secondary_edges,
                                               primary, secondary, fixed_point,
                                               context, matching_steps);
}

void MatchingStepEdgesMdIndex::GetUnmatchedEdgesMdIndex(
    const FlowGraph& flow_graph, const VertexSet& vertices,
    EdgeDoubleMap* edges) {
  edges->clear();
  FlowGraph::EdgeIterator edge, end;
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
      edges->emplace(direction_ == kTopDown
                         ? flow_graph.GetMdIndex(*edge)
                         : flow_graph.GetMdIndexInverted(*edge),
                     *edge);
    }
  }
}

}  // namespace security::bindiff
