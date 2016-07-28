// Copyright 2011-2016 Google Inc. All Rights Reserved.
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

#include <chrono>  // NOLINT
#include <iomanip>
#include <sstream>

#include "third_party/zynamics/binexport/ida/pro_forward.h"  // NOLINT
#include <expr.hpp>                                          // NOLINT
#include <ida.hpp>                                           // NOLINT
#include <idp.hpp>                                           // NOLINT
#include <kernwin.hpp>                                       // NOLINT
#include <loader.hpp>                                        // NOLINT

#include "base/logging.h"
#include "base/stringprintf.h"
#include "strings/strutil.h"
#include "third_party/zynamics/binexport/binexport2_writer.h"
#include "third_party/zynamics/binexport/call_graph.h"
#include "third_party/zynamics/binexport/chain_writer.h"
#include "third_party/zynamics/binexport/database_writer.h"
#include "third_party/zynamics/binexport/dump_writer.h"
#include "third_party/zynamics/binexport/entry_point.h"
#include "third_party/zynamics/binexport/filesystem_util.h"
#include "third_party/zynamics/binexport/flow_analyzer.h"
#include "third_party/zynamics/binexport/flow_graph.h"
#include "third_party/zynamics/binexport/hex_codec.h"
#include "third_party/zynamics/binexport/ida/digest.h"
#include "third_party/zynamics/binexport/ida/log.h"
#include "third_party/zynamics/binexport/ida/names.h"
#include "third_party/zynamics/binexport/instruction.h"
#include "third_party/zynamics/binexport/statistics_writer.h"
#include <version.h>  // NOLINT
#include "third_party/zynamics/binexport/virtual_memory.h"

std::string GetArgument(const char* name) {
  const char* option = get_plugin_options(StrCat("Exporter", name).c_str());
  return option ? option : "";
}

static const char kBinExportSql[] = "BinExport2Sql" BINEXPORT_RELEASE;
static const char kBinExportDiff[] = "BinExport2Diff" BINEXPORT_RELEASE;
static const char kBinExportText[] = "BinExport2Text" BINEXPORT_RELEASE;
static const char kBinExportStatistics[] =
    "BinExport2Statistics" BINEXPORT_RELEASE;
static const char kName[] = "BinExport " BINEXPORT_RELEASE;
static const char kCopyright[] =
    "(c)2004-2011 zynamics GmbH, (c)2011-2016 Google Inc.";
static const char kComment[] =
    "Export to SQL RE-DB, BinDiff binary or text dump";
static const char kHotKey[] = "";

enum ExportMode { kDatabase = 1, kBinary = 2, kText = 3, kStatistics = 4 };

class WaitBox {
 public:
  explicit WaitBox(const std::string& message) {
    show_wait_box("%s", message.c_str());
  }

  ~WaitBox() { hide_wait_box(); }
};

std::string GetDataForHash() {
  std::string data;
  for (segment_t* segment = get_first_seg();
       segment != 0 && data.size() < (32 << 20 /* 32 MiB */);
       segment = get_next_seg(segment->startEA)) {
    // Truncate segments longer than 1MB so we don't produce too long a string.
    for (ea_t address = segment->startEA;
         address < std::min(segment->endEA, segment->startEA + (1 << 20));
         ++address) {
      if (getFlags(address)) {
        // check whether address is loaded
        data += get_byte(address);
      }
    }
  }
  return data;
}

std::string GetDefaultName(ExportMode mode) {
  std::string new_extension;
  switch (mode) {
    case ExportMode::kBinary:
      new_extension = ".BinExport";
      break;
    case ExportMode::kText:
      new_extension = ".dump";
      break;
    case ExportMode::kStatistics:
      new_extension = ".statistics";
      break;
  }
  return ReplaceFileExtension(GetModuleName(), new_extension);
}

void ExportDatabase(ChainWriter& writer) {
  LOG(INFO) << GetModuleName() << ": starting export";
  WaitBox waitBox("HIDECANCEL\nexporting database...");
  auto time_start(std::chrono::system_clock::now());
  EntryPoints entry_points;
  {
    EntryPointAdder entry_point_adder(&entry_points, "function chunks");
    for (size_t i = 0; i < get_fchunk_qty(); ++i) {
      if (const func_t* idaFunc = getn_fchunk(i)) {
        entry_point_adder.Add(idaFunc->startEA,
                              (idaFunc->flags & FUNC_TAIL)
                                  ? EntryPoint::Source::FUNCTION_CHUNK
                                  : EntryPoint::Source::FUNCTION_PROLOGUE);
      }
    }
  }

  // Add imported functions (so we won't miss imported but not referenced
  // functions).
  const auto modules(InitModuleMap());
  {
    EntryPointAdder entry_point_adder(&entry_points, "calls");
    for (const auto& module : modules) {
      entry_point_adder.Add(module.first, EntryPoint::Source::CALL_TARGET);
    }
  }

  Instructions instructions;
  FlowGraph flow_graph;
  CallGraph call_graph;
  AnalyzeFlowIda(&entry_points, &modules, &writer, &instructions, &flow_graph,
                 &call_graph);

  std::chrono::duration<double> processing_time(
      std::chrono::system_clock::now() - time_start);
  LOG(INFO) << GetModuleName() << ": exported "
            << flow_graph.GetFunctions().size() << " functions with "
            << instructions.size() << " instructions in "
            << StringPrintf("%.2fs", processing_time.count());
}

int ExportDatabase(const std::string& schema_name,
                   const std::string& connection_string) {
  ChainWriter writer;
  try {
    const std::string data(GetDataForHash());
    auto database_writer(std::make_shared<DatabaseWriter>(
        schema_name /* Database */, GetModuleName(), 0 /* Module id */,
        EncodeHex(Md5(data)), EncodeHex(Sha1(data)), GetArchitectureName(),
        GetImageBase(), kName /* Version string */,
        !connection_string.empty() ? connection_string
                                   : GetArgument("ConnectionString")));
    int query_size = 0;
    database_writer->set_query_size(
        safe_strto32(GetArgument("QuerySize"), &query_size)
            ? query_size
            : 32 << 20 /* 32 MiB */);
    writer.AddWriter(database_writer);

    ExportDatabase(writer);
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

int ExportBinary(const std::string& filename) {
  try {
    const std::string hash(Sha1(GetDataForHash()));
    ChainWriter writer;
    writer.AddWriter(std::make_shared<BinExport2Writer>(
        ReplaceFileExtension(filename, ""), GetModuleName(), EncodeHex(hash),
        GetArchitectureName()));
    ExportDatabase(writer);
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

void idaapi ButtonBinaryExport(TView** /* fields */, int) {
  std::string name = GetDefaultName(ExportMode::kBinary);
  const char* filename =
      askfile_c(true, name.c_str(), "Please specify result filename");
  if (!filename) {
    return;
  }

  if (FileExists(filename) &&
        askyn_c(0, "'%s' already exists - overwrite?", filename) != 1) {
      return;
  }

  ExportBinary(filename);
}

int ExportText(const std::string& filename) {
  try {
    std::ofstream file(filename.c_str());
    ChainWriter writer;
    writer.AddWriter(std::make_shared<DumpWriter>(file));
    ExportDatabase(writer);
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

void idaapi ButtonTextExport(TView** /* fields */, int) {
  std::string name = GetDefaultName(ExportMode::kText);
  const char* filename =
      askfile_c(true, name.c_str(), "Please specify result filename");
  if (!filename) {
    return;
  }

  if (FileExists(filename) &&
        askyn_c(0, "'%s' already exists - overwrite?", filename) != 1) {
    return;
  }

  ExportText(filename);
}

int ExportStatistics(const std::string& filename) {
  try {
    std::ofstream file(filename.c_str());
    ChainWriter writer;
    writer.AddWriter(std::make_shared<StatisticsWriter>(file));
    ExportDatabase(writer);
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

void idaapi ButtonStatisticsExport(TView** /* fields */, int) {
  std::string name = GetDefaultName(ExportMode::kStatistics);
  const char* filename =
      askfile_c(true, name.c_str(), "Please specify result filename");
  if (!filename) {
    return;
  }

  if (FileExists(filename) &&
        askyn_c(0, "'%s' already exists - overwrite?", filename) != 1) {
    return;
  }

  ExportStatistics(filename);
}

const char* GetDialog() {
  static const std::string kDialog = StrCat(
      "STARTITEM 0\n"
      "BUTTON YES Close\n"  // This is actually the OK button
      "BUTTON CANCEL NONE\n"
      "HELP\n"
      "\n"
      "ENDHELP\n",
      kName,
      "\n\n\n"
      "<BinExport v2 Binary Export:B:1:30:::>\n\n"
      "<Text Dump Export:B:1:30:::>\n\n"
      "<Statistics Export:B:1:30:::>\n\n");
  return kDialog.c_str();
}

int DoExport(ExportMode mode, std::string name,
             const std::string& connection_string) {
  if (name.empty()) {
    try {
      name = GetTempDirectory("BinExport", true);
    } catch (...) {
      name = "./";
    }
  }
  if (IsDirectory(name) && connection_string.empty()) {
    name += "/" + GetDefaultName(mode);
  }

  Instruction::SetBitness(GetArchitectureBitness());
  switch (mode) {
    case ExportMode::kDatabase:
      return ExportDatabase(name, connection_string);
    case ExportMode::kBinary:
      return ExportBinary(name);
    case ExportMode::kText:
      return ExportText(name);
    case ExportMode::kStatistics:
      return ExportStatistics(name);
    default:
      LOG(INFO) << "Error: Invalid export: " << mode;
      return -1;
  }
}

static const char kBinExport2DiffIdcArgs[] = {VT_STR2, 0};
error_t idaapi IdcBinExport2Diff(idc_value_t* argument, idc_value_t*) {
  return DoExport(ExportMode::kBinary, std::string(argument[0].c_str()),
                  "" /* Connection string */);
}

static const char kBinExport2TextIdcArgs[] = {VT_STR2, 0};
error_t idaapi IdcBinExport2Text(idc_value_t* argument, idc_value_t*) {
  return DoExport(ExportMode::kText, std::string(argument[0].c_str()),
                  "" /* Connection string */);
}

static const char kBinExport2StatisticsIdcArgs[] = {VT_STR2, 0};
error_t idaapi IdcBinExport2Statistics(idc_value_t* argument, idc_value_t*) {
  return DoExport(ExportMode::kStatistics, std::string(argument[0].c_str()),
                  "" /* Connection string */);
}

static const char kBinExport2SqlIdcArgs[] = {VT_STR2 /* Host */,
                                             VT_LONG /* Port */,
                                             VT_STR2 /* Database */,
                                             VT_STR2 /* Schema */,
                                             VT_STR2 /* User */,
                                             VT_STR2 /* Password */,
                                             0};
error_t idaapi IdcBinExport2Sql(idc_value_t* argument, idc_value_t*) {
  if (argument[0].vtype != VT_STR2 || argument[1].vtype != VT_LONG ||
      argument[2].vtype != VT_STR2 || argument[3].vtype != VT_STR2 ||
      argument[4].vtype != VT_STR2 || argument[5].vtype != VT_STR2) {
    LOG(INFO) << "Error (BinExport2Sql): required arguments are missing or "
                 "have the wrong type.";
    LOG(INFO) << "Please call like so:";
    LOG(INFO) << "BinExport2Sql('host', port #, 'database', 'schema', 'user', "
                 "'password')";
    return -1;
  }
  std::stringstream connection_string;
  connection_string << "host='" << argument[0].c_str() << "' port='"
                    << argument[1].num << "' dbname='" << argument[2].c_str()
                    << "' user='" << argument[4].c_str() << "' password='"
                    << argument[5].c_str() << "'";
  if (DoExport(ExportMode::kDatabase, argument[3].c_str(),
               connection_string.str()) == -1) {
    return -1;
  }
  return eOk;
}

int idaapi PluginInit() {
  LoggingOptions options;
  options.set_alsologtostderr(ToUpper(GetArgument("AlsoLogToStdErr")) ==
                              "TRUE");
  options.set_log_filename(GetArgument("LogFile"));
  if (!InitLogging(options)) {
    LOG(INFO) << "Error initializing logging, skipping BinExport plugin";
    return PLUGIN_SKIP;
  }

  LOG(INFO) << kName << " (@" << BINEXPORT_REVISION << ", " << __DATE__
#ifndef NDEBUG
            << ", debug build"
#endif
            << "), " << kCopyright;

  addon_info_t addon_info;
  addon_info.cb = sizeof(addon_info_t);
  addon_info.id = "com.google.binexport";
  addon_info.name = kName;
  addon_info.producer = "Google";
  addon_info.version = BINEXPORT_RELEASE " @ " BINEXPORT_REVISION;
  addon_info.url = "https://github.com/google/binexport";
  addon_info.freeform = kCopyright;
  register_addon(&addon_info);

  if (!set_idc_func_ex(kBinExportDiff, IdcBinExport2Diff,
                       kBinExport2DiffIdcArgs, EXTFUN_BASE) ||
      !set_idc_func_ex(kBinExportSql, IdcBinExport2Sql, kBinExport2SqlIdcArgs,
                       EXTFUN_BASE) ||
      !set_idc_func_ex(kBinExportText, IdcBinExport2Text,
                       kBinExport2TextIdcArgs, EXTFUN_BASE) ||
      !set_idc_func_ex(kBinExportStatistics, IdcBinExport2Statistics,
                       kBinExport2StatisticsIdcArgs, EXTFUN_BASE)) {
    LOG(INFO) << "Error registering IDC extension, skipping BinExport plugin";
    return PLUGIN_SKIP;
  }

  return PLUGIN_KEEP;
}

void idaapi PluginTerminate() {
  ShutdownLogging();
}

void idaapi PluginRun(int argument) {
  if (strlen(database_idb) == 0) {
    info("Please open an IDB first.");
    return;
  }

  try {
    GetArchitectureName();
  } catch (const std::exception&) {
    LOG(INFO) << "Warning: Exporting for unknown CPU architecture (Id: "
              << ph.id << ", " << GetArchitectureBitness() << "-bit)";
  }
  try {
    if (argument) {
      std::string connection_string;
      std::string module;
      if (!GetArgument("Host").empty()) {
        connection_string =
            "host='" + GetArgument("Host") + "' user='" + GetArgument("User") +
            "' password='" + GetArgument("Password") + "' port='" +
            GetArgument("Port") + "' dbname='" + GetArgument("Database") + "'";
        module = GetArgument("Schema");
      } else {
        module = GetArgument("Module");
      }

      DoExport(static_cast<ExportMode>(argument), module, connection_string);
    } else {
      AskUsingForm_c(GetDialog(), ButtonBinaryExport, ButtonTextExport,
                     ButtonStatisticsExport);
    }
  } catch (const std::exception& error) {
    LOG(INFO) << "export cancelled: " << error.what();
    warning("export cancelled: %s\n", error.what());
  }
}

#ifdef OPENSSL_IS_BORINGSSL
extern "C" {
// For compatibility with BoringSSL, which does not define these.
void OPENSSL_config(const char *config_name) {}
void OPENSSL_no_config(void) {}
}  // extern "C"
#endif

extern "C" {
plugin_t PLUGIN = {
    IDP_INTERFACE_VERSION,
    PLUGIN_FIX,       // Plugin flags
    PluginInit,       // Initialize
    PluginTerminate,  // Terminate
    PluginRun,        // Invoke plugin
    kComment,         // Statusline text
    nullptr,          // Multi-line help about the plugin, unused
    kName,            // Preferred short name of the plugin.
    kHotKey           // Preferred hotkey to run the plugin.
};
}  // extern "C"
