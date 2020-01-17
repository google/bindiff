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

#include "base/logging.h"
#include "third_party/zynamics/binexport/binexport.h"

namespace security::binexport {

Address GetInstructionAddress(const BinExport2& proto, int index) {
  // Either the instruction has an address stored directly or we keep iterating
  // previous instructions until we find one that does, adding the correct
  // offset. This assumes that there will always be a previous instruction with
  // an absolute address.
  const BinExport2::Instruction* instruction = &proto.instruction(index);
  if (instruction->has_address()) {
    return instruction->address();
  }
  int delta = 0;
  for (--index; index >= 0; --index) {
    instruction = &proto.instruction(index);
    delta += instruction->raw_bytes().size();
    if (instruction->has_address()) {
      return instruction->address() + delta;
    }
  }
  LOG(QFATAL) << "Invalid instruction index";
  return 0;  // Not reached.
}

std::vector<Address> GetAllInstructionAddresses(const BinExport2& proto) {
  std::vector<Address> result;
  if (proto.instruction_size() == 0) {
    return result;
  }
  QCHECK(proto.instruction(0).has_address());
  result.reserve(proto.instruction_size());
  Address address = 0;
  Address next_address = 0;
  for (const auto& instruction : proto.instruction()) {
    if (instruction.has_address()) {
      address = instruction.address();
      next_address = address + instruction.raw_bytes().size();
    } else {
      address = next_address;
      next_address += instruction.raw_bytes().size();
    }
    result.push_back(address);
  }
  return result;
}

}  // namespace security::binexport
