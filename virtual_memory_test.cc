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

#include "third_party/zynamics/binexport/virtual_memory.h"

#include "gtest/gtest.h"

namespace security::binexport {
namespace {

static constexpr Address kBaseAddress = 0x10012345;
static constexpr Byte kTestData1[] = {1, 2,  3,  4,  5,  6,  7,  8,
                                      9, 10, 11, 12, 13, 14, 15, 16};
static constexpr Byte kTestData2[] = {11, 12, 13, 14, 15, 16, 17, 18, 19, 20};

TEST(VirtualMemoryTest, AddMemoryBlock) {
  AddressSpace virtual_memory;
  int flags = AddressSpace::kRead;
  EXPECT_TRUE(virtual_memory.AddMemoryBlock(
      kBaseAddress, AddressSpace::MemoryBlock(10), flags));

  EXPECT_FALSE(virtual_memory.AddMemoryBlock(
      kBaseAddress, AddressSpace::MemoryBlock(10), flags));
  EXPECT_FALSE(virtual_memory.AddMemoryBlock(
      kBaseAddress - 9, AddressSpace::MemoryBlock(10), flags));
  EXPECT_FALSE(virtual_memory.AddMemoryBlock(
      kBaseAddress + 9, AddressSpace::MemoryBlock(10), flags));

  EXPECT_TRUE(virtual_memory.AddMemoryBlock(
      kBaseAddress - 10, AddressSpace::MemoryBlock(10), flags));
  EXPECT_TRUE(virtual_memory.AddMemoryBlock(
      kBaseAddress + 10, AddressSpace::MemoryBlock(10), flags));

  EXPECT_FALSE(virtual_memory.AddMemoryBlock(
      kBaseAddress - 50, AddressSpace::MemoryBlock(100), flags));
  EXPECT_FALSE(virtual_memory.AddMemoryBlock(
      kBaseAddress + 10, AddressSpace::MemoryBlock(100), flags));
  EXPECT_FALSE(virtual_memory.AddMemoryBlock(
      kBaseAddress + 5, AddressSpace::MemoryBlock(20), flags));
}

TEST(VirtualMemoryTest, PagesAreOrdered) {
  AddressSpace virtual_memory;
  int flags = AddressSpace::kRead;
  virtual_memory.AddMemoryBlock(kBaseAddress + 50,
                                AddressSpace::MemoryBlock(10), flags);
  virtual_memory.AddMemoryBlock(kBaseAddress + 30,
                                AddressSpace::MemoryBlock(10), flags);
  virtual_memory.AddMemoryBlock(kBaseAddress + 70,
                                AddressSpace::MemoryBlock(10), flags);
  virtual_memory.AddMemoryBlock(kBaseAddress - 10,
                                AddressSpace::MemoryBlock(10), flags);
  virtual_memory.AddMemoryBlock(kBaseAddress, AddressSpace::MemoryBlock(10),
                                flags);
  Address previous_address = 0;
  for (const auto& block : virtual_memory.data()) {
    EXPECT_LT(previous_address, block.first);
    previous_address = block.first;
  }
  EXPECT_EQ(5, virtual_memory.data().size());
  EXPECT_EQ(50, virtual_memory.size());
}

TEST(VirtualMemoryTest, Access) {
  AddressSpace virtual_memory;
  int flags = AddressSpace::kRead;
  virtual_memory.AddMemoryBlock(
      kBaseAddress, AddressSpace::MemoryBlock(kTestData1, kTestData1 + 10),
      flags);
  virtual_memory.AddMemoryBlock(
      kBaseAddress + 20, AddressSpace::MemoryBlock(kTestData2, kTestData2 + 10),
      flags);

  EXPECT_EQ(1, virtual_memory[kBaseAddress]);
  EXPECT_EQ(2, virtual_memory[kBaseAddress + 1]);
  EXPECT_EQ(10, virtual_memory[kBaseAddress + 9]);
  EXPECT_EQ(20, virtual_memory[kBaseAddress + 29]);
  EXPECT_EQ(10, virtual_memory.GetMemoryBlock(kBaseAddress)->second.size());
  const auto block = virtual_memory.GetMemoryBlock(kBaseAddress + 22);
  ASSERT_NE(block, virtual_memory.data().end());
  EXPECT_EQ(10, block->second.size());
  EXPECT_EQ(kBaseAddress + 20, block->first);
}

TEST(VirtualMemoryTest, ValidAddress) {
  AddressSpace virtual_memory;
  int flags = AddressSpace::kRead;
  EXPECT_FALSE(virtual_memory.IsValidAddress(kBaseAddress));

  virtual_memory.AddMemoryBlock(
      kBaseAddress, AddressSpace::MemoryBlock(kTestData1, kTestData1 + 10),
      flags);

  EXPECT_FALSE(virtual_memory.IsValidAddress(kBaseAddress - 1));
  EXPECT_FALSE(virtual_memory.IsValidAddress(kBaseAddress + 10));
  EXPECT_TRUE(virtual_memory.IsValidAddress(kBaseAddress));
  EXPECT_TRUE(virtual_memory.IsValidAddress(kBaseAddress + 1));
  EXPECT_TRUE(virtual_memory.IsValidAddress(kBaseAddress + 9));

  virtual_memory.AddMemoryBlock(
      kBaseAddress + 20, AddressSpace::MemoryBlock(kTestData2, kTestData2 + 10),
      flags);

  EXPECT_FALSE(virtual_memory.IsValidAddress(kBaseAddress - 1));
  EXPECT_FALSE(virtual_memory.IsValidAddress(kBaseAddress + 10));
  EXPECT_FALSE(virtual_memory.IsValidAddress(kBaseAddress + 19));
  EXPECT_FALSE(virtual_memory.IsValidAddress(kBaseAddress + 30));
  EXPECT_TRUE(virtual_memory.IsValidAddress(kBaseAddress));
  EXPECT_TRUE(virtual_memory.IsValidAddress(kBaseAddress + 1));
  EXPECT_TRUE(virtual_memory.IsValidAddress(kBaseAddress + 9));
  EXPECT_TRUE(virtual_memory.IsValidAddress(kBaseAddress + 20));
  EXPECT_TRUE(virtual_memory.IsValidAddress(kBaseAddress + 21));
  EXPECT_TRUE(virtual_memory.IsValidAddress(kBaseAddress + 29));
}

TEST(VirtualMemoryTest, FlagsNotSet) {
  AddressSpace virtual_memory;
  virtual_memory.AddMemoryBlock(
      kBaseAddress, AddressSpace::MemoryBlock(kTestData1, kTestData1 + 10),
      0 /* no permissions */);

  EXPECT_FALSE(virtual_memory.IsReadable(kBaseAddress));
  EXPECT_FALSE(virtual_memory.IsWritable(kBaseAddress));
  EXPECT_FALSE(virtual_memory.IsExecutable(kBaseAddress));
}

TEST(VirtualMemoryTest, FlagsRead) {
  AddressSpace virtual_memory;
  int flags = AddressSpace::kRead;
  virtual_memory.AddMemoryBlock(
      kBaseAddress, AddressSpace::MemoryBlock(kTestData1, kTestData1 + 10),
      flags);
  EXPECT_TRUE(virtual_memory.IsReadable(kBaseAddress));
  EXPECT_FALSE(virtual_memory.IsWritable(kBaseAddress));
  EXPECT_FALSE(virtual_memory.IsExecutable(kBaseAddress));
}

TEST(VirtualMemoryTest, FlagsWrite) {
  AddressSpace virtual_memory;
  int flags = AddressSpace::kWrite;
  virtual_memory.AddMemoryBlock(
      kBaseAddress, AddressSpace::MemoryBlock(kTestData1, kTestData1 + 10),
      flags);
  EXPECT_FALSE(virtual_memory.IsReadable(kBaseAddress));
  EXPECT_TRUE(virtual_memory.IsWritable(kBaseAddress));
  EXPECT_FALSE(virtual_memory.IsExecutable(kBaseAddress));
}

TEST(VirtualMemoryTest, FlagsExecute) {
  AddressSpace virtual_memory;
  int flags = AddressSpace::kExecute;
  virtual_memory.AddMemoryBlock(
      kBaseAddress, AddressSpace::MemoryBlock(kTestData1, kTestData1 + 10),
      flags);
  EXPECT_FALSE(virtual_memory.IsReadable(kBaseAddress));
  EXPECT_FALSE(virtual_memory.IsWritable(kBaseAddress));
  EXPECT_TRUE(virtual_memory.IsExecutable(kBaseAddress));
}

TEST(VirtualMemoryTest, FlagsAll) {
  AddressSpace virtual_memory;
  int flags =
      AddressSpace::kExecute | AddressSpace::kWrite | AddressSpace::kRead;

  virtual_memory.AddMemoryBlock(
      kBaseAddress, AddressSpace::MemoryBlock(kTestData1, kTestData1 + 10),
      flags);
  EXPECT_TRUE(virtual_memory.IsReadable(kBaseAddress));
  EXPECT_TRUE(virtual_memory.IsWritable(kBaseAddress));
  EXPECT_TRUE(virtual_memory.IsExecutable(kBaseAddress));
}

TEST(VirtualMemoryTest, FlagsWrongAddress) {
  AddressSpace virtual_memory;
  int flags =
      AddressSpace::kExecute | AddressSpace::kWrite | AddressSpace::kRead;
  virtual_memory.AddMemoryBlock(
      kBaseAddress, AddressSpace::MemoryBlock(kTestData1, kTestData1 + 10),
      flags);
  EXPECT_FALSE(virtual_memory.IsReadable(0));
  EXPECT_FALSE(virtual_memory.IsWritable(0));
  EXPECT_FALSE(virtual_memory.IsExecutable(0));
}

TEST(VirtualMemoryTest, FlagsWithinMemoryRegion) {
  AddressSpace virtual_memory;
  int flags = AddressSpace::kExecute;
  virtual_memory.AddMemoryBlock(
      kBaseAddress, AddressSpace::MemoryBlock(kTestData1, kTestData1 + 10),
      flags);
  EXPECT_FALSE(virtual_memory.IsReadable(kBaseAddress + 5));
  EXPECT_FALSE(virtual_memory.IsWritable(kBaseAddress + 5));
  EXPECT_TRUE(virtual_memory.IsExecutable(kBaseAddress + 5));
}

}  // namespace
}  // namespace security::binexport
