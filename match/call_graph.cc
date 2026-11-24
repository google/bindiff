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

#include <algorithm>
#include <cstddef>
#include <cstdint>
#include <initializer_list>
#include <iterator>
#include <limits>
#include <memory>
#include <string>
#include <utility>
#include <vector>

#include "third_party/absl/container/flat_hash_map.h"
#include "third_party/absl/log/check.h"
#include "third_party/absl/log/log.h"
#include "third_party/zynamics/bindiff/config.h"
#include "third_party/zynamics/bindiff/fixed_points.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/match/context.h"
#include "third_party/zynamics/bindiff/match/flow_graph.h"
#include "third_party/zynamics/bindiff/match/function_address_sequence.h"
#include "third_party/zynamics/bindiff/match/function_call_graph_edges_mdindex.h"
#include "third_party/zynamics/bindiff/match/function_call_graph_edges_proximity_mdindex.h"
#include "third_party/zynamics/bindiff/match/function_call_graph_mdindex.h"
#include "third_party/zynamics/bindiff/match/function_call_graph_mdindex_relaxed.h"
#include "third_party/zynamics/bindiff/match/function_call_sequence.h"
#include "third_party/zynamics/bindiff/match/function_flow_graph_edges_mdindex.h"
#include "third_party/zynamics/bindiff/match/function_flow_graph_mdindex.h"
#include "third_party/zynamics/bindiff/match/function_hash.h"
#include "third_party/zynamics/bindiff/match/function_instruction_count.h"
#include "third_party/zynamics/bindiff/match/function_loops.h"
#include "third_party/zynamics/bindiff/match/function_name_hash.h"
#include "third_party/zynamics/bindiff/match/function_prime.h"
#include "third_party/zynamics/bindiff/match/function_string_refs.h"

namespace security::bindiff {

bool IsValidCandidate(const FlowGraph* flow_graph) {
  return !flow_graph->GetFixedPoint() &&
         // These may only be matched by name:
         !(flow_graph->IsTrivial() && flow_graph->IsLibrary());
}

namespace {

bool KeyLessThan(const EdgeFeature& one, const EdgeFeature& two) {
  return one.source_value == two.source_value
             ? one.target_value < two.target_value
             : one.source_value < two.source_value;
}

double GetConfidenceFromConfig(const std::string& name) {
  const auto& algorithms = config::Proto().function_matching();
  const auto found =
      std::find_if(algorithms.begin(), algorithms.end(),
                   [&name](const Config::MatchingStep& step) -> bool {
                     return step.name() == name;
                   });
  return found != algorithms.end() ? found->confidence()
                                   : -1.0 /* Not found/commented out */;
}

// Returns whether any eligible flow graphs have been found.
bool GetFlowGraphs(const CallGraph& call_graph, EdgeFeatures::iterator begin,
                   EdgeFeatures::iterator end, FlowGraphs* flow_graphs) {
  for (; begin != end; ++begin) {
    FlowGraph* source = call_graph.GetFlowGraph(
        boost::source(begin->edge, call_graph.GetGraph()));
    if (source && !source->GetFixedPoint()) {
      flow_graphs->emplace(source);
    }
    FlowGraph* target = call_graph.GetFlowGraph(
        boost::target(begin->edge, call_graph.GetGraph()));
    if (target && !target->GetFixedPoint()) {
      flow_graphs->emplace(target);
    }
  }
  return !flow_graphs->empty();
}

bool AddFixedPointEdge(
    const std::pair<FixedPoints::iterator, bool>& fixed_point_iterator,
    MatchingContext* context, const std::string& name,
    const MatchingStepsFlowGraph& default_steps) {
  if (fixed_point_iterator.second) {
    FixedPoint& fixed_point =
        const_cast<FixedPoint&>(*fixed_point_iterator.first);
    FindFixedPointsBasicBlock(&fixed_point, context, default_steps);
    UpdateFixedPointConfidence(fixed_point);
  }
  return fixed_point_iterator.second;
}

}  // namespace

bool FindFixedPointsEdge(const FlowGraph* primary_parent,
                         const FlowGraph* secondary_parent,
                         EdgeFeatures* edges1, EdgeFeatures* edges2,
                         MatchingContext* context,
                         MatchingSteps* matching_steps,
                         const MatchingStepsFlowGraph& default_steps) {
  const std::string name = matching_steps->front()->name();
  matching_steps->pop_front();

  bool fixed_points_discovered = false;
  std::sort(edges1->begin(), edges1->end(), &KeyLessThan);
  std::sort(edges2->begin(), edges2->end(), &KeyLessThan);
  EdgeFeatures::iterator edges1_it = edges1->begin();
  EdgeFeatures::iterator edges2_it = edges2->begin();
  for (;;) {
    if (edges1_it == edges1->end() || edges2_it == edges2->end()) {
      break;
    }

    const EdgeFeature& key(*edges1_it);
    const EdgeFeatures::iterator next_edge1_it =
        std::upper_bound(edges1_it, edges1->end(), key, &KeyLessThan);
    const size_t count1 = std::distance(edges1_it, next_edge1_it);
    edges2_it = lower_bound(edges2_it, edges2->end(), key, &KeyLessThan);
    const EdgeFeatures::iterator next_edge2_it =
        std::upper_bound(edges2_it, edges2->end(), key, &KeyLessThan);
    const size_t count2 = std::distance(edges2_it, next_edge2_it);
    if (count1 == 1 && count2 == 1) {
      // Unique match.
      const CallGraph::Edge& edge1 = edges1_it->edge;
      FlowGraph* primary_source = context->primary_call_graph_.GetFlowGraph(
          boost::source(edge1, context->primary_call_graph_.GetGraph()));
      FlowGraph* primary_target = context->primary_call_graph_.GetFlowGraph(
          boost::target(edge1, context->primary_call_graph_.GetGraph()));
      const CallGraph::Edge& edge2 = edges2_it->edge;
      FlowGraph* secondary_source = context->secondary_call_graph_.GetFlowGraph(
          boost::source(edge2, context->secondary_call_graph_.GetGraph()));
      FlowGraph* secondary_target = context->secondary_call_graph_.GetFlowGraph(
          boost::target(edge2, context->secondary_call_graph_.GetGraph()));
      // Note: It may be tempting to move the iterators into the
      // AddFixedPointEdge() function. This will change the results though. We
      // want to add both endpoints of the edge as fixed points before doing the
      // basic block matching. Otherwise call targets from matched basic blocks
      // may take precedence over the original edge match.
      std::pair<FixedPoints::iterator, bool> fixed_point_iterator_1 =
          context->AddFixedPoint(primary_source, secondary_source, name);
      std::pair<FixedPoints::iterator, bool> fixed_point_iterator_2 =
          context->AddFixedPoint(primary_target, secondary_target, name);
      fixed_points_discovered |= AddFixedPointEdge(
          fixed_point_iterator_1, context, name, default_steps);
      fixed_points_discovered |= AddFixedPointEdge(
          fixed_point_iterator_2, context, name, default_steps);
    } else if (count1 >= 1 && count2 >= 1 && !matching_steps->empty()) {
      // Ambiguous match: Continue with next matching step.
      MatchingStep* step = matching_steps->front();
      FlowGraphs flow_graphs_1, flow_graphs_2;
      if (GetFlowGraphs(context->primary_call_graph_, edges1_it, next_edge1_it,
                        &flow_graphs_1) &&
          GetFlowGraphs(context->secondary_call_graph_, edges2_it,
                        next_edge2_it, &flow_graphs_2)) {
        fixed_points_discovered |= step->FindFixedPoints(
            primary_parent, secondary_parent, flow_graphs_1, flow_graphs_2,
            *context, *matching_steps, default_steps);
        matching_steps->push_front(step);
      }
    }
    edges1_it = next_edge1_it;
    edges2_it = next_edge2_it;
  }
  return fixed_points_discovered;
}

bool CheckExtraConditions(const FlowGraph* primary, const FlowGraph* secondary,
                          const MatchingStep* step) {
  if (!step->strict_equivalence()) {
    return true;
  }

  return primary->GetMdIndex() == secondary->GetMdIndex();
}

bool FindCallReferenceFixedPoints(FixedPoint* fixed_point,
                                  MatchingContext* context,
                                  const MatchingStepsFlowGraph& default_steps) {
  bool fixed_points_discovered = false;
  const FlowGraph& primary = *fixed_point->GetPrimary();
  const FlowGraph& secondary = *fixed_point->GetSecondary();
  const BasicBlockFixedPoints& basic_block_fixed_points =
      fixed_point->GetBasicBlockFixedPoints();
  for (const auto& basic_block_fixed_point : basic_block_fixed_points) {
    std::pair<FlowGraph::CallTargets::const_iterator,
              FlowGraph::CallTargets::const_iterator>
        calls1 =
            primary.GetCallTargets(basic_block_fixed_point.GetPrimaryVertex());
    std::pair<FlowGraph::CallTargets::const_iterator,
              FlowGraph::CallTargets::const_iterator>
        calls2 = secondary.GetCallTargets(
            basic_block_fixed_point.GetSecondaryVertex());
    // Either side doesn't have any calls or number of calls differs.
    if (calls2.first == calls2.second || calls1.first == calls1.second ||
        (calls2.second - calls2.first != calls1.second - calls1.first)) {
      continue;
    }

    for (; calls1.first != calls1.second; ++calls1.first, ++calls2.first) {
      if (context->FixedPointByPrimary(*calls1.first)) {
        continue;
      }
      const CallGraph::Vertex vertex1 =
          context->primary_call_graph_.GetVertex(*calls1.first);
      const CallGraph::Vertex vertex2 =
          context->secondary_call_graph_.GetVertex(*calls2.first);
      if (vertex1 != CallGraph::kInvalidVertex &&
          vertex2 != CallGraph::kInvalidVertex) {
        FlowGraph* match1 = context->primary_call_graph_.GetFlowGraph(vertex1);
        FlowGraph* match2 =
            context->secondary_call_graph_.GetFlowGraph(vertex2);
        if (match1 && match2) {
          std::pair<FixedPoints::iterator, bool> fixed_point_iterator =
              context->AddFixedPoint(match1, match2,
                                     MatchingStep::kFunctionCallReferenceName);
          if (fixed_point_iterator.second) {
            FixedPoint& fixed_point =
                const_cast<FixedPoint&>(*fixed_point_iterator.first);
            FindFixedPointsBasicBlock(&fixed_point, context, default_steps);
            FindCallReferenceFixedPoints(&fixed_point, context, default_steps);
            UpdateFixedPointConfidence(fixed_point);
            fixed_points_discovered = true;
          }
        }
      }
    }
  }
  return fixed_points_discovered;
}

MatchingStep::MatchingStep(std::string name, std::string display_name)
    : name_(std::move(name)),
      display_name_(std::move(display_name)),
      confidence_(GetConfidenceFromConfig(name_)) {}

void BaseMatchingStepEdgesMdIndex::FeatureDestructor(
    EdgeFeatureIndex* feature) {
  delete feature;
}

bool BaseMatchingStepEdgesMdIndex::FindFixedPoints(
    const FlowGraph* primary_parent, const FlowGraph* secondary_parent,
    FlowGraphs& flow_graphs_1, FlowGraphs& flow_graphs_2,
    MatchingContext& context, MatchingSteps& matching_steps,
    const MatchingStepsFlowGraph& default_steps) {
  EdgeFeatures edges1;
  EdgeFeatures edges2;
  // Note: the if() logic here is an attempt at avoiding unnecessary
  // computation. We don't even have to collect features for the secondary
  // graph if the first set is already empty.
  if (flow_graphs_1.size() < flow_graphs_2.size()) {
    GetUnmatchedEdgesMdIndex(&context, kPrimaryCallGraph, flow_graphs_1,
                             &edges1);
    if (!edges1.empty()) {
      GetUnmatchedEdgesMdIndex(&context, kSecondaryCallGraph, flow_graphs_2,
                               &edges2);
    }
  } else {
    GetUnmatchedEdgesMdIndex(&context, kSecondaryCallGraph, flow_graphs_2,
                             &edges2);
    if (!edges2.empty()) {
      GetUnmatchedEdgesMdIndex(&context, kPrimaryCallGraph, flow_graphs_1,
                               &edges1);
    }
  }
  return ::security::bindiff::FindFixedPointsEdge(
      primary_parent, secondary_parent, &edges1, &edges2, &context,
      &matching_steps, default_steps);
}

void BaseMatchingStepEdgesMdIndex::GetUnmatchedEdgesMdIndex(
    MatchingContext* context, CallGraphType type, const FlowGraphs& flow_graphs,
    EdgeFeatures* edges) {
  CHECK(edges->empty());
  bool is_primary = type == kPrimaryCallGraph;
  const CallGraph& call_graph = is_primary ? context->primary_call_graph_
                                           : context->secondary_call_graph_;
  // We go to great lengths to not keep any writeable data inside the step
  // class, instead relying on matching context (see MatchingStep
  // declaration), as step is used by multiple threads and must be reentrant.
  MatchingContext::FeatureId feature_id =
      is_primary ? primary_feature_ : secondary_feature_;
  EdgeFeatureIndex* cached =
      context->HasCachedFeatures(feature_id)
          ? context->GetCachedFeatures<EdgeFeatureIndex*>(feature_id)
          : nullptr;
  if (cached) {
    FilterResults(*cached, call_graph, flow_graphs, edges);
    return;
  }
  CallGraph::EdgeIterator edge;
  CallGraph::EdgeIterator end;
  auto index = std::make_unique<EdgeFeatureIndex>();
  EdgeFeatures* edge_features = &index->features;
  for (auto [edge, end] = boost::edges(call_graph.GetGraph()); edge != end;
       ++edge) {
    // TODO(cblichmann): Refactor. There is a (near) identical copy of the
    //                   candidate selection logic in
    //                   GetUnmatchedEdgesCallGraphMdIndex() and
    //                   GetUnmatchedEdgesProximityMdIndex().

    if (call_graph.IsDuplicate(*edge) || call_graph.IsCircular(*edge)) {
      continue;
    }

    FlowGraph* source =
        call_graph.GetFlowGraph(boost::source(*edge, call_graph.GetGraph()));
    if (!source || source->GetMdIndex() == 0.0) {
      continue;
    }

    FlowGraph* target =
        call_graph.GetFlowGraph(boost::target(*edge, call_graph.GetGraph()));
    if (!target || target->GetMdIndex() == 0.0) {
      continue;
    }
    edge_features->push_back(
        MakeEdgeFeature(*edge, call_graph, source, target));
  }
  BuildIncidence(call_graph, index.get());
  FilterResults(*index, call_graph, flow_graphs, edges);
  if (context->HasCachedFeatures(feature_id)) {
    context->SetCachedFeatures(feature_id, index.release(), FeatureDestructor);
  }
}

// This functions takes all features and returns only currently relevant ones.
void BaseMatchingStepEdgesMdIndex::FilterResults(
    const EdgeFeatureIndex& index, const CallGraph& call_graph,
    const FlowGraphs& flow_graphs, EdgeFeatures* edges) {
  const EdgeFeatures& all_features = index.features;
  const auto relevant = [&call_graph](const EdgeFeature& edge_feature) {
    const CallGraph::Edge& edge = edge_feature.edge;
    const FlowGraph* source =
        call_graph.GetFlowGraph(boost::source(edge, call_graph.GetGraph()));
    const FlowGraph* target =
        call_graph.GetFlowGraph(boost::target(edge, call_graph.GetGraph()));
    // Already a fixed point, no need to evaluate again.
    return source->GetFixedPoint() == 0 || target->GetFixedPoint() == 0;
  };

  // Diff() calls this for the callers and callees of every fixed point, so
  // scanning all features each time is O(functions * calls) overall. Only look
  // at the features incident to the requested flow graphs, unless that would
  // be more work than the full scan.
  const size_t num_vertices =
      index.offsets.empty() ? 0 : index.offsets.size() - 1;
  size_t num_incident = 0;
  for (const FlowGraph* flow_graph : flow_graphs) {
    const CallGraph::Vertex vertex = flow_graph->GetCallGraphVertex();
    if (vertex < num_vertices) {
      num_incident += index.offsets[vertex + 1] - index.offsets[vertex];
    }
  }
  if (num_incident < all_features.size()) {
    std::vector<uint32_t> candidates;
    candidates.reserve(num_incident);
    for (const FlowGraph* flow_graph : flow_graphs) {
      const CallGraph::Vertex vertex = flow_graph->GetCallGraphVertex();
      if (vertex < num_vertices) {
        candidates.insert(candidates.end(),
                          index.incident.begin() + index.offsets[vertex],
                          index.incident.begin() + index.offsets[vertex + 1]);
      }
    }
    // Edges between two requested flow graphs show up twice. Sorting also
    // restores the original feature order.
    std::sort(candidates.begin(), candidates.end());
    candidates.erase(std::unique(candidates.begin(), candidates.end()),
                     candidates.end());
    for (const uint32_t candidate : candidates) {
      const EdgeFeature& edge_feature = all_features[candidate];
      if (relevant(edge_feature)) {
        edges->push_back(edge_feature);
      }
    }
    return;
  }

  for (const auto& edge_feature : all_features) {
    if (!relevant(edge_feature)) {
      continue;
    }
    const CallGraph::Edge& edge = edge_feature.edge;
    FlowGraph* source =
        call_graph.GetFlowGraph(boost::source(edge, call_graph.GetGraph()));
    FlowGraph* target =
        call_graph.GetFlowGraph(boost::target(edge, call_graph.GetGraph()));
    // TODO(cblichmann): Understand why this condition got here in the first
    //                   place. It is expensive to calculate and at least on the
    //                   libssl sample _reduces_ result quality instead of
    //                   increasing it.
    if (flow_graphs.count(target) == 0 && flow_graphs.count(source) == 0) {
      continue;
    }

    edges->push_back(edge_feature);
  }
}

void BaseMatchingStepEdgesMdIndex::BuildIncidence(const CallGraph& call_graph,
                                                  EdgeFeatureIndex* index) {
  const auto& graph = call_graph.GetGraph();
  const size_t num_vertices = boost::num_vertices(graph);
  const EdgeFeatures& features = index->features;
  CHECK(features.size() < std::numeric_limits<uint32_t>::max());

  std::vector<uint32_t> degree(num_vertices + 1, 0);
  for (const auto& feature : features) {
    const CallGraph::Vertex source = boost::source(feature.edge, graph);
    const CallGraph::Vertex target = boost::target(feature.edge, graph);
    ++degree[source];
    if (target != source) {
      ++degree[target];
    }
  }
  index->offsets.assign(num_vertices + 1, 0);
  for (size_t vertex = 0; vertex < num_vertices; ++vertex) {
    index->offsets[vertex + 1] = index->offsets[vertex] + degree[vertex];
  }
  index->incident.resize(index->offsets[num_vertices]);
  std::vector<uint32_t> next(index->offsets.begin(), index->offsets.end() - 1);
  // Filled in feature order, so each vertex's list is sorted.
  for (uint32_t i = 0; i < features.size(); ++i) {
    const CallGraph::Vertex source = boost::source(features[i].edge, graph);
    const CallGraph::Vertex target = boost::target(features[i].edge, graph);
    index->incident[next[source]++] = i;
    if (target != source) {
      index->incident[next[target]++] = i;
    }
  }
}

MatchingSteps GetDefaultMatchingSteps() {
  static const auto* algorithms =
      []() -> absl::flat_hash_map<std::string, MatchingStep*>* {
    auto* algorithms = new absl::flat_hash_map<std::string, MatchingStep*>;
    for (auto* step : std::initializer_list<MatchingStep*>{
             // Edge based matching algorithms:
             new MatchingStepEdgesFlowGraphMdIndex(),
             new MatchingStepEdgesCallGraphMdIndex(),
             new MatchingStepEdgesProximityMdIndex(),
             // Node based matching algorithms:
             new MatchingStepCallGraphMdIndex(kTopDown),
             new MatchingStepCallGraphMdIndex(kBottomUp),
             new MatchingStepFlowGraphMdIndex(kTopDown),
             new MatchingStepFlowGraphMdIndex(kBottomUp),
             new MatchingStepCallGraphMdIndexRelaxed(),
             new MatchingStepName(),
             new MatchingStepLoops(),
             new MatchingStepCallSequence(MatchingStepCallSequence::EXACT),
             new MatchingStepCallSequence(MatchingStepCallSequence::TOPOLOGY),
             new MatchingStepCallSequence(MatchingStepCallSequence::SEQUENCE),
             new MatchingStepPrime(),
             new MatchingStepHash(),
             new MatchingStepFunctionStringReferences(),
             new MatchingStepFunctionInstructionCount(),
             new MatchingStepSequence(),
         }) {
      (*algorithms)[step->name()] = step;
    }
    return algorithms;
  }();

  static const auto* matching_steps = []() -> const MatchingSteps* {
    auto* matching_steps = new MatchingSteps();
    for (const auto& step : config::Proto().function_matching()) {
      if (auto found = algorithms->find(step.name());
          found != algorithms->end()) {
        matching_steps->push_back(found->second);
      }
    }
    LOG_IF(FATAL, matching_steps->empty())
        << "No function matching algorithms registered";
    return matching_steps;
  }();

  return *matching_steps;
}

}  // namespace security::bindiff
