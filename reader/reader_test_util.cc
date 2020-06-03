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

#include "third_party/zynamics/binexport/reader/reader_test_util.h"

#include <fstream>

#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "third_party/absl/status/status.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/binexport/types.h"
#include "third_party/zynamics/binexport/util/filesystem.h"

namespace security::binexport {

static std::string* g_test_srcdir{};

absl::Status GetBinExportProtoForTesting(absl::string_view filename,
                                         BinExport2* proto) {
  const std::string testfile = JoinPath(*g_test_srcdir, "testdata", filename);

  std::ifstream stream(testfile.c_str(), std::ios::in | std::ios::binary);
  if (!proto->ParseFromIstream(&stream)) {
    return absl::FailedPreconditionError(
        absl::StrCat("Could not parse test file: ", testfile));
  }
  return absl::OkStatus();
}

}  // namespace security::binexport

int main(int argc, char** argv) {
  testing::InitGoogleTest(&argc, argv);
  security::binexport::g_test_srcdir =
      new std::string(argc >= 2 ? argv[1] : GetCurrentDirectory());
  return RUN_ALL_TESTS();
}
