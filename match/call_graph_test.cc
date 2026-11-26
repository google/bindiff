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

#include "third_party/zynamics/bindiff/match/call_graph.h"

#include <cstdint>
#include <initializer_list>
#include <memory>
#include <utility>
#include <vector>

#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/bindiff/call_graph.h"
#include "third_party/zynamics/bindiff/fixed_points.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/instruction.h"
#include "third_party/zynamics/bindiff/test_util.h"
#include "third_party/zynamics/binexport/util/types.h"

namespace security::bindiff {

// Helper class to access private members of BaseMatchingStepEdgesMdIndex.
class BaseMatchingStepEdgesMdIndexPeer {
 public:
  static void BuildIncidence(const CallGraph& call_graph,
                             EdgeFeatureIndex* index) {
    BaseMatchingStepEdgesMdIndex::BuildIncidence(call_graph, index);
  }

  static void FilterResults(const EdgeFeatureIndex& index,
                            const CallGraph& call_graph,
                            const FlowGraphs& flow_graphs,
                            EdgeFeatures* edges) {
    BaseMatchingStepEdgesMdIndex::FilterResults(index, call_graph, flow_graphs,
                                                edges);
  }
};

namespace {

using ::testing::Contains;
using ::testing::ElementsAreArray;
using ::testing::Not;
using ::testing::Pair;

::testing::Environment* const g_bindiff_env =
    ::testing::AddGlobalTestEnvironment(new BinDiffEnvironment());

using Peer = BaseMatchingStepEdgesMdIndexPeer;
using AddressPair = std::pair<Address, Address>;

FunctionBuilder MakeFunction(Address entry_point, const char* name,
                             std::initializer_list<const char*> callees) {
  BasicBlockBuilder entry("entry");
  for (const char* callee : callees) {
    entry.AddInstructions(
        {InstructionBuilder(absl::StrCat("call ", callee))
             .SetCallsFunction(callee)});
  }
  entry.AddInstructions({InstructionBuilder("ret")});
  return FunctionBuilder(entry_point, name).AddBasicBlocks({entry});
}

class EdgeFeatureIndexTest : public ::testing::Test {
 protected:
  // Call graph (e calls itself, which exercises the self-loop handling):
  //   a -> b, a -> c, b -> c, c -> d, e -> e, f -> a
  EdgeFeatureIndexTest()
      : binary_(DiffBinaryBuilder()
                    .AddFunctions({
                        MakeFunction(0x10000, "func_a", {"func_b", "func_c"}),
                        MakeFunction(0x20000, "func_b", {"func_c"}),
                        MakeFunction(0x30000, "func_c", {"func_d"}),
                        MakeFunction(0x40000, "func_d", {}),
                        MakeFunction(0x50000, "func_e", {"func_e"}),
                        MakeFunction(0x60000, "func_f", {"func_a"}),
                    })
                    .Build(cache_)),
        call_graph_(binary_->call_graph) {
    for (auto [edge, end] = boost::edges(call_graph_.GetGraph()); edge != end;
         ++edge) {
      index_.features.push_back({*edge, 0.0, 0.0});
    }
    Peer::BuildIncidence(call_graph_, &index_);
  }

  FlowGraph* GetFlowGraph(Address address) {
    return call_graph_.GetFlowGraph(call_graph_.GetVertex(address));
  }

  AddressPair ToAddresses(const EdgeFeature& feature) const {
    const auto& graph = call_graph_.GetGraph();
    return {call_graph_.GetAddress(boost::source(feature.edge, graph)),
            call_graph_.GetAddress(boost::target(feature.edge, graph))};
  }

  std::vector<AddressPair> ToAddresses(const EdgeFeatures& features) const {
    std::vector<AddressPair> result;
    for (const auto& feature : features) {
      result.push_back(ToAddresses(feature));
    }
    return result;
  }

  // Reference implementation: plain scan over all features, in order.
  std::vector<AddressPair> ExpectedEdges(const FlowGraphs& flow_graphs) const {
    std::vector<AddressPair> result;
    const auto& graph = call_graph_.GetGraph();
    for (const auto& feature : index_.features) {
      FlowGraph* source =
          call_graph_.GetFlowGraph(boost::source(feature.edge, graph));
      FlowGraph* target =
          call_graph_.GetFlowGraph(boost::target(feature.edge, graph));
      if (source->GetFixedPoint() && target->GetFixedPoint()) {
        continue;
      }
      if (flow_graphs.count(source) || flow_graphs.count(target)) {
        result.push_back(ToAddresses(feature));
      }
    }
    return result;
  }

  Instruction::Cache cache_;
  std::unique_ptr<DiffBinary> binary_;
  CallGraph& call_graph_;
  EdgeFeatureIndex index_;
};

TEST_F(EdgeFeatureIndexTest, IncidenceListsCountSelfLoopsOnce) {
  ASSERT_EQ(index_.features.size(), 6);
  ASSERT_EQ(index_.offsets.size(),
            boost::num_vertices(call_graph_.GetGraph()) + 1);
  // 5 regular edges contribute twice, the self-loop once.
  EXPECT_EQ(index_.incident.size(), 11);

  const CallGraph::Vertex e = call_graph_.GetVertex(0x50000);
  EXPECT_EQ(index_.offsets[e + 1] - index_.offsets[e], 1);
}

// Covers both the indexed path (small subsets) and the full-scan fallback (all
// flow graphs), including deduplication of edges whose endpoints are both
// requested and preservation of the original feature order.
TEST_F(EdgeFeatureIndexTest, FilterResultsMatchesFullScanForAllSubsets) {
  const std::vector<FlowGraph*> all(binary_->flow_graphs.begin(),
                                    binary_->flow_graphs.end());
  ASSERT_EQ(all.size(), 6);
  for (uint32_t mask = 0; mask < (1u << all.size()); ++mask) {
    FlowGraphs subset;
    for (size_t i = 0; i < all.size(); ++i) {
      if (mask & (1u << i)) {
        subset.insert(all[i]);
      }
    }
    EdgeFeatures actual;
    Peer::FilterResults(index_, call_graph_, subset, &actual);
    EXPECT_THAT(ToAddresses(actual), ElementsAreArray(ExpectedEdges(subset)))
        << "subset mask " << mask;
  }
}

TEST_F(EdgeFeatureIndexTest, FilterResultsSkipsEdgesBetweenFixedPoints) {
  FlowGraph* a = GetFlowGraph(0x10000);
  FlowGraph* b = GetFlowGraph(0x20000);
  FixedPoint fixed_point(a, b);
  a->SetFixedPoint(&fixed_point);
  b->SetFixedPoint(&fixed_point);

  const FlowGraphs subset = {a};
  EdgeFeatures actual;
  Peer::FilterResults(index_, call_graph_, subset, &actual);
  const std::vector<AddressPair> edges = ToAddresses(actual);
  EXPECT_THAT(edges, Not(Contains(Pair(0x10000, 0x20000))));
  EXPECT_THAT(edges, ElementsAreArray(ExpectedEdges(subset)));
  EXPECT_THAT(edges, Contains(Pair(0x10000, 0x30000)));

  a->SetFixedPoint(nullptr);
  b->SetFixedPoint(nullptr);
}

// A flow graph whose cached call graph vertex does not match the call graph
// must not be looked up through the index, as that selects another function's
// edges. Debug builds flag it, release builds fall back to the full scan.
TEST_F(EdgeFeatureIndexTest, FilterResultsDoesNotIndexStaleVertices) {
  // Same function at the same address, but one vertex further along because of
  // the extra function in front of it.
  std::unique_ptr<DiffBinary> shifted =
      DiffBinaryBuilder()
          .AddFunctions({
              MakeFunction(0x8000, "func_0", {}),
              MakeFunction(0x10000, "func_a", {}),
          })
          .Build(cache_);
  FlowGraph* stale =
      shifted->call_graph.GetFlowGraph(shifted->call_graph.GetVertex(0x10000));
  ASSERT_NE(stale, nullptr);
  ASSERT_NE(stale->GetCallGraphVertex(), call_graph_.GetVertex(0x10000));

  const FlowGraphs subset = {stale};
#ifdef NDEBUG
  EdgeFeatures actual;
  Peer::FilterResults(index_, call_graph_, subset, &actual);
  const std::vector<AddressPair> edges = ToAddresses(actual);
  EXPECT_THAT(edges, ElementsAreArray(ExpectedEdges(subset)));
  EXPECT_THAT(edges, Contains(Pair(0x10000, 0x20000)));
  EXPECT_THAT(edges, Not(Contains(Pair(0x20000, 0x30000))));
#else
  EXPECT_DEATH_IF_SUPPORTED(
      {
        EdgeFeatures actual;
        Peer::FilterResults(index_, call_graph_, subset, &actual);
      },
      "Stale call graph vertex");
#endif
}

}  // namespace
}  // namespace security::bindiff
