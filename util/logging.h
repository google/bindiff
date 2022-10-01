// Copyright 2011-2022 Google LLC
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

#ifndef UTIL_LOG_H_
#define UTIL_LOG_H_

#include <functional>
#include <memory>
#include <string>
#include <thread>  // NOLINT

#include "third_party/absl/base/log_severity.h"
#include "third_party/absl/log/log_sink.h"
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

// Initializes logging with the specified options and a default log sink. It is
// an error to call this function multiple times.
absl::Status InitLogging(const LoggingOptions& options,
                         std::unique_ptr<absl::LogSink> log_sink);

// Shuts down logging, closing any log files.
void ShutdownLogging();

}  // namespace security::binexport

#endif  // UTIL_LOG_H_
