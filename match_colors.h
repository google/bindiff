// Copyright 2011-2023 Google LLC
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

#ifndef MATCH_COLORS_H_
#define MATCH_COLORS_H_

#include <cstdint>

namespace security::bindiff {

constexpr double kManualMatch = -1.0;

// Returns a 32-bit RGB color value suitable for colorizing match similarities.
// If passed kManualMatch, returns a color that can be used to mark manual
// matches.
uint32_t GetMatchColor(double value);

}  // namespace security::bindiff

#endif  // MATCH_COLORS_H_
