// Copyright 2011-2022 Google LLC
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

#include "third_party/zynamics/binexport/reader/flow_graph.h"

#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/binexport/binexport.h"
#include "third_party/zynamics/binexport/reader/graph_utility.h"
#include "third_party/zynamics/binexport/reader/instruction.h"
#include "third_party/zynamics/binexport/testing.h"

namespace security::binexport {
namespace {

using ::testing::Eq;

static constexpr char kBinExport2Item[] =
    "binexport/reader/testdata/"
    "0000500ed9f688a309ee2176462eb978efa9a2fb80fcceb5d8fd08168ea50dfd."
    "BinExport";

class FlowGraphTest : public testing::Test {
 protected:
  void SetUp() override {
    proto_ = GetBinExportForTesting(kBinExport2Item);
    flow_graph_ = FlowGraph::FromBinExport2Proto(
        proto_, proto_.flow_graph(0),
        GetAllInstructionAddresses(proto_));
  }

  std::unique_ptr<FlowGraph> flow_graph_;
  BinExport2 proto_;
};

// Tests if the setup produced a valid flow graph representation. The test is
// checking if the FromProto function produced a valid output for the protocol
// buffer specified in the setup method. If the parsing and conversion has been
// successful the test is successful.
TEST_F(FlowGraphTest, ReadValidData) {
  EXPECT_EQ(proto_.flow_graph(0).edge_size(), flow_graph_->GetEdgeCount());
  EXPECT_EQ(proto_.flow_graph(0).basic_block_index_size(),
            flow_graph_->GetVertexCount());
  int proto_instruction_count = 0;
  for (int basic_block_index : proto_.flow_graph(0).basic_block_index()) {
    const auto& basic_block_proto(proto_.basic_block(basic_block_index));
    for (const auto& instruction_interval :
         basic_block_proto.instruction_index()) {
      const int instruction_end_index(instruction_interval.has_end_index()
                                          ? instruction_interval.end_index()
                                          : instruction_interval.begin_index() +
                                                1);
      proto_instruction_count +=
          instruction_end_index - instruction_interval.begin_index();
    }
  }
  EXPECT_EQ(proto_instruction_count, flow_graph_->GetInstructionCount());

  FlowGraph::Graph graph = flow_graph_->graph();
  EXPECT_EQ(proto_.flow_graph(0).basic_block_index_size(),
            boost::num_vertices(graph));
  EXPECT_EQ(proto_.flow_graph(0).edge_size(), boost::num_edges(graph));
}

// Tests the GetInstruction method. If GetInstruction method provides access to
// the instructions from the specified protocol buffer, and the instructions
// match the instructions specified the test is successful.
TEST_F(FlowGraphTest, GetInstructions) {
  // Reproduce test data with the following queries:
  FlowGraph::Vertex vertex = flow_graph_->GetVertex(0x322152);
  int counter = 0;
  for (Instructions::const_iterator it =
           flow_graph_->GetInstructions(vertex).first;
       it != flow_graph_->GetInstructions(vertex).second; ++it) {
    switch (it->address()) {
      case 0x322152:
        EXPECT_EQ("pushad", it->mnemonic());
        break;
      case 0x322152 + 1:
        EXPECT_EQ("mov", it->mnemonic());
        break;
      case 0x322152 + 1 + 5:
        EXPECT_EQ("mov", it->mnemonic());
        break;
      case 0x322152 + 1 + 5 + 5:
        EXPECT_EQ("lea", it->mnemonic());
        break;
      case 0x322152 + 1 + 5 + 5 + 4:
        EXPECT_EQ("mov", it->mnemonic());
        break;
      default:
        // Should never happen.
        EXPECT_TRUE(false);
    }
    ++counter;
  }
  EXPECT_EQ(5, counter);
}

// Tests that the FromBinExport2Proto method correctly populates Instruction
// call targets. If the total number of call targets matches the expected
// number of call targets, the test is successful.
TEST_F(FlowGraphTest, GetCallTargets) {
  const auto& flow_graph(FlowGraph::FromBinExport2Proto(
      proto_, proto_.flow_graph(1),
      GetAllInstructionAddresses(proto_)));
  const auto& vertex = flow_graph->GetVertex(0x003221BE);
  std::vector<Address> call_targets;
  flow_graph->GetCallTargets(vertex, std::back_inserter(call_targets));
  EXPECT_THAT(call_targets.size(), Eq(9));
}

// Tests that the FromBinExport2Proto method correctly populates Instruction
// call targets for a non-final vertex in a flow graph with multiple vertices
// with call targets. If the total number of call targets matches the expected
// number of call targets, the test is successful.
TEST_F(FlowGraphTest, GetCallTargetsMultiple) {
  const auto& flow_graph(FlowGraph::FromBinExport2Proto(
      proto_, proto_.flow_graph(2),
      GetAllInstructionAddresses(proto_)));
  const auto& vertex = flow_graph->GetVertex(0x00322310);
  std::vector<Address> call_targets;
  flow_graph->GetCallTargets(vertex, std::back_inserter(call_targets));
  EXPECT_THAT(call_targets.size(), Eq(6));
}

// Tests if IsValidVertex returns false for a vertex not present in the graph.
TEST_F(FlowGraphTest, GetVertexInvalidAddress) {
  EXPECT_FALSE(IsValidVertex(flow_graph_->GetVertex(0x003221BE)));
}

// Tests if IsValidVertex returns true for a vertex present in the graph.
TEST_F(FlowGraphTest, GetVertexGoodAddress) {
  EXPECT_TRUE(IsValidVertex(flow_graph_->GetVertex(0x322152)));
}

}  // namespace
}  // namespace security::binexport
