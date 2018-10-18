// Copyright 2011-2018 Google LLC. All Rights Reserved.
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

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_FLOWGRAPH_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_FLOWGRAPH_H_

#include <cstdint>
#include <map>
#include <tuple>
#include <unordered_set>

#include "third_party/zynamics/binexport/edge.h"
#include "third_party/zynamics/binexport/function.h"
#include "third_party/zynamics/binexport/instruction.h"

class CallGraph;

class FlowGraph {
 public:
  // Maximum number of basic blocks/edges/instructions we want to allow for a
  // single function. If a function has more than this, we simply discard it as
  // invalid. kMaxFunctionEarlyBasicBlocks limit is evaluated before bb merging
  // thus its value is set to be relative to kMaxFunctionBasicBlocks
  enum {
    kMaxFunctionBasicBlocks = 5000,
    kMaxFunctionEarlyBasicBlocks = kMaxFunctionBasicBlocks + 1000,
    kMaxFunctionEdges = 5000,
    kMaxFunctionInstructions = 20000
  };

  // Instruction address, operand number, expression id
  using Ref = std::tuple<Address, uint8, int>;
  using Substitutions = std::map<Ref, const string*>;
  using Edges = std::vector<FlowGraphEdge>;

  FlowGraph() = default;
  ~FlowGraph();

  void AddEdge(const FlowGraphEdge& edge);
  const Edges& GetEdges() const;
  const Function* GetFunction(Address address) const;
  Function* GetFunction(Address address);
  Functions& GetFunctions();
  const Functions& GetFunctions() const;
  // Note: Keep the detego namespace, plain "Instructions" clashes with IDA.
  void ReconstructFunctions(detego::Instructions* instructions,
                            CallGraph* call_graph);
  void PruneFlowGraphEdges();
  void AddExpressionSubstitution(Address address, uint8 operator_num,
                                 int expression_id,
                                 const string& substitution);
  const Substitutions& GetSubstitutions() const;
  // Note: Keep the detego namespace, plain "Instructions" clashes with IDA.
  void MarkOrphanInstructions(detego::Instructions* instructions) const;
  void Render(std::ostream* stream, const CallGraph& call_graph) const;

 private:
  using StringCache = std::unordered_set<string>;

  FlowGraph(const FlowGraph& graph) = delete;
  const FlowGraph& operator=(const FlowGraph& graph) = delete;

  std::vector<Address> FindBasicBlockBreaks(detego::Instructions* instructions,
                                            CallGraph* call_graph);
  void CreateBasicBlocks(detego::Instructions* instructions,
                         CallGraph* call_graph);
  void MergeBasicBlocks(const CallGraph& call_graph);
  void FinalizeFunctions(CallGraph* call_graph);

  Edges edges_;
  Functions functions_;
  Substitutions substitutions_;
  StringCache string_cache_;
};

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_FLOWGRAPH_H_
