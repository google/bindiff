// Copyright 2011-2026 Google LLC
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

#include "third_party/zynamics/bindiff/flow_graph.h"

#include <cstdint>
#include <limits>
#include <memory>

#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "third_party/absl/status/status.h"
#include "third_party/absl/status/status_matchers.h"
#include "third_party/absl/status/statusor.h"
#include "third_party/zynamics/bindiff/call_graph.h"
#include "third_party/zynamics/bindiff/instruction.h"
#include "third_party/zynamics/binexport/binexport2.pb.h"

namespace security::bindiff {
namespace {

using ::absl_testing::IsOk;
using ::absl_testing::StatusIs;
using ::testing::Eq;
using ::testing::IsEmpty;
using ::testing::IsFalse;
using ::testing::Not;

BinExport2 MakeValidProto() {
  BinExport2 proto;
  proto.mutable_meta_information()->set_executable_name("test");
  proto.mutable_call_graph()->add_vertex()->set_address(0x1000);

  proto.add_mnemonic()->set_name("nop");
  proto.add_mnemonic()->set_name("ret");

  proto.add_string_table("entry comment");
  auto* comment = proto.add_comment();
  comment->set_instruction_index(0);
  comment->set_string_table_index(0);

  auto* insn0 = proto.add_instruction();
  insn0->set_address(0x1000);
  insn0->set_mnemonic_index(0);
  insn0->set_raw_bytes("\x90");
  insn0->add_comment_index(0);

  auto* insn1 = proto.add_instruction();
  insn1->set_address(0x1001);
  insn1->set_mnemonic_index(1);
  insn1->set_raw_bytes("\xc3");

  auto* bb0 = proto.add_basic_block();
  auto* ir0 = bb0->add_instruction_index();
  ir0->set_begin_index(0);
  ir0->set_end_index(1);

  auto* bb1 = proto.add_basic_block();
  auto* ir1 = bb1->add_instruction_index();
  ir1->set_begin_index(1);
  ir1->set_end_index(2);

  auto* fg = proto.add_flow_graph();
  fg->set_entry_basic_block_index(0);
  fg->add_basic_block_index(0);
  fg->add_basic_block_index(1);
  auto* edge = fg->add_edge();
  edge->set_source_basic_block_index(0);
  edge->set_target_basic_block_index(1);

  return proto;
}

TEST(FlowGraphTest, FlowGraphDefaultValues) {
  FlowGraph flow_graph;
  EXPECT_EQ(0.0, flow_graph.GetMdIndex());
  EXPECT_EQ(0.0, flow_graph.GetMdIndexInverted());
  EXPECT_EQ(0, flow_graph.GetBasicBlockCount());
  EXPECT_EQ(0, flow_graph.GetLoopCount());
  EXPECT_EQ(0, flow_graph.GetEntryPointAddress());
  EXPECT_EQ(nullptr, flow_graph.GetFixedPoint());
  EXPECT_EQ(nullptr, flow_graph.GetCallGraph());

  flow_graph.CalculateCallLevels();
  flow_graph.CalculateTopology();

  // TODO(cblichmann): Test the following:
  //  void SetMdIndex(double index);
  //  void SetMdIndexInverted(double index);
  //  void SetFixedPoint(FixedPoint* fixed_point)
  //  void SetCallGraph(CallGraph* graph);
}

class FlowGraphFromProtoTest : public ::testing::Test {
 protected:
  absl::StatusOr<std::unique_ptr<FlowGraph>> FromProto() {
    call_graph_.Reset();
    if (absl::Status status = call_graph_.Read(proto_, "test.BinExport");
        !status.ok()) {
      return status;
    }
    absl::StatusOr<std::unique_ptr<FlowGraph>> flow_graph =
        FlowGraph::FromProto(proto_, proto_.flow_graph(0), call_graph_, cache_);
    if (!flow_graph.ok()) {
      for (const auto& vertex : proto_.call_graph().vertex()) {
        EXPECT_THAT(call_graph_.GetFlowGraph(uint64_t{vertex.address()}),
                    Eq(nullptr));
      }
    }
    return flow_graph;
  }

  BinExport2 proto_ = MakeValidProto();
  CallGraph call_graph_;
  Instruction::Cache cache_;
};

TEST_F(FlowGraphFromProtoTest, ValidProto) {
  absl::StatusOr<std::unique_ptr<FlowGraph>> flow_graph = FromProto();
  ASSERT_THAT(flow_graph, IsOk());
  EXPECT_THAT((*flow_graph)->GetEntryPointAddress(), Eq(0x1000));
  EXPECT_THAT((*flow_graph)->GetBasicBlockCount(), Eq(2));
  EXPECT_THAT((*flow_graph)->GetInstructionCount(), Eq(2));
  EXPECT_THAT((*flow_graph)->IsTrivial(), IsFalse());
  EXPECT_THAT(call_graph_.comments(), Not(IsEmpty()));
}

TEST_F(FlowGraphFromProtoTest, EntryBasicBlockIndexOutOfRange) {
  proto_.mutable_flow_graph(0)->set_entry_basic_block_index(-1);
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kInvalidArgument));

  proto_.mutable_flow_graph(0)->set_entry_basic_block_index(2);
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kInvalidArgument));
}

TEST_F(FlowGraphFromProtoTest, EntryBasicBlockEmptyInstructions) {
  proto_.mutable_basic_block(0)->clear_instruction_index();
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kFailedPrecondition));
}

TEST_F(FlowGraphFromProtoTest, EntryInstructionBeginIndexOutOfRange) {
  auto* ir = proto_.mutable_basic_block(0)->mutable_instruction_index(0);
  ir->set_begin_index(-1);
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kInvalidArgument));

  ir->set_begin_index(2);
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kInvalidArgument));
}

TEST_F(FlowGraphFromProtoTest, BasicBlockIndexOutOfRange) {
  proto_.mutable_flow_graph(0)->set_basic_block_index(1, -1);
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kFailedPrecondition));

  proto_.mutable_flow_graph(0)->set_basic_block_index(1, 2);
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kFailedPrecondition));
}

TEST_F(FlowGraphFromProtoTest, BasicBlockEmptyInstructionIndices) {
  proto_.mutable_basic_block(1)->clear_instruction_index();
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kFailedPrecondition));
}

TEST_F(FlowGraphFromProtoTest, InstructionBeginIndexOutOfRange) {
  auto* ir = proto_.mutable_basic_block(1)->mutable_instruction_index(0);
  ir->set_begin_index(-1);
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kFailedPrecondition));

  ir->set_begin_index(2);
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kFailedPrecondition));

  ir->clear_end_index();
  ir->set_begin_index(std::numeric_limits<int32_t>::max());
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kFailedPrecondition));
}

TEST_F(FlowGraphFromProtoTest, InstructionEndIndexInvalid) {
  auto* ir = proto_.mutable_basic_block(1)->mutable_instruction_index(0);
  ir->set_end_index(3);
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kFailedPrecondition));

  ir->set_begin_index(1);
  ir->set_end_index(1);
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kFailedPrecondition));

  ir->set_end_index(0);
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kFailedPrecondition));
}

TEST_F(FlowGraphFromProtoTest, MnemonicIndexOutOfRange) {
  proto_.mutable_instruction(0)->set_mnemonic_index(-1);
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kFailedPrecondition));

  proto_.mutable_instruction(0)->set_mnemonic_index(3);
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kFailedPrecondition));
}

TEST_F(FlowGraphFromProtoTest, CommentIndexOutOfRange) {
  proto_.mutable_instruction(0)->set_comment_index(0, -1);
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kFailedPrecondition));

  proto_.mutable_instruction(0)->set_comment_index(0, 1);
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kFailedPrecondition));
}

TEST_F(FlowGraphFromProtoTest, CommentStringTableIndexOutOfRange) {
  proto_.mutable_comment(0)->set_string_table_index(-1);
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kFailedPrecondition));

  proto_.mutable_comment(0)->set_string_table_index(1);
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kFailedPrecondition));
}

TEST_F(FlowGraphFromProtoTest, EdgeSourceBasicBlockIndexOutOfRange) {
  auto* edge = proto_.mutable_flow_graph(0)->mutable_edge(0);
  edge->set_source_basic_block_index(-1);
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kInvalidArgument));

  edge->set_source_basic_block_index(2);
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kInvalidArgument));
}

TEST_F(FlowGraphFromProtoTest, EdgeTargetBasicBlockIndexOutOfRange) {
  auto* edge = proto_.mutable_flow_graph(0)->mutable_edge(0);
  edge->set_target_basic_block_index(-1);
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kInvalidArgument));

  edge->set_target_basic_block_index(2);
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kInvalidArgument));
}

TEST_F(FlowGraphFromProtoTest, EdgeBasicBlockEmptyInstructions) {
  proto_.add_basic_block();
  proto_.mutable_flow_graph(0)->mutable_edge(0)->set_target_basic_block_index(
      2);
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kFailedPrecondition));
}

TEST_F(FlowGraphFromProtoTest, EdgeBasicBlockInstructionIndexOutOfRange) {
  auto* bb = proto_.add_basic_block();
  auto* ir = bb->add_instruction_index();
  proto_.mutable_flow_graph(0)->mutable_edge(0)->set_target_basic_block_index(
      2);

  ir->set_begin_index(-1);
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kInvalidArgument));

  ir->set_begin_index(2);
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kInvalidArgument));
}

TEST_F(FlowGraphFromProtoTest, EdgesWithNoBasicBlocks) {
  proto_.mutable_flow_graph(0)->clear_basic_block_index();
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kFailedPrecondition));
}

TEST_F(FlowGraphFromProtoTest, ValidProtoWithComputedAddresses) {
  proto_.mutable_instruction(1)->clear_address();
  auto* insn2 = proto_.add_instruction();
  insn2->set_mnemonic_index(1);
  insn2->set_raw_bytes("\xc3");

  proto_.mutable_basic_block(0)
      ->mutable_instruction_index(0)
      ->set_end_index(2);
  auto* ir1 = proto_.mutable_basic_block(1)->mutable_instruction_index(0);
  ir1->set_begin_index(2);
  ir1->set_end_index(3);

  absl::StatusOr<std::unique_ptr<FlowGraph>> flow_graph = FromProto();
  ASSERT_THAT(flow_graph, IsOk());
  EXPECT_THAT((*flow_graph)->GetEntryPointAddress(), Eq(0x1000));
  EXPECT_THAT((*flow_graph)->GetBasicBlockCount(), Eq(2));
  EXPECT_THAT((*flow_graph)->GetInstructionCount(), Eq(3));
  EXPECT_THAT((*flow_graph)->GetAddress(0), Eq(0x1000));
  EXPECT_THAT((*flow_graph)->GetAddress(1), Eq(0x1002));
}

TEST_F(FlowGraphFromProtoTest, EntryInstructionMissingAddress) {
  proto_.mutable_call_graph()->mutable_vertex(0)->set_address(0);
  proto_.mutable_instruction(0)->clear_address();
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kFailedPrecondition));
}

TEST_F(FlowGraphFromProtoTest, BasicBlockInstructionMissingAddress) {
  proto_.clear_instruction();
  auto* insn0 = proto_.add_instruction();
  insn0->set_mnemonic_index(0);
  insn0->set_raw_bytes("\x90");
  auto* insn1 = proto_.add_instruction();
  insn1->set_mnemonic_index(0);
  insn1->set_raw_bytes("\x90");
  auto* insn2 = proto_.add_instruction();
  insn2->set_address(0x1000);
  insn2->set_mnemonic_index(1);
  insn2->set_raw_bytes("\xc3");

  proto_.clear_basic_block();
  auto* bb0 = proto_.add_basic_block();
  auto* ir0 = bb0->add_instruction_index();
  ir0->set_begin_index(0);
  ir0->set_end_index(1);
  auto* bb1 = proto_.add_basic_block();
  auto* ir1 = bb1->add_instruction_index();
  ir1->set_begin_index(1);
  ir1->set_end_index(2);
  auto* bb2 = proto_.add_basic_block();
  auto* ir2 = bb2->add_instruction_index();
  ir2->set_begin_index(2);
  ir2->set_end_index(3);

  auto* fg = proto_.mutable_flow_graph(0);
  fg->set_entry_basic_block_index(2);
  fg->clear_edge();
  fg->clear_basic_block_index();
  fg->add_basic_block_index(0);
  fg->add_basic_block_index(2);
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kFailedPrecondition));

  fg->set_basic_block_index(0, 1);
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kFailedPrecondition));
}

TEST_F(FlowGraphFromProtoTest, EdgeBasicBlockInstructionMissingAddress) {
  proto_.clear_instruction();
  auto* insn0 = proto_.add_instruction();
  insn0->set_mnemonic_index(0);
  insn0->set_raw_bytes("\x90");
  auto* insn1 = proto_.add_instruction();
  insn1->set_address(0x1000);
  insn1->set_mnemonic_index(0);
  insn1->set_raw_bytes("\x90");
  auto* insn2 = proto_.add_instruction();
  insn2->set_address(0x1001);
  insn2->set_mnemonic_index(1);
  insn2->set_raw_bytes("\xc3");

  proto_.clear_basic_block();
  auto* bb0 = proto_.add_basic_block();
  auto* ir0 = bb0->add_instruction_index();
  ir0->set_begin_index(1);
  ir0->set_end_index(2);
  auto* bb1 = proto_.add_basic_block();
  auto* ir1 = bb1->add_instruction_index();
  ir1->set_begin_index(2);
  ir1->set_end_index(3);
  auto* bb2 = proto_.add_basic_block();
  auto* ir2 = bb2->add_instruction_index();
  ir2->set_begin_index(0);
  ir2->set_end_index(1);

  auto* edge = proto_.mutable_flow_graph(0)->mutable_edge(0);
  edge->set_source_basic_block_index(2);
  edge->set_target_basic_block_index(1);
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kFailedPrecondition));

  edge->set_source_basic_block_index(0);
  edge->set_target_basic_block_index(2);
  EXPECT_THAT(FromProto(), StatusIs(absl::StatusCode::kFailedPrecondition));
}

}  // namespace
}  // namespace security::bindiff
