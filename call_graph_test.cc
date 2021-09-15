// Copyright 2011-2021 Google LLC
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

#include "third_party/zynamics/bindiff/call_graph.h"

#include <limits>
#include <memory>
#include <stdexcept>

#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "third_party/absl/memory/memory.h"
#include <boost/graph/compressed_sparse_row_graph.hpp>
#include "third_party/zynamics/bindiff/test_util.h"

namespace security::bindiff {
namespace {

using ::testing::DoubleNear;
using ::testing::Eq;
using ::testing::IsEmpty;
using ::testing::IsTrue;
using ::testing::Ne;
using ::testing::StrEq;

TEST(EmptyCallGraphTest, Construction) {
  CallGraph call_graph;  // Empty

  // Verify metadata
  EXPECT_THAT(call_graph.GetFilename(), IsEmpty());
  EXPECT_THAT(call_graph.GetFilePath(), IsEmpty());
  EXPECT_THAT(call_graph.GetExeFilename(), IsEmpty());
  EXPECT_THAT(call_graph.GetExeHash(), IsEmpty());

  // No vertices, no edges
  EXPECT_THAT(boost::num_vertices(call_graph.GetGraph()), Eq(0));
  EXPECT_THAT(boost::num_edges(call_graph.GetGraph()), Eq(0));
  EXPECT_THAT(call_graph.GetVertex(0), Eq(CallGraph::kInvalidVertex));
  EXPECT_THAT(call_graph.GetVertex(std::numeric_limits<Address>::max()),
              Eq(CallGraph::kInvalidVertex));

  // Empty graph's MD index should be exactly zero
  call_graph.CalculateTopology();
  EXPECT_THAT(call_graph.GetMdIndex(), Eq(0.0));

  call_graph.SetMdIndex(47.0);
  EXPECT_THAT(call_graph.GetMdIndex(), Eq(47.0));

  EXPECT_THAT(call_graph.GetComments(), IsEmpty());
}

TEST(EmptyCallGraphTest, AddOrRemoveNullFlowGraphThrows) {
  CallGraph call_graph;  // Empty

  EXPECT_THROW(call_graph.AttachFlowGraph(nullptr), std::runtime_error);
  EXPECT_THROW(call_graph.DetachFlowGraph(nullptr), std::runtime_error);
}

TEST(EmptyCallGraphDeathTest, QueryingVerticesCrashes) {
  CallGraph call_graph;  // Empty

  // These should fail in all builds
  // TODO(cblichmann): Implement bound checks in debug mode.
  EXPECT_DEATH_IF_SUPPORTED(call_graph.GetAddress(CallGraph::kInvalidVertex),
                            "");
  EXPECT_DEATH_IF_SUPPORTED(call_graph.GetMdIndex(CallGraph::kInvalidVertex),
                            "");
  EXPECT_DEATH_IF_SUPPORTED(
      call_graph.GetMdIndexInverted(CallGraph::kInvalidVertex), "");
}

TEST(EmptyCallGraphTest, CrossPlatformFileBasenames) {
  class CallGraphForTesting : public CallGraph {
   public:
    void set_filename(std::string value) { filename_ = std::move(value); }
  } call_graph;  // Empty

  // Plain filename
  call_graph.set_filename("primary.v1.test.exe");
  EXPECT_THAT(call_graph.GetFilename(), StrEq("primary.v1.test"));

  // Windows style
  call_graph.set_filename(R"(C:\TEMP\RE.project\primary.v1.test.exe)");
  EXPECT_THAT(call_graph.GetFilename(), StrEq("primary.v1.test"));

  // Posix style
  call_graph.set_filename(R"(/tmp/RE.project/primary.v1.test.exe)");
  EXPECT_THAT(call_graph.GetFilename(), StrEq("primary.v1.test"));
}

class SimpleCallGraphTest : public ::testing::Test {
 protected:
  SimpleCallGraphTest()
      : binary_(DiffBinaryBuilder()
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
                                      .AddInstructions(
                                          {InstructionBuilder("ret")})}),
                         FunctionBuilder(0x20000, "func_b")
                             .AddBasicBlocks(
                                 {BasicBlockBuilder("entry").AddInstructions(
                                     {InstructionBuilder("call func_a")
                                          .SetCallsFunction("func_a"),
                                      InstructionBuilder("ret")})})})
                    .Build(&cache_)),
        call_graph_(binary_->call_graph) {}

  Instruction::Cache cache_;
  std::unique_ptr<DiffBinary> binary_;
  CallGraph& call_graph_;  // Convenient access to binary_.call_graph
};

TEST_F(SimpleCallGraphTest, Construction) {
  EXPECT_THAT(boost::num_vertices(call_graph_.GetGraph()), Eq(2));
  EXPECT_THAT(boost::num_edges(call_graph_.GetGraph()), Eq(1));
}

TEST_F(SimpleCallGraphTest, TopologyValid) {
  call_graph_.CalculateTopology();
  EXPECT_THAT(call_graph_.GetMdIndex(), DoubleNear(0.132036, 0.000001));

  const auto func_a = call_graph_.GetVertex(0x10000);
  ASSERT_THAT(func_a, Ne(CallGraph::kInvalidVertex));

  const auto func_b = call_graph_.GetVertex(0x20000);
  ASSERT_THAT(func_b, Ne(CallGraph::kInvalidVertex));

  EXPECT_THAT(call_graph_.FindEdge(func_b, func_a).second, IsTrue());
}

}  // namespace
}  // namespace security::bindiff
