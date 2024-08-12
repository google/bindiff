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

#include "third_party/zynamics/bindiff/log_writer.h"

#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "third_party/absl/status/status_matchers.h"
#include "third_party/absl/strings/ascii.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/strings/str_replace.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/zynamics/bindiff/match/call_graph.h"
#include "third_party/zynamics/bindiff/match/flow_graph.h"
#include "third_party/zynamics/bindiff/statistics.h"
#include "third_party/zynamics/bindiff/test_util.h"
#include "third_party/zynamics/binexport/testing.h"
#include "third_party/zynamics/binexport/util/filesystem.h"

namespace security::bindiff {
namespace {

using ::absl_testing::IsOk;
using binexport::GetTestFileContents;
using binexport::GetTestTempPath;
using ::testing::IsTrue;

#ifdef GTEST_USES_POSIX_RE
using ::testing::AllOfArray;
using ::testing::ContainsRegex;
#endif

::testing::Environment* const g_bindiff_env =
    ::testing::AddGlobalTestEnvironment(new BinDiffEnvironment());

class LogWriterTest : public BinDiffTest {
 private:
  void SetUp() override { SetUpBasicFunctionMatch(); }
};

#ifdef GTEST_USES_POSIX_RE
// Converts a regular expression using character classes into a pure POSIX
// one. This is needed because macOS and Linux support different extensions
// to POSIX regexes.
std::string CanonicalizeRegex(absl::string_view s) {
  return absl::StrReplaceAll(s, {{R"(\d)", R"([0-9])"}, {R"(\s)", R"([ \t])"}});
}

std::string RegexEscape(absl::string_view s) {
  return CanonicalizeRegex(
      absl::StrReplaceAll(s, {{R"(()", R"(\()"}, {R"())", R"(\))"}}));
}
#endif

TEST_F(LogWriterTest, Empty) {
  const std::string log = GetTestTempPath("empty.log");
  ResultsLogWriter writer(log);
  EXPECT_THAT(writer.Write(primary_->call_graph, secondary_->call_graph,
                           primary_->flow_graphs, secondary_->flow_graphs,
                           fixed_points_),
              IsOk());

  ASSERT_THAT(FileExists(log), IsTrue());
  const std::string log_output =
      absl::AsciiStrToLower(GetTestFileContents(log));
#ifdef GTEST_USES_POSIX_RE
  constexpr absl::string_view kPaddedValueRegex = R"((\s|\.)*:?\s+\d+(\.\d+)?)";

  // Log should mention the call graph MD indices and overall
  // similarity/confidence. Auto-deduce template argument, as regex matchers are
  // in internal namespace.
  std::vector regexes = {
      ContainsRegex(CanonicalizeRegex(
          absl::StrCat(R"(call graph\s*1\s+md index)", kPaddedValueRegex))),
      ContainsRegex(CanonicalizeRegex(
          absl::StrCat(R"(call graph\s*2\s+md index)", kPaddedValueRegex))),
      ContainsRegex(
          CanonicalizeRegex(absl::StrCat(R"(similarity)", kPaddedValueRegex))),
      ContainsRegex(
          CanonicalizeRegex(absl::StrCat(R"(confidence)", kPaddedValueRegex))),
  };
  EXPECT_THAT(log_output, AllOfArray(regexes));

  // All of the statistics usually shown in the UI should be present.
  regexes.clear();
  for (int i = 0; i < Counts::ui_entry_size(); ++i) {
    regexes.emplace_back(ContainsRegex(CanonicalizeRegex(
        absl::StrCat(RegexEscape(absl::AsciiStrToLower(
                         Counts::GetDisplayName(static_cast<Counts::Kind>(i)))),
                     kPaddedValueRegex))));
  }
  EXPECT_THAT(log_output, AllOfArray(regexes));

  // Same for confidence values.
  regexes.clear();
  for (const auto* step : GetDefaultMatchingSteps()) {
    regexes.emplace_back(ContainsRegex(CanonicalizeRegex(absl::StrCat(
        RegexEscape(absl::AsciiStrToLower(step->name())), kPaddedValueRegex))));
  }
  for (const auto* step : GetDefaultMatchingStepsBasicBlock()) {
    regexes.emplace_back(ContainsRegex(CanonicalizeRegex(absl::StrCat(
        RegexEscape(absl::AsciiStrToLower(step->name())), kPaddedValueRegex))));
  }
  EXPECT_THAT(log_output, AllOfArray(regexes));

  // Verify matches and that everything was matched
  EXPECT_THAT(
      log_output,
      AllOf(
          ContainsRegex(CanonicalizeRegex(R"(matched\s+1\s+of\s+1\s*/\s*1)")),
          ContainsRegex(CanonicalizeRegex(R"(unmatched primary\s+(0|\(0\)))")),
          ContainsRegex(
              CanonicalizeRegex(R"(unmatched secondary\s+(0|\(0\)))"))));
#endif
}

}  // namespace
}  // namespace security::bindiff
