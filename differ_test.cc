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

using ::testing::IsTrue;

namespace security::bindiff {
namespace {

static constexpr const char kFixturesPath[] =
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
              << static_cast<int>(counts["function matches (library)"] +
                                  counts["function matches (non-library)"]);
    LOG(INFO) << absl::StrCat(name, ".matched_basicblocks")
              << static_cast<int>(counts["basicBlock matches (library)"] +
                                  counts["basicBlock matches (non-library)"]);
    LOG(INFO) << absl::StrCat(name, ".matched_instructions")
              << static_cast<int>(counts["instruction matches (library)"] +
                                  counts["instruction matches (non-library)"]);
    LOG(INFO) << absl::StrCat(name, ".matched_edges")
              << static_cast<int>(
                     counts["flowGraph edge matches (library)"] +
                     counts["flowGraph edge matches (non-library)"]);

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
              << counts.find("functions primary (non-library)")->second << "/"
              << counts.find("functions secondary (non-library)")->second << ")"
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

class GroundtruthIntegrationTest : public ::testing::Test {
 protected:
  static void SetUpTestSuite() { ApplyDefaultConfigForTesting(); }
};

TEST_F(GroundtruthIntegrationTest, Run) {
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
