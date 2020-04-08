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

#ifndef FIXED_POINTS_H_
#define FIXED_POINTS_H_

#include <set>
#include <string>

#include "third_party/zynamics/bindiff/change_classifier.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/instruction.h"
#include "third_party/zynamics/binexport/types.h"

namespace security::bindiff {

class MatchingContext;

class BasicBlockFixedPoint {
 public:
  BasicBlockFixedPoint(FlowGraph* primary,
                       FlowGraph::Vertex primary_basic_block,
                       FlowGraph* secondary,
                       FlowGraph::Vertex secondary_basic_block,
                       const std::string& matching_step);
  FlowGraph::Vertex GetPrimaryVertex() const;
  FlowGraph::Vertex GetSecondaryVertex() const;
  const std::string& GetMatchingStep() const;
  void SetMatchingStep(const std::string& matching_step);
  const InstructionMatches& GetInstructionMatches() const;
  InstructionMatches& GetInstructionMatches();

 private:
  const std::string* matching_step_;
  FlowGraph::Vertex primary_vertex_;
  FlowGraph::Vertex secondary_vertex_;
  InstructionMatches instruction_matches_;
};

bool operator<(const BasicBlockFixedPoint& one,
               const BasicBlockFixedPoint& two);

using BasicBlockFixedPoints = std::set<BasicBlockFixedPoint>;

class FixedPoint {
 public:
  FixedPoint(const FixedPoint&);
  const FixedPoint& operator=(const FixedPoint&);
  explicit FixedPoint(FlowGraph* primary = 0, FlowGraph* secondary = 0,
                      const std::string& matching_step = "");
  void Create(FlowGraph* primary, FlowGraph* secondary);
  FlowGraph* GetPrimary() const;
  FlowGraph* GetSecondary() const;
  const std::string& GetMatchingStep() const;
  void SetMatchingStep(const std::string& matching_step);
  BasicBlockFixedPoints::iterator Add(FlowGraph::Vertex primary_vertex,
                                      FlowGraph::Vertex secondary_vertex,
                                      const std::string& step_name);
  BasicBlockFixedPoints& GetBasicBlockFixedPoints();
  const BasicBlockFixedPoints& GetBasicBlockFixedPoints() const;
  void SetConfidence(double confidence);
  double GetConfidence() const;
  void SetSimilarity(double similarity);
  double GetSimilarity() const;
  int GetFlags() const;
  void SetFlags(int flags);
  bool HasFlag(ChangeType flag) const;
  void SetFlag(ChangeType flag);
  void SetCommentsPorted(bool ported);
  bool GetCommentsPorted() const;

 private:
  void swap(FixedPoint& other) throw();

  const std::string* matching_step_;
  FlowGraph* primary_;
  FlowGraph* secondary_;
  BasicBlockFixedPoints basic_block_fixed_points_;
  double confidence_;
  double similarity_;
  int flags_;
  bool comments_ported_;
};

bool operator<(const FixedPoint& one, const FixedPoint& two);
struct FixedPointComparator {
  bool operator()(const FixedPoint* lhs, const FixedPoint* rhs) const {
    return *lhs < *rhs;
  }
};

using FixedPoints = std::set<FixedPoint>;
using FixedPointRefs = std::set<FixedPoint*, FixedPointComparator>;

const std::string* FindString(const std::string& name);

}  // namespace security::bindiff

#endif  // FIXED_POINTS_H_
