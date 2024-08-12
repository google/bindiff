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

#include "third_party/zynamics/bindiff/ida/main_plugin.h"

#include <cstdarg>
#include <cstdint>
#include <cstdio>
#include <cstring>
#include <exception>
#include <limits>
#include <memory>
#include <stdexcept>
#include <string>
#include <thread>  // NOLINT(build/c++11)
#include <utility>
#include <vector>

// clang-format off
#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <bytes.hpp>                                            // NOLINT
#include <diskio.hpp>                                           // NOLINT
#include <expr.hpp>                                             // NOLINT
#include <frame.hpp>                                            // NOLINT
#include <funcs.hpp>                                            // NOLINT
#include <ida.hpp>                                              // NOLINT
#include <idp.hpp>                                              // NOLINT
#include <kernwin.hpp>                                          // NOLINT
#include <loader.hpp>                                           // NOLINT
#include <nalt.hpp>                                             // NOLINT
#include <name.hpp>                                             // NOLINT
#include <ua.hpp>                                               // NOLINT
#include <xref.hpp>                                             // NOLINT
#if IDP_INTERFACE_VERSION < 900
#include <enum.hpp>                                             // NOLINT
#include <struct.hpp>                                           // NOLINT
#endif
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT
// clang-format on

#include "third_party/absl/log/log.h"
#include "third_party/absl/memory/memory.h"
#include "third_party/absl/status/status.h"
#include "third_party/absl/status/statusor.h"
#include "third_party/absl/strings/ascii.h"
#include "third_party/absl/strings/match.h"
#include "third_party/absl/strings/numbers.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/absl/types/span.h"
#include "third_party/zynamics/bindiff/change_classifier.h"
#include "third_party/zynamics/bindiff/config.h"
#include "third_party/zynamics/bindiff/database_writer.h"
#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/groundtruth_writer.h"
#include "third_party/zynamics/bindiff/ida/bindiff_icon.h"
#include "third_party/zynamics/bindiff/ida/matched_functions_chooser.h"
#include "third_party/zynamics/bindiff/ida/results.h"
#include "third_party/zynamics/bindiff/ida/statistics_chooser.h"
#include "third_party/zynamics/bindiff/ida/unmatched_functions_chooser.h"
#include "third_party/zynamics/bindiff/ida/visual_diff.h"
#include "third_party/zynamics/bindiff/log_writer.h"
#include "third_party/zynamics/bindiff/match/call_graph.h"
#include "third_party/zynamics/bindiff/match/context.h"
#include "third_party/zynamics/bindiff/match/flow_graph.h"
#include "third_party/zynamics/bindiff/reader.h"
#include "third_party/zynamics/bindiff/sqlite.h"
#include "third_party/zynamics/bindiff/version.h"
#include "third_party/zynamics/binexport/ida/digest.h"
#include "third_party/zynamics/binexport/ida/log_sink.h"
#include "third_party/zynamics/binexport/ida/ui.h"
#include "third_party/zynamics/binexport/ida/util.h"
#include "third_party/zynamics/binexport/util/filesystem.h"
#include "third_party/zynamics/binexport/util/format.h"
#include "third_party/zynamics/binexport/util/idb_export.h"
#include "third_party/zynamics/binexport/util/logging.h"
#include "third_party/zynamics/binexport/util/process.h"
#include "third_party/zynamics/binexport/util/status_macros.h"
#include "third_party/zynamics/binexport/util/timer.h"
#include "third_party/zynamics/binexport/util/types.h"

namespace security::bindiff {

using ::security::binexport::FormatAddress;
using ::security::binexport::GetInputFileMd5;
using ::security::binexport::GetInputFileSha256;
using ::security::binexport::GetOrCreateAppDataDirectory;
using ::security::binexport::HumanReadableDuration;
using ::security::binexport::IdaLogSink;
using ::security::binexport::IdbExporter;
using ::security::binexport::InitLogging;
using ::security::binexport::LoggingOptions;
using ::security::binexport::ShutdownLogging;
using ::security::binexport::ToStringView;

std::string GetArgument(absl::string_view name) {
  const char* option =
      get_plugin_options(absl::StrCat("BinDiff", name).c_str());
  return option ? option : "";
}

std::string GetLogFilename(const bindiff::Config& config,
                           absl::string_view basename) {
  std::string log_dir = config.log().directory();
  if (log_dir.empty()) {
    log_dir = JoinPath(GetOrCreateAppDataDirectory(kBinDiffName).value_or("."),
                       "logs");
    CreateDirectories(log_dir)
        .IgnoreError();  // Let logging code handle the error
  }
  return JoinPath(log_dir, basename);
}

bool DoSaveResults();

std::string FindFile(absl::string_view path, absl::string_view extension) {
  std::vector<std::string> entries;
  GetDirectoryEntries(path, &entries).IgnoreError();
  for (const auto& entry : entries) {
    if (GetFileExtension(entry) == extension) {
      return JoinPath(path, Basename(entry));
    }
  }
  return "";
}

bool CheckHaveBinExportWithMessage() {
  addon_info_t addon_info;
  if (!get_addon_info("com.google.binexport", &addon_info)) {
    warning("Required BinExport plugin is missing.");
    return false;
  }
  return true;
}

bool CheckHaveIdbWithMessage() {
  if (strlen(get_path(PATH_TYPE_IDB)) == 0) {
    info("AUTOHIDE NONE\nPlease open an IDB first.");
    return false;
  }
  return true;
}

bool CheckHaveResultsWithMessage() {
  if (!Plugin::instance()->results()) {
    info("AUTOHIDE NONE\nPlease perform a diff first.");
    return false;
  }
  return true;
}

template <typename T>
class ActionHandlerWithIdb : public ActionHandler<T> {
  action_state_t idaapi update(action_update_ctx_t* ctx) override {
    return strlen(get_path(PATH_TYPE_IDB)) ? AST_ENABLE_FOR_IDB
                                           : AST_DISABLE_FOR_IDB;
  }
};

template <typename T>
class ActionHandlerWithResults : public ActionHandler<T> {
  action_state_t idaapi update(action_update_ctx_t* ctx) override {
    return Plugin::instance()->results() ? AST_ENABLE : AST_DISABLE;
  }
};

absl::StatusOr<bool> ExportIdbs() {
  if (!CheckHaveIdbWithMessage()) {
    return false;
  }

  NA_ASSIGN_OR_RETURN(std::string temp_dir,
                      GetOrCreateTempDirectory("BinDiff"));

  absl::StatusOr<std::string> secondary_idb = GetOpenFilename(
      "Select Secondary Database", "*.i64;*.idb",
      {{"IDA Databases", "*.i64;*.idb"}, {"All files", kAllFilesFilter}});
  if (!secondary_idb.ok()) {
    return false;
  }
  std::string secondary_idb_path = std::move(*secondary_idb);

  const std::string primary_idb_path(get_path(PATH_TYPE_IDB));
  if (primary_idb_path == secondary_idb_path) {
    return absl::FailedPreconditionError(
        "You cannot open the same database twice. Please copy and rename one "
        "if you want to diff it against itself.");
  }
  if ((Dirname(primary_idb_path) == Dirname(secondary_idb_path)) &&
      (ReplaceFileExtension(primary_idb_path, "") ==
       ReplaceFileExtension(secondary_idb_path, ""))) {
    return absl::FailedPreconditionError(
        "You cannot open a 64-bit database and a 32-bit database with the "
        "same name in the same directory. Please rename or move one of the "
        "files.");
  }

#ifndef __EA64__
  // This can only happen with a primary IDA Pro instance that is not 64-bit
  // address aware.
  if (absl::AsciiStrToUpper(GetFileExtension(primary_idb_path)) == ".IDB" &&
      absl::AsciiStrToUpper(GetFileExtension(secondary_idb_path)) == ".I64") {
    if (ask_yn(ASKBTN_YES,
               "Warning: You requested to diff a 32-bit binary vs. a 64-bit "
               "binary.\n"
               "If the 64-bit binary contains addresses outside the 32-bit "
               "range they will be truncated.\n"
               "To fix this problem please start 64-bit aware IDA and diff "
               "the other way around, i.e. 64-bit vs. 32-bit.\n"
               "Continue anyways?") != ASKBTN_YES) {
      return false;
    }
  }
#endif

  LOG(INFO) << "Diffing " << Basename(primary_idb_path) << " vs "
            << Basename(secondary_idb_path);
  WaitBox wait_box("Exporting idbs...");

  const std::string primary_temp_dir = JoinPath(temp_dir, "primary");
  RemoveAll(primary_temp_dir).IgnoreError();
  NA_RETURN_IF_ERROR(CreateDirectories(primary_temp_dir));

  const std::string secondary_temp_dir = JoinPath(temp_dir, "secondary");
  RemoveAll(secondary_temp_dir).IgnoreError();
  NA_RETURN_IF_ERROR(CreateDirectories(secondary_temp_dir));

  {
    const auto& config = config::Proto();
    auto options =
        IdbExporter::Options()
            .set_export_dir(secondary_temp_dir)
            .set_ida_dir(idadir(/*subdir=*/nullptr))
            .set_alsologtostderr(Plugin::instance()->alsologtostderr());
    if (config.log().to_file()) {
      options.set_log_filename(
          GetLogFilename(config, "bindiff_idapro_secondary.log"));
    }
    IdbExporter exporter(options);
    exporter.AddDatabase(secondary_idb_path);

    absl::Status status;
    std::thread export_thread(
        [&status, &exporter]() { status = exporter.Export(); });

    const std::string primary_binexport = JoinPath(
        primary_temp_dir,
        ReplaceFileExtension(Basename(primary_idb_path), ".BinExport"));
    idc_value_t arg = primary_binexport.c_str();
    if (qstring errbuf; !call_idc_func(
            /*result=*/nullptr, "BinExportBinary", &arg,
            /*argsnum=*/1, &errbuf, /*resolver=*/nullptr)) {
      export_thread.detach();
      return absl::UnknownError(absl::StrCat(
          "Export of the primary database failed: ", ToStringView(errbuf)));
    }

    export_thread.join();
    if (!status.ok()) {
      return absl::UnknownError(absl::StrCat(
          "Export of the secondary database failed: ", status.message()));
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
    if (absl::Status status = SendGuiMessage(config::Proto(), message, [] {});
        !status.ok()) {
      const std::string error_message = absl::StrCat(
          "Cannot launch BinDiff user interface. Process creation failed: ",
          status.message());
      LOG(INFO) << error_message;
      warning("%s\n", error_message.c_str());
    }
  } catch (const std::runtime_error& error_message) {
    LOG(INFO) << "Error while calling BinDiff UI: " << error_message.what();
    warning("Error while calling BinDiff UI: %s\n", error_message.what());
  } catch (...) {
    LOG(INFO) << "Unknown error while calling BinDiff UI";
    warning("Unknown error while calling BinDiff UI\n");
  }
}

bool Plugin::DiscardResults(Plugin::DiscardResultsKind kind) {
  if (!results_) {
    return true;
  }

  if (kind != DiscardResultsKind::kDontSave && results_->is_modified()) {
    const auto answer =
        ask_yn(ASKBTN_YES,
               "%sCurrent diff results have not been"
               " saved - save before closing?",
               kind == DiscardResultsKind::kAskSave ? "HIDECANCEL\n" : "");
    if (answer == ASKBTN_YES) {
      DoSaveResults();
    } else if (answer == ASKBTN_CANCEL) {
      return false;
    }
  }

  MatchedFunctionsChooser::Close();
  UnmatchedFunctionsChooserPrimary::Close();
  UnmatchedFunctionsChooserSecondary::Close();
  StatisticsChooser::Close();

  results_.reset();
  return true;
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
      // in UiHook(). Action names are not guaranteed to be stable, though, so
      // take the last opportunity to ask whether to save the results.
      Plugin::instance()->DiscardResults(Plugin::DiscardResultsKind::kAskSave);
      break;
  }
  return 0;
}

ssize_t idaapi UiHook(void*, int event_id, va_list arguments) {
  switch (event_id) {
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
      flow_graphs->erase(it++);
    } else {
      ++it;
    }
  }
  call_graph->DeleteVertices(start, end);
}

absl::StatusOr<bool> DiffAddressRange(ea_t start_address_source,
                                      ea_t end_address_source,
                                      ea_t start_address_target,
                                      ea_t end_address_target) {
  Plugin& plugin = *Plugin::instance();
  plugin.DiscardResults(Plugin::DiscardResultsKind::kDontSave);
  Timer<> timer;

  NA_ASSIGN_OR_RETURN(const bool exported, ExportIdbs());
  if (!exported) {
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
  // TODO(cblichmann): Create directory with random suffix, so that multiple
  //                   invocations don't interfere with each other.
  NA_ASSIGN_OR_RETURN(const std::string temp_dir,
                      GetOrCreateTempDirectory("BinDiff"));
  const std::string filename1 =
      FindFile(JoinPath(temp_dir, "primary"), ".BinExport");
  if (filename1.empty()) {
    return absl::FailedPreconditionError(
        "Exporting the primary (this) database failed.\n"
        "Please check whether the BinExport plugin is installed correctly.");
  }
  const std::string filename2 =
      FindFile(JoinPath(temp_dir, "secondary"), ".BinExport");
  if (filename2.empty()) {
    return absl::FailedPreconditionError(
        "Exporting the secondary database failed. "
        "Is it opened in another instance?\n"
        "Please close all other IDA instances and try again.");
  }

  NA_RETURN_IF_ERROR(plugin.ClearResults());

  Results* results = plugin.results();
  NA_RETURN_IF_ERROR(Read(filename1, &results->call_graph1_,
                          &results->flow_graphs1_, &results->flow_graph_infos1_,
                          &results->instruction_cache_));
  NA_RETURN_IF_ERROR(Read(filename2, &results->call_graph2_,
                          &results->flow_graphs2_, &results->flow_graph_infos2_,
                          &results->instruction_cache_));
  MatchingContext context(results->call_graph1_, results->call_graph2_,
                          results->flow_graphs1_, results->flow_graphs2_,
                          results->fixed_points_);
  FilterFunctions(start_address_source, end_address_source,
                  &context.primary_call_graph_, &context.primary_flow_graphs_,
                  &results->flow_graph_infos1_);
  FilterFunctions(
      start_address_target, end_address_target, &context.secondary_call_graph_,
      &context.secondary_flow_graphs_, &results->flow_graph_infos2_);

  const MatchingSteps callgraph_steps = GetDefaultMatchingSteps();
  const MatchingStepsFlowGraph basicblock_steps =
      GetDefaultMatchingStepsBasicBlock();
  Diff(&context, callgraph_steps, basicblock_steps);
  LOG(INFO) << absl::StrCat(HumanReadableDuration(timer.elapsed()),
                            " for matching.");
  results->set_modified();
  plugin.ShowResults(Plugin::kResultsShowAll);
  return true;
}

bool DoRediffDatabase() {
  auto* results = Plugin::instance()->results();
  if (!results) {
    warning(
        "You need to create a regular diff before diffing incrementally. "
        "Either create or load one. Diffing incrementally will keep all "
        "manually confirmed matches in the result and try to reassign all "
        "other matches.");
    return false;
  }
  if (absl::Status status = results->IncrementalDiff(); !status.ok()) {
    if (!absl::IsCancelled(status)) {
      const std::string message =
          absl::StrCat("Error while diffing: ", status.message());
      LOG(INFO) << message;
      warning("%s\n", message.c_str());
    }
    return false;
  }
  Plugin::instance()->ShowResults(Plugin::kResultsShowAll);
  return true;
}

bool DoDiffDatabase(bool filtered) {
  if (!CheckHaveBinExportWithMessage() ||
      !Plugin::instance()->DiscardResults(
          Plugin::DiscardResultsKind::kAskSaveCancellable)) {
    return false;
  }

  // Default to full address range
  ea_t start_address_source = 0;
  ea_t end_address_source = std::numeric_limits<ea_t>::max() - 1;
  ea_t start_address_target = 0;
  ea_t end_address_target = std::numeric_limits<ea_t>::max() - 1;

  if (filtered) {
    constexpr char kDialog[] =
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

  absl::StatusOr<bool> diffed =
      DiffAddressRange(start_address_source, end_address_source,
                       start_address_target, end_address_target);
  if (!diffed.ok()) {
    const std::string error_message =
        absl::StrCat("Error while diffing: ", diffed.status().message());
    LOG(INFO) << error_message;
    warning("%s\n", error_message.c_str());
    return false;
  }
  return *diffed;
}

bool DoPortComments() {
  if (!CheckHaveResultsWithMessage()) {
    return false;
  }

  constexpr char kDialog[] =
      "STARTITEM 0\n"
      "Import Symbols/Comments\n"
      "Address range (default: all)\n\n"
      "  <Start address (source):$::16::>\n"
      "  <End address (source):$::16::>\n"
      "  <Start address (target):$::16::>\n"
      "  <End address (target):$::16::>\n\n"
      "Minimum confidence required (default: none)\n\n"
      "  <confidence:q::16::>\n\n"
      "Minimum similarity required (default: none)\n\n"
      "  <similarity:q::16::>\n\n";

  // Default to full address range.
  ea_t start_address_source = 0;
  ea_t end_address_source = std::numeric_limits<ea_t>::max() - 1;
  ea_t start_address_target = 0;
  ea_t end_address_target = std::numeric_limits<ea_t>::max() - 1;
  qstring min_confidence_str = "0.0";
  qstring min_similarity_str = "0.0";
  if (!ask_form(kDialog, &start_address_source, &end_address_source,
                &start_address_target, &end_address_target, &min_confidence_str,
                &min_similarity_str)) {
    return false;
  }

  Timer<> timer;
  double min_confidence;
  double min_similarity;

  if (auto s = ToStringView(min_confidence_str);
      !absl::SimpleAtod(s, &min_confidence)) {
    const std::string message =
        absl::StrCat("Error: Invalid value for minimum confidence: ", s);
    warning("%s\n", message.c_str());
    LOG(INFO) << message;
    return false;
  }
  if (auto s = ToStringView(min_similarity_str);
      !absl::SimpleAtod(s, &min_similarity)) {
    const std::string message =
        absl::StrCat("Error: Invalid value for minimum similarity: ", s);
    warning("%s\n", message.c_str());
    LOG(INFO) << message;
    return false;
  }

  absl::Status status = Plugin::instance()->results()->PortComments(
      start_address_source, end_address_source, start_address_target,
      end_address_target, min_confidence, min_similarity);
  if (!status.ok()) {
    const std::string error_message(status.message());
    LOG(INFO) << "Error: " << error_message;
    warning("Error: %s\n", error_message.c_str());
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
constexpr char kBinDiffDatabaseArgs[] = {VT_STR,  // Secondary database
                                         VT_STR,  // Results file
                                         0};
constexpr ext_idcfunc_t kBinDiffDatabaseIdcFunc = {
    "BinDiffDatabase", IdcBinDiffDatabase, kBinDiffDatabaseArgs, nullptr, 0,
    EXTFUN_BASE};

absl::Status WriteResults(const std::string& filename) {
  LOG(INFO) << "Writing results...";
  auto* results = Plugin::instance()->results();
  const std::string export1 = results->call_graph1_.GetFilePath();
  const std::string export2 = results->call_graph2_.GetFilePath();
  NA_ASSIGN_OR_RETURN(const std::string temp_dir,
                      GetOrCreateTempDirectory("BinDiff"));
  const std::string out_dir = Dirname(filename);

  if (!results->is_incomplete()) {
    NA_ASSIGN_OR_RETURN(
        auto writer,
        DatabaseWriter::Create(
            filename,
            DatabaseWriter::Options().set_include_function_names(
                !config::Proto().binary_format().exclude_function_names())));
    NA_RETURN_IF_ERROR(results->Write(writer.get()));
  } else {
    // Results are incomplete (have been loaded). Copy original result file to
    // temp dir first, so we can overwrite the original if required.
    const std::string input_bindiff = JoinPath(temp_dir, "input.BinDiff");
    std::remove(input_bindiff.c_str());
    NA_RETURN_IF_ERROR(CopyFile(results->input_filename_, input_bindiff));
    {
      NA_ASSIGN_OR_RETURN(auto database,
                          SqliteDatabase::Connect(input_bindiff));
      DatabaseTransmuter writer(database, results->fixed_point_infos_);
      NA_RETURN_IF_ERROR(results->Write(&writer));
    }
    std::remove(filename.c_str());
    NA_RETURN_IF_ERROR(CopyFile(input_bindiff, filename));
    std::remove(input_bindiff.c_str());
  }
  if (const std::string new_export1 = JoinPath(out_dir, Basename(export1));
      export1 != new_export1) {
    std::remove(new_export1.c_str());
    NA_RETURN_IF_ERROR(CopyFile(export1, new_export1));
  }
  if (const std::string new_export2 = JoinPath(out_dir, Basename(export2));
      export2 != new_export2) {
    std::remove(new_export2.c_str());
    NA_RETURN_IF_ERROR(CopyFile(export2, new_export2));
  }

  return absl::OkStatus();
}

absl::Status WriteGroundTruth(const std::string& filename) {
  LOG(INFO) << "Writing to debug ground truth file...";
  auto* results = Plugin::instance()->results();
  GroundtruthWriter writer(filename, results->fixed_point_infos_,
                           results->flow_graph_infos1_,
                           results->flow_graph_infos2_);
  return results->Write(&writer);
}

bool DoSaveResultsLog() {
  if (!CheckHaveResultsWithMessage()) {
    return false;
  }
  auto* results = Plugin::instance()->results();
  if (results->is_incomplete()) {
    info("AUTOHIDE NONE\nSaving to log is not supported for loaded results.");
    return false;
  }

  const std::string default_filename =
      absl::StrCat(results->call_graph1_.GetFilename(), "_vs_",
                   results->call_graph2_.GetFilename(), ".results");
  absl::StatusOr<std::string> filename =
      GetSaveFilename("Save Log As", default_filename,
                      {{"BinDiff Result Log files", "*.results"},
                       {"All files", kAllFilesFilter}});
  if (!filename.ok()) {
    return false;
  }

  WaitBox wait_box("Writing results...");
  Timer<> timer;
  LOG(INFO) << "Writing to log...";
  ResultsLogWriter writer(*filename);
  if (absl::Status status = results->Write(&writer); !status.ok()) {
    throw std::runtime_error(std::string(status.message()));
  }
  LOG(INFO) << absl::StrCat("done (", HumanReadableDuration(timer.elapsed()),
                            ")");
  return true;
}

bool DoSaveResults() {
  if (!CheckHaveResultsWithMessage()) {
    return false;
  }

  auto* results = Plugin::instance()->results();
  // If we have loaded results, input_filename_ will be non-empty.
  const std::string default_filename =
      results->input_filename_.empty()
          ? absl::StrCat(results->call_graph1_.GetFilename(), "_vs_",
                         results->call_graph2_.GetFilename(), ".BinDiff")
          : results->input_filename_;
  absl::StatusOr<std::string> filename =
      GetSaveFilename("Save Results As", default_filename,
                      {{"BinDiff Result files", "*.BinDiff"},
                       {"BinDiff Groundtruth files", "*.truth"}});
  if (!filename.ok()) {
    return false;
  }

  WaitBox wait_box("Writing results...");
  Timer<> timer;

  absl::Status status;
  if (absl::AsciiStrToLower(GetFileExtension(*filename)) == ".TRUTH") {
    status = WriteGroundTruth(*filename);
  } else {
    status = WriteResults(*filename);
  }

  if (!status.ok()) {
    std::string error_message =
        absl::StrCat("Error writing results: ", status.message());
    LOG(INFO) << error_message;
    warning("%s\n", error_message.c_str());
    return false;
  }

  LOG(INFO) << absl::StrCat("done (", HumanReadableDuration(timer.elapsed()),
                            ")");
  return true;
}

absl::Status Plugin::ClearResults() {
  NA_ASSIGN_OR_RETURN(results_, Results::Create());
  return absl::OkStatus();
}

bool Plugin::LoadResults() {
  try {
    if (results_ && results_->is_modified()) {
      const int answer = ask_yn(
          ASKBTN_YES,
          "Current diff results have not been saved - save before closing?");
      if (answer == 1) {  // yes
        DoSaveResults();
      } else if (answer == -1) {  // cancel
        return false;
      }
    }

    absl::StatusOr<std::string> filename =
        GetOpenFilename("Load Results", "*.BinDiff",
                        {{"BinDiff Result files", "*.BinDiff"},
                         {"All files", kAllFilesFilter}});
    if (!filename.ok()) {
      return false;
    }

    LOG(INFO) << "Loading results...";
    WaitBox wait_box("Loading results...");
    Timer<> timer;

    auto temp_dir = GetOrCreateTempDirectory("BinDiff");
    if (!temp_dir.ok()) {
      throw std::runtime_error(std::string(temp_dir.status().message()));
    }

    auto database = SqliteDatabase::Connect(*filename);
    if (!database.ok()) {
      throw std::runtime_error(std::string(database.status().message()));
    }
    DatabaseReader reader(*database, *filename, *temp_dir);
    auto status = ClearResults();
    if (!status.ok()) {
      throw std::runtime_error(std::string(status.message()));
    }
    results_->Read(&reader);

    auto sha256_or = GetInputFileSha256();
    status = sha256_or.status();
    std::string this_hash;
    if (status.ok()) {
      this_hash = std::move(sha256_or).value();
    } else {
      auto md5_or = GetInputFileMd5();
      status = md5_or.status();
      if (status.ok()) {
        this_hash = std::move(md5_or).value();
      }
    }
    if (this_hash.empty()) {
      throw std::runtime_error(std::string(status.message()));
    }
    if (this_hash !=
        absl::AsciiStrToLower(results_->call_graph1_.GetExeHash())) {
      const std::string result_primary_hash =
          results_->call_graph1_.GetExeHash();
      const std::string result_primary_filename =
          results_->call_graph1_.GetExeFilename();
      LOG(INFO) << "The original file hash from the database that is currently "
                   "loaded is different";
      LOG(INFO) << "from the primary one in the result file:";
      LOG(INFO) << "  " << this_hash << " (this IDB)";
      LOG(INFO) << "  " << result_primary_hash << " ("
                << result_primary_filename << ", to be loaded)";
      if (ask_buttons("Continue", "Cancel", "", ASKBTN_BTN1,
                      "HIDECANCEL\n"
                      "The original file hash from the database that is "
                      "currently loaded is different\n"
                      "from the primary one in the result file:\n"
                      "  %s (this IDB)\n"
                      "  %s (%s, to be loaded)\n\n"
                      "If you continue, the results will likely be inaccurate.",
                      this_hash.c_str(), result_primary_hash.c_str(),
                      result_primary_filename.c_str()) != ASKBTN_BTN1) {
        return false;
      }
    }

    ShowResults(kResultsShowAll);

    LOG(INFO) << absl::StrCat("done (", HumanReadableDuration(timer.elapsed()),
                              ")");
    return true;
  } catch (const std::exception& error_message) {
    LOG(INFO) << "Error loading results: " << error_message.what();
    warning("Error loading results: %s\n", error_message.what());
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
#endif

void idaapi ButtonPortCommentsCallback(int /* button_code */,
                                       form_actions_t& actions) {
  if (DoPortComments()) {
    actions.close(/*close_normally=*/1);
  }
}

class DiffDatabaseAction : public ActionHandlerWithIdb<DiffDatabaseAction> {
  int idaapi activate(action_activation_ctx_t*) override {
    return DoDiffDatabase(/*filtered=*/false);  // Refresh windows on success
  }
};

class LoadResultsAction : public ActionHandlerWithIdb<LoadResultsAction> {
  int idaapi activate(action_activation_ctx_t*) override {
    return Plugin::instance()->LoadResults();  // Refresh if user did not cancel
  }
};

class SaveResultsAction : public ActionHandlerWithResults<SaveResultsAction> {
  int idaapi activate(action_activation_ctx_t*) override {
    return DoSaveResults();  // Refresh if user did not cancel
  }
};

class ShowMatchedAction : public ActionHandlerWithResults<ShowMatchedAction> {
  int idaapi activate(action_activation_ctx_t*) override {
    Plugin::instance()->ShowResults(Plugin::kResultsShowMatched);
    return 0;
  }
};

class ShowStatisticsAction
    : public ActionHandlerWithResults<ShowStatisticsAction> {
  int idaapi activate(action_activation_ctx_t*) override {
    Plugin::instance()->ShowResults(Plugin::kResultsShowStatistics);
    return 0;
  }
};

class ShowPrimaryUnmatchedAction
    : public ActionHandlerWithResults<ShowPrimaryUnmatchedAction> {
  int idaapi activate(action_activation_ctx_t*) override {
    Plugin::instance()->ShowResults(Plugin::kResultsShowPrimaryUnmatched);
    return 0;
  }
};

class ShowSecondaryUnmatchedAction
    : public ActionHandlerWithResults<ShowSecondaryUnmatchedAction> {
  int idaapi activate(action_activation_ctx_t*) override {
    Plugin::instance()->ShowResults(Plugin::kResultsShowSecondaryUnmatched);
    return 0;
  }
};

int idaapi ViewCallGraphAction::activate(action_activation_ctx_t* context) {
  const auto& chooser_selection = context->chooser_selection;
  if (chooser_selection.empty()) {
    // Require a selection so the UI can center the call graph on the match.
    return 0;
  }
  Plugin::instance()->VisualDiff(chooser_selection.front(),
                                 /*call_graph_diff=*/true);
  return 1;
}

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
  absl::Status status = results->PortComments(
      absl::MakeConstSpan(&ida_selection.front(), ida_selection.size()), kind);
  if (!status.ok()) {
    const std::string error_message(status.message());
    LOG(INFO) << "Error: " << error_message;
    warning("Error: %s\n", error_message.c_str());
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
  absl::Status status = results->ConfirmMatches(
      absl::MakeConstSpan(&ida_selection.front(), ida_selection.size()));
  if (!status.ok()) {
    const std::string error_message(status.message());
    LOG(INFO) << "Error: " << error_message;
    warning("Error: %s\n", error_message.c_str());
    return 0;
  }
  MatchedFunctionsChooser::Refresh();
  return 1;
}

int HandleCopyAddress(Address address) {
  absl::Status status = CopyToClipboard(FormatAddress(address));
  if (!status.ok()) {
    const std::string error_message(status.message());
    LOG(INFO) << "Error: " << error_message;
    warning("Error: %s\n", error_message.c_str());
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
    absl::Status status =
        results->AddMatch(results->GetPrimaryAddress(index_primary),
                          results->GetSecondaryAddress(index_secondary));
    if (!status.ok()) {
      const std::string error_message(status.message());
      LOG(INFO) << "Error: " << error_message;
      warning("Error: %s\n", error_message.c_str());
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

void Plugin::InitActions() {
  const int bindiff_icon_id =
      load_custom_icon(kBinDiffIcon.data(), kBinDiffIcon.size(), "png");
  register_action(DiffDatabaseAction::MakeActionDesc(
      "bindiff:diff_database", "Bin~D~iff...", "SHIFT-D",
      /*tooltip=*/nullptr, bindiff_icon_id));

  register_action(LoadResultsAction::MakeActionDesc(
      "bindiff:load_results", "~B~inDiff results...", "CTRL-SHIFT-6",
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

  // Primary unmatched chooser
  register_action(CopyAddressAction::MakeActionDesc(
      UnmatchedFunctionsChooserPrimary::kCopyAddressAction, "Copy ~a~ddress",
      /*shortcut=*/"", /*tooltip=*/"", /*icon=*/-1));
  register_action(AddMatchAction::MakeActionDesc(
      UnmatchedFunctionsChooserPrimary::kAddMatchAction, "Add ~m~atch",
      /*shortcut=*/"", /*tooltip=*/"", /*icon=*/-1));

  // Secondary unmatched chooser
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

  create_menu("bindiff:view_bindiff", "BinDiff", "View/Open subviews");
  attach_action_to_menu("View/BinDiff/", "bindiff:show_matched", SETMENU_FIRST);
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
  delete_menu("bindiff:view_bindiff");
}

bool Plugin::Init() {
  auto& config = config::Proto();

  alsologtostderr_ =
      absl::EqualsIgnoreCase(GetArgument("AlsoLogToStdErr"), "TRUE");
  log_filename_ = GetArgument("LogFile");
  if (log_filename_.empty() && config.log().to_file()) {
    log_filename_ = GetLogFilename(config, "bindiff_idapro.log");
  }
  if (auto status = InitLogging(LoggingOptions{}
                                    .set_alsologtostderr(alsologtostderr_)
                                    .set_log_filename(log_filename_),
                                absl::make_unique<IdaLogSink>());
      !status.ok()) {
    msg("Error initializing logging, skipping BinDiff plugin\n");
    return false;
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
    return false;
  }

  if (!add_idc_func(kBinDiffDatabaseIdcFunc)) {
    LOG(INFO) << "Error registering IDC extension, skipping BinDiff plugin";
    return false;
  }

  // Update per-user config with most recent IDA directory. A directory mismatch
  // happens when updating or running different versions of IDA Pro.
  std::string ida_dir_in_config = config.ida().directory();
  config.mutable_ida()->set_directory(idadir(/*subdir=*/nullptr));
  if (config.ida().directory() != ida_dir_in_config) {
    absl::Status status = config::SaveUserConfig(config);
    if (!status.ok()) {
      LOG(INFO) << "Cannot update per-user config: " << status.message();
    }
  }

  InitActions();
  InitMenus();
  init_done_ = true;

  return true;
}

void Plugin::Terminate() {
  unhook_from_notification_point(HT_UI, UiHook, /*user_data=*/nullptr);
  unhook_from_notification_point(HT_IDB, IdbHook, /*user_data=*/nullptr);
  unhook_from_notification_point(HT_IDP, ProcessorHook, /*user_data=*/nullptr);

  if (init_done_) {
    TermMenus();
    DiscardResults(DiscardResultsKind::kAskSave);
  } else {
    DiscardResults(DiscardResultsKind::kDontSave);
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
      "<Save Results ~L~og...:B:1:30::>\n"
#endif
      "\n<Im~p~ort Symbols and Comments...:B:1:30::>\n\n");

  if (!CheckHaveBinExportWithMessage() || !CheckHaveIdbWithMessage()) {
    return false;
  }

  if (results_) {
    // We may have to unload a previous result if the input IDB has changed in
    // the meantime
    auto sha256_or = GetInputFileSha256();
    if (!sha256_or.ok()) {
      throw std::runtime_error{std::string(sha256_or.status().message())};
    }
    if (sha256_or.value() !=
        absl::AsciiStrToLower(results_->call_graph1_.GetExeHash())) {
      warning("Discarding current results since the input IDB has changed.");
      DiscardResults(DiscardResultsKind::kDontSave);
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
             ButtonSaveResultsLogCallback,
#endif
             ButtonPortCommentsCallback);
  }
  return true;
}

}  // namespace security::bindiff

using security::bindiff::Plugin;

plugin_t PLUGIN = {
    IDP_INTERFACE_VERSION,
    PLUGIN_MULTI | PLUGIN_FIX,  // Plugin flags
    Plugin::Register,
    nullptr,                          // Obsolete terminate callback
    nullptr,                          // Obsolete run callback
    Plugin::kComment,                 // Statusline text
    nullptr,                          // Multiline help about the plugin, unused
    security::bindiff::kBinDiffName,  // Preferred short name of the plugin
    Plugin::kHotKey                   // Preferred hotkey to run the plugin
};
