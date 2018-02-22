#ifndef FIXED_POINTS_H_
#define FIXED_POINTS_H_

#include <set>
#include <string>

#include "third_party/zynamics/bindiff/change_classifier.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/instruction.h"
#include "third_party/zynamics/bindiff/utility.h"
#include "third_party/zynamics/binexport/types.h"

class MatchingContext;

class BasicBlockFixedPoint {
 public:
  BasicBlockFixedPoint(FlowGraph* primary,
                       FlowGraph::Vertex primary_basic_block,
                       FlowGraph* secondary,
                       FlowGraph::Vertex secondary_basic_block,
                       const string& matching_step);
  FlowGraph::Vertex GetPrimaryVertex() const;
  FlowGraph::Vertex GetSecondaryVertex() const;
  const string& GetMatchingStep() const;
  void SetMatchingStep(const string& matching_step);
  const InstructionMatches& GetInstructionMatches() const;
  InstructionMatches& GetInstructionMatches();

 private:
  const string* matching_step_;
  FlowGraph::Vertex primary_vertex_;
  FlowGraph::Vertex secondary_vertex_;
  InstructionMatches instruction_matches_;
};

bool operator<(const BasicBlockFixedPoint& one,
               const BasicBlockFixedPoint& two);

typedef std::set<BasicBlockFixedPoint> BasicBlockFixedPoints;

class FixedPoint {
 public:
  FixedPoint(const FixedPoint&);
  const FixedPoint& operator=(const FixedPoint&);
  explicit FixedPoint(FlowGraph* primary = 0, FlowGraph* secondary = 0,
                      const string& matching_step = "");
  void Create(FlowGraph* primary, FlowGraph* secondary);
  FlowGraph* GetPrimary() const;
  FlowGraph* GetSecondary() const;
  const string& GetMatchingStep() const;
  void SetMatchingStep(const string& matching_step);
  BasicBlockFixedPoints::iterator Add(FlowGraph::Vertex primary_vertex,
                                      FlowGraph::Vertex secondary_vertex,
                                      const string& step_name);
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

  const string* matching_step_;
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

typedef std::set<FixedPoint> FixedPoints;
typedef std::set<FixedPoint*, FixedPointComparator> FixedPointRefs;

const string* FindString(const string& name);

#endif  // FIXED_POINTS_H_
