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

#include <string>

#include "strings/stringpiece.h"

// Recursively creates directories for a specified path.
void CreateDirectories(StringPiece path);

// Returns the path to an OS specific directory for temporary files.
std::string GetTempDirectory(StringPiece product_name, bool create);

// Returns the filename (basename) of a path.
std::string GetFilename(StringPiece path);

// Replaces the extension of a filename.
std::string ReplaceFileExtension(StringPiece path, StringPiece new_extension);

// Returns whether a file exists.
bool FileExists(StringPiece path);

// Returns whether a given path is a directory.
bool IsDirectory(StringPiece path);

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_FILESYSTEM_UTIL_H_
