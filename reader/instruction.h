// Copyright 2011-2021 Google LLC
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

// This class provides storage for instruction information. It is targeted
// towards the instructions stored in a BinExport::Flowgraph::Vertex, where
// operands are stored as a std::string.

#ifndef READER_INSTRUCTION_H_
#define READER_INSTRUCTION_H_

#include <string>
#include <vector>

#include "third_party/absl/container/inlined_vector.h"
#include "third_party/absl/types/optional.h"
#include "third_party/zynamics/binexport/architectures.h"
#include "third_party/zynamics/binexport/types.h"

namespace security::binexport {

class Instruction {
 public:
  using CallTargets = absl::InlinedVector<Address, 1>;
  using CommentIndices = absl::InlinedVector<int32_t, 1>;

  Instruction(Address address, const std::string& mnemonic);

  const std::string& mnemonic() const { return mnemonic_; }
  Address address() const { return address_; }

  int index() const { return index_; }
  void set_index(int index) { index_ = index; }

  const std::vector<int>& operands() const { return operand_indices_; }
  void set_operands(const std::vector<int>& operand_indices);

  const CallTargets& call_targets() const { return call_targets_; }
  void set_call_targets(const CallTargets& targets) { call_targets_ = targets; }

  const CommentIndices& comment_indices() const { return comment_indices_; }
  void set_comment_indices(const CommentIndices& comment_indices) {
    comment_indices_ = std::move(comment_indices);
  }

 private:
  Address address_;
  std::string mnemonic_;

  // A backwards reference of the instruction's index in the BinExport proto
  // table.
  int index_;

  // Operand indices from the BinExport2 protocol buffer they were loaded from.
  std::vector<int> operand_indices_;

  // If this is a call instruction, contains potential call targets.
  CallTargets call_targets_;

  // Comment indices from the BinExport2 protocol buffer they were loaded from
  CommentIndices comment_indices_;
};

using Instructions = std::vector<Instruction>;

const Instruction* GetInstruction(const Instructions& instructions,
                                  const Address instruction_address);

// Is this a jump instruction in a given architecture. Returns false for
// unsupported architectures.
bool IsJumpInstruction(const Instruction& instruction,
                       absl::optional<Architecture>);

}  // namespace security::binexport

#endif  // READER_INSTRUCTION_H_
