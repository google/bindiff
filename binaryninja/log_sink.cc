// Copyright 2019-2020 Google LLC
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

#include "third_party/zynamics/binexport/binaryninja/log_sink.h"

// clang-format off
#include "binaryninjaapi.h"  // NOLINT
// clang-format on

#include "base/logging.h"
#include "build/absl/absl/base/log_severity.h"
#include "build/absl/absl/strings/string_view.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/zynamics/binexport/util/logging.h"

namespace security::binexport {

void BinaryNinjaLogSink::Send(const not_absl::LogEntry& entry) {
  BNLogLevel level;
  switch (entry.log_severity()) {
    case absl::LogSeverity::kInfo:
      level = InfoLog;
      break;
    case absl::LogSeverity::kWarning:
      level = WarningLog;
      break;
    case absl::LogSeverity::kError:
    case absl::LogSeverity::kFatal:
    default:
      level = ErrorLog;
      break;
  }
  absl::string_view message = entry.text_message();
  BinaryNinja::Log(level, "%*s", message.size(), message.data());
}

}  // namespace security::binexport
