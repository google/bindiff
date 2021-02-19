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

#ifndef EDGE_H_
#define EDGE_H_

#include <cstddef>
#include <cstdint>

#include "third_party/zynamics/binexport/types.h"

struct FlowGraphEdge {
 public:
  enum Type : uint8_t {
    TYPE_TRUE = 1,
    TYPE_FALSE = 2,
    TYPE_UNCONDITIONAL = 3,
    TYPE_SWITCH = 4
  };

  FlowGraphEdge(Address source, Address target, Type type);
  const char* GetTypeName() const;

  Address source;
  Address target;
  Type type;
};

struct FlowGraphEdgeHash {
  size_t operator()(const FlowGraphEdge& fge) const;
};

// For easy use with set containers. Sorts by source address first, target
// address second.
bool operator<(const FlowGraphEdge& one, const FlowGraphEdge& two);
bool operator==(const FlowGraphEdge& lhs, const FlowGraphEdge& rhs);

#endif  // EDGE_H_
