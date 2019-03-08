#ifndef IDA_NAMES_H_
#define IDA_NAMES_H_

#include <string>

#include "third_party/zynamics/binexport/types.h"

namespace security {
namespace bindiff {

string GetName(Address address);
string GetDemangledName(Address address);

enum class LineComment {
  kAnterior,
  kPosterior
};
string GetLineComments(Address address, LineComment kind);

}  // namespace bindiff
}  // namespace security

#endif  // IDA_NAMES_H_
