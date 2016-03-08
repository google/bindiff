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

#include "third_party/zynamics/binexport/instruction.h"

#include <iomanip>
#include <list>
#include <memory>
#include <sstream>
#include <tuple>

#include "base/logging.h"
#include "third_party/zynamics/binexport/flow_graph.h"
#include "third_party/zynamics/binexport/virtual_memory.h"

namespace {

struct TreeNode {
  typedef std::list<TreeNode*> Children;
  explicit TreeNode(const Expression* expression) : expression_(expression) {}

  const Expression* expression_;
  Children children_;  // Should be stored sorted by position.
};

typedef std::vector<std::shared_ptr<TreeNode>> Tree;

// TODO(user) immediate_float are rendered as hex and aren't properly
//     checked for their sign.
void RenderExpression(std::ostream* stream, const TreeNode& node,
                      bool prefix_type, int substitution_id,
                      const std::string& substitution) {
  const Expression* expression = node.expression_;
  if (expression->GetId() == substitution_id) {
    *stream << substitution;
    return;
  }
  int8_t expression_type = expression->GetType();
  switch (expression_type) {
    case Expression::TYPE_SIZEPREFIX: {
      if ((expression->GetSymbol() != "b4" &&
           Instruction::GetBitness() == 32) ||
          (expression->GetSymbol() != "b8" &&
           Instruction::GetBitness() == 64)) {
        if (prefix_type) {
          stream->write(reinterpret_cast<const char*>(&expression_type),
                        sizeof(expression_type));
        }
        *stream << expression->GetSymbol() << " ";
      }
      for (auto child : node.children_) {
        RenderExpression(stream, *child, prefix_type, substitution_id,
                         substitution);
      }
      break;
    }
    case Expression::TYPE_REGISTER:
    case Expression::TYPE_SYMBOL: {
      if (prefix_type) {
        stream->write(reinterpret_cast<const char*>(&expression_type),
                      sizeof(expression_type));
      }
      *stream << expression->GetSymbol();
      break;
    }
    case Expression::TYPE_OPERATOR: {
      if (node.children_.size() > 1 && expression->GetSymbol() != "{") {
        for (auto it = node.children_.begin(), end = node.children_.end();
             it != end; ++it) {
          RenderExpression(stream, **it, prefix_type, substitution_id,
                           substitution);
          TreeNode::Children::const_iterator j = it;
          if (++j != node.children_.end()) {
            if (prefix_type) {
              stream->write(reinterpret_cast<const char*>(&expression_type),
                            sizeof(expression_type));
            }
            if (expression->GetSymbol() == "+" &&
                (*j)->expression_->IsImmediate()) {
              if (Instruction::IsNegativeValue(
                      (*j)->expression_->GetImmediate()) &&
                  (*j)->expression_->GetSymbol().empty()) {
                // Don't render anything or we'll get: eax+-12
              } else if ((*j)->expression_->GetImmediate() == 0) {
                // Skip "+0".
                it = j;
                continue;
              } else {
                *stream << expression->GetSymbol();
              }
            } else {
              *stream << expression->GetSymbol();
            }
          }
        }
      } else if (expression->GetSymbol() == "{") {
        if (prefix_type) {
          stream->write(reinterpret_cast<const char*>(&expression_type),
                        sizeof(expression_type));
        }
        *stream << "{";
        for (auto it = node.children_.begin(), end = node.children_.end();
             it != end; ++it) {
          RenderExpression(stream, **it, prefix_type, substitution_id,
                           substitution);
          TreeNode::Children::const_iterator j = it;
          if (++j != node.children_.end()) {
            if (prefix_type) {
              stream->write(reinterpret_cast<const char*>(&expression_type),
                            sizeof(expression_type));
            }
            *stream << ",";
          }
        }
        if (prefix_type) {
          stream->write(reinterpret_cast<const char*>(&expression_type),
                        sizeof(expression_type));
        }
        *stream << "}";
      } else {
        if (prefix_type) {
          stream->write(reinterpret_cast<const char*>(&expression_type),
                        sizeof(expression_type));
        }
        *stream << expression->GetSymbol();
        for (auto child : node.children_) {
          RenderExpression(stream, *child, prefix_type, substitution_id,
                           substitution);
        }
      }
      break;
    }
    case Expression::TYPE_DEREFERENCE: {
      if (prefix_type) {
        stream->write(reinterpret_cast<const char*>(&expression_type),
                      sizeof(expression_type));
      }
      *stream << "[";
      for (auto child : node.children_) {
        RenderExpression(stream, *child, prefix_type, substitution_id,
                         substitution);
      }
      if (prefix_type) {
        stream->write(reinterpret_cast<const char*>(&expression_type),
                      sizeof(expression_type));
      }
      *stream << "]";
      break;
    }
    case Expression::TYPE_IMMEDIATE_INT:
    case Expression::TYPE_IMMEDIATE_FLOAT: {
      if (expression->GetSymbol().empty()) {
        if (prefix_type) {
          stream->write(reinterpret_cast<const char*>(&expression_type),
                        sizeof(expression_type));
        }
        if ((Instruction::IsNegativeValue(expression->GetImmediate()) &&
             expression->GetParent() &&
             expression->GetParent()->GetSymbol() == "+") ||
            expression->GetImmediate() <= 9) {
          *stream << std::dec << std::setw(0)
                  << (Instruction::GetBitness() == 32
                          ? static_cast<int32_t>(expression->GetImmediate())
                          : static_cast<int64_t>(expression->GetImmediate()));
        } else {
          *stream << "0x" << std::hex << std::uppercase
                  << (Address)expression->GetImmediate();
        }
      } else {
        // Output the expression substitution instead of the actual value.
        if (prefix_type) {
          stream->write(reinterpret_cast<const char*>(&expression_type),
                        sizeof(expression_type));
        }
        *stream << expression->GetSymbol();
      }
      break;
    }
    case Expression::TYPE_GLOBALVARIABLE:
    case Expression::TYPE_JUMPLABEL:
    case Expression::TYPE_STACKVARIABLE:
    case Expression::TYPE_FUNCTION: {
      if (prefix_type) {
        stream->write(reinterpret_cast<const char*>(&expression_type),
                      sizeof(expression_type));
      }
      *stream << expression->GetSymbol();
      break;
    }
    default:
      *stream << "Unknown expression type in RenderExpression.";
      LOG(INFO) << "Unknown expression type in RenderExpression.";
      break;
  }
}

FlowGraph::Substitutions::const_iterator GetSubstitution(
    Address address, int operand_num,
    FlowGraph::Substitutions::const_iterator subst_begin,
    FlowGraph::Substitutions::const_iterator subst_end,
    std::string* substitution, int* expression_id) {
  auto it = subst_begin;
  while (it != subst_end && std::get<0>(it->first) == address &&
         std::get<1>(it->first) < operand_num) {
    ++it;
  }
  if (it != subst_end) {
    const Address subst_address = std::get<0>(it->first);
    const int subst_operand = std::get<1>(it->first);
    if (subst_address == address && subst_operand == operand_num) {
      *expression_id = std::get<2>(it->first);
      *substitution = *it->second;
    }
  }
  return it;
}

}  // namespace

std::string RenderOperands(const Instruction& instruction,
                           const FlowGraph& flow_graph, bool prefix_type) {
  if (!instruction.GetOperandCount()) {
    return "";
  }

  auto subst_it = flow_graph.GetSubstitutions().lower_bound(
      FlowGraph::Ref(instruction.GetAddress(), 0, 0));
  const auto subst_end = flow_graph.GetSubstitutions().end();
  while (subst_it != subst_end &&
         std::get<0>(subst_it->first) < instruction.GetAddress()) {
    ++subst_it;
  }

  Tree tree;
  std::stringstream stream;
  int operand_num = 0;
  for (auto instruction_it = instruction.GetFirstOperand(),
            instruction_end = instruction.GetLastOperand();
       instruction_it != instruction_end; ++operand_num) {
    const Operand& operand = **instruction_it;
    for (auto expr_it = operand.GetFirstExpression(),
              expr_end = operand.GetLastExpression();
         expr_it != expr_end; ++expr_it) {
      const Expression* expression = *expr_it;
      tree.emplace_back(std::make_shared<TreeNode>(expression));
      for (auto it = tree.rbegin(), tree_end = tree.rend(); it != tree_end;
           ++it) {
        if ((*it)->expression_ == expression->GetParent()) {
          (*it)->children_.push_back(&**tree.rbegin());
          break;
        }
      }
    }

    if (!tree.empty()) {
      std::string substitution;
      int expression_id = 0;
      subst_it =
          GetSubstitution(instruction.GetAddress(), operand_num, subst_it,
                          subst_end, &substitution, &expression_id);
      if (!substitution.empty()) {
        // TODO(user): Stupid hack: There can only be substitutions for
        //                 local and global structures and only local structs
        //                 will ever contain "+".
        if (prefix_type) {
          const auto expression_type =
              static_cast<uint8_t>(substitution.find("+") != std::string::npos
                                       ? Expression::TYPE_STACKVARIABLE
                                       : Expression::TYPE_GLOBALVARIABLE);
          substitution =
              std::string(reinterpret_cast<const char*>(&expression_type),
                          sizeof(expression_type)) +
              substitution;
        }
      }
      RenderExpression(&stream, **tree.begin(), prefix_type, expression_id,
                       substitution);
    }
    tree.clear();

    if (++instruction_it != instruction_end) {
      if (prefix_type) {
        const uint8_t expression_type = Expression::TYPE_NEWOPERAND;
        stream.write(reinterpret_cast<const char*>(&expression_type),
                     sizeof(expression_type));
      }
      stream << ", ";
    }
  }
  return stream.str();
}

void SortInstructions(Instructions* instructions) {
  std::sort(instructions->begin(), instructions->end(),
            [](const Instruction& one, const Instruction& two) {
              return one.GetAddress() < two.GetAddress();
            });
}

Instructions::const_iterator GetInstructionFromRange(
    const InstructionRange& range, Address address) {
  auto it = std::lower_bound(
      range.begin(), range.end(), address,
      [](const Instruction& i, Address a) { return i.GetAddress() < a; });
  return (it != range.end() && it->GetAddress() == address) ? it : range.end();
}

Instructions::iterator GetInstructionFromRange(InstructionRange* range,
                                               Address address) {
  auto it = std::lower_bound(
      range->begin(), range->end(), address,
      [](const Instruction& i, Address a) { return i.GetAddress() < a; });
  return (it != range->end() && it->GetAddress() == address) ? it
                                                             : range->end();
}

Instructions::iterator GetInstruction(Instructions* instructions,
                                      Address address) {
  InstructionRange range(instructions);
  return GetInstructionFromRange(&range, address);
}

Instructions::iterator GetNextInstruction(Instructions* instructions,
                                          Instructions::iterator instruction) {
  Address next_instruction_address = instruction->GetNextInstruction();
  if (!next_instruction_address) {
    return instructions->end();
  }

  // Try the cheap way: Simply check whether the next instruction
  // is the one we need if it isn't: check the next.
  Instructions::iterator next_instruction;
  for (next_instruction = instruction + 1;
       next_instruction != instructions->end() &&
       next_instruction->GetAddress() < next_instruction_address;
       ++next_instruction) {
  }

  if (next_instruction != instructions->end() &&
      next_instruction->GetAddress() == next_instruction_address) {
    return next_instruction;
  }

  return instructions->end();
}

int Instruction::instance_count_ = 0;
Instruction::StringCache Instruction::string_cache_;
Operands Instruction::operands_;
int Instruction::bitness_ = 32;
Instruction::GetBytesCallback Instruction::get_bytes_callback_ = 0;
AddressSpace* Instruction::flags_ = nullptr;
AddressSpace* Instruction::virtual_memory_ = nullptr;

Instruction::Instruction(Address address, Address next_instruction,
                         uint16_t size, const std::string& mnemonic,
                         const Operands& operands)
    : mnemonic_(CacheString(mnemonic)),
      address_(address),
      operand_index_(operands_.size()),
      operand_count_(operands.size()),
      in_degree_(0),
      size_(size) {
  std::copy(operands.begin(), operands.end(), std::back_inserter(operands_));
  ++instance_count_;
  assert(flags_);
  if (flags_->IsValidAddress(address_)) {
    if (next_instruction && next_instruction > address) {
      SetFlag(FLAG_FLOW, true);
    }
    if (mnemonic_->empty()) {
      SetFlag(FLAG_INVALID, true);
    }
  }
}

const Instruction& Instruction::operator=(const Instruction& one) {
  mnemonic_ = one.mnemonic_;
  address_ = one.address_;
  operand_index_ = one.operand_index_;
  operand_count_ = one.operand_count_;
  in_degree_ = one.in_degree_;
  size_ = one.size_;
  return *this;
}

Instruction::Instruction(const Instruction& one)
    : mnemonic_(one.mnemonic_),
      address_(one.address_),
      operand_index_(one.operand_index_),
      operand_count_(one.operand_count_),
      in_degree_(one.in_degree_),
      size_(one.size_) {
  ++instance_count_;
}

Instruction::~Instruction() {
  if (--instance_count_ == 0) {
    StringCache().swap(string_cache_);
    Operands().swap(operands_);
  }
}

void Instruction::SetBitness(int bitness) { bitness_ = bitness; }

int Instruction::GetBitness() { return bitness_; }

void Instruction::SetGetBytesCallback(GetBytesCallback callback) {
  get_bytes_callback_ = callback;
}

void Instruction::SetMemoryFlags(AddressSpace* flags) { flags_ = flags; }

void Instruction::SetVirtualMemory(AddressSpace* virtual_memory) {
  virtual_memory_ = virtual_memory;
}

void Instruction::Render(std::ostream* stream, const FlowGraph& flow_graph,
                         bool prefix_type) const {
  *stream << GetMnemonic() << " ";
  *stream << RenderOperands(*this, flow_graph, prefix_type);
}

Address Instruction::GetAddress() const { return address_; }

int Instruction::GetSize() const { return size_; }

const std::string& Instruction::GetMnemonic() const {
  assert(mnemonic_);
  return *mnemonic_;
}

std::string Instruction::GetBytes() const {
  if (!get_bytes_callback_ && virtual_memory_) {
    // TODO(user) Optimize this so we don't need a call to GetMemoryBlock
    //     for each instruction.
    const auto memory_block = virtual_memory_->GetMemoryBlock(address_);
    return std::string(
        reinterpret_cast<const char*>(&*memory_block->second.begin() +
                                      address_ - memory_block->first),
        size_);
  }

  assert(get_bytes_callback_);
  return get_bytes_callback_(*this);
}

void Instruction::SetNextInstruction(Address) {
  (*flags_)[address_] |= FLAG_FLOW;
}

Address Instruction::GetNextInstruction() const {
  Byte* flags = &(*flags_)[address_];
  if (!(*flags & FLAG_FLOW)) {
    return 0;
  }

  return address_ + size_;
}

const std::string* Instruction::CacheString(const std::string& string) {
  return &*string_cache_.insert(string).first;
}

uint16_t Instruction::GetInDegree() const { return in_degree_; }

void Instruction::AddInEdge() {
  if (in_degree_ < std::numeric_limits<uint16_t>::max()) {
    in_degree_++;
#ifdef _DEBUG
  } else {
    assert(false && "instruction has too many in edges!");
#endif
  }
}

uint8_t Instruction::GetOperandCount() const { return operand_count_; }

Operands::const_iterator Instruction::GetFirstOperand() const {
  return operands_.begin() + operand_index_;
}

Operands::const_iterator Instruction::GetLastOperand() const {
  return GetFirstOperand() + GetOperandCount();
}

const Operand& Instruction::GetOperand(int index) const {
  CHECK(index >= 0 && index < GetOperandCount());
  return *operands_[operand_index_ + index];
}

bool Instruction::IsFlow() const {
  return ((*flags_)[address_] & FLAG_FLOW) != 0;
}

bool Instruction::IsExported(Address address) {
  return ((*flags_)[address] & FLAG_EXPORTED) != 0;
}

bool Instruction::IsExported() const { return IsExported(address_); }

void Instruction::SetExported(bool exported) {
  if (exported) {
    (*flags_)[address_] |= FLAG_EXPORTED;
  } else {
    (*flags_)[address_] &= ~FLAG_EXPORTED;
  }
}

void Instruction::SetFlag(unsigned char flag, bool value) {
  if (value) {
    (*flags_)[address_] |= flag;
  } else {
    (*flags_)[address_] &= ~flag;
  }
}

bool Instruction::HasFlag(unsigned char flag) const {
  return ((*flags_)[address_] & flag) != 0;
}

bool Instruction::IsNegativeValue(int64_t value) {
  return Instruction::GetBitness() == 32 ? static_cast<int32_t>(value) < 0
                                         : static_cast<int64_t>(value) < 0;
}
