// Copyright 2011-2019 Google LLC. All Rights Reserved.
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

#include "base/logging.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/binexport/util/format.h"

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
    LOG(INFO) << absl::StrCat("Excessively long comment at ",
                              security::binexport::FormatAddress(address), ", ",
                              comment->size(), ": ", comment->substr(0, 128),
                              "...");
  }
}
