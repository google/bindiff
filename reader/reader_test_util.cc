// Copyright 2011-2019 Google LLC. All Rights Reserved.
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

#ifndef GOOGLE  // MOE:strip_line
#include <fstream>

#include <gmock/gmock.h>  // NOLINT
#include <gtest/gtest.h>  // NOLINT

#include "third_party/zynamics/binexport/types.h"
#include "third_party/zynamics/binexport/util/canonical_errors.h"
#include "third_party/zynamics/binexport/util/filesystem.h"
// MOE:begin_strip
#else
#include "file/base/helpers.h"
#include "file/base/options.h"
#include "file/base/path.h"
#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "third_party/absl/flags/flag.h"
#endif
// MOE:end_strip
#include "third_party/absl/strings/str_cat.h"

namespace security {
namespace binexport {

#ifndef GOOGLE  // MOE:strip_line
static std::string* g_test_srcdir{};

not_absl::Status GetBinExportProtoForTesting(absl::string_view filename,
                                             BinExport2* proto) {
  std::string testfile = JoinPath(*g_test_srcdir, "testdata", filename);
  std::ifstream stream(testfile.c_str(), std::ios::in | std::ios::binary);
  if (!proto->ParseFromIstream(&stream)) {
    return not_absl::FailedPreconditionError(
        absl::StrCat("Could not parse test file: ", testfile));
  }
  return not_absl::OkStatus();
}
// MOE:begin_strip
#else
not_absl::Status GetBinExportProtoForTesting(absl::string_view filename,
                                             BinExport2* proto) {
  return not_absl::Status(file::GetBinaryProto(
      file::JoinPath(absl::GetFlag(FLAGS_test_srcdir),
                     "google3/third_party/zynamics/binexport/reader/testdata",
                     filename),
      proto, file::Defaults()));
}
#endif
// MOE:end_strip

}  // namespace binexport
}  // namespace security
// MOE:begin_strip
#ifndef GOOGLE

int main(int argc, char** argv) {
  testing::InitGoogleTest(&argc, argv);
  security::binexport::g_test_srcdir =
      new std::string(argc >= 2 ? argv[1] : GetCurrentDirectory());
  return RUN_ALL_TESTS();
}
#endif
// MOE:end_strip
