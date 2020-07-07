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

#include "third_party/zynamics/binexport/ida/main_plugin.h"

// clang-format off
#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <auto.hpp>                                             // NOLINT
#include <expr.hpp>                                             // NOLINT
#include <ida.hpp>                                              // NOLINT
#include <idp.hpp>                                              // NOLINT
#include <kernwin.hpp>                                          // NOLINT
#include <loader.hpp>                                           // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT
// clang-format on

#include "base/logging.h"
#include "third_party/absl/base/attributes.h"
#include "third_party/absl/memory/memory.h"
#include "third_party/absl/strings/ascii.h"
#include "third_party/absl/strings/escaping.h"
#include "third_party/absl/strings/match.h"
#include "third_party/absl/strings/numbers.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/time/time.h"
#include "third_party/zynamics/binexport/binexport2_writer.h"
#include "third_party/zynamics/binexport/call_graph.h"
#ifdef ENABLE_POSTGRESQL
#include "third_party/zynamics/binexport/database/postgresql_writer.h"
#endif
#include "third_party/zynamics/binexport/dump_writer.h"
#include "third_party/zynamics/binexport/entry_point.h"
#include "third_party/zynamics/binexport/flow_analyzer.h"
#include "third_party/zynamics/binexport/flow_graph.h"
#include "third_party/zynamics/binexport/ida/digest.h"
#include "third_party/zynamics/binexport/ida/names.h"
#include "third_party/zynamics/binexport/ida/log_sink.h"
#include "third_party/zynamics/binexport/ida/ui.h"
#include "third_party/zynamics/binexport/instruction.h"
#include "third_party/zynamics/binexport/statistics_writer.h"
#include "third_party/zynamics/binexport/util/filesystem.h"
#include "third_party/zynamics/binexport/util/format.h"
#include "third_party/zynamics/binexport/util/logging.h"
#include "third_party/zynamics/binexport/util/timer.h"
#include "third_party/zynamics/binexport/version.h"
#include "third_party/zynamics/binexport/virtual_memory.h"

namespace security::binexport {

std::string GetArgument(absl::string_view name) {
  const char* option =
      get_plugin_options(absl::StrCat("BinExport", name).c_str());
  return option ? option : "";
}

enum class ExportMode { kSql = 1, kBinary = 2, kText = 3, kStatistics = 4 };

std::string GetDefaultName(ExportMode mode) {
  std::string new_extension;
  switch (mode) {
    case ExportMode::kBinary:
      new_extension = ".BinExport";
      break;
    case ExportMode::kText:
      new_extension = ".txt";
      break;
    case ExportMode::kStatistics:
      new_extension = ".statistics";
      break;
    case ExportMode::kSql:
      // No extension for database export.
      break;
  }
  return ReplaceFileExtension(GetModuleName(), new_extension);
}

void ExportIdb(Writer* writer) {
  LOG(INFO) << GetModuleName() << ": starting export";
  WaitBox wait_box("Exporting database...");
  Timer<> timer;
  EntryPoints entry_points;
  {
    EntryPointManager entry_point_adder(&entry_points, "function chunks");
    for (size_t i = 0; i < get_fchunk_qty(); ++i) {
      if (const func_t* ida_func = getn_fchunk(i)) {
        entry_point_adder.Add(ida_func->start_ea,
                              (ida_func->flags & FUNC_TAIL)
                                  ? EntryPoint::Source::FUNCTION_CHUNK
                                  : EntryPoint::Source::FUNCTION_PROLOGUE);
      }
    }
  }

  // Add imported functions (so we won't miss imported but not referenced
  // functions).
  const auto modules(InitModuleMap());
  {
    EntryPointManager entry_point_adder(&entry_points, "calls");
    for (const auto& module : modules) {
      entry_point_adder.Add(module.first, EntryPoint::Source::CALL_TARGET);
    }
  }

  Instructions instructions;
  FlowGraph flow_graph;
  CallGraph call_graph;
  AnalyzeFlowIda(&entry_points, &modules, writer, &instructions, &flow_graph,
                 &call_graph,
                 Plugin::instance()->x86_noreturn_heuristic()
                     ? FlowGraph::NoReturnHeuristic::kNopsAfterCall
                     : FlowGraph::NoReturnHeuristic::kNone);

  LOG(INFO) << absl::StrCat(
      GetModuleName(), ": exported ", flow_graph.GetFunctions().size(),
      " functions with ", instructions.size(), " instructions in ",
      HumanReadableDuration(timer.elapsed()));
}

#ifdef ENABLE_POSTGRESQL
int ExportSql(absl::string_view schema_name,
              absl::string_view connection_string) {
  try {
    const std::string sha256 = GetInputFileSha256().value_or("");
    const std::string md5 = GetInputFileMd5().value_or("");
    if (sha256.empty() && md5.empty()) {
      throw std::runtime_error("Failed to load input file hashes");
    }
    DatabaseWriter writer(
        std::string(schema_name) /* Database */, GetModuleName(),
        /*module_id=*/0, md5, sha256, GetArchitectureName().value(),
        GetImageBase(), kBinExportName /* "BinExport " + version */,
        !connection_string.empty() ? std::string(connection_string)
                                   : GetArgument("ConnectionString"));
    int query_size = 0;
    writer.set_query_size(
        absl::SimpleAtoi(GetArgument("QuerySize"), &query_size)
            ? query_size
            : 32 << 20 /* 32 MiB */);

    ExportIdb(&writer);
  } catch (const std::exception& error) {
    LOG(INFO) << "Error exporting: " << error.what();
    warning("Error exporting: %s\n", error.what());
    return 666;
  } catch (...) {
    LOG(INFO) << "Error exporting.";
    warning("Error exporting.\n");
    return 666;
  }
  return eOk;
}
#endif

int ExportBinary(const std::string& filename) {
  try {
    std::string hash;
    if (auto sha256_or = GetInputFileSha256(); sha256_or.ok()) {
      hash = std::move(sha256_or).value();
    } else {
      hash = GetInputFileMd5().value_or("");
    }
    BinExport2Writer writer(filename, GetModuleName(), hash,
                            GetArchitectureName().value());
    ExportIdb(&writer);
  } catch (const std::exception& error) {
    LOG(INFO) << "Error exporting: " << error.what();
    warning("Error exporting: %s\n", error.what());
    return 666;
  } catch (...) {
    LOG(INFO) << "Error exporting.";
    warning("Error exporting.\n");
    return 666;
  }
  return eOk;
}

void idaapi ButtonBinaryExport(TWidget** /* fields */, int) {
  const auto name(GetDefaultName(ExportMode::kBinary));
  const char* filename = ask_file(
      /*for_saving=*/true, name.c_str(), "%s",
      "FILTER BinExport v2 files|*.BinExport\nExport to BinExport v2");
  if (!filename) {
    return;
  }

  if (FileExists(filename) &&
      ask_yn(0, "'%s' already exists - overwrite?", filename) != 1) {
    return;
  }

  ExportBinary(filename);
}

int ExportText(const std::string& filename) {
  try {
    std::ofstream file(filename);
    DumpWriter writer{file};
    ExportIdb(&writer);
  } catch (const std::exception& error) {
    LOG(INFO) << "Error exporting: " << error.what();
    warning("Error exporting: %s\n", error.what());
    return 666;
  } catch (...) {
    LOG(INFO) << "Error exporting.";
    warning("Error exporting.\n");
    return 666;
  }
  return eOk;
}

void idaapi ButtonTextExport(TWidget** /* fields */, int) {
  const auto name = GetDefaultName(ExportMode::kText);
  const char* filename = ask_file(
      /*for_saving=*/true, name.c_str(), "%s",
      "FILTER Text files|*.txt\nExport to Text");
  if (!filename) {
    return;
  }

  if (FileExists(filename) &&
      ask_yn(0, "'%s' already exists - overwrite?", filename) != 1) {
    return;
  }

  ExportText(filename);
}

int ExportStatistics(const std::string& filename) {
  try {
    std::ofstream file(filename);
    StatisticsWriter writer{file};
    ExportIdb(&writer);
  } catch (const std::exception& error) {
    LOG(INFO) << "Error exporting: " << error.what();
    warning("Error exporting: %s\n", error.what());
    return 666;
  } catch (...) {
    LOG(INFO) << "Error exporting.";
    warning("Error exporting.\n");
    return 666;
  }
  return eOk;
}

void idaapi ButtonStatisticsExport(TWidget** /* fields */, int) {
  const auto name = GetDefaultName(ExportMode::kStatistics);
  const char* filename = ask_file(
      /*for_saving=*/true, name.c_str(), "%s",
      "FILTER BinExport Statistics|*.statistics\nExport Statistics");
  if (!filename) {
    return;
  }

  if (FileExists(filename) &&
      ask_yn(0, "'%s' already exists - overwrite?", filename) != 1) {
    return;
  }

  ExportStatistics(filename);
}

const char* GetDialog() {
  static const std::string kDialog = absl::StrCat(
      "STARTITEM 0\n"
      "BUTTON YES Close\n"  // This is actually the OK button
      "BUTTON CANCEL NONE\n"
      "HELP\n"
      "See https://github.com/google/binexport/ for details on how to "
      "build/install and use this plugin.\n"
      "ENDHELP\n",
      kBinExportName,
      "\n\n\n"
      "<BinExport v2 Binary Export:B:1:30:::>\n\n"
      "<Text Dump Export:B:1:30:::>\n\n"
      "<Statistics Export:B:1:30:::>\n\n");
  return kDialog.c_str();
}

int DoExport(ExportMode mode, std::string name,
             absl::string_view connection_string) {
  if (name.empty()) {
    name = GetOrCreateTempDirectory("BinExport")
               .value_or(absl::StrCat(".", kPathSeparator));
  }
  if (IsDirectory(name) && connection_string.empty()) {
    name = JoinPath(name, GetDefaultName(mode));
  }

  Instruction::SetBitness(GetArchitectureBitness());
  switch (mode) {
    case ExportMode::kSql:
#ifdef ENABLE_POSTGRESQL
      return ExportSql(name, connection_string);
#else
      LOG(INFO) << "Error: PostgreSQL export not built-in, recompile with "
                   "ENABLE_POSTGRESQL";
      return -1;
#endif
    case ExportMode::kBinary:
      return ExportBinary(name);
    case ExportMode::kText:
      return ExportText(name);
    case ExportMode::kStatistics:
      return ExportStatistics(name);
    default:
      LOG(INFO) << "Error: Invalid export mode: " << static_cast<int>(mode);
      return -1;
  }
}

error_t idaapi IdcBinExportBinary(idc_value_t* argument, idc_value_t*) {
  return DoExport(ExportMode::kBinary, argument[0].c_str(),
                  /*connection_string=*/"");
}
static const char kBinExportBinaryIdcArgs[] = {VT_STR, 0};
static const ext_idcfunc_t kBinExportBinaryIdcFunc = {
    "BinExportBinary", IdcBinExportBinary, kBinExportBinaryIdcArgs, nullptr, 0,
    EXTFUN_BASE};

error_t idaapi IdcBinExportText(idc_value_t* argument, idc_value_t*) {
  return DoExport(ExportMode::kText, argument[0].c_str(),
                  /*connection_string=*/"");
}
static const char kBinExportTextIdcArgs[] = {VT_STR, 0};
static const ext_idcfunc_t kBinExportTextIdcFunc = {
    "BinExportText", IdcBinExportText, kBinExportTextIdcArgs, nullptr, 0,
    EXTFUN_BASE};

error_t idaapi IdcBinExportStatistics(idc_value_t* argument, idc_value_t*) {
  return DoExport(ExportMode::kStatistics, argument[0].c_str(),
                  /*connection_string=*/"");
}
static const char kBinExportStatisticsIdcArgs[] = {VT_STR, 0};
static const ext_idcfunc_t kBinExportStatisticsIdcFunc = {
    "BinExportStatistics",
    IdcBinExportStatistics,
    kBinExportStatisticsIdcArgs,
    nullptr,
    0,
    EXTFUN_BASE};

error_t idaapi IdcBinExportSql(idc_value_t* argument, idc_value_t*) {
#ifdef ENABLE_POSTGRESQL
  if (argument[0].vtype != VT_STR || argument[1].vtype != VT_LONG ||
      argument[2].vtype != VT_STR || argument[3].vtype != VT_STR ||
      argument[4].vtype != VT_STR || argument[5].vtype != VT_STR) {
    LOG(INFO) << "Error (BinExportSql): required arguments are missing or "
                 "have the wrong type.";
    LOG(INFO) << "Usage:";
    LOG(INFO) << "  BinExportSql('host', port, 'database', 'schema', 'user', "
                 "'password')";
    return -1;
  }
  std::string connection_string = absl::StrCat(
      "host='", argument[0].c_str(), "' port='", argument[1].num, "' dbname='",
      argument[2].c_str(), "' user='", argument[4].c_str(), "' password='",
      argument[5].c_str(), "'");
  if (DoExport(ExportMode::kSql, argument[3].c_str(), connection_string) ==
      -1) {
    return -1;
  }
  return eOk;
#else
  LOG(INFO) << "Error (BinExportSql): PostgreSQL export not built-in, "
               "recompile with ENABLE_POSTGRESQL";
  return -1;
#endif
}
static const char kBinExportSqlIdcArgs[] = {VT_STR /* Host */,
                                            VT_LONG /* Port */,
                                            VT_STR /* Database */,
                                            VT_STR /* Schema */,
                                            VT_STR /* User */,
                                            VT_STR /* Password */,
                                            0};
static const ext_idcfunc_t kBinExportSqlIdcFunc = {
    "BinExportSql", IdcBinExportSql, kBinExportSqlIdcArgs, nullptr, 0,
    EXTFUN_BASE};

#ifdef ENABLE_POSTGRESQL
// Builds a database connection string from the plugin arguments given on the
// command-line. Note: This function does not escape any of the strings it gets
// passed in.
std::string GetConnectionStringFromArguments() {
  // See section 32.1.1.1. ("Keyword/Value Connection Strings") at
  // https://www.postgresql.org/docs/9.6/static/libpq-connect.html
  return absl::StrCat("host='", GetArgument("Host"), "' user='",
                      GetArgument("User"), "' password='",
                      GetArgument("Password"), "' port='", GetArgument("Port"),
                      "' dbname='" + GetArgument("Database") + "'");
}
#endif

ssize_t idaapi UiHook(void*, int event_id, va_list arguments) {
  if (event_id != ui_ready_to_run) {
    return 0;
  }

  Instruction::SetBitness(GetArchitectureBitness());

  // If IDA was invoked with -OBinExportAutoAction:<action>, wait for auto
  // analysis to finish, then invoke the requested action and exit.
  const std::string auto_action = GetArgument("AutoAction");
  if (auto_action.empty()) {
    return 0;
  }
  auto_wait();

#ifdef ENABLE_POSTGRESQL
  if (absl::EqualsIgnoreCase(auto_action, kBinExportSqlIdcFunc.name)) {
    DoExport(ExportMode::kSql, GetArgument("Schema"),
             GetConnectionStringFromArguments());
  } else  // NOLINT(readability/braces)
#endif
      if (absl::EqualsIgnoreCase(auto_action, kBinExportBinaryIdcFunc.name)) {
    DoExport(ExportMode::kBinary, GetArgument("Module"),
             /*connection_string=*/"");
  } else if (absl::EqualsIgnoreCase(auto_action, kBinExportTextIdcFunc.name)) {
    DoExport(ExportMode::kText, GetArgument("Module"),
             /*connection_string=*/"");
  } else if (absl::EqualsIgnoreCase(auto_action,
                                    kBinExportStatisticsIdcFunc.name)) {
    DoExport(ExportMode::kStatistics, GetArgument("Module"),
             /*connection_string=*/"");
  } else {
    LOG(INFO) << "Invalid argument for AutoAction: " << auto_action;
  }

  // Do not save the database on exit. This simply deletes the unpacked database
  // and prevents IDA >= 7.1 from segfaulting if '-A' is specified at the same
  // time.
  set_database_flag(DBFL_KILL);
  qexit(0);

  return 0;  // Not reached
}

Plugin::LoadStatus Plugin::Init() {
  alsologtostderr_ =
      absl::AsciiStrToUpper(GetArgument("AlsoLogToStdErr")) == "TRUE";
  if (auto status = InitLogging(LoggingOptions{}
                                    .set_alsologtostderr(alsologtostderr_)
                                    .set_log_filename(GetArgument("LogFile")),
                                absl::make_unique<IdaLogSink>());
      !status.ok()) {
    LOG(INFO) << "Error initializing logging, skipping BinExport plugin";
    return PLUGIN_SKIP;
  }

  const auto heuristic = GetArgument("X86NoReturnHeuristic");
  if (!heuristic.empty()) {
    // If unset, this leaves the default value
    x86_noreturn_heuristic_ = absl::AsciiStrToUpper(heuristic) == "TRUE";
  }

  LOG(INFO) << kBinExportName << " " << kBinExportDetailedVersion << ", "
            << kBinExportCopyright;

  addon_info_t addon_info;
  addon_info.id = "com.google.binexport";
  addon_info.name = kBinExportName;
  addon_info.producer = "Google";
  addon_info.version = kBinExportDetailedVersion;
  addon_info.url = "https://github.com/google/binexport";
  addon_info.freeform = kBinExportCopyright;
  register_addon(&addon_info);

  if (!hook_to_notification_point(HT_UI, UiHook, /*user_data=*/nullptr)) {
    LOG(INFO) << "Internal error: hook_to_notification_point() failed";
    return PLUGIN_SKIP;
  }

  if (!add_idc_func(kBinExportSqlIdcFunc) ||
      !add_idc_func(kBinExportBinaryIdcFunc) ||
      !add_idc_func(kBinExportTextIdcFunc) ||
      !add_idc_func(kBinExportStatisticsIdcFunc)) {
    LOG(INFO) << "Error registering IDC extension, skipping BinExport plugin";
    return PLUGIN_SKIP;
  }

  return PLUGIN_KEEP;
}

void Plugin::Terminate() {
  unhook_from_notification_point(HT_UI, UiHook, /*user_data=*/nullptr);
  ShutdownLogging();
}

bool Plugin::Run(size_t argument) {
  if (strlen(get_path(PATH_TYPE_IDB)) == 0) {
    info("Please open an IDB first.");
    return false;
  }

  LOG_IF(INFO, !GetArchitectureName())
      << "Warning: Exporting for unknown CPU architecture (Id: " << ph.id
      << ", " << GetArchitectureBitness() << "-bit)";
  try {
    if (argument) {
      std::string connection_string;
#ifdef ENABLE_POSTGRESQL
      std::string module;
      if (!GetArgument("Host").empty()) {
        connection_string = GetConnectionStringFromArguments();
        module = GetArgument("Schema");
      } else {
        module = GetArgument("Module");
      }
#else
      std::string module = GetArgument("Module");
#endif

      DoExport(static_cast<ExportMode>(argument), module, connection_string);
    } else {
      ask_form(GetDialog(), ButtonBinaryExport, ButtonTextExport,
               ButtonStatisticsExport);
    }
  } catch (const std::exception& error) {
    LOG(INFO) << "export cancelled: " << error.what();
    warning("export cancelled: %s\n", error.what());
  }
  return true;
}

}  // namespace security::binexport

using security::binexport::Plugin;

plugin_t PLUGIN = {
    IDP_INTERFACE_VERSION,
    PLUGIN_FIX,  // Plugin flags
    []() { return Plugin::instance()->Init(); },
    []() { Plugin::instance()->Terminate(); },
    [](size_t argument) { return Plugin::instance()->Run(argument); },
    Plugin::kComment,  // Statusline text
    nullptr,           // Multi-line help about the plugin, unused
    security::binexport::kBinExportName,  // Preferred short name of the plugin
    Plugin::kHotKey                       // Preferred hotkey to run the plugin
};
