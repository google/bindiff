#ifndef PRIME_SIGNATURE_H_
#define PRIME_SIGNATURE_H_

#include "third_party/zynamics/binexport/types.h"

namespace security {
namespace bindiff {

// Computes the result of raising a number to a non-negative integral power.
// baze^exp is computed in a way that is faster than std::pow (which supports
// arbitrary real exponents).
uint32_t IPow32(uint32_t base, uint32_t exp);

// Returns a numeric signature for the (short) string given in mnemonic. The
// number returned is not a prime number, but a modified Gödel encoding of the
// given mnemonic string. This is used in the instruction cache to uniquely
// identify mnemonics and in the basic block LCS algorithm.
// Note: This function is used instead of perfect hashing of the instruction
// mnemonics in the files participating in a BinDiff run. This is because
// historically, the values returned by this function were persisted to disk in
// the BinExport v1 format. Thus the mnemonic space was unbounded which made
// perfect hashing infeasible.
// TODO(cblichmann): Rename to GetMnemonicId().
uint32_t GetPrime(const std::string& mnemonic);

}  // namespace bindiff
}  // namespace security

#endif  // PRIME_SIGNATURE_H_
