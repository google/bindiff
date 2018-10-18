// Copyright 2011-2018 Google LLC. All Rights Reserved.
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

#include "third_party/zynamics/binexport/util/filesystem.h"

#include <gmock/gmock.h>
#include <gtest/gtest.h>
#include "third_party/absl/strings/str_cat.h"

using ::testing::IsFalse;
using ::testing::IsTrue;

namespace security {
namespace binexport {
namespace {

TEST(FileSystemTest, Filenames) {
  EXPECT_EQ("filename.ext",
            Basename(absl::StrCat(kPathSeparator, "subdir", kPathSeparator,
                                  "filename.ext")));

  EXPECT_EQ(absl::StrCat("subdir1", kPathSeparator, "subdir2"),
            Dirname(absl::StrCat("subdir1", kPathSeparator, "subdir2",
                                 kPathSeparator, "filename.ext")));

  EXPECT_EQ(".ext", GetFileExtension(absl::StrCat("subdir", kPathSeparator,
                                                  "filename.ext")));

  EXPECT_EQ(
      absl::StrCat("subdir", kPathSeparator, "filename.new"),
      ReplaceFileExtension(
          absl::StrCat("subdir", kPathSeparator, "filename.ext"), ".new"));
  EXPECT_EQ(
      absl::StrCat("subdir", kPathSeparator, "filename_noext.new"),
      ReplaceFileExtension(
          absl::StrCat("subdir", kPathSeparator, "filename_noext"), ".new"));
}

TEST(FileSystemTest, JoinPaths) {
  EXPECT_EQ(absl::StrCat("a", kPathSeparator, "b"), JoinPath("a", "b"));
#ifndef WIN32
  EXPECT_EQ("/a/b", JoinPath("/a", "b"));
#endif
}

TEST(FileSystemTest, CreateAndRemoveDirectories) {
  auto temp_dir_or = GetOrCreateTempDirectory("test");
  ASSERT_THAT(temp_dir_or.ok(), IsTrue());
  auto temp_dir = std::move(temp_dir_or).ValueOrDie();

  const auto test_path = JoinPath(temp_dir, "sub", "dir", "s2");
  EXPECT_THAT(CreateDirectories(test_path).ok(), IsTrue());

  EXPECT_THAT(RemoveAll(test_path).ok(), IsTrue());
  EXPECT_THAT(IsDirectory(test_path), IsFalse());
}

}  // namespace
}  // namespace binexport
}  // namespace security
