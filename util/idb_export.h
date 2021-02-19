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

#ifndef UTIL_IDB_EXPORT_H_
#define UTIL_IDB_EXPORT_H_

#include <memory>
#include <vector>

#include "third_party/absl/status/status.h"
#include "third_party/absl/status/statusor.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/absl/synchronization/mutex.h"
#include "third_party/zynamics/binexport/types.h"

namespace security::binexport {

// File extensions for the files used by the IdbExporter.
inline constexpr const char kBinExportExtension[] = ".BinExport";
inline constexpr const char kIdbExtension[] = ".idb";
inline constexpr const char kIdbExtension64[] = ".i64";

// Exports IDA Pro databases into BinExport v2 format. If multiple databases
// were added, exports up to num_threads in parallel.
class IdbExporter {
 public:
  struct Options {
    Options& set_export_dir(std::string value) {
      export_dir = std::move(value);
      return *this;
    }

    Options& set_ida_dir(std::string value) {
      ida_dir = std::move(value);
      return *this;
    }

    Options& set_ida_exe(std::string value) {
      ida_exe = std::move(value);
      return *this;
    }

    Options& set_ida_exe64(std::string value) {
      ida_exe64 = std::move(value);
      return *this;
    }

    Options& set_num_threads(int value) {
      num_threads = value;
      return *this;
    }

    Options& set_alsologtostderr(bool value) {
      alsologtostderr = value;
      return *this;
    }

    Options& set_x86_noreturn_heuristic(bool value) {
      x86_noreturn_heuristic = value;
      return *this;
    }

    std::string export_dir;  // Directory to export the files to
    std::string ida_dir;     // IDA Pro installation directory
    std::string ida_exe;     // Name of the IDA executable, 32-bit addresses
    std::string ida_exe64;   // IDA executable, 64-bit addresses
    int num_threads = 1;
    bool alsologtostderr = false;
    bool x86_noreturn_heuristic = false;
  };

  explicit IdbExporter(Options options) : options_(std::move(options)) {}

  void AddDatabase(std::string path);

  // Performs the database export, calling progress after each database with the
  // current error status. If progress returns false, the export process is
  // stopped.
  absl::Status Export(
      std::function<bool(const absl::Status& status,
                         const std::string& idb_path, double elapsed_sec)>
          progress = nullptr);

 private:
  Options options_;
  absl::Mutex queue_mutex_;
  std::vector<std::string> idb_paths_ ABSL_GUARDED_BY(queue_mutex_);
};

// Returns a list of IDA Pro databases that need to be exported from path
// (non-recursive).
// When there are two databases with the same basename, the 64-bit one will be
// preferred (i.e., the one with an .i64 extension).
// If the existing_binexports is not nullptr, existing .BinExport files are
// added to it. The returned lists are not prefixed with path and are guaranteed
// to be disjoint with respect to their file basenames.
// Note: This function treats file extensions without regard to their case in
//       order to function well on filesystems where this may be an issue
//       (usually Windows and macOS).
absl::StatusOr<std::vector<std::string>> CollectIdbsToExport(
    absl::string_view path, std::vector<std::string>* existing_binexports);

}  // namespace security::binexport

#endif  // UTIL_IDB_EXPORT_H_
