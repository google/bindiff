#ifndef FLOW_GRAPH_MATCH_BASIC_BLOCK_EDGES_LENGAUER_TARJAN_H_
#define FLOW_GRAPH_MATCH_BASIC_BLOCK_EDGES_LENGAUER_TARJAN_H_

#include "third_party/zynamics/bindiff/flow_graph_match.h"

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
  using EdgesByFlowGraph = std::map<const FlowGraph*, EdgeFeatures>;

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

#endif  // FLOW_GRAPH_MATCH_BASIC_BLOCK_EDGES_LENGAUER_TARJAN_H_
