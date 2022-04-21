// Copyright 2021 Google LLC
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

#include "third_party/zynamics/binexport/util/idb_export.h"

#include <cstdlib>
#include <fstream>
#include <string>

#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "third_party/zynamics/binexport/util/filesystem.h"
#include "third_party/zynamics/binexport/util/status_matchers.h"

namespace security::binexport {
namespace {

using ::not_absl::IsOk;
using ::testing::AnyOf;
using ::testing::IsFalse;
using ::testing::Not;
using ::testing::UnorderedElementsAre;

void AssertEmptyFile(const std::string& filepath) {
  std::ofstream out_stream(filepath,
                           std::ios_base::trunc | std::ios_base::binary);
  ASSERT_THAT(out_stream, Not(IsFalse()));
}

TEST(IdbExportTest, CollectIdbsTest) {
  const char* test_tmpdir = getenv("TEST_TMPDIR");
  const std::string tmp_dir =
      test_tmpdir ? test_tmpdir : "idb_export_test_tmpdir";
  ASSERT_THAT(CreateDirectories(JoinPath(tmp_dir, "sub_dir")), IsOk());
  for (const auto& filename : {
           JoinPath(tmp_dir, "sub_dir", "skip_me.BinExport"),
           JoinPath(tmp_dir, "first.i64"),
           JoinPath(tmp_dir, "first.idb"),
           JoinPath(tmp_dir, "second.IDB"),
           JoinPath(tmp_dir, "third.I64"),
           JoinPath(tmp_dir, "third.BinExport"),
           // Will overwrite the file above on Windows/case-insensitive FS.
           JoinPath(tmp_dir, "third.BiNeXpOrT"),
       }) {
    AssertEmptyFile(filename);
  }

  std::vector<std::string> binexports;
  NA_ASSERT_OK_AND_ASSIGN(std::vector<std::string> idbs,
                          CollectIdbsToExport(tmp_dir, &binexports));
  EXPECT_THAT(idbs, UnorderedElementsAre("first.i64", "second.IDB"));
  // Order of directory listings is unspecified, so both are valid below:
  EXPECT_THAT(binexports, UnorderedElementsAre(
                              AnyOf("third.BinExport", "third.BiNeXpOrT")));
}

}  // namespace
}  // namespace security::binexport
