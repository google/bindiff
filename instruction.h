#ifndef INSTRUCTION_H_
#define INSTRUCTION_H_

#include <unordered_map>
#include <vector>

#include "third_party/zynamics/bindiff/utility.h"

namespace security {
namespace bindiff {

class Instruction {
 public:
  struct Cache {
    using PrimeToMnemonic = std::unordered_map<uint32_t, string>;

    void Clear() {
      prime_to_mnemonic_.clear();
    }

    PrimeToMnemonic prime_to_mnemonic_;
  };

  Instruction(Cache* cache, Address address, const string& mnemonic,
              uint32_t prime);
  uint32_t GetPrime() const;
  string GetMnemonic(const Cache* cache) const;
  Address GetAddress() const;

 private:
  Address address_;
  uint32_t prime_;
};

using Instructions = std::vector<Instruction>;
using InstructionMatches =
    std::vector<std::pair<const Instruction*, const Instruction*>>;

void ComputeLcs(const Instructions::const_iterator& instructions1_begin,
                const Instructions::const_iterator& instructions1_end,
                const Instructions::const_iterator& instructions2_begin,
                const Instructions::const_iterator& instructions2_end,
                InstructionMatches& matches);

}  // namespace bindiff
}  // namespace security

#endif  // INSTRUCTION_H_
