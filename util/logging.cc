// Copyright 2011-2019 Google LLC. All Rights Reserved.
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

#include "third_party/absl/base/internal/sysinfo.h"
#include "third_party/absl/log/initialize.h"
#include "third_party/absl/log/log.h"
#include "third_party/absl/log/log_sink.h"
#include "third_party/absl/log/log_sink_registry.h"
#include "third_party/absl/status/status.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/strings/str_format.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/absl/synchronization/mutex.h"
#include "third_party/absl/time/clock.h"
#include "third_party/absl/time/time.h"
#include "third_party/zynamics/binexport/util/process.h"

namespace security::binexport {

class StdErrLogSink : public absl::LogSink {
 private:
  void Send(const absl::LogEntry& entry) override {
    fputs(entry.text_message_with_prefix_and_newline_c_str(), stderr);
  }
};

class FileLogSink : public absl::LogSink {
 public:
  explicit FileLogSink(FILE* file) : file_(file) {}

  ~FileLogSink() override {
    if (file_) {
      fclose(file_);
    }
  }

 private:
  void Send(const absl::LogEntry& entry) override {
    fputs(entry.text_message_with_prefix_and_newline_c_str(), file_);
  }

  void Flush() override { fflush(file_); }

  FILE* file_;
};

static absl::LogSink* g_main_log_sink = nullptr;
static absl::LogSink* g_stderr_log_sink = nullptr;
static absl::LogSink* g_file_log_sink = nullptr;

absl::Status InitLogging(const LoggingOptions& options,
                         std::unique_ptr<absl::LogSink> log_sink) {
  if (!options.log_filename.empty()) {
    FILE* log_file = fopen(options.log_filename.c_str(), "a");
    if (!log_file) {
      return absl::InternalError(absl::StrCat("Could not open log file: \"",
                                              options.log_filename,
                                              "\": ", GetLastOsError(), "\n"));
    }
    g_file_log_sink = new FileLogSink(log_file);
  }

  absl::InitializeLog();

  g_main_log_sink = log_sink.release();
  absl::AddLogSink(g_main_log_sink);

  if (g_file_log_sink) {
    absl::AddLogSink(g_file_log_sink);
  }

  if (options.alsologtostderr) {
    g_stderr_log_sink = new StdErrLogSink();
    absl::AddLogSink(g_stderr_log_sink);
  }

  return absl::OkStatus();
}

void ShutdownLogging() {
  for (absl::LogSink** sink :
       {&g_file_log_sink, &g_stderr_log_sink, &g_main_log_sink}) {
    if (*sink) {
      absl::RemoveLogSink(*sink);
      delete *sink;
      *sink = nullptr;
    }
  }
}

}  // namespace security::binexport
