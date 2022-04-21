// Copyright 2011-2022 Google LLC
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

#include "third_party/zynamics/bindiff/match/context.h"

#include <string>

#include "base/logging.h"
#include "third_party/absl/base/attributes.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/bindiff/flow_graph.h"

namespace security::bindiff {

void UpdateFixedPointConfidence(FixedPoint& fixed_point) {
  FlowGraphs flow_graphs1, flow_graphs2;
  CHECK(flow_graphs1.insert(fixed_point.GetPrimary()).second);
  CHECK(flow_graphs2.insert(fixed_point.GetSecondary()).second);
  FixedPoints fixed_points;
  CHECK(fixed_points.insert(fixed_point).second);
  Histogram histogram;
  Counts counts;
  GetCountsAndHistogram(flow_graphs1, flow_graphs2, fixed_points, &histogram,
                        &counts);
  Confidences confidences;
  fixed_point.SetConfidence(GetConfidence(histogram, &confidences));
  fixed_point.SetSimilarity(GetSimilarityScore(*fixed_point.GetPrimary(),
                                               *fixed_point.GetSecondary(),
                                               histogram, counts));
}

MatchingContext::MatchingContext(CallGraph& call_graph1, CallGraph& call_graph2,
                                 FlowGraphs& flow_graphs1,
                                 FlowGraphs& flow_graphs2,
                                 FixedPoints& fixed_points)
    : primary_call_graph_(call_graph1),
      secondary_call_graph_(call_graph2),
      primary_flow_graphs_(flow_graphs1),
      secondary_flow_graphs_(flow_graphs2),
      fixed_points_(fixed_points),
      new_fixed_points_() {}

MatchingContext::~MatchingContext() {
  // Cleanup for all the cached features
  for (int i = 0; i < kMaxFeature; ++i) {
    if (features_[i].destructor) {
      features_[i].destructor(features_[i].features);
    }
  }
}

std::pair<FixedPoints::iterator, bool> MatchingContext::AddFixedPoint(
    FlowGraph* primary, FlowGraph* secondary, const std::string& step_name) {
  if (primary->GetFixedPoint() || secondary->GetFixedPoint()) {
    // already a fixed point
    return std::make_pair(fixed_points_.end(), false);
  }
  auto insert_position =
      fixed_points_.insert(FixedPoint(primary, secondary, step_name));
  if (!insert_position.second) {
    // already a fixed point
    assert(false && "inconsistent fixed point data");
    return std::make_pair(fixed_points_.end(), false);
  }
  FixedPoint* new_fixed_point = const_cast<FixedPoint*>(
      &*insert_position.first);
  fixed_points_by_primary_[
      primary->GetEntryPointAddress()] = new_fixed_point;
  fixed_points_by_secondary_[
      secondary->GetEntryPointAddress()] = new_fixed_point;
  CHECK(new_fixed_points_.insert(new_fixed_point).second);
  primary->SetFixedPoint(new_fixed_point);
  secondary->SetFixedPoint(new_fixed_point);
  return insert_position;
}

}  // namespace security::bindiff
