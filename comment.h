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

#ifndef COMMENT_H_
#define COMMENT_H_

#include <string>
#include <vector>

#include "third_party/zynamics/binexport/util/types.h"

class BasicComment {
 public:
  // Refer to BinExport2::Comment::Type for documentation on the values below.
  // Note that this enum has different values from the proto based one. The
  // reason for this is mostly historical (BinExport v1).
  enum Type {
    REGULAR = 0,
    ENUM = 1,
    ANTERIOR = 2,
    POSTERIOR = 3,
    FUNCTION = 4,
    LOCATION = 5,
    GLOBAL_REFERENCE = 6,
    LOCAL_REFERENCE = 7,
    STRUCTURE = 8,  // Not implemented
    INVALID,
  };
};

struct Comment : public BasicComment {
 public:
  explicit Comment(Address address, size_t operand_num,
                   const std::string* comment = nullptr, Type type = INVALID,
                   bool repeatable = false);

  Address address;
  size_t operand_num;
  const std::string* comment;
  Type type;
  bool repeatable;
};

using Comments = std::vector<Comment>;
bool SortComments(const Comment& lhs, const Comment& rhs);

#endif  // COMMENT_H_
