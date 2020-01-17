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

#include "gtest/gtest.h"
#include "third_party/zynamics/bindiff/flow_graph.h"

namespace security::bindiff {
namespace {

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

}  // namespace
}  // namespace security::bindiff
