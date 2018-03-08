// Command-line version of BinDiff.

#include <inttypes.h>
#include <signal.h>

#include <cassert>
#include <cstdio>
#include <fstream>
#include <functional>
#include <iomanip>
#include <iostream>  // NOLINT
#include <memory>
#include <mutex>    // NOLINT
#include <sstream>  // NOLINT
#include <string>
#include <thread>  // NOLINT
#include <utility>
#include <vector>

#ifdef GOOGLE
#include "base/commandlineflags.h"
#include "base/init_google.h"
#else
#include <gflags/gflags.h>

using google::ParseCommandLineFlags;
using google::SET_FLAGS_DEFAULT;
using google::SetCommandLineOptionWithMode;
using google::SetUsageMessage;
using google::ShowUsageWithFlags;
#endif  // GOOGLE
#include "base/logging.h"
#include "third_party/absl/strings/ascii.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/time/time.h"

#ifdef WIN32
// Abseil headers include Windows.h, so undo a few macros
#undef CopyFile             // winbase.h
#undef GetCurrentDirectory  // processenv.h
#undef StrCat               // shlwapi.h
#endif

#include "third_party/zynamics/bindiff/call_graph.h"
#include "third_party/zynamics/bindiff/call_graph_matching.h"
#include "third_party/zynamics/bindiff/database_writer.h"
#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/flow_graph_matching.h"
#include "third_party/zynamics/bindiff/log_writer.h"
#include "third_party/zynamics/bindiff/matching.h"
#include "third_party/zynamics/bindiff/xmlconfig.h"
#include "third_party/zynamics/binexport/binexport2.pb.h"
#include "third_party/zynamics/binexport/filesystem_util.h"
#include "third_party/zynamics/binexport/format_util.h"
#include "third_party/zynamics/binexport/timer.h"
#include "third_party/zynamics/binexport/types.h"

using security::binexport::HumanReadableDuration;

// Note: We cannot use new-style flags here because third-party gflags does not
//       support the new syntax yet.
DEFINE_bool(nologo, false, "do not display version/copyright information");
DEFINE_string(primary, "", "primary input file or path in batch mode");
DEFINE_string(secondary, "", "secondary input file (optional)");
DEFINE_string(output_dir, "", "output path, defaults to current directory");
DEFINE_bool(log_format, false, "write results in log file format");
DEFINE_bool(bin_format, false,
            "write results in binary file format that can be loaded by the "
            "BinDiff IDA plugin or the GUI");
DEFINE_bool(md_index, false, "dump MD indices (will not diff anything)");
DEFINE_bool(export, false,
            "batch export .idb files from input directory to BinExport format");
DEFINE_bool(ls, false,
            "list hash/filenames for all .BinExport files in input directory");
DEFINE_string(config, "", "specify config file name");

static const char kBinExportVersion[] = "10";  // Exporter version to use.

std::mutex g_queue_mutex;
volatile bool g_wants_to_quit = false;

using TFiles = std::list<std::pair<string, string>>;
using TUniqueFiles = std::set<string>;

void PrintMessage(absl::string_view message) {
  auto size = message.size();
  fwrite(message.data(), 1 /* Size */, size, stdout);
  fwrite("\n", 1 /* Size */, 1 /* Count */, stdout);
#ifdef GOOGLE
  // If writing to logfiles is enabled, log the message.
  LOG_IF(INFO, base::GetFlag(FLAGS_logtostderr)) << message;
#endif
}

void PrintErrorMessage(absl::string_view message) {
  auto size = message.size();
  fwrite(message.data(), 1 /* Size */, size, stderr);
  fwrite("\n", 1 /* Size */, 1 /* Count */, stderr);
#ifdef GOOGLE
  // If writing to logfiles is enabled, log the message.
  LOG_IF(ERROR, base::GetFlag(FLAGS_logtostderr)) << message;
#endif
}

#ifndef GOOGLE
void UnprefixedLogHandler(google::protobuf::LogLevel level,
                          const char* filename, int line,
                          const string& message) {
  fwrite(message.data(), 1 /* Size */, message.size(), stdout);
  fwrite("\n", 1 /* Size */, 1 /* Count */, stdout);
}
#endif

// This function will try and create a fully specified filename no longer than
// 250 characters. It'll truncate part1 and part2, leaving all other fragments
// as is. If it is not possible to get a short enough name it'll throw an
// exception.
string GetTruncatedFilename(
    const string& path /* Must include trailing slash */,
    const string& part1 /* Potentially truncated */, const string& middle,
    const string& part2 /* Potentially truncated */, const string& extension) {
  enum { kMaxFilename = 250 };

  const string::size_type length = path.size() + part1.size() +
                                        middle.size() + part2.size() +
                                        extension.size();
  if (length <= kMaxFilename) {
    return path + part1 + middle + part2 + extension;
  }

  string::size_type overflow = length - kMaxFilename;

  // First, shorten the longer of the two strings.
  string one(part1);
  string two(part2);
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
    throw std::runtime_error(
        string("cannot create a valid filename, choose shorter input "
                    "names or directories: '") +
        path + part1 + middle + part2 + extension + "'");
  }
  return path + part1.substr(0, one.size() - overflow / 2) + middle +
         part2.substr(0, two.size() - overflow / 2) + extension;
}

class DifferThread {
 public:
  explicit DifferThread(const string& path, const string& out_path,
                        TFiles* files);  // Not owned.
  void operator()();

 private:
  TFiles* file_queue_;
  string path_;
  string out_path_;
};

DifferThread::DifferThread(const string& path, const string& out_path,
                           TFiles* files)
    : file_queue_(files), path_(path), out_path_(out_path) {}

void DifferThread::operator()() {
  const MatchingSteps default_callgraph_steps(GetDefaultMatchingSteps());
  const MatchingStepsFlowGraph default_basicblock_steps(
      GetDefaultMatchingStepsBasicBlock());

  Instruction::Cache instruction_cache;
  FlowGraphs flow_graphs1;
  FlowGraphs flow_graphs2;
  CallGraph call_graph1;
  CallGraph call_graph2;
  string last_file1;
  string last_file2;
  ScopedCleanup cleanup(&flow_graphs1, &flow_graphs2, &instruction_cache);
  do {
    string file1;
    string file2;
    try {
      Timer<> timer;
      {
        // Pop pair from todo queue.
        std::lock_guard<std::mutex> lock(g_queue_mutex);
        if (file_queue_->empty()) {
          break;
        }
        file1 = file_queue_->front().first;
        file2 = file_queue_->front().second;
        file_queue_->pop_front();
      }

      // We need to keep the cache around if one file stays the same
      if (last_file1 != file1 && last_file2 != file2) {
        instruction_cache.Clear();
      }

      // Perform setup and diff.
      // TODO(soerenme): Consider inverted pairs as well, i.e. file1 ==
      //                 last_file2.
      if (last_file1 != file1) {
        PrintMessage(absl::StrCat("reading ", file1));
        DeleteFlowGraphs(&flow_graphs1);
        FlowGraphInfos infos;
        Read(JoinPath(path_, file1 + ".BinExport"), &call_graph1, &flow_graphs1,
             &infos, &instruction_cache);
      } else {
        ResetMatches(&flow_graphs1);
      }

      if (last_file2 != file2) {
        PrintMessage(absl::StrCat("reading ", file2));
        DeleteFlowGraphs(&flow_graphs2);
        FlowGraphInfos infos;
        Read(JoinPath(path_, file2 + ".BinExport"), &call_graph2, &flow_graphs2,
             &infos, &instruction_cache);
      } else {
        ResetMatches(&flow_graphs2);
      }

      PrintMessage(absl::StrCat("diffing ", file1, " vs ", file2));

      FixedPoints fixed_points;
      MatchingContext context(call_graph1, call_graph2, flow_graphs1,
                              flow_graphs2, fixed_points);
      Diff(&context, default_callgraph_steps, default_basicblock_steps);

      Histogram histogram;
      Counts counts;
      GetCountsAndHistogram(flow_graphs1, flow_graphs2, fixed_points,
                            &histogram, &counts);
      const double similarity =
          GetSimilarityScore(call_graph1, call_graph2, histogram, counts);
      Confidences confidences;
      const double confidence = GetConfidence(histogram, &confidences);

      PrintMessage("writing results");
      {
        ChainWriter writer;
        if (FLAGS_log_format) {
          writer.Add(std::make_shared<ResultsLogWriter>(GetTruncatedFilename(
              out_path_ + kPathSeparator, call_graph1.GetFilename(), "_vs_",
              call_graph2.GetFilename(), ".results")));
        }
        if (FLAGS_bin_format || writer.IsEmpty()) {
          writer.Add(std::make_shared<DatabaseWriter>(GetTruncatedFilename(
              out_path_ + kPathSeparator, call_graph1.GetFilename(), "_vs_",
              call_graph2.GetFilename(), ".BinDiff")));
        }

        writer.Write(call_graph1, call_graph2, flow_graphs1, flow_graphs2,
                     fixed_points);

        PrintMessage(absl::StrCat(
            file1, " vs ", file2, " (", HumanReadableDuration(timer.elapsed()),
            "):\tsimilarity:\t", similarity, "\tconfidence:\t", confidence));
        for (const auto& entry : counts) {
          PrintMessage(absl::StrCat("\n\t", entry.first, ":\t", entry.second));
        }
      }

      last_file1 = file1;
      last_file2 = file2;
    } catch (const std::bad_alloc&) {
      PrintErrorMessage(
          absl::StrCat("out of memory diffing ", file1, " vs ", file2));
      last_file1.clear();
      last_file2.clear();
    } catch (const std::exception& error) {
      PrintErrorMessage(absl::StrCat("while diffing ", file1, " vs ", file2,
                                     ": ", error.what()));

      last_file1.clear();
      last_file2.clear();
    }
  } while (!g_wants_to_quit);
}

class ExporterThread {
 public:
  explicit ExporterThread(const string& in_path,
                          const string& out_path,
                          const string& ida_dir,
                          const string& ida_exe,
                          const string& ida_exe64, TUniqueFiles* files)
      : files_(files),
        in_path_(in_path),
        out_path_(out_path),
        ida_dir_(ida_dir),
        ida_exe_(ida_exe),
        ida_exe64_(ida_exe64) {}

  void operator()();

 private:
  TUniqueFiles* files_;
  string in_path_;
  string out_path_;
  string ida_dir_;
  string ida_exe_;
  string ida_exe64_;
};

void ExporterThread::operator()() {
  do {
    Timer<> timer;
    string file;
    {
      std::lock_guard<std::mutex> lock(g_queue_mutex);
      if (files_->empty()) {
        return;
      }
      file = *files_->begin();
      files_->erase(files_->begin());
    }

    // TODO(cblichmann): Bug: What if we have the same basename but as .idb
    //                   _and_ .i64?
    bool ida64 = false;
    auto in_file(JoinPath(in_path_, file + ".idb"));
    if (!FileExists(in_file)) {
      in_file = JoinPath(in_path_, file + ".i64");
      if (!FileExists(in_file)) {
        PrintErrorMessage(absl::StrCat("file not found: ", in_file));
        continue;
      }
      ida64 = true;
    }

    // TODO(cblichmann): Bug: If outpath is a relative path like "." IDA won't
    //                   work. We need to fully expand it first.
    string status_message;
    std::vector<string> args;
    args.push_back(JoinPath(ida_dir_, !ida64 ? ida_exe_ : ida_exe64_));
    args.push_back("-A");
    args.push_back("-OExporterModule:" + out_path_);
#ifndef WIN32
    args.push_back("-S" + JoinPath(out_path_, "run_ida.idc"));
#else
    args.push_back("-S\"" + JoinPath(out_path_, "run_ida.idc") + "\"");
#endif
    args.push_back(in_file);
    if (!SpawnProcess(args, true /* Wait */, &status_message)) {
      PrintErrorMessage(absl::StrCat("failed to spawn IDA export process: ",
                                     GetLastOsError()));
      return;
    }

    PrintMessage(absl::StrCat(HumanReadableDuration(timer.elapsed()), "\t",
                              GetFileSize(in_file), "\t", file));
  } while (!g_wants_to_quit);
}

void CreateIdaScript(const string& out_path) {
  string path(JoinPath(out_path, "run_ida.idc"));
  std::ofstream file(path);
  if (!file) {
    throw std::runtime_error(
        absl::StrCat("could not create IDC script: ", out_path));
  }
  file << "#include <idc.idc>\n"
       << "static main()\n"
       << "{\n"
       << "\tBatch(0);\n"
       << "\tWait();\n"
       << "\tExit(1 - RunPlugin(\"binexport" << kBinExportVersion
       << "\", 2));\n"
       << "}\n";
}

void DeleteIdaScript(const string& out_path) {
  string path(JoinPath(out_path, "run_ida.idc"));
  std::remove(path.c_str());
}

void ListFiles(const string& path) {
  std::vector<string> entries;
  GetDirectoryEntries(path, &entries);

  TUniqueFiles files;
  for (const auto& entry : entries) {
    const auto file_path(JoinPath(path, entry));
    if (IsDirectory(file_path)) {
      continue;
    }
    const auto extension = absl::AsciiStrToUpper(GetFileExtension(file_path));
    if (extension != ".BINEXPORT") {
      continue;
    }
    std::ifstream file(file_path, std::ios_base::binary);
    BinExport2 proto;
    if (proto.ParseFromIstream(&file)) {
      const auto& meta_information = proto.meta_information();
      PrintErrorMessage(absl::StrCat(file_path, ": ",
                                     meta_information.executable_id(), " (",
                                     meta_information.executable_name(), ")"));
      continue;
    }
  }
}

void BatchDiff(const string& path, const string& reference_file,
               const string& out_path) {
  // Collect idb files to diff.
  std::vector<string> entries;
  GetDirectoryEntries(path, &entries);
  TUniqueFiles idb_files;
  TUniqueFiles diff_files;
  for (const auto& entry : entries) {
    auto file_path(JoinPath(path, entry));
    if (IsDirectory(file_path)) {
      continue;
    }
    // Export all idbs in directory.
    const auto extension = absl::AsciiStrToUpper(GetFileExtension(file_path));
    if (extension == ".IDB" || extension == ".I64") {
      if (GetFileSize(file_path) > 0) {
        idb_files.insert(Basename(file_path));
      } else {
        PrintMessage(absl::StrCat("warning: skipping empty file ", file_path));
      }
    } else if (extension == ".BINEXPORT") {
      diff_files.insert(Basename(file_path));
    }
  }

  // TODO(soerenme): Remove all idbs that have already been exported from export
  //                 todo list.
  diff_files.insert(idb_files.begin(), idb_files.end());

  // Create todo list of file pairs.
  TFiles files;
  for (auto i = diff_files.cbegin(), end = diff_files.cend(); i != end; ++i) {
    for (auto j = diff_files.cbegin(); j != end; ++j) {
      if (i != j && (reference_file.empty() || reference_file == *i)) {
        files.emplace_back(*i, *j);
      }
    }
  }

  const size_t num_idbs = idb_files.size();
  const size_t num_diffs = files.size();
  auto config(XmlConfig::LoadFromFile(XmlConfig::GetDefaultFilename()));
  const int num_threads = config->ReadInt("/BinDiff/Threads/@use",
                                          std::thread::hardware_concurrency());
  const string ida_dir = config->ReadString("/BinDiff/Ida/@directory", "");
  const string ida_exe = config->ReadString("/BinDiff/Ida/@executable", "");
  const string ida_exe64 = config->ReadString("/BinDiff/Ida/@executable64", "");
  Timer<> timer;
  {  // Export
    if (!idb_files.empty()) {
      CreateIdaScript(out_path);
    }
    std::vector<std::thread> threads;
    for (int i = 0; i < num_threads; ++i) {
      threads.emplace_back(ExporterThread(path, out_path, ida_dir, ida_exe,
                                          ida_exe64, &idb_files));
    }
    for (auto& thread : threads) {
      thread.join();
    }
  }
  const auto export_time = timer.elapsed();
  timer.restart();

  if (!FLAGS_export) {  // Perform diff
    std::vector<std::thread> threads;
    for (int i = 0; i < num_threads; ++i) {
      threads.emplace_back(DifferThread(out_path, out_path, &files));
    }
    for (auto& thread : threads) {
      thread.join();
    }
  }
  const auto diff_time = timer.elapsed();
  DeleteIdaScript(out_path);

  PrintMessage(absl::StrCat(num_idbs, " files exported in ",
                            HumanReadableDuration(export_time), ", ",
                            num_diffs * (1 - FLAGS_export), " pairs diffed in ",
                            HumanReadableDuration(diff_time)));
}

void DumpMdIndices(const CallGraph& call_graph, const FlowGraphs& flow_graphs) {
  std::cout << "\n"
            << call_graph.GetFilename() << "\n"
            << call_graph.GetMdIndex();
  for (auto i = flow_graphs.cbegin(), end = flow_graphs.cend(); i != end; ++i) {
    std::cout << "\n"
              << std::hex << std::setfill('0') << std::setw(16)
              << (*i)->GetEntryPointAddress() << "\t" << std::fixed
              << std::setprecision(12) << (*i)->GetMdIndex() << "\t"
              << ((*i)->IsLibrary() ? "Library" : "Non-library");
  }
  std::cout << std::endl;
}

void BatchDumpMdIndices(const string& path) {
  std::vector<string> entries;
  GetDirectoryEntries(path, &entries);
  for (const auto& entry : entries) {
    auto file_path(JoinPath(path, entry));
    if (IsDirectory(file_path)) {
      continue;
    }
    auto extension = absl::AsciiStrToUpper(GetFileExtension(file_path));
    if (extension != ".CALL_GRAPH") {
      continue;
    }

    CallGraph call_graph;
    FlowGraphs flow_graphs;
    Instruction::Cache instruction_cache;
    ScopedCleanup cleanup(&flow_graphs, 0, &instruction_cache);
    FlowGraphInfos infos;
    Read(file_path, &call_graph, &flow_graphs, &infos, &instruction_cache);
    DumpMdIndices(call_graph, flow_graphs);
  }
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

int main(int argc, char** argv) {
#ifdef WIN32
  signal(SIGBREAK, SignalHandler);
#endif
  signal(SIGINT, SignalHandler);

  const string current_path(GetCurrentDirectory());
  SetCommandLineOptionWithMode("output_dir", current_path.c_str(),
                               SET_FLAGS_DEFAULT);

  int exit_code = 0;
  try {
    string binary_name(Basename(argv[0]));
    string usage(
        "Finds similarities in binary code.\n"
        "Usage:\n");
    usage +=
        "  " + binary_name +
        " --primary=PRIMARY [--secondary=SECONDARY]\n\n"
        "Example command line to diff all files in a directory against each"
        " other:\n" +
        "  " + binary_name +
        " \\\n"
        "    --primary=/tmp --output_dir=/tmp/result\n"
        "Note that if the directory contains IDA Pro databases these will \n"
        "automatically be exported first.\n"
        "For a single diff:\n" +
        "  " + binary_name +
        " \\\n"
        "    --primary=/tmp/file1.BinExport "
        "--secondary=/tmp/file2.BinExport \\\n"
        "    --output_dir=/tmp/result";
#ifdef GOOGLE
    InitGoogle(usage.c_str(), &argc, &argv, /* remove_flags = */ true);
#else
    SetUsageMessage(usage);
    ParseCommandLineFlags(&argc, &argv, /* remove_flags = */ true);

    SetLogHandler(&UnprefixedLogHandler);
#endif

    if (!FLAGS_nologo) {
      PrintMessage(
          absl::StrCat(kProgramVersion,
                       ", "
#ifdef _DEBUG
                       "debug build, "
#endif
                       "(c)2004-2011 zynamics GmbH, (c)2011-2018 Google LLC."));
    }

    const auto user_app_data =
        GetDirectory(PATH_APPDATA, "BinDiff", /* create = */ false) +
        "bindiff_core.xml";
    const auto common_app_data =
        GetDirectory(PATH_COMMONAPPDATA, "BinDiff", /* create = */ false) +
        "bindiff_core.xml";
    if (!FLAGS_config.empty()) {
      XmlConfig::SetDefaultFilename(FLAGS_config);
    } else if (FileExists(user_app_data)) {
      XmlConfig::SetDefaultFilename(user_app_data);
    } else if (FileExists(common_app_data)) {
      XmlConfig::SetDefaultFilename(common_app_data);
    }
    const XmlConfig& config(GetConfig());
    if (!config.GetDocument()) {
      throw std::runtime_error("config file invalid or not found");
    }
    // This initializes static variables before the threads get to them.
    if (GetDefaultMatchingSteps().empty() ||
        GetDefaultMatchingStepsBasicBlock().empty()) {
      throw std::runtime_error("config file invalid");
    }

    Timer<> timer;
    bool done_something = false;

    std::unique_ptr<CallGraph> call_graph1;
    std::unique_ptr<CallGraph> call_graph2;
    Instruction::Cache instruction_cache;
    FlowGraphs flow_graphs1;
    FlowGraphs flow_graphs2;
    ScopedCleanup cleanup(&flow_graphs1, &flow_graphs2, &instruction_cache);

    if (FLAGS_primary.empty()) {
      throw std::runtime_error("need primary input (--primary)");
    }

    if (FLAGS_output_dir == current_path /* Defaulted */ &&
        IsDirectory(FLAGS_primary.c_str())) {
      FLAGS_output_dir = FLAGS_primary;
    }

    if (!IsDirectory(FLAGS_output_dir.c_str())) {
      throw std::runtime_error(absl::StrCat(
          "output parameter (--output_dir) must be a writable directory: ",
          FLAGS_output_dir));
    }

    if (FileExists(FLAGS_primary.c_str())) {
      // Primary from file system.
      FlowGraphInfos infos;
      call_graph1.reset(new CallGraph());
      Read(FLAGS_primary, call_graph1.get(), &flow_graphs1, &infos,
           &instruction_cache);
    }

    if (IsDirectory(FLAGS_primary.c_str())) {
      // File system batch diff.
      if (FLAGS_ls) {
        ListFiles(FLAGS_primary);
      } else if (FLAGS_md_index) {
        BatchDumpMdIndices(FLAGS_primary);
      } else {
        BatchDiff(FLAGS_primary, FLAGS_secondary, FLAGS_output_dir);
      }
      done_something = true;
    }

    if (FLAGS_md_index && call_graph1 != nullptr) {
      DumpMdIndices(*call_graph1, flow_graphs1);
      done_something = true;
    }

    if (!FLAGS_secondary.empty() &&
        FileExists(FLAGS_secondary.c_str())) {
      // secondary from filesystem
      FlowGraphInfos infos;
      call_graph2.reset(new CallGraph());
      Read(FLAGS_secondary, call_graph2.get(), &flow_graphs2, &infos,
           &instruction_cache);
    }

    if (!done_something && ((!FileExists(FLAGS_primary.c_str()) &&
                             !IsDirectory(FLAGS_primary.c_str())) ||
                            (!FLAGS_secondary.empty() &&
                             (!FileExists(FLAGS_secondary.c_str()) &&
                              !IsDirectory(FLAGS_secondary.c_str()))))) {
      throw std::runtime_error(
          "invalid inputs, --primary and --secondary must point to valid "
          "files/directories.");
    }

    if (call_graph1.get() && call_graph2.get()) {
      const int edges1 = num_edges(call_graph1->GetGraph());
      const int vertices1 = num_vertices(call_graph1->GetGraph());
      const int edges2 = num_edges(call_graph2->GetGraph());
      const int vertices2 = num_vertices(call_graph2->GetGraph());
      PrintMessage(
          absl::StrCat("setup: ", HumanReadableDuration(timer.elapsed())));
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
          absl::StrCat("matching: ", HumanReadableDuration(timer.elapsed())));
      timer.restart();

      PrintMessage(absl::StrCat(
          "matched: ", fixed_points.size(), " of ", flow_graphs1.size(), "/",
          flow_graphs2.size(), " (primary/secondary, ",
          counts.find("functions primary (non-library)")->second, "/",
          counts.find("functions secondary (non-library)")->second,
          " non-library)"));

      PrintMessage(absl::StrCat("call graph MD index: primary   ",
                                call_graph1->GetMdIndex()));
      PrintMessage(absl::StrCat("                     secondary ",
                                call_graph2->GetMdIndex()));
      PrintMessage(absl::StrCat("similarity: ", similarity * 100,
                                "% (confidence: ", confidence * 100, "%)"));

      ChainWriter writer;
      if (FLAGS_log_format) {
        writer.Add(std::make_shared<ResultsLogWriter>(GetTruncatedFilename(
            FLAGS_output_dir + kPathSeparator, call_graph1->GetFilename(),
            "_vs_", call_graph2->GetFilename(), ".results")));
      }
      if (FLAGS_bin_format || writer.IsEmpty()) {
        writer.Add(std::make_shared<DatabaseWriter>(GetTruncatedFilename(
            FLAGS_output_dir + kPathSeparator, call_graph1->GetFilename(),
            "_vs_", call_graph2->GetFilename(), ".BinDiff")));
      }

      if (!writer.IsEmpty()) {
        writer.Write(*call_graph1, *call_graph2, flow_graphs1, flow_graphs2,
                     fixed_points);
        PrintMessage(absl::StrCat("writing results: ",
                                  HumanReadableDuration(timer.elapsed())));
      }
      timer.restart();
      done_something = true;
    }

    if (!done_something) {
      ShowUsageWithFlags(argv[0]);
    }
  } catch (const std::exception& error) {
    PrintErrorMessage(absl::StrCat("error: ", error.what()));
    exit_code = 1;
  } catch (...) {
    PrintErrorMessage("error: an unknown error occurred");
    exit_code = 2;
  }

  return exit_code;
}
