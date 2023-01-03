// Copyright 2011-2023 Google LLC
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

#include "third_party/zynamics/bindiff/writer.h"

#include <memory>

#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "third_party/zynamics/binexport/call_graph.h"

namespace security::bindiff {
namespace {

using ::testing::Eq;
using ::testing::IsFalse;
using ::testing::IsTrue;

class WriterTest : public ::testing::Test {
 protected:
  CallGraph call_graph1_;
  CallGraph call_graph2_;
  FlowGraphs flow_graphs1_;
  FlowGraphs flow_graphs2_;
  FixedPoints fixed_points_;
};

class CountingNopWriter : public Writer {
 public:
  explicit CountingNopWriter(int* counter) : counter_(counter) {}

 private:
  void Write(const CallGraph& /*call_graph1*/, const CallGraph& /*call_graph2*/,
             const FlowGraphs& /*flow_graphs1*/,
             const FlowGraphs& /*flow_graphs2*/,
             const FixedPoints& /*fixed_points*/) override {
    ++*counter_;
  }

  int* counter_;
};

TEST_F(WriterTest, EmptyChainDoesNothing) {
  ChainWriter chain;
  EXPECT_THAT(chain.IsEmpty(), IsTrue());
  chain.Write(call_graph1_, call_graph2_, flow_graphs1_, flow_graphs2_,
              fixed_points_);
}

TEST_F(WriterTest, CanChainWriters) {
  ChainWriter chain;

  int count = 0;
  chain.Add(absl::make_unique<CountingNopWriter>(&count));
  chain.Add(absl::make_unique<CountingNopWriter>(&count));
  chain.Add(absl::make_unique<CountingNopWriter>(&count));
  EXPECT_THAT(chain.IsEmpty(), IsFalse());

  chain.Write(call_graph1_, call_graph2_, flow_graphs1_, flow_graphs2_,
              fixed_points_);
  EXPECT_THAT(count, Eq(3));
}

}  // namespace
}  // namespace security::bindiff
