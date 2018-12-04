#include <cstdint>
#include <cstdio>
#include <fstream>
#include <limits>
#include <memory>
#include <thread>  // NOLINT(build/c++11)

#include "third_party/absl/strings/string_view.h"
#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <bytes.hpp>                                            // NOLINT
#include <diskio.hpp>                                           // NOLINT
#include <enum.hpp>                                             // NOLINT
#include <expr.hpp>                                             // NOLINT
#include <frame.hpp>                                            // NOLINT
#include <funcs.hpp>                                            // NOLINT
#include <ida.hpp>                                              // NOLINT
#include <idp.hpp>                                              // NOLINT
#include <kernwin.hpp>                                          // NOLINT
#include <loader.hpp>                                           // NOLINT
#include <nalt.hpp>                                             // NOLINT
#include <name.hpp>                                             // NOLINT
#include <struct.hpp>                                           // NOLINT
#include <ua.hpp>                                               // NOLINT
#include <xref.hpp>                                             // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT

#include "base/logging.h"
#include "third_party/absl/base/macros.h"
#include "third_party/absl/strings/ascii.h"
#include "third_party/absl/strings/escaping.h"
#include "third_party/absl/strings/match.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/time/time.h"
#include "third_party/zynamics/bindiff/call_graph_match.h"
#include "third_party/zynamics/bindiff/change_classifier.h"
#include "third_party/zynamics/bindiff/database_writer.h"
#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/bindiff/flow_graph_match.h"
#include "third_party/zynamics/bindiff/groundtruth_writer.h"
#include "third_party/zynamics/bindiff/ida/bindiff_icon.h"
#include "third_party/zynamics/bindiff/ida/matched_functions_chooser.h"
#include "third_party/zynamics/bindiff/ida/results.h"
#include "third_party/zynamics/bindiff/ida/statistics_chooser.h"
#include "third_party/zynamics/bindiff/ida/unmatched_functions_chooser.h"
#include "third_party/zynamics/bindiff/ida/visual_diff.h"
#include "third_party/zynamics/bindiff/log_writer.h"
#include "third_party/zynamics/bindiff/match_context.h"
#include "third_party/zynamics/bindiff/utility.h"
#include "third_party/zynamics/bindiff/version.h"
#include "third_party/zynamics/bindiff/xmlconfig.h"
#include "third_party/zynamics/binexport/ida/digest.h"
#include "third_party/zynamics/binexport/ida/log.h"
#include "third_party/zynamics/binexport/ida/ui.h"
#include "third_party/zynamics/binexport/types.h"
#include "third_party/zynamics/binexport/util/filesystem.h"
#include "third_party/zynamics/binexport/util/format.h"
#include "third_party/zynamics/binexport/util/timer.h"
#include "util/task/status_macros.h"
#include "util/task/statusor.h"

namespace security {

using binexport::FormatAddress;
using binexport::HumanReadableDuration;

namespace bindiff {

XmlConfig* g_config = nullptr;  // Used in visual_diff.cc

namespace {

constexpr char kBinExportVersion[] = "10";  // Exporter version to use.
constexpr char kName[] = "BinDiff 5";
constexpr char kComment[] =
    "Structural comparison of executable objects";  // Status line
constexpr char kHotKey[] = "CTRL-6";
constexpr char kCopyright[] =
    "(c)2004-2011 zynamics GmbH, (c)2011-2018 Google LLC.";

bool g_init_done = false;  // Used in PluginTerminate()
bool g_alsologtostderr = false;

Results* g_results = nullptr;

enum ResultFlags {
  kResultsShowMatched = 1 << 0,
  kResultsShowStatistics = 1 << 1,
  kResultsShowPrimaryUnmatched = 1 << 2,
  kResultsShowSecondaryUnmatched = 1 << 3,
  kResultsShowAll = 0xffffffff
};

string GetArgument(const char* name) {
  const char* option =
      get_plugin_options(absl::StrCat("BinDiff", name).c_str());
  return option ? option : "";
}

bool DoSaveResults();

string FindFile(const string& path, const string& extension) {
  std::vector<string> entries;
  GetDirectoryEntries(path, &entries);
  for (const auto& entry : entries) {
    if (GetFileExtension(entry) == extension) {
      return JoinPath(path, Basename(entry));
    }
  }
  return "";
}

bool EnsureIdb() {
  bool result = strlen(get_path(PATH_TYPE_IDB)) > 0;
  if (!result) {
    info("Please open an IDB first.");
  }
  return result;
}

class ExporterThread {
 public:
  struct Options {
    Options& set_alsologtostderr(bool value) {
      alsologtostderr = value;
      return *this;
    }

    Options& set_headless_export_mode(bool value) {
      headless_export_mode = value;
      return *this;
    }

    bool alsologtostderr = false;
    bool headless_export_mode = false;
  };

  static util::StatusOr<ExporterThread> Create(const string& temp_dir,
                                               const string& idb_path,
                                               Options options) {
    string secondary_temp_dir = JoinPath(temp_dir, "secondary");
    RemoveAll(secondary_temp_dir);
    RETURN_IF_ERROR(CreateDirectories(secondary_temp_dir));

    string idc_file = JoinPath(temp_dir, "export_secondary.idc");
    if (!options.headless_export_mode) {
      std::ofstream file{idc_file, std::ios::binary | std::ios::trunc};
      file << "#include <idc.idc>\n"
           << "static main()\n"
           << "{\n"
           << "\tBatch(0);\n"
           << "\tWait();\n"
           << "\tRunPlugin(\"binexport" << kBinExportVersion << "\", 2);\n"
           << "\tExit(0);\n"
           << "}\n";
      if (!file) {
        return util::Status{absl::StatusCode::kUnknown,
                            "error writing helper IDC script"};
      }
    }
    ExporterThread result;
    result.secondary_idb_path_ = idb_path;
    result.secondary_temp_dir_ = std::move(secondary_temp_dir);
    result.idc_file_ = std::move(idc_file);
    result.options_ = std::move(options);
    return result;
  }

  void operator()() {
    const bool is_64bit =
        absl::EndsWith(absl::AsciiStrToUpper(secondary_idb_path_), ".I64");
    std::vector<string> args = {
        JoinPath(idadir(0), is_64bit ? "ida64" : "ida")};
#ifdef WIN32
    args[0] = ReplaceFileExtension(args[0], ".exe");
#endif

    args.push_back("-A");
    args.push_back(absl::StrCat("-OBinExportModule:", secondary_temp_dir_));

    if (options_.alsologtostderr) {
      args.push_back("-OBinExportAlsoLogToStdErr:TRUE");
    }

    if (!options_.headless_export_mode) {
      // Script parameter: We only support the Qt version.
#ifdef WIN32
      args.push_back(absl::StrCat("-S\"", idc_file_, "\""));
#else
      args.push_back(absl::StrCat("-S", idc_file_));
#endif
    } else {
      SetEnvironmentVariable("TVHEADLESS", "1");
      args.push_back("-OBinExportAutoAction:BinExportBinary");
    }

    args.push_back(secondary_idb_path_);

    success_or_ = SpawnProcessAndWait(args);

    // Reset environment variable.
    SetEnvironmentVariable("TVHEADLESS", /*value=*/"");
  }

  bool success() const { return success_or_.ok(); }

  string status() const { return success_or_.status().error_message(); }

 private:
  friend class util::StatusOr<ExporterThread>;
  ExporterThread() = default;

  util::StatusOr<int> success_or_;  // Defaults to absl::StatusCode::kUnknown.
  string secondary_idb_path_;
  string secondary_temp_dir_;
  string idc_file_;
  Options options_;
};

bool ExportIdbs() {
  if (!EnsureIdb()) {
    return false;
  }

  auto temp_dir_or = GetOrCreateTempDirectory("BinDiff");
  if (!temp_dir_or.ok()) {
    return false;
  }
  const string temp_dir = std::move(temp_dir_or).ValueOrDie();

  const char* secondary_idb = ask_file(
      /*for_saving=*/false, "*.idb;*.i64", "%s",
      absl::StrCat("FILTER IDA Databases|*.idb;*.i64|All files|",
                   kAllFilesFilter, "\nSelect Database")
          .c_str());
  if (!secondary_idb) {
    return false;
  }

  const string primary_idb_path(get_path(PATH_TYPE_IDB));
  string secondary_idb_path(secondary_idb);
  if (primary_idb_path == secondary_idb_path) {
    throw std::runtime_error(
        "You cannot open the same IDB file twice. Please copy and rename one if"
        " you want to diff against self.");
  } else if (ReplaceFileExtension(primary_idb_path, "") ==
             ReplaceFileExtension(secondary_idb_path, "")) {
    throw std::runtime_error(
        "You cannot open an idb and an i64 with the same base filename in the "
        "same directory. Please rename or move one of the files.");
  } else if (absl::AsciiStrToUpper(GetFileExtension(primary_idb_path)) ==
                 ".IDB" &&
             absl::AsciiStrToUpper(GetFileExtension(secondary_idb_path)) ==
                 ".I64") {
    if (ask_yn(ASKBTN_YES,
               "Warning: you are trying to diff a 32-bit binary vs. a 64-bit "
               "binary.\n"
               "If the 64-bit binary contains addresses outside the 32-bit "
               "range they will be truncated.\n"
               "To fix this problem please start 64-bit aware IDA and diff "
               "the other way around, i.e. 64-bit vs. 32-bit.\n"
               "Continue anyways?") != 1) {
      return false;
    }
  }
  delete g_results;
  g_results = nullptr;

  LOG(INFO) << "Diffing " << Basename(primary_idb_path) << " vs "
            << Basename(secondary_idb_path);
  WaitBox wait_box("Exporting idbs...");
  {
    auto exporter_or =
        ExporterThread::Create(temp_dir, secondary_idb_path,
                               ExporterThread::Options{}
                                   .set_alsologtostderr(g_alsologtostderr)
                                   .set_headless_export_mode(g_config->ReadBool(
                                       "/BinDiff/Ida/headlessExport",
                                       /*default_value=*/false)));
    if (!exporter_or.ok()) {
      throw std::runtime_error{
          absl::StrCat("Export of the current database failed: ",
                       string(exporter_or.status().error_message()))};
    }
    auto exporter = std::move(exporter_or).ValueOrDie();
    std::thread thread(std::ref(exporter));

    string primary_temp_dir(JoinPath(temp_dir, "primary"));
    RemoveAll(primary_temp_dir);
    auto status = CreateDirectories(primary_temp_dir);
    if (!status.ok()) {
      throw std::runtime_error(status.error_message());
    }

    qstring errbuf;
    idc_value_t arg(primary_temp_dir.c_str());
    if (!call_idc_func(
            /*result=*/nullptr, "BinExportBinary", &arg,
            /*argsnum=*/1, &errbuf, /*resolver=*/nullptr)) {
      thread.detach();
      throw std::runtime_error(absl::StrCat(
          "Export of the current database failed: ", errbuf.c_str()));
    }

    thread.join();
    if (!exporter.success()) {
      throw std::runtime_error(absl::StrCat(
          "Failed to spawn second IDA instance: ", exporter.status()));
    }
  }

  return true;
}

void DoVisualDiff(uint32_t index, bool call_graph_diff) {
  try {
    string message;
    if (!g_results) {
      return;
    }
    if (!call_graph_diff) {
      if (!g_results->PrepareVisualDiff(index, &message)) {
        return;
      }
    } else {
      if (!g_results->PrepareVisualCallGraphDiff(index, &message)) {
        return;
      }
    }

    LOG(INFO) << "Sending result to BinDiff GUI...";
    SendGuiMessage(
        g_config->ReadInt("/BinDiff/Gui/@retries", 20),
        g_config->ReadString("/BinDiff/Gui/@directory",
                             // TODO(cblichmann): Use better defaults
                             "C:\\Program Files\\zynamics\\BinDiff 4.3\\bin"),
        g_config->ReadString("/BinDiff/Gui/@server", "127.0.0.1"),
        static_cast<uint16_t>(g_config->ReadInt("/BinDiff/Gui/@port", 2000)),
        message, nullptr);
  } catch (const std::runtime_error& message) {
    LOG(INFO) << "Error while calling BinDiff GUI: " << message.what();
    warning("Error while calling BinDiff GUI: %s\n", message.what());
  } catch (...) {
    LOG(INFO) << "Unknown error while calling BinDiff GUI";
    warning("Unknown error while calling BinDiff GUI\n");
  }
}

uint32_t idaapi PortCommentsSelectionAsLib(void* /* unused */, uint32_t index) {
  try {
    return g_results->PortComments(index, true /* as_external */);
  } catch (const std::exception& message) {
    LOG(INFO) << "Error: " << message.what();
    warning("Error: %s\n", message.what());
  } catch (...) {
    LOG(INFO) << "Unknown error while porting comments";
    warning("Unknown error while porting comments\n");
  }
  return 0;
}

uint32_t idaapi CopyPrimaryAddress(void* /* unused */, uint32_t index) {
  if (!g_results) {
    return 0;
  }
  return g_results->CopyPrimaryAddress(index);
}

uint32_t idaapi CopySecondaryAddress(void* /* unused */, uint32_t index) {
  if (!g_results) {
    return 0;
  }
  return g_results->CopySecondaryAddress(index);
}

uint32_t idaapi CopyPrimaryAddressUnmatched(void* /* unused */, uint32_t index) {
  if (!g_results) {
    return 0;
  }
  return g_results->CopyPrimaryAddressUnmatched(index);
}

uint32_t idaapi CopySecondaryAddressUnmatched(void* /* unused */, uint32_t index) {
  if (!g_results) {
    return 0;
  }
  return g_results->CopySecondaryAddressUnmatched(index);
}

uint32_t idaapi GetNumUnmatchedPrimary(void* /* unused */) {
  if (!g_results) return 0;

  return g_results->GetNumUnmatchedPrimary();
}

uint32_t idaapi GetNumUnmatchedSecondary(void* /* unused */) {
  if (!g_results) {
    return 0;
  }
  return g_results->GetNumUnmatchedSecondary();
}

void idaapi jumpToUnmatchedPrimaryAddress(void* /* unused */, uint32_t index) {
  if (!g_results) {
    return;
  }
  jumpto(static_cast<ea_t>(g_results->GetPrimaryAddress(index)));
}

uint32_t idaapi AddMatchPrimary(void* /* unused */, uint32_t index) {
  if (!g_results) return 0;

  try {
    WaitBox wait_box("Performing basicblock diff...");
    return g_results->AddMatchPrimary(index);
  } catch (const std::exception& message) {
    LOG(INFO) << "Error: " << message.what();
    warning("Error: %s\n", message.what());
  } catch (...) {
    LOG(INFO) << "Unknown error while adding match";
    warning("Unknown error while adding match\n");
  }
  return 0;
}

uint32_t idaapi AddMatchSecondary(void* /* unused */, uint32_t index) {
  if (!g_results) return 0;

  try {
    WaitBox wait_box("Performing basicblock diff...");
    return g_results->AddMatchSecondary(index);
  } catch (const std::exception& message) {
    LOG(INFO) << "Error: " << message.what();
    warning("Error: %s\n", message.what());
  } catch (...) {
    LOG(INFO) << "Unknown error while adding match";
    warning("Unknown error while adding match\n");
  }
  return 0;
}

void idaapi JumpToMatchAddress(void* /* unused */, uint32_t index) {
  if (!g_results) return;

  jumpto(static_cast<ea_t>(g_results->GetMatchPrimaryAddress(index)));
}

void SaveAndDiscardResults() {
  if (g_results && g_results->IsDirty()) {
    const int answer(ask_yn(ASKBTN_YES,
                            "HIDECANCEL\nCurrent diff results have not been"
                            " saved - save before closing?"));
    if (answer == 1) {  // Yes
      DoSaveResults();
    }
  }

  delete g_results;
  g_results = 0;
}

ssize_t idaapi ProcessorHook(void*, int event_id, va_list /*arguments*/) {
  switch (event_id) {
    case processor_t::ev_term:
      if (!is_database_flag(DBFL_KILL) && g_results) {
        g_results->MarkPortedCommentsInDatabase();
      }
      break;
  }
  return 0;
}

ssize_t idaapi IdbHook(void*, int event_id, va_list /*arguments*/) {
  switch (event_id) {
    case idb_event::closebase:
      SaveAndDiscardResults();
      break;
  }
  return 0;
}

ssize_t idaapi UiHook(void*, int event_id, va_list arguments) {
  switch (event_id) {
    case ui_preprocess_action: {
      absl::string_view name(va_arg(arguments, const char*));
      if (name == "LoadFile" || name == "NewFile" || name == "CloseBase" ||
          name == "Quit") {
        if (g_results && g_results->IsDirty()) {
          const int answer(ask_yn(ASKBTN_YES,
                                  "Current diff results have not been saved -"
                                  " save before closing?"));
          if (answer == 1) {
            // Yes
            DoSaveResults();
          } else if (answer == -1) {
            // Cancel
            // Return that we handled the command ourselves, this will
            // not even show IDA's close dialog
            return 1;
          }
        }
        // delete old results if they weren't dirty of if the user
        // saved/discarded them
        delete g_results;
        g_results = nullptr;
        // Refreshing the choosers here is important as after our
        // "Save results?" confirmation dialog IDA will open its own
        // confirmation. The user can cancel in that dialog. So a "bad"
        // sequence of events is: don't save diff results, but cancel the
        // closing operation in the following IDA dialog. In an ideal world
        // the diff results would be back. As it is we lose the results but
        // at least leave windows/internal data structures in a consistent
        // state.
        refresh_chooser("Matched Functions");
        refresh_chooser("Primary Unmatched");
        refresh_chooser("Secondary Unmatched");
        refresh_chooser("Statistics");
      }
      break;
    }
    case ui_finish_populating_widget_popup: {
      auto* widget = va_arg(arguments, TWidget*);
      auto* popup_handle = va_arg(arguments, TPopupMenu*);
      for (auto& attach : {MatchedFunctionsChooser::AttachActionsToPopup}) {
        if (attach(widget, popup_handle)) {
          break;
        }
      }
      break;
    }
  }
  return 0;
}

void ShowResults(Results* results, const ResultFlags flags = kResultsShowAll) {
  if (!results || results != g_results) {
    throw std::runtime_error("trying to show invalid results");
  }

  results->CreateIndexedViews();

  if (flags & kResultsShowMatched) {
    (new MatchedFunctionsChooser(g_results))->choose();
#if 0
    if (!find_tform("Matched Functions")) {
      static const char* popups[] =  // insert, delete, edit, refresh
          {"Delete Match", "Delete Match", "View Flowgraphs", "Refresh"};
      close_chooser("Matched Functions");
              &DeleteMatch,            // delete callback
              0,                       // insert callback
              0,                       // update callback
              &VisualDiffCallback,     // edit callback (visual diff)
              &JumpToMatchAddress,     // enter callback (jump to)
              nullptr,                 // destroy callback
              popups,                  // popups (insert, delete, edit, refresh)
              0);
      // not currently implemented/used
      // add_chooser_command( "Matched Functions", "View Call graphs",
      //    &VisualCallGraphDiffCallback, -1, -1, CHOOSER_POPUP_MENU );
      add_chooser_command("Matched Functions", "Import Symbols and Comments",
                          &PortCommentsSelection, -1, -1,
                          CHOOSER_POPUP_MENU | CHOOSER_MULTI_SELECTION);
      add_chooser_command("Matched Functions",
                          "Import Symbols and Comments as external lib",
                          &PortCommentsSelectionAsLib, -1, -1,
                          CHOOSER_POPUP_MENU | CHOOSER_MULTI_SELECTION);
      add_chooser_command("Matched Functions", "Confirm Match", &ConfirmMatch,
                          -1, -1, CHOOSER_POPUP_MENU | CHOOSER_MULTI_SELECTION);
#ifdef WIN32
      add_chooser_command("Matched Functions", "Copy Primary Address",
                          &CopyPrimaryAddress, -1, -1, CHOOSER_POPUP_MENU);
      add_chooser_command("Matched Functions", "Copy Secondary Address",
                          &CopySecondaryAddress, -1, -1, CHOOSER_POPUP_MENU);
#endif
    } else {
      refresh_chooser("Matched Functions");
    }
#endif
  }

  if (flags & kResultsShowStatistics) {
    (new StatisticsChooser(g_results))->choose();
#if 0
    if (!find_tform("Statistics")) {
    } else {
      refresh_chooser("Statistics");
    }
#endif
  }

  if (flags & kResultsShowPrimaryUnmatched) {
    (new UnmatchedFunctionsChooserPrimary(g_results))->choose();
#if 0
    choose2(0, -1, -1, -1, -1, static_cast<void*>(0),
            sizeof(widths) / sizeof(widths[0]), widths,
            &GetNumUnmatchedPrimary, &GetUnmatchedPrimaryDescription,
            "Primary Unmatched", -1,         // icon
            1,                               // default
            0,                               // delete callback
            0,                               // new callback
            0,                               // update callback
            0,                               // edit callback
            &jumpToUnmatchedPrimaryAddress,  // enter callback
            nullptr,                         // destroy callback
            popups,  // popups (insert, delete, edit, refresh)
            0);
    add_chooser_command("Primary Unmatched", "Add Match", &AddMatchPrimary,
                        -1, -1, CHOOSER_POPUP_MENU);
#ifdef WIN32
    add_chooser_command("Primary Unmatched", "Copy Address",
                        &CopyPrimaryAddressUnmatched, -1, -1,
                        CHOOSER_POPUP_MENU);
#endif
    refresh_chooser("Primary Unmatched");
#endif
  }

  if (flags & kResultsShowSecondaryUnmatched) {
    (new UnmatchedFunctionsChooserSecondary(g_results))->choose();
  }
}

bool idaapi MenuItemShowResultsCallback(void* user_data) {
  if (!g_results) {
    vinfo("Please perform a diff first", 0);
    return false;
  }

  const ResultFlags flags =
      static_cast<ResultFlags>(reinterpret_cast<int64_t>(user_data));
  ShowResults(g_results, flags);
  return true;
}

// deletes all nodes from callgraph (and corresponding flow graphs) that are
// not within the specified range
void FilterFunctions(ea_t start, ea_t end, CallGraph* call_graph,
                     FlowGraphs* flow_graphs,
                     FlowGraphInfos* flow_graph_infos) {
  for (auto it = flow_graphs->begin(); it != flow_graphs->end();) {
    const FlowGraph* flow_graph = *it;
    const auto entry_point = flow_graph->GetEntryPointAddress();
    if (entry_point < start || entry_point > end) {
      flow_graph_infos->erase(entry_point);
      delete flow_graph;
      // GNU implementation of erase does not return an iterator.
      flow_graphs->erase(it++);
    } else {
      ++it;
    }
  }
  call_graph->DeleteVertices(start, end);
}

bool DiffAddressRange(ea_t start_address_source, ea_t end_address_source,
                      ea_t start_address_target, ea_t end_address_target) {
  Timer<> timer;
  try {
    if (!ExportIdbs()) {
      return false;
    }
  } catch (...) {
    delete g_results;
    g_results = nullptr;
    throw;
  }

  LOG(INFO) << absl::StrCat(HumanReadableDuration(timer.elapsed()),
                            " for exports...");
  LOG(INFO) << absl::StrCat(
      "Diffing address range primary(", FormatAddress(start_address_source),
      " - ", FormatAddress(end_address_source), ") vs secondary(",
      FormatAddress(start_address_target), " - ",
      FormatAddress(end_address_target), ")");
  timer.restart();

  WaitBox wait_box("Performing diff...");
  g_results = new Results();
  auto temp_dir_or = GetOrCreateTempDirectory("BinDiff");
  if (!temp_dir_or.ok()) {
    return false;
  }
  const auto temp_dir = std::move(temp_dir_or).ValueOrDie();
  const auto filename1(FindFile(JoinPath(temp_dir, "primary"), ".BinExport"));
  const auto filename2(FindFile(JoinPath(temp_dir, "secondary"), ".BinExport"));
  if (filename1.empty() || filename2.empty()) {
    throw std::runtime_error(
        "Export failed. Is the secondary IDB opened in another IDA instance? "
        "Please close all other IDA instances and try again.");
  }

  Read(filename1, &g_results->call_graph1_, &g_results->flow_graphs1_,
       &g_results->flow_graph_infos1_, &g_results->instruction_cache_);
  Read(filename2, &g_results->call_graph2_, &g_results->flow_graphs2_,
       &g_results->flow_graph_infos2_, &g_results->instruction_cache_);
  MatchingContext context(g_results->call_graph1_, g_results->call_graph2_,
                          g_results->flow_graphs1_, g_results->flow_graphs2_,
                          g_results->fixed_points_);
  FilterFunctions(start_address_source, end_address_source,
                  &context.primary_call_graph_, &context.primary_flow_graphs_,
                  &g_results->flow_graph_infos1_);
  FilterFunctions(
      start_address_target, end_address_target, &context.secondary_call_graph_,
      &context.secondary_flow_graphs_, &g_results->flow_graph_infos2_);

  const MatchingSteps default_callgraph_steps(GetDefaultMatchingSteps());
  const MatchingStepsFlowGraph default_basicblock_steps(
      GetDefaultMatchingStepsBasicBlock());
  Diff(&context, default_callgraph_steps, default_basicblock_steps);
  LOG(INFO) << absl::StrCat(HumanReadableDuration(timer.elapsed()),
                            " for matching.");

  ShowResults(g_results);
  g_results->SetDirty();

  return true;
}

bool DoRediffDatabase() {
  try {
    if (!g_results) {
      warning(
          "You need to provide a normal diff before diffing incrementally. "
          "Either create or load one. Diffing incrementally will keep all "
          "manually confirmed matches in the result and try to reassign all "
          "other matches.");
      return false;
    }
    const bool result = g_results->IncrementalDiff();
    ShowResults(g_results);
    return result;
  } catch (const std::exception& message) {
    LOG(INFO) << "Error while diffing: " << message.what();
    warning("Error while diffing: %s\n", message.what());
  } catch (...) {
    LOG(INFO) << "Unknown error while diffing.";
    warning("Unknown error while diffing.");
  }
  return false;
}

bool DoDiffDatabase(bool filtered) {
  try {
    if (g_results && g_results->IsDirty()) {
      const int answer =
          ask_yn(ASKBTN_YES,
                 "HIDECANCEL\nCurrent diff results have not been saved - save "
                 "before closing?");
      if (answer == 1) {  // yes
        DoSaveResults();
      } else if (answer == -1) {  // cancel
        return false;
      }
    }

    // Default to full address range
    ea_t start_address_source = 0;
    ea_t end_address_source = std::numeric_limits<ea_t>::max() - 1;
    ea_t start_address_target = 0;
    ea_t end_address_target = std::numeric_limits<ea_t>::max() - 1;

    if (filtered) {
      static const char kDialog[] =
          "STARTITEM 0\n"
          "Diff Database Filtered\n"
          "Specify address ranges to diff (default: all)\n\n"
          "  <Start address (primary)      :$::16::>\n"
          "  <End address (primary):$::16::>\n"
          "  <Start address (secondary):$::16::>\n"
          "  <End address (secondary):$::16::>\n\n";
      if (!ask_form(kDialog, &start_address_source, &end_address_source,
                          &start_address_target, &end_address_target)) {
        return false;
      }
    }
    return DiffAddressRange(start_address_source, end_address_source,
                            start_address_target, end_address_target);
  } catch (const std::exception& message) {
    LOG(INFO) << "Error while diffing: " << message.what();
    warning("Error while diffing: %s\n", message.what());
  } catch (...) {
    LOG(INFO) << "Unknown error while diffing.";
    warning("Unknown error while diffing.");
  }
  return false;
}

bool DoPortComments() {
  if (!g_results) {
    vinfo("Please perform a diff first", 0);
    return false;
  }

  try {
    static const char kDialog[] =
        "STARTITEM 0\n"
        "Import Symbols and Comments\n"
        "Address range (default: all)\n\n"
        "  <Start address (source):$::16::>\n"
        "  <End address (source):$::16::>\n"
        "  <Start address (target):$::16::>\n"
        "  <End address (target):$::16::>\n\n"
        "Minimum confidence required (default: none)\n\n"
        "  <confidence:A::16::>\n\n"
        "Minimum similarity required (default: none)\n\n"
        "  <similarity:A::16::>\n\n";

    // Default to full address range.
    ea_t start_address_source = 0;
    ea_t end_address_source = std::numeric_limits<ea_t>::max() - 1;
    ea_t start_address_target = 0;
    ea_t end_address_target = std::numeric_limits<ea_t>::max() - 1;
    char buffer1[MAXSTR];
    memset(buffer1, 0, MAXSTR);
    buffer1[0] = '0';
    char buffer2[MAXSTR];
    memset(buffer2, 0, MAXSTR);
    buffer2[0] = '0';
    if (!ask_form(kDialog, &start_address_source, &end_address_source,
                        &start_address_target, &end_address_target, buffer1,
                        buffer2)) {
      return false;
    }

    Timer<> timer;
    const double min_confidence = std::stod(buffer1);
    const double min_similarity = std::stod(buffer2);
    g_results->PortComments(start_address_source, end_address_source,
                            start_address_target, end_address_target,
                            min_confidence, min_similarity);
    refresh_chooser("Matched Functions");
    refresh_chooser("Primary Unmatched");
    LOG(INFO) << absl::StrCat(HumanReadableDuration(timer.elapsed()),
                              " for comment porting");
    return true;
  } catch (const std::exception& message) {
    LOG(INFO) << "Error while porting comments: " << message.what();
    warning("Error while porting comments: %s\n", message.what());
  } catch (...) {
    LOG(INFO) << "Unknown error while porting comments.";
    warning("Unknown error while porting comments.");
  }
  return false;
}

error_t idaapi IdcBinDiffDatabase(idc_value_t* argument, idc_value_t*) {
  if (argument[0].vtype != VT_STR || argument[1].vtype != VT_STR) {
    LOG(INFO) << "Error (BinDiffDatabase): required arguments are missing or "
                 "have the wrong type.";
    LOG(INFO) << "Usage:";
    LOG(INFO) << "  BinDiffDatabase('secondary_idb', 'results_file')";
    return -1;
  }
  return DoDiffDatabase(/*filtered=*/false) ? eOk : -1;
}
constexpr char kBinDiffDatabaseArgs[] = {VT_STR, VT_STR};
constexpr ext_idcfunc_t kBinDiffDatabaseIdcFunc = {
    "BinDiffDatabase", IdcBinDiffDatabase, kBinDiffDatabaseArgs, nullptr, 0,
    EXTFUN_BASE};

bool WriteResults(const string& path) {
  if (FileExists(path)) {
    if (ask_yn(ASKBTN_YES, "File\n'%s'\nalready exists - overwrite?",
               path.c_str()) != 1) {
      return false;
    }
  }

  WaitBox wait_box("Writing results...");
  Timer<> timer;
  LOG(INFO) << "Writing results...";
  string export1(g_results->call_graph1_.GetFilePath());
  string export2(g_results->call_graph2_.GetFilePath());
  auto temp_dir_or = GetOrCreateTempDirectory("BinDiff");
  if (!temp_dir_or.ok()) {
    return false;
  }
  const string temp_dir = std::move(temp_dir_or).ValueOrDie();
  const string out_dir(Dirname(path));

  if (!g_results->IsInComplete()) {
    DatabaseWriter writer(path);
    g_results->Write(&writer);
  } else {
    // results are incomplete (have been loaded)
    // copy original result file to temp dir first, so we can overwrite the
    // original if required
    std::remove(JoinPath(temp_dir, "input.BinDiff").c_str());
    CopyFile(g_results->input_filename_, JoinPath(temp_dir, "input.BinDiff"));
    {
      SqliteDatabase database(JoinPath(temp_dir, "input.BinDiff").c_str());
      DatabaseTransmuter writer(database, g_results->fixed_point_infos_);
      g_results->Write(&writer);
    }
    std::remove(path.c_str());
    CopyFile(JoinPath(temp_dir, "input.BinDiff"), path);
    std::remove(JoinPath(temp_dir, "input.BinDiff").c_str());
  }
  string new_export1(JoinPath(out_dir, Basename(export1)));
  if (export1 != new_export1) {
    std::remove(new_export1.c_str());
    CopyFile(export1, new_export1);
  }
  string new_export2(JoinPath(out_dir, Basename(export2)));
  if (export2 != new_export2) {
    std::remove(new_export2.c_str());
    CopyFile(export2, new_export2);
  }

  LOG(INFO) << absl::StrCat("done (", HumanReadableDuration(timer.elapsed()),
                            ")");
  return true;
}

bool DoSaveResultsLog() {
  if (!g_results) {
    vinfo("Please perform a diff first", 0);
    return false;
  }
  if (g_results->IsInComplete()) {
    vinfo("Saving to log is not supported for loaded results", 0);
    return false;
  }

  const string default_filename(
      g_results->call_graph1_.GetFilename() + "_vs_" +
      g_results->call_graph2_.GetFilename() + ".results");
  const char* filename = ask_file(
      /*for_saving=*/true, default_filename.c_str(), "%s",
      absl::StrCat("FILDER BinDiff Result Log files|*.results|All files",
                   kAllFilesFilter, "\nSave Log As")
          .c_str());
  if (!filename) {
    return false;
  }

  if (FileExists(filename) &&
      (ask_yn(ASKBTN_YES, "File exists - overwrite?") != 1)) {
    return false;
  }

  WaitBox wait_box("Writing results...");
  Timer<> timer;
  LOG(INFO) << "Writing to log...";
  ResultsLogWriter writer(filename);
  g_results->Write(&writer);
  LOG(INFO) << absl::StrCat("done (", HumanReadableDuration(timer.elapsed()),
                            ")");
  return true;
}

bool DoSaveResultsDebug() {
  if (!g_results) {
    vinfo("Please perform a diff first", 0);
    return false;
  }

  const string default_filename(
      g_results->call_graph1_.GetFilename() + "_vs_" +
      g_results->call_graph2_.GetFilename() + ".truth");
  const char* filename = ask_file(
      /*for_saving=*/true, default_filename.c_str(), "%s",
      absl::StrCat("FILTER Groundtruth files|*.truth|All files|",
                   kAllFilesFilter, "\nSave Groundtruth As")
          .c_str());
  if (!filename) {
    return false;
  }

  if (FileExists(filename) &&
      (ask_yn(ASKBTN_YES, "File exists - overwrite?") != 1)) {
    return false;
  }

  WaitBox wait_box("Writing results...");
  Timer<> timer;
  LOG(INFO) << "Writing to debug ground truth file...";
  GroundtruthWriter writer(filename, g_results->fixed_point_infos_,
                           g_results->flow_graph_infos1_,
                           g_results->flow_graph_infos2_);
  g_results->Write(&writer);
  LOG(INFO) << absl::StrCat("done (", HumanReadableDuration(timer.elapsed()),
                            ")");

  return true;
}

bool DoSaveResults() {
  if (!g_results) {
    vinfo("Please perform a diff first.", 0);
    return false;
  }

  try {
    // TODO(soerenme) figure out how to use m_InputFileName from g_results if
    // we have loaded results.
    // It seems the filechooser will only ever use the directory part of the
    // default filename _or_ the filename part. I want both!
    // (Hex-Rays has confirmed this to be a bug and promised to fix it in 6.2)
    string default_filename(
        g_results->call_graph1_.GetFilename() + "_vs_" +
        g_results->call_graph2_.GetFilename() + ".BinDiff");
    const char* filename = ask_file(
        /*for_saving=*/true, default_filename.c_str(), "%s",
        absl::StrCat("FILTER BinDiff Result files|*.BinDiff|All files",
                     kAllFilesFilter, "\nSave Results As")
            .c_str());
    if (!filename) {
      return false;
    }

    WriteResults(filename);

    return true;
  } catch (...) {
    LOG(INFO) << "Error writing results.";
    warning("Error writing results.\n");
  }
  return false;
}

bool DoLoadResults() {
  try {
    if (g_results && g_results->IsDirty()) {
      const int answer = ask_yn(
          ASKBTN_YES,
          "Current diff results have not been saved - save before closing?");
      if (answer == 1) {  // yes
        DoSaveResults();
      } else if (answer == -1) {  // cancel
        return false;
      }
    }

    const char* filename = ask_file(
        /*for_saving=*/false, "*.BinDiff", "%s",
        absl::StrCat("FILTER BinDiff Result files|*.BinDiff|All files|",
                     kAllFilesFilter, "\nLoad Results")
            .c_str());
    if (!filename) {
      return false;
    }

    string path(Dirname(filename));

    LOG(INFO) << "Loading results...";
    WaitBox wait_box("Loading results...");
    Timer<> timer;

    delete g_results;
    g_results = new Results();

    auto temp_dir_or = GetOrCreateTempDirectory("BinDiff");
    if (!temp_dir_or.ok()) {
      return false;
    }
    const string temp_dir = std::move(temp_dir_or).ValueOrDie();

    SqliteDatabase database(filename);
    DatabaseReader reader(database, filename, temp_dir);
    g_results->Read(&reader);

    auto sha256_or = GetInputFileSha256();
    auto status = sha256_or.status();
    string hash;
    if (status.ok()) {
      hash = std::move(sha256_or).ValueOrDie();
    } else {
      auto md5_or = GetInputFileMd5();
      status = md5_or.status();
      if (status.ok()) {
        hash = std::move(md5_or).ValueOrDie();
      }
    }
    if (hash.empty()) {
      throw std::runtime_error{status.error_message()};
    }
    if (hash != absl::AsciiStrToLower(g_results->call_graph1_.GetExeHash())) {
      const string message = absl::StrCat(
          "Error: currently loaded IDBs input file hash differs from "
          "result file primary graph. Please load IDB for: ",
          g_results->call_graph1_.GetExeFilename());
      LOG(INFO) << message;
      throw std::runtime_error(message);
    }

    ShowResults(g_results);

    LOG(INFO) << absl::StrCat("done (", HumanReadableDuration(timer.elapsed()),
                              ")");
    return true;
  } catch (const std::exception& message) {
    LOG(INFO) << "Error loading results: " << message.what();
    warning("Error loading results: %s\n", message.what());
  } catch (...) {
    LOG(INFO) << "Error loading results.";
    warning("Error loading results.");
  }
  delete g_results;
  g_results = 0;
  return false;
}

void idaapi ButtonDiffDatabaseCallback(int button_code,
                                       form_actions_t& actions) {
  if (DoDiffDatabase(/*filtered=*/false)) {
    actions.close(/*close_normally=*/1);
  }
}

void idaapi ButtonDiffDatabaseFilteredCallback(int button_code,
                                               form_actions_t& actions) {
  if (DoDiffDatabase(/*filtered=*/true)) {
    actions.close(/*close_normally=*/1);
  }
}

void idaapi ButtonRediffDatabaseCallback(int button_code,
                                         form_actions_t& actions) {
  if (DoRediffDatabase()) {
    actions.close(/*close_normally=*/1);
  }
}

void idaapi ButtonLoadResultsCallback(int button_code,
                                      form_actions_t& actions) {
  if (DoLoadResults()) {
    actions.close(/*close_normally=*/1);
  }
}

void idaapi ButtonSaveResultsCallback(int button_code,
                                      form_actions_t& actions) {
  if (DoSaveResults()) {
    actions.close(/*close_normally=*/1);
  }
}

void idaapi ButtonSaveResultsLogCallback(int button_code,
                                         form_actions_t& actions) {
  if (DoSaveResultsLog()) {
    actions.close(/*close_normally=*/1);
  }
}

void idaapi ButtonSaveResultsDebugCallback(int button_code,
                                           form_actions_t& actions) {
  if (DoSaveResultsDebug()) {
    actions.close(/*close_normally=*/1);
  }
}

void idaapi ButtonPortCommentsCallback(int button_code,
                                       form_actions_t& actions) {
  if (DoPortComments()) {
    actions.close(/*close_normally=*/1);
  }
}

class DiffDatabaseAction : public ActionHandler<DiffDatabaseAction> {
  int idaapi activate(action_activation_ctx_t*) override {
    return DoDiffDatabase(/*filtered=*/false);  // Refresh windows on success
  }
};

class LoadResultsAction : public ActionHandler<LoadResultsAction> {
  int idaapi activate(action_activation_ctx_t*) override {
    return DoLoadResults();  // Refresh if user did not cancel
  }
};

class SaveResultsAction : public ActionHandler<SaveResultsAction> {
  int idaapi activate(action_activation_ctx_t*) override {
    return DoSaveResults();  // Refresh if user did not cancel
  }
};

class PortCommentsAction : public ActionHandler<PortCommentsAction> {
  int idaapi activate(action_activation_ctx_t* context) override {
    int num_selected = context->chooser_selection.size();
    if (g_results && num_selected > 0) {
      try {
        const bool as_external_lib =
            absl::string_view{context->action} ==
            MatchedFunctionsChooser::kImportSymbolsCommentsExternalAction;
        // TODO(cblichmann): Efficient bulk actions in Results class
        for (const auto& index : context->chooser_selection) {
          g_results->PortComments(index, as_external_lib);
        }
        return 1;
      } catch (const std::exception& message) {
        LOG(INFO) << "Error: " << message.what();
        warning("Error: %s\n", message.what());
        return 0;
      } catch (...) {
        LOG(INFO) << "Unknown error while porting comments";
        warning("Unknown error while porting comments\n");
        return 0;
      }
    }
    // Not called from the chooser, display dialog.
    return DoPortComments();  // Refresh if user did not cancel
  }
};

class ShowMatchedAction : public ActionHandler<ShowMatchedAction> {
  int idaapi activate(action_activation_ctx_t*) override {
    ShowResults(g_results, kResultsShowMatched);
    return 0;
  }
};

class ShowStatisticsAction : public ActionHandler<ShowStatisticsAction> {
  int idaapi activate(action_activation_ctx_t*) override {
    ShowResults(g_results, kResultsShowStatistics);
    return 0;
  }
};

class ShowPrimaryUnmatchedAction
    : public ActionHandler<ShowPrimaryUnmatchedAction> {
  int idaapi activate(action_activation_ctx_t*) override {
    ShowResults(g_results, kResultsShowPrimaryUnmatched);
    return 0;
  }
};

class ShowSecondaryUnmatchedAction
    : public ActionHandler<ShowSecondaryUnmatchedAction> {
  int idaapi activate(action_activation_ctx_t*) override {
    ShowResults(g_results, kResultsShowSecondaryUnmatched);
    return 0;
  }
};

class DeleteMatchAction : public ActionHandler<DeleteMatchAction> {
  int idaapi activate(action_activation_ctx_t* context) override {
    if (!g_results) {
      return 0;
    }
    try {
      // TODO(cblichmann): Efficient bulk actions in Results class
      for (const auto& index : context->chooser_selection) {
        g_results->DeleteMatch(index);
      }
      return 1;
    } catch (const std::exception& message) {
      LOG(INFO) << "Error: " << message.what();
      warning("Error: %s\n", message.what());
      return 0;
    } catch (...) {
      LOG(INFO) << "Unknown error while deleting match";
      warning("Unknown error while deleting match\n");
      return 0;
    }
  }
};

class ViewFlowGraphsAction : public ActionHandler<ViewFlowGraphsAction> {
  int idaapi activate(action_activation_ctx_t* context) override {
    const auto& chooser_selection = context->chooser_selection;
    if (chooser_selection.empty()) {
      return 0;
    }
    DoVisualDiff(chooser_selection.front(),
                 /*call_graph_diff=*/false);
    return 1;
  }
};

class ConfirmMatchAction : public ActionHandler<ConfirmMatchAction> {
  int idaapi activate(action_activation_ctx_t* context) override {
        LOG(INFO) << " " << context->action;
    if (!g_results) {
      return 0;
    }
    try {
      // TODO(cblichmann): Efficient bulk actions in Results class
      for (const auto& index : context->chooser_selection) {
        LOG(INFO) << " index: " << index;
        g_results->ConfirmMatch(index);
      }
      return 1;
    } catch (const std::exception& message) {
      LOG(INFO) << "Error: " << message.what();
      warning("Error: %s\n", message.what());
      return 0;
    } catch (...) {
      LOG(INFO) << "Unknown error while confirming match";
      warning("Unknown error while confirming match\n");
      return 0;
    }
  }
};

void InitConfig() {
  const string config_filename("bindiff_core.xml");

  const string user_path = absl::StrCat(
      GetDirectory(PATH_APPDATA, "BinDiff", /*create=*/true), config_filename);
  const string common_path = absl::StrCat(
      GetDirectory(PATH_COMMONAPPDATA, "BinDiff", /*create=*/false),
      config_filename);

  bool have_user_config = false;
  bool have_common_config = false;
  std::unique_ptr<XmlConfig> user_config;
  std::unique_ptr<XmlConfig> common_config;

  // Try to read user's local config
  try {
    user_config = XmlConfig::LoadFromFile(user_path);
    user_config->SetSaveFileName("");  // Prevent saving in destructor
    have_user_config = true;
  } catch (const std::runtime_error&) {
    have_user_config = false;
  }

  // Try to read machine config
  try {
    common_config = XmlConfig::LoadFromFile(common_path);
    common_config->SetSaveFileName("");  // Prevent saving in destructor
    have_common_config = true;
  } catch (const std::runtime_error&) {
    have_common_config = false;
  }

  bool use_common_config = false;
  if (have_user_config && have_common_config) {
    use_common_config = user_config->ReadInt("/BinDiff/@configVersion", 0) <
                        common_config->ReadInt("/BinDiff/@configVersion", 0);
  } else if (have_user_config) {
    use_common_config = false;
  } else if (have_common_config) {
    use_common_config = true;
  }

  if (use_common_config) {
    XmlConfig::SetDefaultFilename(common_path);
    g_config = common_config.release();
    std::remove(user_path.c_str());
    g_config->SetSaveFileName(user_path);
  } else {
    XmlConfig::SetDefaultFilename(user_path);
    g_config = user_config.release();
  }
}

void InitActions() {
  const int bindiff_icon_id =
      load_custom_icon(kBinDiffIcon.data(), kBinDiffIcon.size(), "png");
  register_action(DiffDatabaseAction::MakeActionDesc(
      "bindiff:diff_database", "Bin~D~iff...", "SHIFT-D",
      /*tooltip=*/nullptr, bindiff_icon_id));

  register_action(LoadResultsAction::MakeActionDesc(
      "bindiff:load_results", "~B~inDiff results...", /*shortcut=*/"",
      /*tooltip=*/nullptr, /*icon=*/-1));
  register_action(SaveResultsAction::MakeActionDesc(
      "bindiff:save_results", "Save ~B~inDiff results...", /*shortcut=*/"",
      /*tooltip=*/nullptr, /*icon=*/-1));

  register_action(PortCommentsAction::MakeActionDesc(
      "bindiff:port_comments", "Im~p~ort symbols/comments...",
      /*shortcut=*/"", /*tooltip=*/nullptr, /*icon=*/-1));

  register_action(ShowMatchedAction::MakeActionDesc(
      "bindiff:show_matched", "~M~atched functions", /*shortcut=*/"",
      /*tooltip=*/nullptr, /*icon=*/-1));
  register_action(ShowStatisticsAction::MakeActionDesc(
      "bindiff:show_statistics", "S~t~atistics", /*shortcut=*/"",
      /*tooltip=*/nullptr, /*icon=*/-1));
  register_action(ShowPrimaryUnmatchedAction::MakeActionDesc(
      "bindiff:show_primary_unmatched", "~P~rimary unmatched", /*shortcut=*/"",
      /*tooltip=*/nullptr, /*icon=*/-1));
  register_action(ShowSecondaryUnmatchedAction::MakeActionDesc(
      "bindiff:show_secondary_unmatched", "~S~econdary unmatched",
      /*shortcut=*/"", /*tooltip=*/nullptr, /*icon=*/-1));

  // Matched Functions chooser
  register_action(DeleteMatchAction::MakeActionDesc(
      MatchedFunctionsChooser::kDeleteAction, "~D~elete match", "",
      /*tooltip=*/nullptr, /*icon=*/-1));
  register_action(ViewFlowGraphsAction::MakeActionDesc(
      MatchedFunctionsChooser::kViewFlowGraphsAction, "View flow graphs", "",
      /*tooltip=*/nullptr, /*icon=*/-1));
  register_action(PortCommentsAction::MakeActionDesc(
      MatchedFunctionsChooser::kImportSymbolsCommentsAction,
      "Im~p~ort symbols/comments", "",
      /*tooltip=*/nullptr, /*icon=*/-1));
  register_action(PortCommentsAction::MakeActionDesc(
      MatchedFunctionsChooser::kImportSymbolsCommentsExternalAction,
      "Import symbols/comments as ~e~xternal library", "",
      /*tooltip=*/nullptr, /*icon=*/-1));
  register_action(ConfirmMatchAction::MakeActionDesc(
      MatchedFunctionsChooser::kConfirmMatchAction, "~C~onfirm match", "",
      /*tooltip=*/nullptr, /*icon=*/-1));
}

void InitMenus() {
  attach_action_to_menu("File/ProduceFile", "bindiff:diff_database",
                        SETMENU_APP);
  attach_action_to_menu("File/LoadFile/AdditionalBinaryFile",
                        "bindiff:load_results", SETMENU_APP);
  attach_action_to_menu("File/ProduceFile/CreateCallgraphGDL",
                        "bindiff:save_results", SETMENU_APP);

  attach_action_to_menu("Edit/Comments/InsertPredefinedComment",
                        "bindiff:port_comments", SETMENU_APP);

  attach_action_to_menu("View/BinDiff/", "bindiff:show_matched",
                        SETMENU_FIRST);
  attach_action_to_menu("View/BinDiff/MatchedFunctions",
                        "bindiff:show_statistics", SETMENU_APP);
  attach_action_to_menu("View/BinDiff/Statistics",
                        "bindiff:show_primary_unmatched", SETMENU_APP);
  attach_action_to_menu("View/BinDiff/PrimaryUnmatched",
                        "bindiff:show_secondary_unmatched", SETMENU_APP);
}

void TermMenus() {
  detach_action_from_menu("File/BinDiff", "bindiff:diff_database");
  detach_action_from_menu("File/LoadFile/BinDiffResults",
                          "bindiff:load_results");
  detach_action_from_menu("File/ProduceFile/SaveBinDiffResults",
                          "bindiff:save_results");
  detach_action_from_menu("Edit/Comments/ImportSymbolsAndComments",
                          "bindiff:port_comments");
  detach_action_from_menu("View/BinDiff/MatchedFunctions",
                          "bindiff:show_matched");
  detach_action_from_menu("View/BinDiff/Statistics", "bindiff:show_statistics");
  detach_action_from_menu("View/BinDiff/PrimaryUnmatched",
                          "bindiff:show_primary_unmatched");
  detach_action_from_menu("View/BinDiff/SecondaryUnmatched",
                          "bindiff:show_secondary_unmatched");
}

int idaapi PluginInit() {
  g_alsologtostderr =
      absl::AsciiStrToUpper(GetArgument("AlsoLogToStdErr")) == "TRUE";
  if (!InitLogging(LoggingOptions{}
                       .set_alsologtostderr(g_alsologtostderr)
                       .set_log_filename(GetArgument("LogFile")))) {
    LOG(INFO) << "Error initializing logging, skipping BinDiff plugin";
    return PLUGIN_SKIP;
  }

  LOG(INFO) << string(kProgramVersion) << " (" << __DATE__
#ifndef NDEBUG
            << ", debug build"
#endif
            << "), " << kCopyright;

  addon_info_t addon_info;
  addon_info.id = "com.google.bindiff";
  addon_info.name = kName;
  addon_info.producer = "Google";
  addon_info.version = BINDIFF_MAJOR "." BINDIFF_MINOR "." BINDIFF_PATCH;
  addon_info.url = "https://zynamics.com/bindiff.html";
  addon_info.freeform = kCopyright;
  register_addon(&addon_info);

  if (!hook_to_notification_point(HT_IDP, ProcessorHook,
                                  /*user_data=*/nullptr) ||
      !hook_to_notification_point(HT_IDB, IdbHook, /*user_data=*/nullptr) ||
      !hook_to_notification_point(HT_UI, UiHook, /*user_data=*/nullptr)) {
    LOG(INFO) << "Internal error: hook_to_notification_point() failed";
    return PLUGIN_SKIP;
  }

  if (!add_idc_func(kBinDiffDatabaseIdcFunc)) {
    LOG(INFO) << "Error registering IDC extension, skipping BinDiff plugin";
    return PLUGIN_SKIP;
  }

  try {
    InitConfig();
  } catch (const std::runtime_error&) {
    LOG(INFO)
        << "Error: Could not load configuration file, skipping BinDiff plugin.";
    return PLUGIN_SKIP;
  }
  InitActions();
  InitMenus();
  g_init_done = true;

  return PLUGIN_KEEP;
}

void idaapi PluginTerminate() {
  unhook_from_notification_point(HT_UI, UiHook, /*user_data=*/nullptr);
  unhook_from_notification_point(HT_IDB, IdbHook, /*user_data=*/nullptr);
  unhook_from_notification_point(HT_IDP, ProcessorHook, /*user_data=*/nullptr);

  if (g_init_done) {
    TermMenus();
    SaveAndDiscardResults();
  }

  delete g_config;
  g_config = nullptr;

  ShutdownLogging();
}

bool idaapi PluginRun(size_t /* arg */) {
  static const string kDialogBase = absl::StrCat(
      "STARTITEM 0\n"
      "BUTTON YES Close\n"  // This is actually the OK button
      "BUTTON CANCEL NONE\n"
      "HELP\n"
      "'Diff Database...' diff the currently open IDB against another one "
      "chosen via a file chooser dialog. Please note that the secondary IDB "
      "file must be readable for the BinDiff plugin, i.e. it must not be "
      "opened in another instance of IDA.\n"
      "\n"
      "'Diff Database Filtered...' diff specific address ranges of the "
      "selected databases. You must manually specify a section of the primary "
      "IDB to compare against a section of the secondary IDB. This is useful "
      "for comparing only the non library parts of two executables.\n"
      "\n"
      "'Load Results...' load a previously saved diff result. The primary IDB "
      "used in that diff must already be open in IDA.\n"
      "\n",
      kProgramVersion, "\n",
      kCopyright, "\n",
      "ENDHELP\n",
      kProgramVersion, "\n",
      "\n"
      "<~D~iff Database...:B:1:30::>\n"
      "<D~i~ff Database Filtered...:B:1:30::>\n\n"
      "<L~o~ad Results...:B:1:30::>\n\n"
      );

  static const string kDialogResultsAvailable = absl::StrCat(
      "STARTITEM 0\n"
      "BUTTON YES Close\n"  // This is actually the OK button
      "BUTTON CANCEL NONE\n"
      "HELP\n"
      "'Diff Database...' diff the currently open IDB against another one "
      "chosen via a file chooser dialog. Please note that the secondary IDB "
      "file must be readable for the BinDiff plugin, i.e. it must not be "
      "opened in another instance of IDA.\n"
      "\n"
      "'Diff Database Filtered...' diff specific address ranges of the "
      "selected databases. You must manually specify a section of the primary "
      "IDB to compare against a section of the secondary IDB. This is useful "
      "for comparing only the non library parts of two executables.\n"
      "\n"
      "'Diff Database Incrementally' keep manually confirmed matches (blue "
      "matches with algorithm = 'manual') in the current result and re-match "
      "all others. Thus allowing a partially automated workflow of "
      "continuously improving the diff results.\n"
      "\n"
      "'Load Results...' load a previously saved diff result. The primary IDB "
      "used in that diff must already be open in IDA.\n"
      "\n"
      "'Save Results...' save the current BinDiff matching to a .BinDiff "
      "result file.\n"
      "\n"
      "'Import Symbols and Comments...' copy function names, symbols and "
      "comments from the secondary IDB into the primary IDB for all matched "
      "functions. It is possible to specify a filter so only data for matches "
      "meeting a certain quality threshold or in a certain address range will "
      "be ported.\n"
      "\n",
      kProgramVersion, "\n",
      kCopyright, "\n",
      "ENDHELP\n",
      kProgramVersion, "\n",
      "\n"
      "<~D~iff Database...:B:1:30::>\n"
      "<D~i~ff Database Filtered...:B:1:30::>\n"
      "<Diff Database Incrementally:B:1:30::>\n\n"
      "<L~o~ad Results...:B:1:30::>\n"
      "<~S~ave Results...:B:1:30::>\n"
#ifdef _DEBUG
      "<Save ~G~round Truth results...:B:1:30::>\n"
      "<Save Results ~L~og...:B:1:30::>\n"
#endif
      "\n<Im~p~ort Symbols and Comments...:B:1:30::>\n\n");

  addon_info_t addon_info;
  if (!get_addon_info("com.google.binexport", &addon_info)) {
    warning("Required BinExport plugin is missing.");
    return false;
  }

  if (!EnsureIdb()) {
    return false;
  }

  if (g_results) {
    // We may have to unload a previous result if the input IDB has changed in
    // the meantime
    auto sha256_or = GetInputFileSha256();
    if (!sha256_or.ok()) {
      throw std::runtime_error{sha256_or.status().error_message()};
    }
    if (sha256_or.ValueOrDie() !=
        absl::AsciiStrToLower(g_results->call_graph1_.GetExeHash())) {
      warning("Discarding current results since the input IDB has changed.");

      delete g_results;
      g_results = 0;

      close_chooser("Matched Functions");
      close_chooser("Primary Unmatched");
      close_chooser("Secondary Unmatched");
      close_chooser("Statistics");
    }
  }

  if (!g_results) {
    ask_form(kDialogBase.c_str(), ButtonDiffDatabaseCallback,
             ButtonDiffDatabaseFilteredCallback, ButtonLoadResultsCallback);
  } else {
    ask_form(kDialogResultsAvailable.c_str(), ButtonDiffDatabaseCallback,
             ButtonDiffDatabaseFilteredCallback, ButtonRediffDatabaseCallback,
             ButtonLoadResultsCallback, ButtonSaveResultsCallback,
#ifdef _DEBUG
             ButtonSaveResultsDebugCallback, ButtonSaveResultsLogCallback,
#endif
             ButtonPortCommentsCallback);
  }
  return true;
}

}  // namespace
}  // namespace bindiff
}  // namespace security

plugin_t PLUGIN = {
    IDP_INTERFACE_VERSION,
    PLUGIN_FIX,                          // Plugin flags
    security::bindiff::PluginInit,       // Initialize
    security::bindiff::PluginTerminate,  // Terminate
    security::bindiff::PluginRun,        // Invoke plugin
    security::bindiff::kComment,         // Statusline text
    nullptr,                    // Multiline help about the plugin, unused
    security::bindiff::kName,   // The preferred short name of the plugin
    security::bindiff::kHotKey  // The preferred hotkey to run the plugin
};
