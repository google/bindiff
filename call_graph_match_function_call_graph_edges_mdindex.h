#ifndef CALL_GRAPH_MATCH_FUNCTION_CALL_GRAPH_EDGES_MDINDEX_H_
#define CALL_GRAPH_MATCH_FUNCTION_CALL_GRAPH_EDGES_MDINDEX_H_

#include "third_party/zynamics/bindiff/call_graph_match.h"

namespace security {
namespace bindiff {

// Matches callgraph edges based on their callgraph MD index. This means the
// callgraph leading to that particular call is structurally identical in both
// binaries. Match quality depends on how deep the callstack leading up to this
// edge is: the deeper the less likely is a false match.
class MatchingStepEdgesCallGraphMdIndex : public BaseMatchingStepEdgesMdIndex {
 public:
  MatchingStepEdgesCallGraphMdIndex()
      : BaseMatchingStepEdgesMdIndex(
            "function: edges callgraph MD index",
            MatchingContext::kCallGraphMdIndexPrimary,
            MatchingContext::kCallGraphMdIndexSecondary) {}

 protected:
  EdgeFeature MakeEdgeFeature(CallGraph::Edge edge, const CallGraph& call_graph,
                              FlowGraph* source, FlowGraph* target) override {
    return {edge, call_graph.GetMdIndex(edge), 0.0};
  }
};

}  // namespace bindiff
}  // namespace security

#endif  // CALL_GRAPH_MATCH_FUNCTION_CALL_GRAPH_EDGES_MDINDEX_H_
