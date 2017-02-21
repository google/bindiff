#include <cmath>
#include <cstdint>
#include <cstdio>
#include <fstream>
#include <limits>
#include <map>
#include <memory>
#include <sstream>
#include <thread>

#include <pro.h>        // NOLINT
#include <bytes.hpp>    // NOLINT
#include <diskio.hpp>   // NOLINT
#include <enum.hpp>     // NOLINT
#include <expr.hpp>     // NOLINT
#include <frame.hpp>    // NOLINT
#include <funcs.hpp>    // NOLINT
#include <ida.hpp>      // NOLINT
#include <idp.hpp>      // NOLINT
#include <kernwin.hpp>  // NOLINT
#include <loader.hpp>   // NOLINT
#include <nalt.hpp>     // NOLINT
#include <name.hpp>     // NOLINT
#include <struct.hpp>   // NOLINT
#include <ua.hpp>       // NOLINT
#include <xref.hpp>     // NOLINT

#include "base/logging.h"
#include "base/stringprintf.h"
#include "strings/strutil.h"
#include "third_party/zynamics/bindiff/call_graph_matching.h"
#include "third_party/zynamics/bindiff/change_classifier.h"
#include "third_party/zynamics/bindiff/database_writer.h"
#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/bindiff/flow_graph_matching.h"
#include "third_party/zynamics/bindiff/fortknox_writer.h"
#include "third_party/zynamics/bindiff/ida/visual_diff.h"
#include "third_party/zynamics/bindiff/log_writer.h"
#include "third_party/zynamics/bindiff/matching.h"
#include "third_party/zynamics/bindiff/utility.h"
#include <version.h>  // NOLINT
#include "third_party/zynamics/bindiff/xmlconfig.h"
#include "third_party/zynamics/binexport/binexport2.pb.h"
#include "third_party/zynamics/binexport/filesystem_util.h"
#include "third_party/zynamics/binexport/hex_codec.h"
#include "third_party/zynamics/binexport/ida/digest.h"
#include "third_party/zynamics/binexport/ida/log.h"
#include "third_party/zynamics/binexport/ida/ui.h"
#include "third_party/zynamics/binexport/timer.h"

#include <google/protobuf/io/zero_copy_stream_impl.h>  // NOLINT
#ifdef WIN32
#define WIN32_LEAN_AND_MEAN
#define NOMINMAX
#include <windows.h>
#endif
#if _MSC_VER
#define snprintf _snprintf
#endif  // _MSC_VER

#ifndef __EA64__
#define HEX_ADDRESS "%08X"
#else
#define HEX_ADDRESS "%016llX"
#endif

static const char kBinExportVersion[] = "9";  // Exporter version to use.
static const char kName[] = "BinDiff 4.3";
static const char kComment[] =
    "Structural comparison of executable objects";  // Status line
static const char kHotKey[] = "CTRL-6";
static const char kCopyright[] =
    "(c)2004-2011 zynamics GmbH, (c)2011-2017 Google Inc.";

// Default constructed XmlConfig.
XmlConfig g_config;
bool g_init_done = false;  // Used in PluginTerminate()

enum ResultFlags {
  kResultsShowMatched = 1 << 0,
  kResultsShowStatistics = 1 << 1,
  kResultsShowPrimaryUnmatched = 1 << 2,
  kResultsShowSecondaryUnmatched = 1 << 3,
  kResultsShowAll = 0xffffffff
};

void HsvToRgb(double h, double s, double v,
              unsigned char& r,  // NOLINT(runtime/references)
              unsigned char& g,  // NOLINT(runtime/references)
              unsigned char& b   // NOLINT(runtime/references)
              ) {
  if (std::fabs(s) <=
      std::numeric_limits<double>::epsilon()) {  // achromatic (gray)
    r = g = b = static_cast<unsigned char>(v * 255.0);
    return;
  }

  h /= 60.0;
  const int i = static_cast<int>(floor(h));
  const double f = h - i;
  const double p = v * (1 - s);
  const double q = v * (1 - s * f);
  const double t = v * (1 - s * (1 - f));

  switch (i) {
    case 0:
      r = static_cast<unsigned char>(v * 255);
      g = static_cast<unsigned char>(t * 255);
      b = static_cast<unsigned char>(p * 255);
      break;
    case 1:
      r = static_cast<unsigned char>(q * 255);
      g = static_cast<unsigned char>(v * 255);
      b = static_cast<unsigned char>(p * 255);
      break;
    case 2:
      r = static_cast<unsigned char>(p * 255);
      g = static_cast<unsigned char>(v * 255);
      b = static_cast<unsigned char>(t * 255);
      break;
    case 3:
      r = static_cast<unsigned char>(p * 255);
      g = static_cast<unsigned char>(q * 255);
      b = static_cast<unsigned char>(v * 255);
      break;
    case 4:
      r = static_cast<unsigned char>(t * 255);
      g = static_cast<unsigned char>(p * 255);
      b = static_cast<unsigned char>(v * 255);
      break;
    default:  // case 5:
      r = static_cast<unsigned char>(v * 255);
      g = static_cast<unsigned char>(p * 255);
      b = static_cast<unsigned char>(q * 255);
      break;
  }
}

std::string GetArgument(const char* name) {
  const char* option = get_plugin_options(StrCat("BinDiff", name).c_str());
  return option ? option : "";
}

size_t SetComments(FixedPoint* fixed_point, const Comments& comments,
                   Address start_source, Address end_source,
                   Address start_target, Address end_target,
                   double min_confidence, double min_similarity);
bool DoSaveResults();
uint32_t idaapi GetNumUnmatchedSecondary(void* object);
void idaapi GetUnmatchedSecondaryDescription(void* object, uint32_t index,
                                             char* const* line);
uint32_t idaapi GetNumUnmatchedPrimary(void* object);
void idaapi GetUnmatchedPrimaryDescription(void* object, uint32_t index,
                                           char* const* line);

std::string GetDataForHash() {
  std::string data;
  // 32MiB maximum size for hash data.
  for (segment_t* segment = get_first_seg();
       segment != 0 && data.size() < (32 << 20);
       segment = get_next_seg(segment->startEA)) {
    // truncate segments longer than 1MB so we don't produce too long a string
    for (ea_t address = segment->startEA;
         address < std::min(segment->endEA, segment->startEA + (1 << 20));
         ++address) {
      if (getFlags(address)) {  // check whether address is loaded
        data += get_byte(address);
      }
    }
  }
  return data;
}

void CopyFile(const std::string& from, const std::string& to) {
  std::ifstream input(from.c_str(), std::ios_base::in | std::ios_base::binary);
  std::ofstream output(to.c_str(), std::ios_base::out | std::ios_base::trunc |
                                       std::ios_base::binary);
  output << input.rdbuf();
}

// Sort by: similarity desc, confidence desc, address asc.
bool SortBySimilarity(const FixedPointInfo* one, const FixedPointInfo* two) {
  CHECK(one && two);
  return one->similarity == two->similarity
             ? (one->confidence == two->confidence
                    ? one->primary < two->primary
                    : one->confidence > two->confidence)
             : one->similarity > two->similarity;
}

class Results {
 public:
  Results();
  ~Results();

  size_t GetNumFixedPoints() const;
  size_t GetNumUnmatchedPrimary() const;
  size_t GetNumUnmatchedSecondary() const;
  Address GetPrimaryAddress(size_t index) const;
  Address GetSecondaryAddress(size_t index) const;
  Address GetMatchPrimaryAddress(size_t index) const;
  void GetUnmatchedDescriptionPrimary(size_t index, char* const* line) const;
  void GetUnmatchedDescriptionSecondary(size_t index, char* const* line) const;
  size_t GetNumStatistics() const;
  int DeleteMatch(size_t index);
  int AddMatchPrimary(size_t index);
  int AddMatchSecondary(size_t index);
  int AddMatch(Address primary, Address secondary);
  void GetStatisticsDescription(size_t index, char* const* line) const;
  void GetMatchDescription(size_t index, char* const* line) const;
  bool PrepareVisualDiff(size_t index, std::string* message);
  bool PrepareVisualCallGraphDiff(size_t index, std::string* message);
  void Read(Reader& reader);
  void Write(Writer& writer);
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
  uint32_t GetColor(uint32_t index) const;
  bool IncrementalDiff();
  void MarkPortedCommentsInDatabase();

  static void DeleteTemporaryFiles();

  CallGraph call_graph1_;
  CallGraph call_graph2_;
  std::string input_filename_;
  Instruction::Cache instruction_cache_;
  FixedPointInfos fixed_point_infos_;
  FlowGraphInfos flow_graph_infos1_;
  FlowGraphInfos flow_graph_infos2_;

 private:
  friend bool Diff(ea_t, ea_t, ea_t, ea_t);

  typedef std::vector<FlowGraphInfo*> IndexedFlowGraphs;
  typedef std::vector<FixedPointInfo*> IndexedFixedPoints;

  void GetUnmatchedDescription(const IndexedFlowGraphs& flow_graphs,
                               size_t index, char* const* line) const;
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

  DatabaseWriter temp_database_;
  bool incomplete_results_;
  FlowGraphs flow_graphs1_;
  FlowGraphs flow_graphs2_;
  FixedPoints fixed_points_;

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

Results::Results()
    : temp_database_("temporary.database", true),
      incomplete_results_(false),  // Set when we have loaded from disk.
      similarity_(0.0),
      confidence_(0.0),
      dirty_(false),
      diff_database_id_(0) {}

Results::~Results() {
  // we need to close this explicitly here as otherwise the
  // DeleteTemporaryFiles() call below will fail due to locked db file
  temp_database_.Close();
  DeleteFlowGraphs(&flow_graphs1_);
  DeleteFlowGraphs(&flow_graphs2_);
  DatabaseTransmuter::DeleteTempFile();
  Results::DeleteTemporaryFiles();
}

void Results::SetDirty() { dirty_ = true; }

bool Results::IsDirty() const { return dirty_; }

void Results::DeleteTemporaryFiles() {
  // Extremely dangerous, make very sure GetDirectory _never_ returns something
  // like "C:".
  try {
    RemoveAll(GetTempDirectory("BinDiff", /* create = */ false));
  } catch (...) {  // We don't care if it failed-only litters the temp dir a bit
  }
}

uint32_t Results::GetColor(uint32_t index) const {
  if (!index || index > indexed_fixed_points_.size()) {
    return 0;
  }

  const FixedPointInfo& fixed_point = *indexed_fixed_points_[index - 1];

  if (fixed_point.IsManual()) {
    // Mark manual matches in blue.
    return (230 << 16) | (200 << 8) | 150;
  }
  // Choose hue for automatic matches according to similarity score.
  uint8_t r = 0;
  uint8_t g = 0;
  uint8_t b = 0;
  HsvToRgb(360 * 0.31 * fixed_point.similarity, 0.3, 0.9, r, g, b);
  return (b << 16) | (g << 8) | r;
}

size_t Results::GetNumFixedPoints() const {
  return indexed_fixed_points_.size();
}

size_t Results::GetNumUnmatchedPrimary() const {
  return indexed_flow_graphs1_.size();
}

size_t Results::GetNumUnmatchedSecondary() const {
  return indexed_flow_graphs2_.size();
}

Address Results::GetSecondaryAddress(size_t index) const {
  if (!index || index > indexed_flow_graphs2_.size()) {
    return 0;
  }
  return indexed_flow_graphs2_[index - 1]->address;
}

Address Results::GetPrimaryAddress(size_t index) const {
  if (!index || index > indexed_flow_graphs1_.size()) {
    return 0;
  }
  return indexed_flow_graphs1_[index - 1]->address;
}

Address Results::GetMatchPrimaryAddress(size_t index) const {
  if (!index || index > indexed_fixed_points_.size()) {
    return 0;
  }
  return indexed_fixed_points_[index - 1]->primary;
}

void Results::GetUnmatchedDescriptionPrimary(size_t index,
                                             char* const* line) const {
  GetUnmatchedDescription(indexed_flow_graphs1_, index, line);
}

void Results::GetUnmatchedDescriptionSecondary(size_t index,
                                               char* const* line) const {
  GetUnmatchedDescription(indexed_flow_graphs2_, index, line);
}

size_t Results::GetNumStatistics() const {
  return counts_.size() + histogram_.size() + 2;
}

bool Results::IncrementalDiff() {
  WaitBox wait_box("Performing incremental diff...");

  if (IsInComplete()) {
    const std::string temp_dir(
        GetTempDirectory("BinDiff", /* create = */ true));
    {
      ::Read(call_graph1_.GetFilePath(), &call_graph1_, &flow_graphs1_,
             &flow_graph_infos1_, &instruction_cache_);
      ::Read(call_graph2_.GetFilePath(), &call_graph2_, &flow_graphs2_,
             &flow_graph_infos2_, &instruction_cache_);

      CopyFile(input_filename_, JoinPath(temp_dir, "incremental.BinDiff"));

      SqliteDatabase database(
          JoinPath(temp_dir, "incremental.BinDiff").c_str());
      DatabaseTransmuter writer(database, fixed_point_infos_);
      Write(writer);

      DatabaseReader::ReadFullMatches(&database, &call_graph1_, &call_graph2_,
                                      &flow_graphs1_, &flow_graphs2_,
                                      &fixed_points_);
    }

    std::remove(JoinPath(temp_dir, "incremental.BinDiff").c_str());
    incomplete_results_ = false;
  }

  Timer<> timer;
  MatchingContext context(call_graph1_, call_graph2_, flow_graphs1_,
                          flow_graphs2_, fixed_points_);

  // try to find any confirmed fixedpoints, if we don't have any we can just ret
  bool has_confirmed_fixedpoints = false;
  for (auto i = fixed_points_.cbegin(), end = fixed_points_.cend(); i != end;
       ++i) {
    const FixedPoint& fixedpoint = *i;
    if (fixedpoint.GetMatchingStep() == "function: manual") {
      has_confirmed_fixedpoints = true;
      break;
    }
  }
  if (!has_confirmed_fixedpoints) {
    warning(
        "No manually confirmed fixedpoints found. Please add some matches "
        "or use the matched functions window context menu to confirm automatic "
        "matches before running an incremental diff");
    return false;
  }

  // Remove all non-manual matches from current result
  for (auto i = fixed_points_.begin(), end = fixed_points_.end(); i != end;) {
    FixedPoint& fixed_point = const_cast<FixedPoint&>(*i);
    FlowGraph* primary = fixed_point.GetPrimary();
    FlowGraph* secondary = fixed_point.GetSecondary();
    if (fixed_point.GetMatchingStep() == "function: manual") {
      ++i;
      continue;  // Keep confirmed fixed points.
    }
    fixed_points_.erase(i++);

    primary->ResetMatches();
    secondary->ResetMatches();
    temp_database_.DeleteFromTempDatabase(primary->GetEntryPointAddress(),
                                          secondary->GetEntryPointAddress());
  }

  // These will get refilled by ShowResults().
  indexed_flow_graphs1_.clear();
  indexed_flow_graphs2_.clear();
  indexed_fixed_points_.clear();
  histogram_.clear();
  counts_.clear();

  // Diff
  const MatchingSteps default_callgraph_steps(GetDefaultMatchingSteps());
  const MatchingStepsFlowGraph default_basicblock_steps(
      GetDefaultMatchingStepsBasicBlock());
  Diff(&context, default_callgraph_steps, default_basicblock_steps);

  // Refill fixed point info.
  fixed_point_infos_.clear();
  for (auto i = fixed_points_.cbegin(), end = fixed_points_.cend(); i != end;
       ++i) {
    FixedPointInfo info;
    const FixedPoint& fixed_point = *i;
    info.algorithm = FindString(fixed_point.GetMatchingStep());
    info.confidence = fixed_point.GetConfidence();
    info.evaluate = false;
    info.flags = fixed_point.GetFlags();
    info.primary = fixed_point.GetPrimary()->GetEntryPointAddress();
    info.secondary = fixed_point.GetSecondary()->GetEntryPointAddress();
    info.similarity = fixed_point.GetSimilarity();
    info.comments_ported = fixed_point.GetCommentsPorted();

    Counts counts;
    Histogram histogram;
    FlowGraphs dummy1;
    dummy1.insert(fixed_point.GetPrimary());
    FlowGraphs dummy2;
    dummy2.insert(fixed_point.GetSecondary());
    FixedPoints dummy3;
    dummy3.insert(fixed_point);
    GetCountsAndHistogram(dummy1, dummy2, dummy3, &histogram, &counts);
    info.basic_block_count = counts["basicBlock matches (library)"] +
                             counts["basicBlock matches (non-library)"];
    info.instruction_count = counts["instruction matches (library)"] +
                             counts["instruction matches (non-library)"];
    info.edge_count = counts["flowGraph edge matches (library)"] +
                      counts["flowGraph edge matches (non-library)"];
    fixed_point_infos_.insert(info);
  }

  LOG(INFO) << StringPrintf("%.2fs", timer.elapsed())
            << " seconds for incremental matching.";

  SetDirty();
  return true;
}

void Results::GetStatisticsDescription(size_t index, char* const* line) const {
  // The target buffers are promised to be MAXSTR == 1024 characters long.
  if (!index) {
    strcpy(line[0], "name");
    strcpy(line[1], "value");
    return;
  }

  if (index > GetNumStatistics()) {
    return;
  }
  --index;

  size_t nr = 0;
  std::string description;
  std::string value;
  if (index < counts_.size()) {
    auto i = counts_.cbegin();
    for (; i != counts_.cend() && nr < index; ++i, ++nr) {
    }
    description = i->first;
    value = std::to_string(i->second);
  } else if (index < histogram_.size() + counts_.size()) {
    index -= counts_.size();
    auto i = histogram_.cbegin();
    for (; i != histogram_.cend() && nr < index; ++i, ++nr) {
    }
    description = i->first;
    value = std::to_string(i->second);
  } else if (index == histogram_.size() + counts_.size() + 1) {
    description = "similarity";
    value = std::to_string(similarity_);
  } else {
    description = "confidence";
    value = std::to_string(confidence_);
  }

  snprintf(line[0], MAXSTR, "%s", description.c_str());
  snprintf(line[1], MAXSTR, "%s", value.c_str());
}

int Results::DeleteMatch(size_t index) {
  if (index == static_cast<size_t>(START_SEL) || !index) {
    return 1;
  }
  if (index == static_cast<size_t>(END_SEL)) {
    // Refresh GUI when operation is done.
    // refresh_chooser("Matched Functions");
    refresh_chooser("Primary Unmatched");
    refresh_chooser("Secondary Unmatched");
    refresh_chooser("Statistics");
    return 1;
  }
  --index;
  if (index >= indexed_fixed_points_.size()) {
    return 0;
  }

  // This is real nasty:
  // - recalculate statistics
  // - remove fixedpointinfo
  // - remove matching flowgraph pointer from both graphs if loaded
  // - remove fixedpoint if loaded
  // ( - recalculate similarity and confidence )
  // - update all views
  // - be prepared to save .bindiff result file (again, tricky if it wasn't
  //   loaded fully)

  const FixedPointInfo& fixed_point_info = *indexed_fixed_points_[index];

  temp_database_.DeleteFromTempDatabase(fixed_point_info.primary,
                                        fixed_point_info.secondary);

  if (call_graph2_.IsLibrary(
          call_graph2_.GetVertex(fixed_point_info.secondary)) ||
      flow_graph_infos2_.find(fixed_point_info.secondary) ==
          flow_graph_infos2_.end() ||
      call_graph1_.IsLibrary(
          call_graph1_.GetVertex(fixed_point_info.primary)) ||
      flow_graph_infos1_.find(fixed_point_info.primary) ==
          flow_graph_infos1_.end()) {
    counts_["function matches (library)"] -= 1;
    counts_["basicBlock matches (library)"] -=
        fixed_point_info.basic_block_count;
    counts_["instruction matches (library)"] -=
        fixed_point_info.instruction_count;
    counts_["flowGraph edge matches (library)"] -= fixed_point_info.edge_count;
  } else {
    counts_["function matches (non-library)"] -= 1;
    counts_["basicBlock matches (non-library)"] -=
        fixed_point_info.basic_block_count;
    counts_["instruction matches (non-library)"] -=
        fixed_point_info.instruction_count;
    counts_["flowGraph edge matches (non-library)"] -=
        fixed_point_info.edge_count;
  }
  histogram_[*fixed_point_info.algorithm]--;

  // Remove 0 entries from histogram.
  for (auto i = histogram_.begin(), end = histogram_.end(); i != end;) {
    if (!i->second) {
      histogram_.erase(i++);
    } else {
      ++i;
    }
  }

  // TODO(soerenme) tree search, this is O(n^2) when deleting all matches
  if (!IsInComplete()) {
    for (auto i = fixed_points_.cbegin(), end = fixed_points_.cend(); i != end;
         ++i) {
      const FixedPoint& fixed_point = *i;
      if (fixed_point.GetPrimary()->GetEntryPointAddress() ==
              fixed_point_info.primary &&
          fixed_point.GetSecondary()->GetEntryPointAddress() ==
              fixed_point_info.secondary) {
        FlowGraph* primary = fixed_point.GetPrimary();
        FlowGraph* secondary = fixed_point.GetSecondary();
        fixed_points_.erase(i);
        primary->ResetMatches();
        secondary->ResetMatches();
        break;
      }
    }
  }

  CHECK(flow_graph_infos1_.find(fixed_point_info.primary) !=
        flow_graph_infos1_.end());
  CHECK(flow_graph_infos2_.find(fixed_point_info.secondary) !=
        flow_graph_infos2_.end());
  FlowGraphInfo& primary(
      flow_graph_infos1_.find(fixed_point_info.primary)->second);
  FlowGraphInfo& secondary(
      flow_graph_infos2_.find(fixed_point_info.secondary)->second);
  indexed_flow_graphs1_.push_back(&primary);
  indexed_flow_graphs2_.push_back(&secondary);

  CHECK(fixed_point_infos_.find(fixed_point_info) != fixed_point_infos_.end());
  indexed_fixed_points_.erase(indexed_fixed_points_.begin() + index);

  fixed_point_infos_.erase(fixed_point_info);

  CHECK(indexed_fixed_points_.size() == fixed_point_infos_.size());
  CHECK(IsInComplete() || indexed_fixed_points_.size() == fixed_points_.size());

  SetDirty();

  return 1;
}

FlowGraph* FindGraph(FlowGraphs& graphs, Address address) {
  // TODO(soerenme): Graphs are sorted, we don't need to search the whole thing.
  for (auto i = graphs.begin(), end = graphs.end(); i != end; ++i) {
    if ((*i)->GetEntryPointAddress() == address) {
      return *i;
    }
  }
  return 0;
}

int Results::AddMatch(Address primary, Address secondary) {
  FixedPointInfo fixed_point_info;
  fixed_point_info.algorithm = FindString("function: manual");
  fixed_point_info.confidence = 1.0;
  fixed_point_info.basic_block_count = 0;
  fixed_point_info.edge_count = 0;
  fixed_point_info.instruction_count = 0;
  fixed_point_info.primary = primary;
  fixed_point_info.secondary = secondary;
  fixed_point_info.similarity = 0.0;
  fixed_point_info.flags = 0;
  fixed_point_info.comments_ported = false;
  // Results have been loaded: we need to reload flow graphs and recreate
  // basic block fixed points.
  if (IsInComplete()) {
    FlowGraph primary_graph;
    FlowGraph secondary_graph;
    FixedPoint fixed_point;
    SetupTemporaryFlowGraphs(fixed_point_info, primary_graph, secondary_graph,
                             fixed_point, true);

    Counts counts;
    Histogram histogram;
    FlowGraphs dummy1;
    dummy1.insert(&primary_graph);
    FlowGraphs dummy2;
    dummy2.insert(&secondary_graph);
    FixedPoints dummy3;
    dummy3.insert(fixed_point);
    GetCountsAndHistogram(dummy1, dummy2, dummy3, &histogram, &counts);

    fixed_point.SetMatchingStep("function: manual");
    fixed_point.SetSimilarity(
        GetSimilarityScore(primary_graph, secondary_graph, histogram, counts));
    ClassifyChanges(&fixed_point);
    fixed_point_info.basic_block_count =
        counts["basicBlock matches (library)"] +
        counts["basicBlock matches (non-library)"];
    fixed_point_info.instruction_count =
        counts["instruction matches (library)"] +
        counts["instruction matches (non-library)"];
    fixed_point_info.edge_count =
        counts["flowGraph edge matches (library)"] +
        counts["flowGraph edge matches (non-library)"];
    fixed_point_info.similarity = fixed_point.GetSimilarity();
    fixed_point_info.flags = fixed_point.GetFlags();

    temp_database_.WriteToTempDatabase(fixed_point);

    DeleteTemporaryFlowGraphs();
  } else {
    FlowGraph* primary_graph = FindGraph(flow_graphs1_, primary);
    FlowGraph* secondary_graph = FindGraph(flow_graphs2_, secondary);
    if (!primary_graph || primary_graph->GetEntryPointAddress() != primary ||
        !secondary_graph ||
        secondary_graph->GetEntryPointAddress() != secondary) {
      LOG(INFO) << "invalid graphs in addmatch";
      return 0;
    }
    FixedPoint& fixed_point(const_cast<FixedPoint&>(
        *fixed_points_.insert(FixedPoint(primary_graph, secondary_graph,
                                         "function: manual"))
             .first));
    MatchingContext context(call_graph1_, call_graph2_, flow_graphs1_,
                            flow_graphs2_, fixed_points_);
    primary_graph->SetFixedPoint(&fixed_point);
    secondary_graph->SetFixedPoint(&fixed_point);
    FindFixedPointsBasicBlock(&fixed_point, &context,
                              GetDefaultMatchingStepsBasicBlock());

    Counts counts;
    Histogram histogram;
    FlowGraphs dummy1;
    dummy1.insert(primary_graph);
    FlowGraphs dummy2;
    dummy2.insert(secondary_graph);
    FixedPoints dummy3;
    dummy3.insert(fixed_point);
    GetCountsAndHistogram(dummy1, dummy2, dummy3, &histogram, &counts);

    fixed_point.SetSimilarity(GetSimilarityScore(
        *primary_graph, *secondary_graph, histogram, counts));
    fixed_point.SetConfidence(fixed_point_info.confidence);
    ClassifyChanges(&fixed_point);
    fixed_point_info.basic_block_count =
        counts["basicBlock matches (library)"] +
        counts["basicBlock matches (non-library)"];
    fixed_point_info.instruction_count =
        counts["instruction matches (library)"] +
        counts["instruction matches (non-library)"];
    fixed_point_info.edge_count =
        counts["flowGraph edge matches (library)"] +
        counts["flowGraph edge matches (non-library)"];
    fixed_point_info.similarity = fixed_point.GetSimilarity();
    fixed_point_info.flags = fixed_point.GetFlags();
  }

  fixed_point_infos_.insert(fixed_point_info);
  indexed_fixed_points_.push_back(
      const_cast<FixedPointInfo*>(&*fixed_point_infos_.find(fixed_point_info)));
  std::sort(indexed_fixed_points_.begin(), indexed_fixed_points_.end(),
            &SortBySimilarity);

  if (call_graph2_.IsLibrary(
          call_graph2_.GetVertex(fixed_point_info.secondary)) ||
      flow_graph_infos2_.find(fixed_point_info.secondary) ==
          flow_graph_infos2_.end() ||
      call_graph1_.IsLibrary(
          call_graph1_.GetVertex(fixed_point_info.primary)) ||
      flow_graph_infos1_.find(fixed_point_info.primary) ==
          flow_graph_infos1_.end()) {
    counts_["function matches (library)"] += 1;
    counts_["basicBlock matches (library)"] +=
        fixed_point_info.basic_block_count;
    counts_["instruction matches (library)"] +=
        fixed_point_info.instruction_count;
    counts_["flowGraph edge matches (library)"] += fixed_point_info.edge_count;
  } else {
    counts_["function matches (non-library)"] += 1;
    counts_["basicBlock matches (non-library)"] +=
        fixed_point_info.basic_block_count;
    counts_["instruction matches (non-library)"] +=
        fixed_point_info.instruction_count;
    counts_["flowGraph edge matches (non-library)"] +=
        fixed_point_info.edge_count;
  }
  histogram_[*fixed_point_info.algorithm]++;

  FlowGraphInfo& primary_info(
      flow_graph_infos1_.find(fixed_point_info.primary)->second);
  FlowGraphInfo& secondary_info(
      flow_graph_infos2_.find(fixed_point_info.secondary)->second);
  indexed_flow_graphs1_.erase(std::find(indexed_flow_graphs1_.begin(),
                                        indexed_flow_graphs1_.end(),
                                        &primary_info));
  indexed_flow_graphs2_.erase(std::find(indexed_flow_graphs2_.begin(),
                                        indexed_flow_graphs2_.end(),
                                        &secondary_info));

  refresh_chooser("Matched Functions");
  refresh_chooser("Primary Unmatched");
  refresh_chooser("Secondary Unmatched");
  refresh_chooser("Statistics");
  SetDirty();

  return 1;
}

int Results::AddMatchPrimary(size_t index) {
  static const int widths[] = {10, 30, 5, 6, 5};
  static const char* popups[] = {0, 0, 0, 0};
  const int index2 = choose2(
      CH_MODAL, -1, -1, -1, -1, reinterpret_cast<void*>(this),
      sizeof(widths) / sizeof(widths[0]), widths, &::GetNumUnmatchedSecondary,
      &::GetUnmatchedSecondaryDescription, "Secondary Unmatched", -1 /* Icon */,
      1 /* Default */, 0 /* Delete callback */, 0 /* New callback */,
      0 /* Update callback */, 0 /* Edit callback */, 0 /* Enter callback */,
      0 /* Destroy callback */,
      popups /* Popups (insert, delete, edit, refresh) */, 0);
  if (index2 > 0) {
    const Address primary = GetPrimaryAddress(index);
    const Address secondary = GetSecondaryAddress(index2);
    return AddMatch(primary, secondary);
  }
  return 0;
}

int Results::AddMatchSecondary(size_t index) {
  static const int widths[] = {10, 30, 5, 6, 5};
  static const char* popups[] = {0, 0, 0, 0};
  const int index2 = choose2(
      CH_MODAL, -1, -1, -1, -1, reinterpret_cast<void*>(this),
      sizeof(widths) / sizeof(widths[0]), widths, &::GetNumUnmatchedPrimary,
      &::GetUnmatchedPrimaryDescription, "Primary Unmatched", -1,  // icon
      1,                                                           // default
      0,       // delete callback
      0,       // new callback
      0,       // update callback
      0,       // edit callback
      0,       // enter callback
      0,       // destroy callback
      popups,  // popups (insert, delete, edit, refresh)
      0);
  if (index2 > 0) {
    const Address secondary = GetSecondaryAddress(index);
    const Address primary = GetPrimaryAddress(index2);
    return AddMatch(primary, secondary);
  }
  return 0;
}

std::string GetName(Address address) {
  if (has_user_name(getFlags(static_cast<ea_t>(address)))) {
    qstring ida_name(get_true_name(static_cast<ea_t>(address)));
    if (!ida_name.empty()) {
      return std::string(ida_name.c_str(), ida_name.length());
    }
  }
  return "";
}

std::string GetDemangledName(Address address) {
  if (has_user_name(getFlags(static_cast<ea_t>(address)))) {
    qstring ida_name(get_short_name(static_cast<ea_t>(address)));
    if (!ida_name.empty()) {
      return std::string(ida_name.c_str(), ida_name.length());
    }
  }
  return "";
}

void UpdateName(CallGraph* call_graph, Address address) {
  const std::string& name = GetName(address);
  const CallGraph::Vertex vertex = call_graph->GetVertex(address);
  if (!name.empty() && name != call_graph->GetName(vertex)) {
    call_graph->SetName(vertex, name);
    const std::string& demangled_name = GetDemangledName(address);
    if (demangled_name != name) {
      call_graph->SetDemangledName(vertex, demangled_name);
    } else {
      call_graph->SetDemangledName(vertex, "");
    }
  }
}

void Results::GetMatchDescription(size_t index, char* const* line) const {
  // the target buffers are promised to be MAXSTR == 1024 characters long...
  if (!index) {
    strcpy(line[0], "similarity");
    strcpy(line[1], "confidence");
    strcpy(line[2], "change");
    strcpy(line[3], "EA primary");
    strcpy(line[4], "name primary");
    strcpy(line[5], "EA secondary");
    strcpy(line[6], "name secondary");
    strcpy(line[7], "comments ported");
    strcpy(line[8], "algorithm");
    strcpy(line[9], "matched basicblocks");
    strcpy(line[10], "basicblocks primary");
    strcpy(line[11], "basicblocks secondary");
    strcpy(line[12], "matched instructions");
    strcpy(line[13], "instructions primary");
    strcpy(line[14], "instructions secondary");
    strcpy(line[15], "matched edges");
    strcpy(line[16], "edges primary");
    strcpy(line[17], "edges secondary");
    return;
  }

  if (index > indexed_fixed_points_.size()) {
    return;
  }

  const FixedPointInfo& fixed_point(*indexed_fixed_points_[index - 1]);
  UpdateName(const_cast<CallGraph*>(&call_graph1_), fixed_point.primary);

  FlowGraphInfo empty;
  memset(&empty, 0, sizeof(empty));
  const FlowGraphInfo& primary(
      flow_graph_infos1_.find(fixed_point.primary) != flow_graph_infos1_.end()
          ? flow_graph_infos1_.find(fixed_point.primary)->second
          : empty);
  const FlowGraphInfo& secondary(
      flow_graph_infos2_.find(fixed_point.secondary) != flow_graph_infos2_.end()
          ? flow_graph_infos2_.find(fixed_point.secondary)->second
          : empty);
  snprintf(line[0], MAXSTR, "%.2f", fixed_point.similarity);
  snprintf(line[1], MAXSTR, "%.2f", fixed_point.confidence);
  snprintf(line[2], MAXSTR, "%s",
           GetChangeDescription(ChangeType(fixed_point.flags)).c_str());
  snprintf(line[3], MAXSTR, HEX_ADDRESS,
           static_cast<ea_t>(fixed_point.primary));
  snprintf(line[4], MAXSTR, "%s",
           call_graph1_.GetGoodName(call_graph1_.GetVertex(fixed_point.primary))
               .c_str());
  snprintf(line[5], MAXSTR, HEX_ADDRESS,
           static_cast<ea_t>(fixed_point.secondary));
  snprintf(
      line[6], MAXSTR, "%s",
      call_graph2_.GetGoodName(call_graph2_.GetVertex(fixed_point.secondary))
          .c_str());
  snprintf(line[7], MAXSTR, "%s", fixed_point.comments_ported ? "X" : " ");
  snprintf(
      line[8], MAXSTR, "%s",
      fixed_point.algorithm->substr(fixed_point.algorithm->size() > 10 ? 10 : 0)
          .c_str());
  snprintf(line[9], MAXSTR, "%5d", fixed_point.basic_block_count);
  snprintf(line[10], MAXSTR, "%5d", primary.basic_block_count);
  snprintf(line[11], MAXSTR, "%5d", secondary.basic_block_count);
  snprintf(line[12], MAXSTR, "%6d", fixed_point.instruction_count);
  snprintf(line[13], MAXSTR, "%6d", primary.instruction_count);
  snprintf(line[14], MAXSTR, "%6d", secondary.instruction_count);
  snprintf(line[15], MAXSTR, "%5d", fixed_point.edge_count);
  snprintf(line[16], MAXSTR, "%5d", primary.edge_count);
  snprintf(line[17], MAXSTR, "%5d", secondary.edge_count);
}

void Results::ReadBasicblockMatches(FixedPoint* fixed_point) {
  // we need to check the temporary database first to get up to date data
  // (the user may have added fixedpoints manually)
  // only if we cannot find the fixedpoint there we load from the original db
  int id = 0;
  temp_database_.GetDatabase()
      ->Statement(
          "select coalesce(id, 0) from function where function.address1 = "
          ":address1 and function.address2 = :address2")
      ->BindInt64(fixed_point->GetPrimary()->GetEntryPointAddress())
      .BindInt64(fixed_point->GetSecondary()->GetEntryPointAddress())
      .Execute()
      .Into(&id);
  std::shared_ptr<SqliteDatabase> database;
  if (id) {  // found in temp db
    database.reset(temp_database_.GetDatabase(), [](SqliteDatabase*) {});
  } else {  // load original
    database.reset(new SqliteDatabase(input_filename_.c_str()));
  }

  std::map<int, std::string> algorithms;
  {
    SqliteStatement statement(database.get(),
                              "select id, name from basicblockalgorithm");
    for (statement.Execute(); statement.GotData(); statement.Execute()) {
      int id;
      std::string name;
      statement.Into(&id).Into(&name);
      algorithms[id] = name;
    }
  }

  SqliteStatement statement(
      database.get(),
      "select basicblock.address1, basicblock.address2, basicblock.algorithm "
      "from function "
      "inner join basicblock on functionid = function.id "
      "inner join instruction on basicblockid = basicblock.id "
      "where function.address1 = :address1 and function.address2 = :address2 "
      "order by basicblock.id");
  statement.BindInt64(fixed_point->GetPrimary()->GetEntryPointAddress());
  statement.BindInt64(fixed_point->GetSecondary()->GetEntryPointAddress());

  std::pair<Address, Address> last_basicblock(
      std::make_pair(std::numeric_limits<Address>::max(),
                     std::numeric_limits<Address>::max()));
  int last_algorithm = -1;
  for (statement.Execute(); statement.GotData(); statement.Execute()) {
    std::pair<Address, Address> basicblock;
    int algorithm;
    statement.Into(&basicblock.first).Into(&basicblock.second).Into(&algorithm);
    if (last_algorithm < 0) {
      last_algorithm = algorithm;
      last_basicblock = basicblock;
    }
    if (basicblock != last_basicblock) {
      fixed_point->Add(last_basicblock.first, last_basicblock.second,
                       algorithms[last_algorithm]);
      last_basicblock = basicblock;
      last_algorithm = algorithm;
    }
  }
  if (last_algorithm != -1) {
    fixed_point->Add(last_basicblock.first, last_basicblock.second,
                     algorithms[last_algorithm]);
  }
}

void ReadTemporaryFlowGraph(const FixedPointInfo& fixed_point_info,
                            const FlowGraphInfos& flow_graph_infos,
                            CallGraph* call_graph, FlowGraph* flow_graph,
                            Instruction::Cache* instruction_cache) {
  auto info = flow_graph_infos.find(fixed_point_info.primary);
  if (info == flow_graph_infos.end()) {
    throw std::runtime_error("error: flow graph not found for fixed point");
  }
  std::ifstream stream(call_graph->GetFilePath().c_str(),
                       std::ios_base::binary);
  BinExport2 proto;
  if (!proto.ParseFromIstream(&stream)) {
    throw std::runtime_error("failed parsing protocol buffer");
  }
  for (const auto& proto_flow_graph : proto.flow_graph()) {
    // Entry point address is always set.
    const auto address =
        proto
            .instruction(
                proto.basic_block(proto_flow_graph.entry_basic_block_index())
                    .instruction_index(0)
                    .begin_index())
            .address();
    if (address == info->second.address) {
      flow_graph->SetCallGraph(call_graph);
      flow_graph->Read(proto, proto_flow_graph, call_graph, instruction_cache);
      return;
    }
  }
  throw std::runtime_error("error: flow graph data not found");
}

void Results::SetupTemporaryFlowGraphs(const FixedPointInfo& fixed_point_info,
                                       FlowGraph& primary, FlowGraph& secondary,
                                       FixedPoint& fixed_point,
                                       bool create_instruction_matches) {
  instruction_cache_.Clear();
  try {
    ReadTemporaryFlowGraph(fixed_point_info, flow_graph_infos1_, &call_graph1_,
                           &primary, &instruction_cache_);
  } catch (...) {
    throw std::runtime_error(
        StrCat("error reading: ", call_graph1_.GetFilePath()));
  }
  try {
    ReadTemporaryFlowGraph(fixed_point_info, flow_graph_infos2_, &call_graph2_,
                           &secondary, &instruction_cache_);
  } catch (...) {
    throw std::runtime_error(
        StrCat("error reading: ", call_graph2_.GetFilePath()));
  }
  fixed_point.Create(&primary, &secondary);
  MatchingContext context(call_graph1_, call_graph2_, flow_graphs1_,
                          flow_graphs2_, fixed_points_);
  flow_graphs1_.clear();
  flow_graphs1_.insert(&primary);
  flow_graphs2_.clear();
  flow_graphs2_.insert(&secondary);
  fixed_points_.clear();
  fixed_point.SetConfidence(fixed_point_info.confidence);
  fixed_point.SetSimilarity(fixed_point_info.similarity);
  fixed_point.SetFlags(fixed_point_info.flags);
  fixed_point.SetMatchingStep(*fixed_point_info.algorithm);
  std::pair<FixedPoints::iterator, bool> fixed_point_it =
      fixed_points_.insert(fixed_point);
  primary.SetFixedPoint(const_cast<FixedPoint*>(&*fixed_point_it.first));
  secondary.SetFixedPoint(const_cast<FixedPoint*>(&*fixed_point_it.first));
  call_graph1_.AttachFlowGraph(&primary);
  call_graph2_.AttachFlowGraph(&secondary);
  if (create_instruction_matches) {
    FindFixedPointsBasicBlock(&fixed_point, &context,
                              GetDefaultMatchingStepsBasicBlock());
  } else {
    ReadBasicblockMatches(&fixed_point);
  }
}

void Results::DeleteTemporaryFlowGraphs() {
  flow_graphs1_.clear();
  flow_graphs2_.clear();
  fixed_points_.clear();
}

bool Results::PrepareVisualCallGraphDiff(size_t index, std::string* message) {
  if (!index || index > indexed_fixed_points_.size()) {
    return false;
  }

  const FixedPointInfo& fixed_point_info(*indexed_fixed_points_[index - 1]);
  diff_database_id_++;
  std::string name(StrCat("visual_diff", diff_database_id_, ".database"));
  std::string database_file;
  // TODO(soerenme): Bug: if matches have been manually modified in the meantime
  //                 we are hosed!
  if (IsInComplete()) {
    database_file = input_filename_;
  } else {
    // TODO(soerenme): This is insanely inefficient: every single call graph
    //                 diff recreates the full result.
    DatabaseWriter writer(name, true);
    writer.Write(call_graph1_, call_graph2_, flow_graphs1_, flow_graphs2_,
                 fixed_points_);
    database_file = writer.GetFilename();
  }

  *message = StringPrintf(
      "<BinDiffMatch>\n"
      "\t<Type value=\"callgraph\" />\n"
      "\t<Database path=\"%s\" />\n"
      "\t<Match primary=\"%zu\" secondary=\"%zu\" />\n"
      "\t<Primary path=\"%s\" />\n"
      "\t<Secondary path=\"%s\" />\n"
      "</BinDiffMatch>\n",
      database_file.c_str(), fixed_point_info.primary,
      fixed_point_info.secondary, call_graph1_.GetFilename().c_str(),
      call_graph2_.GetFilename().c_str());
  return true;
}

bool Results::PrepareVisualDiff(size_t index, std::string* message) {
  if (!index || index > indexed_fixed_points_.size()) {
    return false;
  }

  const FixedPointInfo& fixed_point_info(*indexed_fixed_points_[index - 1]);

  FlowGraphInfo empty;
  memset(&empty, 0, sizeof(empty));
  const FlowGraphInfo& primary_info(
      flow_graph_infos1_.find(fixed_point_info.primary) !=
              flow_graph_infos1_.end()
          ? flow_graph_infos1_.find(fixed_point_info.primary)->second
          : empty);
  const FlowGraphInfo& secondary_info(
      flow_graph_infos2_.find(fixed_point_info.secondary) !=
              flow_graph_infos2_.end()
          ? flow_graph_infos2_.find(fixed_point_info.secondary)->second
          : empty);
  if (primary_info.instruction_count == 0 &&
      secondary_info.instruction_count == 0) {
    warning("Both functions are empty, nothing to display!");
    return false;
  }

  FixedPoint fixed_point;
  FlowGraphs flow_graphs1;
  FlowGraphs flow_graphs2;
  FixedPoints fixed_points;
  FlowGraph primary;
  FlowGraph secondary;
  if (IsInComplete()) {
    LOG(INFO) << "Loading incomplete flow graphs";
    // Results have been loaded: we need to reload flow graphs and recreate
    // basic block fixed_points.
    SetupTemporaryFlowGraphs(fixed_point_info, primary, secondary, fixed_point,
                             false);
  } else {
    fixed_point = *FindFixedPoint(fixed_point_info);
  }
  flow_graphs1.insert(fixed_point.GetPrimary());
  flow_graphs2.insert(fixed_point.GetSecondary());
  fixed_points.insert(fixed_point);

  diff_database_id_++;
  std::string name(StrCat("visual_diff", diff_database_id_, ".database"));
  DatabaseWriter writer(name, true);
  writer.Write(call_graph1_, call_graph2_, flow_graphs1, flow_graphs2,
               fixed_points);
  const std::string database_file = writer.GetFilename();

  *message = StringPrintf(
      "<BinDiffMatch>\n"
      "\t<Type value=\"flowgraph\" />\n"
      "\t<Database path=\"%s\" />\n"
      "\t<Primary path=\"%s\" address=\"%zu\" />\n"
      "\t<Secondary path=\"%s\" address=\"%zu\" />\n"
      "</BinDiffMatch>\n",
      database_file.c_str(), call_graph1_.GetFilename().c_str(),
      fixed_point.GetPrimary()->GetEntryPointAddress(),
      call_graph2_.GetFilename().c_str(),
      fixed_point.GetSecondary()->GetEntryPointAddress());

  if (IsInComplete()) {
    DeleteTemporaryFlowGraphs();
  }
  return true;
}

FixedPoint* Results::FindFixedPoint(const FixedPointInfo& fixed_point_info) {
  // TODO(soerenme): Use tree search.
  for (const auto& fixed_point : fixed_points_) {
    CHECK(fixed_point.GetPrimary() && fixed_point.GetSecondary());
    if (fixed_point.GetPrimary()->GetEntryPointAddress() ==
            fixed_point_info.primary &&
        fixed_point.GetSecondary()->GetEntryPointAddress() ==
            fixed_point_info.secondary) {
      return const_cast<FixedPoint*>(&fixed_point);
    }
  }
  return 0;
}

void Results::Read(Reader& reader) {
  flow_graph_infos1_.clear();
  flow_graph_infos2_.clear();
  fixed_point_infos_.clear();
  indexed_flow_graphs1_.clear();
  indexed_flow_graphs2_.clear();
  indexed_fixed_points_.clear();

  incomplete_results_ = true;
  reader.Read(call_graph1_, call_graph2_, flow_graph_infos1_,
              flow_graph_infos2_, fixed_point_infos_);
  if (const DatabaseReader* databaseReader =
          dynamic_cast<DatabaseReader*>(&reader)) {
    input_filename_ = databaseReader->GetInputFilename();
    histogram_ = databaseReader->GetBasicBlockFixedPointInfo();
  } else {
    CHECK(false && "unsupported reader");
  }

  InitializeIndexedVectors();
  Count();
  similarity_ = reader.GetSimilarity();
  confidence_ = reader.GetConfidence();
  dirty_ = false;

  // TODO(soerenme): Iterate over all fixedpoints that have been added manually
  //                 by the Java UI and evaluate them (add basic
  //                 block/instruction matches).
}

void Results::Write(Writer& writer) {
  writer.Write(call_graph1_, call_graph2_, flow_graphs1_, flow_graphs2_,
               fixed_points_);
  dirty_ = false;
}

void Results::CreateIndexedViews() {
  if (indexed_flow_graphs1_.empty() && indexed_flow_graphs2_.empty() &&
      indexed_fixed_points_.empty()) {
    // Only initialize indices the first time around.
    for (const auto& fixed_point : fixed_points_) {
      FixedPointInfo fixed_point_info;
      fixed_point_info.algorithm = FindString(fixed_point.GetMatchingStep());
      fixed_point_info.confidence = fixed_point.GetConfidence();
      fixed_point_info.similarity = fixed_point.GetSimilarity();
      fixed_point_info.flags = fixed_point.GetFlags();
      fixed_point_info.primary =
          fixed_point.GetPrimary()->GetEntryPointAddress();
      fixed_point_info.secondary =
          fixed_point.GetSecondary()->GetEntryPointAddress();
      fixed_point_info.comments_ported = fixed_point.GetCommentsPorted();
      Counts counts;
      Histogram histogram;
      ::Count(fixed_point, &counts, &histogram);
      fixed_point_info.basic_block_count =
          counts["basicBlock matches (library)"] +
          counts["basicBlock matches (non-library)"];
      fixed_point_info.instruction_count =
          counts["instruction matches (library)"] +
          counts["instruction matches (non-library)"];
      fixed_point_info.edge_count =
          counts["flowGraph edge matches (library)"] +
          counts["flowGraph edge matches (non-library)"];
      fixed_point_infos_.insert(fixed_point_info);
    }
    InitializeIndexedVectors();
    GetCountsAndHistogram(flow_graphs1_, flow_graphs2_, fixed_points_,
                          &histogram_, &counts_);
    Confidences confidences;
    confidence_ = GetConfidence(histogram_, &confidences);
    similarity_ =
        GetSimilarityScore(call_graph1_, call_graph2_, histogram_, counts_);
  }
}

void Results::MarkPortedCommentsInTempDatabase() {
  temp_database_.SetCommentsPorted(fixed_point_infos_);
}

// Transfer from temp db to real db.
void Results::MarkPortedCommentsInDatabase() {
  try {
    if (!input_filename_.empty()) {
      SqliteDatabase database(input_filename_.c_str());
      DatabaseTransmuter::MarkPortedComments(
          &database, temp_database_.GetFilename().c_str(), fixed_point_infos_);
    }
  } catch (...) {
    // we swallow any errors here. The database may be read only or
    // the commentsported table doesn't exist. We don't care...
  }
}

int Results::PortComments(Address start_address_source,
                          Address end_address_source,
                          Address start_address_target,
                          Address end_address_target, double min_confidence,
                          double min_similarity) {
  for (size_t index = 1; index <= indexed_fixed_points_.size(); ++index) {
    FixedPointInfo& fixed_point_info(*indexed_fixed_points_[index - 1]);
    if (get_func(static_cast<ea_t>(fixed_point_info.primary))) {
      if (IsInComplete()) {
        FlowGraph primary, secondary;
        FixedPoint fixed_point;
        SetupTemporaryFlowGraphs(fixed_point_info, primary, secondary,
                                 fixed_point, false);

        SetComments(&fixed_point, call_graph2_.GetComments(),
                    start_address_target, end_address_target,
                    start_address_source, end_address_source, min_confidence,
                    min_similarity);

        DeleteTemporaryFlowGraphs();
      } else {
        SetComments(FindFixedPoint(fixed_point_info),
                    call_graph2_.GetComments(), start_address_target,
                    end_address_target, start_address_source,
                    end_address_source, min_confidence, min_similarity);
      }
    }
    fixed_point_info.comments_ported = true;
  }
  MarkPortedCommentsInTempDatabase();
  return 1;  // IDA API 1 == ok
}

int Results::PortComments(int index, bool as_external) {
  if (index == START_SEL) {
    // multiselection support. we must return OK, but cannot do anything yet
    return 1;
  }
  if (index == END_SEL) {
    MarkPortedCommentsInTempDatabase();
    return 1;
  }
  if (!index || index > static_cast<int>(indexed_fixed_points_.size())) {
    return 0;
  }

  FixedPointInfo& fixed_point_info(*indexed_fixed_points_[index - 1]);
  const Address start_address_target = 0;
  const Address end_address_target = std::numeric_limits<ea_t>::max() - 1;
  const Address start_address_source = fixed_point_info.primary;
  if (func_t* function = get_func(static_cast<ea_t>(start_address_source))) {
    const ea_t end_address_source = function->endEA;
    if (as_external) {
      function->flags |= FUNC_LIB;
    }
    if (IsInComplete()) {
      FlowGraph primary, secondary;
      FixedPoint fixed_point;
      SetupTemporaryFlowGraphs(fixed_point_info, primary, secondary,
                               fixed_point, false);

      SetComments(&fixed_point, call_graph2_.GetComments(),
                  start_address_target, end_address_target,
                  start_address_source, end_address_source, 0.0, 0.0);

      DeleteTemporaryFlowGraphs();
    } else {
      SetComments(FindFixedPoint(fixed_point_info), call_graph2_.GetComments(),
                  start_address_target, end_address_target,
                  start_address_source, end_address_source, 0.0, 0.0);
    }
  }
  fixed_point_info.comments_ported = true;
  return 1;
}

int Results::ConfirmMatch(int index) {
  if (index == START_SEL || index == END_SEL) {
    // multiselection support. we must return OK, but cannot do anything yet
    return 1;
  }

  if (!index || index > static_cast<int>(indexed_fixed_points_.size())) {
    return 0;
  }

  FixedPointInfo* fixed_point_info(indexed_fixed_points_[index - 1]);
  fixed_point_info->algorithm = FindString("function: manual");
  fixed_point_info->confidence = 1.0;
  if (!IsInComplete()) {
    FixedPoint* fixed_point(FindFixedPoint(*fixed_point_info));
    fixed_point->SetMatchingStep(*fixed_point_info->algorithm);
    fixed_point->SetConfidence(fixed_point_info->confidence);
  }
  SetDirty();

  return 1;
}

void CopyToClipboard(const std::string& data) {
#ifdef WIN32
  if (!OpenClipboard(0)) {
    throw std::runtime_error(GetLastOsError());
  }
  struct ClipboardCloser {
    ~ClipboardCloser() { CloseClipboard(); }
  } deleter;

  if (!EmptyClipboard()) {
    throw std::runtime_error(GetLastOsError());
  }

  // std::strings are not required to be zero terminated, thus we add an extra
  // zero.
  HGLOBAL buffer_handle =
      GlobalAlloc(GMEM_MOVEABLE | GMEM_ZEROINIT, data.size() + 1);
  if (!buffer_handle) {
    throw std::runtime_error(GetLastOsError());
  }

  bool fail = true;
  char* buffer = static_cast<char*>(GlobalLock(buffer_handle));
  if (buffer) {
    memcpy(buffer, data.c_str(), data.size());
    if (GlobalUnlock(buffer) &&
        SetClipboardData(CF_TEXT, buffer_handle /* Transfer ownership */)) {
      fail = false;
    }
  }
  if (fail) {
    // Only free on failure, as SetClipboardData() takes ownership.
    GlobalFree(buffer_handle);
    throw std::runtime_error(GetLastOsError());
  }
#else
  // TODO(cblichmann): Implement copy to clipbard for Linux/macOS.
#endif
}

int Results::CopyPrimaryAddress(int index) const {
  if (index < 1 || index > static_cast<int>(indexed_fixed_points_.size())) {
    return 0;
  }

  const FixedPointInfo& fixed_point_info(*indexed_fixed_points_[index - 1]);
  enum { kBufferSize = 32 };
  char buffer[kBufferSize];
  memset(buffer, 0, kBufferSize);
  snprintf(buffer, kBufferSize, HEX_ADDRESS,
           static_cast<ea_t>(fixed_point_info.primary));
  CopyToClipboard(buffer);

  return 1;
}

int Results::CopySecondaryAddress(int index) const {
  if (index < 1 || index > static_cast<int>(indexed_fixed_points_.size())) {
    return 0;
  }

  const FixedPointInfo& fixed_point_info(*indexed_fixed_points_[index - 1]);
  enum { kBufferSize = 32 };
  char buffer[kBufferSize];
  memset(buffer, 0, kBufferSize);
  snprintf(buffer, kBufferSize, HEX_ADDRESS,
           static_cast<ea_t>(fixed_point_info.secondary));
  CopyToClipboard(buffer);

  return 1;
}

int Results::CopyPrimaryAddressUnmatched(int index) const {
  if (index < 1 || index > static_cast<int>(indexed_flow_graphs1_.size())) {
    return 0;
  }

  const FlowGraphInfo& flowGraphInfo(*indexed_flow_graphs1_[index - 1]);
  enum { kBufferSize = 32 };
  char buffer[kBufferSize];
  memset(buffer, 0, kBufferSize);
  snprintf(buffer, kBufferSize, HEX_ADDRESS,
           static_cast<ea_t>(flowGraphInfo.address));
  CopyToClipboard(buffer);

  return 1;
}

int Results::CopySecondaryAddressUnmatched(int index) const {
  if (index < 1 || index > static_cast<int>(indexed_flow_graphs2_.size())) {
    return 0;
  }

  const FlowGraphInfo& flowGraphInfo(*indexed_flow_graphs2_[index - 1]);
  enum { kBufferSize = 32 };
  char buffer[kBufferSize];
  memset(buffer, 0, kBufferSize);
  snprintf(buffer, kBufferSize, HEX_ADDRESS,
           static_cast<ea_t>(flowGraphInfo.address));
  CopyToClipboard(buffer);

  return 1;
}

bool Results::IsInComplete() const { return incomplete_results_; }

void Results::GetUnmatchedDescription(const IndexedFlowGraphs& flow_graphs,
                                      size_t index, char* const* line) const {
  // The target buffers are promised to be MAXSTR == 1024 characters long...
  if (!index) {
    strcpy(line[0], "EA");
    strcpy(line[1], "Name");
    strcpy(line[2], "Basicblocks");
    strcpy(line[3], "Instructions");
    strcpy(line[4], "Edges");
    return;
  }

  const FlowGraphInfo& flow_graph_info = *flow_graphs[index - 1];
  // The primary IDB is loaded in IDA and the function name might have been
  // changed manually, thus we need to propagate that information.
  if (&flow_graphs == &indexed_flow_graphs1_) {
    UpdateName(const_cast<CallGraph*>(&call_graph1_), flow_graph_info.address);
  }

  CHECK(flow_graph_info.demangled_name);
  snprintf(line[0], MAXSTR, HEX_ADDRESS,
           static_cast<ea_t>(flow_graph_info.address));
  snprintf(line[1], MAXSTR, "%s",
           flow_graph_info.demangled_name->empty()
               ? flow_graph_info.name->c_str()
               : flow_graph_info.demangled_name->c_str());
  snprintf(line[2], MAXSTR, "%5d", flow_graph_info.basic_block_count);
  snprintf(line[3], MAXSTR, "%6d", flow_graph_info.instruction_count);
  snprintf(line[4], MAXSTR, "%5d", flow_graph_info.edge_count);
}

void Results::InitializeIndexedVectors() {
  std::set<Address> matched_primaries, matched_secondaries;
  for (auto i = fixed_point_infos_.begin(), end = fixed_point_infos_.end();
       i != end; ++i) {
    matched_primaries.insert(i->primary);
    matched_secondaries.insert(i->secondary);
    indexed_fixed_points_.push_back(const_cast<FixedPointInfo*>(&*i));
  }
  std::sort(indexed_fixed_points_.begin(), indexed_fixed_points_.end(),
            &SortBySimilarity);

  for (auto i = flow_graph_infos1_.begin(), end = flow_graph_infos1_.end();
       i != end; ++i) {
    if (matched_primaries.find(i->first) == matched_primaries.end()) {
      indexed_flow_graphs1_.push_back(&i->second);
    }
  }
  for (auto i = flow_graph_infos2_.begin(), end = flow_graph_infos2_.end();
       i != end; ++i) {
    if (matched_secondaries.find(i->first) == matched_secondaries.end()) {
      indexed_flow_graphs2_.push_back(&i->second);
    }
  }

  FlowGraphInfo empty;
  memset(&empty, 0, sizeof(empty));
  {
    CallGraph::VertexIterator i, end;
    for (boost::tie(i, end) = boost::vertices(call_graph1_.GetGraph());
         i != end; ++i) {
      const Address address = call_graph1_.GetAddress(*i);
      if (flow_graph_infos1_.find(address) == flow_graph_infos1_.end()) {
        empty.address = address;
        empty.name = &call_graph1_.GetName(*i);
        empty.demangled_name = &call_graph1_.GetDemangledName(*i);
        flow_graph_infos1_[address] = empty;
        if (matched_primaries.find(address) == matched_primaries.end()) {
          indexed_flow_graphs1_.push_back(&flow_graph_infos1_[address]);
        }
      }
    }
  }
  {
    CallGraph::VertexIterator i, end;
    for (boost::tie(i, end) = boost::vertices(call_graph2_.GetGraph());
         i != end; ++i) {
      const Address address = call_graph2_.GetAddress(*i);
      if (flow_graph_infos2_.find(address) == flow_graph_infos2_.end()) {
        empty.address = address;
        empty.name = &call_graph2_.GetName(*i);
        empty.demangled_name = &call_graph2_.GetDemangledName(*i);
        flow_graph_infos2_[address] = empty;
        if (matched_secondaries.find(address) == matched_secondaries.end()) {
          indexed_flow_graphs2_.push_back(&flow_graph_infos2_[address]);
        }
      }
    }
  }
}

void Results::Count() {
  counts_.clear();
  for (auto i = flow_graph_infos1_.cbegin(), end = flow_graph_infos1_.cend();
       i != end; ++i) {
    const FlowGraphInfo& info = i->second;
    const int is_lib =
        call_graph1_.IsLibrary(call_graph1_.GetVertex(info.address)) ||
        call_graph1_.IsStub(call_graph1_.GetVertex(info.address)) ||
        info.basic_block_count == 0;
    counts_["functions primary (library)"] += is_lib;
    counts_["functions primary (non-library)"] += (1 - is_lib);
    counts_["basicBlocks primary (library)"] += is_lib * info.basic_block_count;
    counts_["basicBlocks primary (non-library)"] +=
        (1 - is_lib) * info.basic_block_count;
    counts_["instructions primary (library)"] +=
        is_lib * info.instruction_count;
    counts_["instructions primary (non-library)"] +=
        (1 - is_lib) * info.instruction_count;
    counts_["flowGraph edges primary (library)"] += is_lib * info.edge_count;
    counts_["flowGraph edges primary (non-library)"] +=
        (1 - is_lib) * info.edge_count;
  }
  {
    CallGraph::VertexIterator i, end;
    for (boost::tie(i, end) = boost::vertices(call_graph1_.GetGraph());
         i != end; ++i) {
      const Address address = call_graph1_.GetAddress(*i);
      if (flow_graph_infos1_.find(address) == flow_graph_infos1_.end()) {
        counts_["functions primary (library)"] += 1;
      }
    }
  }
  for (auto i = flow_graph_infos2_.cbegin(), end = flow_graph_infos2_.cend();
       i != end; ++i) {
    const FlowGraphInfo& info = i->second;
    const int is_lib =
        call_graph2_.IsLibrary(call_graph2_.GetVertex(info.address)) ||
        call_graph2_.IsStub(call_graph2_.GetVertex(info.address)) ||
        info.basic_block_count == 0;
    counts_["functions secondary (library)"] += is_lib;
    counts_["functions secondary (non-library)"] += (1 - is_lib);
    counts_["basicBlocks secondary (library)"] +=
        is_lib * info.basic_block_count;
    counts_["basicBlocks secondary (non-library)"] +=
        (1 - is_lib) * info.basic_block_count;
    counts_["instructions secondary (library)"] +=
        is_lib * info.instruction_count;
    counts_["instructions secondary (non-library)"] +=
        (1 - is_lib) * info.instruction_count;
    counts_["flowGraph edges secondary (library)"] += is_lib * info.edge_count;
    counts_["flowGraph edges secondary (non-library)"] +=
        (1 - is_lib) * info.edge_count;
  }
  {
    CallGraph::VertexIterator i, end;
    for (boost::tie(i, end) = boost::vertices(call_graph2_.GetGraph());
         i != end; ++i) {
      const Address address = call_graph2_.GetAddress(*i);
      if (flow_graph_infos2_.find(address) == flow_graph_infos2_.end()) {
        counts_["functions secondary (library)"] += 1;
      }
    }
  }
  counts_["function matches (library)"] = 0;
  counts_["basicBlock matches (library)"] = 0;
  counts_["instruction matches (library)"] = 0;
  counts_["flowGraph edge matches (library)"] = 0;
  counts_["function matches (non-library)"] = 0;
  counts_["basicBlock matches (non-library)"] = 0;
  counts_["instruction matches (non-library)"] = 0;
  counts_["flowGraph edge matches (non-library)"] = 0;
  for (auto i = fixed_point_infos_.cbegin(), end = fixed_point_infos_.cend();
       i != end; ++i) {
    if (call_graph2_.IsLibrary(call_graph2_.GetVertex(i->secondary)) ||
        flow_graph_infos2_.find(i->secondary) == flow_graph_infos2_.end() ||
        call_graph1_.IsLibrary(call_graph1_.GetVertex(i->primary)) ||
        flow_graph_infos1_.find(i->primary) == flow_graph_infos1_.end()) {
      counts_["function matches (library)"] += 1;
      counts_["basicBlock matches (library)"] += i->basic_block_count;
      counts_["instruction matches (library)"] += i->instruction_count;
      counts_["flowGraph edge matches (library)"] += i->edge_count;
    } else {
      counts_["function matches (non-library)"] += 1;
      counts_["basicBlock matches (non-library)"] += i->basic_block_count;
      counts_["instruction matches (non-library)"] += i->instruction_count;
      counts_["flowGraph edge matches (non-library)"] += i->edge_count;
    }
    histogram_[*i->algorithm]++;
  }
}

Results* g_results = 0;

std::string FindFile(const std::string& path, const std::string& extension) {
  std::vector<std::string> entries;
  GetDirectoryEntries(path, &entries);
  for (const auto& entry : entries) {
    if (GetFileExtension(entry) == extension) {
      return JoinPath(path, Basename(entry));
    }
  }
  return "";
}

class ExporterThread {
 public:
  explicit ExporterThread(const std::string& temp_dir,
                          const std::string& idb_path)
      : success_(false),
        secondary_idb_path_(idb_path),
        secondary_temp_dir(JoinPath(temp_dir, "secondary")),
        idc_file_(JoinPath(temp_dir, "run_secondary.idc")) {
    RemoveAll(secondary_temp_dir);
    CreateDirectories(secondary_temp_dir);

    std::ofstream file(idc_file_.c_str());
    file << "#include <idc.idc>\n"
         << "static main()\n"
         << "{\n"
         << "\tBatch(0);\n"
         << "\tWait();\n"
         << "\tRunPlugin(\"zynamics_binexport_" << kBinExportVersion
         << "\", 2);\n"
         << "\tExit(0);\n"
         << "}\n";
  }

  void operator()() {
    std::string ida_exe(idadir(0));
    const bool is_64bit =
        StringPiece(ToUpper(secondary_idb_path_)).ends_with(".I64");

    std::string s_param;
    // We only support the Qt version.
#ifdef WIN32
    if (is_64bit) {
      ida_exe += "\\idaq64.exe";
    } else {
      ida_exe += "\\idaq.exe";
    }

    // Note: We only support IDA 6.8 or higher. The current quoting behavior
    //       on Windows was introduced in IDA 6.1. Previously, the quotes were
    //       not needed.
    s_param = "-S\"" + idc_file_ + "\"";
#else
    if (is_64bit) {
      ida_exe += "/idaq64";
    } else {
      ida_exe += "/idaq";
    }
    s_param = "-S" + idc_file_;
#endif
    std::vector<std::string> argv;
    argv.push_back(ida_exe);
    argv.push_back("-A");
    argv.push_back("-OExporterModule:" + secondary_temp_dir);
    argv.push_back(s_param);
    argv.push_back(secondary_idb_path_);

    success_ = SpawnProcess(argv, true /* Wait */, &status_message_);
  }

  bool success() const { return success_; }

  const std::string& status() const { return status_message_; }

 private:
  volatile bool success_;
  std::string status_message_;
  std::string secondary_idb_path_;
  std::string secondary_temp_dir;
  std::string idc_file_;
};

bool ExportIdbs() {
  const std::string temp_dir(GetTempDirectory("BinDiff", /* create = */ true));

  const char* secondary_idb = askfile2_c(
      /* forsave = */ false, "*.idb;*.i64",
      StrCat("IDA Databases|*.idb;*.i64|All files|", kAllFilesFilter).c_str(),
      "Select Database");
  if (!secondary_idb) {
    return false;
  }

  const std::string primary_idb_path(database_idb /* IDA global variable */);
  std::string secondary_idb_path(secondary_idb);
  if (primary_idb_path == secondary_idb_path) {
    throw std::runtime_error(
        "You cannot open the same IDB file twice. Please copy and rename one if"
        " you want to diff against self.");
  } else if (ReplaceFileExtension(primary_idb_path, "") ==
             ReplaceFileExtension(secondary_idb_path, "")) {
    throw std::runtime_error(
        "You cannot open an idb and an i64 with the same base filename in the "
        "same directory. Please rename or move one of the files.");
  } else if (GetFileExtension(primary_idb_path) == ".idb" &&
             GetFileExtension(secondary_idb_path) == ".i64") {
    if (askyn_c(0,
                "Warning: you are trying to diff a 32-bit binary vs. a 64-bit "
                "binary.\n"
                "If the 64-bit binary contains addresses outside the 32-bit "
                "range they will be truncated.\n"
                "To fix this problem please start 64-bit aware IDA and diff "
                "the other way around, i.e. 64-bit vs. 32-bit.\n"
                "Continue anyways?") != 1) {
      return false;
    }
  }
  delete g_results;
  g_results = 0;

  LOG(INFO) << "Diffing " << Basename(primary_idb_path) << " vs "
            << Basename(secondary_idb_path);
  WaitBox wait_box("Exporting idbs...");
  {
    ExporterThread exporter(temp_dir, secondary_idb_path);
    std::thread thread(std::ref(exporter));

    std::string primary_temp_dir(JoinPath(temp_dir, "primary"));
    RemoveAll(primary_temp_dir);
    CreateDirectories(primary_temp_dir);

    char errbuf[MAXSTR];
    idc_value_t arg(primary_temp_dir.c_str());
    if (!Run(StrCat("BinExport2Diff", kBinExportVersion).c_str(),
             /* argsnum = */ 1, &arg, /* result = */ nullptr, errbuf,
             MAXSTR - 1)) {
      throw std::runtime_error(
          StrCat("Error: Export of the current database failed: ", errbuf));
    }

    thread.join();
    if (!exporter.success()) {
      throw std::runtime_error(
          StrCat("Failed to spawn second IDA instance: ", exporter.status()));
    }
  }

  return true;
}

bool PortFunctionName(FixedPoint* fixed_point) {
  CallGraph* primary_call_graph = fixed_point->GetPrimary()->GetCallGraph();
  CallGraph* secondary_call_graph = fixed_point->GetSecondary()->GetCallGraph();
  const Address primary_address =
      fixed_point->GetPrimary()->GetEntryPointAddress();
  const Address secondary_address =
      fixed_point->GetSecondary()->GetEntryPointAddress();
  if (const func_t* function = get_func(static_cast<ea_t>(primary_address))) {
    if (function->startEA == primary_address) {
      if (!secondary_call_graph->HasRealName(
              secondary_call_graph->GetVertex(secondary_address))) {
        return false;
      }

      const std::string& name = fixed_point->GetSecondary()->GetName();
      enum { BUFFER_SIZE = MAXSTR };
      char buffer[BUFFER_SIZE];
      get_true_name(static_cast<ea_t>(primary_address),
                    static_cast<ea_t>(primary_address), buffer, BUFFER_SIZE);
      if (std::string(buffer) != name) {
        set_name(static_cast<ea_t>(primary_address), name.c_str(),
                 SN_NOWARN | SN_CHECK);
        const CallGraph::Vertex vertex =
            primary_call_graph->GetVertex(primary_address);
        primary_call_graph->SetName(vertex, name);
        primary_call_graph->SetDemangledName(
            vertex, GetDemangledName(static_cast<ea_t>(primary_address)));
        return true;
      }
    }
  }
  return false;
}

// Taken from IDA SDK 6.1 nalt.hpp. ExtraGet has since been deprecated.
inline ssize_t ExtraGet(ea_t ea, int what, char* buf, size_t bufsize) {
  return netnode(ea).supstr(what, buf, bufsize);
}

std::string GetLineComments(Address address, int direction) {
  char buffer[4096];
  const size_t bufferSize = sizeof(buffer) / sizeof(buffer[0]);

  std::string comment;
  if (direction < 0) {
    // anterior comments
    for (int i = 0; ExtraGet(static_cast<ea_t>(address), E_PREV + i, buffer,
                             bufferSize) != -1;
         ++i) {
      comment += buffer + std::string("\n");
    }
  } else if (direction > 0) {
    // posterior comments
    for (int i = 0; ExtraGet(static_cast<ea_t>(address), E_NEXT + i, buffer,
                             bufferSize) != -1;
         ++i) {
      comment += buffer + std::string("\n");
    }
  }
  if (!comment.empty()) {
    comment = comment.substr(0, comment.size() - 1);
  }
  return comment;
}

size_t SetComments(Address source, Address target, const Comments& comments,
                   FixedPoint* fixed_point = 0) {
  int comment_count = 0;
  const OperatorId begin(std::make_pair(source, 0));
  for (auto i = comments.lower_bound(begin);
       i != comments.end() && i->first.first == source; ++i, ++comment_count) {
    CHECK(source == i->first.first);
    const Comment& comment = i->second;
    const Address address = target;
    const int operand_id = i->first.second;

    // Do not port auto generated names (unfortunately this does not work for
    // comments)
    // The IDA API is totally broken here. See:
    // https://zynamics.fogbugz.com/default.asp?4451
    if ((comment.type_ == Comment::ENUM || comment.type_ == Comment::LOCATION ||
         comment.type_ == Comment::GLOBALREFERENCE ||
         comment.type_ == Comment::LOCALREFERENCE) &&
        !is_uname(comment.comment_.c_str())) {
      continue;
    }

    switch (comment.type_) {
      case Comment::REGULAR: {
        set_cmt(static_cast<ea_t>(address), comment.comment_.c_str(),
                comment.repeatable_);
      } break;
      case Comment::ENUM: {
        unsigned char serial;
        if (isEnum0(getFlags(static_cast<ea_t>(address))) && operand_id == 0) {
          if (int id = get_enum_id(static_cast<ea_t>(address), operand_id,
                                   &serial) != BADNODE) {
            set_enum_name(id, comment.comment_.c_str());
          }
        }
        if (isEnum1(getFlags(static_cast<ea_t>(address))) && operand_id == 1) {
          if (int id = get_enum_id(static_cast<ea_t>(address), operand_id,
                                   &serial) != BADNODE) {
            set_enum_name(id, comment.comment_.c_str());
          }
        }
      } break;
      case Comment::FUNCTION: {
        if (func_t* function = get_func(static_cast<ea_t>(address))) {
          if (function->startEA == address) {
            set_func_cmt(function, comment.comment_.c_str(),
                         comment.repeatable_);
          }
        }
      } break;
      case Comment::LOCATION: {
        if (fixed_point) {
          PortFunctionName(fixed_point);
        }
      } break;
      case Comment::ANTERIOR: {
        const std::string existing_comment = GetLineComments(address, -1);
        if (existing_comment.rfind(comment.comment_) == std::string::npos) {
          describe(static_cast<ea_t>(address), true, "%s",
                   comment.comment_.c_str());
        }
      } break;
      case Comment::POSTERIOR: {
        const std::string existing_comment = GetLineComments(address, +1);
        if (existing_comment.rfind(comment.comment_) == std::string::npos) {
          describe(static_cast<ea_t>(address), false, "%s",
                   comment.comment_.c_str());
        }
      } break;
      case Comment::GLOBALREFERENCE: {
        int count = 0;
        xrefblk_t xb;
        for (bool ok = xb.first_from(static_cast<ea_t>(address), XREF_DATA); ok;
             ok = xb.next_from(), ++count) {
          if (count == operand_id - UA_MAXOP - 1024) {
            char current_name[MAXSTR];
            get_name(BADADDR, xb.to, current_name, MAXSTR);
            if (strcmp(current_name, comment.comment_.c_str()) != 0) {
              set_name(xb.to, comment.comment_.c_str(), SN_NOWARN | SN_CHECK);
            }
            break;
          }
        }
      } break;
      case Comment::LOCALREFERENCE: {
        func_t* function = get_func(static_cast<ea_t>(address));
        if (!function) break;

        struc_t* frame = get_frame(function);
        if (!frame) break;

        for (int operand_num = 0; operand_num < UA_MAXOP; ++operand_num) {
          const ea_t offset = calc_stkvar_struc_offset(
              function, static_cast<ea_t>(address), operand_num);
          if (offset == BADADDR) {
            continue;
          }

          if (operand_num == operand_id - UA_MAXOP - 2048) {
            set_member_name(frame, offset, comment.comment_.c_str());
          }
        }
      } break;
      case Comment::STRUCTURE: {
        /*
        tid_t id = 0;
        adiff_t disp = 0;
        adiff_t delta = 0;
        if (get_struct_operand(address, operand_num, &id, &disp, &delta)) {
          // Bug: this must be recursive for nested structs
          if (const struc_t* structure = get_struc(id)) {
            set_struc_name(structure->id, comment.m_Comment.c_str());
          }
          // TODO: structure members
        } */
      } break;
      default:
        LOG(INFO) << "Unknown comment type " << comment.type_ << ": "
                  << StringPrintf(HEX_ADDRESS, source) << " -> "
                  << StringPrintf(HEX_ADDRESS, target) << " "
                  << i->second.comment_;
        break;
    }
  }
  return comment_count;
}

size_t SetComments(FixedPoint* fixed_point, const Comments& comments,
                   Address start_source, Address end_source,
                   Address start_target, Address end_target,
                   double min_confidence, double min_similarity) {
  // we call SetComments three times here which potentially sets a single
  // comment multiple times. We have to do this however, because we are
  // iterating over fixedpoints and might miss comments otherwise.
  // i.e. we may have a function fixed point but no corresponding instruction
  // fixed point for the function's entry point address
  size_t counts = 0;
  Address source = fixed_point->GetSecondary()->GetEntryPointAddress();
  Address target = fixed_point->GetPrimary()->GetEntryPointAddress();
  fixed_point->SetCommentsPorted(true);

  if (source >= start_source && source <= end_source &&
      target >= start_target && target <= end_target) {
    if (fixed_point->GetConfidence() >= min_confidence &&
        fixed_point->GetSimilarity() >= min_similarity) {
      counts += SetComments(source, target, comments, fixed_point);
      counts += PortFunctionName(fixed_point);
    } else {
      // Skip whole function if similarity or confidence criteria aren't
      // satisfied.
      return counts;
    }
  }

  auto source_vertex = fixed_point->GetSecondary()->GetVertex(source);
  auto target_vertex = fixed_point->GetPrimary()->GetVertex(target);
  const BasicBlockFixedPoints& basic_block_fixed_points =
      fixed_point->GetBasicBlockFixedPoints();
  for (auto j = basic_block_fixed_points.begin();
       j != basic_block_fixed_points.end(); ++j) {
    if (source_vertex != j->GetSecondaryVertex() ||
        target_vertex != j->GetPrimaryVertex()) {
      source_vertex = j->GetSecondaryVertex();
      target_vertex = j->GetPrimaryVertex();
      source = fixed_point->GetSecondary()->GetAddress(source_vertex);
      target = fixed_point->GetPrimary()->GetAddress(target_vertex);
      if (source >= start_source && source <= end_source &&
          target >= start_target && target <= end_target) {
        counts += SetComments(source, target, comments);
      }
    }

    const InstructionMatches& instruction_matches = j->GetInstructionMatches();
    for (auto k = instruction_matches.begin(); k != instruction_matches.end();
         ++k) {
      const Address target_address = k->first->GetAddress();
      const Address source_address = k->second->GetAddress();
      if (source != source_address || target != target_address) {
        source = source_address;
        target = target_address;
        source_vertex = fixed_point->GetSecondary()->GetVertex(source);
        target_vertex = fixed_point->GetPrimary()->GetVertex(target);
        if (source >= start_source && source <= end_source &&
            target >= start_target && target <= end_target) {
          counts += SetComments(source, target, comments);
        }
      }
    }
  }
  return counts;
}

uint32_t idaapi GetNumFixedPoints(void* /* unused */) {
  if (!g_results) {
    return 0;
  }
  return g_results->GetNumFixedPoints();
}

void DoVisualDiff(void*, uint32_t index, bool call_graph_diff) {
  try {
    std::string message;
    if (!g_results) {
      return;
    }
    if (!call_graph_diff) {
      if (!g_results->PrepareVisualDiff(index, &message)) {
        return;
      }
    } else {
      if (!g_results->PrepareVisualCallGraphDiff(index, &message)) {
        return;
      }
    }

    LOG(INFO) << "Sending result to BinDiff GUI...";
    SendGuiMessage(
        g_config.ReadInt("/BinDiffDeluxe/Gui/@retries", 20),
        g_config.ReadString("/BinDiffDeluxe/Gui/@directory",
                            "C:\\Program Files\\zynamics\\BinDiff 4.3\\bin"),
        g_config.ReadString("/BinDiffDeluxe/Gui/@server", "127.0.0.1"),
        static_cast<unsigned short>(
            g_config.ReadInt("/BinDiffDeluxe/Gui/@port", 2000)),
        message, nullptr);
  } catch (const std::bad_alloc&) {
    LOG(INFO)
        << "Out-of-memory. Please try again with more memory available. Some "
           "extremely large binaries may require a 64bit version of BinDiff - "
           "please contact zynamics to request one.";
    warning(
        "Out-of-memory. Please try again with more memory available. Some "
        "extremely large binaries\nmay require a 64bit version of BinDiff - "
        "please contact zynamics to request one.");
  } catch (const std::runtime_error& message) {
    LOG(INFO) << "Error while calling BinDiff GUI:" << message.what();
    warning("Error while calling BinDiff GUI: %s\n", message.what());
  } catch (...) {
    LOG(INFO) << "Unknown error while calling BinDiff GUI";
    warning("Unknown error while calling BinDiff GUI\n");
  }
}

void idaapi VisualDiffCallback(void*, uint32_t index) {
  DoVisualDiff(nullptr, index, false /* No call graph diff */);
}

void idaapi VisualCallGraphDiffCallback(void*, uint32_t index) {
  DoVisualDiff(nullptr, index, false /* Call graph diff */);
}

uint32_t idaapi PortCommentsSelectionAsLib(void* /* unused */, uint32_t index) {
  try {
    return g_results->PortComments(index, true /* as_external */);
  } catch (const std::exception& message) {
    LOG(INFO) << "Error: " << message.what();
    warning("Error: %s\n", message.what());
  } catch (...) {
    LOG(INFO) << "Unknown error while porting comments";
    warning("Unknown error while porting comments\n");
  }
  return 0;
}

uint32_t idaapi PortCommentsSelection(void* /* unused */, uint32_t index) {
  try {
    return g_results->PortComments(index, false /* as_external */);
  } catch (const std::exception& message) {
    LOG(INFO) << "Error: " << message.what();
    warning("Error: %s\n", message.what());
  } catch (...) {
    LOG(INFO) << "Unknown error while porting comments";
    warning("Unknown error while porting comments\n");
  }
  return 0;
}

uint32_t idaapi ConfirmMatch(void* /* unused */, uint32_t index) {
  try {
    return g_results->ConfirmMatch(index);
  } catch (const std::exception& message) {
    LOG(INFO) << "Error: " << message.what();
    warning("Error: %s\n", message.what());
  } catch (...) {
    LOG(INFO) << "Unknown error while confirming match";
    warning("Unknown error while confirming match\n");
  }
  return 0;
}

uint32_t idaapi CopyPrimaryAddress(void* /* unused */, uint32_t index) {
  if (!g_results) return 0;

  return g_results->CopyPrimaryAddress(index);
}

uint32_t idaapi CopySecondaryAddress(void* /* unused */, uint32_t index) {
  if (!g_results) return 0;

  return g_results->CopySecondaryAddress(index);
}

uint32_t idaapi CopyPrimaryAddressUnmatched(void* /* unused */, uint32_t index) {
  if (!g_results) return 0;

  return g_results->CopyPrimaryAddressUnmatched(index);
}

uint32_t idaapi CopySecondaryAddressUnmatched(void* /* unused */, uint32_t index) {
  if (!g_results) return 0;

  return g_results->CopySecondaryAddressUnmatched(index);
}

uint32_t idaapi GetNumUnmatchedPrimary(void* /* unused */) {
  if (!g_results) return 0;

  return g_results->GetNumUnmatchedPrimary();
}

uint32_t idaapi GetNumUnmatchedSecondary(void* /* unused */) {
  if (!g_results) {
    return 0;
  }
  return g_results->GetNumUnmatchedSecondary();
}

void idaapi jumpToUnmatchedPrimaryAddress(void* /* unused */, uint32_t index) {
  if (!g_results) {
    return;
  }
  jumpto(static_cast<ea_t>(g_results->GetPrimaryAddress(index)));
}

void idaapi GetUnmatchedPrimaryDescription(void* /* unused */, uint32_t index,
                                           char* const* line) {
  if (!g_results) {
    return;
  }
  g_results->GetUnmatchedDescriptionPrimary(index, line);
}

void idaapi GetUnmatchedSecondaryDescription(void* /* unused */, uint32_t index,
                                             char* const* line) {
  if (!g_results) {
    return;
  }
  g_results->GetUnmatchedDescriptionSecondary(index, line);
}

uint32_t idaapi GetNumStatistics(void* /* unused */) {
  if (!g_results) {
    return 0;
  }
  return g_results->GetNumStatistics();
}

void idaapi GetStatisticsDescription(void* /* unused */, uint32_t index,
                                     char* const* line) {
  if (!g_results) {
    return;
  }
  g_results->GetStatisticsDescription(index, line);
}

void idaapi GetMatchDescription(void* /* unused */, uint32_t index,
                                char* const* line) {
  if (!g_results) {
    return;
  }
  g_results->GetMatchDescription(index, line);
}

uint32_t idaapi DeleteMatch(void* /* unused */, uint32_t index) {
  if (!g_results) {
    return 0;
  }

  try {
    return g_results->DeleteMatch(index);
  } catch (const std::exception& message) {
    LOG(INFO) << "Error: " << message.what();
    warning("Error: %s\n", message.what());
  } catch (...) {
    LOG(INFO) << "Unknown error while deleting match";
    warning("Unknown error while deleting match\n");
  }
  return 0;
}

uint32_t idaapi AddMatchPrimary(void* /* unused */, uint32_t index) {
  if (!g_results) return 0;

  try {
    WaitBox wait_box("Performing basicblock diff...");
    return g_results->AddMatchPrimary(index);
  } catch (const std::exception& message) {
    LOG(INFO) << "Error: " << message.what();
    warning("Error: %s\n", message.what());
  } catch (...) {
    LOG(INFO) << "Unknown error while adding match";
    warning("Unknown error while adding match\n");
  }
  return 0;
}

uint32_t idaapi AddMatchSecondary(void* /* unused */, uint32_t index) {
  if (!g_results) return 0;

  try {
    WaitBox wait_box("Performing basicblock diff...");
    return g_results->AddMatchSecondary(index);
  } catch (const std::exception& message) {
    LOG(INFO) << "Error: " << message.what();
    warning("Error: %s\n", message.what());
  } catch (...) {
    LOG(INFO) << "Unknown error while adding match";
    warning("Unknown error while adding match\n");
  }
  return 0;
}

void idaapi jumpToMatchAddress(void* /* unused */, uint32_t index) {
  if (!g_results) return;

  jumpto(static_cast<ea_t>(g_results->GetMatchPrimaryAddress(index)));
}

// Need to close forms if we are calling choose2 from askUsingForm_c callback
// and the choose2 window is still open. If we don't close it first IDA hangs
// in a deadlock.
void CloseForm(const char* name) {
  TForm* form = find_tform(name);
  if (form) {
    close_tform(form, FORM_SAVE);
  }
}

void SaveAndDiscardResults() {
  if (g_results && g_results->IsDirty()) {
    const int answer(askyn_c(0,
                             "HIDECANCEL\nCurrent diff results have not been"
                             " saved - save before closing?"));
    if (answer == 1) {  // Yes
      DoSaveResults();
    }
  }

  delete g_results;
  g_results = 0;
}

int idaapi ProcessorHook(void*, int event_id, va_list /*arguments*/) {
  switch (event_id) {
    case processor_t::term: {
      if (!(database_flags & DBFL_KILL) && g_results) {
        g_results->MarkPortedCommentsInDatabase();
      }
    } break;
    case processor_t::closebase: {
      SaveAndDiscardResults();
    } break;
  }
  return 0;
}

int idaapi UiHook(void*, int eventId, va_list arguments) {
  switch (eventId) {
    case ui_get_chooser_item_attrs: {
      if (g_results) {
        // pop user supplied void* from arguments first (but ignore, since
        // this may go stale)
        if (reinterpret_cast<uint32_t>(va_arg(arguments, void*)) != 0x00000001) {
          // Magic value so we don't color IDA's windows.
          return 0;
        }
        const uint32_t index = va_arg(arguments, uint32_t);
        if (chooser_item_attrs_t* attributes =
                va_arg(arguments, chooser_item_attrs_t*)) {
          attributes->color = g_results->GetColor(index);
        }
      }
    } break;
    case ui_preprocess: {
      const char* name(va_arg(arguments, const char*));
      if (strcmp(name, "LoadFile") == 0 || strcmp(name, "NewFile") == 0 ||
          strcmp(name, "CloseBase") == 0 || strcmp(name, "Quit") == 0) {
        if (g_results && g_results->IsDirty()) {
          const int answer(askyn_c(0,
                                   "Current diff results have not been saved -"
                                   " save before closing?"));
          if (answer == 1) {
            // Yes
            DoSaveResults();
          } else if (answer == -1) {
            // Cancel
            // Return that we handled the command ourselves, this will
            // not even show IDA's close dialog
            return 1;
          }
        }
        // delete old results if they weren't dirty of if the user
        // saved/discarded them
        delete g_results;
        g_results = 0;
        // refreshing the choosers here is important as after our
        // "save results?" confirmation dialog IDA will open its own
        // confirmation. The user can cancel in that dialog. So a "bad"
        // sequence of events is: don't save diff results, but cancel the
        // closing operation in the following IDA dialog. In an ideal world
        // the diff results would be back. As it is we lose the results but
        // at least leave windows/internal data structures in a consistent
        // state.
        refresh_chooser("Matched Functions");
        refresh_chooser("Primary Unmatched");
        refresh_chooser("Secondary Unmatched");
        refresh_chooser("Statistics");
      }
    } break;
  }
  return 0;
}

void ShowResults(Results* results, const ResultFlags flags = kResultsShowAll) {
  if (!results || results != g_results) {
    throw std::runtime_error("trying to show invalid results");
  }

  results->CreateIndexedViews();

  if (flags & kResultsShowMatched) {
    if (!find_tform("Matched Functions")) {
      static const int widths[] = {5, 3, 3, 10, 30, 10, 30, 1, 30,
                                   5, 5, 5, 6,  6,  6,  5,  5, 5};
      static const char* popups[] =  // insert, delete, edit, refresh
          {"Delete Match", "Delete Match", "View Flowgraphs", "Refresh"};
      CloseForm("Matched Functions");
      choose2(CH_MULTI | CH_ATTRS, -1, -1, -1, -1,
              // Magic value to differentiate our window from IDA's.
              reinterpret_cast<void*>(0x00000001),
              static_cast<int>(sizeof(widths) / sizeof(widths[0])), widths,
              &GetNumFixedPoints, &GetMatchDescription, "Matched Functions",
              -1,                      // icon
              static_cast<uint32_t>(1),  // default
              &DeleteMatch,            // delete callback
              0,                       // insert callback
              0,                       // update callback
              &VisualDiffCallback,     // edit callback (visual diff)
              &jumpToMatchAddress,     // enter callback (jump to)
              nullptr,                 // destroy callback
              popups,                  // popups (insert, delete, edit, refresh)
              0);

      // not currently implemented/used
      // add_chooser_command( "Matched Functions", "View Call graphs",
      //    &VisualCallGraphDiffCallback, -1, -1, CHOOSER_POPUP_MENU );
      // @bug: IDA will forget the selection state if CHOOSER_MULTI_SELECTION is
      //       set. See fogbugz 2946 and my mail to hex rays.
      // @bug: IDA doesn't allow adding hotkeys for custom menu entries. See
      //       fogbugz 2945 and my mail to hex rays.
      add_chooser_command("Matched Functions", "Import Symbols and Comments",
                          &PortCommentsSelection, -1, -1,
                          CHOOSER_POPUP_MENU | CHOOSER_MULTI_SELECTION);

      add_chooser_command("Matched Functions",
                          "Import Symbols and Comments as external lib",
                          &PortCommentsSelectionAsLib, -1, -1,
                          CHOOSER_POPUP_MENU | CHOOSER_MULTI_SELECTION);

      add_chooser_command("Matched Functions", "Confirm Match", &ConfirmMatch,
                          -1, -1, CHOOSER_POPUP_MENU | CHOOSER_MULTI_SELECTION);
#ifdef WIN32
      add_chooser_command("Matched Functions", "Copy Primary Address",
                          &CopyPrimaryAddress, -1, -1, CHOOSER_POPUP_MENU);
      add_chooser_command("Matched Functions", "Copy Secondary Address",
                          &CopySecondaryAddress, -1, -1, CHOOSER_POPUP_MENU);
#endif
    } else {
      refresh_chooser("Matched Functions");
    }
  }

  if (flags & kResultsShowStatistics) {
    if (!find_tform("Statistics")) {
      static const int widths[] = {30, 12};
      static const char* popups[] = {0, 0, 0, 0};
      CloseForm("Statistics");
      choose2(0, -1, -1, -1, -1, static_cast<void*>(0),
              static_cast<int>(sizeof(widths) / sizeof(widths[0])), widths,
              &GetNumStatistics, &GetStatisticsDescription, "Statistics",
              -1,                      // icon
              static_cast<uint32_t>(1),  // default
              0,                       // delete callback
              0,                       // new callback
              0,                       // update callback
              0,                       // edit callback
              0,                       // enter callback
              nullptr,                 // destroy callback
              popups,                  // popups (insert, delete, edit, refresh)
              0);
    } else {
      refresh_chooser("Statistics");
    }
  }

  if (flags & kResultsShowPrimaryUnmatched) {
    if (!find_tform("Primary Unmatched")) {
      static const int widths[] = {10, 30, 5, 6, 5};
      static const char* popups[] = {0, 0, 0, 0};
      CloseForm("Primary Unmatched");
      choose2(0, -1, -1, -1, -1, static_cast<void*>(0),
              sizeof(widths) / sizeof(widths[0]), widths,
              &GetNumUnmatchedPrimary, &GetUnmatchedPrimaryDescription,
              "Primary Unmatched", -1,         // icon
              1,                               // default
              0,                               // delete callback
              0,                               // new callback
              0,                               // update callback
              0,                               // edit callback
              &jumpToUnmatchedPrimaryAddress,  // enter callback
              nullptr,                         // destroy callback
              popups,  // popups (insert, delete, edit, refresh)
              0);

      add_chooser_command("Primary Unmatched", "Add Match", &AddMatchPrimary,
                          -1, -1, CHOOSER_POPUP_MENU);
#ifdef WIN32
      add_chooser_command("Primary Unmatched", "Copy Address",
                          &CopyPrimaryAddressUnmatched, -1, -1,
                          CHOOSER_POPUP_MENU);
#endif
    } else {
      refresh_chooser("Primary Unmatched");
    }
  }

  if (flags & kResultsShowSecondaryUnmatched) {
    if (!find_tform("Secondary Unmatched")) {
      static const int widths[] = {10, 30, 5, 6, 5};
      static const char* popups[] = {0, 0, 0, 0};
      CloseForm("Secondary Unmatched");
      choose2(0, -1, -1, -1, -1, static_cast<void*>(0),
              sizeof(widths) / sizeof(widths[0]), widths,
              &GetNumUnmatchedSecondary, &GetUnmatchedSecondaryDescription,
              "Secondary Unmatched", -1,  // icon
              1,                          // default
              0,                          // delete callback
              0,                          // new callback
              0,                          // update callback
              0,                          // edit callback
              0,                          // enter callback
              nullptr,                    // destroy callback
              popups,  // popups (insert, delete, edit, refresh)
              0);

      add_chooser_command("Secondary Unmatched", "Add Match",
                          &AddMatchSecondary, -1, -1, CHOOSER_POPUP_MENU);
#ifdef WIN32
      add_chooser_command("Secondary Unmatched", "Copy Address",
                          &CopySecondaryAddressUnmatched, -1, -1,
                          CHOOSER_POPUP_MENU);
#endif
    } else {
      refresh_chooser("Secondary Unmatched");
    }
  }
}

// Forward Declaration
void idaapi PluginRun(int /**/);

bool idaapi MenuItemShowMainWindowCallback(void* /* unused */) {
  PluginRun(0);
  return true;
}

bool idaapi MenuItemShowResultsCallback(void* user_data) {
  if (!g_results) {
    vinfo("Please perform a diff first", 0);
    return false;
  }

  const ResultFlags flags =
      static_cast<ResultFlags>(reinterpret_cast<int>(user_data));
  ShowResults(g_results, flags);
  return true;
}

// deletes all nodes from callgraph (and corresponding flow graphs) that are
// not within the specified range
void FilterFunctions(ea_t start, ea_t end, CallGraph* call_graph,
                     FlowGraphs* flow_graphs,
                     FlowGraphInfos* flow_graph_infos) {
  for (auto i = flow_graphs->begin(); i != flow_graphs->end();) {
    const FlowGraph* flow_graph = *i;
    if (flow_graph->GetEntryPointAddress() < start ||
        flow_graph->GetEntryPointAddress() > end) {
      flow_graph_infos->erase(flow_graph->GetEntryPointAddress());
      delete flow_graph;
      // GNU implementation of erase does not return an iterator.
      flow_graphs->erase(i++);
    } else {
      ++i;
    }
  }
  call_graph->DeleteVertices(start, end);
}

bool Diff(ea_t start_address_source, ea_t end_address_source,
          ea_t start_address_target, ea_t end_address_target) {
  Timer<> timer;
  try {
    if (!ExportIdbs()) {
      return false;
    }
  } catch (...) {
    delete g_results;
    g_results = 0;
    throw;
  }

  LOG(INFO) << StringPrintf("%.2fs", timer.elapsed())
            << " seconds for exports...";
  LOG(INFO) << "Diffing address range primary("
            << StringPrintf(HEX_ADDRESS, start_address_source) << " - "
            << StringPrintf(HEX_ADDRESS, end_address_source)
            << ") vs secondary("
            << StringPrintf(HEX_ADDRESS, start_address_target) << " - "
            << StringPrintf(HEX_ADDRESS, end_address_target) << ").";
  timer.restart();

  WaitBox wait_box("Performing diff...");
  g_results = new Results();
  const auto temp_dir(GetTempDirectory("BinDiff", /* create = */ true));
  const auto filename1(FindFile(JoinPath(temp_dir, "primary"), ".BinExport"));
  const auto filename2(FindFile(JoinPath(temp_dir, "secondary"), ".BinExport"));
  if (filename1.empty() || filename2.empty()) {
    throw std::runtime_error(
        "Export failed. Is the secondary idb opened in another IDA instance? "
        "Please close all other IDA instances and try again.");
  }

  Read(filename1, &g_results->call_graph1_, &g_results->flow_graphs1_,
       &g_results->flow_graph_infos1_, &g_results->instruction_cache_);
  Read(filename2, &g_results->call_graph2_, &g_results->flow_graphs2_,
       &g_results->flow_graph_infos2_, &g_results->instruction_cache_);
  MatchingContext context(g_results->call_graph1_, g_results->call_graph2_,
                          g_results->flow_graphs1_, g_results->flow_graphs2_,
                          g_results->fixed_points_);
  FilterFunctions(start_address_source, end_address_source,
                  &context.primary_call_graph_, &context.primary_flow_graphs_,
                  &g_results->flow_graph_infos1_);
  FilterFunctions(
      start_address_target, end_address_target, &context.secondary_call_graph_,
      &context.secondary_flow_graphs_, &g_results->flow_graph_infos2_);

  const MatchingSteps default_callgraph_steps(GetDefaultMatchingSteps());
  const MatchingStepsFlowGraph default_basicblock_steps(
      GetDefaultMatchingStepsBasicBlock());
  Diff(&context, default_callgraph_steps, default_basicblock_steps);
  LOG(INFO) << StringPrintf("%.2fs", timer.elapsed())
            << " seconds for matching.";

  ShowResults(g_results);
  g_results->SetDirty();

  return true;
}

bool DoRediffDatabase() {
  try {
    if (!g_results) {
      warning(
          "You need to provide a normal diff before diffing incrementally. "
          "Either create or load one. Diffing incrementally will keep all "
          "manually confirmed matches in the result and try to reassign all "
          "other matches.");
      return false;
    }
    const bool result = g_results->IncrementalDiff();
    ShowResults(g_results);
    return result;
  } catch (const std::bad_alloc&) {
    LOG(INFO)
        << "Out-of-memory. Please try again with more memory available. Some "
           "extremely large binaries may require a 64bit version of BinDiff -"
           " please contact zynamics to request one.";
    warning(
        "Out-of-memory. Please try again with more memory available. Some "
        "extremely large binaries\nmay require a 64bit version of BinDiff -"
        " please contact zynamics to request one.");
  } catch (const std::exception& message) {
    LOG(INFO) << "Error while diffing: " << message.what();
    warning("Error while diffing: %s\n", message.what());
  } catch (...) {
    LOG(INFO) << "Unknown error while diffing.";
    warning("Unknown error while diffing.");
  }
  return false;
}

bool DoDiffDatabase(bool filtered) {
  try {
    if (g_results && g_results->IsDirty()) {
      const int answer =
          askyn_c(0,
                  "HIDECANCEL\nCurrent diff results have not been saved - save "
                  "before closing?");
      if (answer == 1) {  // yes
        DoSaveResults();
      } else if (answer == -1) {  // cancel
        return false;
      }
    }

    // Default to full address range
    ea_t start_address_source = 0;
    ea_t end_address_source = std::numeric_limits<ea_t>::max() - 1;
    ea_t start_address_target = 0;
    ea_t end_address_target = std::numeric_limits<ea_t>::max() - 1;

    if (filtered) {
      static const char kDialog[] =
          "STARTITEM 0\n"
          "Diff Database Filtered\n"
          "Specify address ranges to diff (default: all)\n\n"
          "  <Start address (primary)      :$::16::>\n"
          "  <End address (primary):$::16::>\n"
          "  <Start address (secondary):$::16::>\n"
          "  <End address (secondary):$::16::>\n\n";

      if (!AskUsingForm_c(kDialog, &start_address_source, &end_address_source,
                          &start_address_target, &end_address_target)) {
        return false;
      }
    }
    return Diff(start_address_source, end_address_source, start_address_target,
                end_address_target);
  } catch (const std::bad_alloc&) {
    LOG(INFO)
        << "Out-of-memory. Please try again with more memory available. Some "
           "extremely large binaries may require a 64bit version of BinDiff -"
           " please contact zynamics to request one.";
    warning(
        "Out-of-memory. Please try again with more memory available. Some "
        "extremely large binaries\nmay require a 64bit version of BinDiff -"
        " please contact zynamics to request one.");
  } catch (const std::exception& message) {
    LOG(INFO) << "Error while diffing: " << message.what();
    warning("Error while diffing: %s\n", message.what());
  } catch (...) {
    LOG(INFO) << "Unknown error while diffing.";
    warning("Unknown error while diffing.");
  }
  return false;
}

bool idaapi MenuItemDiffDatabaseFilteredCallback(void* /* unused */) {
  // Refresh screen on successful diff
  return DoDiffDatabase(/* filtered = */ true);
}

void idaapi ButtonDiffDatabaseFilteredCallback(TView* fields[], int) {
  if (DoDiffDatabase(/* filtered = */ true)) {
    close_form(fields, 1);
  }
}

bool idaapi MenuItemDiffDatabaseCallback(void* /* unused */) {
  // Refresh screen on successful diff
  return DoDiffDatabase(/* filtered = */ false);
}

void idaapi ButtonDiffDatabaseCallback(TView* fields[], int) {
  if (DoDiffDatabase(/* filtered = */ false)) {
    close_form(fields, 1);
  }
}

void idaapi ButtonRediffDatabaseCallback(TView* fields[], int) {
  if (DoRediffDatabase()) {
    close_form(fields, 1);
  }
}

bool DoPortComments() {
  if (!g_results) {
    vinfo("Please perform a diff first", 0);
    return false;
  }

  try {
    static const char kDialog[] =
        "STARTITEM 0\n"
        "Import Symbols and Comments\n"
        "Address range (default: all)\n\n"
        "  <Start address (source):$::16::>\n"
        "  <End address (source):$::16::>\n"
        "  <Start address (target):$::16::>\n"
        "  <End address (target):$::16::>\n\n"
        "Minimum confidence required (default: none)\n\n"
        "  <confidence:A::16::>\n\n"
        "Minimum similarity required (default: none)\n\n"
        "  <similarity:A::16::>\n\n";

    // Default to full address range.
    ea_t start_address_source = 0;
    ea_t end_address_source = std::numeric_limits<ea_t>::max() - 1;
    ea_t start_address_target = 0;
    ea_t end_address_target = std::numeric_limits<ea_t>::max() - 1;
    char buffer1[MAXSTR];
    memset(buffer1, 0, MAXSTR);
    buffer1[0] = '0';
    char buffer2[MAXSTR];
    memset(buffer2, 0, MAXSTR);
    buffer2[0] = '0';
    if (!AskUsingForm_c(kDialog, &start_address_source, &end_address_source,
                        &start_address_target, &end_address_target, buffer1,
                        buffer2)) {
      return false;
    }

    Timer<> timer;
    const double min_confidence = std::stod(buffer1);
    const double min_similarity = std::stod(buffer2);
    g_results->PortComments(start_address_source, end_address_source,
                            start_address_target, end_address_target,
                            min_confidence, min_similarity);
    refresh_chooser("Matched Functions");
    refresh_chooser("Primary Unmatched");
    LOG(INFO) << StringPrintf("%.2fs", timer.elapsed())
              << " seconds for comment porting.";
    return true;
  } catch (const std::bad_alloc&) {
    LOG(INFO)
        << "Out-of-memory. Please try again with more memory available. Some "
           "extremely large binaries may require a 64bit version of BinDiff -"
           " please contact zynamics to request one.";
    warning(
        "Out-of-memory. Please try again with more memory available. Some "
        "extremely large binaries\nmay require a 64bit version of BinDiff -"
        " please contact zynamics to request one.");
  } catch (const std::exception& message) {
    LOG(INFO) << "Error while porting comments: " << message.what();
    warning("Error while porting comments: %s\n", message.what());
  } catch (...) {
    LOG(INFO) << "Unknown error while porting comments.";
    warning("Unknown error while porting comments.");
  }
  return false;
}

bool idaapi MenuItemPortCommentsCallback(void* /* unused */) {
  // Refresh screen if user did not cancel.
  return DoPortComments();
}

void idaapi ButtonPortCommentsCallback(TView* fields[], int) {
  if (DoPortComments()) {
    close_form(fields, 1);
  }
}

bool WriteResults(const std::string& path) {
  if (FileExists(path)) {
    if (askyn_c(0, "File\n'%s'\nalready exists - overwrite?",
                path.c_str()) != 1) {
      return false;
    }
  }

  WaitBox wait_box("Writing results...");
  Timer<> timer;
  LOG(INFO) << "Writing results...";
  std::string export1(g_results->call_graph1_.GetFilePath());
  std::string export2(g_results->call_graph2_.GetFilePath());
  const std::string temp_dir =
      GetTempDirectory("BinDiff", /* create = */ true);
  const std::string out_dir(Dirname(path));

  if (!g_results->IsInComplete()) {
    DatabaseWriter writer(path);
    g_results->Write(writer);
  } else {
    // results are incomplete (have been loaded)
    // copy original result file to temp dir first, so we can overwrite the
    // original if required
    std::remove(JoinPath(temp_dir, "input.BinDiff").c_str());
    CopyFile(g_results->input_filename_, JoinPath(temp_dir, "input.BinDiff"));
    {
      SqliteDatabase database(JoinPath(temp_dir, "input.BinDiff").c_str());
      DatabaseTransmuter writer(database, g_results->fixed_point_infos_);
      g_results->Write(writer);
    }
    std::remove(path.c_str());
    CopyFile(JoinPath(temp_dir, "input.BinDiff"), path);
    std::remove(JoinPath(temp_dir, "input.BinDiff").c_str());
  }
  std::string new_export1(JoinPath(out_dir, Basename(export1)));
  if (export1 != new_export1) {
    std::remove(new_export1.c_str());
    CopyFile(export1, new_export1);
  }
  std::string new_export2(JoinPath(out_dir, Basename(export2)));
  if (export2 != new_export2) {
    std::remove(new_export2.c_str());
    CopyFile(export2, new_export2);
  }

  LOG(INFO) << "done (" << StringPrintf("%.2fs", timer.elapsed())
            << ").";
  return true;
}

bool DoSaveResultsLog() {
  if (!g_results) {
    vinfo("Please perform a diff first", 0);
    return false;
  }
  if (g_results->IsInComplete()) {
    vinfo("Saving to log is not supported for loaded results", 0);
    return false;
  }

  const std::string default_filename(
      g_results->call_graph1_.GetFilename() + "_vs_" +
      g_results->call_graph2_.GetFilename() + ".results");
  const char* filename = askfile2_c(
      /* forsave = */ true, default_filename.c_str(),
      StrCat("BinDiff Result Log files|*.results|All files", kAllFilesFilter)
          .c_str(),
      "Save Log As");
  if (!filename) {
    return false;
  }

  if (FileExists(filename) && (askyn_c(0, "File exists - overwrite?") != 1)) {
    return false;
  }

  WaitBox wait_box("Writing results...");
  Timer<> timer;
  LOG(INFO) << "Writing to log...";
  ResultsLogWriter writer(filename);
  g_results->Write(writer);
  LOG(INFO) << "done (" << StringPrintf("%.2fs", timer.elapsed()) << ").";

  return true;
}

void idaapi ButtonSaveResultsLogCallback(TView* [], int) { DoSaveResultsLog(); }

bool DoSaveResultsDebug() {
  if (!g_results) {
    vinfo("Please perform a diff first", 0);
    return false;
  }

  const std::string default_filename(
      g_results->call_graph1_.GetFilename() + "_vs_" +
      g_results->call_graph2_.GetFilename() + ".truth");
  const char* filename = askfile2_c(
      /* forsave = */ true, default_filename.c_str(),
      StrCat("Groundtruth files|*.truth|All files|", kAllFilesFilter).c_str(),
      "Save Groundtruth As");
  if (!filename) {
    return false;
  }

  if (FileExists(filename) && (askyn_c(0, "File exists - overwrite?") != 1)) {
    return false;
  }

  WaitBox wait_box("Writing results...");
  Timer<> timer;
  LOG(INFO) << "Writing to debug ground truth file...";
  FortknoxWriter writer(filename, g_results->fixed_point_infos_,
                        g_results->flow_graph_infos1_,
                        g_results->flow_graph_infos2_);
  g_results->Write(writer);
  LOG(INFO) << "done (" << StringPrintf("%.2fs", timer.elapsed()) << ").";

  return true;
}

void idaapi ButtonSaveResultsDebugCallback(TView* [], int) {
  DoSaveResultsDebug();
}

bool DoSaveResults() {
  if (!g_results) {
    vinfo("Please perform a diff first.", 0);
    return false;
  }

  try {
    // TODO(soerenme) figure out how to use m_InputFileName from g_results if
    // we have loaded results.
    // It seems the filechooser will only ever use the directory part of the
    // default filename _or_ the filename part. I want both!
    // (Hex-Rays has confirmed this to be a bug and promised to fix it in 6.2)
    std::string default_filename(
        g_results->call_graph1_.GetFilename() + "_vs_" +
        g_results->call_graph2_.GetFilename() + ".BinDiff");
    const char* filename = askfile2_c(
        /* forsave = */ true, default_filename.c_str(),
        StrCat("BinDiff Result files|*.BinDiff|All files", kAllFilesFilter)
            .c_str(),
        "Save Results As");
    if (!filename) {
      return false;
    }

    WriteResults(filename);

    return true;
  } catch (...) {
    LOG(INFO) << "Error writing results.";
    warning("Error writing results.\n");
  }
  return false;
}

bool idaapi MenuItemSaveResultsCallback(void* /* unused */) {
  // Refresh screen if user did not cancel
  return DoSaveResults();
}

void idaapi ButtonSaveResultsCallback(TView* [], int) { DoSaveResults(); }

bool DoLoadResults() {
  try {
    if (g_results && g_results->IsDirty()) {
      const int answer = askyn_c(
          0, "Current diff results have not been saved - save before closing?");
      if (answer == 1) {  // yes
        DoSaveResults();
      } else if (answer == -1) {  // cancel
        return false;
      }
    }

    const char* filename = askfile2_c(
        /* forsave = */ false, "*.BinDiff",
        StrCat("BinDiff Result files|*.BinDiff|All files|", kAllFilesFilter)
            .c_str(),
        "Load Results");
    if (!filename) {
      return false;
    }

    std::string path(Dirname(filename));

    LOG(INFO) << "Loading results...";
    WaitBox wait_box("Loading results...");
    Timer<> timer;

    delete g_results;
    g_results = new Results();

    const std::string temp_dir(
        GetTempDirectory("BinDiff", /* create = */ true));

    SqliteDatabase database(filename);
    DatabaseReader reader(database, filename, temp_dir);
    g_results->Read(reader);

    // See b/27371897.
    const std::string hash(EncodeHex(Sha1(GetDataForHash())));
    if (ToUpper(hash) != ToUpper(g_results->call_graph1_.GetExeHash())) {
      LOG(INFO) << "Warning: currently loaded IDBs input file MD5 differs from "
                   "result file primary graph. Please load IDB for: "
                << g_results->call_graph1_.GetExeFilename();
      throw std::runtime_error(StrCat(
          "loaded IDB must match primary graph in results file. Please load "
          "IDB for: ",
          g_results->call_graph1_.GetExeFilename()));
    }

    ShowResults(g_results);

    LOG(INFO) << "done (" << StringPrintf("%.2fs", timer.elapsed()) << ").";
    return true;
  } catch (const std::bad_alloc&) {
    LOG(INFO) << "Out-of-memory. Please try again with more memory available. "
                 "Some extremely large binaries may require the 64-bit "
                 "command-line version of BinDiff";
    warning(
        "\nOut-of-memory. Please try again with more memory available.\nSome "
        "extremely large binaries may require the 64-bit command-line version "
        "of BinDiff");
  } catch (const std::exception& message) {
    LOG(INFO) << "Error loading results: " << message.what();
    warning("Error loading results: %s\n", message.what());
  } catch (...) {
    LOG(INFO) << "Error loading results.";
    warning("Error loading results.");
  }
  delete g_results;
  g_results = 0;
  return false;
}

bool idaapi MenuItemLoadResultsCallback(void* /* unused */) {
  // Refresh screen if user did not cancel
  return DoLoadResults();
}

void idaapi ButtonLoadResultsCallback(TView* fields[], int /* unused */) {
  if (DoLoadResults()) {
    close_form(fields, 1);
  }
}

void InitConfig() {
  const std::string config_filename("bindiff.xml");

  const std::string user_path(
      GetDirectory(PATH_APPDATA, "BinDiff", /* create = */ true) +
      config_filename);
  const std::string common_path(
      GetDirectory(PATH_COMMONAPPDATA, "BinDiff", /* create = */ false) +
      config_filename);

  bool have_user_config;
  bool have_common_config;
  XmlConfig user_config;
  XmlConfig common_config;

  // Try to read user's local config
  try {
    user_config.Init(user_path, std::string("BinDiffDeluxe"));
    user_config.SetSaveFileName("");  // Prevent saving in destructor
    have_user_config = true;
  } catch (const std::runtime_error&) {
    have_user_config = false;
  }

  // Try to read machine config
  try {
    common_config.Init(common_path, std::string("BinDiffDeluxe"));
    common_config.SetSaveFileName("");  // Prevent saving in destructor
    have_common_config = true;
  } catch (const std::runtime_error&) {
    have_common_config = false;
  }

  bool use_common_config = false;
  if (have_user_config && have_common_config) {
    use_common_config =
        user_config.ReadInt("/BinDiffDeluxe/@configVersion", 0) <
        common_config.ReadInt("/BinDiffDeluxe/@configVersion", 0);
  } else if (have_user_config) {
    use_common_config = false;
  } else if (have_common_config) {
    use_common_config = true;
  }

  if (use_common_config) {
    XmlConfig::SetDefaultFilename(common_path);
    g_config.Init(common_path, "BinDiffDeluxe");
    std::remove(user_path.c_str());
    g_config.SetSaveFileName(user_path);
  } else {
    XmlConfig::SetDefaultFilename(user_path);
    g_config.Init(user_path, "BinDiffDeluxe");
  }
}

void InitMenus() {
  add_menu_item("File/Produce file", "~D~iff Database...", "SHIFT-D",
                SETMENU_APP, MenuItemDiffDatabaseCallback, 0);

  add_menu_item("Edit/Comments/Insert predefined comment...",
                "Im~p~ort Symbols and Comments...", "", SETMENU_APP,
                MenuItemPortCommentsCallback, 0);

  // Different paths are used for the GUI version
  const bool result = add_menu_item("File/Load file/Additional binary file...",
                                    "~B~inDiff Results...", "", SETMENU_APP,
                                    MenuItemLoadResultsCallback, 0);
  if (!result) {
    add_menu_item("File/Save", "~B~inDiff Results...", "", SETMENU_INS,
                  MenuItemLoadResultsCallback, 0);
  }

  add_menu_item("File/Produce file/Create callgraph GDL...",
                "Save ~B~inDiff Results...", "", SETMENU_INS,
                MenuItemSaveResultsCallback, 0);

  // Adding this item to be able to use the hotkey in every view, not just in
  // the main assembly view
  add_menu_item("View/Open subviews/Problems", "BinDiff Main Window", "CTRL-6",
                SETMENU_APP, MenuItemShowMainWindowCallback, 0);

  add_menu_item("View/Open subviews/BinDiff Main Window",
                "BinDiff Matched Functions", "", SETMENU_APP,
                MenuItemShowResultsCallback,
                reinterpret_cast<void*>(kResultsShowMatched));
  add_menu_item("View/Open subviews/BinDiff Matched Functions",
                "BinDiff Statistics", "", SETMENU_APP,
                MenuItemShowResultsCallback,
                reinterpret_cast<void*>(kResultsShowStatistics));
  add_menu_item("View/Open subviews/BinDiff Statistics",
                "BinDiff Primary Unmatched", "", SETMENU_APP,
                MenuItemShowResultsCallback,
                reinterpret_cast<void*>(kResultsShowPrimaryUnmatched));
  add_menu_item("View/Open subviews/BinDiff Primary Unmatched",
                "BinDiff Secondary Unmatched", "", SETMENU_APP,
                MenuItemShowResultsCallback,
                reinterpret_cast<void*>(kResultsShowSecondaryUnmatched));
}

int idaapi PluginInit() {
  LoggingOptions options;
  options.set_alsologtostderr(ToUpper(GetArgument("AlsoLogToStdErr")) ==
                              "TRUE");
  options.set_log_filename(GetArgument("LogFile"));
  if (!InitLogging(options)) {
    LOG(INFO) << "Error initializing logging, skipping BinDiff plugin";
    return PLUGIN_SKIP;
  }

  LOG(INFO) << kProgramVersion << " (" << __DATE__
#ifdef _DEBUG
            << ", debug build"
#endif
            << "), " << kCopyright;

  addon_info_t addon_info;
  addon_info.cb = sizeof(addon_info_t);
  addon_info.id = "com.google.bindiff";
  addon_info.name = kName;
  addon_info.producer = "Google";
  addon_info.version = BINDIFF_MAJOR "." BINDIFF_MINOR "." BINDIFF_PATCH;
  addon_info.url = "https://zynamics.com/bindiff.html";
  addon_info.freeform = kCopyright;
  register_addon(&addon_info);

  if (!hook_to_notification_point(HT_IDP, ProcessorHook,
                                  nullptr /* User data */) ||
      !hook_to_notification_point(HT_UI, UiHook, nullptr /* User data */)) {
    LOG(INFO) << "hook_to_notification_point error";
    return PLUGIN_SKIP;
  }

  try {
    InitConfig();
  } catch (const std::runtime_error&) {
    LOG(INFO)
        << "Error: Could not load configuration file, skipping BinDiff plugin.";
    return PLUGIN_SKIP;
  }
  InitMenus();

  g_init_done = true;
  return PLUGIN_KEEP;
}

void TermMenus() {
  del_menu_item("File/Diff Database...");
  del_menu_item("Edit/Comments/Import Symbols and Comments...");
  del_menu_item("File/Load file/BinDiff Results...");
  del_menu_item("File/Save/BinDiff Results...");
  del_menu_item("File/Produce file/Save BinDiff Results...");
  del_menu_item("View/Open subviews/BinDiff Main Window");
  del_menu_item("View/Open subviews/BinDiff Matched Functions");
  del_menu_item("View/Open subviews/BinDiff Statistics");
  del_menu_item("View/Open subviews/BinDiff Primary Unmatched");
  del_menu_item("View/Open subviews/BinDiff Secondary Unmatched");
}

void idaapi PluginTerminate() {
  unhook_from_notification_point(HT_UI, UiHook, nullptr /* User data */);
  unhook_from_notification_point(HT_IDP, ProcessorHook,
                                 nullptr /* User data */);

  if (!g_init_done) {
    ShutdownLogging();
    return;
  }

  TermMenus();
  SaveAndDiscardResults();

  ShutdownLogging();
}

void idaapi PluginRun(int /* arg */) {
  static const std::string kDialogBase = StrCat(
      "STARTITEM 0\n"
      "BUTTON YES Close\n"  // This is actually the OK button
      "BUTTON CANCEL NONE\n"
      "HELP\n"
      "'Diff Database...' diff the currently open IDB against another one "
      "chosen via a file chooser dialog. Please note that the secondary IDB "
      "file must be readable for the BinDiff plugin, i.e. it must not be "
      "opened in another instance of IDA."
      "\n\n"
      "'Diff Database Filtered...' diff specific address ranges of the "
      "selected databases. You must manually specify a section of the primary "
      "IDB to compare against a section of the secondary IDB. This is useful "
      "for comparing only the non library parts of two executables."
      "\n\n"
      "'Load Results...' load a previously saved diff result. The primary IDB "
      "used in that diff must already be open in IDA."
      "ENDHELP\n",
      kProgramVersion,
      "\n\n"
      "<~D~iff Database...:B:1:30::>\n"
      "<D~i~ff Database Filtered...:B:1:30::>\n\n"
      "<L~o~ad Results...:B:1:30::>\n\n");

  static const std::string kDialogResultsAvailable = StrCat(
      "STARTITEM 0\n"
      "BUTTON YES Close\n"  // This is actually the OK button
      "BUTTON CANCEL NONE\n"
      "HELP\n"
      "'Diff Database...' diff the currently open IDB against another one "
      "chosen via a file chooser dialog. Please note that the secondary IDB "
      "file must be readable for the BinDiff plugin, i.e. it must not be "
      "opened in another instance of IDA."
      "\n\n"
      "'Diff Database Filtered...' diff specific address ranges of the "
      "selected databases. You must manually specify a section of the primary "
      "IDB to compare against a section of the secondary IDB. This is useful "
      "for comparing only the non library parts of two executables."
      "\n\n"
      "'Diff Database Incrementally' keep manually confirmed matches (blue "
      "matches with algorithm = 'manual') in the current result and re-match "
      "all others. Thus allowing a partially automated workflow of "
      "continuously improving the diff results."
      "\n\n"
      "'Load Results...' load a previously saved diff result. The primary IDB "
      "used in that diff must already be open in IDA."
      "\n\n"
      "'Save Results...' save the current BinDiff matching to a .BinDiff "
      "result file."
      "\n\n"
      "'Import Symbols and Comments...' copy function names, symbols and "
      "comments from the secondary IDB into the primary IDB for all matched "
      "functions. It is possible to specify a filter so only data for matches "
      "meeting a certain quality threshold or in a certain address range will "
      "be ported."
      "\n\n",
      kProgramVersion,
      "\nCopyright (c)2004-2011 zynamics GmbH\n"
      "\nCopyright (c)2011-2017 Google Inc.\n"
      "ENDHELP\n",
      kProgramVersion,
      "\n\n"
      "<~D~iff Database...:B:1:30::>\n"
      "<D~i~ff Database Filtered...:B:1:30::>\n"
      "<Diff Database Incrementally:B:1:30::>\n\n"
      "<L~o~ad Results...:B:1:30::>\n"
      "<~S~ave Results...:B:1:30::>\n"
#ifdef _DEBUG
      "<Save Results ~F~ortknox...:B:1:30::>\n"
      "<Save Results ~L~og...:B:1:30::>\n"
#endif
      "\n<Im~p~ort Symbols and Comments...:B:1:30::>\n\n");

  if (g_results) {
    // We may have to unload a previous result if the input IDB has changed in
    // the meantime
    const std::string hash(EncodeHex(Sha1(GetDataForHash())));
    if (ToUpper(hash) != ToUpper(g_results->call_graph1_.GetExeHash())) {
      warning("Discarding current results since the input IDB has changed.");

      delete g_results;
      g_results = 0;

      close_chooser("Matched Functions");
      close_chooser("Primary Unmatched");
      close_chooser("Secondary Unmatched");
      close_chooser("Statistics");
    }
  }

  if (!g_results) {
    AskUsingForm_c(kDialogBase.c_str(), ButtonDiffDatabaseCallback,
                   ButtonDiffDatabaseFilteredCallback,
                   ButtonLoadResultsCallback);
  } else {
    AskUsingForm_c(
        kDialogResultsAvailable.c_str(), ButtonDiffDatabaseCallback,
        ButtonDiffDatabaseFilteredCallback, ButtonRediffDatabaseCallback,
        ButtonLoadResultsCallback, ButtonSaveResultsCallback,
#ifdef _DEBUG
        ButtonSaveResultsDebugCallback, ButtonSaveResultsLogCallback,
#endif
        ButtonPortCommentsCallback);
  }
}

extern "C" {

plugin_t PLUGIN = {
    IDP_INTERFACE_VERSION,
    PLUGIN_FIX,       // Flags
    PluginInit,       // Initialize
    PluginTerminate,  // Terminate
    PluginRun,        // Invoke plugin
    kComment,         // Statusline text
    nullptr,          // Multiline help about the plugin, unused
    kName,            // The preferred short name of the plugin
    kHotKey           // The preferred hotkey to run the plugin
};

}  // extern "C"
