#include "gtest/gtest.h"
#include "third_party/zynamics/bindiff/flow_graph.h"

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

  // TODO(soerenme) Test the following:
  //  void SetMdIndex(double index);
  //  void SetMdIndexInverted(double index);
  //  void SetFixedPoint(FixedPoint* fixed_point)
  //  void SetCallGraph(CallGraph* graph);
}

}  // namespace
