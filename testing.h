// Copyright 2011-2021 Google LLC
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

#ifndef TESTING_H_
#define TESTING_H_

#include <string>

#include "third_party/absl/status/status.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/zynamics/binexport/binexport2.pb.h"

namespace security::binexport {

// Returns a writable path usable in tests. If the name argument is specified,
// returns a name under that path. This can then be used for creating temporary
// test files and/or directories.
std::string GetTestTempPath(absl::string_view name = {});

// Returns a filename relative to the the root of the source tree. Use this to
// access data files in tests.
std::string GetTestSourcePath(absl::string_view name);

// Returns the contents of a file. Fails the current test on error.
std::string GetTestFileContents(absl::string_view path);

// Writes the given string to a file. Fails the current test on error.
void SetTestFileContents(absl::string_view path, absl::string_view content);

// Returns a parsed BinExport2 proto test fixture. The fixture should be in the
// same directory tree as returned by GetTestSourcePath(). Fails the current
// test on error.
BinExport2 GetBinExportForTesting(absl::string_view name);

}  // namespace security::binexport

#endif  // TESTING_H_
