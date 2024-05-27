// Copyright 2011-2024 Google LLC
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

#ifndef IDA_VISUAL_DIFF_H_
#define IDA_VISUAL_DIFF_H_

#include <cstdint>
#include <functional>

#include "third_party/absl/functional/function_ref.h"
#include "third_party/absl/status/status.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/zynamics/bindiff/config.h"
#include "third_party/zynamics/binexport/util/types.h"

namespace security::bindiff {

// Sends a one-shot message to the Java UI, starting it if it is not already
// open.
absl::Status SendGuiMessage(const Config& config, absl::string_view arguments,
                            absl::FunctionRef<void()> on_retry_callback);

}  // namespace security::bindiff

#endif  // VISUAL_DIFF_H_
