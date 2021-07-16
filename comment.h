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

#ifndef COMMENTS_H_
#define COMMENTS_H_

#include <string>
#include <utility>

#include "third_party/absl/container/btree_map.h"
#include "third_party/zynamics/binexport/util/types.h"

namespace security::bindiff {

struct Comment {
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
    INVALID,        // For compatibility with BinExport
  };

  std::string comment;

  // Whether the comment is propagated to all locations that reference the
  // original location.
  bool repeatable = false;

  Type type = REGULAR;
};

using OperatorId = std::pair<Address, int>;
using CommentsByOperatorId = absl::btree_map<OperatorId, Comment>;

}  // namespace security::bindiff

#endif  // COMMENTS_H_
