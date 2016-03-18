// Copyright 2011-2016 Google Inc. All Rights Reserved.
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

#ifdef WIN32
#include <shlobj.h>
#include <shlwapi.h>
#include <stdlib.h>
#include <windows.h>
#undef StrCat  // shlwapi.h
#else
#include <sys/stat.h>
#include <sys/types.h>
#ifdef __APPLE__
#include <libgen.h>
#include <cerrno>
#include <cstring>
#endif  // __APPLE__
#endif  // WIN32

#include <algorithm>

#include "strings/strutil.h"

void CreateDirectories(StringPiece path) {
  if (path.empty()) {
    return;
  }
#ifdef WIN32
  std::string real_path(path.ToString());
  std::replace(real_path.begin(), real_path.end(), '/', '\\');
  const int success = SHCreateDirectoryEx(nullptr, real_path.c_str(), nullptr);
  if (success != ERROR_SUCCESS && success != ERROR_ALREADY_EXISTS &&
      success != ERROR_FILE_EXISTS) {
    throw std::runtime_error(
        StrCat("cannot create directory \"", path, "\"").c_str());
  }
#else
  size_t slash = path.rfind('/');
  if (slash != StringPiece::npos) {
    // Create parent directory recursively.
    CreateDirectories(path.substr(0, slash));
  }
  if (mkdir(path.ToString().c_str(), 0775) == -1) {
    // Ignore existing directories.
    if (errno != EEXIST) {
      throw std::runtime_error(
          StrCat("cannot create directory \"", path, "\"").c_str());
    }
  }
#endif
}

std::string GetTempDirectory(StringPiece product_name, bool create) {
#ifdef WIN32
  char buffer[MAX_PATH] = {0};
  if (!GetTempPath(MAX_PATH, buffer)) {
    throw std::runtime_error("GetTempPath() failed");
  }
  std::string path(StrCat(buffer, "zynamics\\", product_name, "\\"));
#else
  std::string path(StrCat("/tmp/zynamics/", product_name, "/"));
#endif  // WIN32
  CreateDirectories(path);
  return path;
}

std::string GetFilename(StringPiece path) {
#ifdef WIN32
  char filename[_MAX_FNAME] = {0};
  char extension[_MAX_EXT] = {0};
  if (_splitpath_s(path.ToString().c_str(), nullptr /* Drive */,
                   0 /* Drive length */, nullptr /* Directory */,
                   0 /* Directory length */, filename, _MAX_FNAME, extension,
                   _MAX_EXT) != 0) {
    throw std::runtime_error(
        StrCat("_splitpath_s() failed for \"", path, "\"").c_str());
  }
  return StrCat(filename, extension);
#else
  auto path_copy(path.ToString());
  return std::string(basename(&path_copy[0]));
#endif  // WIN32
}

std::string ReplaceFileExtension(StringPiece path, StringPiece new_extension) {
#ifdef WIN32
  char filename[_MAX_FNAME] = {0};
  if (_splitpath_s(path.ToString().c_str(), nullptr /* Drive */,
                   0 /* Drive length */, nullptr /* Directory */,
                   0 /* Directory length */, filename, _MAX_FNAME,
                   nullptr /* Extension */, 0 /* Extension length */) != 0) {
    throw std::runtime_error(
        StrCat("_splitpath_s() failed for \"", path, "\"").c_str());
  }
  return StrCat(filename, new_extension);
#else
  auto path_copy(path.ToString());
  StringPiece filename = basename(&path_copy[0]);
  auto pos = filename.find_last_of(".");
  if (pos == StringPiece::npos) {
    pos = filename.size();
  }
  return StrCat(filename.substr(0, pos), new_extension);
#endif  // WIN32
}

#ifndef WIN32
mode_t GetFileMode(StringPiece path) {
  struct stat file_info;
  if (stat(path.ToString().c_str(), &file_info) == -1) {
    switch (errno) {
      case EACCES:
      case ENOENT:
      case ENOTDIR:
        return 0;
      default:
        throw std::runtime_error(
            StrCat("stat() failed for \"", path, "\": ", strerror(errno))
                .c_str());
    }
  }
  return file_info.st_mode;
}
#endif  // WIN32

bool FileExists(StringPiece path) {
#ifdef WIN32
  return PathFileExists(path.ToString().c_str()) == TRUE;
#else
  return S_ISREG(GetFileMode(path));
#endif  // WIN32
}

bool IsDirectory(StringPiece path) {
#ifdef WIN32
  return PathIsDirectory(path.ToString().c_str()) == TRUE;
#else
  return S_ISDIR(GetFileMode(path));
#endif  // WIN32
}
