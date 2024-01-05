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

#ifndef VERSION_H_
#define VERSION_H_

namespace security::bindiff {

// The product name, "BinDiff"
extern const char* kBinDiffName;

// The BinDiff version, a single stringified integer
extern const char* kBinDiffRelease;

// The matching BinExport version
extern const char* kBinDiffBinExportRelease;

// Detailed version and build information:
// "N (Google internal, YYYYMMDD, debug build)"
extern const char* kBinDiffDetailedVersion;

// Full copyright string with current year
extern const char* kBinDiffCopyright;

}  // namespace security::bindiff

#endif  // VERSION_H_
