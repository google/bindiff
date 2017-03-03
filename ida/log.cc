// Copyright 2011-2017 Google Inc. All Rights Reserved.
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
#include <chrono>  // NOLINT
#include <cstdio>
#include <cstring>
#include <ctime>
#include <functional>
#include <thread>  // NOLINT

#include "third_party/zynamics/binexport/ida/pro_forward.h"  // NOLINT
#include <kernwin.hpp>                                       // NOLINT

#include "base/logging.h"
#include "base/stringprintf.h"

namespace {

static LogHandler* g_old_log_handler = nullptr;
static FILE* g_log_file = nullptr;
static LoggingOptions g_logging_options;

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

char LogLevelToChar(LogLevel level) {
  switch (level) {
    case LogLevel::LOGLEVEL_INFO:
      return 'I';
    case LogLevel::LOGLEVEL_WARNING:
      return 'W';
    case LogLevel::LOGLEVEL_ERROR:
      return 'E';
    case LogLevel::LOGLEVEL_FATAL:
      return 'F';
    default:
      return '?';
  }
}

// Logs a single log message. Should be executed on the IDA main thread.
void LogLine(LogLevel level, const char* filename, int line,
             const std::string& message) {
  if (g_logging_options.alsologtostderr() || g_log_file != nullptr) {
    auto now = std::chrono::system_clock::now();
    auto now_time = std::chrono::system_clock::to_time_t(now);
    char log_time[21 /* "0125 16:09:42.992535" */] = {'\0'};
    std::strftime(log_time, sizeof(log_time), "%m%d %T",
                  std::localtime(&now_time));

    // TODO(cblichmann): Windows
    const char* basename = strrchr(filename, '/');
    if (*basename != '\0') {
      ++basename;
    }

    // Prepare log line
    // TODO(cblichmann): Windows/OS X
    std::string formatted(StringPrintf(
        "%c%s %7u [%s:%d]: %s\n", LogLevelToChar(level), log_time,
        std::this_thread::get_id(), basename, line, message.c_str()));

    if (g_logging_options.alsologtostderr()) {
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

void IdaLogHandler(google::protobuf::LogLevel level, const char* filename,
                   int line, const std::string& message) {
  // Do all logging in IDA's main thread.
  IdaExecutor executor(std::bind(LogLine, level, filename, line, message));
  execute_sync(executor, MFF_FAST);
}

}  // namespace

bool InitLogging(const LoggingOptions& options) {
  ShutdownLogging();

  g_logging_options = options;
  if (!g_logging_options.log_filename().empty()) {
    const char* log_filename = g_logging_options.log_filename().c_str();
    g_log_file = fopen(log_filename, "a");
    if (!g_log_file) {
      msg("Could not open log file: \"%s\": %s\n", log_filename,
          std::strerror(errno));
      return false;
    }
  }

  g_old_log_handler = SetLogHandler(&IdaLogHandler);
  return true;
}

void ShutdownLogging() {
  if (g_log_file) {
    fclose(g_log_file);
  }
  if (g_old_log_handler) {
    SetLogHandler(g_old_log_handler);
  }
}
