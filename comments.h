#ifndef COMMENTS_H_
#define COMMENTS_H_

#include <map>
#include <string>

#include "third_party/zynamics/bindiff/utility.h"
#include "third_party/zynamics/binexport/types.h"

namespace security {
namespace bindiff {

struct Comment {
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
  };

  std::string comment;
  bool repeatable = false;
  Type type = REGULAR;
};

using OperatorId = std::pair<Address, int>;
using Comments = std::map<OperatorId, Comment>;

}  // namespace bindiff
}  // namespace security

#endif  // COMMENTS_H_
