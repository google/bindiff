// Copyright 2011-2017 Google Inc. All Rights Reserved.
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

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_VIRTUAL_MEMORY_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_VIRTUAL_MEMORY_H_

#include <atomic>
#include <map>
#include <vector>

#include "third_party/zynamics/binexport/types.h"

class AddressSpace {
 public:
  enum {
    kRead = 1 << 0,    // Address space is readable.
    kWrite = 1 << 1,   // Address space is writable.
    kExecute = 1 << 2  // Address space is executable.
  };

  typedef std::vector<Byte> MemoryBlock;
  typedef std::map<Address, MemoryBlock> Data;
  typedef std::map<Address, int> Flags;
  typedef std::map<Address, int> Ids;

  // Copies the block. Returns true iff the block was added successfully, false
  // if the block overlaps with existing memory.
  bool AddMemoryBlock(Address address, const MemoryBlock& block, int flags);

  // Returns the memory block containing address.
  Data::const_iterator GetMemoryBlock(Address address) const;

  Data::iterator GetMemoryBlock(Address address);

  // Returns true iff address is mapped in this address space, i.e. falls into
  // one of the memory blocks owned by this class.
  bool IsValidAddress(Address address) const;

  // Returns true iff the MemoryBlock at this address is readable.
  bool IsReadable(Address address) const;

  // Returns true iff the MemoryBlock at this address is writable.
  bool IsWritable(Address address) const;

  // Returns true iff the MemoryBlock at this address is executable.
  bool IsExecutable(Address address) const;

  // Get flags for a specific address:
  int GetFlags(Address address) const;

  // Size of the entire address space in bytes. Runtime O(number of memory
  // blocks).
  size_t size() const;

  // Read only address to the map of memory blocks. Sorted by ascending address.
  const Data& data() const { return data_; }

  // Access the byte at address. Undefined behavior if address is not mapped in
  // this AddressSpace.
  const Byte& operator[](Address address) const;
  Byte& operator[](Address address);

  // Interprets the bytes at address as a little endian value and stores the
  // results. Returns true if the read was successful.
  template <typename T>
  bool ReadLittleEndian(Address address, T* data) const;
  template <typename T>
  bool ReadLittleEndian(const MemoryBlock& memory_block,
                        MemoryBlock::size_type index, T* data) const;

 private:
  Data data_;
  Flags flags_;
};

template <typename T>
bool AddressSpace::ReadLittleEndian(Address address, T* data) const {
  const auto memory_block = GetMemoryBlock(address);
  if (memory_block == data_.end()) {
    return false;
  }
  return ReadLittleEndian(memory_block->second, address - memory_block->first,
                          data);
}

template <typename T>
bool AddressSpace::ReadLittleEndian(const MemoryBlock& memory_block,
                                    MemoryBlock::size_type index,
                                    T* data) const {
  if (!data || index + sizeof(T) > memory_block.size()) {
    return false;
  }
  *data = 0;
  for (T i = 0; i < sizeof(T); ++i) {
    *data |= static_cast<T>(memory_block[index + i]) << (i * 8);
  }
  return true;
}

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_VIRTUAL_MEMORY_H_
