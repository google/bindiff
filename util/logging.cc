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

#include "third_party/zynamics/binexport/util/logging.h"

#include <cerrno>
#include <cstdio>
#include <cstring>
#include <ctime>
#include <functional>
#include <iomanip>
#include <sstream>
#include <thread>  // NOLINT

#include "base/logging.h"
#include "third_party/absl/status/status.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/time/clock.h"
#include "third_party/absl/time/time.h"

namespace security::binexport {

static LogHandler* g_old_log_handler = nullptr;
static FILE* g_log_file = nullptr;
static auto* g_logging_options = new LoggingOptions{};

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

void LogLine(LogLevel level, const char* filename, int line,
             const std::string& message) {
  if (!g_logging_options->alsologtostderr && g_log_file == nullptr) {
    return;
  }
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
  std::string formatted =
      absl::StrCat(LogLevelToCStr(level),
                   absl::FormatTime("%m%d %R:%E6S" /* "0125 16:09:42.992535" */,
                                    absl::Now(), absl::LocalTimeZone()),
                   " ", thread_id.str().substr(0, kThreadIdWidth), " ",
                   basename, ":", line, "] ", message, "\n");

  if (g_logging_options->alsologtostderr) {
    fputs(formatted.c_str(), stderr);
    fflush(stderr);
  }
  if (g_log_file) {
    fputs(formatted.c_str(), g_log_file);
    fflush(g_log_file);
  }
}

absl::Status InitLogging(const LoggingOptions& options,
                         LogHandler* log_handler) {
  ShutdownLogging();

  *g_logging_options = options;
  if (!g_logging_options->log_filename.empty()) {
    const char* log_filename = g_logging_options->log_filename.c_str();
    g_log_file = fopen(log_filename, "a");
    if (!g_log_file) {
      return absl::InternalError(
          absl::StrCat("Could not open log file: \"", log_filename,
                       "\": ", std::strerror(errno), "\n"));
    }
  }

  g_old_log_handler = SetLogHandler(log_handler);
  return absl::OkStatus();
}

void ShutdownLogging() {
  if (g_log_file) {
    fclose(g_log_file);
  }
  if (g_old_log_handler) {
    SetLogHandler(g_old_log_handler);
  }
}

}  // namespace security::binexport
