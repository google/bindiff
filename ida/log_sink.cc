// Copyright 2011-2022 Google LLC. All Rights Reserved.
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

#include "third_party/zynamics/binexport/ida/log_sink.h"

#include <algorithm>

// clang-format off
#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <kernwin.hpp>                                          // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT
// clang-format on

#include "third_party/absl/strings/string_view.h"
#include "third_party/zynamics/binexport/util/logging.h"

namespace security::binexport {

void IdaLogSink::Send(const absl::LogEntry& entry) {
  auto message = entry.text_message_with_newline();
  // Output up to 8K of log data (including the new line).
  // Note: msg() and vmsg() are thread-safe.
  msg("%.*s",
      static_cast<int>(std::clamp(message.size(), size_t(0), size_t(8192))),
      message.data());
}

}  // namespace security::binexport
