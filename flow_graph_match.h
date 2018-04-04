#ifndef FLOW_GRAPH_MATCH_H_
#define FLOW_GRAPH_MATCH_H_

#include <set>
#include <string>

#include "third_party/zynamics/bindiff/match_context.h"
#include "third_party/zynamics/binexport/types.h"

class MatchingStepFlowGraph;

using VertexSet = std::set<FlowGraph::Vertex>;

MatchingStepsFlowGraph GetDefaultMatchingStepsBasicBlock();
void FindFixedPointsBasicBlock(FixedPoint* fixed_point,
                               MatchingContext* context,
                               const MatchingStepsFlowGraph& default_steps);

class MatchingStepFlowGraph {
 public:
  explicit MatchingStepFlowGraph(const string& name);
  virtual ~MatchingStepFlowGraph();

  virtual bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                               const VertexSet& vertices1,
                               const VertexSet& vertices2,
                               FixedPoint* fixed_point,
                               MatchingContext* context,
                               MatchingStepsFlowGraph* matching_steps) = 0;

  double GetConfidence() const;
  const string& GetName() const;
  bool IsEdgeMatching() const;

 protected:
  string name_;
  double confidence_;
  bool edge_matching_;
};

#endif  // FLOW_GRAPH_MATCH_H_
