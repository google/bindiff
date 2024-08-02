// Copyright 2011-2024 Google LLC
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

#ifndef IDA_RESULTS_H_
#define IDA_RESULTS_H_

#include <cstddef>
#include <memory>
#include <string>
#include <vector>

// clang-format off
#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT
// clang-format on

#include "third_party/absl/status/status.h"
#include "third_party/absl/status/statusor.h"
#include "third_party/absl/types/span.h"
#include "third_party/zynamics/bindiff/call_graph.h"
#include "third_party/zynamics/bindiff/database_writer.h"
#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/bindiff/fixed_points.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/instruction.h"
#include "third_party/zynamics/bindiff/reader.h"
#include "third_party/zynamics/bindiff/statistics.h"
#include "third_party/zynamics/bindiff/writer.h"
#include "third_party/zynamics/binexport/util/types.h"

namespace security::bindiff {

class Results {
 public:
  struct StatisticDescription {
    std::string name;
    bool is_count;
    union {
      size_t count;
      double value;
    };
  };

  struct MatchDescription {
    double similarity;
    double confidence;
    ChangeType change_type;
    Address address_primary;
    std::string name_primary;
    Address address_secondary;
    std::string name_secondary;
    bool comments_ported;
    std::string algorithm_name;
    int basic_block_count;
    int basic_block_count_primary;
    int basic_block_count_secondary;
    int edge_count;
    int edge_count_primary;
    int edge_count_secondary;
    int instruction_count;
    int instruction_count_primary;
    int instruction_count_secondary;
    bool manual;
  };

  struct UnmatchedDescription {
    Address address;
    std::string name;
    int basic_block_count;
    int instruction_count;
    int edge_count;
  };

  ~Results();

  static absl::StatusOr<std::unique_ptr<Results>> Create();

  size_t GetNumUnmatchedPrimary() const;
  UnmatchedDescription GetUnmatchedDescriptionPrimary(size_t index) const;

  size_t GetNumUnmatchedSecondary() const;
  UnmatchedDescription GetUnmatchedDescriptionSecondary(size_t index) const;

  Address GetPrimaryAddress(size_t index) const;
  Address GetSecondaryAddress(size_t index) const;
  Address GetMatchPrimaryAddress(size_t index) const;
  Address GetMatchSecondaryAddress(size_t index) const;

  absl::Status DeleteMatches(absl::Span<const size_t> indices);

  // Creates a new match for the functions given by their respective addresses
  // in the primary and secondary database.
  absl::Status AddMatch(Address primary, Address secondary);

  size_t GetNumStatistics() const;
  StatisticDescription GetStatisticDescription(size_t index) const;

  size_t GetNumMatches() const;
  MatchDescription GetMatchDescription(size_t index) const;

  bool PrepareVisualDiff(size_t index, std::string* message);
  bool PrepareVisualCallGraphDiff(size_t index, std::string* message);
  void Read(Reader* reader);
  absl::Status Write(Writer* writer);
  void CreateIndexedViews();

  // Marks the matches indicated by the given indices as manually confirmed.
  // Returns an error if any of the indices are out of range. Matches already
  // marked are not reset in that case.
  absl::Status ConfirmMatches(absl::Span<const size_t> indices);

  // Imports symbols and comments from matches in other binary into the current
  // database. If specified, mark the imported symbols/comments as coming from
  // an external library.
  enum PortCommentsKind { kNormal, kAsExternalLib };
  absl::Status PortComments(absl::Span<const size_t> indices,
                            PortCommentsKind how);

  // Like the vector version of PortComments(), but instead of importing from
  // matches, imports by address ranges.
  absl::Status PortComments(Address start_address_source,
                            Address end_address_source,
                            Address start_address_target,
                            Address end_address_target, double min_confidence,
                            double min_similarity);

  bool is_incomplete() const;

  void set_modified();
  bool is_modified() const;

  absl::Status IncrementalDiff();
  void MarkPortedCommentsInDatabase();

  bool should_reset_selection() { return should_reset_selection_; }
  void set_should_reset_selection(bool value) {
    should_reset_selection_ = value;
  }

  CallGraph call_graph1_;
  CallGraph call_graph2_;
  std::string input_filename_;
  Instruction::Cache instruction_cache_;
  FixedPointInfos fixed_point_infos_;
  FlowGraphInfos flow_graph_infos1_;
  FlowGraphInfos flow_graph_infos2_;

 private:
  using IndexedFlowGraphs = std::vector<FlowGraphInfo*>;
  using IndexedFixedPoints = std::vector<FixedPointInfo*>;

  Results() = default;

  static void DeleteTemporaryFiles();

  UnmatchedDescription GetUnmatchedDescription(
      const IndexedFlowGraphs& flow_graphs, size_t index) const;
  void InitializeIndexedVectors();
  void Count();
  void SetupTemporaryFlowGraphs(const FixedPointInfo& fixed_point_info,
                                FlowGraph& primary, FlowGraph& secondary,
                                FixedPoint& fixed_point,
                                bool create_instruction_matches);
  void DeleteTemporaryFlowGraphs();
  FixedPoint* FindFixedPoint(const FixedPointInfo& info);
  void ReadBasicblockMatches(FixedPoint* fixed_point);
  void MarkPortedCommentsInTempDatabase();

  std::unique_ptr<DatabaseWriter> temp_database_;
  bool incomplete_ = false;  // Set when we have loaded from disk

 public:
  FlowGraphs flow_graphs1_;
  FlowGraphs flow_graphs2_;
  FixedPoints fixed_points_;

 private:
  IndexedFlowGraphs indexed_flow_graphs1_;
  IndexedFlowGraphs indexed_flow_graphs2_;
  IndexedFixedPoints indexed_fixed_points_;
  Histogram histogram_;
  Counts counts_;
  double similarity_ = 0.0;
  double confidence_ = 0.0;
  bool modified_ = false;

  // Whether choosers should reset their item selection
  bool should_reset_selection_ = false;
  int diff_database_id_ = 0;
};

}  // namespace security::bindiff

#endif  // IDA_RESULTS_H_
