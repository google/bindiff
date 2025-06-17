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

#include "third_party/zynamics/bindiff/differ.h"

#include <algorithm>
#include <cmath>
#include <cstdint>
#include <fstream>
#include <ios>
#include <memory>
#include <string>

#include "third_party/absl/base/nullability.h"
#include "third_party/absl/log/check.h"
#include "third_party/absl/memory/memory.h"
#include "third_party/absl/status/status.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/bindiff/call_graph.h"
#include "third_party/zynamics/bindiff/change_classifier.h"
#include "third_party/zynamics/bindiff/fixed_points.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/instruction.h"
#include "third_party/zynamics/bindiff/match/call_graph.h"
#include "third_party/zynamics/bindiff/match/context.h"
#include "third_party/zynamics/bindiff/match/flow_graph.h"
#include "third_party/zynamics/bindiff/reader.h"
#include "third_party/zynamics/bindiff/statistics.h"
#include "third_party/zynamics/binexport/binexport2.pb.h"
#include "third_party/zynamics/binexport/util/filesystem.h"
#include "third_party/zynamics/binexport/util/format.h"
#include "third_party/zynamics/binexport/util/status_macros.h"
#include "third_party/zynamics/binexport/util/types.h"

namespace security::bindiff {

using ::security::binexport::FormatAddress;

// Return the immediate children of the call graph node denoted by
// address. Skip nodes that have already been matched.
void GetUnmatchedChildren(const CallGraph& call_graph, CallGraph::Vertex vertex,
                          FlowGraphs* absl_nonnull children) {
  for (auto [edge_it, edge_end] =
           boost::out_edges(vertex, call_graph.GetGraph());
       edge_it != edge_end; ++edge_it) {
    if (call_graph.IsDuplicate(*edge_it)) {
      continue;
    }

    const CallGraph::Vertex target =
        boost::target(*edge_it, call_graph.GetGraph());

    FlowGraph* child = call_graph.GetFlowGraph(target);
    if (!child || child->GetFixedPoint()) {
      continue;
    }

    children->insert(child);
  }
}

// Returns the immediate parents of the call graph node denoted by address.
// Skips nodes that have already been matched.
void GetUnmatchedParents(const CallGraph& call_graph, CallGraph::Vertex vertex,
                         FlowGraphs* absl_nonnull parents) {
  for (auto [edge_it, edge_end] =
           boost::in_edges(vertex, call_graph.GetGraph());
       edge_it != edge_end; ++edge_it) {
    if (call_graph.IsDuplicate(*edge_it)) {
      continue;
    }

    const CallGraph::Vertex source =
        boost::source(*edge_it, call_graph.GetGraph());

    FlowGraph* parent = call_graph.GetFlowGraph(source);
    if (!parent || parent->GetFixedPoint()) {
      continue;
    }

    parents->insert(parent);
  }
}

// Adds empty flow graphs to all call graph vertices that don't already have one
// attached (for example for DLL stub functions). Returns an error if a flow
// graph already exists for a call graph vertex.
absl::Status AddSubsToCallGraph(CallGraph* absl_nonnull call_graph,
                                FlowGraphs* absl_nonnull flow_graphs) {
  for (auto [it, end] = boost::vertices(call_graph->GetGraph()); it != end;
       ++it) {
    const CallGraph::Vertex vertex = *it;
    const Address address = call_graph->GetAddress(vertex);
    FlowGraph* flow_graph = call_graph->GetFlowGraph(vertex);
    if (flow_graph) {
      continue;
    }

    flow_graph = new FlowGraph(call_graph, address);
    call_graph->SetStub(vertex, true);
    call_graph->SetLibrary(vertex, true);
    if (!flow_graphs->insert(flow_graph).second) {
      return absl::FailedPreconditionError(
          absl::StrCat("a flow graph exists at ", FormatAddress(address)));
    }
  }
  return absl::OkStatus();
}

absl::Status SetupGraphsFromProto(
    const BinExport2& proto, const std::string& filename,
    CallGraph* absl_nonnull call_graph,
    FlowGraphs* absl_nonnull flow_graphs,
    FlowGraphInfos* absl_nullable flow_graph_infos,
    Instruction::Cache* absl_nonnull instruction_cache) {
  NA_RETURN_IF_ERROR(call_graph->Read(proto, filename));
  for (const auto& proto_flow_graph : proto.flow_graph()) {
    if (proto_flow_graph.basic_block_index_size() == 0) {
      continue;
    }
    auto flow_graph = absl::make_unique<FlowGraph>();
    NA_RETURN_IF_ERROR(flow_graph->Read(proto, proto_flow_graph, call_graph,
                                        instruction_cache));

    Counts counts;
    Count(*flow_graph, &counts);

    if (flow_graph_infos) {
      const auto address = flow_graph->GetEntryPointAddress();
      auto& info = (*flow_graph_infos)[address];
      info.address = address;
      info.name = &flow_graph->GetName();
      info.demangled_name = &flow_graph->GetDemangledName();
      info.basic_block_count = counts[Counts::kBasicBlocksLibrary] +
                               counts[Counts::kBasicBlocksNonLibrary];
      info.edge_count =
          counts[Counts::kEdgesLibrary] + counts[Counts::kEdgesNonLibrary];
      info.instruction_count = counts[Counts::kInstructionsLibrary] +
                               counts[Counts::kInstructionsNonLibrary];
    }
    flow_graphs->insert(flow_graph.release());
  }

  return AddSubsToCallGraph(call_graph, flow_graphs);
}

absl::Status Read(const std::string& filename,
                  CallGraph* absl_nonnull call_graph,
                  FlowGraphs* absl_nonnull flow_graphs,
                  FlowGraphInfos* absl_nullable flow_graph_infos,
                  Instruction::Cache* absl_nonnull instruction_cache) {
  call_graph->Reset();
  DeleteFlowGraphs(flow_graphs);
  if (flow_graph_infos) {
    flow_graph_infos->clear();
  }

  constexpr int64_t kMinFileSize = 8;
  NA_ASSIGN_OR_RETURN(int64_t file_size, GetFileSize(filename));
  if (file_size <= kMinFileSize) {
    return absl::FailedPreconditionError(
        absl::StrCat("file too small: ", filename));
  }

  std::ifstream stream(filename, std::ios::binary);
  BinExport2 proto;
  if (!proto.ParseFromIstream(&stream)) {
    return absl::FailedPreconditionError(
        absl::StrCat("parsing failed for exported file: ", filename));
  }
  return SetupGraphsFromProto(proto, filename, call_graph, flow_graphs,
                              flow_graph_infos, instruction_cache);
}

void DeleteFlowGraphs(FlowGraphs* absl_nullable flow_graphs) {
  if (!flow_graphs) {
    return;
  }

  for (auto* flow_graph : *flow_graphs) {
    delete flow_graph;
  }
  flow_graphs->clear();
}

ScopedCleanup::ScopedCleanup(
    FlowGraphs* absl_nullable flow_graphs1,
    FlowGraphs* absl_nullable flow_graphs2,
    Instruction::Cache* absl_nullable instruction_cache)
    : flow_graphs1_(flow_graphs1),
      flow_graphs2_(flow_graphs2),
      instruction_cache_(instruction_cache) {}

ScopedCleanup::~ScopedCleanup() {
  DeleteFlowGraphs(flow_graphs1_);
  DeleteFlowGraphs(flow_graphs2_);
  if (instruction_cache_) {
    instruction_cache_->clear();
  }
}

void ResetMatches(FlowGraphs* absl_nonnull flow_graphs) {
  for (auto* flow_graph : *flow_graphs) {
    flow_graph->ResetMatches();
  }
}

void Diff(MatchingContext* absl_nonnull context,
          const MatchingSteps& call_graph_steps,
          const MatchingStepsFlowGraph& basic_block_steps) {
  // The outer loop controls the rigorousness for initial matching while the
  // inner loop tries to resolve ambiguities by drilling down the matchingSteps
  // lists.
  for (MatchingSteps matching_steps_for_current_level = call_graph_steps;
       !matching_steps_for_current_level.empty();
       matching_steps_for_current_level.pop_front()) {
    context->new_fixed_points_.clear();
    MatchingSteps matching_steps = matching_steps_for_current_level;
    MatchingStep* step = matching_steps.front();
    step->FindFixedPoints(
        nullptr /* primary_parent */, nullptr /* secondary_parent */,
        context->primary_flow_graphs_, context->secondary_flow_graphs_,
        *context, matching_steps, basic_block_steps);
    matching_steps = matching_steps_for_current_level;

    bool more_fixed_points_discovered = false;
    do {
      more_fixed_points_discovered = false;

      // Performance: We iterate over _all_ fixed points discovered so far. The
      // idea being that parents/children that previously lead to ambiguous
      // matches may now be unique after some of their siblings have been
      // matched. This is expensive and we may want to iterate new fixed points
      // only instead?
      // Propagate down to the children of the new fixed points.
      for (const auto& fixed_point : context->fixed_points_) {
        matching_steps = matching_steps_for_current_level;
        FlowGraphs primary_children, secondary_children;
        GetUnmatchedChildren(context->primary_call_graph_,
                             fixed_point.GetPrimary()->GetCallGraphVertex(),
                             &primary_children);
        GetUnmatchedChildren(context->secondary_call_graph_,
                             fixed_point.GetSecondary()->GetCallGraphVertex(),
                             &secondary_children);
        if (!primary_children.empty() && !secondary_children.empty()) {
          more_fixed_points_discovered |= step->FindFixedPoints(
              fixed_point.GetPrimary(), fixed_point.GetSecondary(),
              primary_children, secondary_children, *context, matching_steps,
              basic_block_steps);
        }
      }

      // Propagate up to the parents of the new fixed points.
      for (const auto& fixed_point : context->fixed_points_) {
        matching_steps = matching_steps_for_current_level;
        FlowGraphs primary_parents, secondary_parents;
        GetUnmatchedParents(context->primary_call_graph_,
                            fixed_point.GetPrimary()->GetCallGraphVertex(),
                            &primary_parents);
        GetUnmatchedParents(context->secondary_call_graph_,
                            fixed_point.GetSecondary()->GetCallGraphVertex(),
                            &secondary_parents);
        if (!primary_parents.empty() && !secondary_parents.empty()) {
          more_fixed_points_discovered |= step->FindFixedPoints(
              fixed_point.GetPrimary(), fixed_point.GetSecondary(),
              primary_parents, secondary_parents, *context, matching_steps,
              basic_block_steps);
        }
      }
    } while (more_fixed_points_discovered);

    // After collecting initial fixed points for this step: iterate over all of
    // them and find call reference fixed points.
    for (auto* fixed_point : context->new_fixed_points_) {
      FindCallReferenceFixedPoints(fixed_point, context, basic_block_steps);
    }
  }
  ClassifyChanges(context);
}

void Count(const FlowGraph& flow_graph, Counts* absl_nonnull counts) {
  FlowGraphs flow_graphs;
  CHECK(flow_graphs.insert(&const_cast<FlowGraph&>(flow_graph)).second);
  Count(flow_graphs, counts);
}

void Count(const FlowGraphs& flow_graphs, Counts* absl_nonnull counts) {
  uint64_t num_functions = 0;
  uint64_t num_basic_blocks = 0;
  uint64_t num_instructions = 0;
  uint64_t num_edges = 0;
  uint64_t num_lib_functions = 0;
  uint64_t num_lib_basic_blocks = 0;
  uint64_t num_lib_instructions = 0;
  uint64_t num_lib_edges = 0;
  for (const FlowGraph* flow_graph : flow_graphs) {
    uint64_t& basic_blocks =
        flow_graph->IsLibrary() ? num_lib_basic_blocks : num_basic_blocks;
    uint64_t& instructions =
        flow_graph->IsLibrary() ? num_lib_instructions : num_instructions;
    uint64_t& edges = flow_graph->IsLibrary() ? num_lib_edges : num_edges;
    num_functions += 1 - flow_graph->IsLibrary();
    num_lib_functions += flow_graph->IsLibrary();

    for (auto [it, end] = boost::vertices(flow_graph->GetGraph()); it != end;
         ++it) {
      ++basic_blocks;
      instructions += flow_graph->GetInstructionCount(*it);
    }
    edges += boost::num_edges(flow_graph->GetGraph());
  }

  (*counts)[Counts::kFunctionsLibrary] = num_lib_functions;
  (*counts)[Counts::kFunctionsNonLibrary] = num_functions;
  (*counts)[Counts::kBasicBlocksLibrary] = num_lib_basic_blocks;
  (*counts)[Counts::kBasicBlocksNonLibrary] = num_basic_blocks;
  (*counts)[Counts::kInstructionsLibrary] = num_lib_instructions;
  (*counts)[Counts::kInstructionsNonLibrary] = num_instructions;
  (*counts)[Counts::kEdgesLibrary] = num_lib_edges;
  (*counts)[Counts::kEdgesNonLibrary] = num_edges;
}

void Count(const FixedPoint& fixed_point, Counts* counts,
           Histogram* histogram) {
  (*counts)[Counts::kFunctionMatchesLibrary] = 0;
  (*counts)[Counts::kBasicBlockMatchesLibrary] = 0;
  (*counts)[Counts::kInstructionMatchesLibrary] = 0;
  (*counts)[Counts::kFlowGraphEdgeMatchesLibrary] = 0;
  (*counts)[Counts::kFunctionMatchesNonLibrary] = 0;
  (*counts)[Counts::kBasicBlockMatchesNonLibrary] = 0;
  (*counts)[Counts::kInstructionMatchesNonLibrary] = 0;
  (*counts)[Counts::kFlowGraphEdgeMatchesNonLibrary] = 0;

  const FlowGraph* primary = fixed_point.GetPrimary();
  const FlowGraph* secondary = fixed_point.GetSecondary();
  const bool library = primary->IsLibrary() || secondary->IsLibrary();
  uint64_t& functions = (*counts)[library ? Counts::kFunctionMatchesLibrary
                                          : Counts::kFunctionMatchesNonLibrary];
  uint64_t& basic_blocks =
      (*counts)[library ? Counts::kBasicBlockMatchesLibrary
                        : Counts::kBasicBlockMatchesNonLibrary];
  uint64_t& instructions =
      (*counts)[library ? Counts::kInstructionMatchesLibrary
                        : Counts::kInstructionMatchesNonLibrary];
  uint64_t& edges =
      (*counts)[library ? Counts::kFlowGraphEdgeMatchesLibrary
                        : Counts::kFlowGraphEdgeMatchesNonLibrary];

  ++(*histogram)[fixed_point.GetMatchingStep()];
  ++functions;
  basic_blocks += fixed_point.GetBasicBlockFixedPoints().size();
  for (const auto& basic_block : fixed_point.GetBasicBlockFixedPoints()) {
    ++(*histogram)[basic_block.GetMatchingStep()];
    instructions += basic_block.GetInstructionMatches().size();
  }

  for (auto [primary_it, primary_end] = boost::edges(primary->GetGraph());
       primary_it != primary_end; ++primary_it) {
    const auto source1 = boost::source(*primary_it, primary->GetGraph());
    const auto target1 = boost::target(*primary_it, primary->GetGraph());
    // Source and target basic blocks are matched, check whether there's an
    // edge connecting the two.
    if (primary->GetFixedPoint(source1) && primary->GetFixedPoint(target1)) {
      const auto source2 =
          primary->GetFixedPoint(source1)->GetSecondaryVertex();
      const Address target2 =
          primary->GetFixedPoint(target1)->GetSecondaryVertex();
      // Both are in secondary graph as well.
      for (auto [secondary_it, secondary_end] =
               boost::out_edges(source2, secondary->GetGraph());
           secondary_it != secondary_end; ++secondary_it) {
        if (boost::target(*secondary_it, secondary->GetGraph()) == target2) {
          ++edges;
          break;
        }
      }
    }
  }
}

double GetConfidence(const Histogram& histogram, Confidences* confidences) {
  for (const auto* step : GetDefaultMatchingSteps()) {
    (*confidences)[step->name()] = step->confidence();
  }
  for (const auto* step : GetDefaultMatchingStepsBasicBlock()) {
    (*confidences)[step->name()] = step->confidence();
  }
  (*confidences)[MatchingStepFlowGraph::kBasicBlockPropagationName] = 0.0;
  (*confidences)[MatchingStep::kFunctionCallReferenceName] = 0.75;
  double confidence = 0.0;
  double match_count = 0;
  for (const auto& [name, value] : histogram) {
    confidence += value * (*confidences)[name];
    match_count += value;
  }
  // Sigmoid squashing function
  return match_count
             ? 1.0 / (1.0 + exp(-(confidence / match_count - 0.5) * 10.0))
             : 0.0;
}

void GetCountsAndHistogram(const FlowGraphs& flow_graphs1,
                           const FlowGraphs& flow_graphs2,
                           const FixedPoints& fixed_points,
                           Histogram* absl_nonnull histogram,
                           Counts* absl_nonnull counts) {
  Counts counts1;
  Counts counts2;
  Count(flow_graphs1, &counts1);
  Count(flow_graphs2, &counts2);

  (*counts)[Counts::kFunctionsPrimaryLibrary] =
      counts1[Counts::kFunctionsLibrary];
  (*counts)[Counts::kFunctionsPrimaryNonLibrary] =
      counts1[Counts::kFunctionsNonLibrary];
  (*counts)[Counts::kFunctionsSecondaryLibrary] =
      counts2[Counts::kFunctionsLibrary];
  (*counts)[Counts::kFunctionsSecondaryNonLibrary] =
      counts2[Counts::kFunctionsNonLibrary];
  (*counts)[Counts::kBasicBlocksPrimaryLibrary] =
      counts1[Counts::kBasicBlocksLibrary];
  (*counts)[Counts::kBasicBlocksPrimaryNonLibrary] =
      counts1[Counts::kBasicBlocksNonLibrary];
  (*counts)[Counts::kBasicBlocksSecondaryLibrary] =
      counts2[Counts::kBasicBlocksLibrary];
  (*counts)[Counts::kBasicBlocksSecondaryNonLibrary] =
      counts2[Counts::kBasicBlocksNonLibrary];

  (*counts)[Counts::kInstructionsPrimaryLibrary] =
      counts1[Counts::kInstructionsLibrary];
  (*counts)[Counts::kInstructionsPrimaryNonLibrary] =
      counts1[Counts::kInstructionsNonLibrary];
  (*counts)[Counts::kInstructionsSecondaryLibrary] =
      counts2[Counts::kInstructionsLibrary];
  (*counts)[Counts::kInstructionsSecondaryNonLibrary] =
      counts2[Counts::kInstructionsNonLibrary];

  (*counts)[Counts::kFlowGraphEdgesPrimaryLibrary] =
      counts1[Counts::kEdgesLibrary];
  (*counts)[Counts::kFlowGraphEdgesPrimaryNonLibrary] =
      counts1[Counts::kEdgesNonLibrary];
  (*counts)[Counts::kFlowGraphEdgesSecondaryLibrary] =
      counts2[Counts::kEdgesLibrary];
  (*counts)[Counts::kFlowGraphEdgesSecondaryNonLibrary] =
      counts2[Counts::kEdgesNonLibrary];

  (*counts)[Counts::kFunctionMatchesLibrary] = 0;
  (*counts)[Counts::kBasicBlockMatchesLibrary] = 0;
  (*counts)[Counts::kInstructionMatchesLibrary] = 0;
  (*counts)[Counts::kFlowGraphEdgeMatchesLibrary] = 0;
  (*counts)[Counts::kFunctionMatchesNonLibrary] = 0;
  (*counts)[Counts::kBasicBlockMatchesNonLibrary] = 0;
  (*counts)[Counts::kInstructionMatchesNonLibrary] = 0;
  (*counts)[Counts::kFlowGraphEdgeMatchesNonLibrary] = 0;
  for (auto i = fixed_points.cbegin(), end = fixed_points.cend(); i != end;
       ++i) {
    Counts fixed_point_counts;
    Count(*i, &fixed_point_counts, histogram);
    (*counts)[Counts::kFunctionMatchesLibrary] +=
        fixed_point_counts[Counts::kFunctionMatchesLibrary];
    (*counts)[Counts::kBasicBlockMatchesLibrary] +=
        fixed_point_counts[Counts::kBasicBlockMatchesLibrary];
    (*counts)[Counts::kInstructionMatchesLibrary] +=
        fixed_point_counts[Counts::kInstructionMatchesLibrary];
    (*counts)[Counts::kFlowGraphEdgeMatchesLibrary] +=
        fixed_point_counts[Counts::kFlowGraphEdgeMatchesLibrary];
    (*counts)[Counts::kFunctionMatchesNonLibrary] +=
        fixed_point_counts[Counts::kFunctionMatchesNonLibrary];
    (*counts)[Counts::kBasicBlockMatchesNonLibrary] +=
        fixed_point_counts[Counts::kBasicBlockMatchesNonLibrary];
    (*counts)[Counts::kInstructionMatchesNonLibrary] +=
        fixed_point_counts[Counts::kInstructionMatchesNonLibrary];
    (*counts)[Counts::kFlowGraphEdgeMatchesNonLibrary] +=
        fixed_point_counts[Counts::kFlowGraphEdgeMatchesNonLibrary];
  }
}

// Flow graph similarity includes library functions.
double GetSimilarityScore(const FlowGraph& flow_graph1,
                          const FlowGraph& flow_graph2,
                          const Histogram& histogram, const Counts& counts) {
  const int basic_block_matches = counts[Counts::kBasicBlockMatchesNonLibrary] +
                                  counts[Counts::kBasicBlockMatchesLibrary];
  const int basic_blocks_primary =
      counts[Counts::kBasicBlocksPrimaryNonLibrary] +
      counts[Counts::kBasicBlocksPrimaryLibrary];
  const int basic_blocks_secondary =
      counts[Counts::kBasicBlocksSecondaryNonLibrary] +
      counts[Counts::kBasicBlocksSecondaryLibrary];
  const int instruction_matches =
      counts[Counts::kInstructionMatchesNonLibrary] +
      counts[Counts::kInstructionMatchesLibrary];
  const int instructions_primary =
      counts[Counts::kInstructionsPrimaryNonLibrary] +
      counts[Counts::kInstructionsPrimaryLibrary];
  const int instructions_secondary =
      counts[Counts::kInstructionsSecondaryNonLibrary] +
      counts[Counts::kInstructionsSecondaryLibrary];
  const int edge_matches = counts[Counts::kFlowGraphEdgeMatchesNonLibrary] +
                           counts[Counts::kFlowGraphEdgeMatchesLibrary];
  const int edges_primary = counts[Counts::kFlowGraphEdgesPrimaryNonLibrary] +
                            counts[Counts::kFlowGraphEdgesPrimaryLibrary];
  const int edges_secondary =
      counts[Counts::kFlowGraphEdgesSecondaryNonLibrary] +
      counts[Counts::kFlowGraphEdgesSecondaryLibrary];

  if (basic_block_matches == basic_blocks_primary &&
      basic_block_matches == basic_blocks_secondary &&
      instruction_matches == instructions_primary &&
      instruction_matches == instructions_secondary) {
    return 1.0;
  }

  double similarity = 0;
  similarity += 0.55 * edge_matches /
                (std::max(1.0, 0.5 * (edges_primary + edges_secondary)));
  similarity +=
      0.30 * basic_block_matches /
      (std::max(1.0, 0.5 * (basic_blocks_primary + basic_blocks_secondary)));
  similarity +=
      0.15 * instruction_matches /
      (std::max(1.0, 0.5 * (instructions_primary + instructions_secondary)));
  similarity = std::min(similarity, 1.0);
  similarity +=
      1.0 - std::fabs(flow_graph1.GetMdIndex() - flow_graph2.GetMdIndex()) /
                (1.0 + flow_graph1.GetMdIndex() + flow_graph2.GetMdIndex());
  similarity /= 2.0;

  // TODO(soerenme) Investigate this:
  //     Disable this because a 1.0 match gets voted down due to low confidence.
  Confidences confidences;
  similarity *= GetConfidence(histogram, &confidences);
  return similarity;
}

// Global similarity score excludes library functions so these won't
// inflate our similarity score.
double GetSimilarityScore(const CallGraph& call_graph1,
                          const CallGraph& call_graph2,
                          const Histogram& histogram, const Counts& counts) {
  double similarity = 0;
  similarity +=
      0.35 * counts[Counts::kFlowGraphEdgeMatchesNonLibrary] /
      (std::max(1.0,
                0.5 * (counts[Counts::kFlowGraphEdgesPrimaryNonLibrary] +
                       counts[Counts::kFlowGraphEdgesSecondaryNonLibrary])));
  similarity +=
      0.25 * counts[Counts::kBasicBlockMatchesNonLibrary] /
      (std::max(1.0, 0.5 * (counts[Counts::kBasicBlocksPrimaryNonLibrary] +
                            counts[Counts::kBasicBlocksSecondaryNonLibrary])));
  similarity +=
      0.10 * counts[Counts::kFunctionMatchesNonLibrary] /
      (std::max(1.0, 0.5 * (counts[Counts::kFunctionsPrimaryNonLibrary] +
                            counts[Counts::kFunctionsSecondaryNonLibrary])));
  similarity +=
      0.10 * counts[Counts::kInstructionMatchesNonLibrary] /
      (std::max(1.0, 0.5 * (counts[Counts::kInstructionsPrimaryNonLibrary] +
                            counts[Counts::kInstructionsSecondaryNonLibrary])));
  similarity +=
      0.20 *
      (1.0 - std::fabs(call_graph1.GetMdIndex() - call_graph2.GetMdIndex()) /
                 (1.0 + call_graph1.GetMdIndex() + call_graph2.GetMdIndex()));
  similarity = std::min(similarity, 1.0);

  Confidences confidences;
  similarity *= GetConfidence(histogram, &confidences);
  return similarity;
}

}  // namespace security::bindiff
