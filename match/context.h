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

#ifndef MATCH_CONTEXT_H_
#define MATCH_CONTEXT_H_

#include <list>
#include <unordered_map>

#include "third_party/absl/strings/string_view.h"
#include "third_party/zynamics/bindiff/fixed_points.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/graph_util.h"
#include "third_party/zynamics/binexport/util/types.h"

namespace security::bindiff {

class CallGraph;
class MatchingStep;
class MatchingStepFlowGraph;

using MatchingSteps = std::list<MatchingStep*>;
using MatchingStepsFlowGraph = std::list<MatchingStepFlowGraph*>;

enum Direction {
  kTopDown,
  kBottomUp
};

void UpdateFixedPointConfidence(FixedPoint& fixed_point);

class MatchingContext {
 public:
  // MatchingContext is used as location for storing cached data for differ
  // steps (which cannot keep such data for themselves). If such a caching is
  // not desirable, such as in the IDA plugin, we may not use it.
  // Each cached resourse is identified by a FeatureId element, so that
  // different consumers will never conflict on slots assignment.
  enum FeatureId {
    kFlowGraphMdIndexPrimary = 0,
    kFlowGraphMdIndexSecondary,
    kCallGraphMdIndexPrimary,
    kCallGraphMdIndexSecondary,
    kEdgeProperies,
    kMaxFeature
  };

  MatchingContext(CallGraph& call_graph1, CallGraph& call_graph2,
                  FlowGraphs& flow_graphs1, FlowGraphs& flow_graphs2,
                  FixedPoints& fixed_points);
  ~MatchingContext();

  MatchingContext(const MatchingContext&) = delete;
  MatchingContext& operator=(const MatchingContext&) = delete;

  CallGraph& primary_call_graph_;
  CallGraph& secondary_call_graph_;
  FlowGraphs& primary_flow_graphs_;
  FlowGraphs& secondary_flow_graphs_;
  FixedPoints& fixed_points_;
  // New fixed points discovered since the last matching step.
  FixedPointRefs new_fixed_points_;

  std::pair<FixedPoints::iterator, bool> AddFixedPoint(
      FlowGraph* primary, FlowGraph* secondary, const std::string& step_name);

  FixedPoint* FixedPointByPrimary(Address entry_point) const {
    auto found = fixed_points_by_primary_.find(entry_point);
    if (found != fixed_points_by_primary_.end()) {
      return found->second;
    }
    return nullptr;
  }

  FixedPoint* FixedPointBySecondary(Address entry_point) const {
    auto found = fixed_points_by_secondary_.find(entry_point);
    if (found != fixed_points_by_secondary_.end()) {
      return found->second;
    }
    return nullptr;
  }

  bool HasCachedFeatures(FeatureId id) const {
    return id >= 0 && id < kMaxFeature;
  }

  template <typename T>
  T GetCachedFeatures(FeatureId id) const {
    assert(id >= 0 && id < kMaxFeature);
    return reinterpret_cast<T>(features_[id].features);
  }

  template <typename T>
  void SetCachedFeatures(FeatureId id, T value, void (*destructor)(T)) {
    assert(id >= 0 && id < kMaxFeature);
    if (features_[id].destructor) {
      features_[id].destructor(features_[id].features);
    }
    features_[id].features = value;
    features_[id].destructor = reinterpret_cast<FeaturesDestructor>(destructor);
  }

 private:
  using FixedPointByAddress = std::unordered_map<Address, FixedPoint*>;
  using FeaturesDestructor = void (*)(void*);

  struct FeatureRecord {
    void* features;
    FeaturesDestructor destructor;
  };

  FixedPointByAddress fixed_points_by_primary_;
  FixedPointByAddress fixed_points_by_secondary_;
  FeatureRecord features_[kMaxFeature] = {};
};

}  // namespace security::bindiff

#endif  // MATCH_CONTEXT_H_
