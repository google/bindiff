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

#ifndef UTIL_PROCESS_H_
#define UTIL_PROCESS_H_

#include <string>
#include <vector>

#include "third_party/absl/status/status.h"
#include "third_party/absl/status/statusor.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/zynamics/binexport/types.h"

namespace security::binexport {

// Returns the last OS error as a string.
std::string GetLastOsError();

// Sets the contents of an environment variable for the current process.
// If value is empty, the variable will be unset. Returns true on success.
bool SetEnvironmentVariable(const std::string& name, const std::string& value);

// Spawns a process and waits for it to end.
// argv: A vector with command-line arguments, the first item must be the name
// of the process' image file.
// On success this function returns the exit code of the process if wait was
// true. If wait was false, returns zero on success.
absl::StatusOr<int> SpawnProcessAndWait(const std::vector<std::string>& argv);

// Like SpawnProcessAndWait, but does not wait for the child process to finish.
absl::Status SpawnProcess(const std::vector<std::string>& argv);

// Returns the platform-specific application data directory, which is a
// per-user, writable path. If the directory does not exists, the function tries
// to create it.
// Returns one of these paths when called with "BinDiff":
//   OS       Typical value
//   ------------------------------------------------------
//   Windows  C:\Users\<User>\AppData\Roaming\BinDiff
//            %AppData%\BinDiff
//   Linux    /home/<User>/.bindiff
//   macOS    /Users/<User>/Library/Application Supprt/BinDiff
absl::StatusOr<std::string> GetOrCreateAppDataDirectory(
    absl::string_view product_name);

// Returns the platform-specific per-machine application data directory. This is
// usually a non-writable path.
// Returns one of these paths when called with "BinDiff":
//   OS       Typical value
//   ------------------------------------------------------
//   Windows  C:\ProgramData\BinDiff
//            %ProgramData%\BinDiff
//   Linux    /etc/opt/bindiff
//   macOS    /Library/Application Support/BinDiff
absl::StatusOr<std::string> GetCommonAppDataDirectory(
    absl::string_view product_name);

}  // namespace security::binexport

#endif  // UTIL_PROCESS_H_
