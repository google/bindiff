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

#include "third_party/zynamics/binexport/hash.h"

// Adapted from the optimized version at http://www.cse.yorku.ca/~oz/hash.html
uint32_t GetSdbmHash(const std::string& data) {
  uint32_t hash = 0;
  for (size_t i = 0; i < data.size(); ++i) {
    hash = data[i] + (hash << 6) + (hash << 16) - hash;
  }
  return hash;
}
