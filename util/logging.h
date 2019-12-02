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
#include <string>

#include "base/logging.h"
#include "third_party/absl/status/status.h"

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

// Logs a single line according to the current global logging options.
void LogLine(LogLevel level, const char* filename, int line,
             const std::string& message);

// Initializes logging. Not thread safe.
absl::Status InitLogging(const LoggingOptions& options,
                         LogHandler* log_handler);

// Shuts down logging and closes the log file. Not thread safe.
void ShutdownLogging();

}  // namespace security::binexport

#endif  // UTIL_LOG_H_
