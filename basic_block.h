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

#ifndef BASIC_BLOCK_H_
#define BASIC_BLOCK_H_

#include <forward_list>
#include <iosfwd>
#include <memory>
#include <map>
#include <vector>
#undef max

#include "absl/container/btree_map.h"
#include "third_party/zynamics/binexport/instruction.h"
#include "third_party/zynamics/binexport/nested_iterator.h"
#include "third_party/zynamics/binexport/range.h"
#include "third_party/zynamics/binexport/types.h"

class CallGraph;
class FlowGraph;
class BasicBlock;

// This class is used to initialize BasicBlocks. It uses internally a vector
// instead of a linked list for range storage, this way ranges can be appended
// and the container can be cleared without much overhead.
class BasicBlockInstructions {
 public:
  friend class BasicBlock;

  // Adds an instruction by an iterator. The iterator may be used to store the
  // instruction and to access it later. For an instance of this class, the
  // iterator must refer to the same Instructions container in each invocation.
  void AddInstruction(Instructions::iterator instruction);
  void Clear() { ranges_.clear(); }

 private:
  using InstructionRanges = std::vector<InstructionRange>;
  InstructionRanges ranges_;
};

using BasicBlocks = std::vector<BasicBlock*>;

#pragma pack(push, 1)
class BasicBlock {
 private:
  // Important: This must be a sorted container.
  using Cache = absl::btree_map<Address, std::unique_ptr<BasicBlock>>;

  // In most cases there is only one InstructionRange per BasicBlock. Exceptions
  // are overlapping instructions and appended BasicBlocks. A linked list is
  // used because it has the lowest memory overhead with only one element.
  using InstructionRanges = std::forward_list<InstructionRange>;
  using RangeIterator = InstructionRanges::iterator;
  using RangeConstIterator = InstructionRanges::const_iterator;

 public:
  using InstructionIterator = NestedIterator<RangeIterator>;
  using InstructionConstIterator = NestedIterator<RangeConstIterator>;

  // Copying disallowed to avoid deep-copies of ranges_ container.
  BasicBlock(const BasicBlock&) = delete;
  const BasicBlock& operator=(const BasicBlock&) = delete;

  // Returns the basic block at entry_point_address.
  static BasicBlock* Find(Address entry_point_address) {
    const auto pivot(cache_.find(entry_point_address));
    if (pivot != cache_.end() &&
        pivot->second->GetEntryPoint() == entry_point_address) {
      return pivot->second.get();
    }
    return nullptr;
  }

  // Returns the basic block containing address, if any.
  static BasicBlock* FindContaining(Address address) {
    auto pivot(cache_.lower_bound(address));
    if (pivot != cache_.end() && pivot->second->GetEntryPoint() == address) {
      return pivot->second.get();
    }

    // We have not found a basic block at address. Next we search for a basic
    // block containing address. We try to do this efficiently. Basic blocks do
    // not necessarily have strictly increasing addresses (due to basic block
    // merging or overlapping blocks), so we need to perform an exhaustive
    // search. However, we know the likeliest location for a match will be
    // somewhere in the immediate vicinity of address, so we search from pivot
    // outwards. We also check the last address first before iterating all
    // instructions of a basic block. The rationale is that most calls will
    // either be looking for the entry point or exit point address of a basic
    // block.
    Cache::reverse_iterator left(pivot);
    Cache::iterator right(pivot);
    for (; left != cache_.rend() || right != cache_.end();) {
      if (left != cache_.rend()) {
        const auto* basic_block = left->second.get();
        if (basic_block->GetLastAddress() == address ||
            basic_block->GetInstruction(address) != basic_block->end()) {
          return left->second.get();
        }
        ++left;
      }
      if (right != cache_.end()) {
        const auto* basic_block = right->second.get();
        if (basic_block->GetLastAddress() == address ||
            basic_block->GetInstruction(address) != basic_block->end()) {
          return right->second.get();
        }
        ++right;
      }
    }
    return nullptr;
  }

  // Returns nullptr if instructions is empty or if a block already exists
  // at the same entry point.
  static BasicBlock* Create(BasicBlockInstructions* instructions);

  static void DestroyCache() { Cache().swap(cache_); }

  static Cache& blocks() { return cache_; }

  void set_id(int id) { id_ = id; }
  int id() const { return id_; }

  // begin() and end() implemented to support range based for loops.
  // Instruction flags may be modified, need non-const access too.
  InstructionIterator begin() {
    return InstructionIterator(ranges_.begin(), ranges_.end());
  }
  InstructionIterator end() { return InstructionIterator(ranges_.end()); }
  InstructionConstIterator begin() const {
    return InstructionConstIterator(ranges_.begin(), ranges_.end());
  }
  InstructionConstIterator end() const {
    return InstructionConstIterator(ranges_.end());
  }

  InstructionConstIterator GetInstruction(Address address) const;
  Address GetLastAddress() const {
    auto last(BeforeEndRange());
    return ((last->end() - 1)->GetAddress());
  }
  Address GetEntryPoint() const {
    auto entry_point(begin());
    return entry_point->GetAddress();
  }

  void AppendBlock(const BasicBlock& other) { Append(&other.ranges_); }

  int GetInstructionCount() const;
  void Render(std::ostream* stream, const CallGraph& call_graph,
              const FlowGraph& flow_graph) const;

 private:
  explicit BasicBlock(BasicBlockInstructions* instructions) : id_(-1) {
    Append(&instructions->ranges_);
  }

  template <typename Container>
  void Append(Container* other_ranges) {
    auto last(BeforeEndRange());

    for (auto& other_range : *other_ranges) {
      if (!other_range.empty()) {
        last = ranges_.insert_after(last, other_range);
      }
    }
  }

  RangeConstIterator BeforeEndRange() const;

  static Cache cache_;

  int id_;  // Careful: This might not stay constant for shared basic blocks.
  InstructionRanges ranges_;
};
#pragma pack(pop)

#endif  // BASIC_BLOCK_H_
