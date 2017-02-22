#ifndef IDA_NAMES_H_
#define IDA_NAMES_H_

#include <string>

#include "third_party/zynamics/binexport/types.h"

std::string GetName(Address address);
std::string GetDemangledName(Address address);
std::string GetLineComments(Address address, int direction);

#endif  // IDA_NAMES_H_
