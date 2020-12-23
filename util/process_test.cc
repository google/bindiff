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

#include "third_party/zynamics/binexport/util/process.h"

#include <string>
#include <vector>

#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/binexport/util/status_matchers.h"

namespace security::binexport {
namespace {

using ::not_absl::IsOk;
using ::testing::Eq;
using ::testing::IsEmpty;
using ::testing::IsFalse;
using ::testing::Not;

TEST(UtilityTest, SpawnProcessEmptyArgs) {
  auto exit_code_or = SpawnProcessAndWait({});
  EXPECT_THAT(exit_code_or.ok(), IsFalse());
  EXPECT_THAT(exit_code_or.status().message(), Not(IsEmpty()));
}

TEST(UtilityTest, SpawnProcessNonExistingWait) {
  auto exit_code_or = SpawnProcessAndWait({"not.an.executable"});
  EXPECT_THAT(exit_code_or.ok(), IsFalse());
  EXPECT_THAT(exit_code_or.status().message(), Not(IsEmpty()));
}

TEST(UtilityTest, SpawnProcessNonExistingNoWait) {
  auto status = SpawnProcess({"not.an.executable"});
  EXPECT_THAT(status.ok(), IsFalse());
  EXPECT_THAT(status.message(), Not(IsEmpty()));
}

constexpr int kExitCode = 42;

std::vector<std::string> GetSpawnProcessArgs() {
  return {
#ifndef WIN32
      "sh", "-c", absl::StrCat("exit ", kExitCode)
#else
      "cmd.exe", absl::StrCat("/C exit ", kExitCode)
#endif
  };
}

TEST(UtilityTest, SpawnProcessWait) {
  NA_ASSERT_OK_AND_ASSIGN(int exit_code,
                          SpawnProcessAndWait(GetSpawnProcessArgs()));
  EXPECT_THAT(exit_code, Eq(kExitCode));
}

TEST(UtilityTest, SpawnProcessNoWait) {
  ASSERT_THAT(SpawnProcess(GetSpawnProcessArgs()), IsOk());
}

}  // namespace
}  // namespace security::binexport
