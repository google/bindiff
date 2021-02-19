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

#ifndef VERSION_H_
#define VERSION_H_

namespace security::binexport {

// The product name, "BinExport"
extern const char* kBinExportName;

// The BinExport release number. A single stringified integer.
extern const char* kBinExportRelease;

// Detailed version and build information:
// "N (@cafec0d, YYYYMMDD, debug build)"
extern const char* kBinExportDetailedVersion;

// Full copyright string with current year
extern const char* kBinExportCopyright;

}  // namespace security::binexport

#endif  // VERSION_H_
