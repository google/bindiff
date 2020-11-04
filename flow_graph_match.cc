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

#include "third_party/zynamics/bindiff/flow_graph_match.h"

#include <map>
#include <unordered_map>

#include "base/logging.h"
#include "third_party/absl/container/flat_hash_map.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/bindiff/config.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_call_refs.h"
#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_edges_lengauer_tarjan.h"
#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_edges_mdindex.h"
#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_edges_prime.h"
#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_entry_node.h"
#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_hash.h"
#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_instruction_count.h"
#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_jump_sequence.h"
#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_loop_entry.h"
#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_mdindex.h"
#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_mdindex_relaxed.h"
#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_prime.h"
#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_self_loop.h"
#include "third_party/zynamics/bindiff/flow_graph_match_basic_block_string_refs.h"
#include "third_party/zynamics/bindiff/match_context.h"

namespace security::bindiff {
namespace {

double GetConfidenceFromConfig(const std::string& name) {
  const auto& algorithms = config::Proto().basic_block_matching();
  const auto found = algorithms.find(name);
  return found != algorithms.end() ? found->second.confidence()
                                   : -1.0 /* Not found/commented out */;
}

// Special matching step of last resort. Single unmatched parents/children of
// matched basic blocks are matched - with no regard of their content.
bool MatchUnique(const VertexSet& vertices1, const VertexSet& vertices2,
                 FixedPoint& fixed_point) {
  if (vertices1.size() == 1 && vertices2.size() == 1) {
    return fixed_point.Add(*vertices1.begin(), *vertices2.begin(),
                           MatchingStepFlowGraph::kBasicBlockPropagationName) !=
           fixed_point.GetBasicBlockFixedPoints().end();
  }
  return false;
}

void GetUnmatchedChildren(const FlowGraph* graph, FlowGraph::Vertex vertex,
                          VertexSet* vertices) {
  vertices->clear();
  for (auto [it, end] = boost::out_edges(vertex, graph->GetGraph()); it != end;
       ++it) {
    const auto target = boost::target(*it, graph->GetGraph());
    if (!graph->GetFixedPoint(target)) {
      vertices->emplace(target);
    }
  }
}

void GetUnmatchedParents(const FlowGraph* graph, FlowGraph::Vertex vertex,
                         VertexSet* vertices) {
  vertices->clear();
  for (auto [it, end] = boost::in_edges(vertex, graph->GetGraph()); it != end;
       ++it) {
    const auto source = boost::source(*it, graph->GetGraph());
    if (!graph->GetFixedPoint(source)) {
      vertices->emplace(source);
    }
  }
}

}  // namespace

void AddFlag(FlowGraph* flow_graph, const FlowGraph::Edge& edge, size_t flag) {
  const auto source = boost::source(edge, flow_graph->GetGraph());
  const auto target = boost::target(edge, flow_graph->GetGraph());
  flow_graph->SetFlags(source, flow_graph->GetFlags(source) | flag);
  flow_graph->SetFlags(target, flow_graph->GetFlags(target) | flag);
}

void FindFixedPointsBasicBlock(FixedPoint* fixed_point,
                               MatchingContext* context,
                               const MatchingStepsFlowGraph& default_steps) {
  FlowGraph* primary = fixed_point->GetPrimary();
  FlowGraph* secondary = fixed_point->GetSecondary();
  VertexSet vertices1, vertices2;
  for (MatchingStepsFlowGraph matching_steps_for_current_level = default_steps;
       !matching_steps_for_current_level.empty();
       matching_steps_for_current_level.pop_front()) {
    for (auto [it, end] = boost::vertices(primary->GetGraph()); it != end;
         ++it) {
      if (!primary->GetFixedPoint(*it)) {
        vertices1.emplace(*it);
      }
    }
    for (auto [it, end] = boost::vertices(secondary->GetGraph()); it != end;
         ++it) {
      if (!secondary->GetFixedPoint(*it)) {
        vertices2.emplace(*it);
      }
    }
    if (vertices1.empty() || vertices2.empty()) {
      return;  // Already matched everything.
    }
    MatchingStepsFlowGraph matching_steps = matching_steps_for_current_level;
    matching_steps.front()->FindFixedPoints(primary, secondary, vertices1,
                                            vertices2, fixed_point, context,
                                            &matching_steps);
    matching_steps = matching_steps_for_current_level;

    bool more_fixed_points_discovered = false;
    do {
      more_fixed_points_discovered = false;
      BasicBlockFixedPoints& fixed_points =
          fixed_point->GetBasicBlockFixedPoints();
      // Propagate down to unmatched children.
      for (const auto& basic_block_fixed_point : fixed_points) {
        GetUnmatchedChildren(
            primary, basic_block_fixed_point.GetPrimaryVertex(), &vertices1);
        GetUnmatchedChildren(secondary,
                             basic_block_fixed_point.GetSecondaryVertex(),
                             &vertices2);
        matching_steps = matching_steps_for_current_level;
        if (!vertices1.empty() && !vertices2.empty()) {
          more_fixed_points_discovered |=
              matching_steps.front()->FindFixedPoints(
                  primary, secondary, vertices1, vertices2, fixed_point,
                  context, &matching_steps);
        }
      }

      // Propagate up to unmatched parents.
      for (const auto& basic_block_fixed_point : fixed_points) {
        GetUnmatchedParents(primary, basic_block_fixed_point.GetPrimaryVertex(),
                            &vertices1);
        GetUnmatchedParents(secondary,
                            basic_block_fixed_point.GetSecondaryVertex(),
                            &vertices2);
        matching_steps = matching_steps_for_current_level;
        if (!vertices1.empty() && !vertices2.empty()) {
          more_fixed_points_discovered |=
              matching_steps.front()->FindFixedPoints(
                  primary, secondary, vertices1, vertices2, fixed_point,
                  context, &matching_steps);
        }
      }
    } while (more_fixed_points_discovered);
  }

  bool more_fixed_points_discovered = false;
  do {
    // Last resort: Match everything that's connected to a fixed point via
    // a unique edge.
    more_fixed_points_discovered = false;
    BasicBlockFixedPoints& fixed_points =
        fixed_point->GetBasicBlockFixedPoints();
    for (const auto& basic_block_fixed_point : fixed_points) {
      // Propagate down to unmatched children.
      GetUnmatchedChildren(primary, basic_block_fixed_point.GetPrimaryVertex(),
                           &vertices1);
      GetUnmatchedChildren(
          secondary, basic_block_fixed_point.GetSecondaryVertex(), &vertices2);
      more_fixed_points_discovered |=
          MatchUnique(vertices1, vertices2, *fixed_point);
      // Propagate up to unmatched parents.
      GetUnmatchedParents(primary, basic_block_fixed_point.GetPrimaryVertex(),
                          &vertices1);
      GetUnmatchedParents(
          secondary, basic_block_fixed_point.GetSecondaryVertex(), &vertices2);
      more_fixed_points_discovered |=
          MatchUnique(vertices1, vertices2, *fixed_point);
    }
  } while (more_fixed_points_discovered);
}

MatchingStepFlowGraph::MatchingStepFlowGraph(std::string name,
                                             std::string display_name)
    : name_(std::move(name)),
      display_name_(std::move(display_name)),
      confidence_(GetConfidenceFromConfig(name_)) {}

MatchingStepsFlowGraph GetDefaultMatchingStepsBasicBlock() {
  static const auto* algorithms =
      []() -> absl::flat_hash_map<std::string, MatchingStepFlowGraph*>* {
    auto* algorithms =
        new absl::flat_hash_map<std::string, MatchingStepFlowGraph*>;
    // TODO(cblichmann): Add proximity md index matching.
    // TODO(cblichmann): Add relaxed and proximity edge matching.
    // TODO(cblichmann): Make it possible to disable propagation == 1 matching.
    for (auto* step : std::initializer_list<MatchingStepFlowGraph*>{
             // Edge based algorithms:
             new MatchingStepEdgesMdIndex(kTopDown),
             new MatchingStepEdgesMdIndex(kBottomUp),
             new MatchingStepEdgesPrimeProduct(),
             new MatchingStepEdgesLoop(),
             // Basic block based algorithms:
             new MatchingStepMdIndex(kTopDown),
             new MatchingStepMdIndex(kBottomUp),
             new MatchingStepHashBasicBlock(4),
             new MatchingStepPrimeBasicBlock(4),
             new MatchingStepCallReferences(),
             new MatchingStepStringReferences(),
             new MatchingStepMdIndexRelaxed(),
             new MatchingStepPrimeBasicBlock(0),
             new MatchingStepLoopEntry(),
             new MatchingStepSelfLoops(),
             new MatchingStepEntryNodes(kTopDown),
             new MatchingStepEntryNodes(kBottomUp),
             new MatchingStepInstructionCount(),
             new MatchingStepJumpSequence(),
         }) {
      (*algorithms)[step->name()] = step;
    }
    return algorithms;
  }();

  static const auto* matching_steps = []() -> const MatchingStepsFlowGraph* {
    auto* matching_steps = new MatchingStepsFlowGraph();
    for (const auto& [name, _] : config::Proto().basic_block_matching()) {
      if (auto found = algorithms->find(name); found != algorithms->end()) {
        matching_steps->push_back(found->second);
      }
    }
    LOG_IF(FATAL, matching_steps->empty())
        << "No basic block matching algorithms registered";
    return matching_steps;
  }();

  return *matching_steps;
}

}  // namespace security::bindiff
