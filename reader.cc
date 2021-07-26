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

#include "third_party/zynamics/bindiff/reader.h"
#include "third_party/zynamics/bindiff/call_graph.h"
#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/bindiff/flow_graph.h"

namespace security::bindiff {

bool operator<(const FixedPointInfo& one, const FixedPointInfo& two) {
  return one.primary < two.primary;
}

bool FixedPointInfo::IsManual() const {
  return confidence == 1.0 && algorithm &&
         algorithm->find("manual") != std::string::npos;
}

}  // namespace security::bindiff
