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

#include "third_party/zynamics/binexport/virtual_memory.h"

bool AddressSpace::AddMemoryBlock(Address address, const MemoryBlock& block,
                                  int flags) {
  auto it = data_.upper_bound(address);
  if (it != data_.end() && it->first < address + block.size()) {
    // Intersecting the next block.
    return false;
  }
  if (it != data_.begin()) {
    --it;
    if (it->first + it->second.size() > address) {
      // Intersecting the preceding block.
      return false;
    }
  }

  return data_.emplace(address, block).second &&
         flags_.emplace(address, flags).second;
}

AddressSpace::Data::const_iterator AddressSpace::GetMemoryBlock(
    Address address) const {
  auto it = data_.upper_bound(address);
  if (it != data_.begin()) {
    --it;
    if (it->first <= address && it->first + it->second.size() > address) {
      return it;
    }
  }
  return data_.end();
}

AddressSpace::Data::iterator AddressSpace::GetMemoryBlock(Address address) {
  auto it = data_.upper_bound(address);
  if (it != data_.begin()) {
    --it;
    if (it->first <= address && it->first + it->second.size() > address) {
      return it;
    }
  }
  return data_.end();
}

const Byte& AddressSpace::operator[](Address address) const {
  auto it = data_.upper_bound(address);
  --it;
  return it->second[address - it->first];
}

Byte& AddressSpace::operator[](Address address) {
  auto it = data_.upper_bound(address);
  --it;
  return it->second[address - it->first];
}

bool AddressSpace::IsValidAddress(Address address) const {
  return GetMemoryBlock(address) != data_.end();
}

bool AddressSpace::IsReadable(Address address) const {
  return GetFlags(address) & kRead;
}

bool AddressSpace::IsWritable(Address address) const {
  return GetFlags(address) & kWrite;
}

bool AddressSpace::IsExecutable(Address address) const {
  return GetFlags(address) & kExecute;
}

int AddressSpace::GetFlags(Address address) const {
  auto memory_it = GetMemoryBlock(address);
  if (memory_it == data_.end()) {
    return 0;
  }
  auto flags_it = flags_.find(memory_it->first);
  if (flags_it != flags_.end()) {
    return flags_it->second;
  }
  return 0;
}

// TODO(user) It is somewhat unexpected to have a size method be O(N). Maybe
//     cache the size value in the class?
size_t AddressSpace::size() const {
  size_t value = 0;
  for (const auto& block : data_) {
    value += block.second.size();
  }
  return value;
}
