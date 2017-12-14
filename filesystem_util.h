// Copyright 2011-2017 Google Inc. All Rights Reserved.
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

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_FILESYSTEM_UTIL_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_FILESYSTEM_UTIL_H_

#include <cstdint>
#include <initializer_list>
#include <string>
#include <vector>

#include "third_party/absl/strings/string_view.h"
#include "third_party/zynamics/binexport/types.h"

extern const char kPathSeparator[];
extern const char kAllFilesFilter[];

namespace internal {
// Not part of the public API.
string JoinPathImpl(std::initializer_list<absl::string_view> paths);
}  // namespace internal

// Returns the current working directory.
string GetCurrentDirectory();

// Recursively creates directories for a specified path.
void CreateDirectories(absl::string_view path);

// Returns the path to an OS specific directory for temporary files.
string GetTempDirectory(absl::string_view product_name, bool create);

// Returns the filename (basename) of a path.
string Basename(absl::string_view path);

// Returns the directory part of a path. On Windows, this includes the drive
// letter.
string Dirname(absl::string_view path);

// Returns the extension of a filename.
string GetFileExtension(absl::string_view path);

// Replaces the extension of a filename.
string ReplaceFileExtension(absl::string_view path,
                            absl::string_view new_extension);

// Joins multiple paths together using the platform-specific path separator.
// Arguments must be convertible to absl::string_view.
template <typename... T>
inline string JoinPath(const T&... args) {
  return ::internal::JoinPathImpl({args...});
}

// Returns the size of a file on disk or -1 on error.
int64 GetFileSize(absl::string_view path);

// Returns whether a file exists.
bool FileExists(absl::string_view path);

// Returns whether a given path is a directory.
bool IsDirectory(absl::string_view path);

// List the files in the specified directory.
void GetDirectoryEntries(absl::string_view path, std::vector<string>* result);

// Removes all files and sub-directories under path.
void RemoveAll(absl::string_view path);

// Copies a file. Throws std::runtime_error on error.
void CopyFile(absl::string_view from, absl::string_view to);

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_FILESYSTEM_UTIL_H_
