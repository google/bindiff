#ifndef CALL_GRAPH_MATCH_H_
#define CALL_GRAPH_MATCH_H_

#include "base/logging.h"
#include "third_party/zynamics/bindiff/flow_graph_match.h"
#include "third_party/zynamics/bindiff/match_context.h"
#include "third_party/zynamics/binexport/types.h"

MatchingSteps GetDefaultMatchingSteps();
bool FindCallReferenceFixedPoints(FixedPoint* fixed_point,
                                  MatchingContext* context,
                                  const MatchingStepsFlowGraph& default_steps);

// This class implements a single step in BinDiff's matching scheme. Each
// deriving class gets called to find yet-unknown fixed points (new associations
// between the two binaries). The name is a bit of a misnomer, since all steps
// based on MatchingStep really only work on a per-function level.
// Note: Since adding new fixed points may lead to discovery of addtional fixed
//       points, the methods of this class must be reentrant. I.e. if execution
//       of FindFixedPoints() in turn calls global ::FindFixedPoints(), it may
//       be called by the later in a mutually recursive way. Hence, deriving
//       classes should not depend on global mutable state.
class MatchingStep {
 public:
  explicit MatchingStep(const string& name);
  virtual ~MatchingStep() = default;

  // Tries to discover additional matches in the two binaries described by their
  // call and flow graphs.
  virtual bool FindFixedPoints(const FlowGraph* primary_parent,
                               const FlowGraph* secondary_parent,
                               FlowGraphs& primary_flow_graphs,
                               FlowGraphs& secondary_flow_graphs,
                               MatchingContext& context,
                               MatchingSteps& matching_steps,
                               const MatchingStepsFlowGraph& default_steps) = 0;

  const string& GetName() const { return name_; }

  double GetConfidence() const { return confidence_; }

  bool NeedsStrictEquivalence() const { return strict_equivalence_; }

 protected:
  string name_;
  double confidence_;
  bool strict_equivalence_;
};

struct EdgeFeature {
  CallGraph::Edge edge;
  double source_value;
  double target_value;
};

using EdgeFeatures = std::vector<EdgeFeature>;

// A MatchingStep class that serves as the base class for all edge-based
// matching steps that use the MD Index. It takes care of attaching EdgeFeature
// as attributes to the call graph edges. These features will be cached
// internally, so the individual matching steps do not need to store additional
// data.
class BaseMatchingStepEdgesMdIndex : public MatchingStep {
 public:
  BaseMatchingStepEdgesMdIndex(const char* name,
                               MatchingContext::FeatureId primary_feature,
                               MatchingContext::FeatureId secondary_feature)
      : MatchingStep(name),
        primary_feature_(primary_feature),
        secondary_feature_(secondary_feature) {}

  bool FindFixedPoints(const FlowGraph* primary_parent,
                       const FlowGraph* secondary_parent,
                       FlowGraphs& flow_graphs_1, FlowGraphs& flow_graphs_2,
                       MatchingContext& context, MatchingSteps& matching_steps,
                       const MatchingStepsFlowGraph& default_steps) override;

 protected:
  virtual EdgeFeature MakeEdgeFeature(CallGraph::Edge edge,
                                      const CallGraph& call_graph,
                                      FlowGraph* source, FlowGraph* target) = 0;

 private:
  enum CallGraphType { kPrimaryCallGraph, kSecondaryCallGraph };

  static void FeatureDestructor(EdgeFeatures* feature);

  void GetUnmatchedEdgesMdIndex(MatchingContext* context, CallGraphType type,
                                const FlowGraphs& flow_graphs,
                                EdgeFeatures* edges);

  // This functions takes all features and returns only currently relevant ones.
  void FilterResults(const EdgeFeatures& all_features,
                     const CallGraph& call_graph, const FlowGraphs& flow_graphs,
                     EdgeFeatures* edges);

  MatchingContext::FeatureId primary_feature_;
  MatchingContext::FeatureId secondary_feature_;
};


using FlowGraphDoubleMap = std::multimap<double, FlowGraph*, std::less<double>>;
using FlowGraphIntMap = std::multimap<uint64_t, FlowGraph*>;

bool IsValidCandidate(const FlowGraph* flow_graph);
bool CheckExtraConditions(const FlowGraph* primary, const FlowGraph* secondary,
                          const MatchingStep* step);

template <typename TFlowGraphMap>
bool FindFixedPoints(const FlowGraph* primary_parent,
                     const FlowGraph* secondary_parent,
                     TFlowGraphMap& flow_graphs_map_1,
                     TFlowGraphMap& flow_graphs_map_2, MatchingContext* context,
                     MatchingSteps& matching_steps,
                     const MatchingStepsFlowGraph& default_steps) {
  const MatchingStep* step = matching_steps.front();
  matching_steps.pop_front();

  bool fix_points_discovered = false;
  for (auto primary_feature = flow_graphs_map_1.cbegin();
       primary_feature != flow_graphs_map_1.cend();) {
    const typename TFlowGraphMap::key_type key = primary_feature->first;
    auto range_1 = flow_graphs_map_1.equal_range(key);
    auto range_2 = flow_graphs_map_2.equal_range(key);
    if (range_1.first == range_1.second || range_2.first == range_2.second) {
      ++primary_feature;
      continue;
    }

    // std::distance is linear in the number of elements in the range for
    // multimaps. We are only interested if either one of the feature sets has
    // more than one entry, so we test manually.
    auto next_1 = range_1.first;
    ++next_1;
    auto next_2 = range_2.first;
    ++next_2;
    if (next_1 != range_1.second || next_2 != range_2.second) {
      if (!matching_steps.empty()) {
        MatchingStep* drill_down_step = matching_steps.front();
        FlowGraphs flow_graphs_1;
        for (; range_1.first != range_1.second; ++range_1.first) {
          CHECK(flow_graphs_1.emplace(range_1.first->second).second);
        }
        FlowGraphs flow_graphs_2;
        for (; range_2.first != range_2.second; ++range_2.first) {
          CHECK(flow_graphs_2.emplace(range_2.first->second).second);
        }
        fix_points_discovered |= drill_down_step->FindFixedPoints(
            primary_parent, secondary_parent, flow_graphs_1, flow_graphs_2,
            *context, matching_steps, default_steps);
        matching_steps.push_front(drill_down_step);
      }
      flow_graphs_map_1.erase(key);
      flow_graphs_map_2.erase(key);
      primary_feature = flow_graphs_map_1.upper_bound(key);
      continue;
    }
    // At this point we know the feature to be unique on either side.
    FlowGraph* primary = range_1.first->second;
    FlowGraph* secondary = range_2.first->second;
    if (!CheckExtraConditions(primary, secondary, step)) {
      ++primary_feature;
      continue;
    }
    std::pair<FixedPoints::iterator, bool> fixed_point_iterator =
        context->AddFixedPoint(primary, secondary, step->GetName());
    if (!fixed_point_iterator.second) {
      ++primary_feature;
      continue;
    }

    {
      FixedPoint& fixed_point =
          const_cast<FixedPoint&>(*fixed_point_iterator.first);
      FindFixedPointsBasicBlock(&fixed_point, context, default_steps);
      UpdateFixedPointConfidence(fixed_point);
    }

    ++primary_feature;
    fix_points_discovered = true;
  }
  return fix_points_discovered;
}

bool FindFixedPointsEdge(const FlowGraph* primary_parent,
                         const FlowGraph* secondary_parent,
                         EdgeFeatures* edges1, EdgeFeatures* edges2,
                         MatchingContext* context,
                         MatchingSteps* matching_steps,
                         const MatchingStepsFlowGraph& default_steps);

#endif  // CALL_GRAPH_MATCH_H_
