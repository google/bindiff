// Copyright 2011-2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#ifndef INSTRUCTION_H_
#define INSTRUCTION_H_

#include <string>
#include <unordered_map>
#include <vector>

#include "base/integral_types.h"
#include "third_party/zynamics/bindiff/utility.h"

namespace security::bindiff {

class Instruction {
 public:
  using Cache = std::unordered_map<uint32_t, std::string>;

  Instruction(Cache* cache, Address address, const std::string& mnemonic,
              uint32_t prime);
  uint32_t GetPrime() const;
  std::string GetMnemonic(const Cache* cache) const;
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

}  // namespace security::bindiff

#endif  // INSTRUCTION_H_
