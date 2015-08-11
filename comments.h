#ifndef COMMENTS_H_
#define COMMENTS_H_

#include <map>
#include <string>

#include "third_party/zynamics/bindiff/utility.h"

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
  };

  explicit Comment(const std::string& comment = "", Type type = REGULAR,
                   bool repeatable = false);

  std::string comment_;
  bool repeatable_;
  Type type_;
};

typedef std::pair<Address, int> OperatorId;
typedef std::map<OperatorId, Comment> Comments;

#endif  // COMMENTS_H_
