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

#ifndef UTIL_LOG_H_
#define UTIL_LOG_H_

#include <functional>
#include <memory>
#include <string>
#include <thread>  // NOLINT

#include "base/logging.h"
#include "third_party/absl/base/log_severity.h"
#include "third_party/absl/status/status.h"
#include "third_party/absl/time/clock.h"
#include "third_party/absl/time/time.h"

namespace not_absl {

class LogEntry {
 public:
  LogEntry() : timestamp_(absl::Now()) {}
  LogEntry(absl::string_view full_filename, int line,
           absl::LogSeverity severity, absl::Time timestamp)
      : full_filename_(full_filename),
        line_(line),
        severity_(absl::NormalizeLogSeverity(severity)),
        timestamp_(timestamp),
        tid_(GetCachedTID()) {}

  std::string ToString() const;

  absl::string_view source_filename() const { return full_filename_; }
  int source_line() const { return line_; }
  absl::LogSeverity log_severity() const { return severity_; }
  absl::Time timestamp() const { return timestamp_; }

  absl::string_view text_message() const { return text_message_; }
  void set_text_message(absl::string_view text_message) {
    text_message_ = text_message;
    formatted_message_.clear();
  }

 private:
  static uint32_t GetCachedTID();

  absl::string_view full_filename_ = "";
  int line_ = 0;
  absl::LogSeverity severity_ = absl::LogSeverity::kInfo;
  absl::Time timestamp_;
  uint32_t tid_ = 0;
  absl::string_view text_message_ = "";
  mutable std::string formatted_message_;  // Cached result of ToString()
};

class LogSink {
 public:
  virtual ~LogSink() = default;

  virtual void Send(const not_absl::LogEntry& entry) = 0;
  virtual void WaitTillSent() {}
};

}  // namespace not_absl

namespace security::binexport {

struct LoggingOptions {
  LoggingOptions& set_alsologtostderr(bool value) {
    alsologtostderr = value;
    return *this;
  }

  LoggingOptions& set_log_filename(const std::string& filename) {
    log_filename = filename;
    return *this;
  }

  bool alsologtostderr = false;
  std::string log_filename;
};

// Initializes logging with the specified options and a default log sink.
absl::Status InitLogging(const LoggingOptions& options,
                         std::unique_ptr<not_absl::LogSink> log_sink);

// Shuts down logging, closing any log files.
void ShutdownLogging();

}  // namespace security::binexport

#endif  // UTIL_LOG_H_
