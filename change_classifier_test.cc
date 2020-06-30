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

::testing::Environment* const g_bindiff_env =
    ::testing::AddGlobalTestEnvironment(new BinDiffEnvironment());

class ChangeClassifierTest : public BinDiffTest {
 private:
  void SetUp() override { SetUpBasicFunctionMatch(); }
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
  MatchingContext context(primary_->call_graph, secondary_->call_graph,
                          primary_->flow_graphs, secondary_->flow_graphs,
                          fixed_points_);
  ClassifyChanges(&context);

  // Check that the fixed point has changed instructions
  EXPECT_THAT(GetChangeDescription(fixed_points_.begin()->GetFlags()),
              StrEq("-I--E--"));
  }

}  // namespace
}  // namespace security::bindiff
