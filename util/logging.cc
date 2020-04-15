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
#include <memory>
#include <sstream>
#include <thread>  // NOLINT

#include "base/logging.h"
#include "third_party/absl/base/internal/sysinfo.h"
#include "third_party/absl/status/status.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/strings/str_format.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/absl/synchronization/mutex.h"
#include "third_party/absl/time/clock.h"
#include "third_party/absl/time/time.h"
#include "third_party/zynamics/binexport/util/process.h"

namespace not_absl {

uint32_t LogEntry::GetCachedTID() {
  // Have to reach into Abseil internals to avoid platform-specific code
  // duplication.
  static thread_local uint32_t id = absl::base_internal::GetTID();
  return id;
}

std::string LogEntry::ToString() const {
  if (!formatted_message_.empty()) {
    return formatted_message_;
  }

  // Filename always has Unix path separators, so this works on Windows, too.
  const auto last_slash = full_filename_.find_last_of('/');
  absl::string_view basename = last_slash == absl::string_view::npos
                                   ? full_filename_
                                   : full_filename_.substr(last_slash + 1);

  // Prepare log line
  formatted_message_ = absl::StrFormat(
      "%c%s %7d %s:%d] %s\n", absl::LogSeverityName(severity_)[0],
      absl::FormatTime("%m%d %R:%E6S" /* "0125 16:09:42.992535" */, absl::Now(),
                       absl::LocalTimeZone()),
      GetCachedTID(), basename, line_, text_message_);
  return formatted_message_;
}

ABSL_CONST_INIT absl::Mutex g_sinks_mutex(absl::kConstInit);
auto* g_sinks ABSL_GUARDED_BY(g_sinks_mutex)
    ABSL_PT_GUARDED_BY(g_sinks_mutex) = new std::vector<not_absl::LogSink*>();

void AddLogSink(not_absl::LogSink* sink) ABSL_LOCKS_EXCLUDED(g_sinks_mutex) {
  absl::MutexLock global_sinks_lock(&g_sinks_mutex);
  g_sinks->push_back(sink);
}

void RemoveLogSink(not_absl::LogSink* sink) ABSL_LOCKS_EXCLUDED(g_sinks_mutex) {
  absl::MutexLock global_sinks_lock(&g_sinks_mutex);
  g_sinks->erase(std::remove(g_sinks->begin(), g_sinks->end(), sink),
                 g_sinks->end());
}

}  // namespace not_absl

namespace security::binexport {

class StdErrLogSink : public not_absl::LogSink {
 private:
  void Send(const not_absl::LogEntry& entry) override {
    fputs(entry.ToString().c_str(), stderr);
  }
};

class FileLogSink : public not_absl::LogSink {
 public:
  explicit FileLogSink(FILE* file) : file_(file) {}

  ~FileLogSink() override {
    if (file_) {
      fclose(file_);
    }
  }

 private:
  void Send(const not_absl::LogEntry& entry) override {
    fputs(entry.ToString().c_str(), file_);
    fflush(file_);
  }

  FILE* file_;
};

void LogSinkLogHandler(::google::protobuf::LogLevel level, const char* filename,
                       int line, const std::string& message) {
  not_absl::LogEntry entry(filename, line,
                           static_cast<absl::LogSeverity>(level), absl::Now());
  entry.set_text_message(message);
  for (const auto& sink : *not_absl::g_sinks) {
    sink->Send(entry);
  }
}

static LogHandler* g_old_log_handler = nullptr;
static not_absl::LogSink* g_main_log_sink = nullptr;
static not_absl::LogSink* g_stderr_log_sink = nullptr;
static not_absl::LogSink* g_file_log_sink = nullptr;

absl::Status InitLogging(const LoggingOptions& options,
                         std::unique_ptr<not_absl::LogSink> log_sink) {
  ShutdownLogging();

  g_main_log_sink = log_sink.release();
  not_absl::AddLogSink(g_main_log_sink);

  if (!options.log_filename.empty()) {
    FILE* log_file = fopen(options.log_filename.c_str(), "a");
    if (!log_file) {
      return absl::InternalError(absl::StrCat("Could not open log file: \"",
                                              options.log_filename,
                                              "\": ", GetLastOsError(), "\n"));
    }
    g_file_log_sink = new FileLogSink(log_file);
    not_absl::AddLogSink(g_file_log_sink);
  }

  if (options.alsologtostderr) {
    g_stderr_log_sink = new StdErrLogSink();
    not_absl::AddLogSink(g_stderr_log_sink);
  }

  g_old_log_handler = SetLogHandler(&LogSinkLogHandler);
  return absl::OkStatus();
}

void ShutdownLogging() {
  for (not_absl::LogSink** sink :
       {&g_file_log_sink, &g_stderr_log_sink, &g_main_log_sink}) {
    if (*sink) {
      not_absl::RemoveLogSink(*sink);
      delete *sink;
      *sink = nullptr;
    }
  }
  if (g_old_log_handler) {
    SetLogHandler(g_old_log_handler);
  }
}

}  // namespace security::binexport
