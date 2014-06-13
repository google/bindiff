// Command-line version of BinDiff.
//
// Define this (globally in project settings!!) to write a huge callstack
// logfile to outDir. This is useful for understanding algorithm performance
// and why fixedpoints have been assigned.
// #define REASONTREE

#include <cassert>
#include <fstream>
#include <functional>
#include <iomanip>
#include <iostream>
#include <signal.h>
#include <sstream>
#include <string>
#include <utility>
#include <vector>

#include "third_party/boost/do_not_include_from_google3_only_third_party/boost/boost/algorithm/string.hpp"
#include "third_party/boost/do_not_include_from_google3_only_third_party/boost/boost/assign/list_of.hpp"
#include "third_party/boost/do_not_include_from_google3_only_third_party/boost/boost/date_time.hpp"
#include "third_party/boost/do_not_include_from_google3_only_third_party/boost/boost/filesystem/convenience.hpp"
#include "third_party/boost/do_not_include_from_google3_only_third_party/boost/boost/filesystem/operations.hpp"
#include "third_party/boost/do_not_include_from_google3_only_third_party/boost/boost/lexical_cast.hpp"
#include "third_party/boost/do_not_include_from_google3_only_third_party/boost/boost/program_options.hpp"
#include "third_party/boost/do_not_include_from_google3_only_third_party/boost/boost/scoped_array.hpp"
#include "third_party/boost/do_not_include_from_google3_only_third_party/boost/boost/scoped_ptr.hpp"
#include "third_party/boost/do_not_include_from_google3_only_third_party/boost/boost/thread.hpp"
#include "third_party/boost/do_not_include_from_google3_only_third_party/boost/boost/timer.hpp"
#include "third_party/zynamics/bindetego/binexport.pb.h"
#include "third_party/zynamics/bindiff/binexport_header.h"
#include "third_party/zynamics/bindiff/callgraph.h"
#include "third_party/zynamics/bindiff/callgraphmatching.h"
#include "third_party/zynamics/bindiff/databasewriter.h"
#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/bindiff/exporter_thread.h"
#include "third_party/zynamics/bindiff/flowgraph.h"
#include "third_party/zynamics/bindiff/flowgraphmatching.h"
#include "third_party/zynamics/bindiff/log.h"
#include "third_party/zynamics/bindiff/logwriter.h"
#include "third_party/zynamics/bindiff/matching.h"
#include "third_party/zynamics/bindiff/reasontree.h"
#include "third_party/zynamics/zylibcpp/utility/utility.h"
#include "third_party/zynamics/zylibcpp/utility/xmlconfig.h"

#include <google/protobuf/io/zero_copy_stream_impl.h>
#undef min
#undef max

static const char kBinExportVersion[] = "7";  // Exporter version to use.

boost::mutex g_QueueMutex;
volatile bool g_WantsToQuit = false;

typedef std::list<std::pair<std::string, std::string> > TFiles;
typedef std::set<std::string, std::less<std::string> > TUniqueFiles;

// This function will try and create a fully specified filename no longer than
// 250 characters. It'll truncate part1 and part2, leaving all other fragments
// as is. If it is not possible to get a short enough name it'll throw an
// exception.
std::string getTruncatedFilename(
    const std::string& path,           // must include trailing slash
    const std::string& part1,          // potentially truncated
    const std::string& middle,
    const std::string& part2,          // potentially truncated
    const std::string& extension) {
    enum { MAX_LENGTH = 250 };

    const std::string::size_type length = path.size()
         + part1.size() + middle.size() + part2.size() + extension.size();
    if (length <= MAX_LENGTH)
      return path + part1 + middle + part2 + extension;

    std::string::size_type overflow = length - MAX_LENGTH;

    // first, shorten the longer of the two strings
    std::string one(part1);
    std::string two(part2);
    if (part1.size() > part2.size()) {
      one = part1.substr(0, std::max(part2.size(),
          part1.size() > overflow ? part1.size() - overflow : 0));
      overflow -= part1.size() - one.size();
    } else if (part2.size() > part1.size()) {
      two = part2.substr(0, std::max(part1.size(),
        part2.size() > overflow ? part2.size() - overflow : 0));
      overflow -= part2.size() - two.size();
    }
    if (!overflow)
      return path + one + middle + two + extension;

    // second, if that still wasn't enough, shorten both strings equally
    assert(one.size() == two.size());
    if (overflow / 2 >= one.size()) {
      throw std::runtime_error((
          "Cannot create a valid filename, please choose shorter input names "
          "or directories! '" + path + part1 + middle + part2 + extension +
          "'").c_str());
    }
    return path
        + part1.substr(0, one.size() - overflow / 2)
        + middle
        + part2.substr(0, two.size() - overflow / 2)
        + extension;
}

std::string readBinaryFile(const boost::filesystem::path& path) {
  const uintmax_t fileSize = boost::filesystem::file_size(path);
  if (!fileSize) {
    return std::string();
  }

  std::ifstream file(path.c_str(), std::ios_base::binary);
  std::string buffer;
  buffer.resize(static_cast<unsigned int>(fileSize));
  file.read(&*buffer.begin(), static_cast<std::streamsize>(fileSize));
  return buffer;
}

class CDifferThread {
 public:
  explicit CDifferThread(const std::string& path, const std::string& outPath,
                         const boost::program_options::variables_map& options,
                         TFiles* files);
  void operator()();

 private:
  TFiles*       m_FileQueue;
  std::string   m_Path;
  std::string   m_OutPath;
  boost::program_options::variables_map m_Options;
};

CDifferThread::CDifferThread(
    const std::string& path, const std::string& outPath,
    const boost::program_options::variables_map& options, TFiles* files)
  : m_FileQueue(files)
  , m_Path(path)
  , m_OutPath(outPath)
  , m_Options(options) {
}

void CDifferThread::operator()() {
  const MatchingSteps
    default_callgraph_steps(GetDefaultMatchingSteps());
  const MatchingStepsFlowGraph
    default_basicblock_steps(GetDefaultMatchingStepsBasicBlock());

  Instruction::Cache instructionCache;
  FlowGraphs flowGraphs1, flowGraphs2;
  CallGraph callGraph1, callGraph2;
  std::string lastFile1, lastFile2;
  ScopedCleanup cleanup(&flowGraphs1, &flowGraphs2, &instructionCache);
  for (;;) {
    std::string file1, file2;
    try {
      boost::timer timer;
      { // pop pair from todo queue
        boost::mutex::scoped_lock lock(g_QueueMutex);
        if (m_FileQueue->empty())
          break;
        file1 = m_FileQueue->front().first;
        file2 = m_FileQueue->front().second;
        m_FileQueue->pop_front();
      }

      // we need to keep the cache around if one file stays the same
      if (lastFile1 != file1 && lastFile2 != file2) {
        instructionCache.Clear();
      }

      // perform setup and diff
      // TODO(soerenme) consider inverted pairs as well, i.e. file1 == lastFile2
      if (lastFile1 != file1) {
        LOG(INFO) << "reading " << file1;
        DeleteFlowGraphs(&flowGraphs1);
        FlowGraphInfos infos;
        Read(m_Path + "/" + file1 + ".BinExport", callGraph1, flowGraphs1,
            infos, &instructionCache);
      } else {
        ResetMatches(&flowGraphs1);
      }

      if (lastFile2 != file2) {
        LOG(INFO) << "reading " << file2;
        DeleteFlowGraphs(&flowGraphs2);
        FlowGraphInfos infos;
        Read(m_Path + "/" + file2 + ".BinExport", callGraph2, flowGraphs2,
            infos, &instructionCache);
      } else {
        ResetMatches(&flowGraphs2);
      }

      LOG(INFO) << "diffing " << file1 << " vs " << file2;

      FixedPoints fixedPoints;
      MatchingContext context(callGraph1, callGraph2,
                               flowGraphs1, flowGraphs2, fixedPoints);
      Diff(&context, default_callgraph_steps, default_basicblock_steps);

      Histogram histogram;
      Counts counts;
      GetCountsAndHistogram(flowGraphs1, flowGraphs2,
                            fixedPoints, &histogram, &counts);
      const double similarity = GetSimilarityScore(
          callGraph1, callGraph2, histogram, counts);
      Confidences confidences;
      const double confidence = GetConfidence(histogram, &confidences);

      LOG(INFO) << "writing results";
      {
        ChainWriter writer;
        if (m_Options.count("log")) {
          writer.Add(boost::shared_ptr<Writer>(new ResultsLogWriter(
              getTruncatedFilename(
                  m_OutPath + "/",
                  callGraph1.GetFilename(),
                  "_vs_",
                  callGraph2.GetFilename(),
                  ".results"))));
        }
        if (m_Options.count("bin") || writer.IsEmpty()) {
          writer.Add(boost::shared_ptr<Writer>(new DatabaseWriter(
              getTruncatedFilename(
                  m_OutPath + "/",
                  callGraph1.GetFilename(),
                  "_vs_",
                  callGraph2.GetFilename(),
                  ".BinDiff"))));
        }

        writer.Write(callGraph1, callGraph2,
                     flowGraphs1, flowGraphs2, fixedPoints);

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

      lastFile1 = file1;
      lastFile2 = file2;

      if (g_WantsToQuit)
        break;
    } catch(const std::bad_alloc&) {
      LOG(INFO) << file1 << " vs " << file2;
#ifdef _WIN32
      LOG(INFO) << "Out-of-memory. Please try again with more memory available. "
          "Some extremely large binaries\nmay require a 64-bit version of "
          "BinDiff - please contact zynamics to request one.";
#else
      LOG(INFO) << "Out-of-memory. Please try again with more memory available.";
#endif

      lastFile1 = lastFile2 = "";
    } catch(const std::exception& error) {
      LOG(INFO) << file1 << " vs " << file2 << " : " << error.what();

      lastFile1 = lastFile2 = "";
    }
  }
}

class CExporterThread {
 public:
  explicit CExporterThread(const std::string& inPath,
                           const std::string& outPath,
                           const std::string& idaDir,
                           const std::string& idaExe,
                           const std::string& idaExe64,
                           TUniqueFiles* files);
  void operator()();

 private:
  TUniqueFiles* m_Files;
  std::string   m_InPath;
  std::string   m_OutPath;
  std::string   m_IdaDir;
  std::string   m_IdaExe;
  std::string   m_IdaExe64;
};

CExporterThread::CExporterThread(const std::string& inPath,
                                 const std::string& outPath,
                                 const std::string& idaDir,
                                 const std::string& idaExe,
                                 const std::string& idaExe64,
                                 TUniqueFiles* files)
  : m_Files(files)
  , m_InPath(inPath)
  , m_OutPath(outPath)
  , m_IdaDir(idaDir)
  , m_IdaExe(idaExe)
  , m_IdaExe64(idaExe64) {
}

void CExporterThread::operator()() {
  for (;;) {
    boost::timer timer;
    std::string file;
    {
      boost::mutex::scoped_lock lock(g_QueueMutex);
      if (m_Files->empty())
        return;
      file = *m_Files->begin();
      m_Files->erase(m_Files->begin());
    }

    const boost::filesystem::path inPath(m_InPath);
    const boost::filesystem::path outPath(m_OutPath);

    // Needed for Linux, but doesn't hurt on Windows as well
    EnvironmentVariables env;
    env["TVHEADLESS"] = "1";

    // @bug: what if we have the same base name but as .idb _and_ .i64?
    bool ida64 = false;
    boost::filesystem::path inFile(inPath / (file + ".idb"));
    if (!boost::filesystem::exists(inFile)) {
      inFile = (inPath / (file + ".i64"));
      if (!boost::filesystem::exists(inFile)) {
        LOG(INFO) << "\"" << inFile << "\" not found";
        continue;
      }
      ida64 = true;
    }

    // Call IDA with custom environment and close its standard file descriptors
    // @bug: if outpath is a relative path like "." IDA won't work. We need to
    //       fully expand it first
    std::string status_message;
    if (!SpawnProcess(boost::assign::list_of
        (m_IdaDir + "/" + (!ida64 ? m_IdaExe : m_IdaExe64))
        ("-A")
        ("-OExporterModule:"+ outPath.string())
        ("-S" + (outPath / "runIda.idc").string())
        (inFile.string()), env, true, true, &status_message)) {
      LOG(INFO) << "failed to spawn IDA export process: " 
               << GetLastWindowsError();
      LOG(INFO) << status_message;
      return;
    }

    LOG(INFO) << std::fixed << std::setprecision(2) << timer.elapsed() << "\t"
        << boost::filesystem::file_size(inFile) << "\t" << file;

    if (g_WantsToQuit)
      return;
    /*		{
     // CHECK: Is this more user-friendly? I guess if we cannot start
     //        a new IDA process while batch-diffing, all subsequent
     //        attempts to do so will fail as well.
     std::cLOG(INFO) << "Error: Could not start IDA with `'" <<
     curIdb << "\".";
     break; //

     // Maybe this is the way to go? -Would be a more suitable message
     // for batch-diffing, since the user may want to just try to diff
     // as many files as possible and ignore in-between failures.
     std::cLOG(INFO) << "Warning: Could not start IDA with `'" <<
     curIdb << "\"."; //
     }*/
  }
}

void createIdaScript(const std::string& outPath) {
  boost::filesystem::path path(outPath);
  std::ofstream file((path / "runIda.idc").c_str());
  if (!file) {
    throw std::runtime_error(
        ("Could not create idc script at \"" + outPath + "\"").c_str());
  }
  file
      << "#include <idc.idc>\n" << "static main()\n" << "{\n"
      << "\tBatch(0);\n" << "\tWait();\n"
      << "\tExit( 1 - RunPlugin(\"zynamics_binexport_"
      << kBinExportVersion << "\", 2 ));\n"
      << "}\n";
}

void deleteIdaScript(const std::string& outPath) {
  boost::filesystem::path path(outPath);
  boost::filesystem::remove(path / "runIda.idc");
}

void listFiles(const std::string & path) {
  TUniqueFiles files;
  boost::filesystem::path inPath(path.c_str());
  for (boost::filesystem::directory_iterator i(inPath),
      end = boost::filesystem::directory_iterator(); i != end; ++i) {
    try {
      if (boost::algorithm::to_lower_copy(boost::filesystem::extension(*i)) ==
          ".binexport") {
        std::ifstream file(i->path().c_str(), std::ios_base::binary);
        google::protobuf::io::IstreamInputStream stream(&file);
        BinExportHeader header(&file);
        BinExport::Meta metaInformation;
        metaInformation.ParseFromBoundedZeroCopyStream(
            &stream, header.call_graph_offset - header.meta_offset);
        LOG(INFO) << EncodeHex(metaInformation.input_hash()) << " ("
                 << metaInformation.input_binary() << ")";
      }
    } catch(const std::runtime_error& error) {
      LOG(INFO) << error.what() << " " << i->path();
    }
  }
}

void batchDiff(const std::string& path, const std::string& referenceFile,
               const std::string& outPath,
               const boost::program_options::variables_map& options) {
  // collect idb files to diff
  TUniqueFiles idbFiles;
  TUniqueFiles diffFiles;
  boost::filesystem::path inPath(path.c_str());
  for (boost::filesystem::directory_iterator i(inPath),
      end = boost::filesystem::directory_iterator(); i != end; ++i) {
    // export all idbs in directory
    if (boost::algorithm::to_lower_copy(boost::filesystem::extension(*i)) ==
        ".idb" || boost::algorithm::to_lower_copy(
            boost::filesystem::extension(*i)) == ".i64") {
      if (boost::filesystem::file_size(*i))
        idbFiles.insert(boost::filesystem::basename(*i));
      else
        LOG(INFO) << "Warning: skipping empty file " << *i;
    } else if (boost::algorithm::to_lower_copy(
          boost::filesystem::extension(*i)) == ".binexport") {
      diffFiles.insert(boost::filesystem::basename(*i));
    }
  }

  // remove all idbs that have already been exported from export todo list
  // @bug: this won't work if outdir != indir
  // @bug: this also doesn't work with the new exporter that prepends a
  //       directory name to the .BinExport filename
  // TUniqueFiles temp;
  // std::set_difference( idbFiles.begin(), idbFiles.end(), diffFiles.begin(),
  //     diffFiles.end(), std::inserter( temp, temp.begin()));
  // add all .callGraph files to todo list
  diffFiles.insert(idbFiles.begin(), idbFiles.end());
  // temp.swap( idbFiles );

  // create todo list of file pairs
  TFiles files;
  for (TUniqueFiles::const_iterator i = diffFiles.begin(),
      end = diffFiles.end(); i != end; ++i) {
    for (TUniqueFiles::const_iterator j = diffFiles.begin(); j != end; ++j) {
      if (i != j) {
        if (referenceFile.empty() || referenceFile == *i) {
          files.push_back(std::make_pair(*i, *j));
        }
      }
    }
  }

  // @note: @bug: remove. this makes the code intentionally inefficient for
  //              benchmarking purposes
  // std::vector< TFiles::value_type > shuffle( files.begin(), files.end());
  // std::random_shuffle( shuffle.begin(), shuffle.end());
  // files.assign( shuffle.begin(), shuffle.end());

  const size_t nrOfIdbs = idbFiles.size();
  const size_t nrOfDiffs = files.size();
  const unsigned nrOfHardwareThreads = boost::thread::hardware_concurrency();
  XmlConfig config(XmlConfig::GetDefaultFilename(), "BinDiffDeluxe");
  const unsigned nrOfThreads =
      config.ReadInt("/BinDiffDeluxe/Threads/@use", nrOfHardwareThreads);
  const std::string idaDir =
      config.ReadString("/BinDiffDeluxe/Ida/@directory", "");
  const std::string idaExe =
      config.ReadString("/BinDiffDeluxe/Ida/@executable", "");
  const std::string idaExe64 =
      config.ReadString("/BinDiffDeluxe/Ida/@executable64", "");
  boost::timer timer;
  { // export
    if (!idbFiles.empty())
      createIdaScript(outPath);
    boost::thread_group threads;
    for (unsigned i = 0; i < nrOfThreads; ++i) {
      threads.create_thread(CExporterThread(
          inPath.string(), outPath, idaDir, idaExe, idaExe64, &idbFiles));
    }
    threads.join_all();
  }
  const double exportTime = timer.elapsed();

  timer.restart();
  if (!options.count("export")) {   // perform diff
    boost::thread_group threads;
    for (unsigned i = 0; i < nrOfThreads; ++i)
      threads.create_thread(CDifferThread(outPath, outPath, options, &files));
    threads.join_all();
  }
  const double diffTime = timer.elapsed();
  deleteIdaScript(outPath);

  LOG(INFO)
      << nrOfIdbs << " files exported in " << std::fixed << std::setprecision(2)
      << exportTime << " seconds, "
      << (nrOfDiffs * (1 - options.count("export"))) << " pairs diffed in "
      << std::fixed << std::setprecision(2) << diffTime << " seconds";
}

void dumpMdIndices(const CallGraph& callGraph, const FlowGraphs& flowGraphs) {
  std::cout << "\n" << callGraph.GetFilename() << "\n"
      << callGraph.GetMdIndex();
  for (FlowGraphs::const_iterator i = flowGraphs.begin(),
      end = flowGraphs.end(); i != end; ++i) {
    std::cout << "\n" << (*i)->IsLibrary() << "\t" << std::fixed
        << std::setprecision(12) << (*i)->GetMdIndex();
  }
  std::cout << std::endl;
}

void batchDumpMdIndices(const std::string& path) {
  boost::filesystem::path inPath(path.c_str());
  for (boost::filesystem::directory_iterator i(inPath),
      end = boost::filesystem::directory_iterator(); i != end; ++i) {
    if (boost::filesystem::extension(*i) != ".callGraph")
      continue;

    CallGraph callGraph;
    FlowGraphs flowGraphs;
    Instruction::Cache instructionCache;
    ScopedCleanup cleanup(&flowGraphs, 0, &instructionCache);
    FlowGraphInfos infos;
    Read(i->path().string(), callGraph, flowGraphs, infos, &instructionCache);
    dumpMdIndices(callGraph, flowGraphs);
  }
}

void signalHandler(int code) {
  switch (code) {
#ifndef UNIX_COMPILE
    case SIGBREAK:  // Ctrl-Break, not available on Unix
#endif
    case SIGINT:    // Ctrl-C
      LOG(INFO) << "Please wait, initiating orderly shutdown after current "
          "operations finish.";
      g_WantsToQuit = true;
      break;
  }
}

int main(int nrOfArguments, char** arguments) {
#ifndef UNIX_COMPILE
  signal(SIGBREAK, signalHandler);
#endif
  signal(SIGINT, signalHandler);

  XmlConfig::SetDefaultFilename(
    boost::filesystem::exists(
      GetDirectory(PATH_APPDATA, "BinDiff", false) + "BinDiffDeluxe.xml")
        ? GetDirectory(PATH_APPDATA, "BinDiff", false) + "BinDiffDeluxe.xml"
        : GetDirectory(PATH_COMMONAPPDATA, "BinDiff", false) + "BinDiffDeluxe.xml");

  int returnCode(0);

  try {
    XmlConfig config;
    try {
      config.Init(XmlConfig::GetDefaultFilename(), "BinDiffDeluxe");
    } catch(const std::runtime_error&) {
    }  // we ignore config file not found at this point because it may still be
       // set via the command line

  LOG(INFO) << kProgramVersion << " (" << __DATE__
#ifdef _DEBUG
           << ", debug build"
#endif
           << ") - (c)2004-2014 Google Inc.";

    // echo original command line to log file
    std::string commandline;
    for (int i = 0; i < nrOfArguments; ++i)
      commandline += *(arguments + i) + std::string(" ");
    LOG(INFO) << commandline;

    boost::timer timer;

    boost::program_options::options_description description(
        "command line parameters");
    description.add_options()
        ("help,h", "shows available command line options")
        ("sourcepath1,i", boost::program_options::value<std::string>(),
            "primary input file (or path in batch mode)")
        ("sourcepath2,j", boost::program_options::value<std::string>(),
            "secondary input file (optional)")
        ("outpath,o", boost::program_options::value<std::string>(),
            "output path (optional)")
        ("log,l", "write results in log file format")
        ("knox,k", "write results in fortknox file format")
        ("bin,b", "write results in binary file format (can be loaded by "
            "DifferDeluxe IDA plugin or BinDiff GUI)")
        ("mdindex,m", "dump md indices (will not diff anything)")
        ("export,e", "batch export idb files from input directory to "
            "*.BinExport")
        ("ls", "list hash/filenames for all .BinExport files in input "
            "directory")
        ("config,c", boost::program_options::value<std::string>(),
            "specify config file name (defaults to user home directory "
            "BinDiffDeluxe.xml)");

    boost::program_options::variables_map variables;
    store(boost::program_options::command_line_parser(nrOfArguments, arguments).
        options(description).run(), variables);
    notify(variables);
    bool doneSomething = false;

    if (variables.count("config")) {
      XmlConfig::SetDefaultFilename(variables["config"].as<std::string>());
      config.Init(XmlConfig::GetDefaultFilename(), "BinDiffDeluxe");
    }
    if (!config.GetDocument())
      throw std::runtime_error("config file invalid or not found");

    // this initializes static variables before the threads get to them
    if (GetDefaultMatchingSteps().empty()
        || GetDefaultMatchingStepsBasicBlock().empty()) {
      throw std::runtime_error("config file invalid");
    }

    boost::scoped_ptr<CallGraph> callGraph1;
    boost::scoped_ptr<CallGraph> callGraph2;
    Instruction::Cache instructionCache;
    FlowGraphs flowGraphs1, flowGraphs2;
    ScopedCleanup cleanup(&flowGraphs1, &flowGraphs2, &instructionCache);

    std::string outPath(boost::filesystem::current_path().string());
    if (variables.count("outpath"))
      outPath = variables["outpath"].as<std::string> ();
    else if (variables.count("sourcepath1")
        && boost::filesystem::is_directory(
            variables["sourcepath1"].as<std::string>())) {
      outPath = variables["sourcepath1"].as<std::string>();
    }

    if (!boost::filesystem::is_directory(outPath)) {
      throw std::runtime_error(
          "outpath parameter (-o) must be a directory and writeable! Supplied "
          "value: \"" + outPath + "\"");
    }

#ifdef REASONTREE
    std::ofstream reasoningFile((outPath + "/differDeluxe.reason").c_str());
    CReasoningTree::getInstance().setTarget(&reasoningFile);
#endif

    if (variables.count("sourcepath1")
        && boost::filesystem::is_regular_file(
            variables["sourcepath1"].as<std::string>())) {
      // primary from filesystem
      FlowGraphInfos infos;
      callGraph1.reset(new CallGraph());
      Read(variables["sourcepath1"].as<std::string>(), *callGraph1,
          flowGraphs1, infos, &instructionCache);
    }

    if (variables.count("sourcepath1")
        && boost::filesystem::is_directory(
            variables["sourcepath1"].as<std::string>())) {
      // file system batch diff
      if (variables.count("ls")) {
        listFiles(variables["sourcepath1"].as<std::string>());
      } else if (!variables.count("mdindex")) {
        batchDiff(variables["sourcepath1"].as<std::string>(),
                  variables.count("sourcepath2")
                      ? variables["sourcepath2"].as<std::string> ()
                      : "",
                  outPath, variables);
      } else {
        batchDumpMdIndices(variables["sourcepath1"].as<std::string>());
      }
      doneSomething = true;
    }

    if (variables.count("mdindex") && callGraph1.get()) {
      dumpMdIndices(*callGraph1, flowGraphs1);
      doneSomething = true;
    }

    if (variables.count("sourcepath2") &&
        boost::filesystem::is_regular_file(
            variables["sourcepath2"].as<std::string>())) {
      // secondary from filesystem
      FlowGraphInfos infos;
      callGraph2.reset(new CallGraph());
      Read(variables["sourcepath2"].as<std::string> (), *callGraph2,
          flowGraphs2, infos, &instructionCache);
    }

    if (!doneSomething &&
        ((variables.count("sourcepath1") &&
          (!boost::filesystem::is_regular_file(
               variables["sourcepath1"].as<std::string>()) &&
           !boost::filesystem::is_directory(
               variables["sourcepath1"].as<std::string>()))) ||
         (variables.count("sourcepath2") &&
          (!boost::filesystem::is_regular_file(
               variables["sourcepath2"].as<std::string>()) &&
           !boost::filesystem::is_directory(
               variables["sourcepath2"].as<std::string>()))))) {
      throw std::runtime_error(
          "Invalid inputs. Please make sure -i and -j "
          "point to valid files/directories.");
    }

    if (callGraph1.get() && callGraph2.get()) {
      const int edges1 = num_edges(callGraph1->GetGraph());
      const int vertices1 = num_vertices(callGraph1->GetGraph());
      const int edges2 = num_edges(callGraph2->GetGraph());
      const int vertices2 = num_vertices(callGraph2->GetGraph());
      LOG(INFO)
          << "setup: " << timer.elapsed() << " sec. "
          << callGraph1->GetFilename() << " has " << vertices1
          << " functions and " << edges1 << " calls. "
          << callGraph2->GetFilename() << " has " << vertices2
          << " functions and " << edges2 << " calls.";
      timer.restart();

      const MatchingSteps default_callgraph_steps(GetDefaultMatchingSteps());
      const MatchingStepsFlowGraph default_basicblock_steps(
          GetDefaultMatchingStepsBasicBlock());
      FixedPoints fixedPoints;
      MatchingContext context(
          *callGraph1, *callGraph2, flowGraphs1, flowGraphs2, fixedPoints);
      Diff(&context, default_callgraph_steps, default_basicblock_steps);

      Histogram histogram;
      Counts counts;
      GetCountsAndHistogram(
          flowGraphs1, flowGraphs2, fixedPoints, &histogram, &counts);
      Confidences confidences;
      const double confidence = GetConfidence(histogram, &confidences);
      const double similarity = GetSimilarityScore(
          *callGraph1, *callGraph2, histogram, counts);

      LOG(INFO) << "matching: " << timer.elapsed() << " sec.";
      timer.restart();

      LOG(INFO)
          << "matched " << fixedPoints.size() << " of "
          << flowGraphs1.size() << "/" << flowGraphs2.size() << " ("
          << counts.find("functions primary (non-library)")->second << "/"
          << counts.find("functions secondary (non-library)")->second << ")";
      LOG(INFO)
          << "callGraph1 MD index " << std::dec << std::setprecision(16)
          << callGraph1->GetMdIndex() << "\tcallGraph2 MD index " << std::dec
          << std::setprecision(16) << callGraph2->GetMdIndex();
      LOG(INFO)
          << "similarity: " << std::setfill(' ') << std::setw(5)
          << std::setprecision(4) << (similarity * 100.0) << "%"
          << "\tconfidence: " << std::setfill(' ') << std::setw(5)
          << std::setprecision(4) << (confidence * 100.0) << "%";

      ChainWriter writer;
      if (variables.count("log")) {
        writer.Add(boost::shared_ptr<Writer>(new ResultsLogWriter(
            getTruncatedFilename(
                outPath + "/",
                callGraph1->GetFilename(),
                "_vs_",
                callGraph2->GetFilename(),
                ".results"))));
      }
      if (variables.count("bin") ||
          (writer.IsEmpty() && variables.count("outpath"))) {
        writer.Add(boost::shared_ptr<Writer>(new DatabaseWriter(
            getTruncatedFilename(
                outPath + "/",
                callGraph1->GetFilename(),
                "_vs_",
                callGraph2->GetFilename(),
                ".BinDiff"))));
      }

      if (!writer.IsEmpty()) {
        writer.Write(
            *callGraph1, *callGraph2, flowGraphs1, flowGraphs2, fixedPoints);
        LOG(INFO)
            << "writing results: " << std::setprecision(3) << timer.elapsed()
            << " sec." << std::endl;
      }
      timer.restart();
      doneSomething = true;
    }

    if (variables.count("help") || !doneSomething) {
      std::cout << description << std::endl;
      std::cout
        << "example command line to diff all files in a directory against each "
           "other:\nBinDiff_Deluxe -iC:/temp -oC:/temp/result\nnote that if the"
           " directory contains idbs these will automatically\nbe exported "
           "first.\nfor a single diff:\nBinDiff_Deluxe "
           "-iC:/temp/file1.BinExport -jC:/temp/file2.BinExport "
           "-oC:/temp/result" << std::endl;
    }
  } catch(const std::bad_alloc&) {
      LOG(INFO)
          << "Out-of-memory. Please try again with more memory available. Some "
             "extremely large binaries\nmay require a 64bit version of BinDiff "
             "- please contact zynamics to request one.";
    returnCode = 3;
  } catch(const std::exception& error) {
    LOG(INFO) << "an error occurred: " << error.what();
    returnCode = 1;
  } catch(...) {
    LOG(INFO) << "an unknown error occurred";
    returnCode = 2;
  }

  // Need to explicitly shutdown logging, otherwise the logging thread will stay
  // active
  ShutdownLogging();
  return returnCode;
}

