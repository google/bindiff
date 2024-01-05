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

// First stage launcher for the BinDiff UI.
// This executable just selects and runs the native JVM launcher binary for the
// correct architecture (AArch64 or x86-64) in order to present BinDiff as
// Universal Binary.
// While this can be implemented in a simple shell script, this does not play
// well with macOS' Transparency, Consent, and Control (TCC) mechanism. For
// example, using a shell script launcher will lead to "Operation not permitted"
// errors instead of a permission prompt, when Java code tries to access files
// in protected folders (like "Desktop", "Documents", etc.). This is because the
// originating binary (i.e. "/bin/sh" or similar) will not invoke any UX and
// receive an error unless "Full Disk Access" was granted manually in System
// Preferences.

static_assert(__APPLE__, "This code is intended for macOS only");

#include <crt_externs.h>
#include <mach-o/dyld.h>
#include <sys/param.h>
#include <unistd.h>

#include <cerrno>
#include <cstdint>
#include <cstdlib>
#include <cstring>
#include <iostream>  // NOLINT
#include <string>

#include "third_party/zynamics/binexport/util/filesystem.h"

namespace security::bindiff {

std::string GetProgramPath() {
  uint32_t buf_size = MAXPATHLEN + 1;
  std::string path(buf_size, '\0');
  if (_NSGetExecutablePath(&path[0], &buf_size) < 0) {
    // Buffer was too small; try again with the size required.
    path.resize(buf_size, '\0');
    _NSGetExecutablePath(&path[0], &buf_size);
  }
  // The string in path_buf is zero terminated.
  path.resize(strlen(path.c_str()));
  return path;
}

}  // namespace security::bindiff

int main(int argc, char* argv[]) {
  using security::bindiff::GetProgramPath;

#if defined(__arm64__)
  constexpr char kNativeLauncherBasename[] = "BinDiff-arm64";
#elif defined(__x86_64__)
  constexpr char kNativeLauncherBasename[] = "BinDiff-x86_64";
#else
  static_assert(false, "Unsupported architecture: must be AArch64 or x86-64");
#endif
  const std::string native_jvm_launcher =
      JoinPath(Dirname(GetProgramPath()), kNativeLauncherBasename);
  execve(native_jvm_launcher.c_str(), argv, *_NSGetEnviron());
  // If we end up here, execve() failed, report this.
  std::cerr << "ERROR: Executing second stage launcher '" << native_jvm_launcher
            << "' :" << strerror(errno) << "\n";
  return EXIT_FAILURE;
}
