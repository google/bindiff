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

#include "strings/stringpiece.h"

extern const char kPathSeparator[];
extern const char kAllFilesFilter[];

namespace internal {
// Not part of the public API.
std::string JoinPathImpl(std::initializer_list<StringPiece> paths);
}  // namespace internal

// Returns the current working directory.
std::string GetCurrentDirectory();

// Recursively creates directories for a specified path.
void CreateDirectories(StringPiece path);

// Returns the path to an OS specific directory for temporary files.
std::string GetTempDirectory(StringPiece product_name, bool create);

// Returns the filename (basename) of a path.
std::string Basename(StringPiece path);

// Returns the directory part of a path. On Windows, this includes the drive
// letter.
std::string Dirname(StringPiece path);

// Returns the extension of a filename.
std::string GetFileExtension(StringPiece path);

// Replaces the extension of a filename.
std::string ReplaceFileExtension(StringPiece path, StringPiece new_extension);

// Joins multiple paths together using the platform-specific path separator.
// Arguments must be convertible to StringPiece.
template <typename... T>
inline std::string JoinPath(const T&... args) {
  return ::internal::JoinPathImpl({args...});
}

// Returns the size of a file on disk or -1 on error.
int64_t GetFileSize(StringPiece path);

// Returns whether a file exists.
bool FileExists(StringPiece path);

// Returns whether a given path is a directory.
bool IsDirectory(StringPiece path);

// List the files in the specified directory.
void GetDirectoryEntries(StringPiece path, std::vector<std::string>* result);

// Removes all files and sub-directories under path.
void RemoveAll(StringPiece path);

// Copies a file. Throws std::runtime_error on error.
void CopyFile(StringPiece from, StringPiece to);

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_FILESYSTEM_UTIL_H_
