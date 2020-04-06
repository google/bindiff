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
#include "third_party/absl/memory/memory.h"
#include "third_party/absl/strings/str_split.h"
#include "third_party/zynamics/bindiff/call_graph.h"
#include "third_party/zynamics/bindiff/config.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/instruction.h"
#include "third_party/zynamics/bindiff/prime_signature.h"

namespace security::bindiff {

void ApplyDefaultConfigForTesting() {
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

std::unique_ptr<FlowGraph> FunctionBuilder::Build(TestCallGraph* call_graph,
                                                  Instruction::Cache* cache) {
  using Graph = FlowGraph::Graph;
  using VertexInfo = FlowGraph::VertexInfo;
  using EdgeInfo = FlowGraph::EdgeInfo;

  auto address = entry_point_;
  int instruction_offset = 0;
  std::vector<VertexInfo> vertices;

  auto flow_graph = absl::make_unique<TestFlowGraph>(call_graph, address);

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
      flow_graph->instructions_.emplace_back(
          cache, address++, instruction.mnemonic_, instruction.prime_);
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

DiffBinary DiffBinaryBuilder::Build(Instruction::Cache* cache) {
  using Graph = CallGraph::Graph;
  using EdgeInfo = CallGraph::EdgeInfo;

  DiffBinary diff_binary;

  auto& graph = diff_binary.call_graph.GetGraph();
  std::vector<std::pair<Graph::edges_size_type, Graph::edges_size_type>> edges;
  std::vector<EdgeInfo> edge_properties;
  Graph temp_graph(boost::edges_are_unsorted_multi_pass, edges.begin(),
                   edges.end(), edge_properties.begin(), functions_.size());
  std::swap(graph, temp_graph);

  auto func_it = functions_.begin();
  for (auto [it, end] = boost::vertices(graph); it != end; ++it, ++func_it) {
    auto& vertex = graph[*it];
    vertex.address_ = func_it->entry_point_;
    vertex.name_ = std::move(func_it->name_);

    diff_binary.flow_graphs.insert(
        func_it->Build(&diff_binary.call_graph, cache).release());
  }
  // TODO(cblichmann): Call graph edges

  diff_binary.call_graph.Init();
  return diff_binary;
}

}  // namespace security::bindiff
