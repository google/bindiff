// Copyright 2011-2020 Google LLC
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

#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_instruction_count.h"

#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "third_party/absl/memory/memory.h"
#include "third_party/zynamics/bindiff/fixed_points.h"
#include "third_party/zynamics/bindiff/flow_graph_match.h"
#include "third_party/zynamics/bindiff/match_context.h"
#include "third_party/zynamics/bindiff/test_util.h"

namespace security::bindiff {
namespace {

using ::testing::SizeIs;

::testing::Environment* const g_bindiff_env =
    ::testing::AddGlobalTestEnvironment(new BinDiffEnvironment());

class FlowGraphMatchBasicBlockInstructionCountTest : public BinDiffTest {
 private:
  void SetUp() override {
    SetUpBasicFunctions();

    FixedPoint fixed_point(*primary_->flow_graphs.begin(),
                           *secondary_->flow_graphs.begin(),
                           MatchingStep::kFunctionManualName);
    fixed_point.Add(0, 0, MatchingStepFlowGraph::kBasicBlockManualName);
    fixed_points_.insert(std::move(fixed_point));
  }
};

TEST_F(FlowGraphMatchBasicBlockInstructionCountTest, DefaultFlowGraphMatches) {
  auto step = absl::make_unique<MatchingStepInstructionCount>();
  MatchingStepsFlowGraph steps = {step.get()};

  MatchingContext context(primary_->call_graph, secondary_->call_graph,
                          primary_->flow_graphs, secondary_->flow_graphs,
                          fixed_points_);

  FixedPoint fixed_point(*fixed_points_.begin());
  FindFixedPointsBasicBlock(&fixed_point, &context, steps);
  EXPECT_THAT(fixed_point.GetBasicBlockFixedPoints(), SizeIs(4));
}

}  // namespace
}  // namespace security::bindiff
