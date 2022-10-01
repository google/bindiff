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

// Tests the functionality of the CallGraph class.

#include "third_party/zynamics/binexport/reader/call_graph.h"

#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/binexport/reader/graph_utility.h"
#include "third_party/zynamics/binexport/testing.h"

namespace security::binexport {
namespace {

using ::testing::Eq;

static constexpr char kBinExport2Item[] =
    "binexport/reader/testdata/"
    "0000500ed9f688a309ee2176462eb978efa9a2fb80fcceb5d8fd08168ea50dfd."
    "BinExport";

class CallGraphTest : public testing::Test {
 protected:
  void SetUp() override {
    proto_ = GetBinExportForTesting(kBinExport2Item);
    call_graph_ = CallGraph::FromBinExport2Proto(proto_);
  }

  std::unique_ptr<CallGraph> call_graph_;
  BinExport2 proto_;
};

TEST_F(CallGraphTest, ReadValidData) {
  EXPECT_THAT(boost::num_vertices(call_graph_->graph()),
              Eq(proto_.call_graph().vertex_size()));
  EXPECT_THAT(boost::num_edges(call_graph_->graph()),
              Eq(proto_.call_graph().edge_size()));
}

TEST_F(CallGraphTest, ValidateVertex) {
  int counter = 0;
  for (const auto& vertex : proto_.call_graph().vertex()) {
    if (IsValidVertex(call_graph_->GetVertex(vertex.address()))) {
      ++counter;
    }
  }
  EXPECT_THAT(counter, Eq(proto_.call_graph().vertex_size()));
}

TEST_F(CallGraphTest, GetVertexGetAddress) {
  for (const auto& vertex : proto_.call_graph().vertex()) {
    const auto address = vertex.address();
    EXPECT_THAT(call_graph_->GetAddress(call_graph_->GetVertex(address)),
                Eq(address));
  }
}

}  // namespace
}  // namespace security::binexport
