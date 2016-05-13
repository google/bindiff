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

#include "third_party/zynamics/binexport/address_references.h"

bool operator<(const AddressReference& one, const AddressReference& two) {
  if (one.source_ == two.source_) {
    if (one.target_ == two.target_) {
      return one.kind_ < two.kind_;
    }
    return one.target_ < two.target_;
  }
  return one.source_ < two.source_;
}

bool operator==(const AddressReference& one, const AddressReference& two) {
  return one.source_ == two.source_ && one.target_ == two.target_ &&
         one.source_expression_ == two.source_expression_ &&
         one.source_operand_ == two.source_operand_ && one.kind_ == two.kind_;
}
