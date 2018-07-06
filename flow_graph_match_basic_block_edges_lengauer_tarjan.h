#ifndef FLOW_GRAPH_MATCH_BASIC_BLOCK_EDGES_LENGAUER_TARJAN_H_
#define FLOW_GRAPH_MATCH_BASIC_BLOCK_EDGES_LENGAUER_TARJAN_H_

#include "third_party/zynamics/bindiff/flow_graph_match.h"

namespace security {
namespace bindiff {

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
                       MatchingStepsFlowGraph* matching_steps) override {
    EdgeIntMap primary_edges;
    EdgeIntMap secondary_edges;
    GetUnmatchedEdgesLoop(context, *primary, vertices1, &primary_edges);
    GetUnmatchedEdgesLoop(context, *secondary, vertices2, &secondary_edges);
    return FindFixedPointsBasicBlockEdgeInternal(
        &primary_edges, &secondary_edges, primary, secondary, fixed_point,
        context, matching_steps);
  }

 private:
  using EdgeFeatures = std::vector<int>;
  using EdgesByFlowGraph = std::map<const FlowGraph*, EdgeFeatures>;

  enum {
    kIsCircular = 1 << 0,
    kIsEdgeDominated = 1 << 1,
  };

  static void FeatureDestructor(EdgesByFlowGraph* features) { delete features; }

  void GetUnmatchedEdgesLoop(MatchingContext* context,
                             const FlowGraph& flow_graph,
                             const VertexSet& vertices, EdgeIntMap* edges) {
    const MatchingContext::FeatureId feature_id =
        MatchingContext::kEdgeProperies;
    EdgesByFlowGraph* cached = nullptr;
    if (context->HasCachedFeatures(feature_id)) {
      cached = context->GetCachedFeatures<EdgesByFlowGraph*>(feature_id);
      if (!cached) {
        cached = new EdgesByFlowGraph();
        context->SetCachedFeatures(feature_id, cached, FeatureDestructor);
      }
    }
    edges->clear();
    FlowGraph::EdgeIterator edge, end;
    EdgeFeatures* features = nullptr;
    if (cached) {
      auto result = cached->emplace(&flow_graph, EdgeFeatures());
      // We previously cached data for this flow graph, if it's already present
      // in the map.
      features = &result.first->second;
      if (!result.second) {
        // Element wasn't added, features list is filled in.
        int edge_index = 0;
        for (boost::tie(edge, end) = boost::edges(flow_graph.GetGraph());
             edge != end; ++edge, ++edge_index) {
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
    for (boost::tie(edge, end) = boost::edges(flow_graph.GetGraph());
         edge != end; ++edge) {
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
};

}  // namespace bindiff
}  // namespace security

#endif  // FLOW_GRAPH_MATCH_BASIC_BLOCK_EDGES_LENGAUER_TARJAN_H_
