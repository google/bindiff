// Copyright 2011-2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#include "third_party/zynamics/binexport/comment.h"

#include "third_party/absl/log/log.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/binexport/util/format.h"

bool SortComments(const Comment& lhs, const Comment& rhs) {
  if (lhs.address == rhs.address) {
    if (lhs.type == rhs.type) {
      return lhs.operand_num < rhs.operand_num;
    }
    return lhs.type < rhs.type;
  }
  return lhs.address < rhs.address;
}

Comment::Comment(Address address, size_t operand_num,
                 const std::string* comment, Type type, bool repeatable)
    : address(address),
      operand_num(operand_num),
      comment(comment),
      type(type),
      repeatable(repeatable) {
  if (comment && comment->size() >= 4096) {
    LOG(INFO) << absl::StrCat("Excessively long comment at ",
                              security::binexport::FormatAddress(address), ", ",
                              comment->size(), ": ", comment->substr(0, 128),
                              "...");
  }
}
