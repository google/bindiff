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

#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/zynamics/bindiff/call_graph.h"
#include "third_party/zynamics/bindiff/call_graph_match.h"
#include "third_party/zynamics/bindiff/fixed_points.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/flow_graph_match.h"
#include "third_party/zynamics/bindiff/instruction.h"

namespace security::bindiff {

// A test environment that loads a BinDiff default configuration suitable for
// testing. This helps to avoid using the the built-in default configuration
// as it has name matching enabled, which can make tests pointless (both
// binaries contain full symbols and BinDiff will simply match everything
// based on that).
class BinDiffEnvironment : public ::testing::Environment {
 public:
  void SetUp() override;
};

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

  InstructionBuilder& SetCallsFunction(absl::string_view name) {
    calls_function_ = std::string(name);
    return *this;
  }

 private:
  friend class FunctionBuilder;

  Address address_ = 0;
  std::string mnemonic_;
  std::string disassembly_;
  std::string calls_function_;
  uint32_t prime_ = 0;
  uint8_t length_ = 1;  // Assume single-byte instructions by default
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

  void InitInstructions();

  Address entry_point_;
  std::string name_;
  std::vector<BasicBlockBuilder> basic_blocks_;
  std::vector<absl::string_view> out_calls_;
};

// Holder struct for one side of a diff.
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

  std::unique_ptr<DiffBinary> Build(Instruction::Cache* cache);

 private:
  std::vector<FunctionBuilder> functions_;
};

class BinDiffTest : public ::testing::Test {
 protected:
  // Sets up this test with BinDiff strutures corresponding to two simple
  // functions that are matched using "manual" matching.
  // This can be used in tests that just need simple BinDiff context ensure
  // basic functionality works.
  //
  // The matches that are added correspond to the functions below (in a
  // fictional x86-dialect where all instructions have length 1):
  //
  //   0x10000 func_a()              0x20000 func_b()
  //   0x10000 entry:                0x20000 entry:
  //   0x10000   test eax, eax       0x20000   sub eax, eax
  //   0x10001   jz loc_10004        0x20001   jz loc_20004
  //   0x10002 loc_10002:            0x20002 loc_20002:
  //   0x10002   mov eax, 1          0x20002   mov eax, 1
  //   0x10003   jmp loc_10005       0x20003   jmp loc_20005
  //   0x10004 loc_10004:            0x20004 loc_20004:
  //   0x10004   xor eax, eax        0x20004   xor eax, eax
  //   0x10005 loc_10005:            0x20005 loc_20005:
  //   0x10005   ret                 0x20005   ret
  //
  // The functions themselves as well as their basic blocks are set to manually
  // matched.
  void SetUpBasicFunctionMatch();

  // Like above, but omit the actual matches.
  void SetUpBasicFunctions();

  Instruction::Cache cache_;
  std::unique_ptr<DiffBinary> primary_;
  std::unique_ptr<DiffBinary> secondary_;
  FixedPoints fixed_points_;
};

}  // namespace security::bindiff

#endif  // TEST_UTIL_H_
