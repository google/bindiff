// Copyright 2011-2023 Google LLC
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

#ifndef MATCH_FUNCTION_FLOW_GRAPH_EDGES_MDINDEX_H_
#define MATCH_FUNCTION_FLOW_GRAPH_EDGES_MDINDEX_H_

#include "third_party/zynamics/bindiff/match/call_graph.h"

namespace security::bindiff {

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

}  // namespace security::bindiff

#endif  // MATCH_FUNCTION_FLOW_GRAPH_EDGES_MDINDEX_H_
