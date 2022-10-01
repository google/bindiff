// Copyright 2011-2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#include "third_party/zynamics/binexport/ida/metapc.h"

#include <array>
#include <cinttypes>
#include <limits>
#include <string>

// clang-format off
#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <ida.hpp>                                              // NOLINT
#include <idp.hpp>                                              // NOLINT
#include <intel.hpp>                                            // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT
// clang-format on

#include "third_party/absl/log/log.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/binexport/base_types.h"
#include "third_party/zynamics/binexport/call_graph.h"
#include "third_party/zynamics/binexport/flow_graph.h"
#include "third_party/zynamics/binexport/ida/names.h"
#include "third_party/zynamics/binexport/util/format.h"

namespace security::binexport {
namespace {

bool IsStringInstruction(absl::string_view mnemonic) {
  constexpr std::array<absl::string_view, 7> kStringInstructions = {
      // Keep sorted
      "cmps", "ins", "lods", "movs", "outs", "scas", "stos",
  };
  return std::binary_search(kStringInstructions.begin(),
                            kStringInstructions.end(), mnemonic.substr(0, 4));
}

std::string GetSegmentSelector(const op_t& operand) {
  const size_t index = static_cast<size_t>(operand.specval >> 16);
  if (!index) {
    return "";
  }
  return GetRegisterName(index, 2) + ":";
}

int GetSegmentSize(const insn_t& instruction, const segment_t* segment) {
  if (segment) {
    // IDA: 0 = 16, 1 = 32, 2 = 64
    return (16 << segment->bitness) >> 3;
  }
  if (instruction.auxpref & aux_use32) {
    return 4;
  }
  if (instruction.auxpref & aux_use64) {
    return 8;
  }
  return 2;
}

std::string GetExtendedRegisterName(const op_t& operand) {
  switch (operand.type) {
    case o_trreg:  // Test register
      return absl::StrCat("tr", operand.reg);
    case o_dbreg:  // Debug register
      return absl::StrCat("dr", operand.reg);
    case o_crreg:  // Control register
      return absl::StrCat("cr", operand.reg);
    case o_mmxreg:  // MMX register
      return absl::StrCat("mm(", operand.reg, ")");
    case o_xmmreg:  // SSE register
      return absl::StrCat("xmm", operand.reg);
    case o_ymmreg:  // AVX register
      return absl::StrCat("ymm", operand.reg);
    case o_zmmreg:  // AVX-512 register
      return absl::StrCat("zmm", operand.reg);
    case o_fpreg:  // Floating point register
      return absl::StrCat("st(", operand.reg, ")");
    default:
      return absl::StrCat("unk(", operand.reg, ")");
  }
}

// This is special, as operand size may be different from expression in deref,
// i.e.:
// mov ax, word ss:[edx + 16]
size_t GetSibOperandSize(Address address) {
  const segment_t* segment = getseg(static_cast<ea_t>(address));
  // IDA: 0 = 16, 1 = 32, 2 = 64
  return (16 << segment->bitness) >> 3;
}

std::string GetSibBase(const insn_t& instruction, const op_t& operand) {
  const size_t opsize = GetSibOperandSize(instruction.ea);
  std::string name = GetRegisterName(x86_base(instruction, operand), opsize);
  if (name.empty()) {
    // Retry with size == 4, otherwise mmx instructions would try with 16 and
    // fail.
    name = GetRegisterName(x86_base(instruction, operand), 4);
  }
  return name;
}

std::string GetSibIndex(const insn_t& instruction, const op_t& operand) {
  const int index = x86_index(instruction, operand);
  size_t opsize = GetSibOperandSize(instruction.ea);
  if (opsize <= 1) {
    opsize = 4;  // @bug: can sib be 1 byte in size? I get an empty reg name in
                 // those cases
  }
  return index != INDEX_NONE ? GetRegisterName(index, opsize) : "";
}

int GetSibScale(const op_t& operand) { return x86_scale(operand); }

Address GetSibImmediate(const op_t& operand) { return operand.addr; }

std::string GetInstanceName(Address address) { return GetName(address, false); }

void HandlePhraseExpression(Expressions* expressions, FlowGraph* flow_graph,
                            const insn_t& instruction, const op_t& operand,
                            uint8_t operand_num) {
  std::string base, index;
  if (ad16(instruction)) {  // https://zynamics.fogbugz.com/default.asp?2792
    switch (operand.phrase) {
      case 0:
        base = "bx";
        index = "si";
        break;
      case 1:
        base = "bx";
        index = "di";
        break;
      case 2:
        base = "bp";
        index = "si";
        break;
      case 3:
        base = "bp";
        index = "di";
        break;
      case 4:
        base = "si";
        break;
      case 5:
        base = "di";
        break;
      case 6:
        base = "bp";
        break;
      case 7:
        base = "bx";
        break;
    }
  } else {
    base = GetSibBase(instruction, operand);
    index = GetSibIndex(instruction, operand);
  }
  const int scale = GetSibScale(operand);

  std::string variable_name = GetVariableName(instruction, operand_num);

  Expression* expression = nullptr;
  if (size_t operand_size = GetOperandByteSize(instruction, operand);
      operand_size != 0) {
    expression = Expression::Create(expression, GetSizePrefix(operand_size), 0,
                                    Expression::TYPE_SIZEPREFIX, 0);
    expressions->push_back(expression);
  }
  expression = Expression::Create(expression, GetSegmentSelector(operand), 0,
                                  Expression::TYPE_OPERATOR, 0);
  expressions->push_back(expression);
  expression =
      Expression::Create(expression, "[", 0, Expression::TYPE_DEREFERENCE, 0);
  expressions->push_back(expression);
  if (!index.empty()) {
    expression =
        Expression::Create(expression, "+", 0, Expression::TYPE_OPERATOR, 0);
    expressions->push_back(expression);
  }
  Expression* temp =
      Expression::Create(expression, base, 0, Expression::TYPE_REGISTER, 0);
  expressions->push_back(temp);
  if (!variable_name.empty() && IsStackVariable(instruction.ea, operand_num)) {
    flow_graph->AddExpressionSubstitution(
        instruction.ea, operand_num, temp->GetId(), base + "+" + variable_name);
  }

  if (!index.empty()) {
    if (scale) {
      Expression* parent = nullptr;
      parent =
          Expression::Create(expression, "*", 0, Expression::TYPE_OPERATOR, 1);
      expressions->push_back(parent);
      expressions->push_back(
          Expression::Create(parent, index, 0, Expression::TYPE_REGISTER, 0));
      expressions->push_back(Expression::Create(
          parent, "", 1 << scale, Expression::TYPE_IMMEDIATE_INT, 1));
    } else {
      expressions->push_back(Expression::Create(expression, index, 0,
                                                Expression::TYPE_REGISTER, 1));
    }
  }
}

// Creates a tree for expressions of the form:
// section:[base + index*scale + disp]
// The tree will look like:
// size -> segment -> [ -> + --> base
//                           \-> * --> index
//                           \     \-> scale
//                           \-> disp
void HandleDisplacementExpression(const insn_t& instruction,
                                  const op_t& operand, uint8_t operand_num,
                                  Expressions* expressions) {
  const std::string base = GetSibBase(instruction, operand);
  const std::string index = GetSibIndex(instruction, operand);
  const int scale = GetSibScale(operand);
  const Address immediate = GetSibImmediate(operand);
  int8_t pos = 0;

  Expression* expression = nullptr;
  Expression* register_expression = nullptr;
  if (size_t operand_size = GetOperandByteSize(instruction, operand);
      operand_size != 0) {
    expression = Expression::Create(expression, GetSizePrefix(operand_size), 0,
                                    Expression::TYPE_SIZEPREFIX, pos);
    expressions->push_back(expression);
  }
  expression = Expression::Create(expression, GetSegmentSelector(operand), 0,
                                  Expression::TYPE_OPERATOR, pos);
  expressions->push_back(expression);
  expression =
      Expression::Create(expression, "[", 0, Expression::TYPE_DEREFERENCE, pos);
  expressions->push_back(expression);
  expression =
      Expression::Create(expression, "+", 0, Expression::TYPE_OPERATOR, pos);
  expressions->push_back(expression);
  register_expression =
      Expression::Create(expression, base, 0, Expression::TYPE_REGISTER, pos);
  expressions->push_back(register_expression);

  Expression::Type expression_type = Expression::TYPE_IMMEDIATE_INT;
  std::string variable_name = GetVariableName(instruction, operand_num);
  if (variable_name.empty()) {
    const Name name = GetName(instruction.ea, immediate, operand_num, true);
    variable_name = name.name;
    expression_type = name.empty() ? Expression::TYPE_IMMEDIATE_INT
                                   : Expression::TYPE_GLOBALVARIABLE;
  }
  if (!index.empty()) {
    if (scale) {
      Expression* parent = nullptr;
      expressions->push_back(
          parent = Expression::Create(expression, "*", 0,
                                      Expression::TYPE_OPERATOR, ++pos));
      expressions->push_back(
          Expression::Create(parent, index, 0, Expression::TYPE_REGISTER, 0));
      expressions->push_back(Expression::Create(
          parent, "", 1 << scale, Expression::TYPE_IMMEDIATE_INT, 1));
    } else {
      expressions->push_back(Expression::Create(
          expression, index, 0, Expression::TYPE_REGISTER, ++pos));
    }
  }
  expressions->push_back(
      expression = Expression::Create(expression, variable_name, immediate,
                                      expression_type, ++pos));
}

void HandleMemoryExpression(const insn_t& instruction, const op_t& operand,
                            uint8_t operand_num, Expressions* expressions,
                            FlowGraph* flow_graph) {
  const Address immediate = to_ea(instruction.cs, operand.addr);
  const Name name = GetName(instruction.ea, immediate, operand_num, true);
  const std::string index = GetSibIndex(instruction, operand);
  const int scale = GetSibScale(operand);

  // 1) lookup type ID by name
  // 2) write entry to flow graph AND member_types(!)
  Expression* expression = nullptr;
  if (size_t operand_size = GetOperandByteSize(instruction, operand);
      operand_size != 0) {
    expression = Expression::Create(expression, GetSizePrefix(operand_size), 0,
                                    Expression::TYPE_SIZEPREFIX, 0);
    expressions->push_back(expression);
  }
  if (name.empty()) {
    const std::string global =
        GetGlobalStructureName(instruction.ea, immediate, operand_num);
    if (!global.empty()) {
      flow_graph->AddExpressionSubstitution(instruction.ea, operand_num,
                                            expression->GetId(), global);
    }
  }
  expression = Expression::Create(expression, GetSegmentSelector(operand), 0,
                                  Expression::TYPE_OPERATOR, 0);
  expressions->push_back(expression);
  expression =
      Expression::Create(expression, "[", 0, Expression::TYPE_DEREFERENCE, 0);
  expressions->push_back(expression);
  Expression* parent = expression;

  if (scale && !index.empty()) {
    if (immediate || !name.empty()) {
      parent =
          Expression::Create(expression, "+", 0, Expression::TYPE_OPERATOR, 0);
      expressions->push_back(parent);
      expressions->push_back(Expression::Create(
          parent, name.name, immediate,
          name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type, 0));
    }
    parent = Expression::Create(parent, "*", 0, Expression::TYPE_OPERATOR, 1);
    expressions->push_back(parent);
    expressions->push_back(
        Expression::Create(parent, index, 0, Expression::TYPE_REGISTER, 0));
    expressions->push_back(Expression::Create(
        parent, "", 1 << scale, Expression::TYPE_IMMEDIATE_INT, 1));
  } else {
    expression = Expression::Create(
        parent, name.name, immediate,
        name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type, 0);
    expressions->push_back(expression);
  }
}

void HandleImmediate(const insn_t& instruction, const op_t& operand,
                     uint8_t operand_num, Expressions* expressions) {
  Address immediate = operand.value;
  Name name = GetName(instruction.ea, operand.value, operand_num, true);
  if (name.empty()) {
    name.name = GetGlobalStructureName(instruction.ea, immediate, operand_num);
    name.type = Expression::TYPE_GLOBALVARIABLE;
  }
  // By default IDA will perform a sign/zero extension, returning a 32 bit
  // operand size even when the encoding only contains an 8 bit value. This is
  // consistent with what the CPU actually computes, but not with the intel
  // opcode specification. BeaEngine follows the Intel convention, leading to
  // lots of spurious diffs between the two disassemblers. This here is an
  // attempt at undoing the sign extension.
  size_t operand_size = GetOperandByteSize(instruction, operand);
  if (operand.offb && instruction.size - operand.offb < operand_size) {
    // Immediates are typically the last bytes of an instruction.
    operand_size = instruction.size - operand.offb;
    // Mask the last n bytes of the original immediate.
    Address mask =
        std::numeric_limits<Address>::max() >> (64 - 8 * operand_size);
    immediate &= mask;
  }
  Expression* expression = nullptr;
  if (operand_size != 0) {
    expression = Expression::Create(expression, GetSizePrefix(operand_size), 0,
                                    Expression::TYPE_SIZEPREFIX, 0);
    expressions->push_back(expression);
  }
  expression = Expression::Create(
      expression, name.name, immediate,
      name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type, 0);
  expressions->push_back(expression);
}

Operands ParseOperandsIdaMetaPc(const insn_t& instruction,
                                CallGraph* /* call_graph */,
                                FlowGraph* flow_graph) {
  Operands operands;
  uint8_t skipped = 0;
  for (uint8_t operand_position = 0; operand_position < UA_MAXOP;
       ++operand_position) {
    Expressions expressions;
    const op_t& operand = instruction.ops[operand_position];
    if (operand.type == o_void) {
      break;
    }
    if (!(operand.flags & OF_SHOW)) {  // skip hidden expressions
      ++skipped;
      continue;
    }

    Expression* expression = nullptr;
    size_t operand_size = GetOperandByteSize(instruction, operand);
    switch (operand.type) {
      case o_trreg:   // Test register
      case o_dbreg:   // Debug register
      case o_crreg:   // Control register
      case o_mmxreg:  // MMX register
      case o_xmmreg:  // SSE register
      case o_ymmreg:  // AVX register
      case o_zmmreg:  // AVX-512 register
      case o_fpreg:   // Floating point register
        if (operand_size != 0) {
          expression =
              Expression::Create(expression, GetSizePrefix(operand_size), 0,
                                 Expression::TYPE_SIZEPREFIX, 0);
          expressions.push_back(expression);
        }
        expression =
            Expression::Create(expression, GetExtendedRegisterName(operand), 0,
                               Expression::TYPE_REGISTER, 0);
        expressions.push_back(expression);
        break;

      case o_reg:  // register
        if (operand_size != 0) {
          expression =
              Expression::Create(expression, GetSizePrefix(operand_size), 0,
                                 Expression::TYPE_SIZEPREFIX, 0);
          expressions.push_back(expression);
        }
        expression = Expression::Create(
            expression, GetRegisterName(operand.reg, operand_size), 0,
            Expression::TYPE_REGISTER, 0);
        expressions.push_back(expression);
        break;
      case o_mem:  // direct memory reference
        HandleMemoryExpression(instruction, operand, operand_position - skipped,
                               &expressions, flow_graph);
        break;

      case o_phrase:  // Memory Ref [Base Reg + Index Reg]    phrase
        HandlePhraseExpression(&expressions, flow_graph, instruction, operand,
                               operand_position - skipped);
        break;

      case o_displ:  // array dereference [Base Reg + Index Reg + Displacement]
        HandleDisplacementExpression(instruction, operand,
                                     operand_position - skipped, &expressions);
        break;

      case o_imm:  // Immediate value
        HandleImmediate(instruction, operand, operand_position - skipped,
                        &expressions);
        break;

      case o_far:   // Immediate Far Address (CODE)
      case o_near:  // Immediate Near Address (CODE)
      {
        const Address immediate = operand.addr;
        Name name = GetName(instruction.ea, immediate,
                            operand_position - skipped, true);
        if (name.empty()) {
          name.name = GetGlobalStructureName(instruction.ea, immediate,
                                             operand_position - skipped);
          name.type = Expression::TYPE_GLOBALVARIABLE;
        }
        if (operand_size != 0) {
          expression =
              Expression::Create(expression,
                                 GetSizePrefix(GetSegmentSize(
                                     instruction, /* segment = */ nullptr)),
                                 0, Expression::TYPE_SIZEPREFIX, 0);
          expressions.push_back(expression);
        }
        expression = Expression::Create(
            expression, name.name, immediate,
            name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type, 0);
        expressions.push_back(expression);
        break;
      }

      default:
        LOG(INFO) << absl::StrCat("Warning: unknown operand type ",
                                  operand.type, " at ",
                                  FormatAddress(instruction.ea));
        break;
    }

    operands.push_back(Operand::CreateOperand(expressions));
  }

  Operands(operands).swap(operands);
  return operands;
}

}  // namespace

Instruction ParseInstructionIdaMetaPc(const insn_t& instruction,
                                      CallGraph* call_graph,
                                      FlowGraph* flow_graph) {
  if (!IsCode(instruction.ea)) {
    return Instruction(instruction.ea);
  }
  std::string mnemonic(GetMnemonic(instruction.ea));
  if (mnemonic.empty()) {
    return Instruction(instruction.ea);
  }

  const Address next_instruction = instruction.ea + instruction.size;
  const flags_t next_flags = get_flags(static_cast<ea_t>(next_instruction));

  // add suffix from hidden operand
  if (IsStringInstruction(mnemonic)) {
    for (size_t operand_position = 0;
         operand_position < UA_MAXOP &&
             instruction.ops[operand_position].type != o_void;
         ++operand_position) {
      if (mnemonic == "outs" && operand_position != 1) {
        continue;
      }
      const op_t& operand = instruction.ops[operand_position];
      if (!(operand.flags & OF_SHOW)) {  // hidden operand, get suffix from it
        if (operand.dtype == dt_byte) {
          mnemonic += "b";
        } else if (operand.dtype == dt_word) {
          mnemonic += "w";
        } else if (operand.dtype == dt_dword) {
          mnemonic += "d";
        } else {
          // Default add machine word size suffix. IDA sometimes omits the
          // suffix otherwise.
          mnemonic += inf_is_64bit() ? "d" : "w";
        }
        break;
      }
    }
  }

  // add prefix (if any) to string instructions
  if (instruction.auxpref & aux_lock) {
    mnemonic = "lock " + mnemonic;
  }
  if (instruction.auxpref & aux_rep) {
    if (instruction.itype == NN_scas || instruction.itype == NN_cmps) {
      mnemonic = "repe " + mnemonic;
    } else {
      mnemonic = "rep " + mnemonic;
    }
  }
  if (instruction.auxpref & aux_repne) {
    mnemonic = "repne " + mnemonic;
  }

  return Instruction(
      instruction.ea, is_flow(next_flags) ? next_instruction : 0,
      instruction.size, mnemonic,
      ParseOperandsIdaMetaPc(instruction, call_graph, flow_graph));
}

}  // namespace security::binexport
