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

#include "third_party/zynamics/binexport/util/idb_export.h"

#include <fstream>
#include <memory>
#include <thread>  // NOLINT

#include "third_party/absl/base/attributes.h"
#include "third_party/absl/container/flat_hash_map.h"
#include "third_party/absl/container/flat_hash_set.h"
#include "third_party/absl/memory/memory.h"
#include "third_party/absl/status/status.h"
#include "third_party/absl/status/statusor.h"
#include "third_party/absl/strings/ascii.h"
#include "third_party/absl/strings/match.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/strings/str_format.h"
#include "third_party/absl/strings/str_join.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/absl/synchronization/mutex.h"
#include "third_party/absl/time/time.h"
#include "third_party/zynamics/binexport/util/filesystem.h"
#include "third_party/zynamics/binexport/util/process.h"
#include "third_party/zynamics/binexport/util/status_macros.h"
#include "third_party/zynamics/binexport/util/timer.h"

namespace security::binexport {
namespace {

constexpr const char* OptionBool(bool value) {
  return value ? "TRUE" : "FALSE";
}

}  // namespace

void IdbExporter::AddDatabase(std::string path) {
  absl::MutexLock lock{&queue_mutex_};
  idb_paths_.push_back(std::move(path));
}

absl::Status ExportDatabase(const std::string& idb_path,
                            const IdbExporter::Options& options) {
  const bool is_64bit = absl::EndsWithIgnoreCase(idb_path, kIdbExtension64);
  // Check existence first to avoid running IDA needlessly.
  if (!FileExists(idb_path)) {
    return absl::NotFoundError(absl::StrCat("File not found: " + idb_path));
  }

  std::string ida_exe = is_64bit ? options.ida_exe64 : options.ida_exe;
  if (ida_exe.empty()) {
#ifdef _WIN32
    ida_exe = is_64bit ? "ida64.exe" : "ida.exe";
#else
    ida_exe = is_64bit ? "ida64" : "ida";
#endif
  }
  std::vector<std::string> args = {
      JoinPath(options.ida_dir, ida_exe),
      "-A",
      absl::StrCat("-OBinExportModule:",
                   // Make the output name deterministic. When only using
                   // specifying a directory, BinExport will use the IDB's
                   // original executable name as the base name.
                   JoinPath(options.export_dir,
                            ReplaceFileExtension(Basename(idb_path),
                                                 kBinExportExtension))),
      absl::StrCat("-OBinExportX86NoReturnHeuristic:",
                   OptionBool(options.x86_noreturn_heuristic)),
      absl::StrCat("-OBinExportAlsoLogToStdErr:",
                   OptionBool(options.alsologtostderr)),
      "-OBinExportAutoAction:BinExportBinary",
      idb_path,
  };

  SetEnvironmentVariable("TVHEADLESS", "1");
  absl::StatusOr<int> exit_or = SpawnProcessAndWait(args);

  // Reset environment variable.
  SetEnvironmentVariable("TVHEADLESS", /*value=*/"");
  return exit_or.status();
}

absl::Status IdbExporter::Export(
    std::function<bool(const absl::Status&, const std::string&, double)>
        progress) {
  std::vector<std::thread> threads;
  threads.reserve(options_.num_threads);
  std::vector<absl::Status> statuses(options_.num_threads);
  for (int i = 0; i < options_.num_threads; ++i) {
    auto& status = statuses[i];
    threads.emplace_back([this, &status, &progress]() {
      while (true) {
        std::string idb_path;
        {
          absl::MutexLock lock{&queue_mutex_};
          if (idb_paths_.empty()) {
            break;
          }
          idb_path = std::move(idb_paths_.back());
          idb_paths_.pop_back();
        }
        Timer<> timer;
        status = ExportDatabase(idb_path, options_);
        if (progress && !progress(status, idb_path, timer.elapsed())) {
          break;
        }
      }
    });
  }
  for (auto& thread : threads) {
    thread.join();
  }

  // Return first error
  for (const auto& status : statuses) {
    NA_RETURN_IF_ERROR(status);
  }
  return absl::OkStatus();
}

absl::StatusOr<std::vector<std::string>> CollectIdbsToExport(
    absl::string_view path, std::vector<std::string>* existing_binexports) {
  std::vector<std::string> entries;
  NA_RETURN_IF_ERROR(GetDirectoryEntries(path, &entries));

  absl::flat_hash_map<std::string, std::string> split_entries;
  absl::flat_hash_map<std::string, std::string> split_binexports;
  for (const auto& entry : entries) {
    if (IsDirectory(JoinPath(path, entry))) {
      continue;
    }
    std::string filename = ReplaceFileExtension(entry, "");
    std::string ext = GetFileExtension(entry);

    const bool is_binexport = absl::EqualsIgnoreCase(ext, kBinExportExtension);
    if (is_binexport) {
      if (split_binexports.emplace(filename, ext).second) {
        split_entries.erase(filename);
        if (existing_binexports) {
          existing_binexports->push_back(absl::StrCat(filename, ext));
        }
      }
    } else if (split_binexports.count(filename) == 0) {
      const bool is_i64 = absl::EqualsIgnoreCase(ext, kIdbExtension64);
      const bool is_idb = absl::EqualsIgnoreCase(ext, kIdbExtension);
      if (is_i64 || is_idb) {
        auto& split_ext = split_entries[std::move(filename)];
        if (split_ext.empty() || absl::EqualsIgnoreCase(split_ext, ext) ||
            (is_i64 && absl::EqualsIgnoreCase(split_ext, kIdbExtension))) {
          split_ext = std::move(ext);
        }
      }
    }
  }
  entries.clear();
  for (const auto& [filename, ext] : split_entries) {
    entries.push_back(absl::StrCat(filename, ext));
  }
  entries.shrink_to_fit();
  return entries;
}

}  // namespace security::binexport
