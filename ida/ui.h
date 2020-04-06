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

#ifndef IDA_UI_H_
#define IDA_UI_H_

#include <cstdint>

#include "third_party/absl/status/status.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/zynamics/binexport/types.h"

namespace security::bindiff {

constexpr double kManualMatch = -1.0;

// Returns a 32-bit RGB color value suitable for colorizing match similarities.
// If passed kManualMatch, returns a color that can be used to mark manual
// matches.
uint32_t GetMatchColor(double value);

// Copies a short unformatted plain text string to clipboard. Short in this
// context means no more than a few kilobytes. There is no hard limit, but due
// to escaping there might be some memory blow-up.
absl::Status CopyToClipboard(absl::string_view data);

}  // namespace security::bindiff

#endif  // IDA_UI_H_
