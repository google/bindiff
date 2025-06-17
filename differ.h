// Copyright 2011-2024 Google LLC
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

#ifndef DIFFER_H_
#define DIFFER_H_

#include <cstddef>
#include <string>

#include "third_party/absl/base/nullability.h"
#include "third_party/absl/container/btree_map.h"
#include "third_party/absl/status/status.h"
#include "third_party/zynamics/bindiff/call_graph.h"
#include "third_party/zynamics/bindiff/fixed_points.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/instruction.h"
#include "third_party/zynamics/bindiff/match/context.h"
#include "third_party/zynamics/bindiff/reader.h"
#include "third_party/zynamics/bindiff/statistics.h"

namespace security::bindiff {

class MatchingContext;

// These need to be sorted
using Histogram = absl::btree_map<std::string, size_t>;
using Confidences = absl::btree_map<std::string, double>;

// Main entry point to the differ, runs the core algorithm and produces a
// (partial) matching between the two inputs.
void Diff(MatchingContext* absl_nonnull context,
          const MatchingSteps& call_graph_steps,
          const MatchingStepsFlowGraph& basic_block_steps);

class ScopedCleanup {
 public:
  ScopedCleanup(FlowGraphs* absl_nullable flow_graphs1,
                FlowGraphs* absl_nullable flow_graphs2,
                Instruction::Cache* absl_nullable instruction_cache);
  ~ScopedCleanup();

 private:
  FlowGraphs* flow_graphs1_;
  FlowGraphs* flow_graphs2_;
  Instruction::Cache* instruction_cache_;
};

void DeleteFlowGraphs(FlowGraphs* absl_nullable flow_graphs);

// Removes all fixed point assignments from flow graphs so they can be used
// again for a different comparison.
void ResetMatches(FlowGraphs* absl_nonnull flow_graphs);

// Loads a .BinExport file into the internal data structures.
absl::Status Read(const std::string& filename,
                  CallGraph* absl_nonnull call_graph,
                  FlowGraphs* absl_nonnull flow_graphs,
                  FlowGraphInfos* absl_nullable flow_graph_infos,
                  Instruction::Cache* absl_nonnull instruction_cache);

// Gets the similarity score for two full binaries.
double GetSimilarityScore(const CallGraph& call_graph1,
                          const CallGraph& call_graph2,
                          const Histogram& histogram, const Counts& counts);

// Gets the similarity score for a pair of functions.
double GetSimilarityScore(const FlowGraph& flow_graph1,
                          const FlowGraph& flow_graph2,
                          const Histogram& histogram, const Counts& counts);

// Gets the confidence value for a match specified by histogram.
double GetConfidence(const Histogram& histogram, Confidences* confidences);

// Prepares data structures for retrieving confidence and similarity.
void GetCountsAndHistogram(const FlowGraphs& flow_graphs1,
                           const FlowGraphs& flow_graphs2,
                           const FixedPoints& fixed_points,
                           Histogram* absl_nonnull histogram,
                           Counts* absl_nonnull counts);

// Collects various statistics (no of instructions/edges/basic blocks).
void Count(const FlowGraphs& flow_graphs, Counts* absl_nonnull counts);
void Count(const FlowGraph& flow_graph, Counts* absl_nonnull counts);
void Count(const FixedPoint& fixed_point, Counts* absl_nonnull counts,
           Histogram* absl_nonnull histogram);

}  // namespace security::bindiff

#endif  // DIFFER_H_
