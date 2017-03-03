// Copyright 2011-2017 Google Inc. All Rights Reserved.
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

#include "third_party/zynamics/binexport/filesystem_util.h"

#include <gtest/gtest.h>
#include "strings/strutil.h"

TEST(FileSystemUtilTest, Filenames) {
  EXPECT_EQ("filename.ext", Basename(StrCat(kPathSeparator, "subdir",
                                            kPathSeparator, "filename.ext")));

  EXPECT_EQ(StrCat("subdir1", kPathSeparator, "subdir2"),
            Dirname(StrCat("subdir1", kPathSeparator, "subdir2", kPathSeparator,
                           "filename.ext")));

  EXPECT_EQ(".ext",
            GetFileExtension(StrCat("subdir", kPathSeparator, "filename.ext")));

  EXPECT_EQ(StrCat("subdir", kPathSeparator, "filename.new"),
            ReplaceFileExtension(
                StrCat("subdir", kPathSeparator, "filename.ext"), ".new"));
  EXPECT_EQ(StrCat("subdir", kPathSeparator, "filename_noext.new"),
            ReplaceFileExtension(
                StrCat("subdir", kPathSeparator, "filename_noext"), ".new"));
}

TEST(FileSystemUtilTest, JoinPaths) {
  EXPECT_EQ(StrCat("a", kPathSeparator, "b"), JoinPath("a", "b"));
#ifndef WIN32
  EXPECT_EQ("/a/b", JoinPath("/a", "b"));
#endif
}
