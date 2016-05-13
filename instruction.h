// Copyright 2011-2016 Google Inc. All Rights Reserved.
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

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_INSTRUCTION_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_INSTRUCTION_H_

#include <ostream>
#include <string>
#include <vector>

#include <boost/unordered_set.hpp>  // NOLINT

#include "third_party/zynamics/binexport/operand.h"
#include "third_party/zynamics/binexport/range.h"

class FlowGraph;
class AddressSpace;

enum {
  FLAG_NONE = 0,

  // Used in main disassembly loop: instruction address already visited.
  FLAG_VISITED = 1 << 0,

  // This is a flow instruction and continues to the next (as opposed to jmps or
  // calls).
  // TODO(user) As olonho@ pointed out, this flag is used somewhat
  // inconsistently throughout the code base. We should be using the following
  // definition: if the flag is set, execution is guaranteed to flow to the next
  // instruction. I.e. branches and switches do not have it set. Calls may or
  // may not, depending on whether they return.
  FLAG_FLOW = 1 << 1,

  // This instruction has already been exported (used in database writer).
  FLAG_EXPORTED = 1 << 2,

  // This instruction is a branch, conditional or unconditional. Currently only
  // set by Bea. Intentionally the same value as FLAG_EXPORTED as they are both
  // used in isolated and independent contexts and can be reset/forgotten after
  // use.
  FLAG_BRANCH = 1 << 2,

  // The instruction was matched by the library signatures.
  FLAG_LIB = 1 << 3,

  // This instruction is invalid.
  FLAG_INVALID = 1 << 4,

  // This instruction is a call.
  FLAG_CALL = 1 << 5,

  // A switch table or vtable starts at this address.
  FLAG_TABLE_START = 1 << 6,

  // This instruction is a NOP instruction.
  FLAG_NOP = 1 << 7,
};

#pragma pack(push, 1)
class Instruction {
 public:
  typedef std::string (*GetBytesCallback)(const Instruction& instruction);
  typedef boost::unordered_set<std::string, boost::hash<std::string>,
                               std::equal_to<std::string>> StringCache;

  explicit Instruction(Address address, Address next_instruction = 0,
                       uint16_t size = 0, const std::string& mnemonic = "",
                       const Operands& operands = Operands());
  Instruction& operator=(const Instruction&);
  Instruction(const Instruction&);
  ~Instruction();

  static const std::string* CacheString(const std::string& string);
  static void SetBitness(int bitness);
  static int GetBitness();
  static void SetGetBytesCallback(GetBytesCallback callback);
  static void SetMemoryFlags(AddressSpace* flags);
  static void SetVirtualMemory(AddressSpace* virtual_memory);
  static bool IsNegativeValue(int64_t value);

  uint8_t GetOperandCount() const;
  Operands::iterator begin() const;
  Operands::iterator end() const;
  Operands::const_iterator cbegin() const;
  Operands::const_iterator cend() const;
  const Operand& GetOperand(int operand_number) const;
  Address GetAddress() const;
  int GetSize() const;
  Address GetNextInstruction() const;
  void SetNextInstruction(Address address);
  const std::string& GetMnemonic() const;
  std::string GetBytes() const;
  uint16_t GetInDegree() const;
  void AddInEdge();
  void Render(std::ostream* stream, const FlowGraph& flow_graph) const;

  bool IsFlow() const;
  bool IsExported() const;
  static bool IsExported(Address address);
  void SetExported(bool exported);
  void SetFlag(unsigned char flag, bool value);
  bool HasFlag(unsigned char flag) const;

 private:
  const std::string* mnemonic_;  // 4|8 + overhead in stringcache
  Address address_;              // 8
  uint32_t operand_index_;       // 4 + overhead in operand pointer vector
  uint8_t operand_count_;        // 1
  uint16_t in_degree_;           // 2 TODO(user) in-degree count in edges.
  uint8_t size_;                 // 1

  static int instance_count_;
  static StringCache string_cache_;
  static Operands operands_;
  static int bitness_;
  static GetBytesCallback get_bytes_callback_;
  static AddressSpace* flags_;
  static AddressSpace* virtual_memory_;
};
#pragma pack(pop)

// TODO(user): Remove this hack. It's here because IDA defines a global
//                 variable of the same name.
namespace detego {
typedef std::vector<Instruction> Instructions;
}  // namespace detego
using namespace detego;

typedef Range<detego::Instructions> InstructionRange;

void SortInstructions(detego::Instructions* instructions);
Instructions::const_iterator GetInstructionFromRange(
    const InstructionRange& range, Address address);
Instructions::iterator GetInstructionFromRange(InstructionRange* range,
                                               Address address);
Instructions::iterator GetInstruction(detego::Instructions* instructions,
                                      Address address);
Instructions::iterator GetNextInstruction(detego::Instructions* instructions,
                                          Instructions::iterator instruction);
std::string RenderOperands(const Instruction& instruction,
                           const FlowGraph& flow_graph);

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_INSTRUCTION_H_
