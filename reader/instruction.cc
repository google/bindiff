// Copyright 2011-2019 Google LLC. All Rights Reserved.
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

#include "third_party/zynamics/binexport/reader/instruction.h"

namespace security::binexport {

Instruction::Instruction(Address address, const std::string& mnemonic)
    : address_(address), mnemonic_(mnemonic) {}

void Instruction::set_operands(const std::vector<int>& operand_indices) {
  operand_indices_ = operand_indices;
}

const Instruction* GetInstruction(const Instructions& instructions,
                                  const Address instruction_address) {
  for (const auto& instruction : instructions) {
    if (instruction.address() == instruction_address) {
      return &instruction;
    }
  }
  return nullptr;
}

bool IsJumpInstruction(const Instruction& instruction,
                       absl::optional<Architecture> architecture) {
  // TODO(b/114701180): Implement this function, at least for ARM and AArch64.
  // TODO(xmsm): false is a good default, but should we return it for
  //             unsupported architectures, or signal an error instead?
  return false;
}

}  // namespace security::binexport
