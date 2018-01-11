// A set of benchmarks for BinDiff performance. To get a baseline on your local
// machine, run with the command line below (using the local data directory
// instead of the default SrcFs one).
/**
blaze run third_party/zynamics/bindiff:differ_benchmark -c opt -- \
--alsologtostderr \
--data_path=/google/src/cloud/soerenme/clean/google3/third_party/zynamics/bindiff/fixtures
*/

#include <iomanip>

#include "base/commandlineflags.h"
#include "base/init_google.h"
#include "base/logging.h"
#include "base/sysinfo.h"
#include "base/time.h"
#include "base/timer.h"
#include "file/base/helpers.h"
#include "file/base/options.h"
#include "file/base/path.h"
#include "strings/human_readable.h"
#include "strings/substitute.h"
#include "third_party/absl/time/time.h"
#include "third_party/zynamics/bindiff/bindiff.proto.h"
#include "third_party/zynamics/bindiff/call_graph_matching.h"
#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/bindiff/flow_graph_matching.h"
#include "util/task/status.h"

DEFINE_FLAG(string, data_path,
            "/google_src/files/head/depot/google3/third_party/zynamics/bindiff/"
            "fixtures",
            "Path of the BinDiff text fixtures to use (should contain a "
            "groundtruth_tests.list text proto file).");

namespace {

void Diff(const string& primary_path, const string& secondary_path) {
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
    ReadGoogle(primary_path, &call_graph1, &flow_graphs1, &flow_graph_infos1,
               &instruction_cache);
    const double time_primary = timer.Get();
    LOG(INFO) << "primary:   "
              << HumanReadableElapsedTime::ToShortString(
                     absl::Seconds(time_primary))
              << " " << primary_path;

    timer.Restart();
    ReadGoogle(secondary_path, &call_graph2, &flow_graphs2, &flow_graph_infos2,
               &instruction_cache);
    const double time_secondary = timer.Get();
    LOG(INFO) << "secondary: "
              << HumanReadableElapsedTime::ToShortString(
                     absl::Seconds(time_secondary))
              << " " << secondary_path;

    timer.Restart();
    FixedPoints fixed_points;
    MatchingContext context(call_graph1, call_graph2, flow_graphs1,
                            flow_graphs2, fixed_points);
    Diff(&context, default_call_graph_steps, default_basicblock_steps);
    const double time_diff = timer.Get();

    Histogram histogram;
    Counts counts;
    GetCountsAndHistogram(flow_graphs1, flow_graphs2, fixed_points, &histogram,
                          &counts);

    Confidences confidences;
    const double confidence = GetConfidence(histogram, &confidences);
    const double similarity =
        GetSimilarityScore(call_graph1, call_graph2, histogram, counts);

    LOG(INFO) << strings::Substitute(
        "$0 diffing, similarity $1%, confidence $2%, matched $3 of $4/$5 "
        "($6/$7 non-library)",
        HumanReadableElapsedTime::ToShortString(absl::Seconds(time_diff)),
        similarity * 100.0, confidence * 100.0, fixed_points.size(),
        flow_graphs1.size(), flow_graphs2.size(),
        counts.find("functions primary (non-library)")->second,
        counts.find("functions secondary (non-library)")->second);
    LOG(INFO) << strings::Substitute(
        "primary call graph MD index $0, secondary call graph MD index $1",
        call_graph1.GetMdIndex(), call_graph2.GetMdIndex());
  } catch (const std::runtime_error& error) {
    LOG(ERROR) << "error: " << primary_path << " vs " << secondary_path << "\n"
               << error.what();
  } catch (...) {
    LOG(ERROR) << "error: " << primary_path << " vs " << secondary_path << "\n"
               << "unknown exception.";
  }
}

void RunAllDiffs() {
  const auto& path = base::GetFlag(FLAGS_data_path);

  // TODO(soerenme) Add a set of DEX files to the test. DEX seems to have unique
  //     performance issues.
  BinDiff::TestPackage tests;
  QCHECK_OK(file::GetTextProto(file::JoinPath(path, "groundtruth_tests.list"),
                               &tests, file::Defaults()));

  WallTimer timer;
  timer.Start();
  for (const auto& test : tests.test()) {
    LOG(INFO) << "-------------------------------------------";
    LOG(INFO) << test.name();
    Diff(file::JoinPath(path, test.primary_item_path()),
         file::JoinPath(path, test.secondary_item_path()));
  }
  LOG(INFO) << "Memory usage: "
            << HumanReadableNumBytes::ToString(MemoryUsageForExport());
  LOG(INFO) << "Total time: "
            << HumanReadableElapsedTime::ToShortString(
                   absl::Seconds(timer.Get()));
}

}  // namespace

int main(int argc, char** argv) {
  InitGoogle(argv[0], &argc, &argv, true /* Remove flags */);

  RunAllDiffs();
  return 0;
}
