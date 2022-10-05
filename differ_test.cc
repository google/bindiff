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

#include "third_party/zynamics/bindiff/differ.h"

#include <algorithm>
#include <charconv>
#include <cstdint>
#include <vector>

#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "third_party/absl/flags/flag.h"
#include "third_party/absl/log/check.h"
#include "third_party/absl/log/log.h"
#include "third_party/absl/status/status.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/strings/str_split.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/zynamics/bindiff/config.h"
#include "third_party/zynamics/bindiff/groundtruth_writer.h"
#include "third_party/zynamics/bindiff/match/call_graph.h"
#include "third_party/zynamics/bindiff/match/context.h"
#include "third_party/zynamics/bindiff/match/flow_graph.h"
#include "third_party/zynamics/bindiff/reader.h"
#include "third_party/zynamics/bindiff/test_util.h"
#include "third_party/zynamics/binexport/testing.h"
#include "third_party/zynamics/binexport/util/filesystem.h"
#include "third_party/zynamics/binexport/util/timer.h"

namespace security::bindiff {
namespace {

using ::security::binexport::GetTestFileContents;
using ::security::binexport::GetTestSourcePath;
using ::security::binexport::GetTestTempPath;

::testing::Environment* const g_bindiff_env =
    ::testing::AddGlobalTestEnvironment(new BinDiffEnvironment());

using Matches = std::vector<std::pair<uint64_t, uint64_t>>;

Matches ParseMatches(absl::string_view path) {
  Matches matches;
  for (absl::string_view line :
       absl::StrSplit(GetTestFileContents(path), '\n')) {
    if (line.empty()) {
      continue;
    }

    std::pair<absl::string_view, absl::string_view> values =
        absl::StrSplit(line, ' ');

    uint64_t primary = 0;
    QCHECK(
        std::from_chars(values.first.begin(), values.first.end(), primary, 16)
            .ec == std::errc());
    uint64_t secondary = 0;
    QCHECK(std::from_chars(values.second.begin(), values.second.end(),
                           secondary, 16)
               .ec == std::errc());

    matches.emplace_back(primary, secondary);
  }
  QCHECK(std::is_sorted(
      matches.cbegin(), matches.cend(),
      [](const Matches::value_type& first, const Matches::value_type& second) {
        return first.first < second.first;
      }));
  return matches;
}

std::string PaddedStr(absl::string_view s) {
  return absl::StrFormat("  %-24s", s);
}

void CompareToGroundTruth(absl::string_view test_name,
                          absl::string_view result_path,
                          absl::string_view truth_path) {
  Matches result_matches = ParseMatches(result_path);
  Matches true_matches = ParseMatches(truth_path);

  int correct_matches = 0;
  int incorrect_matches = 0;
  int extra_matches = 0;
  int missing_matches = 0;
  for (auto it = result_matches.cbegin(), jt = true_matches.cbegin(),
            it_end = result_matches.cend(), jt_end = true_matches.cend();
       it != it_end || jt != jt_end;) {
    if (it != it_end && jt != jt_end) {
      if (*it == *jt) {  // correct match
        ++correct_matches;
        ++it;
        ++jt;
      } else if (it->first == jt->first) {  // incorrect match
        ++incorrect_matches;
        ++it;
        ++jt;
      } else if (it->first < jt->first) {  // extra match
        ++extra_matches;
        ++it;
      } else if (it->first > jt->first) {  // missing match
        ++missing_matches;
        ++jt;
      } else {
        FAIL() << "invalid case";
      }
    } else if (it == it_end) {  // no more results => missing match
      ++missing_matches;
      ++jt;
    } else if (jt == jt_end) {  // no more truth => extra match
      ++extra_matches;
      ++it;
    } else {
      FAIL() << "invalid case";
    }
  }
  LOG(INFO) << PaddedStr("correct:") << correct_matches;
  LOG(INFO) << PaddedStr("incorrect:") << incorrect_matches;
  LOG(INFO) << PaddedStr("extra:") << extra_matches;
  LOG(INFO) << PaddedStr("missing:") << missing_matches;
}

struct FixtureMetadata {
  FixtureMetadata& set_name(absl::string_view value) {
    name = GetTestSourcePath(value);
    return *this;
  }

  FixtureMetadata& set_primary(absl::string_view value) {
    primary = GetTestSourcePath(value);
    return *this;
  }

  FixtureMetadata& set_secondary(absl::string_view value) {
    secondary = GetTestSourcePath(value);
    return *this;
  }

  FixtureMetadata& set_truth(absl::string_view value) {
    truth = GetTestSourcePath(value);
    return *this;
  }

  std::string name;
  std::string primary;
  std::string secondary;
  std::string truth;
};

class GroundtruthTest
    : public testing::TestWithParam<FixtureMetadata> {};

TEST_P(GroundtruthTest, Run) {
  const FixtureMetadata& meta = GetParam();

  LOG(INFO) << "Testing " << meta.name << " <<<";
  LOG(INFO) << PaddedStr("primary:") << meta.primary;
  LOG(INFO) << PaddedStr("secondary:") << meta.secondary;
  LOG(INFO) << PaddedStr("truth:") << meta.truth;

  const MatchingSteps default_call_graph_steps = GetDefaultMatchingSteps();
  const MatchingStepsFlowGraph default_basicblock_steps =
      GetDefaultMatchingStepsBasicBlock();

  Instruction::Cache instruction_cache;
  CallGraph call_graph1, call_graph2;
  FlowGraphs flow_graphs1, flow_graphs2;
  ScopedCleanup cleanup(&flow_graphs1, &flow_graphs2, &instruction_cache);
  try {
    FlowGraphInfos flow_graph_infos1;
    FlowGraphInfos flow_graph_infos2;

    Timer<> timer;
    Read(meta.primary, &call_graph1, &flow_graphs1, &flow_graph_infos1,
         &instruction_cache);
    const double time_primary = timer.elapsed();
    LOG(INFO) << PaddedStr("time primary:") << time_primary;

    timer.restart();
    Read(meta.secondary, &call_graph2, &flow_graphs2, &flow_graph_infos2,
         &instruction_cache);
    const double time_secondary = timer.elapsed();
    LOG(INFO) << PaddedStr("time secondary:") << time_secondary;

    timer.restart();
    FixedPoints fixed_points;
    MatchingContext context(call_graph1, call_graph2, flow_graphs1,
                            flow_graphs2, fixed_points);
    Diff(&context, default_call_graph_steps, default_basicblock_steps);

    Histogram histogram;
    Counts counts;
    GetCountsAndHistogram(flow_graphs1, flow_graphs2, fixed_points, &histogram,
                          &counts);

    const double similarity =
        GetSimilarityScore(call_graph1, call_graph2, histogram, counts);
    Confidences confidences;
    const double confidence = GetConfidence(histogram, &confidences);
    LOG(INFO) << PaddedStr("diff time:") << timer.elapsed();
    // TODO(cblichmann): Collect maximum amount of memory used
    LOG(INFO) << PaddedStr("max memory:");
    LOG(INFO) << PaddedStr("similarity:") << similarity;
    LOG(INFO) << PaddedStr("confidence:") << confidence;
    LOG(INFO) << PaddedStr("total functions:") << flow_graphs1.size() << " vs "
              << flow_graphs2.size();
    LOG(INFO) << PaddedStr("non-library functions:")
              << counts[Counts::kFunctionsPrimaryNonLibrary] << " vs "
              << counts[Counts::kFunctionsSecondaryNonLibrary];
    LOG(INFO) << PaddedStr("matched functions:")
              << counts[Counts::kFunctionMatchesLibrary] +
                     counts[Counts::kFunctionMatchesNonLibrary];
    LOG(INFO) << PaddedStr("matched basic blocks:")
              << counts[Counts::kBasicBlockMatchesLibrary] +
                     counts[Counts::kBasicBlockMatchesNonLibrary];
    LOG(INFO) << PaddedStr("matched instructions:")
              << counts[Counts::kInstructionMatchesLibrary] +
                     counts[Counts::kInstructionMatchesNonLibrary];
    LOG(INFO) << PaddedStr("matched edges:")
              << counts[Counts::kFlowGraphEdgeMatchesLibrary] +
                     counts[Counts::kFlowGraphEdgeMatchesNonLibrary];
    LOG(INFO) << PaddedStr("call graph MD indices:") << call_graph1.GetMdIndex()
              << " vs " << call_graph2.GetMdIndex();

    const std::string result_path =
        GetTestTempPath(absl::StrCat(call_graph1.GetFilename(), "_vs_",
                                     call_graph2.GetFilename(), ".truth"));
    GroundtruthWriter writer(result_path);
    writer.Write(call_graph1, call_graph2, flow_graphs1, flow_graphs2,
                 fixed_points);
    CompareToGroundTruth(meta.name, result_path, meta.truth);
  } catch (const std::runtime_error& error) {
    FAIL() << meta.name << ": " << error.what();
  } catch (...) {
    FAIL() << meta.name << ": unknown exception";
  }
  LOG(INFO) << ">>>";
}

INSTANTIATE_TEST_SUITE_P(
    GtTest, GroundtruthTest,
    testing::Values(
        FixtureMetadata{}
            .set_name("insider")
            .set_primary("bindiff/fixtures/insider/insider_gcc.BinExport")
            .set_secondary("bindiff/fixtures/insider/insider_lcc.BinExport")
            .set_truth(
                "bindiff/fixtures/insider/insider_gcc_vs_insider_lcc.truth"),
        FixtureMetadata{}
            .set_name("libssl 0.9.8g (x86)")
            .set_primary("bindiff/fixtures/libssl/"
                         "libssl.0.9.8g.x86.gcc.4.3.3.a.BinExport")
            .set_secondary("bindiff/fixtures/libssl/"
                           "libssl.0.9.8g.x86.gcc.3.4.6.a.BinExport")
            .set_truth("bindiff/fixtures/libssl/"
                       "libssl.0.9.8g.x86.gcc.4.3.3.a_vs_libssl.0.9g.x86.gcc.3."
                       "4.6.a.truth"),
        FixtureMetadata{}
            .set_primary("bindiff/fixtures/minievil/"
                         "0d0d06e42bb39a4a8fd1a3da8a9be8d2abaacd4c7373d4cd36cd6"
                         "fac6f4d1650.BinExport")
            .set_secondary("bindiff/fixtures/minievil/"
                           "70726a39fda45d1e0bb167a2bf3825db0960529368a5814c4f4"
                           "3d59b4d585e79.BinExport")
            .set_truth("bindiff/fixtures/minievil/minievil.truth"),
        FixtureMetadata{}
            .set_primary("bindiff/fixtures/mydoom/Mydoom-vc_orig.BinExport")
            .set_secondary("bindiff/fixtures/mydoom/Mydoom-vc_optz.BinExport")
            .set_truth("bindiff/fixtures/mydoom/"
                       "Mydoom-vc_orig_vs_Mydoom-vc_optz.truth")));

}  // namespace
}  // namespace security::bindiff
