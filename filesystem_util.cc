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

#include "third_party/zynamics/binexport/filesystem_util.h"

#ifdef WIN32
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#include <shellapi.h>
#include <shlobj.h>
#include <shlwapi.h>
#include <stdlib.h>
#undef CopyFile             // winbase.h
#undef GetCurrentDirectory  // processenv.h
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
#endif

#include <algorithm>
#include <fstream>
#include <string>

#include "strings/strutil.h"

using ::std::string;

#ifdef WIN32
  const char kPathSeparator[] = "\\";
  const char kAllFilesFilter[] = "*.*";
#else
  const char kPathSeparator[] = "/";
  const char kAllFilesFilter[] = "*";
#endif

namespace internal {

std::string JoinPathImpl(std::initializer_list<StringPiece> paths) {
  string result;
  for (auto& path : paths) {
    if (path.empty()) {
      continue;
    }
    if (result.empty() || result[result.size() - 1] == kPathSeparator[0]) {
      StrAppend(&result, path);
    } else {
      StrAppend(&result, kPathSeparator, path);
    }
  }
  return result;
}

}  // namespace internal

std::string GetCurrentDirectory() {
  enum { kMaxPath = 260 };
  char buffer[kMaxPath] = {0};
#ifdef WIN32
  if (GetCurrentDirectoryA(kMaxPath, buffer) == 0) {
    throw std::runtime_error("GetCurrentDirectoryA() failed");
  }
#else
  if (!getcwd(buffer, kMaxPath)) {
    throw std::runtime_error(StrCat("getcwd() failed: ", strerror(errno)));
  }
#endif
  return std::string(buffer);
}

void CreateDirectories(StringPiece path) {
  if (path.empty()) {
    return;
  }
#ifdef WIN32
  std::string real_path(path);
  std::replace(real_path.begin(), real_path.end(), '/', '\\');
  const int success = SHCreateDirectoryEx(nullptr, real_path.c_str(), nullptr);
  if (success != ERROR_SUCCESS && success != ERROR_ALREADY_EXISTS &&
      success != ERROR_FILE_EXISTS) {
    throw std::runtime_error(StrCat("cannot create directory \"", path, "\""));
  }
#else
  size_t slash = path.rfind('/');
  if (slash != StringPiece::npos) {
    // Create parent directory recursively.
    CreateDirectories(path.substr(0, slash));
  }
  std::string path_copy(path);
  if (mkdir(path_copy.c_str(), 0775) == -1) {
    // Ignore existing directories.
    if (errno != EEXIST) {
      throw std::runtime_error(
          StrCat("cannot create directory \"", path, "\""));
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
#endif
  if (create) {
    CreateDirectories(path);
  }
  return path;
}

std::string Basename(StringPiece path) {
  std::string path_copy(path);
#ifdef WIN32
  char filename[_MAX_FNAME] = {0};
  char extension[_MAX_EXT] = {0};
  if (_splitpath_s(path_copy.c_str(), nullptr /* Drive */, 0 /* Drive length */,
                   nullptr /* Directory */, 0 /* Directory length */, filename,
                   _MAX_FNAME, extension, _MAX_EXT) != 0) {
    throw std::runtime_error(
        StrCat("_splitpath_s() failed for \"", path, "\""));
  }
  return StrCat(filename, extension);
#else
  return std::string(basename(&path_copy[0]));
#endif
}

std::string Dirname(StringPiece path) {
  std::string path_copy(path);
#ifdef WIN32
  char drive[_MAX_DRIVE] = {0};
  char dirname[_MAX_DIR] = {0};
  if (_splitpath_s(path_copy.c_str(), drive, _MAX_DRIVE,
                   dirname /* Directory */, _MAX_FNAME, nullptr /* Filename */,
                   0 /* Filename length */, nullptr /* Extension */,
                   0 /* Extension length */) != 0) {
    throw std::runtime_error(
        StrCat("_splitpath_s() failed for \"", path, "\""));
  }
  StringPiece strip_dir(dirname);
  if (!strip_dir.empty() &&
      (strip_dir[strip_dir.size() - 1] == kPathSeparator[0])) {
    strip_dir.remove_suffix(1);
  }
  return StrCat(drive, strip_dir);
#else
  return std::string(dirname(&path_copy[0]));
#endif
}

std::string GetFileExtension(StringPiece path) {
#ifdef WIN32
  char extension[_MAX_EXT] = {0};
  std::string path_copy(path);
  if (_splitpath_s(path_copy.c_str(), nullptr /* Drive */, 0 /* Drive length */,
                   nullptr /* Directory */, 0 /* Directory length */,
                   nullptr /* filename */, 0 /* Filename length */, extension,
                   _MAX_EXT) != 0) {
    throw std::runtime_error(
        StrCat("_splitpath_s() failed for \"", path, "\""));
  }
  return extension;
#else
  path.remove_prefix(path.rfind("."));
  return std::string(path);
#endif
}

std::string ReplaceFileExtension(StringPiece path, StringPiece new_extension) {
#ifdef WIN32
  std::string path_copy(path);
  char drive[_MAX_DRIVE] = {0};
  char directory[_MAX_DIR] = {0};
  char filename[_MAX_FNAME] = {0};
  if (_splitpath_s(path_copy.c_str(), drive, _MAX_DRIVE, directory, _MAX_DIR,
                   filename, _MAX_FNAME, nullptr /* Extension */,
                   0 /* Extension length */) != 0) {
    throw std::runtime_error(
        StrCat("_splitpath_s() failed for \"", path, "\""));
  }
  return StrCat(drive, directory, filename, new_extension);
#else
  auto pos = path.find_last_of(".");
  return StrCat(path.substr(0, pos), new_extension);
#endif
}

#ifndef WIN32
namespace {

mode_t GetFileMode(StringPiece path) {
  struct stat file_info;
  std::string path_copy(path);
  if (stat(path_copy.c_str(), &file_info) == -1) {
    switch (errno) {
      case EACCES:
      case ENOENT:
      case ENOTDIR:
        return 0;
      default:
        throw std::runtime_error(
            StrCat("stat() failed for \"", path, "\": ", strerror(errno)));
    }
  }
  return file_info.st_mode;
}

}  // namespace
#endif

int64_t GetFileSize(StringPiece path) {
  std::ifstream stream(std::string(path).c_str(), std::ifstream::ate);
  auto size = static_cast<int64_t>(stream.tellg());
  return !stream ? size : -1;
}

bool FileExists(StringPiece path) {
#ifdef WIN32
  std::string path_copy(path);
  return PathFileExists(path_copy.c_str()) == TRUE;
#else
  return S_ISREG(GetFileMode(path));
#endif
}

bool IsDirectory(StringPiece path) {
#ifdef WIN32
  std::string path_copy(path);
  return PathIsDirectory(path_copy.c_str()) == TRUE;
#else
  return S_ISDIR(GetFileMode(path));
#endif
}

void GetDirectoryEntries(StringPiece path, std::vector<std::string>* result) {
  std::string path_copy(path);
#ifdef WIN32
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
    throw std::runtime_error(StrCat("FindFirstFile() failed for: ", path));
  }
#else
  errno = 0;
  DIR* directory = opendir(path_copy.c_str());
  if (!directory) {
    throw std::runtime_error(
        StrCat("opendir() failed for \"", path, "\": ", strerror(errno)));
  }
  struct dirent* entry;
  while ((entry = readdir(directory))) {
    const std::string name(entry->d_name);
    if (name != "." && name != "..") {
      result->push_back(name);
    }
  }
  if (errno != 0) {
    throw std::runtime_error(StrCat("readdir() failed: ", strerror(errno)));
  }
  closedir(directory);
#endif
}

void RemoveAll(StringPiece path) {
  std::string path_copy(path);
#ifdef WIN32
  path_copy += '\0';  // Ensure double null-termination.
  SHFILEOPSTRUCT operation = {};
  operation.wFunc = FO_DELETE;
  operation.pFrom = path_copy.c_str();
  operation.fFlags = FOF_NO_UI;
  if (SHFileOperation(&operation) != 0) {
    throw std::runtime_error(StrCat("SHFileOperation() failed for: ", path));
  }
#else
  errno = 0;
  if (!IsDirectory(path)) {
    return;
  }
  DIR* directory = opendir(path_copy.c_str());
  if (!directory) {
    throw std::runtime_error(
        StrCat("opendir() failed for \"", path, "\": ", strerror(errno))
            .c_str());
  }
  struct dirent* entry;
  while ((entry = readdir(directory))) {
    const std::string name(entry->d_name);
    if (name == "." || name == "..") {
      continue;
    }
    const std::string full_path(JoinPath(path, name));
    if (IsDirectory(full_path)) {
      RemoveAll(full_path);
    } else {
      std::remove(full_path.c_str());
    }
  }
  if (errno != 0) {
    throw std::runtime_error(StrCat("readdir() failed: ", strerror(errno)));
  }
  closedir(directory);
  if (rmdir(path_copy.c_str()) == -1) {
    throw std::runtime_error(StrCat("rmdir() failed: ", strerror(errno)));
  }
#endif
}

void CopyFile(StringPiece from, StringPiece to) {
  std::ifstream input(std::string(from),
                      std::ios_base::in | std::ios_base::binary);
  std::ofstream output(std::string(to), std::ios_base::out |
                                            std::ios_base::trunc |
                                            std::ios_base::binary);
  output << input.rdbuf();
}
