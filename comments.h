#ifndef COMMENTS_H_
#define COMMENTS_H_

#include <map>
#include <string>

#include "third_party/zynamics/binexport/types.h"

namespace security::bindiff {

struct Comment {
 public:
  enum Type {
    REGULAR = 0,
    ENUM = 1,
    ANTERIOR = 2,
    POSTERIOR = 3,
    FUNCTION = 4,
    LOCATION = 5,
    GLOBAL_REFERENCE = 6,
    LOCAL_REFERENCE = 7,
    STRUCTURE = 8,
  };

  std::string comment;
  bool repeatable = false;
  Type type = REGULAR;
};

using OperatorId = std::pair<Address, int>;
using CommentsByOperatorId = std::map<OperatorId, Comment>;

}  // namespace security::bindiff

#endif  // COMMENTS_H_
