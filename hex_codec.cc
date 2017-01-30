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

#include "third_party/zynamics/binexport/hex_codec.h"

#include <cstdint>

std::string EncodeHex(const std::string& line) {
  static const char kLookup[] = "0123456789ABCDEF";

  if (line.empty()) {
    return std::string();
  }
  std::string result;
  result.resize(line.size() * 2);
  for (int i = 0, j = 0, end = line.size(); i < end; ++i) {
    auto c = line[i];
    result[j++] = kLookup[(c & 0xF0) >> 4];
    result[j++] = kLookup[c & 0x0F];
  }
  return result;
}

std::string DecodeHex(const std::string& line) {
  static const uint8_t kLookup[] = {
      0,  0,  0,  0,  0,  0,  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0,  0,  0,  0,  0,  0,  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0,  0,  0,  0,  0,  0,  0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,  // 0123456789
      0,  0,  0,  0,  0,  0,  0,                    // :;<=>?@ (gap)
      10, 11, 12, 13, 14, 15,                       // ABCDEF
      0,  0,  0,  0,  0,  0,  0, 0, 0, 0, 0, 0, 0,  // GHIJKLMNOPQRS (gap)
      0,  0,  0,  0,  0,  0,  0, 0, 0, 0, 0, 0, 0,  // TUVWXYZ[/]^_` (gap)
      10, 11, 12, 13, 14, 15                        // abcdef
  };

  if (line.empty()) {
    return std::string();
  }
  std::string result;
  result.resize(line.size() >> 1);
  for (int i = 0, j = 0, end = line.size(); i < end; ) {
    auto& c = result[j++];
    c = kLookup[line[i++]] << 4;
    c |= kLookup[line[i++]];
  }
  return result;
}
