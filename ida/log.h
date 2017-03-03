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

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_IDA_LOG_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_IDA_LOG_H_

#include <string>

class LoggingOptions {
 public:
  LoggingOptions() = default;

  void set_alsologtostderr(bool value) { alsologtostderr_ = value; }

  bool alsologtostderr() const { return alsologtostderr_; }

  void set_log_filename(const std::string& filename) {
    log_filename_ = filename;
  }

  const std::string& log_filename() const { return log_filename_; }

 private:
  bool alsologtostderr_ = false;
  std::string log_filename_;
};

// Initializes logging. Not thread safe.
bool InitLogging(const LoggingOptions& options);

// Shuts down logging and closes the log file. Not thread safe.
void ShutdownLogging();

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_IDA_LOG_H_
