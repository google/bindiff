#ifndef IDA_NAMES_H_
#define IDA_NAMES_H_

#include <string>

#include "third_party/zynamics/binexport/types.h"

string GetName(Address address);
string GetDemangledName(Address address);
string GetLineComments(Address address, int direction);

#endif  // IDA_NAMES_H_
