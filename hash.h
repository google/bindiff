// Copyright 2011-2021 Google LLC
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

#ifndef HASH_H_
#define HASH_H_

#include <string>

#include "third_party/zynamics/binexport/types.h"

// Calculates a general-purpose, non-cryptographic hash over the contents of a
// string.
uint32_t GetSdbmHash(const std::string& data);

#endif  // HASH_H_
