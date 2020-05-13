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

// Tests the functionality of the Instruction class.

#include "third_party/zynamics/binexport/reader/instruction.h"

#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "third_party/zynamics/binexport/binexport.h"

namespace security::binexport {
namespace {

TEST(InstructionTest, FindInstructionValid) {
  const std::vector<Instruction> instructions{{10000000, "mnemonic0"},
                                              {10001000, "mnemonic1"},
                                              {10002000, "mnemonic2"},
                                              {10003000, "mnemonic3"},
                                              {10004000, "mnemonic4"}};
  EXPECT_EQ(&instructions[2], GetInstruction(instructions, 10002000));
}

TEST(InstructionTest, FindInstructionInvalid) {
  const std::vector<Instruction> instructions{{10000000, "mnemonic0"},
                                              {10001000, "mnemonic1"},
                                              {10002000, "mnemonic2"},
                                              {10003000, "mnemonic3"},
                                              {10004000, "mnemonic4"}};
  EXPECT_EQ(nullptr, GetInstruction(instructions, 12345678));
}

TEST(InstructionTest, GetInstructionAddresses) {
  const int kNumInstructions = 51;
  std::vector<BinExport2::Instruction> instructions(kNumInstructions);
  for (int i = 0; i * i < kNumInstructions; ++i) {
    instructions[i * i].set_address(100 * i);
  }
  for (int i = 0; i < kNumInstructions; ++i) {
    instructions[i].set_raw_bytes("\5\4");
  }

  BinExport2 binexport_proto;
  std::copy(
      instructions.begin(), instructions.end(),
      RepeatedPtrFieldBackInserter(binexport_proto.mutable_instruction()));

  std::vector<Address> addresses = GetAllInstructionAddresses(binexport_proto);
  for (int i = 0; i * i < 51; ++i) {
    for (int j = i * i; j < (i + 1) * (i + 1) && j < kNumInstructions; ++j) {
      EXPECT_EQ(100 * i + (j - i * i) * 2, addresses[j]);
    }
  }
}

}  // namespace
}  // namespace security::binexport
