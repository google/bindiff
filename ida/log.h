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

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_IDA_LOG_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_IDA_LOG_H_

#include "third_party/zynamics/binexport/types.h"

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

// Initializes logging. Not thread safe.
bool InitLogging(const LoggingOptions& options);

// Shuts down logging and closes the log file. Not thread safe.
void ShutdownLogging();

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_IDA_LOG_H_
