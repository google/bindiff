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

#include "third_party/zynamics/binexport/util/filesystem.h"

#ifdef _WIN32
#include <windows.h>
#include <shellapi.h>
#include <shlobj.h>
#include <shlwapi.h>
#include <stdlib.h>
#undef CopyFile             // winbase.h
#undef GetCurrentDirectory  // processenv.h
#undef GetFullPathName      // fileapi.h
#undef StrCat               // shlwapi.h
#else
#include <dirent.h>
#include <libgen.h>  // POSIX basename()
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>
#endif

#ifdef __APPLE__
#include <cerrno>
#include <cstring>
#else
#include <filesystem>
#include <system_error>  // NOLINT(build/c++11)
#endif

#include <algorithm>
#include <fstream>
#include <string>

#include "third_party/absl/strings/ascii.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/strings/str_replace.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/absl/strings/strip.h"
#include "third_party/zynamics/binexport/util/canonical_errors.h"
#include "third_party/zynamics/binexport/util/status_macros.h"

#ifdef _WIN32
const char kPathSeparator[] = "\\";
const char kAllFilesFilter[] = "*.*";
#else
const char kPathSeparator[] = "/";
const char kAllFilesFilter[] = "*";
#endif

namespace internal {

std::string JoinPathImpl(std::initializer_list<absl::string_view> paths) {
  std::string result;
  for (auto& path : paths) {
    if (path.empty()) {
      continue;
    }
    if (result.empty() || result[result.size() - 1] == kPathSeparator[0]) {
      absl::StrAppend(&result, path);
    } else {
      absl::StrAppend(&result, kPathSeparator, path);
    }
  }
  return result;
}

}  // namespace internal

std::string GetCurrentDirectory() {
  enum { kMaxPath = 260 };
  char buffer[kMaxPath] = {0};
#ifdef _WIN32
  if (GetCurrentDirectoryA(kMaxPath, buffer) == 0) {
    return "";
  }
#else
  if (!getcwd(buffer, kMaxPath)) {
    return "";
  }
#endif
  return std::string(buffer);
}

std::string GetFullPathName(absl::string_view path) {
#ifdef _WIN32
  if (!PathIsRelativeA(std::string(path).c_str())) {
    return std::string(path);
  }
#else
  if (absl::StartsWith(path, kPathSeparator)) {
    return std::string(path);
  }
#endif
  return JoinPath(GetCurrentDirectory(), path);
}

not_absl::Status CreateDirectories(absl::string_view path) {
  if (path.empty()) {
    return not_absl::OkStatus();
  }
#ifdef _WIN32
  std::string real_path = absl::StrReplaceAll(path, {{"/", "\\"}});
  const int result = SHCreateDirectoryEx(
      /*hwnd=*/nullptr, real_path.c_str(), /*psa=*/nullptr);
  if (result != ERROR_SUCCESS && result != ERROR_ALREADY_EXISTS &&
      result != ERROR_FILE_EXISTS) {
    return not_absl::UnknownError(
        absl::StrCat("cannot create directory \"", path, "\""));
  }
#else
  auto slash = path.rfind('/');
  if (slash != absl::string_view::npos) {
    // Create parent directory recursively.
    NA_RETURN_IF_ERROR(CreateDirectories(path.substr(0, slash)));
  }
  std::string path_copy(path);
  if (mkdir(path_copy.c_str(), 0775) == -1) {
    // Ignore existing directories.
    if (errno != EEXIST) {
      return not_absl::UnknownError(absl::StrCat(
          "cannot create directory \"", path, "\": ", strerror(errno)));
    }
  }
#endif
  return not_absl::OkStatus();
}

namespace {

not_absl::StatusOr<std::string> DoGetOrCreateTempDirectory(
    absl::string_view product_name, bool create) {
  std::string path;
#ifdef _WIN32
  char buffer[MAX_PATH] = {0};
  if (GetTempPath(MAX_PATH, buffer) == 0) {
    return not_absl::UnknownError("error getting temp directory");
  }
  path = JoinPath(buffer, product_name);
#else
  path = JoinPath("/tmp", absl::AsciiStrToLower(product_name));
#endif
  if (create) {
    NA_RETURN_IF_ERROR(CreateDirectories(path));
  }
  return path;
}

}  // namespace

not_absl::StatusOr<std::string> GetTempDirectory(
    absl::string_view product_name) {
  return DoGetOrCreateTempDirectory(product_name, /*create=*/false);
}

not_absl::StatusOr<std::string> GetOrCreateTempDirectory(
    absl::string_view product_name) {
  return DoGetOrCreateTempDirectory(product_name, /*create=*/true);
}

std::string Basename(absl::string_view path) {
  const auto last_slash = path.find_last_of(kPathSeparator[0]);
  return std::string(last_slash == absl::string_view::npos
                         ? path
                         : absl::ClippedSubstr(path, last_slash + 1));
}

std::string Dirname(absl::string_view path) {
  std::string path_copy(path);
#ifdef _WIN32
  char drive[_MAX_DRIVE] = {0};
  char dirname[_MAX_DIR] = {0};
  if (_splitpath_s(path_copy.c_str(), drive, _MAX_DRIVE,
                   dirname /* Directory */, _MAX_FNAME, nullptr /* Filename */,
                   0 /* Filename length */, nullptr /* Extension */,
                   0 /* Extension length */) != 0) {
    return ".";  // Safe default in case this ever happens.
  }
  return absl::StrCat(drive, absl::StripSuffix(dirname, kPathSeparator));
#else
  return std::string(dirname(&path_copy[0]));
#endif
}

std::string GetFileExtension(absl::string_view path) {
  std::string extension = Basename(path);
  auto pos = extension.rfind(".");
  return pos != absl::string_view::npos ? extension.substr(pos) : "";
}

std::string ReplaceFileExtension(absl::string_view path,
                                 absl::string_view new_extension) {
  auto last_slash = path.find_last_of(kPathSeparator[0]);
  if (last_slash == absl::string_view::npos) {
    last_slash = 0;
  }
  auto pos = path.substr(last_slash).find_last_of(".");
  if (pos != absl::string_view::npos) {
    pos += last_slash;
  }
  return absl::StrCat(path.substr(0, pos), new_extension);
}

#ifndef _WIN32
namespace {

not_absl::StatusOr<mode_t> GetFileMode(absl::string_view path) {
  struct stat file_info;
  std::string path_copy(path);
  if (stat(path_copy.c_str(), &file_info) == -1) {
    switch (errno) {
      case EACCES:
      case ENOENT:
      case ENOTDIR:
        return 0;
      default:
        not_absl::UnknownError(absl::StrCat("stat() failed for \"", path,
                                            "\": ", strerror(errno)));
    }
  }
  return file_info.st_mode;
}

}  // namespace
#endif

not_absl::StatusOr<int64_t> GetFileSize(absl::string_view path) {
  std::ifstream stream(std::string(path), std::ifstream::ate);
  auto size = static_cast<int64_t>(stream.tellg());
  if (stream) {
    return size;
  }
  return not_absl::UnknownError(
      absl::StrCat("cannot get file size for: ", path));
}

bool FileExists(absl::string_view path) {
#ifdef _WIN32
  std::string path_copy(path);
  return PathFileExists(path_copy.c_str()) == TRUE;
#else
  auto mode_or = GetFileMode(path);
  return mode_or.ok() ? S_ISREG(mode_or.ValueOrDie()) : false;
#endif
}

bool IsDirectory(absl::string_view path) {
#ifdef _WIN32
  std::string path_copy(path);
  return PathIsDirectory(path_copy.c_str()) == FILE_ATTRIBUTE_DIRECTORY;
#else
  auto mode_or = GetFileMode(path);
  return mode_or.ok() ? S_ISDIR(mode_or.ValueOrDie()) : false;
#endif
}

not_absl::Status GetDirectoryEntries(absl::string_view path,
                                     std::vector<std::string>* result) {
#ifdef _WIN32
  std::string path_copy(JoinPath(path, "*"));  // Assume path is a directory
  WIN32_FIND_DATA entry;
  HANDLE directory = FindFirstFile(path_copy.c_str(), &entry);
  bool error = directory == INVALID_HANDLE_VALUE;
  if (!error) {
    do {
      result->push_back(entry.cFileName);
    } while (FindNextFile(directory, &entry) != 0);
    error = GetLastError() != ERROR_NO_MORE_FILES;
    FindClose(directory);
  }
  if (error) {
    return not_absl::UnknownError(
        absl::StrCat("FindFirstFile() failed for: ", path));
  }
#else
  std::string path_copy(path);
  errno = 0;
  DIR* directory = opendir(path_copy.c_str());
  if (!directory) {
    return not_absl::UnknownError(
        absl::StrCat("opendir() failed for \"", path, "\": ", strerror(errno)));
  }
  struct dirent* entry;
  while ((entry = readdir(directory))) {
    const std::string name(entry->d_name);
    if (name != "." && name != "..") {
      result->push_back(name);
    }
  }
  if (errno != 0) {
    return not_absl::UnknownError(
        absl::StrCat("readdir() failed: ", strerror(errno)));
  }
  closedir(directory);
#endif
  return not_absl::OkStatus();
}

not_absl::Status RemoveAll(absl::string_view path) {
  std::string path_copy(path);
#ifndef __APPLE__
  // TODO(cblichmann): Use on all platforms once XCode has filesystem.
  namespace fs = std::filesystem;
  if (std::error_code ec;
      fs::remove_all(path_copy, ec) == static_cast<std::uintmax_t>(-1)) {
    return not_absl::UnknownError(
        absl::StrCat("remove_all() failed for \"", path, "\": ", ec.message()));
  }
#else
  errno = 0;
  if (!IsDirectory(path)) {
    return not_absl::UnknownError(absl::StrCat("Not a directory: ", path));
  }
  DIR* directory = opendir(path_copy.c_str());
  if (!directory) {
    return not_absl::UnknownError(
        absl::StrCat("opendir() failed for \"", path, "\": ", strerror(errno)));
  }
  struct dirent* entry;
  while ((entry = readdir(directory))) {
    const std::string name(entry->d_name);
    if (name == "." || name == "..") {
      continue;
    }
    const std::string full_path(JoinPath(path, name));
    if (IsDirectory(full_path)) {
      NA_RETURN_IF_ERROR(RemoveAll(full_path));
    } else {
      std::remove(full_path.c_str());
    }
  }
  if (errno != 0) {
    return not_absl::UnknownError(
        absl::StrCat("readdir() failed: ", strerror(errno)));
  }
  closedir(directory);
  if (rmdir(path_copy.c_str()) == -1) {
    return not_absl::UnknownError(
        absl::StrCat("rmdir() failed: ", strerror(errno)));
  }
#endif
  return not_absl::OkStatus();
}

not_absl::Status CopyFile(absl::string_view from, absl::string_view to) {
  std::ifstream input(std::string(from), std::ios::in | std::ios::binary);
  std::ofstream output(std::string(to),
                       std::ios::out | std::ios::trunc | std::ios::binary);
  output << input.rdbuf();
  if (!input || !output) {
    return not_absl::UnknownError("error copying file");
  }
  return not_absl::OkStatus();
}
