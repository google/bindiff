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

#include "third_party/zynamics/bindiff/match/basic_block_edges_lengauer_tarjan.h"

namespace security::bindiff {

bool MatchingStepEdgesLoop::FindFixedPoints(
    FlowGraph* primary, FlowGraph* secondary, const VertexSet& vertices1,
    const VertexSet& vertices2, FixedPoint* fixed_point,
    MatchingContext* context, MatchingStepsFlowGraph* matching_steps) {
  EdgeIntMap primary_edges;
  EdgeIntMap secondary_edges;
  GetUnmatchedEdgesLoop(context, *primary, vertices1, &primary_edges);
  GetUnmatchedEdgesLoop(context, *secondary, vertices2, &secondary_edges);
  return FindFixedPointsBasicBlockEdgeInternal(&primary_edges, &secondary_edges,
                                               primary, secondary, fixed_point,
                                               context, matching_steps);
}

void MatchingStepEdgesLoop::GetUnmatchedEdgesLoop(MatchingContext* context,
                                                  const FlowGraph& flow_graph,
                                                  const VertexSet& vertices,
                                                  EdgeIntMap* edges) {
  const MatchingContext::FeatureId feature_id = MatchingContext::kEdgeProperies;
  EdgesByFlowGraph* cached = nullptr;
  if (context->HasCachedFeatures(feature_id)) {
    cached = context->GetCachedFeatures<EdgesByFlowGraph*>(feature_id);
    if (!cached) {
      cached = new EdgesByFlowGraph();
      context->SetCachedFeatures(feature_id, cached, FeatureDestructor);
    }
  }
  edges->clear();
  EdgeFeatures* features = nullptr;
  if (cached) {
    auto [entry, inserted] = cached->emplace(&flow_graph, EdgeFeatures());
    // We previously cached data for this flow graph, if it's already present in
    // the map.
    features = &entry->second;
    if (!inserted) {
      // Element wasn't added, features list is filled in.
      int edge_index = 0;
      for (auto [edge, end] = boost::edges(flow_graph.GetGraph()); edge != end;
           ++edge, ++edge_index) {
        int edge_feature = (*features)[edge_index];
        if (((edge_feature & kIsCircular) != 0) ||
            ((edge_feature & kIsEdgeDominated) == 0)) {
          continue;
        }
        const auto source = boost::source(*edge, flow_graph.GetGraph());
        const auto target = boost::target(*edge, flow_graph.GetGraph());
        if ((flow_graph.GetFixedPoint(source) == nullptr ||
             flow_graph.GetFixedPoint(target) == nullptr) &&
            (vertices.count(source) > 0 || vertices.count(target) > 0)) {
          edges->emplace(1, *edge);
        }
      }
      return;
    }
  }
  // Non-cached version, also fills cache, if needed.
  for (auto [edge, end] = boost::edges(flow_graph.GetGraph()); edge != end;
       ++edge) {
    int edge_feature = 0;
    edge_feature |= flow_graph.IsCircular(*edge) ? kIsCircular : 0;
    edge_feature |=
        (flow_graph.GetFlags(*edge) & FlowGraph::EDGE_DOMINATED) != 0
            ? kIsEdgeDominated
            : 0;
    if (features) {
      features->emplace_back(edge_feature);
    }
    if (((edge_feature & kIsCircular) != 0) ||
        ((edge_feature & kIsEdgeDominated) == 0)) {
      continue;
    }

    const auto source = boost::source(*edge, flow_graph.GetGraph());
    const auto target = boost::target(*edge, flow_graph.GetGraph());
    if (!flow_graph.IsCircular(*edge) &&
        (flow_graph.GetFixedPoint(source) == nullptr ||
         flow_graph.GetFixedPoint(target) == nullptr) &&
        (vertices.find(source) != vertices.end() ||
         vertices.find(target) != vertices.end()) &&
        (flow_graph.GetFlags(*edge) & FlowGraph::EDGE_DOMINATED)) {
      edges->emplace(1, *edge);
    }
  }
}

}  // namespace security::bindiff
