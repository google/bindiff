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

#include "third_party/zynamics/bindiff/differ.h"

#include <algorithm>
#include <iomanip>
#include <vector>

#include "base/commandlineflags.h"
#include "base/sysinfo.h"
#include "base/timer.h"
#include "file/base/file.h"
#include "file/base/filelinereader.h"
#include "file/base/helpers.h"
#include "file/base/path.h"
#include "file/util/temp_path.h"
#include "net/proto2/public/text_format.h"
#include "strings/numbers.h"
#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "third_party/absl/flags/flag.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/zynamics/bindiff/bindiff.pb.h"
#include "third_party/zynamics/bindiff/call_graph_match.h"
#include "third_party/zynamics/bindiff/config.h"
#include "third_party/zynamics/bindiff/flow_graph_match.h"
#include "third_party/zynamics/bindiff/groundtruth_writer.h"
#include "third_party/zynamics/bindiff/match_context.h"
#include "third_party/zynamics/bindiff/test_util.h"
#include "util/task/status.h"

namespace security::bindiff {
namespace {

using ::testing::IsTrue;

::testing::Environment* const g_bindiff_env =
    ::testing::AddGlobalTestEnvironment(new BinDiffEnvironment());

constexpr absl::string_view kFixturesPath =
    "/google3/third_party/zynamics/bindiff/fixtures/";

using Matches = std::vector<std::pair<uint64_t, uint64_t>>;

void MatchParser(Matches* matches, char* line) {
  ABSL_DIE_IF_NULL(matches)->emplace_back(
      strings::ParseLeadingHex64Value(line, 0),
      strings::ParseLeadingHex64Value(strchr(line, ' '), 0));
}

bool IsSorted(const Matches& matches) {
  return std::is_sorted(
      matches.cbegin(), matches.cend(),
      [](const Matches::value_type& first, const Matches::value_type& second) {
        return first.first < second.first;
      });
}

void CompareToGroundTruth(const std::string& test_name,
                          const std::string& result_path,
                          const std::string& truth_path) {
  Matches result_matches;
  Matches true_matches;
  QCHECK(ParseFileByLines(result_path,
                          NewPermanentCallback(&MatchParser, &result_matches)));
  QCHECK(IsSorted(result_matches));
  QCHECK(ParseFileByLines(truth_path,
                          NewPermanentCallback(&MatchParser, &true_matches)));
  QCHECK(IsSorted(true_matches));

  int correct_matches = 0;
  int incorrect_matches = 0;
  int extra_matches = 0;
  int missing_matches = 0;
  for (auto i = result_matches.begin(), j = true_matches.begin(),
            iend = result_matches.end(), jend = true_matches.end();
       i != iend || j != jend;) {
    if (i != iend && j != jend) {
      if (*i == *j) {  // correct match
        ++correct_matches;
        ++i;
        ++j;
      } else if (i->first == j->first) {  // incorrect match
        ++incorrect_matches;
        ++i;
        ++j;
      } else if (i->first < j->first) {  // extra match
        ++extra_matches;
        ++i;
      } else if (i->first > j->first) {  // missing match
        ++missing_matches;
        ++j;
      } else {
        LOG(QFATAL) << "invalid case";
      }
    } else if (i == iend) {  // no more results => missing match
      ++missing_matches;
      ++j;
    } else if (j == jend) {  // no more truth => extra match
      ++extra_matches;
      ++i;
    } else {
      LOG(QFATAL) << "invalid case";
    }
  }
  LOG(INFO) << "correct: " << correct_matches
            << ", incorrect: " << incorrect_matches
            << ", extra: " << extra_matches << ", missing: " << missing_matches;

  LOG(INFO) << absl::StrCat(test_name, ".groundtruth_correct")
            << correct_matches;
  LOG(INFO) << absl::StrCat(test_name, ".groundtruth_incorrect")
            << incorrect_matches;
  LOG(INFO) << absl::StrCat(test_name, ".groundtruth_extra") << extra_matches;
  LOG(INFO) << absl::StrCat(test_name, ".groundtruth_missing")
            << missing_matches;
}

void Diff(const std::string& name, const std::string& primary_path,
          const std::string& secondary_path, const std::string& truth_path) {
  LOG(INFO) << "'" << name << "': '" << primary_path << "' vs '"
            << secondary_path << "' -> '" << truth_path << "'";

  const MatchingSteps default_call_graph_steps(GetDefaultMatchingSteps());
  const MatchingStepsFlowGraph default_basicblock_steps(
      GetDefaultMatchingStepsBasicBlock());

  Instruction::Cache instruction_cache;
  CallGraph call_graph1, call_graph2;
  FlowGraphs flow_graphs1, flow_graphs2;
  ScopedCleanup cleanup(&flow_graphs1, &flow_graphs2, &instruction_cache);
  try {
    FlowGraphInfos flow_graph_infos1, flow_graph_infos2;

    WallTimer timer;
    timer.Start();
    Read(primary_path, &call_graph1, &flow_graphs1, &flow_graph_infos1,
         &instruction_cache);
    const double time_primary = timer.Get();
    LOG(INFO) << absl::StrCat(name, ".time_primary") << time_primary;

    timer.Restart();
    Read(secondary_path, &call_graph2, &flow_graphs2, &flow_graph_infos2,
         &instruction_cache);
    const double time_secondary = timer.Get();
    LOG(INFO) << absl::StrCat(name, ".time_secondary") << time_secondary;

    LOG(INFO) << std::fixed << std::setw(7) << std::setprecision(3)
              << std::setfill(' ') << time_primary << " seconds"
              << " read '" << primary_path << "' '"
              << call_graph1.GetExeFilename() << "' " << std::fixed
              << std::setw(7) << std::setprecision(3) << std::setfill(' ')
              << time_secondary << " seconds"
              << " read '" << secondary_path << "' '"
              << call_graph2.GetExeFilename() << "'";

    timer.Restart();
    FixedPoints fixed_points;
    MatchingContext context(call_graph1, call_graph2, flow_graphs1,
                            flow_graphs2, fixed_points);
    Diff(&context, default_call_graph_steps, default_basicblock_steps);

    Histogram histogram;
    Counts counts;
    GetCountsAndHistogram(flow_graphs1, flow_graphs2, fixed_points, &histogram,
                          &counts);

    Confidences confidences;
    const double confidence = GetConfidence(histogram, &confidences);
    const double similarity =
        GetSimilarityScore(call_graph1, call_graph2, histogram, counts);
    LOG(INFO) << absl::StrCat(name, ".time_diff") << timer.Get();
    LOG(INFO) << absl::StrCat(name, ".diff_memory") << MemoryUsageForExport();
    LOG(INFO) << absl::StrCat(name, ".diff_similarity") << similarity;
    LOG(INFO) << absl::StrCat(name, ".diff_confidence") << confidence;
    LOG(INFO) << absl::StrCat(name, ".matched_functions")
              << static_cast<int>(counts[Counts::kFunctionMatchesLibrary] +
                                  counts[Counts::kFunctionMatchesNonLibrary]);
    LOG(INFO) << absl::StrCat(name, ".matched_basicblocks")
              << static_cast<int>(counts[Counts::kBasicBlockMatchesLibrary] +
                                  counts[Counts::kBasicBlockMatchesNonLibrary]);
    LOG(INFO) << absl::StrCat(name, ".matched_instructions")
              << static_cast<int>(
                     counts[Counts::kInstructionMatchesLibrary] +
                     counts[Counts::kInstructionMatchesNonLibrary]);
    LOG(INFO) << absl::StrCat(name, ".matched_edges")
              << static_cast<int>(
                     counts[Counts::kFlowGraphEdgeMatchesLibrary] +
                     counts[Counts::kFlowGraphEdgeMatchesNonLibrary]);

    LOG(INFO) << std::fixed << std::setw(7) << std::setprecision(3)
              << std::setfill(' ') << timer.Get() << " seconds"
              << " similarity: " << std::fixed << std::setw(6)
              << std::setprecision(3) << std::setfill(' ')
              << (similarity * 100.0) << "%"
              << " confidence: " << std::fixed << std::setw(6)
              << std::setprecision(3) << std::setfill(' ')
              << (confidence * 100.0) << "%"
              << " matched " << fixed_points.size() << " of "
              << flow_graphs1.size() << "/" << flow_graphs2.size() << " ("
              << counts[Counts::kFunctionsPrimaryNonLibrary] << "/"
              << counts[Counts::kFunctionsSecondaryNonLibrary] << ")"
              << " call graph1 MD index " << std::fixed << std::setprecision(3)
              << call_graph1.GetMdIndex() << " call graph2 MD index "
              << std::fixed << std::setprecision(3) << call_graph2.GetMdIndex();

    if (!truth_path.empty()) {
      TempPath path(TempPath::Local);
      const std::string result_path(
          absl::StrCat(path.path(), call_graph1.GetFilename().c_str(), "_vs_",
                       call_graph2.GetFilename().c_str(), ".truth"));
      GroundtruthWriter writer(result_path);
      writer.Write(call_graph1, call_graph2, flow_graphs1, flow_graphs2,
                   fixed_points);
      CompareToGroundTruth(name, result_path, truth_path);
    }
  } catch (const std::runtime_error& error) {
    LOG(ERROR) << "error: '" << call_graph1.GetExeFilename() << "' vs '"
               << call_graph2.GetExeFilename() << "' ('" << primary_path
               << "' vs '" << secondary_path << "')\n" << error.what();
  } catch (...) {
    LOG(ERROR) << "error: '" << call_graph1.GetExeFilename() << "' vs '"
               << call_graph2.GetExeFilename() << "' ('" << primary_path
               << "' vs '" << secondary_path << "')\n"
               << "unknown exception, skipping";
  }
}

TEST(GroundtruthIntegrationTest, Run) {
  std::string buffer;
  CHECK_OK(
      file::GetContents(file::JoinPath(absl::GetFlag(FLAGS_test_srcdir),
                                       kFixturesPath, "groundtruth_tests.list"),
                        &buffer, file::Defaults()))
      << "Failed opening groundtruth_tests.list";
  BinDiff::TestPackage test_set;
  ASSERT_THAT(proto2::TextFormat::ParseFromString(buffer, &test_set), IsTrue());

  for (const auto& test : test_set.test()) {
    Diff(test.name(),
         file::JoinPath(absl::GetFlag(FLAGS_test_srcdir), kFixturesPath,
                        test.primary_item_path()),
         file::JoinPath(absl::GetFlag(FLAGS_test_srcdir), kFixturesPath,
                        test.secondary_item_path()),
         test.truth_file_path().empty()
             ? ""
             : file::JoinPath(absl::GetFlag(FLAGS_test_srcdir), kFixturesPath,
                              test.truth_file_path()));
  }

  // TODO(cblichmann): This used to store the results to Fortknox. Store in
  //                   Monarch instead.
}

}  // namespace
}  // namespace security::bindiff
