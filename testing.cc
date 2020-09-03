#include "third_party/zynamics/binexport/testing.h"

#include <cstdlib>
#include <fstream>

#include "base/logging.h"
#include "gtest/gtest.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/binexport/util/filesystem.h"

namespace security::binexport {

std::string GetTestTempPath(absl::string_view name) {
  // When using Bazel, the environment variable TEST_TMPDIR is guaranteed to be
  // set. The CMake build should set this up correctly, too.
  // See https://docs.bazel.build/versions/master/test-encyclopedia.html for
  // details.
  const char* test_tmpdir = getenv("TEST_TMPDIR");
  return JoinPath(test_tmpdir ? test_tmpdir : "", name);
}

std::string GetTestSourcePath(absl::string_view name) {
  const char* test_srcdir = getenv("TEST_SRCDIR");
  return JoinPath(test_srcdir ? test_srcdir : "", name);
}

std::string GetTestFileContents(absl::string_view path) {
  std::ifstream in_stream(std::string(path), std::ios_base::binary);
  std::ostringstream out_stream;
  out_stream << in_stream.rdbuf();
  if (!in_stream || !out_stream) {
    LOG(FATAL) << absl::StrCat("Error during read: ", path);
  }
  return out_stream.str();
}

void SetTestFileContents(absl::string_view path, absl::string_view content) {
  std::ofstream out_stream(std::string(path),
                           std::ios_base::trunc | std::ios_base::binary);
  if (!out_stream) {
    LOG(FATAL) << absl::StrCat("Failed to open file: ", path);
  }
  out_stream.write(content.data(), content.size());
  if (!out_stream) {
    LOG(FATAL) << absl::StrCat("Error during write: ", path);
  }
}

BinExport2 GetBinExportForTesting(absl::string_view name) {
  const std::string testfile = GetTestSourcePath(name);
  std::ifstream stream(testfile.c_str(), std::ios::in | std::ios::binary);
  BinExport2 proto;
  if (!proto.ParseFromIstream(&stream)) {
    LOG(FATAL) << absl::StrCat("Could not parse test file: ", testfile);
  }
  return proto;
}

}  // namespace security::binexport
