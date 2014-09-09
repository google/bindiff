// Command-line version of BinDiff.
//
// Define this (globally in project settings!!) to write a huge callstack
// logfile to outDir. This is useful for understanding algorithm performance
// and why fixedpoints have been assigned.
// #define REASONTREE

#include <cassert>
#include <fstream>
#include <functional>
#include <memory>
#include <iomanip>
#include <iostream>
#include <signal.h>
#include <sstream>
#include <string>
#include <utility>
#include <vector>

#ifdef GOOGLE
#include "base/commandlineflags.h"
#include "base/init_google.h"
#else
#define GFLAGS_DLL_DECL
#include <gflags/gflags.h>

using google::ParseCommandLineFlags;
using google::SET_FLAGS_DEFAULT;
using google::SetCommandLineOptionWithMode;
using google::SetUsageMessage;
using google::ShowUsageWithFlags;
#endif  // GOOGLE
#include "third_party/boost/do_not_include_from_google3_only_third_party/boost/boost/algorithm/string.hpp"
#include "third_party/boost/do_not_include_from_google3_only_third_party/boost/boost/date_time.hpp"
#include "third_party/boost/do_not_include_from_google3_only_third_party/boost/boost/filesystem/convenience.hpp"
#include "third_party/boost/do_not_include_from_google3_only_third_party/boost/boost/filesystem/operations.hpp"
#include "third_party/boost/do_not_include_from_google3_only_third_party/boost/boost/thread.hpp"
#include "third_party/boost/do_not_include_from_google3_only_third_party/boost/boost/timer.hpp"
#include "third_party/zynamics/bindetego/binexport.pb.h"
#include "third_party/zynamics/bindiff/binexport_header.h"
#include "third_party/zynamics/bindiff/callgraph.h"
#include "third_party/zynamics/bindiff/callgraphmatching.h"
#include "third_party/zynamics/bindiff/databasewriter.h"
#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/bindiff/flowgraph.h"
#include "third_party/zynamics/bindiff/flowgraphmatching.h"
#include "third_party/zynamics/bindiff/log.h"
#include "third_party/zynamics/bindiff/logwriter.h"
#include "third_party/zynamics/bindiff/matching.h"
#include "third_party/zynamics/bindiff/reasontree.h"
#include "third_party/zynamics/zylibcpp/utility/utility.h"
#include "third_party/zynamics/zylibcpp/utility/xmlconfig.h"

#undef min
#undef max

namespace fs = boost::filesystem;

DEFINE_string(primary, "" /* Default */,
              "Primary input file or path in batch mode");
DEFINE_string(secondary, "" /* Default */, "Secondary input file (optional)");
DEFINE_string(output_dir, "" /* Default */,
              "Output path, defaults to current directory");
DEFINE_bool(log_format, false /* Default */,
            "Write results in log file format");
DEFINE_bool(knox_format, false /* Default */,
            "Write results in FortKnox format");
DEFINE_bool(bin_format, false /* Default */,
            "Write results in binary file format that can be loaded by the "
            "BinDiff IDA plugin or the GUI");
DEFINE_bool(md_index, false /* Default */,
            "Dump MD indices (will not diff anything)");
DEFINE_bool(export, false /* Default */,
            "Batch export .idb files from input directory to BinExport format");
DEFINE_bool(ls, false /* Default */,
            "List hash/filenames for all .BinExport files in input directory");
DEFINE_string(config, "" /* Default */, "Specify config file name");

static const char kBinExportVersion[] = "7";  // Exporter version to use.

boost::mutex g_queue_mutex;
volatile bool g_wants_to_quit = false;

typedef std::list<std::pair<std::string, std::string>> TFiles;
typedef std::set<std::string> TUniqueFiles;

// This function will try and create a fully specified filename no longer than
// 250 characters. It'll truncate part1 and part2, leaving all other fragments
// as is. If it is not possible to get a short enough name it'll throw an
// exception.
std::string GetTruncatedFilename(
    const std::string& path /* Must include trailing slash */,
    const std::string& part1 /* Potentially truncated */,
    const std::string& middle,
    const std::string& part2 /* Potentially truncated */,
    const std::string& extension) {
  enum { kMaxFilename = 250 };

  const std::string::size_type length = path.size() + part1.size() +
                                        middle.size() + part2.size() +
                                        extension.size();
  if (length <= kMaxFilename) {
    return path + part1 + middle + part2 + extension;
  }

  std::string::size_type overflow = length - kMaxFilename;

  // First, shorten the longer of the two strings.
  std::string one(part1);
  std::string two(part2);
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
        ("Cannot create a valid filename, please choose shorter input names "
         "or directories! '" +
         path + part1 + middle + part2 + extension + "'").c_str());
  }
  return path + part1.substr(0, one.size() - overflow / 2) + middle +
         part2.substr(0, two.size() - overflow / 2) + extension;
}

class DifferThread {
 public:
  explicit DifferThread(const std::string& path, const std::string& out_path,
                        TFiles* files);
  void operator()();

 private:
  TFiles* file_queue_;
  std::string path_;
  std::string out_path_;
};

DifferThread::DifferThread(const std::string& path, const std::string& out_path,
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
  std::string last_file1;
  std::string last_file2;
  ScopedCleanup cleanup(&flow_graphs1, &flow_graphs2, &instruction_cache);
  for (;;) {
    std::string file1;
    std::string file2;
    try {
      boost::timer timer;
      {
        // Pop pair from todo queue.
        boost::mutex::scoped_lock lock(g_queue_mutex);
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
        LOG(INFO) << "reading " << file1;
        DeleteFlowGraphs(&flow_graphs1);
        FlowGraphInfos infos;
        Read(path_ + "/" + file1 + ".BinExport", call_graph1, flow_graphs1,
             infos, &instruction_cache);
      } else {
        ResetMatches(&flow_graphs1);
      }

      if (last_file2 != file2) {
        LOG(INFO) << "reading " << file2;
        DeleteFlowGraphs(&flow_graphs2);
        FlowGraphInfos infos;
        Read(path_ + "/" + file2 + ".BinExport", call_graph2, flow_graphs2,
             infos, &instruction_cache);
      } else {
        ResetMatches(&flow_graphs2);
      }

      LOG(INFO) << "diffing " << file1 << " vs " << file2;

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

      LOG(INFO) << "writing results";
      {
        ChainWriter writer;
        if (FLAGS_log_format) {
          writer.Add(std::make_shared<ResultsLogWriter>(GetTruncatedFilename(
              out_path_ + "/", call_graph1.GetFilename(), "_vs_",
              call_graph2.GetFilename(), ".results")));
        }
        if (FLAGS_bin_format || writer.IsEmpty()) {
          writer.Add(std::make_shared<DatabaseWriter>(GetTruncatedFilename(
              out_path_ + "/", call_graph1.GetFilename(), "_vs_",
              call_graph2.GetFilename(), ".BinDiff")));
        }

        writer.Write(call_graph1, call_graph2, flow_graphs1, flow_graphs2,
                     fixed_points);

        LOG(INFO) << file1 << " vs " << file2 << " ( " << std::fixed
                  << std::setprecision(3) << timer.elapsed() << " sec ) :"
                  << "\tsimilarity:\t" << std::fixed << std::setprecision(6)
                  << similarity << "\tconfidence:\t" << std::fixed
                  << std::setprecision(6) << confidence;
        for (Counts::const_iterator i = counts.begin(), end = counts.end();
             i != end; ++i) {
          LOG(INFO) << "\n\t" << i->first << ":\t" << i->second;
        }
      }

      last_file1 = file1;
      last_file2 = file2;

      if (g_wants_to_quit) {
        break;
      }
    } catch (const std::bad_alloc&) {
      LOG(INFO) << file1 << " vs " << file2;
#ifdef _WIN32
      LOG(INFO)
          << "Out-of-memory. Please try again with more memory available. "
             "Some extremely large binaries\nmay require a 64-bit version of "
             "BinDiff - please contact zynamics to request one.";
#else
      LOG(INFO)
          << "Out-of-memory. Please try again with more memory available.";
#endif

      last_file1.clear();
      last_file2.clear();
    } catch (const std::exception& error) {
      LOG(INFO) << file1 << " vs " << file2 << " : " << error.what();

      last_file1.clear();
      last_file2.clear();
    }
  }
}

class ExporterThread {
 public:
  explicit ExporterThread(const std::string& in_path,
                          const std::string& out_path,
                          const std::string& ida_dir,
                          const std::string& ida_exe,
                          const std::string& ida_exe64, TUniqueFiles* files)
      : files_(files),
        in_path_(in_path),
        out_path_(out_path),
        ida_dir_(ida_dir),
        ida_exe_(ida_exe),
        ida_exe64_(ida_exe64) {}

  void operator()();

 private:
  TUniqueFiles* files_;
  std::string in_path_;
  std::string out_path_;
  std::string ida_dir_;
  std::string ida_exe_;
  std::string ida_exe64_;
};

void ExporterThread::operator()() {
  for (;;) {
    boost::timer timer;
    std::string file;
    {
      boost::mutex::scoped_lock lock(g_queue_mutex);
      if (files_->empty()) {
        return;
      }
      file = *files_->begin();
      files_->erase(files_->begin());
    }

    const fs::path in_path(in_path_);
    const fs::path out_path(out_path_);

    // @bug: what if we have the same base name but as .idb _and_ .i64?
    bool ida64 = false;
    fs::path inFile(in_path / (file + ".idb"));
    if (!fs::exists(inFile)) {
      inFile = (in_path / (file + ".i64"));
      if (!fs::exists(inFile)) {
        LOG(INFO) << "\"" << inFile << "\" not found";
        continue;
      }
      ida64 = true;
    }

    // @bug: If outpath is a relative path like "." IDA won't work. We need to
    // fully expand it first.
    std::string status_message;
    std::vector<std::string> args;
    args.push_back(ida_dir_ + "/" + (!ida64 ? ida_exe_ : ida_exe64_));
    args.push_back("-A");
    args.push_back("-OExporterModule:" + out_path.string());
#ifdef UNIX_COMPILE
    args.push_back("-S" + (out_path / "run_ida.idc").string());
#else
    args.push_back("-S\"" + (out_path / "run_ida.idc").string() + "\"");
#endif
    args.push_back(inFile.string());
    if (!SpawnProcess(args, true /* Wait */, &status_message)) {
      LOG(INFO) << "failed to spawn IDA export process: "
                << GetLastWindowsError();
      LOG(INFO) << status_message;
      return;
    }

    LOG(INFO) << std::fixed << std::setprecision(2) << timer.elapsed() << "\t"
              << fs::file_size(inFile) << "\t" << file;

    if (g_wants_to_quit) {
      return;
    }
  }
}

void CreateIdaScript(const std::string& out_path) {
  fs::path path(out_path);
  std::ofstream file((path / "run_ida.idc").c_str());
  if (!file) {
    throw std::runtime_error(
        ("Could not create idc script at \"" + out_path + "\"").c_str());
  }
  file << "#include <idc.idc>\n"
       << "static main()\n"
       << "{\n"
       << "\tBatch(0);\n"
       << "\tWait();\n"
       << "\tExit( 1 - RunPlugin(\"zynamics_binexport_" << kBinExportVersion
       << "\", 2 ));\n"
       << "}\n";
}

void DeleteIdaScript(const std::string& out_path) {
  fs::path path(out_path);
  fs::remove(path / "run_ida.idc");
}

void ListFiles(const std::string& path) {
  TUniqueFiles files;
  fs::path in_path(path.c_str());
  for (fs::directory_iterator i(in_path), end = fs::directory_iterator();
       i != end; ++i) {
    try {
      if (boost::algorithm::to_lower_copy(fs::extension(*i)) == ".binexport") {
        std::ifstream file(i->path().c_str(), std::ios_base::binary);
        BinExportHeader header(&file);
        BinExport::Meta metaInformation;
        std::string buffer(header.call_graph_offset - header.meta_offset, '\0');
        file.read(&buffer[0], buffer.size());
        metaInformation.ParseFromString(buffer);
        LOG(INFO) << EncodeHex(metaInformation.input_hash()) << " ("
                  << metaInformation.input_binary() << ")";
      }
    } catch (const std::runtime_error& error) {
      LOG(INFO) << error.what() << " " << i->path();
    }
  }
}

void BatchDiff(const std::string& path, const std::string& reference_file,
               const std::string& out_path) {
  // Collect idb files to diff.
  TUniqueFiles idb_files;
  TUniqueFiles diff_files;
  fs::path in_path(path.c_str());
  for (fs::directory_iterator i(in_path), end = fs::directory_iterator();
       i != end; ++i) {
    // Export all idbs in directory.
    std::string extension(boost::algorithm::to_lower_copy(fs::extension(*i)));
    if (extension == ".idb" || extension == ".i64") {
      if (fs::file_size(*i)) {
        idb_files.insert(fs::basename(*i));
      } else {
        LOG(INFO) << "Warning: skipping empty file " << *i;
      }
    } else if (boost::algorithm::to_lower_copy(fs::extension(*i)) ==
               ".binexport") {
      diff_files.insert(fs::basename(*i));
    }
  }

  // TODO(soerenme): Remove all idbs that have already been exported from export
  // todo list.
  diff_files.insert(idb_files.begin(), idb_files.end());

  // Create todo list of file pairs.
  TFiles files;
  for (auto i = diff_files.cbegin(), end = diff_files.cend(); i != end; ++i) {
    for (auto j = diff_files.cbegin(); j != end; ++j) {
      if (i != j) {
        if (reference_file.empty() || reference_file == *i) {
          files.push_back(std::make_pair(*i, *j));
        }
      }
    }
  }

  const size_t num_idbs = idb_files.size();
  const size_t num_diffs = files.size();
  const unsigned num_hardware_threads = boost::thread::hardware_concurrency();
  XmlConfig config(XmlConfig::GetDefaultFilename(), "BinDiffDeluxe");
  const unsigned num_threads =
      config.ReadInt("/BinDiffDeluxe/Threads/@use", num_hardware_threads);
  const std::string ida_dir =
      config.ReadString("/BinDiffDeluxe/Ida/@directory", "");
  const std::string ida_exe =
      config.ReadString("/BinDiffDeluxe/Ida/@executable", "");
  const std::string ida_exe64 =
      config.ReadString("/BinDiffDeluxe/Ida/@executable64", "");
  boost::timer timer;
  {  // Export
    if (!idb_files.empty()) {
      CreateIdaScript(out_path);
    }
    boost::thread_group threads;
    for (unsigned i = 0; i < num_threads; ++i) {
      threads.create_thread(ExporterThread(in_path.string(), out_path, ida_dir,
                                           ida_exe, ida_exe64, &idb_files));
    }
    threads.join_all();
  }
  const double exportTime = timer.elapsed();

  timer.restart();
  if (!FLAGS_export) {  // perform diff
    boost::thread_group threads;
    for (unsigned i = 0; i < num_threads; ++i) {
      threads.create_thread(DifferThread(out_path, out_path, &files));
    }
    threads.join_all();
  }
  const double diffTime = timer.elapsed();
  DeleteIdaScript(out_path);

  LOG(INFO) << num_idbs << " files exported in " << std::fixed
            << std::setprecision(2) << exportTime << " seconds, "
            << (num_diffs * (1 - FLAGS_export)) << " pairs diffed in "
            << std::fixed << std::setprecision(2) << diffTime << " seconds";
}

void DumpMdIndices(const CallGraph& call_graph, const FlowGraphs& flow_graphs) {
  std::cout << "\n" << call_graph.GetFilename() << "\n"
            << call_graph.GetMdIndex();
  for (auto i = flow_graphs.cbegin(), end = flow_graphs.cend(); i != end; ++i) {
    std::cout << "\n" << std::hex << std::setfill('0') << std::setw(16)
              << (*i)->GetEntryPointAddress() << "\t"
              << std::fixed << std::setprecision(12) << (*i)->GetMdIndex()
              << "\t" << ((*i)->IsLibrary() ? "Library" : "Non-library");
  }
  std::cout << std::endl;
}

void BatchDumpMdIndices(const std::string& path) {
  fs::path in_path(path.c_str());
  for (fs::directory_iterator i(in_path), end = fs::directory_iterator();
       i != end; ++i) {
    if (fs::extension(*i) != ".call_graph") {
      continue;
    }

    CallGraph call_graph;
    FlowGraphs flow_graphs;
    Instruction::Cache instruction_cache;
    ScopedCleanup cleanup(&flow_graphs, 0, &instruction_cache);
    FlowGraphInfos infos;
    Read(i->path().string(), call_graph, flow_graphs, infos,
         &instruction_cache);
    DumpMdIndices(call_graph, flow_graphs);
  }
}

void SignalHandler(int code) {
  static int signal_count = 0;
  switch (code) {
#ifndef UNIX_COMPILE
    case SIGBREAK:  // Ctrl-Break, not available on Unix
#endif
    case SIGINT:  // Ctrl-C
      if (++signal_count < 3) {
        LOG(INFO)
            << "Gracefully shutting down after current operations finish.";
        g_wants_to_quit = true;
      } else {
        LOG(INFO) << "Forcefully terminating process.";
        exit(1);
      }
      break;
  }
}

int main(int argc, char** argv) {
#ifndef UNIX_COMPILE
  signal(SIGBREAK, SignalHandler);
#endif
  signal(SIGINT, SignalHandler);

  const std::string current_path(fs::current_path().string());
  SetCommandLineOptionWithMode("output_dir", current_path.c_str(),
                               SET_FLAGS_DEFAULT);
  SetCommandLineOptionWithMode("alsologtostderr", "true", SET_FLAGS_DEFAULT);

  int exit_code = 0;
  try {
    std::string usage(
        "Finds similarities in binary code.\n"
        "Usage:\n");
    usage +=
        "  " + std::string(argv[0]) +
        " --primary=PRIMARY [--secondary=SECONDARY]\n\n"
        "Example command line to diff all files in a directory against each"
        " other:\n" +
        "  " + std::string(argv[0]) +
        " \\\n"
        "    --primary=/tmp --output_dirc=/tmp/result\n"
        "Note that if the directory contains IDA Pro databases these will \n"
        "automatically be exported first.\n"
        "For a single diff:\n" +
        "  " + std::string(argv[0]) +
        " \\\n"
        "    --primary=/tmp/file1.BinExport "
        "--secondary=/tmp/file2.BinExport \\\n"
        "    --output_dir=/tmp/result";
#ifdef GOOGLE
    InitGoogle(usage.c_str(), &argc, &argv, true /* Remove flags */);
#else
    SetUsageMessage(usage);
    ParseCommandLineFlags(&argc, &argv, true /* Remove flags */);
#endif

    LOG(INFO) << kProgramVersion << " (" << __DATE__
#ifdef _DEBUG
              << ", debug build"
#endif
              << ") - (c)2004-2014 Google Inc.";

    const auto user_app_data =
        GetDirectory(PATH_APPDATA, "BinDiff", false) + "bindiff.xml";
    const auto common_app_data =
        GetDirectory(PATH_COMMONAPPDATA, "BinDiff", false) + "bindiff.xml";
    if (!FLAGS_config.empty()) {
      XmlConfig::SetDefaultFilename(FLAGS_config);
    } else if (boost::filesystem::exists(user_app_data)) {
      XmlConfig::SetDefaultFilename(user_app_data);
    } else if (boost::filesystem::exists(common_app_data)) {
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

    // echo original command line to log file
    std::string commandline;
    for (int i = 0; i < argc; ++i) {
      commandline += *(argv + i) + std::string(" ");
    }
    LOG(INFO) << commandline;

    boost::timer timer;
    bool done_something = false;

    std::unique_ptr<CallGraph> call_graph1;
    std::unique_ptr<CallGraph> call_graph2;
    Instruction::Cache instruction_cache;
    FlowGraphs flow_graphs1, flow_graphs2;
    ScopedCleanup cleanup(&flow_graphs1, &flow_graphs2, &instruction_cache);

    if (FLAGS_primary.empty()) {
      throw std::runtime_error("Need primary input (--primary)");
    }

    if (FLAGS_output_dir == current_path /* Defaulted */ &&
        fs::is_directory(FLAGS_primary.c_str())) {
      FLAGS_output_dir = FLAGS_primary;
    }

    if (!fs::is_directory(FLAGS_output_dir.c_str())) {
      throw std::runtime_error(
          "Output parameter (--output_dir) must be a writeable directory! "
          "Supplied value: \"" +
          FLAGS_output_dir + "\"");
    }

#ifdef REASONTREE
    std::ofstream reasoningFile(
        (FLAGS_output_dir + "/differDeluxe.reason").c_str());
    CReasoningTree::getInstance().setTarget(&reasoningFile);
#endif

    if (fs::is_regular_file(FLAGS_primary.c_str())) {
      // Primary from file system.
      FlowGraphInfos infos;
      call_graph1.reset(new CallGraph());
      Read(FLAGS_primary, *call_graph1, flow_graphs1, infos,
           &instruction_cache);
    }

    if (fs::is_directory(FLAGS_primary.c_str())) {
      // File system batch diff.
      if (FLAGS_ls) {
        ListFiles(FLAGS_primary);
      } else if (!FLAGS_md_index) {
        BatchDiff(FLAGS_primary, FLAGS_secondary, FLAGS_output_dir);
      } else {
        BatchDumpMdIndices(FLAGS_primary);
      }
      done_something = true;
    }

    if (FLAGS_md_index && call_graph1 != nullptr) {
      DumpMdIndices(*call_graph1, flow_graphs1);
      done_something = true;
    }

    if (!FLAGS_secondary.empty() &&
        fs::is_regular_file(FLAGS_secondary.c_str())) {
      // secondary from filesystem
      FlowGraphInfos infos;
      call_graph2.reset(new CallGraph());
      Read(FLAGS_secondary, *call_graph2, flow_graphs2, infos,
           &instruction_cache);
    }

    if (!done_something && ((!fs::is_regular_file(FLAGS_primary.c_str()) &&
                             !fs::is_directory(FLAGS_primary.c_str())) ||
                            (!FLAGS_secondary.empty() &&
                             (!fs::is_regular_file(FLAGS_secondary.c_str()) &&
                              !fs::is_directory(FLAGS_secondary.c_str()))))) {
      throw std::runtime_error(
          "Invalid inputs. Please make sure --primary and --secondary "
          "point to valid files/directories.");
    }

    if (call_graph1.get() && call_graph2.get()) {
      const int edges1 = num_edges(call_graph1->GetGraph());
      const int vertices1 = num_vertices(call_graph1->GetGraph());
      const int edges2 = num_edges(call_graph2->GetGraph());
      const int vertices2 = num_vertices(call_graph2->GetGraph());
      LOG(INFO) << "setup: " << timer.elapsed() << " sec. "
                << call_graph1->GetFilename() << " has " << vertices1
                << " functions and " << edges1 << " calls. "
                << call_graph2->GetFilename() << " has " << vertices2
                << " functions and " << edges2 << " calls.";
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

      LOG(INFO) << "matching: " << timer.elapsed() << " sec.";
      timer.restart();

      LOG(INFO) << "matched " << fixed_points.size() << " of "
                << flow_graphs1.size() << "/" << flow_graphs2.size() << " ("
                << counts.find("functions primary (non-library)")->second << "/"
                << counts.find("functions secondary (non-library)")->second
                << ")";
      LOG(INFO) << "call_graph1 MD index " << std::dec << std::setprecision(16)
                << call_graph1->GetMdIndex() << "\tcall_graph2 MD index "
                << std::dec << std::setprecision(16)
                << call_graph2->GetMdIndex();
      LOG(INFO) << "similarity: " << std::setfill(' ') << std::setw(5)
                << std::setprecision(4) << (similarity * 100.0) << "%"
                << "\tconfidence: " << std::setfill(' ') << std::setw(5)
                << std::setprecision(4) << (confidence * 100.0) << "%";

      ChainWriter writer;
      if (FLAGS_log_format) {
        writer.Add(std::make_shared<ResultsLogWriter>(GetTruncatedFilename(
            FLAGS_output_dir + "/", call_graph1->GetFilename(), "_vs_",
            call_graph2->GetFilename(), ".results")));
      }
      if (FLAGS_bin_format || writer.IsEmpty()) {
        writer.Add(std::make_shared<DatabaseWriter>(GetTruncatedFilename(
            FLAGS_output_dir + "/", call_graph1->GetFilename(), "_vs_",
            call_graph2->GetFilename(), ".BinDiff")));
      }

      if (!writer.IsEmpty()) {
        writer.Write(*call_graph1, *call_graph2, flow_graphs1, flow_graphs2,
                     fixed_points);
        LOG(INFO) << "writing results: " << std::setprecision(3)
                  << timer.elapsed() << " sec." << std::endl;
      }
      timer.restart();
      done_something = true;
    }

    if (!done_something) {
      ShowUsageWithFlags(argv[0]);
    }
  } catch (const std::bad_alloc&) {
    LOG(INFO)
        << "Out-of-memory. Please try again with more memory available. Some "
           "extremely large binaries\nmay require a 64bit version of BinDiff "
           "- please contact zynamics to request one.";
    exit_code = 3;
  } catch (const std::exception& error) {
    LOG(INFO) << "an error occurred: " << error.what();
    exit_code = 1;
  } catch (...) {
    LOG(INFO) << "an unknown error occurred";
    exit_code = 2;
  }

  // Need to explicitly shutdown logging, otherwise the logging thread will stay
  // active.
  ShutdownLogging();
  return exit_code;
}
