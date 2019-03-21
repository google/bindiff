#include "third_party/zynamics/bindiff/fixed_points.h"

#include "base/logging.h"
#include "third_party/zynamics/bindiff/call_graph_match.h"
#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/match_context.h"

namespace security {
namespace bindiff {

std::vector<std::string> InitStringPool() {
  std::vector<std::string> pool;
  try {
    Confidences confidences;
    Histogram histogram;
    GetConfidence(histogram, &confidences);
    pool.reserve(confidences.size());
    for (auto i = confidences.cbegin(), end = confidences.cend(); i != end;
         ++i) {
      pool.push_back(i->first);
    }
  } catch (...) {
    // May throw if config file is missing.
  }

  pool.push_back(MatchingStep::kFunctionManualName);
  std::sort(pool.begin(), pool.end());
  return pool;
}

const std::string* FindString(const std::string& name) {
  static const std::string kStringPoolEmptyString;
  static const std::vector<std::string> string_pool(InitStringPool());
  auto i = std::lower_bound(string_pool.begin(), string_pool.end(), name);
  if (i != string_pool.end() && *i == name) {
    return &(*i);
  }
  return &kStringPoolEmptyString;
}

BasicBlockFixedPoint::BasicBlockFixedPoint(
    FlowGraph* primary, FlowGraph::Vertex primary_basic_block,
    FlowGraph* secondary, FlowGraph::Vertex secondary_basic_block,
    const std::string& matching_step)
    : matching_step_(matching_step.empty() ? 0 : FindString(matching_step)),
      primary_vertex_(primary_basic_block),
      secondary_vertex_(secondary_basic_block),
      instruction_matches_() {
  // matching_step is empty if we are loading results. We do not want to compute
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
      secondary_(secondary),
      basic_block_fixed_points_(),
      confidence_(0),
      similarity_(0),
      flags_(CHANGE_NONE),
      comments_ported_(false) {}

FixedPoint::FixedPoint(const FixedPoint& other)
    : matching_step_(other.matching_step_),
      primary_(other.primary_),
      secondary_(other.secondary_),
      basic_block_fixed_points_(other.basic_block_fixed_points_),
      confidence_(other.confidence_),
      similarity_(other.similarity_),
      flags_(other.flags_),
      comments_ported_(other.comments_ported_) {}

const FixedPoint& FixedPoint::operator=(const FixedPoint& other) {
  FixedPoint(other).swap(*this);
  return *this;
}

void FixedPoint::swap(FixedPoint& other) throw() {
  std::swap(matching_step_, other.matching_step_);
  std::swap(primary_, other.primary_);
  std::swap(secondary_, other.secondary_);
  std::swap(basic_block_fixed_points_, other.basic_block_fixed_points_);
  std::swap(confidence_, other.confidence_);
  std::swap(similarity_, other.similarity_);
  std::swap(flags_, other.flags_);
  std::swap(comments_ported_, other.comments_ported_);
}

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

}  // namespace bindiff
}  // namespace security
