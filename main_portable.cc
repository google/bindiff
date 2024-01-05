// Copyright 2011-2024 Google LLC
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

// Command-line version of BinDiff.

#include <algorithm>
#include <atomic>
#include <cassert>
#include <csignal>
#include <cstdint>
#include <cstdio>
#include <cstdlib>
#include <fstream>
#include <functional>
#include <iomanip>
#include <iostream>  // NOLINT
#include <memory>
#include <sstream>  // NOLINT
#include <stdexcept>
#include <string>
#include <thread>  // NOLINT
#include <utility>
#include <vector>

#include "third_party/absl/base/const_init.h"
#include "third_party/absl/container/flat_hash_map.h"
#include "third_party/absl/flags/flag.h"
#include "third_party/absl/flags/internal/usage.h"
#include "third_party/absl/flags/parse.h"
#include "third_party/absl/flags/usage.h"
#include "third_party/absl/flags/usage_config.h"
#include "third_party/absl/log/globals.h"
#include "third_party/absl/log/initialize.h"
#include "third_party/absl/memory/memory.h"
#include "third_party/absl/status/status.h"
#include "third_party/absl/strings/ascii.h"
#include "third_party/absl/strings/match.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/strings/str_format.h"
#include "third_party/absl/strings/str_split.h"
#include "third_party/absl/synchronization/mutex.h"
#include "third_party/zynamics/bindiff/call_graph.h"
#include "third_party/zynamics/bindiff/config.h"
#include "third_party/zynamics/bindiff/database_writer.h"
#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/log_writer.h"
#include "third_party/zynamics/bindiff/match/call_graph.h"
#include "third_party/zynamics/bindiff/match/context.h"
#include "third_party/zynamics/bindiff/match/flow_graph.h"
#include "third_party/zynamics/bindiff/start_ui.h"
#include "third_party/zynamics/bindiff/version.h"
#include "third_party/zynamics/binexport/binexport2.pb.h"
#include "third_party/zynamics/binexport/util/filesystem.h"
#include "third_party/zynamics/binexport/util/format.h"
#include "third_party/zynamics/binexport/util/idb_export.h"
#include "third_party/zynamics/binexport/util/status_macros.h"
#include "third_party/zynamics/binexport/util/timer.h"
#include "third_party/zynamics/binexport/util/types.h"

ABSL_FLAG(bool, logo, true, "display version/copyright information");
ABSL_FLAG(bool, ui, false, "launch the BinDiff UI");
ABSL_FLAG(std::string, primary, "", "primary input file or path in batch mode");
ABSL_FLAG(std::string, secondary, "", "secondary input file (optional)");
ABSL_FLAG(std::string, output_dir, "",
          "output path, defaults to current directory");
ABSL_FLAG(std::vector<std::string>, output_format, {"bin"},
          "comma-separated list of output formats: log (text file), bin[ary] "
          "(BinDiff database loadable by the disassembler plugins). An empty "
          "value or using 'none' will disable writing to disk completely.");
ABSL_FLAG(bool, md_index, false, "dump MD indices (will not diff anything)");
ABSL_FLAG(bool, export, false,
          "batch export .idb files from input directory to BinExport format");
ABSL_FLAG(bool, ls, false,
          "list hash/filenames for all .BinExport files in input directory");
ABSL_FLAG(std::string, config, "", "specify config file name");
ABSL_FLAG(bool, print_config, false,
          "print parsed configuration to stdout and exit");

namespace security::bindiff {

using ::security::binexport::CollectIdbsToExport;
using ::security::binexport::FormatAddress;
using ::security::binexport::HumanReadableDuration;
using ::security::binexport::IdbExporter;
using ::security::binexport::kBinExportExtension;

ABSL_CONST_INIT absl::Mutex g_queue_mutex(absl::kConstInit);
std::atomic<bool> g_wants_to_quit = ATOMIC_VAR_INIT(false);

bool g_output_binary = false;
bool g_output_log = false;

using DiffPairList = std::list<std::pair<std::string, std::string>>;

void PrintMessage(absl::string_view message) {
  auto size = message.size();
  fwrite(message.data(), 1 /* Size */, size, stdout);
  fwrite("\n", 1 /* Size */, 1 /* Count */, stdout);
}

void PrintErrorMessage(absl::string_view message) {
  auto size = message.size();
  fwrite(message.data(), 1 /* Size */, size, stderr);
  fwrite("\n", 1 /* Size */, 1 /* Count */, stderr);
}

// This function will try and create a fully specified filename no longer than
// 250 characters. It'll truncate part1 and part2, leaving all other fragments
// as is. If it is not possible to get a short enough name it'll return an
// error.
absl::StatusOr<std::string> GetTruncatedFilename(
    const std::string& path /* Must include trailing slash */,
    const std::string& part1 /* Potentially truncated */,
    const std::string& middle,
    const std::string& part2 /* Potentially truncated */,
    const std::string& extension) {
  constexpr size_t kMaxFilename = 250;

  const size_t length = path.size() + part1.size() + middle.size() +
                        part2.size() + extension.size();
  if (length <= kMaxFilename) {
    return absl::StrCat(path, part1, middle, part2, extension);
  }

  size_t overflow = length - kMaxFilename;

  // First, shorten the longer of the two strings.
  std::string one = part1;
  std::string two = part2;
  if (part1.size() > part2.size()) {
    one = part1.substr(
        0, std::max(part2.size(),
                    part1.size() > overflow ? part1.size() - overflow : 0));
    overflow -= part1.size() - one.size();
  } else if (part2.size() > part1.size()) {
    two = part2.substr(
        0, std::max(part1.size(),
                    part2.size() > overflow ? part2.size() - overflow : 0));
    overflow -= part2.size() - two.size();
  }
  if (!overflow) {
    return path + one + middle + two + extension;
  }

  // Second, if that still wasn't enough, shorten both strings equally.
  assert(one.size() == two.size());
  if (overflow / 2 >= one.size()) {
    return absl::InvalidArgumentError(
        absl::StrCat("Cannot create a valid filename, choose shorter input "
                     "names/directories: ",
                     path, part1, middle, part2, extension));
  }
  return absl::StrCat(path, part1.substr(0, one.size() - overflow / 2), middle,
                      part2.substr(0, two.size() - overflow / 2), extension);
}

class DifferThread {
 public:
  explicit DifferThread(const std::string& path, const std::string& out_path,
                        DiffPairList* files);  // Not owned.

  void operator()();

 private:
  DiffPairList* file_queue_;
  std::string path_;
  std::string out_path_;
};

DifferThread::DifferThread(const std::string& path, const std::string& out_path,
                           DiffPairList* files)
    : file_queue_(files), path_(path), out_path_(out_path) {}

void DifferThread::operator()() {
  const MatchingSteps call_graph_steps = GetDefaultMatchingSteps();
  const MatchingStepsFlowGraph basic_block_steps =
      GetDefaultMatchingStepsBasicBlock();
  Instruction::Cache instruction_cache;
  FlowGraphs flow_graphs1;
  FlowGraphs flow_graphs2;
  CallGraph call_graph1;
  CallGraph call_graph2;
  std::string file1;
  std::string file2;
  std::string last_file1;
  std::string last_file2;
  ScopedCleanup cleanup(&flow_graphs1, &flow_graphs2, &instruction_cache);

  auto did_handle_error = [&](const absl::Status& status) {
    if (status.ok()) {
      return false;  // Error not handled
    }
    PrintErrorMessage(absl::StrCat("while diffing ", file1, " vs ", file2, ": ",
                                   status.message()));
    last_file1.clear();
    last_file2.clear();
    return true;  // Error handled
  };

  do {
    Timer<> timer;
    {
      // Pop pair from todo queue.
      absl::MutexLock lock(&g_queue_mutex);
      if (file_queue_->empty()) {
        break;
      }
      std::tie(file1, file2) = file_queue_->front();
      file_queue_->pop_front();
    }

    // We need to keep the cache around if one file stays the same
    if (last_file1 != file1 && last_file2 != file2) {
      instruction_cache.clear();
    }

    // Perform setup and diff.
    // TODO(cblichmann): Consider inverted pairs as well, i.e. file1 ==
    //                   last_file2.
    if (last_file1 != file1) {
      PrintMessage(absl::StrCat("Reading ", file1));
      DeleteFlowGraphs(&flow_graphs1);
      if (did_handle_error(Read(JoinPath(path_, file1), &call_graph1,
                                &flow_graphs1, /*flow_graph_infos=*/nullptr,
                                &instruction_cache))) {
        continue;
      }
    } else {
      ResetMatches(&flow_graphs1);
    }

    if (last_file2 != file2) {
      PrintMessage(absl::StrCat("Reading ", file2));
      DeleteFlowGraphs(&flow_graphs2);
      if (did_handle_error(Read(JoinPath(path_, file2), &call_graph2,
                                &flow_graphs2, /*flow_graph_infos=*/nullptr,
                                &instruction_cache))) {
        continue;
      }
    } else {
      ResetMatches(&flow_graphs2);
    }

    PrintMessage(absl::StrCat("Diffing ", file1, " vs ", file2));

    FixedPoints fixed_points;
    MatchingContext context(call_graph1, call_graph2, flow_graphs1,
                            flow_graphs2, fixed_points);
    Diff(&context, call_graph_steps, basic_block_steps);

    Histogram histogram;
    Counts counts;
    GetCountsAndHistogram(flow_graphs1, flow_graphs2, fixed_points, &histogram,
                          &counts);
    const double similarity =
        GetSimilarityScore(call_graph1, call_graph2, histogram, counts);
    Confidences confidences;
    const double confidence = GetConfidence(histogram, &confidences);

    ChainWriter writer;
    if (g_output_log) {
      absl::StatusOr<std::string> filename = GetTruncatedFilename(
          out_path_ + kPathSeparator, call_graph1.GetFilename(), "_vs_",
          call_graph2.GetFilename(), ".results");
      if (did_handle_error(filename.status())) {
        continue;
      }
      writer.Add(absl::make_unique<ResultsLogWriter>(*filename));
    }
    if (g_output_binary) {
      absl::StatusOr<std::string> filename = GetTruncatedFilename(
          out_path_ + kPathSeparator, call_graph1.GetFilename(), "_vs_",
          call_graph2.GetFilename(), ".BinDiff");
      if (did_handle_error(filename.status())) {
        continue;
      }
      auto database_writer = DatabaseWriter::Create(
          *filename,
          DatabaseWriter::Options().set_include_function_names(
              !config::Proto().binary_format().exclude_function_names()));
      if (did_handle_error(database_writer.status())) {
        continue;
      }
      writer.Add(std::move(*database_writer));
    }

    if (!writer.empty()) {
      PrintMessage("Writing results");
      if (did_handle_error(writer.Write(call_graph1, call_graph2, flow_graphs1,
                                        flow_graphs2, fixed_points))) {
        continue;
      }
    }

    std::string result_message = absl::StrCat(
        file1, " vs ", file2, " (", HumanReadableDuration(timer.elapsed()),
        "):\tsimilarity:\t", similarity, "\tconfidence:\t", confidence);
    for (int i = 0; i < counts.ui_entry_size(); ++i) {
      const auto& [name, value] = counts.GetEntry(i);
      absl::StrAppend(&result_message, "\n\t", name, ":\t", value);
    }
    PrintMessage(result_message);

    last_file1 = file1;
    last_file2 = file2;
  } while (!g_wants_to_quit);
}

absl::Status ListFiles(const std::string& path) {
  std::vector<std::string> entries;
  if (absl::Status status = GetDirectoryEntries(path, &entries); !status.ok()) {
    return absl::FailedPreconditionError(
        absl::StrCat("error listing files: ", status.message()));
  }

  for (const auto& entry : entries) {
    const std::string file_path = JoinPath(path, entry);
    if (IsDirectory(file_path) ||
        absl::AsciiStrToUpper(GetFileExtension(file_path)) != ".BINEXPORT") {
      continue;
    }
    std::ifstream file(file_path, std::ios_base::binary);
    BinExport2 proto;
    if (proto.ParseFromIstream(&file)) {
      const auto& meta_information = proto.meta_information();
      PrintErrorMessage(absl::StrCat(file_path, ": ",
                                     meta_information.executable_id(), " (",
                                     meta_information.executable_name(), ")"));
    }
  }
  return absl::OkStatus();
}

absl::Status BatchDiff(const std::string& path,
                       const std::string& reference_file,
                       const std::string& out_path) {
  const std::string full_path = GetFullPathName(path);
  const std::string full_reference_file =
      !reference_file.empty() ? GetFullPathName(reference_file) : "";
  const std::string full_out_path = GetFullPathName(out_path);

  std::vector<std::string> binexports;
  NA_ASSIGN_OR_RETURN(std::vector<std::string> idbs,
                      CollectIdbsToExport(full_path, &binexports));

  const auto& config = config::Proto();
  const int num_threads = config.num_threads() > 0
                              ? config.num_threads()
                              : std::thread::hardware_concurrency();
  IdbExporter exporter(
      IdbExporter::Options()
          .set_export_dir(full_out_path)
          .set_num_threads(num_threads)
          .set_ida_dir(config.ida().directory())
          .set_x86_noreturn_heuristic(
              config.ida().binexport_x86_noreturn_heuristic()));
  for (const std::string& idb : idbs) {
    const std::string full_idb_path = JoinPath(full_path, idb);
    if (GetFileSize(full_idb_path).value_or(0) > 0) {
      exporter.AddDatabase(full_idb_path);
      binexports.push_back(ReplaceFileExtension(idb, kBinExportExtension));
    } else {
      PrintMessage(
          absl::StrCat("Warning: skipping empty file ", full_idb_path));
    }
  }

  // Create todo list of file pairs.
  DiffPairList files;
  for (auto it = binexports.begin(), end = binexports.end(); it != end; ++it) {
    for (auto jt = binexports.begin(); jt != end; ++jt) {
      if (it == jt) {
        continue;
      }
      if (full_reference_file.empty() ||
          full_reference_file == JoinPath(full_path, *it)) {
        files.emplace_back(*it, *jt);
      }
    }
  }

  Timer<> timer;
  int num_exported = 0;
  exporter
      .Export([&num_exported](const absl::Status& status,
                              const std::string& idb_path, double elapsed) {
        if (!status.ok()) {
          PrintErrorMessage(status.message());
        } else {
          PrintMessage(absl::StrCat(HumanReadableDuration(elapsed), "\t",
                                    GetFileSize(idb_path).value_or(0), "\t",
                                    idb_path));
          ++num_exported;
        }
        return !g_wants_to_quit;
      })
      .IgnoreError();
  const auto export_time = timer.elapsed();
  PrintMessage(absl::StrCat(num_exported, " files exported in ",
                            HumanReadableDuration(export_time)));

  timer.restart();
  if (!absl::GetFlag(FLAGS_export)) {  // Perform diff
    std::vector<std::thread> threads;
    threads.reserve(num_threads);
    int num_diffed = files.size();
    for (int i = 0; i < num_threads; ++i) {
      threads.emplace_back(DifferThread(full_path, full_out_path, &files));
    }
    for (auto& thread : threads) {
      thread.join();
    }
    const auto diff_time = timer.elapsed();
    PrintMessage(absl::StrCat(num_diffed, " pairs diffed in ",
                              HumanReadableDuration(diff_time)));
  }
  return absl::OkStatus();
}

void DumpMdIndices(const CallGraph& call_graph, const FlowGraphs& flow_graphs) {
  std::cout << "\n"
            << call_graph.GetFilename() << "\n"
            << call_graph.GetMdIndex();
  for (auto i = flow_graphs.cbegin(), end = flow_graphs.cend(); i != end; ++i) {
    std::cout << "\n"
              << FormatAddress((*i)->GetEntryPointAddress()) << "\t"
              << std::fixed << std::setprecision(12) << (*i)->GetMdIndex()
              << "\t" << ((*i)->IsLibrary() ? "Library" : "Non-library");
  }
  std::cout << std::endl;
}

absl::Status BatchDumpMdIndices(const std::string& path) {
  std::vector<std::string> entries;
  if (absl::Status status = GetDirectoryEntries(path, &entries); !status.ok()) {
    return absl::UnknownError(absl::StrCat("error listing files in `", path,
                                           "`: ", status.message()));
  }
  for (const auto& entry : entries) {
    std::string file_path = JoinPath(path, entry);
    if (IsDirectory(file_path) ||
        absl::AsciiStrToUpper(GetFileExtension(file_path)) != ".CALL_GRAPH") {
      continue;
    }

    CallGraph call_graph;
    FlowGraphs flow_graphs;
    Instruction::Cache instruction_cache;
    ScopedCleanup cleanup(&flow_graphs, 0, &instruction_cache);
    FlowGraphInfos infos;
    NA_RETURN_IF_ERROR(
        Read(file_path, &call_graph, &flow_graphs, &infos, &instruction_cache));
    DumpMdIndices(call_graph, flow_graphs);
  }
  return absl::OkStatus();
}

void SignalHandler(int code) {
  static int signal_count = 0;
  switch (code) {
#ifdef WIN32
    case SIGBREAK:  // Ctrl-Break, not available on Unix
#endif
    case SIGINT:  // Ctrl-C
      if (++signal_count < 3) {
        PrintErrorMessage("shutting down after current operations finish");
        g_wants_to_quit = true;
      } else {
        PrintErrorMessage("forcefully terminating process");
        exit(1);
      }
      break;
  }
}

// Install Abseil Flags' library usage callbacks. This needs to be done before
// any operation that may call one of the callbacks.
void InstallFlagsUsageConfig() {
  absl::FlagsUsageConfig usage_config;
  usage_config.contains_help_flags = [](absl::string_view filename) {
    return !absl::StartsWith(filename, "core library");
  };
  usage_config.contains_helpshort_flags = usage_config.contains_help_flags;
  usage_config.version_string = []() {
    return absl::StrCat(kBinDiffName, " ", kBinDiffDetailedVersion, "\n");
  };
  usage_config.normalize_filename =
      [](absl::string_view filename) -> std::string {
    return absl::StartsWith(filename, "absl") ? "core library" : "this binary";
  };
  absl::SetFlagsUsageConfig(usage_config);
}

absl::Status BinDiffMain(int argc, char* argv[]) {
  const std::string binary_name = Basename(argv[0]);
  const std::string usage = absl::StrFormat(
      "Find similarities and differences in disassembled code.\n"
      "Usage: %1$s [OPTION] DIRECTORY\n"
      "  or:  %1$s [OPTION] PRIMARY SECONDARY\n"
      "  or:  %1$s [OPTION] --primary=PRIMARY [--secondary=SECONDARY]\n"
      "  or:  %1$s --ui [UIOPTION...]\n"
      "In the 1st form, diff all files in a directory against each other. If\n"
      "the directory contains IDA Pro databases these will be exported first.\n"
      "In the 2nd and 3rd form, diff two previously exported binaries.\n"
      "In the 4th form, launch the BinDiff UI.",
      binary_name);
  absl::SetProgramUsageMessage(usage);
  InstallFlagsUsageConfig();
  std::vector<std::string> positional;
  {
    const std::vector<char*> parsed_argv = absl::ParseCommandLine(argc, argv);
    positional.assign(parsed_argv.begin() + 1, parsed_argv.end());
  }

  absl::SetStderrThreshold(absl::LogSeverityAtLeast::kInfo);
  absl::InitializeLog();

#ifdef WIN32
  signal(SIGBREAK, SignalHandler);
#endif
  signal(SIGINT, SignalHandler);

  const std::string current_path = GetCurrentDirectory();
  if (absl::GetFlag(FLAGS_output_dir).empty()) {
    absl::SetFlag(&FLAGS_output_dir, current_path);
  }

  if (absl::GetFlag(FLAGS_logo) && !absl::GetFlag(FLAGS_print_config)) {
    PrintMessage(absl::StrCat(kBinDiffName, " ", kBinDiffDetailedVersion, ", ",
                              kBinDiffCopyright));
  }

  auto& config = config::Proto();
  if (!absl::GetFlag(FLAGS_config).empty()) {
    NA_ASSIGN_OR_RETURN(auto loaded_config,
                        config::LoadFromFile(absl::GetFlag(FLAGS_config)));
    config = config::Defaults();
    config::MergeInto(loaded_config, config);
  }

  // Print configuration to stdout if requested
  if (absl::GetFlag(FLAGS_print_config)) {
    const std::string config = config::AsJsonString(config::Proto());
    const std::vector<absl::string_view> lines = absl::StrSplit(config, '\n');
    if (lines.front().empty()) {
      return absl::InternalError("Serialization error");
    }
    std::for_each(lines.begin(), lines.end(), PrintMessage);
    return absl::OkStatus();
  }

  // Launch Java UI if requested
  if (binary_name == "bindiff_ui" || absl::GetFlag(FLAGS_ui)) {
    NA_RETURN_IF_ERROR(StartUiWithOptions(
        positional, StartUiOptions{}
                        .set_java_binary(config.ui().java_binary())
                        .set_java_vm_options(config.ui().java_vm_option())
                        .set_max_heap_size_mb(config.ui().max_heap_size_mb())
                        .set_bindiff_dir(config.directory())));
    return absl::OkStatus();
  }

  // This initializes static variables before the threads get to them
  if (GetDefaultMatchingSteps().empty() ||
      GetDefaultMatchingStepsBasicBlock().empty()) {
    return absl::FailedPreconditionError("Config file invalid");
  }

  for (const auto& entry : absl::GetFlag(FLAGS_output_format)) {
    const std::string format = absl::AsciiStrToUpper(entry);
    if (format == "BIN" || format == "BINARY") {
      g_output_binary = true;
    } else if (format == "LOG") {
      g_output_log = true;
    } else if (format == "NONE") {
      if (absl::GetFlag(FLAGS_output_format).size() != 1) {
        return absl::InvalidArgumentError(absl::StrCat(
            "If specified, '", entry, "' needs to be the only output format"));
      }
    } else {
      return absl::InvalidArgumentError(
          absl::StrCat("Invalid output format: ", entry));
    }
  }

  // Prefer named arguments over positional ones
  std::string primary = absl::GetFlag(FLAGS_primary);
  std::string secondary = absl::GetFlag(FLAGS_secondary);
  {
    auto pos_it = positional.begin();
    auto pos_end = positional.end();
    if (primary.empty() && pos_it != pos_end) {
      primary = *pos_it++;
    }
    if (secondary.empty() && pos_it != pos_end) {
      secondary = *pos_it++;
    }
    if (pos_it != pos_end) {
      return absl::InvalidArgumentError("Extra arguments on command line");
    }
  }

  if (primary.empty()) {
    return absl::InvalidArgumentError("Need primary input (--primary)");
  }

  try {
    Timer<> timer;
    bool done_something = false;

    std::unique_ptr<CallGraph> call_graph1;
    std::unique_ptr<CallGraph> call_graph2;
    Instruction::Cache instruction_cache;
    FlowGraphs flow_graphs1;
    FlowGraphs flow_graphs2;
    ScopedCleanup cleanup(&flow_graphs1, &flow_graphs2, &instruction_cache);

    if (absl::GetFlag(FLAGS_output_dir) == current_path /* Defaulted */ &&
        IsDirectory(primary)) {
      absl::SetFlag(&FLAGS_output_dir, primary);
    }

    if (!IsDirectory(absl::GetFlag(FLAGS_output_dir))) {
      return absl::FailedPreconditionError(absl::StrCat(
          "Output parameter (--output_dir) must be a writable directory: ",
          absl::GetFlag(FLAGS_output_dir)));
    }

    if (FileExists(primary)) {
      // Primary from file system.
      FlowGraphInfos infos;
      call_graph1 = absl::make_unique<CallGraph>();
      NA_RETURN_IF_ERROR(Read(primary, call_graph1.get(), &flow_graphs1, &infos,
                              &instruction_cache));
    }

    if (IsDirectory(primary)) {
      // File system batch diff.
      if (absl::GetFlag(FLAGS_ls)) {
        NA_RETURN_IF_ERROR(ListFiles(primary));
      } else if (absl::GetFlag(FLAGS_md_index)) {
        NA_RETURN_IF_ERROR(BatchDumpMdIndices(primary));
      } else {
        NA_RETURN_IF_ERROR(
            BatchDiff(primary, secondary, absl::GetFlag(FLAGS_output_dir)));
      }
      done_something = true;
    }

    if (absl::GetFlag(FLAGS_md_index) && call_graph1 != nullptr) {
      DumpMdIndices(*call_graph1, flow_graphs1);
      done_something = true;
    }

    if (!secondary.empty() && FileExists(secondary)) {
      // secondary from filesystem
      FlowGraphInfos infos;
      call_graph2 = absl::make_unique<CallGraph>();
      NA_RETURN_IF_ERROR(Read(secondary, call_graph2.get(), &flow_graphs2,
                              &infos, &instruction_cache));
    }

    if ((!done_something && !FileExists(primary) && !IsDirectory(primary)) ||
        (!secondary.empty() && !FileExists(secondary) &&
         !IsDirectory(secondary))) {
      return absl::FailedPreconditionError(
          "Invalid inputs, --primary and --secondary must point to valid "
          "files/directories.");
    }

    if (call_graph1.get() && call_graph2.get()) {
      const int edges1 = num_edges(call_graph1->GetGraph());
      const int vertices1 = num_vertices(call_graph1->GetGraph());
      const int edges2 = num_edges(call_graph2->GetGraph());
      const int vertices2 = num_vertices(call_graph2->GetGraph());
      PrintMessage(
          absl::StrCat("Setup: ", HumanReadableDuration(timer.elapsed())));
      PrintMessage(absl::StrCat("primary:   ", call_graph1->GetFilename(), ": ",
                                vertices1, " functions, ", edges1, " calls"));
      PrintMessage(absl::StrCat("secondary: ", call_graph2->GetFilename(), ": ",
                                vertices2, " functions, ", edges2, " calls"));
      timer.restart();

      const MatchingSteps default_callgraph_steps(GetDefaultMatchingSteps());
      const MatchingStepsFlowGraph default_basicblock_steps(
          GetDefaultMatchingStepsBasicBlock());
      FixedPoints fixed_points;
      MatchingContext context(*call_graph1, *call_graph2, flow_graphs1,
                              flow_graphs2, fixed_points);
      Diff(&context, default_callgraph_steps, default_basicblock_steps);

      Histogram histogram;
      Counts counts;
      GetCountsAndHistogram(flow_graphs1, flow_graphs2, fixed_points,
                            &histogram, &counts);
      Confidences confidences;
      const double confidence = GetConfidence(histogram, &confidences);
      const double similarity =
          GetSimilarityScore(*call_graph1, *call_graph2, histogram, counts);

      PrintMessage(
          absl::StrCat("Matching: ", HumanReadableDuration(timer.elapsed())));
      timer.restart();

      PrintMessage(absl::StrCat(
          "matched: ", fixed_points.size(), " of ", flow_graphs1.size(), "/",
          flow_graphs2.size(), " (primary/secondary, ",
          counts[Counts::kFunctionsPrimaryNonLibrary], "/",
          counts[Counts::kFunctionsSecondaryNonLibrary], " non-library)"));

      PrintMessage(absl::StrCat("call graph MD index: primary   ",
                                call_graph1->GetMdIndex()));
      PrintMessage(absl::StrCat("                     secondary ",
                                call_graph2->GetMdIndex()));
      PrintMessage(absl::StrCat("Similarity: ", similarity * 100,
                                "% (Confidence: ", confidence * 100, "%)"));

      ChainWriter writer;
      if (g_output_log) {
        NA_ASSIGN_OR_RETURN(
            std::string filename,
            GetTruncatedFilename(
                absl::GetFlag(FLAGS_output_dir) + kPathSeparator,
                call_graph1->GetFilename(), "_vs_", call_graph2->GetFilename(),
                ".results"));
        writer.Add(std::make_unique<ResultsLogWriter>(filename));
      }
      if (g_output_binary) {
        NA_ASSIGN_OR_RETURN(
            std::string filename,
            GetTruncatedFilename(
                absl::GetFlag(FLAGS_output_dir) + kPathSeparator,
                call_graph1->GetFilename(), "_vs_", call_graph2->GetFilename(),
                ".BinDiff"));
        NA_ASSIGN_OR_RETURN(auto database_writer,
                            DatabaseWriter::Create(filename));
        writer.Add(std::move(database_writer));
      }

      if (!writer.empty()) {
        NA_RETURN_IF_ERROR(writer.Write(*call_graph1, *call_graph2,
                                        flow_graphs1, flow_graphs2,
                                        fixed_points));
        PrintMessage(absl::StrCat("Writing results: ",
                                  HumanReadableDuration(timer.elapsed())));
      }
      timer.restart();
      done_something = true;
    }

    if (!done_something) {
      absl::flags_internal::FlagsHelp(
          std::cout, "", absl::flags_internal::HelpFormat::kHumanReadable,
          usage);
    }
  } catch (const std::exception& error) {
    return absl::UnknownError(error.what());
  } catch (...) {
    return absl::UnknownError("An unknown error occurred");
  }
  return absl::OkStatus();
}

}  // namespace security::bindiff

int main(int argc, char** argv) {
  if (auto status = security::bindiff::BinDiffMain(argc, argv); !status.ok()) {
    security::bindiff::PrintErrorMessage(
        absl::StrCat("Error: ", status.message()));
    return EXIT_FAILURE;
  }
  return EXIT_SUCCESS;
}
