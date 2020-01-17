// Copyright 2011-2020 Google LLC
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

#include "third_party/zynamics/bindiff/ida/names.h"

// clang-format off
#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <bytes.hpp>                                            // NOLINT
#include <lines.hpp>                                            // NOLINT
#include <name.hpp>                                             // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT
// clang-format on

#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/binexport/ida/util.h"

namespace security::bindiff {

using binexport::ToString;

std::string GetName(Address address) {
  if (has_user_name(get_full_flags(static_cast<ea_t>(address)))) {
    return ToString(get_name(static_cast<ea_t>(address)));
  }
  return "";
}

std::string GetDemangledName(Address address) {
  if (has_user_name(get_full_flags(static_cast<ea_t>(address)))) {
    return ToString(get_short_name(static_cast<ea_t>(address)));
  }
  return "";
}

bool GetLineComment(Address address, int n, std::string* output) {
  qstring ida_comment;
  ssize_t result = get_extra_cmt(&ida_comment, address, n);
  *output = ToString(ida_comment);
  // Note: get_extra_cmt() returns < 0 if there is no line comment for "n".
  //       It is valid for a line comment to be empty, such as when there are
  //       multiple paragraphs of line comments.
  return result >= 0;
}

std::string GetLineComments(Address address, LineComment kind) {
  std::string buffer;
  std::string comment;

  if (kind == LineComment::kAnterior) {
    for (int i = 0; GetLineComment(address, E_PREV + i, &buffer); ++i) {
      absl::StrAppend(&comment, buffer, "\n");
    }
  } else if (kind == LineComment::kPosterior) {
    for (int i = 0; GetLineComment(address, E_NEXT + i, &buffer); ++i) {
      absl::StrAppend(&comment, buffer, "\n");
    }
  }
  if (!comment.empty()) {
    comment = comment.substr(0, comment.size() - 1);
  }
  return comment;
}

}  // namespace security::bindiff
