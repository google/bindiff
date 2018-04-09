#ifndef FLOW_GRAPH_MATCH_H_
#define FLOW_GRAPH_MATCH_H_

#include <map>
#include <set>
#include <string>

#include "third_party/zynamics/bindiff/match_context.h"
#include "third_party/zynamics/binexport/types.h"

namespace security {
namespace bindiff {

using VertexSet = std::set<FlowGraph::Vertex>;
using VertexDoubleMap = std::multimap<double, FlowGraph::Vertex>;
using VertexIntMap = std::multimap<uint64_t, FlowGraph::Vertex>;
using EdgeDoubleMap = std::multimap<double, FlowGraph::Edge>;
using EdgeIntMap = std::multimap<uint64_t, FlowGraph::Edge>;

class MatchingStepFlowGraph {
 public:
  explicit MatchingStepFlowGraph(const string& name);
  virtual ~MatchingStepFlowGraph() = default;

  virtual bool FindFixedPoints(FlowGraph* primary, FlowGraph* secondary,
                               const VertexSet& vertices1,
                               const VertexSet& vertices2,
                               FixedPoint* fixed_point,
                               MatchingContext* context,
                               MatchingStepsFlowGraph* matching_steps) = 0;

  const string& GetName() const { return name_; }

  double GetConfidence() const { return confidence_; }

  bool IsEdgeMatching() const { return edge_matching_; }

 protected:
  string name_;
  double confidence_;
  bool edge_matching_;
};

MatchingStepsFlowGraph GetDefaultMatchingStepsBasicBlock();
void FindFixedPointsBasicBlock(FixedPoint* fixed_point,
                               MatchingContext* context,
                               const MatchingStepsFlowGraph& default_steps);

template <typename VertexMap>
bool FindFixedPointsBasicBlockInternal(FlowGraph* primary, FlowGraph* secondary,
                                       VertexMap* vertices_1,
                                       VertexMap* vertices_2,
                                       FixedPoint* fixed_point,
                                       MatchingContext* context,
                                       MatchingStepsFlowGraph* matching_steps) {
  const string name = matching_steps->front()->GetName();
  matching_steps->pop_front();

  bool fix_points_discovered = false;
  for (typename VertexMap::const_iterator i = vertices_1->begin();
       i != vertices_1->end();) {
    const size_t count1 = vertices_1->count(i->first);
    const size_t count2 = vertices_2->count(i->first);
    if (count1 == 0 || count2 == 0) {
      ++i;
      continue;
    }

    if (count1 > 1 || count2 > 1) {
      const typename VertexMap::key_type key = i->first;

      // Continue with next matching step.
      if (!matching_steps->empty()) {
        MatchingStepFlowGraph* step = matching_steps->front();
        VertexSet basic_blocks_1, basic_blocks_2;
        for (auto range = vertices_1->equal_range(key);
             range.first != range.second; ++range.first) {
          basic_blocks_1.emplace(range.first->second);
        }
        for (auto range = vertices_2->equal_range(key);
             range.first != range.second; ++range.first) {
          basic_blocks_2.emplace(range.first->second);
        }
        if (!basic_blocks_1.empty() && !basic_blocks_2.empty()) {
          fix_points_discovered |= step->FindFixedPoints(
              primary, secondary, basic_blocks_1, basic_blocks_2, fixed_point,
              context, matching_steps);
          matching_steps->push_front(step);
        }
      }
      vertices_1->erase(key);
      vertices_2->erase(key);
      i = vertices_1->upper_bound(key);
      continue;
    }

    if (fixed_point->Add(i->second, vertices_2->find(i->first)->second, name) ==
        fixed_point->GetBasicBlockFixedPoints().end()) {
      ++i;
      continue;
    }

    ++i;
    fix_points_discovered = true;
  }

  return fix_points_discovered;
}

template <typename EdgeMap>
void GetVertices(const FlowGraph& flow_graph, const EdgeMap& edges,
                 const typename EdgeMap::key_type key,
                 VertexSet* basic_blocks) {
  for (auto range = edges.equal_range(key); range.first != range.second;
       ++range.first) {
    const auto& edge = range.first->second;
    const auto source = boost::source(edge, flow_graph.GetGraph());
    if (!flow_graph.GetFixedPoint(source)) {
      basic_blocks->emplace(source);
    }
    const auto target = boost::target(edge, flow_graph.GetGraph());
    if (!flow_graph.GetFixedPoint(target)) {
      basic_blocks->emplace(target);
    }
  }
}

void AddFlag(FlowGraph* flow_graph, const FlowGraph::Edge& edge, size_t flag);

template <typename EdgeMap>
bool FindFixedPointsBasicBlockEdgeInternal(
    EdgeMap* edges1, EdgeMap* edges2, FlowGraph* flow_graph1,
    FlowGraph* flow_graph2, FixedPoint* fixed_point, MatchingContext* context,
    MatchingStepsFlowGraph* matching_steps) {
  const string name = matching_steps->front()->GetName();
  matching_steps->pop_front();
  const size_t step_index = matching_steps->size();

  bool fixed_points_discovered = false;
  for (typename EdgeMap::const_iterator i = edges1->begin();
       i != edges1->end();) {
    const typename EdgeMap::key_type key = i->first;
    const size_t count1 = edges1->count(key);
    const size_t count2 = edges2->count(key);
    if (count1 != 1 || count2 != 1) {
      if (count1 >= 1 && count2 >= 1) {
        // Mark basic block equivalence for this matching step.
        for (auto range = edges1->equal_range(key); range.first != range.second;
             ++range.first) {
          AddFlag(flow_graph1, range.first->second, 1 << step_index);
        }
        for (auto range = edges2->equal_range(key); range.first != range.second;
             ++range.first) {
          AddFlag(flow_graph2, range.first->second, 1 << step_index);
        }

        // Continue with next matching step.
        if (!matching_steps->empty()) {
          MatchingStepFlowGraph* step = matching_steps->front();
          VertexSet basic_blocks_1, basic_blocks_2;
          GetVertices(*flow_graph1, *edges1, key, &basic_blocks_1);
          GetVertices(*flow_graph2, *edges2, key, &basic_blocks_2);
          if (!basic_blocks_1.empty() && !basic_blocks_2.empty()) {
            fixed_points_discovered |= step->FindFixedPoints(
                flow_graph1, flow_graph2, basic_blocks_1, basic_blocks_2,
                fixed_point, context, matching_steps);
            matching_steps->push_front(step);
          }
        }
      }

      edges1->erase(key);
      edges2->erase(key);
      i = edges1->upper_bound(key);
      continue;
    }

    FlowGraph::Edge edge1 = i->second;
    FlowGraph::Edge edge2 = edges2->find(key)->second;
    if (fixed_point->Add(boost::source(edge1, flow_graph1->GetGraph()),
                         boost::source(edge2, flow_graph2->GetGraph()), name) !=
            fixed_point->GetBasicBlockFixedPoints().end() ||
        fixed_point->Add(boost::target(edge1, flow_graph1->GetGraph()),
                         boost::target(edge2, flow_graph2->GetGraph()), name) !=
            fixed_point->GetBasicBlockFixedPoints().end()) {
      fixed_points_discovered = true;
    }
    ++i;
  }
  return fixed_points_discovered;
}

}  // namespace bindiff
}  // namespace security

#endif  // FLOW_GRAPH_MATCH_H_
