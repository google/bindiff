// Copyright 2011-2019 Google LLC. All Rights Reserved.
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

#ifndef UTIL_FILESYSTEM_H_
#define UTIL_FILESYSTEM_H_

#include <cstdint>
#include <initializer_list>
#include <string>
#include <vector>

#include "third_party/absl/strings/string_view.h"
#include "third_party/zynamics/binexport/types.h"
#include "third_party/zynamics/binexport/util/status.h"
#include "third_party/zynamics/binexport/util/statusor.h"

#ifdef WIN32
// Some Abseil headers include Windows.h, so undo a few macros
#undef CopyFile             // winbase.h
#undef GetCurrentDirectory  // processenv.h
#undef StrCat               // shlwapi.h
#endif

extern const char kPathSeparator[];
extern const char kAllFilesFilter[];

namespace internal {
// Not part of the public API.
std::string JoinPathImpl(std::initializer_list<absl::string_view> paths);
}  // namespace internal

// Returns the current working directory. On error, this returns an empty
// std::string.
std::string GetCurrentDirectory();

// Recursively creates directories for a specified path.
not_absl::Status CreateDirectories(absl::string_view path);

// Returns the path to an OS-specific directory for temporary files.
not_absl::StatusOr<std::string> GetTempDirectory(
    absl::string_view product_name);

// Like GetTempDirectory(), but creates the directory if it does not exist
// already.
not_absl::StatusOr<std::string> GetOrCreateTempDirectory(
    absl::string_view product_name);

// Returns the filename (basename) of a path.
std::string Basename(absl::string_view path);

// Returns the directory part of a path. On Windows, this includes the drive
// letter.
std::string Dirname(absl::string_view path);

// Returns the extension of a filename.
std::string GetFileExtension(absl::string_view path);

// Replaces the extension of a filename.
std::string ReplaceFileExtension(absl::string_view path,
                                 absl::string_view new_extension);

// Joins multiple paths together using the platform-specific path separator.
// Arguments must be convertible to absl::string_view.
template <typename... T>
inline std::string JoinPath(const T&... args) {
  return ::internal::JoinPathImpl({args...});
}

// Returns the size of a file on disk.
not_absl::StatusOr<int64_t> GetFileSize(absl::string_view path);

// Returns whether a file exists.
bool FileExists(absl::string_view path);

// Returns whether a given path is a directory.
bool IsDirectory(absl::string_view path);

// List the files in the specified directory.
not_absl::Status GetDirectoryEntries(absl::string_view path,
                                     std::vector<std::string>* result);

// Removes all files and sub-directories under path.
not_absl::Status RemoveAll(absl::string_view path);

// Copies a file.
not_absl::Status CopyFile(absl::string_view from, absl::string_view to);

#endif  // UTIL_FILESYSTEM_H_
