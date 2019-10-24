#ifndef IDA_NAMES_H_
#define IDA_NAMES_H_

#include <string>

#include "third_party/zynamics/binexport/types.h"

namespace security::bindiff {

std::string GetName(Address address);
std::string GetDemangledName(Address address);

enum class LineComment {
  kAnterior,
  kPosterior
};
std::string GetLineComments(Address address, LineComment kind);

}  // namespace security::bindiff

#endif  // IDA_NAMES_H_
