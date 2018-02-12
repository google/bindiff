#include <cstdint>
#include <cstdio>
#include <limits>
#include <map>
#include <memory>
#include <sstream>
#include <thread>

#include <pro.h>        // NOLINT
#include <bytes.hpp>    // NOLINT
#include <diskio.hpp>   // NOLINT
#include <enum.hpp>     // NOLINT
#include <expr.hpp>     // NOLINT
#include <frame.hpp>    // NOLINT
#include <funcs.hpp>    // NOLINT
#include <ida.hpp>      // NOLINT
#include <idp.hpp>      // NOLINT
#include <kernwin.hpp>  // NOLINT
#include <loader.hpp>   // NOLINT
#include <nalt.hpp>     // NOLINT
#include <name.hpp>     // NOLINT
#include <struct.hpp>   // NOLINT
#include <ua.hpp>       // NOLINT
#include <xref.hpp>     // NOLINT

#include <version.h>  // NOLINT
#include "base/logging.h"
#include "base/stringprintf.h"
#ifndef GOOGLE  // MOE:strip_line
#include "strings/strutil.h"
#endif  // MOE:strip_line
#include "third_party/absl/strings/escaping.h"
#include "third_party/zynamics/bindiff/call_graph_matching.h"
#include "third_party/zynamics/bindiff/change_classifier.h"
#include "third_party/zynamics/bindiff/database_writer.h"
#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/bindiff/flow_graph_matching.h"
#include "third_party/zynamics/bindiff/groundtruth_writer.h"
#include "third_party/zynamics/bindiff/ida/results.h"
#include "third_party/zynamics/bindiff/ida/visual_diff.h"
#include "third_party/zynamics/bindiff/log_writer.h"
#include "third_party/zynamics/bindiff/matching.h"
#include "third_party/zynamics/bindiff/utility.h"
#include "third_party/zynamics/bindiff/xmlconfig.h"
#include "third_party/zynamics/binexport/filesystem_util.h"
#include "third_party/zynamics/binexport/ida/digest.h"
#include "third_party/zynamics/binexport/ida/log.h"
#include "third_party/zynamics/binexport/ida/ui.h"
#include "third_party/zynamics/binexport/timer.h"
#include "third_party/zynamics/binexport/types.h"

#include <google/protobuf/io/zero_copy_stream_impl.h>  // NOLINT
#if _MSC_VER
#define snprintf _snprintf
#endif  // _MSC_VER

static const char kBinExportVersion[] = "9";  // Exporter version to use.
static const char kName[] = "BinDiff 4.3";
static const char kComment[] =
    "Structural comparison of executable objects";  // Status line
static const char kHotKey[] = "CTRL-6";
static const char kCopyright[] =
    "(c)2004-2011 zynamics GmbH, (c)2011-2018 Google LLC.";

XmlConfig* g_config = nullptr;
bool g_init_done = false;  // Used in PluginTerminate()

Results* g_results = nullptr;

enum ResultFlags {
  kResultsShowMatched = 1 << 0,
  kResultsShowStatistics = 1 << 1,
  kResultsShowPrimaryUnmatched = 1 << 2,
  kResultsShowSecondaryUnmatched = 1 << 3,
  kResultsShowAll = 0xffffffff
};

std::string GetArgument(const char* name) {
  const char* option = get_plugin_options(StrCat("BinDiff", name).c_str());
  return option ? option : "";
}

bool DoSaveResults();

std::string GetDataForHash() {
  std::string data;
  // 32MiB maximum size for hash data.
  for (segment_t* segment = get_first_seg();
       segment != 0 && data.size() < (32 << 20);
       segment = get_next_seg(segment->startEA)) {
    // truncate segments longer than 1MB so we don't produce too long a string
    for (ea_t address = segment->startEA;
         address < std::min(segment->endEA, segment->startEA + (1 << 20));
         ++address) {
      if (getFlags(address)) {  // check whether address is loaded
        data += get_byte(address);
      }
    }
  }
  return data;
}

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

bool EnsureIdb() {
  bool result = strlen(database_idb) > 0;
  if (!result) {
    info("Please open an IDB first.");
  }
  return result;
}

class ExporterThread {
 public:
  explicit ExporterThread(const std::string& temp_dir,
                          const std::string& idb_path)
      : success_(false),
        secondary_idb_path_(idb_path),
        secondary_temp_dir(JoinPath(temp_dir, "secondary")),
        idc_file_(JoinPath(temp_dir, "run_secondary.idc")) {
    RemoveAll(secondary_temp_dir);
    CreateDirectories(secondary_temp_dir);

    std::ofstream file(idc_file_.c_str());
    file << "#include <idc.idc>\n"
         << "static main()\n"
         << "{\n"
         << "\tBatch(0);\n"
         << "\tWait();\n"
         << "\tRunPlugin(\"zynamics_binexport_" << kBinExportVersion
         << "\", 2);\n"
         << "\tExit(0);\n"
         << "}\n";
  }

  void operator()() {
    std::string ida_exe(idadir(0));
    const bool is_64bit =
        StringPiece(ToUpper(secondary_idb_path_)).ends_with(".I64");

    std::string s_param;
    // We only support the Qt version.
#ifdef WIN32
    if (is_64bit) {
      ida_exe += "\\idaq64.exe";
    } else {
      ida_exe += "\\idaq.exe";
    }

    // Note: We only support IDA 6.8 or higher. The current quoting behavior
    //       on Windows was introduced in IDA 6.1. Previously, the quotes were
    //       not needed.
    s_param = "-S\"" + idc_file_ + "\"";
#else
    if (is_64bit) {
      ida_exe += "/idaq64";
    } else {
      ida_exe += "/idaq";
    }
    s_param = "-S" + idc_file_;
#endif
    std::vector<std::string> argv;
    argv.push_back(ida_exe);
    argv.push_back("-A");
    argv.push_back("-OExporterModule:" + secondary_temp_dir);
    argv.push_back(s_param);
    argv.push_back(secondary_idb_path_);

    success_ = SpawnProcess(argv, true /* Wait */, &status_message_);
  }

  bool success() const { return success_; }

  const std::string& status() const { return status_message_; }

 private:
  volatile bool success_;
  std::string status_message_;
  std::string secondary_idb_path_;
  std::string secondary_temp_dir;
  std::string idc_file_;
};

bool ExportIdbs() {
  if (!EnsureIdb()) {
    return false;
  }

  const std::string temp_dir(GetTempDirectory("BinDiff", /* create = */ true));

  const char* secondary_idb = askfile2_c(
      /* forsave = */ false, "*.idb;*.i64",
      StrCat("IDA Databases|*.idb;*.i64|All files|", kAllFilesFilter).c_str(),
      "Select Database");
  if (!secondary_idb) {
    return false;
  }

  const std::string primary_idb_path(database_idb /* IDA global variable */);
  std::string secondary_idb_path(secondary_idb);
  if (primary_idb_path == secondary_idb_path) {
    throw std::runtime_error(
        "You cannot open the same IDB file twice. Please copy and rename one if"
        " you want to diff against self.");
  } else if (ReplaceFileExtension(primary_idb_path, "") ==
             ReplaceFileExtension(secondary_idb_path, "")) {
    throw std::runtime_error(
        "You cannot open an idb and an i64 with the same base filename in the "
        "same directory. Please rename or move one of the files.");
  } else if (GetFileExtension(primary_idb_path) == ".idb" &&
             GetFileExtension(secondary_idb_path) == ".i64") {
    if (askyn_c(0,
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
    ExporterThread exporter(temp_dir, secondary_idb_path);
    std::thread thread(std::ref(exporter));

    std::string primary_temp_dir(JoinPath(temp_dir, "primary"));
    RemoveAll(primary_temp_dir);
    CreateDirectories(primary_temp_dir);

    char errbuf[MAXSTR];
    idc_value_t arg(primary_temp_dir.c_str());
    if (!Run(StrCat("BinExport2Diff", kBinExportVersion).c_str(),
             /* argsnum = */ 1, &arg, /* result = */ nullptr, errbuf,
             MAXSTR - 1)) {
      throw std::runtime_error(
          StrCat("Error: Export of the current database failed: ", errbuf));
    }

    thread.join();
    if (!exporter.success()) {
      throw std::runtime_error(
          StrCat("Failed to spawn second IDA instance: ", exporter.status()));
    }
  }

  return true;
}

uint32_t idaapi GetNumFixedPoints(void* /* unused */) {
  if (!g_results) {
    return 0;
  }
  return g_results->GetNumFixedPoints();
}

void DoVisualDiff(uint32_t index, bool call_graph_diff) {
  try {
    std::string message;
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
                             "C:\\Program Files\\zynamics\\BinDiff 4.3\\bin"),
        g_config->ReadString("/BinDiff/Gui/@server", "127.0.0.1"),
        static_cast<unsigned short>(
            g_config->ReadInt("/BinDiff/Gui/@port", 2000)),
        message, nullptr);
  } catch (const std::bad_alloc&) {
    LOG(INFO) << "Out-of-memory. Some extremely large binaries may require to "
                 "use the 64-bit command-line version of BinDiff.";
    warning(
        "Out-of-memory. Some extremely large binaries may require to use the "
        "64-bit command-line version of BinDiff.");
  } catch (const std::runtime_error& message) {
    LOG(INFO) << "Error while calling BinDiff GUI: " << message.what();
    warning("Error while calling BinDiff GUI: %s\n", message.what());
  } catch (...) {
    LOG(INFO) << "Unknown error while calling BinDiff GUI";
    warning("Unknown error while calling BinDiff GUI\n");
  }
}

void idaapi VisualDiffCallback(void* /* unused */, uint32_t index) {
  DoVisualDiff(index, /* call_graph_diff = */ false);
}

void idaapi VisualCallGraphDiffCallback(void* /* unused */, uint32_t index) {
  DoVisualDiff(index, /* call_graph_diff = */ true);
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

uint32_t idaapi PortCommentsSelection(void* /* unused */, uint32_t index) {
  try {
    return g_results->PortComments(index, false /* as_external */);
  } catch (const std::exception& message) {
    LOG(INFO) << "Error: " << message.what();
    warning("Error: %s\n", message.what());
  } catch (...) {
    LOG(INFO) << "Unknown error while porting comments";
    warning("Unknown error while porting comments\n");
  }
  return 0;
}

uint32_t idaapi ConfirmMatch(void* /* unused */, uint32_t index) {
  try {
    return g_results->ConfirmMatch(index);
  } catch (const std::exception& message) {
    LOG(INFO) << "Error: " << message.what();
    warning("Error: %s\n", message.what());
  } catch (...) {
    LOG(INFO) << "Unknown error while confirming match";
    warning("Unknown error while confirming match\n");
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

void idaapi GetUnmatchedPrimaryDescription(void* /* unused */, uint32_t index,
                                           char* const* line) {
  if (!g_results) {
    return;
  }
  g_results->GetUnmatchedDescriptionPrimary(index, line);
}

void idaapi GetUnmatchedSecondaryDescription(void* /* unused */, uint32_t index,
                                             char* const* line) {
  if (!g_results) {
    return;
  }
  g_results->GetUnmatchedDescriptionSecondary(index, line);
}

uint32_t idaapi GetNumStatistics(void* /* unused */) {
  if (!g_results) {
    return 0;
  }
  return g_results->GetNumStatistics();
}

void idaapi GetStatisticsDescription(void* /* unused */, uint32_t index,
                                     char* const* line) {
  if (!g_results) {
    return;
  }
  g_results->GetStatisticsDescription(index, line);
}

void idaapi GetMatchDescription(void* /* unused */, uint32_t index,
                                char* const* line) {
  if (!g_results) {
    return;
  }
  g_results->GetMatchDescription(index, line);
}

uint32_t idaapi DeleteMatch(void* /* unused */, uint32_t index) {
  if (!g_results) {
    return 0;
  }

  try {
    return g_results->DeleteMatch(index);
  } catch (const std::exception& message) {
    LOG(INFO) << "Error: " << message.what();
    warning("Error: %s\n", message.what());
  } catch (...) {
    LOG(INFO) << "Unknown error while deleting match";
    warning("Unknown error while deleting match\n");
  }
  return 0;
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

void idaapi jumpToMatchAddress(void* /* unused */, uint32_t index) {
  if (!g_results) return;

  jumpto(static_cast<ea_t>(g_results->GetMatchPrimaryAddress(index)));
}

// Need to close forms if we are calling choose2 from askUsingForm_c callback
// and the choose2 window is still open. If we don't close it first IDA hangs
// in a deadlock.
void CloseForm(const char* name) {
  TForm* form = find_tform(name);
  if (form) {
    close_tform(form, FORM_SAVE);
  }
}

void SaveAndDiscardResults() {
  if (g_results && g_results->IsDirty()) {
    const int answer(askyn_c(0,
                             "HIDECANCEL\nCurrent diff results have not been"
                             " saved - save before closing?"));
    if (answer == 1) {  // Yes
      DoSaveResults();
    }
  }

  delete g_results;
  g_results = 0;
}

int idaapi ProcessorHook(void*, int event_id, va_list /*arguments*/) {
  switch (event_id) {
    case processor_t::term: {
      if (!(database_flags & DBFL_KILL) && g_results) {
        g_results->MarkPortedCommentsInDatabase();
      }
    } break;
    case processor_t::closebase: {
      SaveAndDiscardResults();
    } break;
  }
  return 0;
}

int idaapi UiHook(void*, int eventId, va_list arguments) {
  switch (eventId) {
    case ui_get_chooser_item_attrs: {
      if (g_results) {
        // pop user supplied void* from arguments first (but ignore, since
        // this may go stale)
        if (reinterpret_cast<uint32_t>(va_arg(arguments, void*)) != 0x00000001) {
          // Magic value so we don't color IDA's windows.
          return 0;
        }
        const uint32_t index = va_arg(arguments, uint32_t);
        if (chooser_item_attrs_t* attributes =
                va_arg(arguments, chooser_item_attrs_t*)) {
          attributes->color = g_results->GetColor(index);
        }
      }
    } break;
    case ui_preprocess: {
      const char* name(va_arg(arguments, const char*));
      if (strcmp(name, "LoadFile") == 0 || strcmp(name, "NewFile") == 0 ||
          strcmp(name, "CloseBase") == 0 || strcmp(name, "Quit") == 0) {
        if (g_results && g_results->IsDirty()) {
          const int answer(askyn_c(0,
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
        g_results = 0;
        // refreshing the choosers here is important as after our
        // "save results?" confirmation dialog IDA will open its own
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
    } break;
  }
  return 0;
}

void ShowResults(Results* results, const ResultFlags flags = kResultsShowAll) {
  if (!results || results != g_results) {
    throw std::runtime_error("trying to show invalid results");
  }

  results->CreateIndexedViews();

  if (flags & kResultsShowMatched) {
    if (!find_tform("Matched Functions")) {
      static const int widths[] = {5, 3, 3, 10, 30, 10, 30, 1, 30,
                                   5, 5, 5, 6,  6,  6,  5,  5, 5};
      static const char* popups[] =  // insert, delete, edit, refresh
          {"Delete Match", "Delete Match", "View Flowgraphs", "Refresh"};
      CloseForm("Matched Functions");
      choose2(CH_MULTI | CH_ATTRS, -1, -1, -1, -1,
              // Magic value to differentiate our window from IDA's.
              reinterpret_cast<void*>(0x00000001),
              static_cast<int>(sizeof(widths) / sizeof(widths[0])), widths,
              &GetNumFixedPoints, &GetMatchDescription, "Matched Functions",
              -1,                      // icon
              static_cast<uint32_t>(1),  // default
              &DeleteMatch,            // delete callback
              0,                       // insert callback
              0,                       // update callback
              &VisualDiffCallback,     // edit callback (visual diff)
              &jumpToMatchAddress,     // enter callback (jump to)
              nullptr,                 // destroy callback
              popups,                  // popups (insert, delete, edit, refresh)
              0);

      // not currently implemented/used
      // add_chooser_command( "Matched Functions", "View Call graphs",
      //    &VisualCallGraphDiffCallback, -1, -1, CHOOSER_POPUP_MENU );
      // @bug: IDA will forget the selection state if CHOOSER_MULTI_SELECTION is
      //       set. See fogbugz 2946 and my mail to hex rays.
      // @bug: IDA doesn't allow adding hotkeys for custom menu entries. See
      //       fogbugz 2945 and my mail to hex rays.
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
  }

  if (flags & kResultsShowStatistics) {
    if (!find_tform("Statistics")) {
      static const int widths[] = {30, 12};
      static const char* popups[] = {0, 0, 0, 0};
      CloseForm("Statistics");
      choose2(0, -1, -1, -1, -1, static_cast<void*>(0),
              static_cast<int>(sizeof(widths) / sizeof(widths[0])), widths,
              &GetNumStatistics, &GetStatisticsDescription, "Statistics",
              -1,                      // icon
              static_cast<uint32_t>(1),  // default
              0,                       // delete callback
              0,                       // new callback
              0,                       // update callback
              0,                       // edit callback
              0,                       // enter callback
              nullptr,                 // destroy callback
              popups,                  // popups (insert, delete, edit, refresh)
              0);
    } else {
      refresh_chooser("Statistics");
    }
  }

  if (flags & kResultsShowPrimaryUnmatched) {
    if (!find_tform("Primary Unmatched")) {
      static const int widths[] = {10, 30, 5, 6, 5};
      static const char* popups[] = {0, 0, 0, 0};
      CloseForm("Primary Unmatched");
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
    } else {
      refresh_chooser("Primary Unmatched");
    }
  }

  if (flags & kResultsShowSecondaryUnmatched) {
    if (!find_tform("Secondary Unmatched")) {
      static const int widths[] = {10, 30, 5, 6, 5};
      static const char* popups[] = {0, 0, 0, 0};
      CloseForm("Secondary Unmatched");
      choose2(0, -1, -1, -1, -1, static_cast<void*>(0),
              sizeof(widths) / sizeof(widths[0]), widths,
              &GetNumUnmatchedSecondary, &GetUnmatchedSecondaryDescription,
              "Secondary Unmatched", -1,  // icon
              1,                          // default
              0,                          // delete callback
              0,                          // new callback
              0,                          // update callback
              0,                          // edit callback
              0,                          // enter callback
              nullptr,                    // destroy callback
              popups,  // popups (insert, delete, edit, refresh)
              0);

      add_chooser_command("Secondary Unmatched", "Add Match",
                          &AddMatchSecondary, -1, -1, CHOOSER_POPUP_MENU);
#ifdef WIN32
      add_chooser_command("Secondary Unmatched", "Copy Address",
                          &CopySecondaryAddressUnmatched, -1, -1,
                          CHOOSER_POPUP_MENU);
#endif
    } else {
      refresh_chooser("Secondary Unmatched");
    }
  }
}

bool idaapi MenuItemShowResultsCallback(void* user_data) {
  if (!g_results) {
    vinfo("Please perform a diff first", 0);
    return false;
  }

  const ResultFlags flags =
      static_cast<ResultFlags>(reinterpret_cast<int>(user_data));
  ShowResults(g_results, flags);
  return true;
}

// deletes all nodes from callgraph (and corresponding flow graphs) that are
// not within the specified range
void FilterFunctions(ea_t start, ea_t end, CallGraph* call_graph,
                     FlowGraphs* flow_graphs,
                     FlowGraphInfos* flow_graph_infos) {
  for (auto i = flow_graphs->begin(); i != flow_graphs->end();) {
    const FlowGraph* flow_graph = *i;
    if (flow_graph->GetEntryPointAddress() < start ||
        flow_graph->GetEntryPointAddress() > end) {
      flow_graph_infos->erase(flow_graph->GetEntryPointAddress());
      delete flow_graph;
      // GNU implementation of erase does not return an iterator.
      flow_graphs->erase(i++);
    } else {
      ++i;
    }
  }
  call_graph->DeleteVertices(start, end);
}

bool Diff(ea_t start_address_source, ea_t end_address_source,
          ea_t start_address_target, ea_t end_address_target) {
  Timer<> timer;
  try {
    if (!ExportIdbs()) {
      return false;
    }
  } catch (...) {
    delete g_results;
    g_results = 0;
    throw;
  }

  LOG(INFO) << StringPrintf("%.2fs", timer.elapsed())
            << " seconds for exports...";
  LOG(INFO) << "Diffing address range primary("
            << StringPrintf(HEX_ADDRESS, start_address_source) << " - "
            << StringPrintf(HEX_ADDRESS, end_address_source)
            << ") vs secondary("
            << StringPrintf(HEX_ADDRESS, start_address_target) << " - "
            << StringPrintf(HEX_ADDRESS, end_address_target) << ").";
  timer.restart();

  WaitBox wait_box("Performing diff...");
  g_results = new Results();
  const auto temp_dir(GetTempDirectory("BinDiff", /* create = */ true));
  const auto filename1(FindFile(JoinPath(temp_dir, "primary"), ".BinExport"));
  const auto filename2(FindFile(JoinPath(temp_dir, "secondary"), ".BinExport"));
  if (filename1.empty() || filename2.empty()) {
    throw std::runtime_error(
        "Export failed. Is the secondary idb opened in another IDA instance? "
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
  LOG(INFO) << StringPrintf("%.2fs", timer.elapsed())
            << " seconds for matching.";

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
  } catch (const std::bad_alloc&) {
    LOG(INFO) << "Out-of-memory. Some extremely large binaries may require to "
                 "use the 64-bit command-line version of BinDiff.";
    warning(
        "Out-of-memory. Some extremely large binaries may require to use the "
        "64-bit command-line version of BinDiff.");
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
          askyn_c(0,
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

      if (!AskUsingForm_c(kDialog, &start_address_source, &end_address_source,
                          &start_address_target, &end_address_target)) {
        return false;
      }
    }
    return Diff(start_address_source, end_address_source, start_address_target,
                end_address_target);
  } catch (const std::bad_alloc&) {
    LOG(INFO) << "Out-of-memory. Some extremely large binaries may require to "
                 "use the 64-bit command-line version of BinDiff.";
    warning(
        "Out-of-memory. Some extremely large binaries may require to use the "
        "64-bit command-line version of BinDiff.");
  } catch (const std::exception& message) {
    LOG(INFO) << "Error while diffing: " << message.what();
    warning("Error while diffing: %s\n", message.what());
  } catch (...) {
    LOG(INFO) << "Unknown error while diffing.";
    warning("Unknown error while diffing.");
  }
  return false;
}

bool idaapi MenuItemDiffDatabaseFilteredCallback(void* /* unused */) {
  // Refresh screen on successful diff
  return DoDiffDatabase(/* filtered = */ true);
}

void idaapi ButtonDiffDatabaseFilteredCallback(TView* fields[], int) {
  if (DoDiffDatabase(/* filtered = */ true)) {
    close_form(fields, 1);
  }
}

bool idaapi MenuItemDiffDatabaseCallback(void* /* unused */) {
  // Refresh screen on successful diff
  return DoDiffDatabase(/* filtered = */ false);
}

void idaapi ButtonDiffDatabaseCallback(TView* fields[], int) {
  if (DoDiffDatabase(/* filtered = */ false)) {
    close_form(fields, 1);
  }
}

void idaapi ButtonRediffDatabaseCallback(TView* fields[], int) {
  if (DoRediffDatabase()) {
    close_form(fields, 1);
  }
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
    if (!AskUsingForm_c(kDialog, &start_address_source, &end_address_source,
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
    LOG(INFO) << StringPrintf("%.2fs", timer.elapsed())
              << " seconds for comment porting.";
    return true;
  } catch (const std::bad_alloc&) {
    LOG(INFO) << "Out-of-memory. Some extremely large binaries may require to "
                 "use the 64-bit command-line version of BinDiff.";
    warning(
        "Out-of-memory. Some extremely large binaries may require to use the "
        "64-bit command-line version of BinDiff.");
  } catch (const std::exception& message) {
    LOG(INFO) << "Error while porting comments: " << message.what();
    warning("Error while porting comments: %s\n", message.what());
  } catch (...) {
    LOG(INFO) << "Unknown error while porting comments.";
    warning("Unknown error while porting comments.");
  }
  return false;
}

bool idaapi MenuItemPortCommentsCallback(void* /* unused */) {
  // Refresh screen if user did not cancel.
  return DoPortComments();
}

void idaapi ButtonPortCommentsCallback(TView* fields[], int) {
  if (DoPortComments()) {
    close_form(fields, 1);
  }
}

bool WriteResults(const std::string& path) {
  if (FileExists(path)) {
    if (askyn_c(0, "File\n'%s'\nalready exists - overwrite?",
                path.c_str()) != 1) {
      return false;
    }
  }

  WaitBox wait_box("Writing results...");
  Timer<> timer;
  LOG(INFO) << "Writing results...";
  std::string export1(g_results->call_graph1_.GetFilePath());
  std::string export2(g_results->call_graph2_.GetFilePath());
  const std::string temp_dir =
      GetTempDirectory("BinDiff", /* create = */ true);
  const std::string out_dir(Dirname(path));

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

  LOG(INFO) << "done (" << StringPrintf("%.2fs", timer.elapsed())
            << ").";
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

  const std::string default_filename(
      g_results->call_graph1_.GetFilename() + "_vs_" +
      g_results->call_graph2_.GetFilename() + ".results");
  const char* filename = askfile2_c(
      /* forsave = */ true, default_filename.c_str(),
      StrCat("BinDiff Result Log files|*.results|All files", kAllFilesFilter)
          .c_str(),
      "Save Log As");
  if (!filename) {
    return false;
  }

  if (FileExists(filename) && (askyn_c(0, "File exists - overwrite?") != 1)) {
    return false;
  }

  WaitBox wait_box("Writing results...");
  Timer<> timer;
  LOG(INFO) << "Writing to log...";
  ResultsLogWriter writer(filename);
  g_results->Write(&writer);
  LOG(INFO) << "done (" << StringPrintf("%.2fs", timer.elapsed()) << ").";

  return true;
}

void idaapi ButtonSaveResultsLogCallback(TView* [], int) { DoSaveResultsLog(); }

bool DoSaveResultsDebug() {
  if (!g_results) {
    vinfo("Please perform a diff first", 0);
    return false;
  }

  const std::string default_filename(
      g_results->call_graph1_.GetFilename() + "_vs_" +
      g_results->call_graph2_.GetFilename() + ".truth");
  const char* filename = askfile2_c(
      /* forsave = */ true, default_filename.c_str(),
      StrCat("Groundtruth files|*.truth|All files|", kAllFilesFilter).c_str(),
      "Save Groundtruth As");
  if (!filename) {
    return false;
  }

  if (FileExists(filename) && (askyn_c(0, "File exists - overwrite?") != 1)) {
    return false;
  }

  WaitBox wait_box("Writing results...");
  Timer<> timer;
  LOG(INFO) << "Writing to debug ground truth file...";
  GroundtruthWriter writer(filename, g_results->fixed_point_infos_,
                           g_results->flow_graph_infos1_,
                           g_results->flow_graph_infos2_);
  g_results->Write(&writer);
  LOG(INFO) << "done (" << StringPrintf("%.2fs", timer.elapsed()) << ").";

  return true;
}

void idaapi ButtonSaveResultsDebugCallback(TView* [], int) {
  DoSaveResultsDebug();
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
    std::string default_filename(
        g_results->call_graph1_.GetFilename() + "_vs_" +
        g_results->call_graph2_.GetFilename() + ".BinDiff");
    const char* filename = askfile2_c(
        /* forsave = */ true, default_filename.c_str(),
        StrCat("BinDiff Result files|*.BinDiff|All files", kAllFilesFilter)
            .c_str(),
        "Save Results As");
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

bool idaapi MenuItemSaveResultsCallback(void* /* unused */) {
  // Refresh screen if user did not cancel
  return DoSaveResults();
}

void idaapi ButtonSaveResultsCallback(TView* [], int) { DoSaveResults(); }

bool DoLoadResults() {
  try {
    if (g_results && g_results->IsDirty()) {
      const int answer = askyn_c(
          0, "Current diff results have not been saved - save before closing?");
      if (answer == 1) {  // yes
        DoSaveResults();
      } else if (answer == -1) {  // cancel
        return false;
      }
    }

    const char* filename = askfile2_c(
        /* forsave = */ false, "*.BinDiff",
        StrCat("BinDiff Result files|*.BinDiff|All files|", kAllFilesFilter)
            .c_str(),
        "Load Results");
    if (!filename) {
      return false;
    }

    std::string path(Dirname(filename));

    LOG(INFO) << "Loading results...";
    WaitBox wait_box("Loading results...");
    Timer<> timer;

    delete g_results;
    g_results = new Results();

    const std::string temp_dir(
        GetTempDirectory("BinDiff", /* create = */ true));

    SqliteDatabase database(filename);
    DatabaseReader reader(database, filename, temp_dir);
    g_results->Read(&reader);

    // See b/27371897.
    const std::string hash(absl::BytesToHexString(Sha1(GetDataForHash())));
    if (ToUpper(hash) != ToUpper(g_results->call_graph1_.GetExeHash())) {
      LOG(INFO) << "Warning: currently loaded IDBs input file MD5 differs from "
                   "result file primary graph. Please load IDB for: "
                << g_results->call_graph1_.GetExeFilename();
      throw std::runtime_error(StrCat(
          "loaded IDB must match primary graph in results file. Please load "
          "IDB for: ",
          g_results->call_graph1_.GetExeFilename()));
    }

    ShowResults(g_results);

    LOG(INFO) << "done (" << StringPrintf("%.2fs", timer.elapsed()) << ").";
    return true;
  } catch (const std::bad_alloc&) {
    LOG(INFO) << "Out-of-memory. Some extremely large binaries may require to "
                 "use the 64-bit command-line version of BinDiff.";
    warning(
        "Out-of-memory. Some extremely large binaries may require to use the "
        "64-bit command-line version of BinDiff.");
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

bool idaapi MenuItemLoadResultsCallback(void* /* unused */) {
  // Refresh screen if user did not cancel
  return DoLoadResults();
}

void idaapi ButtonLoadResultsCallback(TView* fields[], int /* unused */) {
  if (DoLoadResults()) {
    close_form(fields, 1);
  }
}

void InitConfig() {
  const std::string config_filename("bindiff_core.xml");

  const std::string user_path(
      GetDirectory(PATH_APPDATA, "BinDiff", /* create = */ true) +
      config_filename);
  const std::string common_path(
      GetDirectory(PATH_COMMONAPPDATA, "BinDiff", /* create = */ false) +
      config_filename);

  bool have_user_config;
  bool have_common_config;
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

void InitMenus() {
  add_menu_item("File/Produce file", "~D~iff Database...", "SHIFT-D",
                SETMENU_APP, MenuItemDiffDatabaseCallback, 0);

  add_menu_item("Edit/Comments/Insert predefined comment...",
                "Im~p~ort Symbols and Comments...", "", SETMENU_APP,
                MenuItemPortCommentsCallback, 0);

  // Different paths are used for the GUI version
  const bool result = add_menu_item("File/Load file/Additional binary file...",
                                    "~B~inDiff Results...", "", SETMENU_APP,
                                    MenuItemLoadResultsCallback, 0);
  if (!result) {
    add_menu_item("File/Save", "~B~inDiff Results...", "", SETMENU_INS,
                  MenuItemLoadResultsCallback, 0);
  }

  add_menu_item("File/Produce file/Create callgraph GDL...",
                "Save ~B~inDiff Results...", "", SETMENU_INS,
                MenuItemSaveResultsCallback, 0);

  add_menu_item("View/Open subviews/Problems",
                "BinDiff Matched Functions", "", SETMENU_APP,
                MenuItemShowResultsCallback,
                reinterpret_cast<void*>(kResultsShowMatched));
  add_menu_item("View/Open subviews/BinDiff Matched Functions",
                "BinDiff Statistics", "", SETMENU_APP,
                MenuItemShowResultsCallback,
                reinterpret_cast<void*>(kResultsShowStatistics));
  add_menu_item("View/Open subviews/BinDiff Statistics",
                "BinDiff Primary Unmatched", "", SETMENU_APP,
                MenuItemShowResultsCallback,
                reinterpret_cast<void*>(kResultsShowPrimaryUnmatched));
  add_menu_item("View/Open subviews/BinDiff Primary Unmatched",
                "BinDiff Secondary Unmatched", "", SETMENU_APP,
                MenuItemShowResultsCallback,
                reinterpret_cast<void*>(kResultsShowSecondaryUnmatched));
}

int idaapi PluginInit() {
  LoggingOptions options;
  options.set_alsologtostderr(ToUpper(GetArgument("AlsoLogToStdErr")) ==
                              "TRUE");
  options.set_log_filename(GetArgument("LogFile"));
  if (!InitLogging(options)) {
    LOG(INFO) << "Error initializing logging, skipping BinDiff plugin";
    return PLUGIN_SKIP;
  }

  LOG(INFO) << kProgramVersion << " (" << __DATE__
#ifdef _DEBUG
            << ", debug build"
#endif
            << "), " << kCopyright;

  addon_info_t addon_info;
  addon_info.cb = sizeof(addon_info_t);
  addon_info.id = "com.google.bindiff";
  addon_info.name = kName;
  addon_info.producer = "Google";
  addon_info.version = BINDIFF_MAJOR "." BINDIFF_MINOR "." BINDIFF_PATCH;
  addon_info.url = "https://zynamics.com/bindiff.html";
  addon_info.freeform = kCopyright;
  register_addon(&addon_info);

  if (!hook_to_notification_point(HT_IDP, ProcessorHook,
                                  nullptr /* User data */) ||
      !hook_to_notification_point(HT_UI, UiHook, nullptr /* User data */)) {
    LOG(INFO) << "hook_to_notification_point error";
    return PLUGIN_SKIP;
  }

  try {
    InitConfig();
  } catch (const std::runtime_error&) {
    LOG(INFO)
        << "Error: Could not load configuration file, skipping BinDiff plugin.";
    return PLUGIN_SKIP;
  }
  InitMenus();

  g_init_done = true;
  return PLUGIN_KEEP;
}

void TermMenus() {
  del_menu_item("File/Diff Database...");
  del_menu_item("Edit/Comments/Import Symbols and Comments...");
  del_menu_item("File/Load file/BinDiff Results...");
  del_menu_item("File/Save/BinDiff Results...");
  del_menu_item("File/Produce file/Save BinDiff Results...");
  del_menu_item("View/Open subviews/BinDiff Main Window");
  del_menu_item("View/Open subviews/BinDiff Matched Functions");
  del_menu_item("View/Open subviews/BinDiff Statistics");
  del_menu_item("View/Open subviews/BinDiff Primary Unmatched");
  del_menu_item("View/Open subviews/BinDiff Secondary Unmatched");
}

void idaapi PluginTerminate() {
  unhook_from_notification_point(HT_UI, UiHook, nullptr /* User data */);
  unhook_from_notification_point(HT_IDP, ProcessorHook,
                                 nullptr /* User data */);

  if (g_init_done) {
    TermMenus();
    SaveAndDiscardResults();
  }

  delete g_config;  // Also saves the config
  g_config = nullptr;

  ShutdownLogging();
}

void idaapi PluginRun(int /* arg */) {
  static const std::string kDialogBase = StrCat(
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

  static const std::string kDialogResultsAvailable = StrCat(
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

  if (!EnsureIdb()) {
    return;
  }

  if (g_results) {
    // We may have to unload a previous result if the input IDB has changed in
    // the meantime
    const std::string hash(absl::BytesToHexString(Sha1(GetDataForHash())));
    if (ToUpper(hash) != ToUpper(g_results->call_graph1_.GetExeHash())) {
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
    AskUsingForm_c(kDialogBase.c_str(), ButtonDiffDatabaseCallback,
                   ButtonDiffDatabaseFilteredCallback,
                   ButtonLoadResultsCallback);
  } else {
    AskUsingForm_c(
        kDialogResultsAvailable.c_str(), ButtonDiffDatabaseCallback,
        ButtonDiffDatabaseFilteredCallback, ButtonRediffDatabaseCallback,
        ButtonLoadResultsCallback, ButtonSaveResultsCallback,
#ifdef _DEBUG
        ButtonSaveResultsDebugCallback, ButtonSaveResultsLogCallback,
#endif
        ButtonPortCommentsCallback);
  }
}

extern "C" {

plugin_t PLUGIN = {
    IDP_INTERFACE_VERSION,
    PLUGIN_FIX,       // Flags
    PluginInit,       // Initialize
    PluginTerminate,  // Terminate
    PluginRun,        // Invoke plugin
    kComment,         // Statusline text
    nullptr,          // Multiline help about the plugin, unused
    kName,            // The preferred short name of the plugin
    kHotKey           // The preferred hotkey to run the plugin
};

}  // extern "C"
