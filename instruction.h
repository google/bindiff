#ifndef INSTRUCTION_H_
#define INSTRUCTION_H_

#include <unordered_map>
#include <vector>

#include "third_party/zynamics/bindiff/utility.h"

class Instruction {
 public:
  struct Cache {
    typedef std::unordered_map<uint32_t, std::string> PrimeToMnemonic;

    void Clear() {
      prime_to_mnemonic_.clear();
    }

    PrimeToMnemonic prime_to_mnemonic_;
  };

  Instruction(Cache* cache, Address address, const std::string& mnemonic,
              uint32_t prime);
  uint32_t GetPrime() const;
  std::string GetMnemonic(const Cache* cache) const;
  Address GetAddress() const;

 private:
  Address address_;
  uint32_t prime_;
};

typedef std::vector<Instruction> Instructions;
typedef std::vector<std::pair<const Instruction*, const Instruction*> >
    InstructionMatches;

void ComputeLcs(const Instructions::const_iterator& instructions1_begin,
                const Instructions::const_iterator& instructions1_end,
                const Instructions::const_iterator& instructions2_begin,
                const Instructions::const_iterator& instructions2_end,
                InstructionMatches& matches);

#endif  // INSTRUCTION_H_
