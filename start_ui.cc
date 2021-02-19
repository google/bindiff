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

#include "third_party/zynamics/bindiff/start_ui.h"

#ifdef _WIN32
#define _WIN32_WINNT 0x0501
#include <windows.h>  // NOLINT
#else
#include <netdb.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>
#ifdef __APPLE__
#include <sys/sysctl.h>
#else
#include <sys/sysinfo.h>  // For sysinfo struct
#endif
#endif

#include <cstdint>
#include <cstdlib>

#include "third_party/absl/status/status.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/strings/str_join.h"
#include "third_party/absl/strings/str_split.h"
#include "third_party/zynamics/binexport/util/filesystem.h"
#include "third_party/zynamics/binexport/util/process.h"

namespace security::bindiff {

using ::security::binexport::SpawnProcess;

std::string GetJavaHomeDir() {
#ifdef _WIN32
  char buffer[MAX_PATH] = {0};

  // Try environment variable first.
  int size = GetEnvironmentVariable("JAVA_HOME", &buffer[0], MAX_PATH);
  if (size != 0 && size < MAX_PATH) {
    return buffer;
  }
#else
  const char* java_home = getenv("JAVA_HOME");
  if (java_home) {
    return java_home;
  }
#endif
  return "";
}

uint64_t GetPhysicalMemSize() {
#if defined(_WIN32)
  MEMORYSTATUSEX mi;
  mi.dwLength = sizeof(MEMORYSTATUSEX);
  GlobalMemoryStatusEx(&mi);
  return mi.ullTotalPhys;
#elif defined(__APPLE__)
  uint64_t result;
  int param[2];
  param[0] = CTL_HW;
  param[1] = HW_MEMSIZE;
  size_t length = sizeof(uint64_t);
  sysctl(param, 2, &result, &length, nullptr, 0);
  return result;
#else
  struct sysinfo mi;
  sysinfo(&mi);
  return static_cast<uint64_t>(mi.totalram) * mi.mem_unit;
#endif
}

absl::Status StartUiWithOptions(const std::vector<std::string>& extra_args,
                                const StartUiOptions& options) {
  std::vector<std::string> argv;

  // Set max heap size to 75% of available physical memory if unset.
  const int max_heap_mb(
      options.max_heap_size_mb > 0
          ? options.max_heap_size_mb
          : std::max(static_cast<uint64_t>(512),
                     (GetPhysicalMemSize() / 1024 / 1024) * 3 / 4));

#ifdef __APPLE__
  using ::security::binexport::SetEnvironmentVariable;

  // On macOS, try to use the launcher binary first if java-binary is not set
  // (the default). This improves overall UX: the dock icon will show correctly
  // for example.
  if (options.java_binary.empty()) {
    // Directory layout:
    //   <prefix>/BinDiff.app/Contents/app            (bindiff.jar)
    //   <prefix>/BinDiff.app/Contents/MacOS          ("bindiff_dir")
    //   <prefix>/BinDiff.app/Contents/MacOS/bin      (BinDiff binaries)
    //   <prefix>/BinDiff.app/Contents/MacOS/BinDiff  (Launcher)
    argv = {"/usr/bin/open", JoinPath(options.bindiff_dir, "../..")};

    // The launcher does not take any JVM arguments, so they have to be set via
    // environment variable.
    std::string tool_options =
        absl::StrCat("-Xms128m -Xmx", max_heap_mb, "m",
                     absl::StrJoin(options.java_vm_options, " "));
    SetEnvironmentVariable("JAVA_TOOL_OPTIONS", tool_options);

    argv.insert(argv.end(), extra_args.begin(), extra_args.end());

    if (SpawnProcess(argv).ok()) {
      return absl::OkStatus();
    }
    // Try again using the regular process below.
  }
#endif

  argv = {options.java_binary};
  std::string& java_exe = argv.front();
  if (java_exe.empty()) {
    java_exe = GetJavaHomeDir();
    if (!java_exe.empty()) {
      absl::StrAppend(&java_exe, kPathSeparator, "bin", kPathSeparator);
    }
#ifdef _WIN32
    absl::StrAppend(&java_exe, "javaw.exe");
#else
    absl::StrAppend(&java_exe, "java");
#endif
  }

  // Command-line takes precedence over JAVA_TOOL_OPTIONS, which may be set on
  // macOS.
  argv.push_back("-Xms128m");
  argv.push_back(absl::StrCat("-Xmx", max_heap_mb, "m"));

  argv.insert(argv.end(), options.java_vm_options.begin(),
              options.java_vm_options.end());
#ifdef __APPLE__
  argv.push_back("-Xdock:name=BinDiff");
#endif

  argv.push_back("-jar");
  bool found_jar = false;
  // Try to find the UI JAR file in several locations (b/63617055).
  for (const auto& relative_path : {"bin",
#ifdef __APPLE__
                                    "../app",
#endif
                                    ""}) {
    const std::string jar_file =
        JoinPath(options.bindiff_dir, relative_path, "bindiff.jar");
    found_jar = FileExists(jar_file);
    if (found_jar) {
      argv.push_back(jar_file);
      break;
    }
  }
  if (!found_jar) {
    return absl::NotFoundError(
        absl::StrCat("Missing jar file in prefix: ", options.bindiff_dir));
  }
  argv.insert(argv.end(), extra_args.begin(), extra_args.end());

  return SpawnProcess(argv);
}

}  // namespace security::bindiff
