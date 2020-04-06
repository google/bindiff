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

#include <cstdlib>

#include "third_party/absl/status/status.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/strings/str_format.h"
#include "third_party/absl/strings/str_split.h"
#include "third_party/zynamics/bindiff/utility.h"
#include "third_party/zynamics/binexport/util/filesystem.h"

namespace security::bindiff {

#ifdef _WIN32
// Minimum required version of the JRE
constexpr double kMinJavaVersion = 11.0;

bool RegQueryStringValue(HKEY key, const char* name, char* buffer,
                         int bufsize) {
  DWORD type;
  DWORD size;

  // Do we have a value of that name?
  if (RegQueryValueEx(key, name, 0, &type, 0, &size) != ERROR_SUCCESS) {
    return false;
  }

  // Is it the right type and not too large?
  if (type != REG_SZ || (size >= static_cast<uint32_t>(bufsize))) {
    return false;
  }

  return RegQueryValueEx(key, name, 0, 0, reinterpret_cast<uint8_t*>(buffer),
                         &size) == ERROR_SUCCESS;
}
#endif

std::string GetJavaHomeDir() {
  std::string result;
#ifdef _WIN32
  char buffer[MAX_PATH] = {0};

  // Try environment variable first.
  int size = GetEnvironmentVariable("JAVA_HOME", &buffer[0], MAX_PATH);
  if (size != 0 && size < MAX_PATH) {
    return buffer;
  }

  HKEY key;
  // Only try JDK key, as newer Java versions (> 9) do not ship the JRE
  // separately anymore.
  if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, R"(SOFTWARE\JavaSoft\JDK)", 0, KEY_READ,
                   &key) != ERROR_SUCCESS) {
    return "";
  }

  if (RegQueryStringValue(key, "CurrentVersion", &buffer[0], MAX_PATH)) {
    // Parse the first part of the version (e.g. 11.0) and ignore everything
    // else.
    const double cur_var = strtod(buffer, nullptr);
    HKEY subkey(0);
    if (cur_var >= kMinJavaVersion &&
        RegOpenKeyEx(key, buffer, 0, KEY_READ, &subkey) == ERROR_SUCCESS) {
      if (RegQueryStringValue(subkey, "JavaHome", &buffer[0], MAX_PATH)) {
        result = buffer;
      }
      RegCloseKey(subkey);
    }
  }
  RegCloseKey(key);
#else
  const char* java_home = getenv("JAVA_HOME");
  if (java_home) {
    result = java_home;
  }
#endif

  return result;
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

absl::Status StartUiWithOptions(std::vector<std::string> extra_args,
                                const StartUiOptions& options) {
  std::vector<std::string> argv;

  // Set max heap size to 75% of available physical memory if unset.
  const int max_heap_mb(
      options.max_heap_size_mb > 0
          ? options.max_heap_size_mb
          : std::max(static_cast<uint64_t>(512),
                     (GetPhysicalMemSize() / 1024 / 1024) * 3 / 4));

#ifdef __APPLE__
  // On macOS, try to use the launcher binary first if java-binary is not set
  // (the default). This improves overall UX: the dock icon will show correctly
  // for example.
  if (options.java_binary.empty()) {
    // Directory layout:
    //   <prefix>/BinDiff.app/Contents/app            (bindiff.jar)
    //   <prefix>/BinDiff.app/Contents/MacOS/BinDiff  (Launcher)
    argv = {"/usr/bin/open", JoinPath(options.gui_dir, "../..")};

    // The launcher does not take any JVM arguments, so they have to be set via
    // environment variable.
    std::string tool_options = absl::StrFormat("-Xms128m -Xmx%dm", max_heap_mb);
    if (!options.java_vm_options.empty()) {
      absl::StrAppend(&tool_options, " ", options.java_vm_options);
    }
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
  argv.push_back(absl::StrFormat("-Xmx%dm", max_heap_mb));

  for (const auto& vm_arg :
       absl::StrSplit(options.java_vm_options, ' ', absl::SkipWhitespace())) {
    argv.emplace_back(vm_arg);
  }
#ifdef __APPLE__
  argv.push_back("-Xdock:name=BinDiff");
#endif

  argv.push_back("-jar");
  constexpr char kGuiJarName[] = "bindiff.jar";
  std::string jar_file = JoinPath(options.gui_dir, "bin", kGuiJarName);
  if (!FileExists(jar_file)) {
    // Try again without the "bin" dir (b/63617055).
    jar_file = JoinPath(options.gui_dir, kGuiJarName);
    if (!FileExists(jar_file)) {
      return absl::NotFoundError(absl::StrCat("Missing jar file: ", jar_file));
    }
  }
  argv.push_back(jar_file);
  argv.insert(argv.end(), extra_args.begin(), extra_args.end());

  return SpawnProcess(argv);
}

}  // namespace security::bindiff
