#ifndef CALL_GRAPH_MATCH_FUNCTION_FLOW_GRAPH_EDGES_MDINDEX_H_
#define CALL_GRAPH_MATCH_FUNCTION_FLOW_GRAPH_EDGES_MDINDEX_H_

#include "third_party/zynamics/bindiff/call_graph_match.h"

namespace security {
namespace bindiff {

// Matches callgraph edges based on their source and target function's MD
// indices. Thus calls between two structurally identical functions are matched.
class MatchingStepEdgesFlowGraphMdIndex : public BaseMatchingStepEdgesMdIndex {
 public:
  MatchingStepEdgesFlowGraphMdIndex()
      : BaseMatchingStepEdgesMdIndex(
            "function: edges flowgraph MD index",
            "Function: Edges Flow Graph MD Index",
            MatchingContext::kFlowGraphMdIndexPrimary,
            MatchingContext::kFlowGraphMdIndexSecondary) {}

 protected:
  EdgeFeature MakeEdgeFeature(CallGraph::Edge edge, const CallGraph& call_graph,
                              FlowGraph* source, FlowGraph* target) override {
    return {edge, source->GetMdIndex(), target->GetMdIndex()};
  }
};

}  // namespace bindiff
}  // namespace security

#endif  // CALL_GRAPH_MATCH_FUNCTION_FLOW_GRAPH_EDGES_MDINDEX_H_
