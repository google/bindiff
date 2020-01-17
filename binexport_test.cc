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

#include "third_party/zynamics/binexport/binexport.h"

#include "gmock/gmock.h"
#include "gtest/gtest.h"

using ::testing::Eq;

namespace security::binexport {
namespace {

Address AddInstruction(Address start_address, int8_t size, BinExport2* proto) {
  auto* instruction = proto->add_instruction();
  if (start_address != 0) {
    instruction->set_address(start_address);
  }
  instruction->mutable_raw_bytes()->resize(size, 'A');
  return start_address + size;
}

TEST(BinExportTest, TestInstructionAddress) {
  BinExport2 proto;
  AddInstruction(/*start_address=*/0x10000000, /*size=*/4, &proto);
  AddInstruction(/*start_address=*/0, /*size=*/8, &proto);
  AddInstruction(/*start_address=*/0, /*size=*/4, &proto);
  AddInstruction(/*start_address=*/0, /*size=*/8, &proto);
  AddInstruction(/*start_address=*/0x10000100, /*size=*/4, &proto);
  AddInstruction(/*start_address=*/0, /*size=*/8, &proto);
  AddInstruction(/*start_address=*/0, /*size=*/4, &proto);
  AddInstruction(/*start_address=*/0, /*size=*/8, &proto);
  AddInstruction(/*start_address=*/0x20000000, /*size=*/4, &proto);
  AddInstruction(/*start_address=*/0x20000004, /*size=*/4, &proto);
  AddInstruction(/*start_address=*/0x20000008, /*size=*/4, &proto);

  EXPECT_THAT(GetInstructionAddress(proto, 0), Eq(0x10000000));
  EXPECT_THAT(GetInstructionAddress(proto, 3), Eq(0x10000010));
  EXPECT_THAT(GetInstructionAddress(proto, 7), Eq(0x10000110));
  EXPECT_THAT(GetInstructionAddress(proto, 9), Eq(0x20000004));
}

}  // namespace
}  // namespace security::binexport
