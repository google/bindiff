// Copyright 2021 Google LLC
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

#include <initializer_list>
#include <memory>
#include <tuple>
#include <utility>

#include "third_party/absl/container/flat_hash_map.h"
#include "third_party/absl/container/flat_hash_set.h"
#include "third_party/absl/memory/memory.h"
#include "third_party/absl/strings/str_split.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/zynamics/bindiff/call_graph.h"
#include "third_party/zynamics/bindiff/call_graph_match.h"
#include "third_party/zynamics/bindiff/config.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/instruction.h"
#include "third_party/zynamics/bindiff/prime_signature.h"

namespace security::bindiff {

void BinDiffEnvironment::SetUp() {
  auto& config = config::Proto();
  config.Clear();
  for (const auto& [name, confidence] :
       std::initializer_list<std::pair<std::string, double>>{
           {"function: hash matching", 1.0},
           {"function: edges flowgraph MD index", 1.0},
           {"function: edges callgraph MD index", 0.9},
           {"function: MD index matching (flowgraph MD index, top down)", 0.9},
           {"function: MD index matching (flowgraph MD index, bottom up)", 0.9},
           {"function: prime signature matching", 0.9},
           {"function: MD index matching (callGraph MD index, top down)", 0.8},
           {"function: MD index matching (callGraph MD index, bottom up)", 0.8},
           // Disabled by default. Included here for completeness.
           // {"function: edges proximity MD index", 0.7},
           {"function: relaxed MD index matching", 0.7},
           {"function: instruction count", 0.4},
           {"function: address sequence", 0.4},
           {"function: string references", 0.7},
           {"function: loop count matching", 0.6},
           {"function: call sequence matching(exact)", 0.1},
           {"function: call sequence matching(topology)", 0.0},
           {"function: call sequence matching(sequence)", 0.0},
       }) {
    Config::MatchingStep* step = config.add_function_matching();
    step->set_name(name);
    step->set_confidence(confidence);
  }
  for (const auto& [name, confidence] :
       std::initializer_list<std::pair<std::string, double>>{
           {"basicBlock: edges prime product", 1.0},
           {"basicBlock: hash matching (4 instructions minimum)", 1.0},
           {"basicBlock: prime matching (4 instructions minimum)", 0.9},
           {"basicBlock: call reference matching", 0.8},
           {"basicBlock: string references matching", 0.8},
           {"basicBlock: edges MD index (top down)", 0.7},
           {"basicBlock: MD index matching (top down)", 0.7},
           {"basicBlock: edges MD index (bottom up)", 0.7},
           {"basicBlock: MD index matching (bottom up)", 0.7},
           {"basicBlock: relaxed MD index matching", 0.6},
           {"basicBlock: prime matching (0 instructions minimum)", 0.5},
           {"basicBlock: edges Lengauer Tarjan dominated", 0.4},
           {"basicBlock: loop entry matching", 0.4},
           {"basicBlock: self loop matching", 0.3},
           {"basicBlock: entry point matching", 0.2},
           {"basicBlock: exit point matching", 0.1},
           {"basicBlock: instruction count matching", 0.0},
           {"basicBlock: jump sequence matching", 0.0},
       }) {
    Config::MatchingStep* step = config.add_basic_block_matching();
    step->set_name(name);
    step->set_confidence(confidence);
  }
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
