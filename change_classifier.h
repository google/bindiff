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

#ifndef CHANGE_CLASSIFIER_H_
#define CHANGE_CLASSIFIER_H_

#include <string>

#include "third_party/zynamics/binexport/types.h"

namespace security::bindiff {

class MatchingContext;
class FixedPoint;

enum ChangeType {
  CHANGE_NONE = 0,
  CHANGE_STRUCTURAL = 1 << 0,       // G-raph
  CHANGE_INSTRUCTIONS = 1 << 1,     // I-nstruction
  CHANGE_OPERANDS = 1 << 2,         // O-perand
  CHANGE_BRANCHINVERSION = 1 << 3,  // J-ump
  CHANGE_ENTRYPOINT = 1 << 4,       // E-entrypoint
  CHANGE_LOOPS = 1 << 5,            // L-oop
  CHANGE_CALLS = 1 << 6,            // C-all
  CHANGE_COUNT = 7
};

// Returns a short descirption of the specified change. The returned string is
// similar to a Posix "ls -l" output. For example, if only structural changes
// have occured, it looks like this: "G------".
std::string GetChangeDescription(int change_flags);

void ClassifyChanges(FixedPoint* fixed_point);
void ClassifyChanges(MatchingContext* context);

}  // namespace security::bindiff

#endif  // CHANGE_CLASSIFIER_H_
