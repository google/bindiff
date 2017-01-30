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

#include "third_party/zynamics/binexport/edge.h"

bool operator<(const FlowGraphEdge& one, const FlowGraphEdge& two) {
  if (one.source == two.source) {
    if (one.target == two.target) {
      return one.type < two.type;
    }
    return one.target < two.target;
  }
  return one.source < two.source;
}

FlowGraphEdge::FlowGraphEdge(Address source, Address target, Type type)
    : source(source), target(target), type(type) {}

const char* FlowGraphEdge::GetTypeName() const {
  switch (type) {
    case TYPE_TRUE:
      return "true";
    case TYPE_FALSE:
      return "false";
    case TYPE_UNCONDITIONAL:
      return "unconditional";
    case TYPE_SWITCH:
      return "switch";
  }
}
