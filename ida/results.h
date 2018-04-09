#ifndef IDA_RESULTS_H_
#define IDA_RESULTS_H_

#include <string>
#include <vector>

#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT

#include "base/integral_types.h"
#include "third_party/zynamics/bindiff/call_graph.h"
#include "third_party/zynamics/bindiff/database_writer.h"
#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/instruction.h"
#include "third_party/zynamics/bindiff/reader.h"
#include "third_party/zynamics/bindiff/writer.h"
#include "third_party/zynamics/binexport/types.h"

namespace security {
namespace bindiff {

#ifndef __EA64__
#define HEX_ADDRESS "%08X"
#else
#define HEX_ADDRESS "%016llX"
#endif

class Results {
 public:
  struct StatisticDescription {
    string name;
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
    string name_primary;
    Address address_secondary;
    string name_secondary;
    bool comments_ported;
    string algorithm_name;
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
    string name;
    int basic_block_count;
    int instruction_count;
    int edge_count;
  };

  Results();
  ~Results();

  size_t GetNumUnmatchedPrimary() const;
  UnmatchedDescription GetUnmatchedDescriptionPrimary(size_t index) const;

  size_t GetNumUnmatchedSecondary() const;
  UnmatchedDescription GetUnmatchedDescriptionSecondary(size_t index) const;

  Address GetPrimaryAddress(size_t index) const;
  Address GetSecondaryAddress(size_t index) const;
  Address GetMatchPrimaryAddress(size_t index) const;

  int DeleteMatch(size_t index);
  int AddMatchPrimary(size_t index);
  int AddMatchSecondary(size_t index);
  int AddMatch(Address primary, Address secondary);

  size_t GetNumStatistics() const;
  StatisticDescription GetStatisticDescription(size_t index) const;

  size_t GetNumMatches() const;
  MatchDescription GetMatchDescription(int index) const;

  bool PrepareVisualDiff(size_t index, string* message);
  bool PrepareVisualCallGraphDiff(size_t index, string* message);
  void Read(Reader* reader);
  void Write(Writer* writer);
  void WriteFromIncompleteResults();
  void CreateIndexedViews();
  int CopyPrimaryAddress(int index) const;
  int CopySecondaryAddress(int index) const;
  int CopyPrimaryAddressUnmatched(int index) const;
  int CopySecondaryAddressUnmatched(int index) const;
  int ConfirmMatch(int index);
  int PortComments(int index, bool as_external);
  int PortComments(Address start_address_source, Address end_address_source,
                   Address start_address_target, Address end_address_target,
                   double min_confidence, double min_similarity);
  bool IsInComplete() const;
  void SetDirty();
  bool IsDirty() const;
  bool IncrementalDiff();
  void MarkPortedCommentsInDatabase();

  static void DeleteTemporaryFiles();

  CallGraph call_graph1_;
  CallGraph call_graph2_;
  string input_filename_;
  Instruction::Cache instruction_cache_;
  FixedPointInfos fixed_point_infos_;
  FlowGraphInfos flow_graph_infos1_;
  FlowGraphInfos flow_graph_infos2_;

 private:
  friend bool Diff(ea_t, ea_t, ea_t, ea_t);

  using IndexedFlowGraphs = std::vector<FlowGraphInfo*>;
  using IndexedFixedPoints = std::vector<FixedPointInfo*>;

  UnmatchedDescription GetUnmatchedDescription(
      const IndexedFlowGraphs& flow_graphs, size_t index) const;
  void InitializeIndexedVectors();
  void Count();
  void SetupTemporaryFlowGraphs(
      const FixedPointInfo& fixed_point_info,
      FlowGraph& primary,       // NOLINT(runtime/references)
      FlowGraph& secondary,     // NOLINT(runtime/references)
      FixedPoint& fixed_point,  // NOLINT(runtime/references)
      bool create_instruction_matches);
  void DeleteTemporaryFlowGraphs();
  FixedPoint* FindFixedPoint(const FixedPointInfo& info);
  void ReadBasicblockMatches(FixedPoint* fixed_point);
  void MarkPortedCommentsInTempDatabase();

  DatabaseWriter temp_database_;
  bool incomplete_results_;

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
  double similarity_;
  double confidence_;
  bool dirty_;
  int diff_database_id_;
};

}  // namespace bindiff
}  // namespace security

#endif  // IDA_RESULTS_H_
