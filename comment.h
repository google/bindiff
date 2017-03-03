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

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_COMMENT_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_COMMENT_H_

#include <string>
#include <vector>

#include "third_party/zynamics/binexport/types.h"

#pragma pack(push, 1)
class Comment {
 public:
  enum Type {
    REGULAR = 0,
    ENUM = 1,
    ANTERIOR = 2,
    POSTERIOR = 3,
    FUNCTION = 4,
    LOCATION = 5,
    GLOBALREFERENCE = 6,
    LOCALREFERENCE = 7,
    STRUCTURE = 8,
    INVALID
  };

  explicit Comment(Address address, size_t operand_num,
                   const std::string* comment = 0, Type type = INVALID,
                   bool repeatable = false);

  Address address_;
  size_t operand_num_;
  const std::string* comment_;
  bool repeatable_;
  Type type_;
};
#pragma pack(pop)

typedef std::vector<Comment> Comments;
bool SortComments(const Comment& one, const Comment& two);

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_COMMENT_H_
