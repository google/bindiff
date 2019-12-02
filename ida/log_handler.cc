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

#include "third_party/zynamics/binexport/ida/log_handler.h"

// clang-format off
#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <kernwin.hpp>                                          // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT
// clang-format on

#include "third_party/zynamics/binexport/util/logging.h"

namespace security::binexport {

void IdaLogHandler(LogLevel level, const char* filename, int line,
                   const std::string& message) {
  // Do all logging in IDA's main thread.
  struct IdaExecutor : public exec_request_t {
    explicit IdaExecutor(std::function<void()> callback)
        : callback_(callback) {}

    int idaapi execute() override {
      callback_();
      return 0;
    }

    std::function<void()> callback_;
  };
  IdaExecutor executor([level, filename, line, &message] {
    LogLine(level, filename, line, message);
    msg("%s\n", message.c_str());
  });
  execute_sync(executor, MFF_FAST);
}

}  // namespace security::binexport