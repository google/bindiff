#ifndef PRIME_SIGNATURE_H_
#define PRIME_SIGNATURE_H_

#include "third_party/zynamics/binexport/types.h"

namespace security {
namespace bindiff {

uint32_t GetPrime(const string& mnemonic);

}  // namespace bindiff
}  // namespace security

#endif  // PRIME_SIGNATURE_H_
