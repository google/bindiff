// Copyright 2011-2022 Google LLC
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

#include "third_party/zynamics/binexport/util/process.h"

#ifdef _WIN32
#define _WIN32_WINNT 0x0501
#define VC_EXTRALEAN
#include <shlobj.h>  // Windows get folder stuff
#include <windows.h>
#undef SetEnvironmentVariable  // winbase.h
#else
#include <spawn.h>     // For posix_spawnp()
#include <sys/wait.h>  // For waitpid()
#include <unistd.h>    // For getpid()
#ifdef __APPLE__
#include <crt_externs.h>
#include <sys/sysctl.h>
#else
#include <sys/sysinfo.h>
#endif
#include <sys/stat.h>
#endif
#include <algorithm>
#include <cerrno>
#include <cstdlib>
#include <cstring>

#include "third_party/absl/base/attributes.h"
#include "third_party/absl/status/status.h"
#include "third_party/absl/strings/ascii.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/strings/str_replace.h"
#include "third_party/zynamics/binexport/util/filesystem.h"
#include "third_party/zynamics/binexport/util/status_macros.h"

namespace security::binexport {

std::string GetLastOsError() {
#ifdef _WIN32
  LPVOID message_buffer = nullptr;
  FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM |
                    FORMAT_MESSAGE_IGNORE_INSERTS,
                /*lpSource=*/nullptr, GetLastError(),
                MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
                reinterpret_cast<LPTSTR>(&message_buffer), /*nSize=*/0,
                /*Arguments=*/nullptr);

  std::string result(reinterpret_cast<const char*>(message_buffer),
                     strlen(reinterpret_cast<const char*>(message_buffer)));
  LocalFree(message_buffer);
  return result;
#else
  return strerror(errno);
#endif
}

bool SetEnvironmentVariable(const std::string& name, const std::string& value) {
#ifdef _WIN32
  // Call safe CRT function instead of ::SetEnvironmentVariable(), because the
  // latter no longer deletes environment variables if their content is empty.
  return _putenv_s(name.c_str(), value.c_str());
#else
  if (value.empty()) {
    return unsetenv(name.c_str());
  }
  return setenv(name.c_str(), value.c_str(), true /* Overwrite */);
#endif
}

namespace {

absl::StatusOr<int> DoSpawnProcess(const std::vector<std::string>& argv,
                                   bool wait) {
  if (argv.empty()) {
    return absl::InvalidArgumentError("Empty argument list");
  }
  int exit_code = 0;

#ifndef _WIN32
#ifdef __APPLE__
  char** environ = *_NSGetEnviron();
#endif  // __APPLE__
  std::vector<char*> spawned_args(argv.size() + 1);
  for (size_t i = 0; i < argv.size(); ++i) {
    spawned_args[i] = const_cast<char*>(argv[i].c_str());
  }
  // Last element must be 0.
  spawned_args.back() = nullptr;

  // Spawn the child process.
  pid_t childpid;
  if (int error =
          posix_spawnp(&childpid, spawned_args[0], /*file_actions=*/nullptr,
                       /*attrp=*/nullptr, &spawned_args[0], environ);
      error != 0) {
    return absl::UnknownError(
        absl::StrCat("Error executing '", argv[0], "': ", strerror(error)));
  }

  // Wait for the child process to exit or for error information to be
  // available.
  int status = 0;
  int options = WUNTRACED | WCONTINUED | (wait ? 0 : WNOHANG);
  do {
    if (waitpid(childpid, &status, options) == -1) {
      if (errno == ECHILD) {  // No child to wait on
        break;
      }
      return absl::UnknownError(absl::StrCat("Error waiting for '", argv[0],
                                             "': ", GetLastOsError()));
    }
  } while (wait && !WIFEXITED(status) && !WIFSIGNALED(status));

  exit_code = WEXITSTATUS(status);
  // Need to check for exit code 127 if an error occurred after posix_spawnp()
  // returned (see POSIX.1-2017 description).
  if (exit_code == 127) {
    return absl::UnknownError(absl::StrCat("Error executing: '", argv[0], "'"));
  }
#else
  // Build command-line.
  std::string cmdline = absl::StrCat("\"", argv[0], "\"");
  for (int i = 1; i < argv.size(); ++i) {
    // Copy argument, escape all quotes and wrap result in quotes.
    absl::StrAppend(&cmdline, " \"",
                    absl::StrReplaceAll(argv[i], {{"\"", "\\\""}}), "\"");
  }

  STARTUPINFO si{};
  PROCESS_INFORMATION pi{};

  GetStartupInfo(&si);
  si.cb = sizeof(si);
  si.dwFlags = STARTF_USESHOWWINDOW;
  si.wShowWindow = SW_HIDE;

  if (CreateProcess(
          /*lpApplicationName=*/nullptr,
          // Note: Per MSDN, "the Unicode version of this function,
          //       CreateProcessW, can modify the contents of this string".
          /*lpCommandLine=*/const_cast<LPSTR>(cmdline.c_str()),
          /*lpProcessAttributes=*/nullptr,
          /*lpThreadAttributes=*/nullptr, /*bInheritHandles=*/0,
          /*dwCreationFlags=*/CREATE_NO_WINDOW,
          /*lpEnvironment=*/nullptr, /*lpCurrentDirectory=*/nullptr,
          /*lpStartupInfo=*/&si, /*lpProcessInformation=*/&pi) == 0) {
    return absl::UnknownError(absl::StrCat("Error executing: '", cmdline, "'"));
  }

  if (wait) {
    WaitForSingleObject(pi.hProcess, INFINITE);
    DWORD wide_exit_code = 0;
    GetExitCodeProcess(pi.hProcess, &wide_exit_code);
    exit_code = static_cast<int>(wide_exit_code);
  }

  // http://msdn.microsoft.com/en-us/library/ms682425.aspx:
  // "Handles in PROCESS_INFORMATION must be closed with CloseHandle when they
  // are no longer needed."
  CloseHandle(pi.hProcess);
  CloseHandle(pi.hThread);
#endif

  return exit_code;
}

}  // namespace

absl::StatusOr<int> SpawnProcessAndWait(const std::vector<std::string>& argv) {
  return DoSpawnProcess(argv, /*wait=*/true);
}

absl::Status SpawnProcess(const std::vector<std::string>& argv) {
  return DoSpawnProcess(argv, /*wait=*/false).status();
}

absl::StatusOr<std::string> GetOrCreateAppDataDirectory(
    absl::string_view product_name) {
  std::string path;
#ifndef _WIN32
  const char* home_dir = getenv("HOME");
  if (!home_dir) {
    return absl::NotFoundError("Home directory not set");
  }
#ifdef __APPLE__
  path = JoinPath(home_dir, "Library", "Application Support", product_name);
#else
  path = JoinPath(home_dir,
                  absl::StrCat(".", absl::AsciiStrToLower(product_name)));
#endif  // __APPLE__
#else
  char buffer[MAX_PATH] = {0};
  if (SHGetFolderPath(/*hwndOwner=*/0, CSIDL_APPDATA, /*hToken=*/0,
                      /*dwFlags=*/0, buffer) != S_OK) {
    return absl::UnknownError(GetLastOsError());
  }
  path = JoinPath(buffer, product_name);
#endif  // _WIN32
  NA_RETURN_IF_ERROR(CreateDirectories(path));
  return path;
}

absl::StatusOr<std::string> GetCommonAppDataDirectory(
    absl::string_view product_name) {
  std::string path;
#ifndef _WIN32
#ifdef __APPLE__
  path = JoinPath("/Library", "Application Support", product_name);
#else
  path = JoinPath("/etc/opt/", absl::AsciiStrToLower(product_name));
#endif  // __APPLE__
#else
  char buffer[MAX_PATH] = {0};
  if (SHGetFolderPath(/*hwndOwner=*/0, CSIDL_COMMON_APPDATA, /*hToken=*/0,
                      /*dwFlags=*/0, buffer) != S_OK) {
    return absl::UnknownError(GetLastOsError());
  }
  path = JoinPath(buffer, product_name);
#endif  // _WIN32
  if (!IsDirectory(path)) {
    return absl::NotFoundError(
        absl::StrCat("Configuration directory not found: ", path));
  }
  return path;
}

}  // namespace security::binexport
