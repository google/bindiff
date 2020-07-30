// Copyright 2020 Google LLC
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

#include "third_party/zynamics/bindiff/test_util.h"

#include <memory>
#include <tuple>
#include <utility>

#include "third_party/absl/container/flat_hash_map.h"
#include "third_party/absl/container/flat_hash_set.h"
#include "third_party/absl/memory/memory.h"
#include "third_party/absl/strings/str_split.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/zynamics/bindiff/call_graph.h"
#include "third_party/zynamics/bindiff/config.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/instruction.h"
#include "third_party/zynamics/bindiff/prime_signature.h"

namespace security::bindiff {

void BinDiffEnvironment::SetUp() {
  GetConfig()
      ->LoadFromString(R"raw(<?xml version="1.0"?>
<bindiff config-version="6">
  <function-matching>
    <!-- <step confidence="1.0" algorithm="function: name hash matching" /> -->
    <step confidence="1.0" algorithm="function: hash matching" />
    <step confidence="1.0" algorithm="function: edges flowgraph MD index" />
    <step confidence="0.9" algorithm="function: edges callgraph MD index" />
    <step confidence="0.9" algorithm="function: MD index matching (flowgraph MD index, top down)" />
    <step confidence="0.9" algorithm="function: MD index matching (flowgraph MD index, bottom up)" />
    <step confidence="0.9" algorithm="function: prime signature matching" />
    <step confidence="0.8" algorithm="function: MD index matching (callGraph MD index, top down)" />
    <step confidence="0.8" algorithm="function: MD index matching (callGraph MD index, bottom up)" />
    <!-- <step confidence="0.7" algorithm="function: edges proximity MD index" /> -->
    <step confidence="0.7" algorithm="function: relaxed MD index matching" />
    <step confidence="0.4" algorithm="function: instruction count" />
    <step confidence="0.4" algorithm="function: address sequence" />
    <step confidence="0.7" algorithm="function: string references" />
    <step confidence="0.6" algorithm="function: loop count matching" />
    <step confidence="0.1" algorithm="function: call sequence matching(exact)" />
    <step confidence="0.0" algorithm="function: call sequence matching(topology)" />
    <step confidence="0.0" algorithm="function: call sequence matching(sequence)" />
  </function-matching>
  <basic-block-matching>
    <step confidence="1.0" algorithm="basicBlock: edges prime product" />
    <step confidence="1.0" algorithm="basicBlock: hash matching (4 instructions minimum)" />
    <step confidence="0.9" algorithm="basicBlock: prime matching (4 instructions minimum)" />
    <step confidence="0.8" algorithm="basicBlock: call reference matching" />
    <step confidence="0.8" algorithm="basicBlock: string references matching" />
    <step confidence="0.7" algorithm="basicBlock: edges MD index (top down)" />
    <step confidence="0.7" algorithm="basicBlock: MD index matching (top down)" />
    <step confidence="0.7" algorithm="basicBlock: edges MD index (bottom up)" />
    <step confidence="0.7" algorithm="basicBlock: MD index matching (bottom up)" />
    <step confidence="0.6" algorithm="basicBlock: relaxed MD index matching" />
    <step confidence="0.5" algorithm="basicBlock: prime matching (0 instructions minimum)" />
    <step confidence="0.4" algorithm="basicBlock: edges Lengauer Tarjan dominated" />
    <step confidence="0.4" algorithm="basicBlock: loop entry matching" />
    <step confidence="0.3" algorithm="basicBlock: self loop matching" />
    <step confidence="0.2" algorithm="basicBlock: entry point matching" />
    <step confidence="0.1" algorithm="basicBlock: exit point matching" />
    <step confidence="0.0" algorithm="basicBlock: instruction count matching" />
    <step confidence="0.0" algorithm="basicBlock: jump sequence matching" />
  </basic-block-matching>
</bindiff>)raw")
      .IgnoreError();
}

DiffBinary::~DiffBinary() {
  for (const auto* flow_graph : flow_graphs) {
    delete flow_graph;
  }
}

InstructionBuilder::InstructionBuilder(absl::string_view line) {
  std::pair<absl::string_view, absl::string_view> parts =
      absl::StrSplit(line, absl::MaxSplits(' ', 1));
  mnemonic_ = std::string(parts.first);
  disassembly_ = std::string(parts.second);
  prime_ = GetPrime(mnemonic_);
}

void FunctionBuilder::InitInstructions() {
  if (!out_calls_.empty()) {
    return;
  }

  auto address = entry_point_;
  for (auto& basic_block : basic_blocks_) {
    for (auto& instruction : basic_block.instructions_) {
      instruction.address_ = address;
      if (!instruction.calls_function_.empty()) {
        out_calls_.emplace_back(instruction.calls_function_);
      }
      address += instruction.length_;
    }
  }
}

std::unique_ptr<FlowGraph> FunctionBuilder::Build(TestCallGraph* call_graph,
                                                  Instruction::Cache* cache) {
  using Graph = FlowGraph::Graph;
  using VertexInfo = FlowGraph::VertexInfo;
  using EdgeInfo = FlowGraph::EdgeInfo;

  int instruction_offset = 0;
  std::vector<VertexInfo> vertices;

  InitInstructions();
  auto flow_graph = absl::make_unique<TestFlowGraph>(call_graph, entry_point_);

  std::vector<std::pair<Graph::edges_size_type, Graph::edges_size_type>> edges;
  std::vector<EdgeInfo> properties;

  int label_id = 0;
  absl::flat_hash_map<absl::string_view, int> labels;
  for (const auto& basic_block : basic_blocks_) {
    VertexInfo* vertex = &vertices.emplace_back();
    vertex->instruction_start_ = instruction_offset;
    vertex->prime_ = 0;
    labels[basic_block.label_] = label_id++;
    for (auto& instruction : basic_block.instructions_) {
      ++instruction_offset;
      flow_graph->instructions_.emplace_back(cache, instruction.address_,
                                             instruction.mnemonic_,
                                             instruction.prime_);
      vertex->prime_ += instruction.prime_;
    }
  }

  auto end = basic_blocks_.end();
  for (auto it = basic_blocks_.begin(); it != end; ++it) {
    auto& basic_block = *it;
    if (basic_block.out_flow_labels_.empty()) {  // Unconditional flow into next
      if (auto next = it + 1; next != end) {
        basic_block.SetFlow(next->label_);
      }
    }
    for (const auto& [edge_type, out_flow_label] :
         basic_block.out_flow_labels_) {
      edges.emplace_back(labels[basic_block.label_], labels[out_flow_label]);
      properties.emplace_back().flags_ = edge_type;
    }
  }

  auto& graph = flow_graph->GetGraph();
  Graph temp_graph(boost::edges_are_unsorted_multi_pass, edges.begin(),
                   edges.end(), properties.begin(), vertices.size());
  std::swap(graph, temp_graph);
  int j = 0;
  for (auto [it, end] = boost::vertices(graph); it != end; ++it, ++j) {
    graph[*it] = vertices[j];
  }

  flow_graph->Init();
  return flow_graph;
}

std::unique_ptr<DiffBinary> DiffBinaryBuilder::Build(
    Instruction::Cache* cache) {
  using Graph = CallGraph::Graph;
  using EdgeInfo = CallGraph::EdgeInfo;

  auto diff_binary = absl::make_unique<DiffBinary>();

  // Note: Identifying functions by string is terribly inefficient, but this is
  //       for the benefit of test code that is easier to read. Graphs built by
  //       DiffBinaryBuilder should always be fairly small, so we can accept
  //       inefficiencies in this code.
  int label_id = 0;
  absl::flat_hash_map<absl::string_view, int>
      labels;  // Function name to vertex
  absl::flat_hash_map<int, absl::flat_hash_set<absl::string_view>>
      all_calls;  // Vertex to list of called function names
  for (auto& function : functions_) {
    // Calculate instruction addresses and collect calls to other functions.
    function.InitInstructions();
    labels[function.name_] = label_id;
    all_calls[label_id].insert(function.out_calls_.begin(),
                               function.out_calls_.end());
    ++label_id;
  }

  std::vector<std::pair<Graph::edges_size_type, Graph::edges_size_type>> edges;
  edges.reserve(all_calls.size());
  std::vector<EdgeInfo> edge_properties(all_calls.size());
  for (const auto& [source_id, out_calls] : all_calls) {
    for (const auto& named_call : out_calls) {
      edges.emplace_back(source_id, labels[named_call]);
    }
  }

  auto& graph = diff_binary->call_graph.GetGraph();
  Graph temp_graph(boost::edges_are_unsorted_multi_pass, edges.begin(),
                   edges.end(), edge_properties.begin(), functions_.size());
  std::swap(graph, temp_graph);

  auto func_it = functions_.begin();
  for (auto [it, end] = boost::vertices(graph); it != end; ++it, ++func_it) {
    auto& vertex = graph[*it];
    vertex.address_ = func_it->entry_point_;
    vertex.name_ = std::move(func_it->name_);
  }
  diff_binary->call_graph.Init();

  for (auto& function : functions_) {
    diff_binary->flow_graphs.insert(
        function.Build(&diff_binary->call_graph, cache).release());
  }
  return diff_binary;
}

void BinDiffTest::SetUpBasicFunctions() {
  primary_ =
      DiffBinaryBuilder()
          .AddFunctions(
              {FunctionBuilder(0x10000, "func_a")
                   .AddBasicBlocks(
                       {BasicBlockBuilder("entry")
                            .AddInstructions(
                                {InstructionBuilder("test eax, eax"),
                                 InstructionBuilder("jz loc_10004")})
                            .SetFlowTrue("loc_10002")
                            .SetFlowFalse("loc_10004"),
                        BasicBlockBuilder("loc_10002")
                            .AddInstructions(
                                {InstructionBuilder("mov eax, 1"),
                                 InstructionBuilder("jmp loc_10005")})
                            .SetFlow("loc_10005"),
                        BasicBlockBuilder("loc_10004")
                            .AddInstructions(
                                {InstructionBuilder("xor eax, eax")})
                            .SetFlow("loc_10005"),
                        BasicBlockBuilder("loc_10005")
                            .AddInstructions({InstructionBuilder("ret")})})})
          .Build(&cache_);
  secondary_ =
      DiffBinaryBuilder()
          .AddFunctions(
              {FunctionBuilder(0x20000, "func_b")
                   .AddBasicBlocks(
                       {BasicBlockBuilder("entry")
                            .AddInstructions(
                                {InstructionBuilder("sub eax, eax"),
                                 InstructionBuilder("jz loc_20004")})
                            .SetFlowTrue("loc_20002")
                            .SetFlowFalse("loc_20004"),
                        BasicBlockBuilder("loc_20002")
                            .AddInstructions(
                                {InstructionBuilder("mov eax, 1"),
                                 InstructionBuilder("jmp loc_20005")})
                            .SetFlow("loc_20005"),
                        BasicBlockBuilder("loc_20004")
                            .AddInstructions(
                                {InstructionBuilder("xor eax, eax")})
                            .SetFlow("loc_20005"),
                        BasicBlockBuilder("loc_20005")
                            .AddInstructions({InstructionBuilder("ret")})})})
          .Build(&cache_);
}

void BinDiffTest::SetUpBasicFunctionMatch() {
  SetUpBasicFunctions();

  fixed_points_.clear();
  FixedPoint fixed_point(*primary_->flow_graphs.begin(),
                         *secondary_->flow_graphs.begin(),
                         MatchingStep::kFunctionManualName);
  fixed_point.Add(0, 0, MatchingStepFlowGraph::kBasicBlockManualName);
  fixed_point.Add(1, 1, MatchingStepFlowGraph::kBasicBlockManualName);
  fixed_point.Add(2, 2, MatchingStepFlowGraph::kBasicBlockManualName);
  fixed_point.Add(3, 3, MatchingStepFlowGraph::kBasicBlockManualName);
  fixed_points_.insert(std::move(fixed_point));
}

}  // namespace security::bindiff
