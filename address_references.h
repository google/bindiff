// Copyright 2011-2016 Google Inc. All Rights Reserved.
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

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_ADDRESS_REFERENCES_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_ADDRESS_REFERENCES_H_

#include <algorithm>
#include <vector>

#include "third_party/zynamics/binexport/types.h"
#include "third_party/zynamics/binexport/virtual_memory.h"
#include "third_party/zynamics/binexport/entry_point.h"

enum AddressReferenceType {
  TYPE_TRUE = 0,
  TYPE_FALSE = 1,
  TYPE_UNCONDITIONAL = 2,
  TYPE_SWITCH = 3,
  TYPE_CALL_DIRECT = 4,
  TYPE_CALL_INDIRECT = 5,
  //  TYPE_CALL_INDIRECT_VIRTUAL  = 6,  // This is obsolete and unused.
  TYPE_DATA = 7,
  TYPE_DATA_STRING = 8,
  TYPE_DATA_WIDE_STRING = 9,
};

struct AddressReference {
  AddressReference(Address source, std::pair<int, int> operand_expression,
                   Address target, AddressReferenceType kind,
                   int reference_size)
      : source_(source),
        target_(target),
        source_operand_(operand_expression.first),
        source_expression_(operand_expression.second),
        size_(reference_size),
        kind_(static_cast<uint8_t>(kind)) {}

  AddressReference(Address source, std::pair<int, int> operand_expression,
                   Address target, AddressReferenceType kind)
      : AddressReference(source, operand_expression, target, kind,
                         0 /* default size */) {}

  inline bool IsCall() const {
    return kind_ == TYPE_CALL_DIRECT || kind_ == TYPE_CALL_INDIRECT;
  }

  inline bool IsBranch() const {
    return kind_ == TYPE_UNCONDITIONAL || kind_ == TYPE_TRUE ||
           kind_ == TYPE_FALSE || kind_ == TYPE_SWITCH;
  }

  Address source_;
  Address target_;
  int source_operand_;
  int source_expression_;
  int size_;
  uint8_t kind_;
};

typedef std::vector<AddressReference> AddressReferences;

bool operator<(const AddressReference& one, const AddressReference& two);
bool operator==(const AddressReference& one, const AddressReference& two);

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_ADDRESS_REFERENCES_H_
