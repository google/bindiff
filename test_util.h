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

#ifndef TEST_UTIL_H_
#define TEST_UTIL_H_

#include <memory>
#include <string>

#include "third_party/zynamics/bindiff/call_graph.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/instruction.h"

#ifndef GOOGLE
#include <gmock/gmock.h>
#include <gtest/gtest.h>
#else
#include "gmock/gmock.h"
#include "gtest/gtest.h"
#endif

namespace security::bindiff {

// Loads a BinDiff default configuration suitable for testing. This helps to
// avoid using the the built-in default configuration as it has name matching
// enabled, which can make tests pointless (both binaries contain full symbols
// and BinDiff will simply match everything based on that).
// Intended to be calles from a test suite's SetUpTestSuite().
void ApplyDefaultConfigForTesting();

// Call graph class that exposes more parts of its internal API for testing.
class TestCallGraph : public CallGraph {
 public:
  using CallGraph::CallGraph;
  using CallGraph::Init;
};

// Similar to TestCallGraph, a flow graph class exposing protected members.
class TestFlowGraph : public FlowGraph {
 public:
  using FlowGraph::FlowGraph;
  using FlowGraph::Init;
  using FlowGraph::instructions_;
};

class InstructionBuilder {
 public:
  explicit InstructionBuilder(absl::string_view line);

 private:
  friend class FunctionBuilder;

  Address address_ = 0;
  std::string mnemonic_;
  std::string disassembly_;
  uint32_t prime_ = 0;
};

class BasicBlockBuilder {
 public:
  BasicBlockBuilder(absl::string_view label) : label_(label) {}

  BasicBlockBuilder& AddInstructions(
      std::initializer_list<InstructionBuilder> instructions) {
    instructions_.insert(instructions_.end(), instructions.begin(),
                         instructions.end());
    return *this;
  }

  BasicBlockBuilder& SetFlowTrue(absl::string_view label) {
    out_flow_labels_[FlowGraph::EDGE_TRUE] = std::string(label);
    return *this;
  }

  BasicBlockBuilder& SetFlowFalse(absl::string_view label) {
    out_flow_labels_[FlowGraph::EDGE_FALSE] = std::string(label);
    return *this;
  }

  BasicBlockBuilder& SetFlow(absl::string_view label) {
    out_flow_labels_[FlowGraph::EDGE_UNCONDITIONAL] = std::string(label);
    return *this;
  }

 private:
  friend class FunctionBuilder;

  std::string label_;
  std::map<int, std::string> out_flow_labels_;
  std::vector<InstructionBuilder> instructions_;
};

class FunctionBuilder {
 public:
  FunctionBuilder(Address entry_point, absl::string_view name)
      : entry_point_(entry_point), name_(name) {}

  FunctionBuilder(const FunctionBuilder&) = default;
  FunctionBuilder& operator=(const FunctionBuilder&) = default;

  FunctionBuilder& AddBasicBlocks(
      std::initializer_list<BasicBlockBuilder> basic_blocks) {
    basic_blocks_.insert(basic_blocks_.end(), basic_blocks.begin(),
                         basic_blocks.end());
    return *this;
  }

  std::unique_ptr<FlowGraph> Build(TestCallGraph* call_graph,
                                   Instruction::Cache* cache);

 private:
  friend class DiffBinaryBuilder;

  Address entry_point_;
  std::string name_;
  std::vector<BasicBlockBuilder> basic_blocks_;
};

// Holder struct for one size of a diff.
struct DiffBinary {
  ~DiffBinary();

  Instruction::Cache* cache;
  TestCallGraph call_graph;
  FlowGraphs flow_graphs;
};

class DiffBinaryBuilder {
 public:
  DiffBinaryBuilder& AddFunctions(
      std::initializer_list<FunctionBuilder> functions) {
    functions_.insert(functions_.end(), functions.begin(), functions.end());
    return *this;
  }

  DiffBinary Build(Instruction::Cache* cache);

 private:
  std::vector<FunctionBuilder> functions_;
};

}  // namespace security::bindiff

#endif  // TEST_UTIL_H_
