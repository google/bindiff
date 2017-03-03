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

#include "third_party/zynamics/binexport/comment.h"

#include <cinttypes>

#include "base/logging.h"
#include "base/stringprintf.h"

bool SortComments(const Comment& lhs, const Comment& rhs) {
  if (lhs.address_ == rhs.address_) {
    if (lhs.type_ == rhs.type_) {
      return lhs.operand_num_ < rhs.operand_num_;
    }
    return lhs.type_ < rhs.type_;
  }
  return lhs.address_ < rhs.address_;
}

Comment::Comment(Address address, size_t operand_num,
                 const std::string* comment, Type type, bool repeatable)
    : address_(address),
      operand_num_(operand_num),
      comment_(comment),
      repeatable_(repeatable),
      type_(type) {
  if (comment && comment->size() >= 4096) {
    LOG(INFO) << "Excessively long comment at "
              << StringPrintf("%08" PRIx64, address) << ", " << comment->size()
              << " bytes: " << *comment;
  }
}
