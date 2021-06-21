// Copyright 2011-2021 Google LLC
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

#ifndef DIFFER_H_
#define DIFFER_H_

#include <array>
#include <map>
#include <string>

#include "third_party/zynamics/bindiff/call_graph.h"
#include "third_party/zynamics/bindiff/fixed_points.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/match/context.h"
#include "third_party/zynamics/bindiff/reader.h"
#include "third_party/zynamics/bindiff/statistics.h"
#include "third_party/zynamics/binexport/types.h"

namespace security::bindiff {

class MatchingContext;

// These need to be sorted
using Histogram = std::map<std::string, size_t>;
using Confidences = std::map<std::string, double>;

// Main entry point to the differ. Runs the core algorithm and produces a
// (partial) matching between the two inputs.
void Diff(MatchingContext* context,
          const MatchingSteps& default_call_graph_steps,
          const MatchingStepsFlowGraph& default_basic_block_steps);

class ScopedCleanup {
 public:
  ScopedCleanup(FlowGraphs* flow_graphs1, FlowGraphs* flow_graphs2,
                Instruction::Cache* instruction_cache);
  ~ScopedCleanup();

 private:
  FlowGraphs* flow_graphs1_;
  FlowGraphs* flow_graphs2_;
  Instruction::Cache* instruction_cache_;
};

void DeleteFlowGraphs(FlowGraphs* flow_graphs);

// Remove all fixed point assignments from flow graphs so they can be used again
// for a different comparison.
void ResetMatches(FlowGraphs* flow_graphs);

// This adds empty flow graphs to all call graph vertices that don't already
// have one attached.
void AddSubsToCallGraph(CallGraph* call_graph, FlowGraphs* flow_graphs);

// Load a .BinExport file into the internal data structures.
// TODO(cblichmann): Make this function return an error instead of throwing.
void Read(const std::string& filename, CallGraph* call_graph,
          FlowGraphs* flow_graphs, FlowGraphInfos* flow_graph_infos,
          Instruction::Cache* instruction_cache);

// Get the similarity score for two full binaries.
double GetSimilarityScore(const CallGraph& call_graph1,
                          const CallGraph& call_graph2,
                          const Histogram& histogram, const Counts& counts);

// Get the similarity score for a pair of functions.
double GetSimilarityScore(const FlowGraph& flow_graph1,
                          const FlowGraph& flow_graph2,
                          const Histogram& histogram, const Counts& counts);

// Get the confidence value for a match specified by histogram.
double GetConfidence(const Histogram& histogram, Confidences* confidences);

// Prepare data structures for retrieving confidence and similarity.
void GetCountsAndHistogram(const FlowGraphs& flow_graphs1,
                           const FlowGraphs& flow_graphs2,
                           const FixedPoints& fixed_points,
                           Histogram* histogram, Counts* counts);

// Collect various statistics (no of instructions/edges/basic blocks).
void Count(const FlowGraphs& flow_graphs, Counts* counts);
void Count(const FlowGraph& flow_graph, Counts* counts);
void Count(const FixedPoint& fixed_point, Counts* counts, Histogram* histogram);

}  // namespace security::bindiff

#endif  // DIFFER_H_
