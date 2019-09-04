// Copyright 2011-2019 Google LLC. All Rights Reserved.
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

#include "third_party/zynamics/binexport/ida/log.h"

#include <cerrno>
#include <cstdio>
#include <cstring>
#include <ctime>
#include <functional>
#include <iomanip>
#include <sstream>
#include <thread>  // NOLINT

#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <kernwin.hpp>                                          // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT

#include "base/logging.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/time/clock.h"
#include "third_party/absl/time/time.h"

namespace {

#ifndef GOOGLE  // MOE:strip_line
static LogHandler* g_old_log_handler = nullptr;
#endif  // MOE:strip_line
static FILE* g_log_file = nullptr;
static auto* g_logging_options = new LoggingOptions{};

class IdaExecutor : public exec_request_t {
 public:
  explicit IdaExecutor(std::function<void()> callback) : callback_(callback) {}

  int idaapi execute() override {
    callback_();
    return 0;
  }

 private:
  std::function<void()> callback_;
};

#ifndef GOOGLE  // MOE:strip_line
const char* const LogLevelToCStr(LogLevel level) {
  switch (level) {
    case LogLevel::LOGLEVEL_INFO:
      return "I";
    case LogLevel::LOGLEVEL_WARNING:
      return "W";
    case LogLevel::LOGLEVEL_ERROR:
      return "E";
    case LogLevel::LOGLEVEL_FATAL:
      return "F";
    default:
      return "?";
  }
}

// Logs a single log message. Should be executed on the IDA main thread.
void LogLine(LogLevel level, const char* filename, int line,
             const std::string& message) {
  if (g_logging_options->alsologtostderr || g_log_file != nullptr) {
    // Filename always has Unix path separators, so this works on Windows, too.
    const char* basename = strrchr(filename, '/');
    if (*basename != '\0') {
      ++basename;
    }

    // Prepare log line
    std::ostringstream thread_id;
    enum { kThreadIdWidth = 7 };
    thread_id << std::setw(kThreadIdWidth) << std::setfill(' ')
              << std::this_thread::get_id();
    std::string formatted = absl::StrCat(
        LogLevelToCStr(level),
        absl::FormatTime("%m%d %R:%E6S" /* "0125 16:09:42.992535" */,
                         absl::Now(), absl::LocalTimeZone()),
        " ", thread_id.str().substr(0, kThreadIdWidth), " ", basename, ":",
        line, "] ", message, "\n");

    if (g_logging_options->alsologtostderr) {
      fputs(formatted.c_str(), stderr);
      fflush(stderr);
    }
    if (g_log_file) {
      fputs(formatted.c_str(), g_log_file);
      fflush(g_log_file);
    }
  }

  msg("%s\n", message.c_str());
}

void IdaLogHandler(LogLevel level, const char* filename, int line,
                   const std::string& message) {
  // Do all logging in IDA's main thread.
  IdaExecutor executor(std::bind(LogLine, level, filename, line, message));
  execute_sync(executor, MFF_FAST);
}

// MOE:begin_strip
#else
// TODO(cblichmann): Implement LogSink for Google3
#endif
// MOE:end_strip
}  // namespace

bool InitLogging(const LoggingOptions& options) {
  ShutdownLogging();

  *g_logging_options = options;
  if (!g_logging_options->log_filename.empty()) {
    const char* log_filename = g_logging_options->log_filename.c_str();
    g_log_file = fopen(log_filename, "a");
    if (!g_log_file) {
      msg("Could not open log file: \"%s\": %s\n", log_filename,
          std::strerror(errno));
      return false;
    }
  }

#ifndef GOOGLE  // MOE:strip_line
  g_old_log_handler = SetLogHandler(&IdaLogHandler);
#endif  // MOE:strip_line
  return true;
}

void ShutdownLogging() {
  if (g_log_file) {
    fclose(g_log_file);
  }
#ifndef GOOGLE  // MOE:strip_line
  if (g_old_log_handler) {
    SetLogHandler(g_old_log_handler);
  }
#endif  // MOE:strip_line
}
