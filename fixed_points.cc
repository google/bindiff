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

#include "third_party/zynamics/bindiff/fixed_points.h"

#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/match/call_graph.h"
#include "third_party/zynamics/bindiff/match/context.h"

namespace security::bindiff {

const std::string* FindString(const std::string& name) {
  static const auto* const kStringPoolEmptyString = new std::string();
  static const auto* const kStringPool =
      []() -> std::vector<std::string>* {
    auto* pool = new std::vector<std::string>();
    try {
      Confidences confidences;
      Histogram histogram;
      GetConfidence(histogram, &confidences);
      pool->reserve(confidences.size());
      for (const auto& [key, _] : confidences) {
        pool->push_back(key);
      }
    } catch (...) {
      // May throw if config file is missing.
    }

    pool->push_back(MatchingStep::kFunctionManualName);
    std::sort(pool->begin(), pool->end());
    return pool;
  }();

  auto it = std::lower_bound(kStringPool->begin(), kStringPool->end(), name);
  if (it != kStringPool->end() && *it == name) {
    return &(*it);
  }
  return kStringPoolEmptyString;
}

BasicBlockFixedPoint::BasicBlockFixedPoint(
    FlowGraph* primary, FlowGraph::Vertex primary_basic_block,
    FlowGraph* secondary, FlowGraph::Vertex secondary_basic_block,
    const std::string& matching_step)
    : matching_step_(matching_step.empty() ? 0 : FindString(matching_step)),
      primary_vertex_(primary_basic_block),
      secondary_vertex_(secondary_basic_block),
      instruction_matches_() {
  // Matching_step is empty if we are loading results. We do not want to compute
  // the LCS automatically in that case since we'll be loading it from disk.
  if (!matching_step.empty()) {
    auto primary_instructions = primary->GetInstructions(primary_vertex_);
    auto secondary_instructions = secondary->GetInstructions(secondary_vertex_);
    ComputeLcs(primary_instructions.first, primary_instructions.second,
               secondary_instructions.first, secondary_instructions.second,
               instruction_matches_);
  }
}

FlowGraph::Vertex BasicBlockFixedPoint::GetPrimaryVertex() const {
  return primary_vertex_;
}

FlowGraph::Vertex BasicBlockFixedPoint::GetSecondaryVertex() const {
  return secondary_vertex_;
}

void BasicBlockFixedPoint::SetMatchingStep(const std::string& matching_step) {
  matching_step_ = FindString(matching_step);
}

const std::string& BasicBlockFixedPoint::GetMatchingStep() const {
  static const std::string kEmpty;
  return matching_step_ ? *matching_step_ : kEmpty;
}

const InstructionMatches& BasicBlockFixedPoint::GetInstructionMatches() const {
  return instruction_matches_;
}

InstructionMatches& BasicBlockFixedPoint::GetInstructionMatches() {
  return instruction_matches_;
}

bool operator<(const BasicBlockFixedPoint& one,
               const BasicBlockFixedPoint& two) {
  if (one.GetPrimaryVertex() == two.GetPrimaryVertex()) {
    return one.GetSecondaryVertex() < two.GetSecondaryVertex();
  }
  return one.GetPrimaryVertex() < two.GetPrimaryVertex();
}

FixedPoint::FixedPoint(FlowGraph* primary, FlowGraph* secondary,
                       const std::string& matching_step)
    : matching_step_(matching_step.empty() ? 0 : FindString(matching_step)),
      primary_(primary),
      secondary_(secondary) {}

void FixedPoint::SetCommentsPorted(bool ported) { comments_ported_ = ported; }

bool FixedPoint::GetCommentsPorted() const { return comments_ported_; }

int FixedPoint::GetFlags() const { return flags_; }

void FixedPoint::SetFlags(int flags) { flags_ = flags; }

bool FixedPoint::HasFlag(ChangeType flag) const { return (flags_ & flag) != 0; }

void FixedPoint::SetFlag(ChangeType flag) { flags_ |= flag; }

void FixedPoint::Create(FlowGraph* primary, FlowGraph* secondary) {
  primary_ = primary;
  secondary_ = secondary;
  matching_step_ = FindString("");
}

BasicBlockFixedPoints::iterator FixedPoint::Add(
    FlowGraph::Vertex primary_vertex, FlowGraph::Vertex secondary_vertex,
    const std::string& step_name) {
  if (primary_->GetFixedPoint(primary_vertex)) {
    return basic_block_fixed_points_.end();
  }
  if (secondary_->GetFixedPoint(secondary_vertex)) {
    return basic_block_fixed_points_.end();
  }

  std::pair<BasicBlockFixedPoints::iterator, bool>
      basic_block_fixed_point_iterator = basic_block_fixed_points_.insert(
          BasicBlockFixedPoint(GetPrimary(), primary_vertex, GetSecondary(),
                               secondary_vertex, step_name));
  if (!basic_block_fixed_point_iterator.second) {
    // Already a fixed point. Not discovered above because address == 0.
    return basic_block_fixed_points_.end();
  }
  BasicBlockFixedPoint* fixed_point = const_cast<BasicBlockFixedPoint*>(
      &*basic_block_fixed_point_iterator.first);
  primary_->SetFixedPoint(primary_vertex, fixed_point);
  secondary_->SetFixedPoint(secondary_vertex, fixed_point);
  return basic_block_fixed_point_iterator.first;
}

void FixedPoint::SetConfidence(double confidence) { confidence_ = confidence; }

double FixedPoint::GetConfidence() const { return confidence_; }

void FixedPoint::SetSimilarity(double similarity) { similarity_ = similarity; }

double FixedPoint::GetSimilarity() const { return similarity_; }

FlowGraph* FixedPoint::GetPrimary() const { return primary_; }

FlowGraph* FixedPoint::GetSecondary() const { return secondary_; }

const std::string& FixedPoint::GetMatchingStep() const {
  static std::string kEmpty;
  return matching_step_ ? *matching_step_ : kEmpty;
}

void FixedPoint::SetMatchingStep(const std::string& matching_step) {
  matching_step_ = FindString(matching_step);
}

BasicBlockFixedPoints& FixedPoint::GetBasicBlockFixedPoints() {
  return basic_block_fixed_points_;
}

const BasicBlockFixedPoints& FixedPoint::GetBasicBlockFixedPoints() const {
  return basic_block_fixed_points_;
}

// It's highly important to sort by address since call sequence matching will
// produce non-deterministic results if we don't.
bool operator<(const FixedPoint& one, const FixedPoint& two) {
  if (one.GetPrimary()->GetEntryPointAddress() ==
      two.GetPrimary()->GetEntryPointAddress()) {
    return one.GetSecondary()->GetEntryPointAddress() <
           two.GetSecondary()->GetEntryPointAddress();
  }
  return one.GetPrimary()->GetEntryPointAddress() <
         two.GetPrimary()->GetEntryPointAddress();
}

}  // namespace security::bindiff
