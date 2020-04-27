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

#include "third_party/zynamics/bindiff/change_classifier.h"

#include <boost/graph/compressed_sparse_row_graph.hpp>  // NOLINT(readability/boost)
#include <limits>
#include <vector>

#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "third_party/zynamics/bindiff/call_graph.h"
#include "third_party/zynamics/bindiff/call_graph_match.h"
#include "third_party/zynamics/bindiff/fixed_points.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/flow_graph_match.h"
#include "third_party/zynamics/bindiff/instruction.h"
#include "third_party/zynamics/bindiff/match_context.h"
#include "third_party/zynamics/bindiff/prime_signature.h"
#include "third_party/zynamics/bindiff/test_util.h"

namespace security::bindiff {
namespace {

using ::testing::StrEq;

class ChangeClassifierTest : public ::testing::Test {
 protected:
  static void SetUpTestSuite() { ApplyDefaultConfigForTesting(); }
};

TEST_F(ChangeClassifierTest, ChangeDescription) {
  EXPECT_THAT(GetChangeDescription(CHANGE_NONE), StrEq("-------"));
  EXPECT_THAT(GetChangeDescription(CHANGE_STRUCTURAL | CHANGE_OPERANDS |
                                   CHANGE_ENTRYPOINT | CHANGE_CALLS),
              StrEq("G-O-E-C"));
  EXPECT_THAT(
      GetChangeDescription(CHANGE_STRUCTURAL | CHANGE_INSTRUCTIONS |
                           CHANGE_OPERANDS | CHANGE_BRANCHINVERSION |
                           CHANGE_ENTRYPOINT | CHANGE_LOOPS | CHANGE_CALLS),
      StrEq("GIOJELC"));
}

TEST_F(ChangeClassifierTest, BasicChange) {
  Instruction::Cache cache;

  auto primary =
      DiffBinaryBuilder()
          .AddFunctions(
              {FunctionBuilder(0x10000, "func_a")
                   .AddBasicBlocks(
                       {BasicBlockBuilder("entry")
                            .AddInstructions(
                                {InstructionBuilder("test eax, eax"),
                                 InstructionBuilder("jz loc_10002")})
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
          .Build(&cache);
  auto secondary =
      DiffBinaryBuilder()
          .AddFunctions(
              {FunctionBuilder(0x20000, "func_b")
                   .AddBasicBlocks(
                       {BasicBlockBuilder("entry")
                            .AddInstructions(
                                {InstructionBuilder("sub eax, eax"),
                                 InstructionBuilder("jz loc_20002")})
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
          .Build(&cache);

  FixedPoints fixed_points;
  FixedPoint fixed_point(*primary.flow_graphs.begin(),
                         *secondary.flow_graphs.begin(),
                         MatchingStep::kFunctionManualName);
  fixed_point.Add(0, 0, MatchingStepFlowGraph::kBasicBlockManualName);
  fixed_point.Add(1, 1, MatchingStepFlowGraph::kBasicBlockManualName);
  fixed_point.Add(2, 2, MatchingStepFlowGraph::kBasicBlockManualName);
  fixed_point.Add(3, 3, MatchingStepFlowGraph::kBasicBlockManualName);
  fixed_points.insert(fixed_point);

  MatchingContext context(primary.call_graph, secondary.call_graph,
                          primary.flow_graphs, secondary.flow_graphs,
                          fixed_points);
  ClassifyChanges(&context);

  // Check that the fixed point has changed instructions
  EXPECT_THAT(GetChangeDescription(fixed_points.begin()->GetFlags()),
              StrEq("-I--E--"));
  }

}  // namespace
}  // namespace security::bindiff
