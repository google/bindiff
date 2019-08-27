#include "third_party/zynamics/bindiff/ida/main_plugin.h"

#include <cstdio>
#include <fstream>
#include <limits>
#include <memory>
#include <stdexcept>
#include <thread>  // NOLINT(build/c++11)

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
#include "third_party/absl/strings/string_view.h"
#include "third_party/absl/time/time.h"
#include "third_party/absl/types/span.h"
#include "third_party/zynamics/bindiff/call_graph_match.h"
#include "third_party/zynamics/bindiff/change_classifier.h"
#include "third_party/zynamics/bindiff/config.h"
#include "third_party/zynamics/bindiff/database_writer.h"
#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/bindiff/flow_graph_match.h"
#include "third_party/zynamics/bindiff/groundtruth_writer.h"
#include "third_party/zynamics/bindiff/ida/bindiff_icon.h"
#include "third_party/zynamics/bindiff/ida/matched_functions_chooser.h"
#include "third_party/zynamics/bindiff/ida/results.h"
#include "third_party/zynamics/bindiff/ida/statistics_chooser.h"
#include "third_party/zynamics/bindiff/ida/ui.h"
#include "third_party/zynamics/bindiff/ida/unmatched_functions_chooser.h"
#include "third_party/zynamics/bindiff/ida/visual_diff.h"
#include "third_party/zynamics/bindiff/idb_export.h"
#include "third_party/zynamics/bindiff/log_writer.h"
#include "third_party/zynamics/bindiff/match_context.h"
#include "third_party/zynamics/bindiff/utility.h"
#include "third_party/zynamics/bindiff/version.h"
#include "third_party/zynamics/binexport/ida/digest.h"
#include "third_party/zynamics/binexport/ida/log.h"
#include "third_party/zynamics/binexport/ida/ui.h"
#include "third_party/zynamics/binexport/types.h"
#include "third_party/zynamics/binexport/util/canonical_errors.h"
#include "third_party/zynamics/binexport/util/filesystem.h"
#include "third_party/zynamics/binexport/util/format.h"
#include "third_party/zynamics/binexport/util/status.h"
#include "third_party/zynamics/binexport/util/status_macros.h"
#include "third_party/zynamics/binexport/util/statusor.h"
#include "third_party/zynamics/binexport/util/timer.h"

namespace security {
namespace bindiff {

using binexport::FormatAddress;
using binexport::GetInputFileMd5;
using binexport::GetInputFileSha256;
using binexport::HumanReadableDuration;

constexpr char Plugin::kComment[];
constexpr char Plugin::kHotKey[];

std::string GetArgument(const char* name) {
  const char* option =
      get_plugin_options(absl::StrCat("BinDiff", name).c_str());
  return option ? option : "";
}

bool DoSaveResults();

std::string FindFile(const std::string& path, const std::string& extension) {
  std::vector<std::string> entries;
  GetDirectoryEntries(path, &entries);
  for (const auto& entry : entries) {
    if (GetFileExtension(entry) == extension) {
      return JoinPath(path, Basename(entry));
    }
  }
  return "";
}

bool CheckHaveIdbWithMessage() {
  bool result = strlen(get_path(PATH_TYPE_IDB)) > 0;
  if (!result) {
    info("AUTOHIDE NONE\nPlease open an IDB first.");
  }
  return result;
}

bool CheckHaveResultsWithMessage() {
  if (!Plugin::instance()->results()) {
    info("AUTOHIDE NONE\nPlease perform a diff first.");
    return false;
  }
  return true;
}

bool ExportIdbs() {
  if (!CheckHaveIdbWithMessage()) {
    return false;
  }

  auto temp_dir_or = GetOrCreateTempDirectory("BinDiff");
  if (!temp_dir_or.ok()) {
    return false;
  }
  const std::string temp_dir = std::move(temp_dir_or).ValueOrDie();

  const char* secondary_idb = ask_file(
      /*for_saving=*/false, "*.idb;*.i64", "%s",
      absl::StrCat("FILTER IDA Databases|*.idb;*.i64|All files|",
                   kAllFilesFilter, "\nSelect Database")
          .c_str());
  if (!secondary_idb) {
    return false;
  }

  const std::string primary_idb_path(get_path(PATH_TYPE_IDB));
  std::string secondary_idb_path(secondary_idb);
  if (primary_idb_path == secondary_idb_path) {
    throw std::runtime_error(
        "You cannot open the same IDB file twice. Please copy and rename one if"
        " you want to diff against itself.");
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

  LOG(INFO) << "Diffing " << Basename(primary_idb_path) << " vs "
            << Basename(secondary_idb_path);
  WaitBox wait_box("Exporting idbs...");

  const std::string primary_temp_dir = JoinPath(temp_dir, "primary");
  RemoveAll(primary_temp_dir).IgnoreError();
  not_absl::Status status = CreateDirectories(primary_temp_dir);
  if (!status.ok()) {
    throw std::runtime_error{std::string(status.message())};
  }

  const std::string secondary_temp_dir = JoinPath(temp_dir, "secondary");
  RemoveAll(secondary_temp_dir).IgnoreError();
  status = CreateDirectories(secondary_temp_dir);
  if (!status.ok()) {
    throw std::runtime_error{std::string(status.message())};
  }

  {
    const auto* config = GetConfig();
    auto exporter_or = IdbExporter::Create(
        IdbExporter::Options{}
            .set_export_dir(secondary_temp_dir)
            .set_ida_dir(idadir(0))
            .set_ida_exe(config->ReadString("/BinDiff/Ida/@executable", ""))
            .set_ida_exe64(config->ReadString("/BinDiff/Ida/@executable64", ""))
            .set_alsologtostderr(Plugin::instance()->alsologtostderr()));
    if (!exporter_or.ok()) {
      throw std::runtime_error{
          absl::StrCat("Export of the current database failed: ",
                       exporter_or.status().message())};
    }
    auto exporter = std::move(exporter_or).ValueOrDie();
    exporter->AddDatabase(secondary_idb_path);

    std::thread export_thread(
        [&status, &exporter]() { status = exporter->Export(); });

    qstring errbuf;
    idc_value_t arg = primary_temp_dir.c_str();
    if (!call_idc_func(
            /*result=*/nullptr, "BinExportBinary", &arg,
            /*argsnum=*/1, &errbuf, /*resolver=*/nullptr)) {
      export_thread.detach();
      throw std::runtime_error(absl::StrCat(
          "Export of the primary database failed: ", errbuf.c_str()));
    }

    export_thread.join();
    if (!status.ok()) {
      throw std::runtime_error{absl::StrCat(
          "Export of the secondary database failed: ", status.message())};
    }
  }

  return true;
}

void Plugin::VisualDiff(uint32_t index, bool call_graph_diff) {
  if (!results_) {
    return;
  }
  try {
    std::string message;
    const bool result =
        !call_graph_diff
            ? results_->PrepareVisualDiff(index, &message)
            : results_->PrepareVisualCallGraphDiff(index, &message);
    if (!result) {
      return;
    }

    LOG(INFO) << "Sending result to BinDiff GUI...";
    const auto* config = GetConfig();
    SendGuiMessage(
        config->ReadInt("/BinDiff/Gui/@retries", 20),
        config->ReadString("/BinDiff/Gui/@directory",
                           // TODO(cblichmann): Use better defaults
                           "C:\\Program Files\\zynamics\\BinDiff 5.0\\bin"),
        config->ReadString("/BinDiff/Gui/@server", "127.0.0.1"),
        static_cast<uint16_t>(config->ReadInt("/BinDiff/Gui/@port", 2000)),
        message, nullptr);
  } catch (const std::runtime_error& message) {
    LOG(INFO) << "Error while calling BinDiff GUI: " << message.what();
    warning("Error while calling BinDiff GUI: %s\n", message.what());
  } catch (...) {
    LOG(INFO) << "Unknown error while calling BinDiff GUI";
    warning("Unknown error while calling BinDiff GUI\n");
  }
}

void Plugin::DiscardResults() {
  if (!results_) {
    return;
  }

  MatchedFunctionsChooser::Close();
  UnmatchedFunctionsChooserPrimary::Close();
  UnmatchedFunctionsChooserSecondary::Close();
  StatisticsChooser::Close();

  results_.reset();
}

void Plugin::SaveAndDiscardResults() {
  if (results_ && results_->IsDirty()) {
    const auto answer = ask_yn(ASKBTN_YES,
                               "HIDECANCEL\nCurrent diff results have not been"
                               " saved - save before closing?");
    if (answer == ASKBTN_YES) {  // Yes
      DoSaveResults();
    }
  }

  DiscardResults();
}

ssize_t idaapi ProcessorHook(void*, int event_id, va_list /*arguments*/) {
  switch (event_id) {
    case processor_t::ev_term: {
      auto* results = Plugin::instance()->results();
      if (!is_database_flag(DBFL_KILL) && results) {
        results->MarkPortedCommentsInDatabase();
      }
      break;
    }
  }
  return 0;
}

ssize_t idaapi IdbHook(void*, int event_id, va_list /*arguments*/) {
  switch (event_id) {
    case idb_event::closebase:
      // This is not strictly necessary as the "CloseBase" action will be caught
      // in UiHook(). Action names are not guaranteed to be stable, take the
      // last opportunity to ask whether to save the results.
      Plugin::instance()->SaveAndDiscardResults();
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
        auto* results = Plugin::instance()->results();
        // TODO(cblichmann): Merge with SaveAndDiscardResults()
        if (results && results->IsDirty()) {
          const int answer = ask_yn(ASKBTN_YES,
                                    "Current diff results have not been saved -"
                                    " save before closing?");
          if (answer == ASKBTN_YES) {
            // Yes
            DoSaveResults();
          } else if (answer == ASKBTN_CANCEL) {
            // Cancel
            // Return that we handled the command ourselves, this will
            // not even show IDA's close dialog
            return 1;
          }
        }
        // Delete old results if they weren't dirty or if the user
        // saved/discarded them
        Plugin::instance()->DiscardResults();

        // After our "Save results?" confirmation dialog IDA will open its own
        // confirmation. The user can cancel in that dialog. So a "bad" sequence
        // of events is: don't save diff results, but cancel the closing
        // operation in the following IDA dialog. In an ideal world the diff
        // results would be back. As it is we lose the results but at least
        // leave windows/internal data structures in a consistent state.
      }
      break;
    }
    case ui_finish_populating_widget_popup: {
      auto* widget = va_arg(arguments, TWidget*);
      auto* popup_handle = va_arg(arguments, TPopupMenu*);
      attach_action_to_popup(widget, popup_handle, nullptr);  // Add separator
      for (auto& attach :
           {MatchedFunctionsChooser::AttachActionsToPopup,
            UnmatchedFunctionsChooserPrimary::AttachActionsToPopup,
            UnmatchedFunctionsChooserSecondary::AttachActionsToPopup}) {
        if (attach(widget, popup_handle)) {
          break;
        }
      }
      // Add separator before "Font..."
      attach_action_to_popup(widget, popup_handle, nullptr);
      break;
    }
  }
  return 0;
}

void Plugin::ShowResults(Plugin::ResultFlags flags) {
  if (!results_) {
    return;
  }
  results_->CreateIndexedViews();

  // Note: The chooser instances will be owned/freed by IDA.
  if (flags & kResultsShowMatched) {
    (new MatchedFunctionsChooser())->choose();
  }
  if (flags & kResultsShowStatistics) {
    (new StatisticsChooser())->choose();
  }
  if (flags & kResultsShowPrimaryUnmatched) {
    (new UnmatchedFunctionsChooserPrimary())->choose();
  }
  if (flags & kResultsShowSecondaryUnmatched) {
    (new UnmatchedFunctionsChooserSecondary())->choose();
  }
}

bool idaapi MenuItemShowResultsCallback(void* user_data) {
  if (!CheckHaveResultsWithMessage()) {
    return false;
  }
  Plugin::instance()->ShowResults(
      static_cast<Plugin::ResultFlags>(reinterpret_cast<int64_t>(user_data)));
  return true;
}

// Deletes all nodes from callgraph (and corresponding flow graphs) that are
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
  Plugin::instance()->DiscardResults();
  Timer<> timer;
  if (!ExportIdbs()) {
    return false;
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
  Plugin::instance()->set_results(new Results());
  auto* results = Plugin::instance()->results();
  auto temp_dir_or = GetOrCreateTempDirectory("BinDiff");
  if (!temp_dir_or.ok()) {
    return false;
  }
  const auto temp_dir = std::move(temp_dir_or).ValueOrDie();
  const auto filename1(FindFile(JoinPath(temp_dir, "primary"), ".BinExport"));
  const auto filename2(FindFile(JoinPath(temp_dir, "secondary"), ".BinExport"));
  if (filename1.empty() || filename2.empty()) {
    throw std::runtime_error(
        "Export failed. Is the secondary IDB opened in another IDA instance?\n"
        "Please close all other IDA instances and try again.");
  }

  Read(filename1, &results->call_graph1_, &results->flow_graphs1_,
       &results->flow_graph_infos1_, &results->instruction_cache_);
  Read(filename2, &results->call_graph2_, &results->flow_graphs2_,
       &results->flow_graph_infos2_, &results->instruction_cache_);
  MatchingContext context(results->call_graph1_, results->call_graph2_,
                          results->flow_graphs1_, results->flow_graphs2_,
                          results->fixed_points_);
  FilterFunctions(start_address_source, end_address_source,
                  &context.primary_call_graph_, &context.primary_flow_graphs_,
                  &results->flow_graph_infos1_);
  FilterFunctions(
      start_address_target, end_address_target, &context.secondary_call_graph_,
      &context.secondary_flow_graphs_, &results->flow_graph_infos2_);

  const MatchingSteps default_callgraph_steps(GetDefaultMatchingSteps());
  const MatchingStepsFlowGraph default_basicblock_steps(
      GetDefaultMatchingStepsBasicBlock());
  Diff(&context, default_callgraph_steps, default_basicblock_steps);
  LOG(INFO) << absl::StrCat(HumanReadableDuration(timer.elapsed()),
                            " for matching.");

  Plugin::instance()->ShowResults(Plugin::kResultsShowAll);
  results->SetDirty();

  return true;
}

bool DoRediffDatabase() {
  try {
    auto* results = Plugin::instance()->results();
    if (!results) {
      warning(
          "You need to provide a normal diff before diffing incrementally. "
          "Either create or load one. Diffing incrementally will keep all "
          "manually confirmed matches in the result and try to reassign all "
          "other matches.");
      return false;
    }
    const bool success = results->IncrementalDiff();
    Plugin::instance()->ShowResults(Plugin::kResultsShowAll);
    return success;
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
    auto* results = Plugin::instance()->results();
    // TODO(cblichmann): Merge with SaveAndDiscardResults()
    if (results && results->IsDirty()) {
      const int answer = ask_yn(
          ASKBTN_YES,
          "Current diff results have not been saved - save before closing?");
      if (answer == ASKBTN_YES) {
        DoSaveResults();
      } else if (answer == ASKBTN_CANCEL) {  // cancel
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
  if (!CheckHaveResultsWithMessage()) {
    return false;
  }

  static const char kDialog[] =
      "STARTITEM 0\n"
      "Import Symbols/Comments\n"
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
  char buffer1[MAXSTR]{0};
  buffer1[0] = '0';
  char buffer2[MAXSTR]{0};
  buffer2[0] = '0';
  if (!ask_form(kDialog, &start_address_source, &end_address_source,
                &start_address_target, &end_address_target, buffer1, buffer2)) {
    return false;
  }

  Timer<> timer;
  const double min_confidence = std::stod(buffer1);
  const double min_similarity = std::stod(buffer2);
  not_absl::Status status = Plugin::instance()->results()->PortComments(
      start_address_source, end_address_source, start_address_target,
      end_address_target, min_confidence, min_similarity);
  if (!status.ok()) {
    const std::string message(status.message());
    LOG(INFO) << "Error: " << message;
    warning("Error: %s\n", message.c_str());
    return false;
  }
  MatchedFunctionsChooser::Refresh();
  UnmatchedFunctionsChooserPrimary::Refresh();
  LOG(INFO) << absl::StrCat(HumanReadableDuration(timer.elapsed()),
                            " for comment porting");
  return true;
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

bool WriteResults(const std::string& path) {
  if (FileExists(path)) {
    if (ask_yn(ASKBTN_YES, "File\n'%s'\nalready exists - overwrite?",
               path.c_str()) != 1) {
      return false;
    }
  }

  WaitBox wait_box("Writing results...");
  Timer<> timer;
  LOG(INFO) << "Writing results...";
  auto* results = Plugin::instance()->results();
  std::string export1(results->call_graph1_.GetFilePath());
  std::string export2(results->call_graph2_.GetFilePath());
  auto temp_dir_or = GetOrCreateTempDirectory("BinDiff");
  if (!temp_dir_or.ok()) {
    return false;
  }
  const std::string temp_dir = std::move(temp_dir_or).ValueOrDie();
  const std::string out_dir(Dirname(path));

  if (!results->IsIncomplete()) {
    DatabaseWriter writer(path);
    results->Write(&writer);
  } else {
    // results are incomplete (have been loaded)
    // copy original result file to temp dir first, so we can overwrite the
    // original if required
    std::remove(JoinPath(temp_dir, "input.BinDiff").c_str());
    CopyFile(results->input_filename_, JoinPath(temp_dir, "input.BinDiff"));
    {
      SqliteDatabase database(JoinPath(temp_dir, "input.BinDiff").c_str());
      DatabaseTransmuter writer(database, results->fixed_point_infos_);
      results->Write(&writer);
    }
    std::remove(path.c_str());
    CopyFile(JoinPath(temp_dir, "input.BinDiff"), path);
    std::remove(JoinPath(temp_dir, "input.BinDiff").c_str());
  }
  std::string new_export1(JoinPath(out_dir, Basename(export1)));
  if (export1 != new_export1) {
    std::remove(new_export1.c_str());
    CopyFile(export1, new_export1);
  }
  std::string new_export2(JoinPath(out_dir, Basename(export2)));
  if (export2 != new_export2) {
    std::remove(new_export2.c_str());
    CopyFile(export2, new_export2);
  }

  LOG(INFO) << absl::StrCat("done (", HumanReadableDuration(timer.elapsed()),
                            ")");
  return true;
}

bool DoSaveResultsLog() {
  if (!CheckHaveResultsWithMessage()) {
    return false;
  }
  auto* results = Plugin::instance()->results();
  if (results->IsIncomplete()) {
    info("AUTOHIDE NONE\nSaving to log is not supported for loaded results.");
    return false;
  }

  const std::string default_filename(
      results->call_graph1_.GetFilename() + "_vs_" +
      results->call_graph2_.GetFilename() + ".results");
  const char* filename = ask_file(
      /*for_saving=*/true, default_filename.c_str(), "%s",
      absl::StrCat("FILTER BinDiff Result Log files|*.results|All files",
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
  results->Write(&writer);
  LOG(INFO) << absl::StrCat("done (", HumanReadableDuration(timer.elapsed()),
                            ")");
  return true;
}

bool DoSaveResultsDebug() {
  if (!CheckHaveResultsWithMessage()) {
    return false;
  }

  auto* results = Plugin::instance()->results();
  const std::string default_filename(
      results->call_graph1_.GetFilename() + "_vs_" +
      results->call_graph2_.GetFilename() + ".truth");
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
  GroundtruthWriter writer(filename, results->fixed_point_infos_,
                           results->flow_graph_infos1_,
                           results->flow_graph_infos2_);
  results->Write(&writer);
  LOG(INFO) << absl::StrCat("done (", HumanReadableDuration(timer.elapsed()),
                            ")");

  return true;
}

bool DoSaveResults() {
  if (!CheckHaveResultsWithMessage()) {
    return false;
  }

  try {
    auto* results = Plugin::instance()->results();
    // TODO(soerenme) figure out how to use m_InputFileName from g_results if
    // we have loaded results.
    // It seems the filechooser will only ever use the directory part of the
    // default filename _or_ the filename part. I want both!
    // (Hex-Rays has confirmed this to be a bug and promised to fix it in 6.2)
    std::string default_filename(
        results->call_graph1_.GetFilename() + "_vs_" +
        results->call_graph2_.GetFilename() + ".BinDiff");
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

bool Plugin::LoadResults() {
  try {
    if (results_ && results_->IsDirty()) {
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

    std::string path(Dirname(filename));

    LOG(INFO) << "Loading results...";
    WaitBox wait_box("Loading results...");
    Timer<> timer;

    results_.reset(new Results());

    auto temp_dir_or = GetOrCreateTempDirectory("BinDiff");
    if (!temp_dir_or.ok()) {
      return false;
    }
    const std::string temp_dir = std::move(temp_dir_or).ValueOrDie();

    SqliteDatabase database(filename);
    DatabaseReader reader(database, filename, temp_dir);
    results_->Read(&reader);

    auto sha256_or = GetInputFileSha256();
    auto status = sha256_or.status();
    std::string hash;
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
      throw std::runtime_error{std::string(status.message())};
    }
    if (hash != absl::AsciiStrToLower(results_->call_graph1_.GetExeHash())) {
      const std::string message = absl::StrCat(
          "Error: currently loaded IDBs input file hash differs from "
          "result file primary graph. Please load IDB for: ",
          results_->call_graph1_.GetExeFilename());
      LOG(INFO) << message;
      throw std::runtime_error(message);
    }

    ShowResults(kResultsShowAll);

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
  results_.reset();
  return false;
}

void idaapi ButtonDiffDatabaseCallback(int /* button_code */,
                                       form_actions_t& actions) {
  if (DoDiffDatabase(/*filtered=*/false)) {
    actions.close(/*close_normally=*/1);
  }
}

void idaapi ButtonDiffDatabaseFilteredCallback(int /* button_code */,
                                               form_actions_t& actions) {
  if (DoDiffDatabase(/*filtered=*/true)) {
    actions.close(/*close_normally=*/1);
  }
}

void idaapi ButtonRediffDatabaseCallback(int /* button_code */,
                                         form_actions_t& actions) {
  if (DoRediffDatabase()) {
    actions.close(/*close_normally=*/1);
  }
}

void idaapi ButtonLoadResultsCallback(int /* button_code */,
                                      form_actions_t& actions) {
  if (Plugin::instance()->LoadResults()) {
    actions.close(/*close_normally=*/1);
  }
}

void idaapi ButtonSaveResultsCallback(int /* button_code */,
                                      form_actions_t& actions) {
  if (DoSaveResults()) {
    actions.close(/*close_normally=*/1);
  }
}

#ifndef NDEBUG
void idaapi ButtonSaveResultsLogCallback(int /* button_code */,
                                         form_actions_t& actions) {
  if (DoSaveResultsLog()) {
    actions.close(/*close_normally=*/1);
  }
}

void idaapi ButtonSaveResultsDebugCallback(int /* button_code */,
                                           form_actions_t& actions) {
  if (DoSaveResultsDebug()) {
    actions.close(/*close_normally=*/1);
  }
}
#endif

void idaapi ButtonPortCommentsCallback(int /* button_code */,
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
    return Plugin::instance()->LoadResults();  // Refresh if user did not cancel
  }
};

class SaveResultsAction : public ActionHandler<SaveResultsAction> {
  int idaapi activate(action_activation_ctx_t*) override {
    return DoSaveResults();  // Refresh if user did not cancel
  }
};

class ShowMatchedAction : public ActionHandler<ShowMatchedAction> {
  int idaapi activate(action_activation_ctx_t*) override {
    Plugin::instance()->ShowResults(Plugin::kResultsShowMatched);
    return 0;
  }
};

class ShowStatisticsAction : public ActionHandler<ShowStatisticsAction> {
  int idaapi activate(action_activation_ctx_t*) override {
    Plugin::instance()->ShowResults(Plugin::kResultsShowStatistics);
    return 0;
  }
};

class ShowPrimaryUnmatchedAction
    : public ActionHandler<ShowPrimaryUnmatchedAction> {
  int idaapi activate(action_activation_ctx_t*) override {
    Plugin::instance()->ShowResults(Plugin::kResultsShowPrimaryUnmatched);
    return 0;
  }
};

class ShowSecondaryUnmatchedAction
    : public ActionHandler<ShowSecondaryUnmatchedAction> {
  int idaapi activate(action_activation_ctx_t*) override {
    Plugin::instance()->ShowResults(Plugin::kResultsShowSecondaryUnmatched);
    return 0;
  }
};

int idaapi ViewFlowGraphsAction::activate(action_activation_ctx_t* context) {
  const auto& chooser_selection = context->chooser_selection;
  if (chooser_selection.empty()) {
    return 0;
  }
  Plugin::instance()->VisualDiff(chooser_selection.front(),
                                 /*call_graph_diff=*/false);
  return 1;
}

int HandleImportSymbolsComments(action_activation_ctx_t* context,
                                Results::PortCommentsKind kind) {
  auto* results = Plugin::instance()->results();
  if (!results) {
    return 0;
  }
  const auto ida_selection = context->chooser_selection;
  not_absl::Status status = results->PortComments(
      absl::MakeConstSpan(&ida_selection.front(), ida_selection.size()), kind);
  if (!status.ok()) {
    const std::string message(status.message());
    LOG(INFO) << "Error: " << message;
    warning("Error: %s\n", message.c_str());
    return 0;
  }
  // Need to refresh all choosers
  MatchedFunctionsChooser::Refresh();
  UnmatchedFunctionsChooserPrimary::Refresh();
  UnmatchedFunctionsChooserSecondary::Refresh();
  StatisticsChooser::Refresh();
  return 1;
}

int idaapi
ImportSymbolsCommentsAction::activate(action_activation_ctx_t* context) {
  return HandleImportSymbolsComments(context, Results::kNormal);
}

int idaapi ImportSymbolsCommentsExternalAction::activate(
    action_activation_ctx_t* context) {
  return HandleImportSymbolsComments(context, Results::kAsExternalLib);
}

int idaapi ConfirmMatchesAction::activate(action_activation_ctx_t* context) {
  auto* results = Plugin::instance()->results();
  if (!results) {
    return 0;
  }
  const auto& ida_selection = context->chooser_selection;
  not_absl::Status status = results->ConfirmMatches(
      absl::MakeConstSpan(&ida_selection.front(), ida_selection.size()));
  if (!status.ok()) {
    const std::string message(status.message());
    LOG(INFO) << "Error: " << message;
    warning("Error: %s\n", message.c_str());
    return 0;
  }
  MatchedFunctionsChooser::Refresh();
  return 1;
}

int HandleCopyAddress(Address address) {
  not_absl::Status status = CopyToClipboard(FormatAddress(address));
  if (!status.ok()) {
    const std::string message(status.message());
    LOG(INFO) << "Error: " << message;
    warning("Error: %s\n", message.c_str());
    return 0;
  }
  return 1;
}

int idaapi
CopyPrimaryAddressAction::activate(action_activation_ctx_t* context) {
  auto* results = Plugin::instance()->results();
  if (!results || context->chooser_selection.empty()) {
    return 0;
  }
  return HandleCopyAddress(
      results->GetMatchPrimaryAddress(context->chooser_selection.front()));
}

int idaapi
CopySecondaryAddressAction::activate(action_activation_ctx_t* context) {
  auto* results = Plugin::instance()->results();
  if (!results || context->chooser_selection.empty()) {
    return 0;
  }
  return HandleCopyAddress(
      results->GetMatchSecondaryAddress(context->chooser_selection.front()));
}

class CopyAddressAction : public ActionHandler<CopyAddressAction> {
  int idaapi activate(action_activation_ctx_t* context) override {
    auto* results = Plugin::instance()->results();
    if (!results || context->chooser_selection.empty()) {
      return 0;
    }
    const auto index = context->chooser_selection.front();
    absl::string_view action{context->action};
    Address address;
    if (action == UnmatchedFunctionsChooserPrimary::kCopyAddressAction) {
      address = results->GetUnmatchedDescriptionPrimary(index).address;
    } else if (action ==
               UnmatchedFunctionsChooserSecondary::kCopyAddressAction) {
      address = results->GetUnmatchedDescriptionSecondary(index).address;
    } else {
      return 0;
    }
    return HandleCopyAddress(address);
  }
};

class AddMatchAction : public ActionHandler<AddMatchAction> {
  int idaapi activate(action_activation_ctx_t* context) override {
    auto* results = Plugin::instance()->results();
    if (!results || context->chooser_selection.empty()) {
      return 0;
    }
    absl::string_view action{context->action};
    ssize_t index_primary = chooser_base_t::NO_SELECTION;
    ssize_t index_secondary = chooser_base_t::NO_SELECTION;
    if (action == UnmatchedFunctionsChooserPrimary::kAddMatchAction) {
      index_primary = context->chooser_selection.front();
      index_secondary = UnmatchedFunctionsAddMatchChooserSecondary().choose();
    } else if (action == UnmatchedFunctionsChooserSecondary::kAddMatchAction) {
      index_primary = UnmatchedFunctionsAddMatchChooserPrimary().choose();
      index_secondary = context->chooser_selection.front();
    }
    if (index_primary == chooser_base_t::NO_SELECTION ||
        index_secondary == chooser_base_t::NO_SELECTION) {
      return 0;  // User cancelled
    }
    WaitBox wait_box("Performing basic block diff...");
    not_absl::Status status =
        results->AddMatch(results->GetPrimaryAddress(index_primary),
                            results->GetSecondaryAddress(index_secondary));
    if (!status.ok()) {
      const std::string message(status.message());
      LOG(INFO) << "Error: " << message;
      warning("Error: %s\n", message.c_str());
      return 0;
    }
    MatchedFunctionsChooser::Refresh();
    UnmatchedFunctionsChooserPrimary::Refresh();
    UnmatchedFunctionsChooserSecondary::Refresh();
    StatisticsChooser::Refresh();
    return 1;
  }
};

class ImportSymbolsCommentsGlobalAction
    : public ActionHandler<ImportSymbolsCommentsGlobalAction> {
 public:
  static constexpr const char kName[] =
      "bindiff:import_symbols_comments_global";
  static constexpr const char kLabel[] = "Im~p~ort symbols/comments...";
  static constexpr const char kShortCut[] = "";
  static constexpr const char* kTooltip = nullptr;

 private:
  int idaapi activate(action_activation_ctx_t* context) override {
    // Not called from the chooser, display dialog.
    return DoPortComments();  // Refresh if user did not cancel
  }
};

constexpr const char ImportSymbolsCommentsGlobalAction::kName[];
constexpr const char ImportSymbolsCommentsGlobalAction::kLabel[];
constexpr const char ImportSymbolsCommentsGlobalAction::kShortCut[];
constexpr const char* ImportSymbolsCommentsGlobalAction::kTooltip;

void Plugin::InitActions() {
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

  register_action(ImportSymbolsCommentsGlobalAction::MakeActionDesc());

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

  MatchedFunctionsChooser::RegisterActions();

  // Unmatched choosers
  register_action(CopyAddressAction::MakeActionDesc(
      UnmatchedFunctionsChooserPrimary::kCopyAddressAction, "Copy ~a~ddress",
      /*shortcut=*/"", /*tooltip=*/"", /*icon=*/-1));
  register_action(AddMatchAction::MakeActionDesc(
      UnmatchedFunctionsChooserPrimary::kAddMatchAction, "Add ~m~atch",
      /*shortcut=*/"", /*tooltip=*/"", /*icon=*/-1));
  register_action(CopyAddressAction::MakeActionDesc(
      UnmatchedFunctionsChooserSecondary::kCopyAddressAction, "Copy ~a~ddress",
      /*shortcut=*/"", /*tooltip=*/"", /*icon=*/-1));
  register_action(AddMatchAction::MakeActionDesc(
      UnmatchedFunctionsChooserSecondary::kAddMatchAction, "Add ~m~atch",
      /*shortcut=*/"", /*tooltip=*/"", /*icon=*/-1));
}

void Plugin::InitMenus() {
  attach_action_to_menu("File/ProduceFile", "bindiff:diff_database",
                        SETMENU_APP);
  attach_action_to_menu("File/LoadFile/AdditionalBinaryFile",
                        "bindiff:load_results", SETMENU_APP);
  attach_action_to_menu("File/ProduceFile/CreateCallgraphGDL",
                        "bindiff:save_results", SETMENU_APP);

  attach_action_to_menu("Edit/Comments/InsertPredefinedComment",
                        "bindiff:port_comments", SETMENU_APP);

  // TODO(cblichmann): Use create_menu() in IDA 7.3
  attach_action_to_menu("View", nullptr, SETMENU_APP);
  attach_action_to_menu("View/BinDiff/", "bindiff:show_matched",
                        SETMENU_FIRST);
  attach_action_to_menu("View/BinDiff/MatchedFunctions",
                        "bindiff:show_statistics", SETMENU_APP);
  attach_action_to_menu("View/BinDiff/Statistics",
                        "bindiff:show_primary_unmatched", SETMENU_APP);
  attach_action_to_menu("View/BinDiff/PrimaryUnmatched",
                        "bindiff:show_secondary_unmatched", SETMENU_APP);
}

void Plugin::TermMenus() {
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

int Plugin::Init() {
  alsologtostderr_ =
      absl::AsciiStrToUpper(GetArgument("AlsoLogToStdErr")) == "TRUE";
  if (!InitLogging(LoggingOptions{}
                       .set_alsologtostderr(alsologtostderr_)
                       .set_log_filename(GetArgument("LogFile")))) {
    LOG(INFO) << "Error initializing logging, skipping BinDiff plugin";
    return PLUGIN_SKIP;
  }

  LOG(INFO) << kBinDiffName << " " << kBinDiffDetailedVersion << ", "
            << kBinDiffCopyright;

  addon_info_t addon_info;
  addon_info.id = "com.google.bindiff";
  addon_info.name = kBinDiffName;
  addon_info.producer = "Google";
  addon_info.version = kBinDiffDetailedVersion;
  addon_info.url = "https://zynamics.com/bindiff.html";
  addon_info.freeform = kBinDiffCopyright;
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

  if (!InitConfig().ok()) {
    LOG(ERROR)
        << "Error: Could not load configuration file, skipping BinDiff plugin.";
    return PLUGIN_SKIP;
  }
  InitActions();
  InitMenus();
  init_done_ = true;

  return PLUGIN_KEEP;
}

void Plugin::Terminate() {
  unhook_from_notification_point(HT_UI, UiHook, /*user_data=*/nullptr);
  unhook_from_notification_point(HT_IDB, IdbHook, /*user_data=*/nullptr);
  unhook_from_notification_point(HT_IDP, ProcessorHook, /*user_data=*/nullptr);

  if (init_done_) {
    TermMenus();
    SaveAndDiscardResults();
  } else {
    DiscardResults();
  }

  ShutdownLogging();
}

bool Plugin::Run(size_t /* argument */) {
  static const std::string kDialogBase = absl::StrCat(
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
      kBinDiffName, " ", kBinDiffDetailedVersion, "\n", kBinDiffCopyright, "\n",
      "ENDHELP\n", kBinDiffName, " ", kBinDiffDetailedVersion, "\n",
      "\n"
      "<~D~iff Database...:B:1:30::>\n"
      "<D~i~ff Database Filtered...:B:1:30::>\n\n"
      "<L~o~ad Results...:B:1:30::>\n\n");

  static const std::string kDialogResultsAvailable = absl::StrCat(
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
      kBinDiffName, " ", kBinDiffDetailedVersion, "\n", kBinDiffCopyright, "\n",
      "ENDHELP\n", kBinDiffName, " ", kBinDiffDetailedVersion, "\n",
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

  if (!CheckHaveIdbWithMessage()) {
    return false;
  }

  if (results_) {
    // We may have to unload a previous result if the input IDB has changed in
    // the meantime
    auto sha256_or = GetInputFileSha256();
    if (!sha256_or.ok()) {
      throw std::runtime_error{std::string(sha256_or.status().message())};
    }
    if (sha256_or.ValueOrDie() !=
        absl::AsciiStrToLower(results_->call_graph1_.GetExeHash())) {
      warning("Discarding current results since the input IDB has changed.");
      DiscardResults();
    }
  }

  if (!results_) {
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

}  // namespace bindiff
}  // namespace security

using security::bindiff::Plugin;

plugin_t PLUGIN = {
    IDP_INTERFACE_VERSION,
    PLUGIN_FIX,  // Plugin flags
    []() { return Plugin::instance()->Init(); },
    []() { Plugin::instance()->Terminate(); },
    [](size_t argument) { return Plugin::instance()->Run(argument); },
    Plugin::kComment,                 // Statusline text
    nullptr,                          // Multiline help about the plugin, unused
    security::bindiff::kBinDiffName,  // Preferred short name of the plugin
    Plugin::kHotKey                   // Preferred hotkey to run the plugin
};
