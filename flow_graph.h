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

#ifndef FLOWGRAPH_H_
#define FLOWGRAPH_H_

#include <cstdint>
#include <map>
#include <tuple>

#include "absl/container/btree_map.h"
#include "third_party/absl/container/node_hash_set.h"
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

  enum class NoReturnHeuristic { kNopsAfterCall, kNone };

  // Instruction address, operand number, expression id
  using Ref = std::tuple<Address, uint8_t, int>;
  using Substitutions = absl::btree_map<Ref, const std::string*>;  // Ordered
  using Edges = std::vector<FlowGraphEdge>;

  FlowGraph() = default;

  FlowGraph(const FlowGraph&) = delete;
  FlowGraph& operator=(const FlowGraph&) = delete;

  ~FlowGraph();

  void AddEdge(const FlowGraphEdge& edge);
  const Edges& GetEdges() const { return edges_; }
  const Function* GetFunction(Address address) const;
  Function* GetFunction(Address address);
  Functions& GetFunctions() { return functions_; }
  const Functions& GetFunctions() const { return functions_; }

  // Follows code flow from every function entry point and creates basic blocks
  // and functions. If any instruction in a basic block gets executed, all of
  // them will be. This means that basic blocks end iff:
  // - the instruction is a branch
  // - the instruction is a branch target
  // - the instruction is a call target (function entry point)
  // - the instruction follows a call and is invalid (assuming a non-returning
  //   call)
  // - the instruction is a resynchronization point, i.e. a sequence of
  //   overlapping instructions merges again at the current one.
  // Note: Keep the detego namespace, plain "Instructions" clashes with IDA.
  void ReconstructFunctions(
      detego::Instructions* instructions, CallGraph* call_graph,
      NoReturnHeuristic noreturn_heuristic = NoReturnHeuristic::kNopsAfterCall);

  void PruneFlowGraphEdges();
  void AddExpressionSubstitution(Address address, uint8_t operator_num,
                                 int expression_id,
                                 const std::string& substitution);
  const Substitutions& GetSubstitutions() const { return substitutions_; }

  // Sets FLAG_INVALID on all instructions that are no longer referenced by any
  // basic block. Note that we cannot easily delete instructions from the vector
  // as they are stored by value and others are pointing to it.
  // Note: Keep the detego namespace, plain "Instructions" clashes with IDA.
  void MarkOrphanInstructions(detego::Instructions* instructions) const;

  void Render(std::ostream* stream, const CallGraph& call_graph) const;

 private:
  // Returns a vector of instruction addresses that start new basic blocks. This
  // deals with cases where the end of a basic block is induced from outside the
  // basic block, i.e. branches or calls into the instruction sequence.
  std::vector<Address> FindBasicBlockBreaks(
      detego::Instructions* instructions, CallGraph* call_graph,
      NoReturnHeuristic noreturn_heuristic);

  // Follows flow from every function entry point and collects a global "soup"
  // of basic blocks. Not yet attached to any function.
  void CreateBasicBlocks(detego::Instructions* instructions,
                         CallGraph* call_graph,
                         NoReturnHeuristic noreturn_heuristic);

  // Merges basic blocks iff:
  // 1) source basic block has exactly 1 out-edge
  // 2) target basic block has exactly 1 in-edge
  // 3) source basic block != target basic block
  // 4) target basic block != function entry point (we want to leave that
  //    intact)
  void MergeBasicBlocks(const CallGraph& call_graph);
  void FinalizeFunctions(CallGraph* call_graph);

  Edges edges_;
  Functions functions_;
  Substitutions substitutions_;
  absl::node_hash_set<std::string> string_cache_;
};

#endif  // FLOWGRAPH_H_
