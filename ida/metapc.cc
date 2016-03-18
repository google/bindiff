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

#include "third_party/zynamics/binexport/ida/metapc.h"

#include <cinttypes>
#include <limits>
#include <string>

#include "third_party/zynamics/binexport/ida/pro_forward.h"  // NOLINT
#include <ida.hpp>                                           // NOLINT
#include <idp.hpp>                                           // NOLINT
#include <intel.hpp>                                         // NOLINT

#include "base/logging.h"
#include "base/stringprintf.h"
#include "third_party/zynamics/binexport/base_types.h"
#include "third_party/zynamics/binexport/call_graph.h"
#include "third_party/zynamics/binexport/flow_graph.h"
#include "third_party/zynamics/binexport/ida/names.h"
#include "third_party/zynamics/binexport/type_system.h"

bool IsStringInstruction(const std::string& mnemonic) {
  static std::set<std::string> instructions = {"ins",  "outs", "movs", "cmps",
                                               "stos", "lods", "scas"};
  return instructions.find(mnemonic.substr(0, 4)) != instructions.end();
}

int GetSegmentSize(const segment_t* segment = nullptr) {
  if (segment) {
    // IDA: 0 = 16, 1 = 32, 2 = 64
    return (16 << segment->bitness) >> 3;
  }
  if (cmd.auxpref & aux_use32) {
    return 4;
  }
  if (cmd.auxpref & aux_use64) {
    return 8;
  }
  return 2;
}

std::string GetSegmentSelector(const op_t& operand) {
  const size_t index = static_cast<size_t>(operand.specval >> 16);
  if (!index) {
    return "";
  }
  return GetRegisterName(index, 2) + ":";
}

std::string GetExtendedRegisterName(const op_t& operand) {
  switch (operand.type) {
    case o_trreg:  // Test register
      return "tr" + std::to_string(operand.reg);
    case o_dbreg:  // Debug register
      return "dr" + std::to_string(operand.reg);
    case o_crreg:  // Control register
      return "cr" + std::to_string(operand.reg);
    case o_mmxreg:  // MMX register
      return "mm(" + std::to_string(operand.reg) + ")";
    case o_xmmreg:  // SSE register
      return "xmm" + std::to_string(operand.reg);
    case o_ymmreg:  // AVX register
      return "ymm" + std::to_string(operand.reg);
    case o_zmmreg:  // AVX-512 register
      return "zmm" + std::to_string(operand.reg);
    case o_fpreg:  // Floating point register
      return "st(" + std::to_string(operand.reg) + ")";
    default:
      throw std::runtime_error("invalid register in getExtendedRegisterName");
  }
}

// This is special, as operand size may be different from expression in deref,
// i.e.:
// mov ax, word ss:[edx + 16]
size_t GetSibOperandSize(Address address) {
  const segment_t* segment = getseg(static_cast<ea_t>(address));
  // fucking IDA: 0 = 16, 1 = 32, 2 = 64
  return (16 << segment->bitness) >> 3;
}

// Warning: this function uses the global cmd!
std::string GetSibBase(const insn_t& instruction, const op_t& operand) {
  const size_t opsize = GetSibOperandSize(instruction.ea);
  std::string name = GetRegisterName(x86_base(operand), opsize);
  if (name.empty()) {
    // Retry with size == 4, otherwise mmx instructions would try with 16 and
    // fail.
    name = GetRegisterName(x86_base(operand), 4);
  }
  return name;
}

std::string GetSibIndex(
    const insn_t& instruction,
    const op_t& operand) {  // @warning: this function uses the global cmd!
  const int index = x86_index(operand);
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
                            TypeSystem* type_system, const Address address,
                            const insn_t& instruction, const op_t& operand,
                            uint8_t operand_num) {
  std::string base, index;
  if (ad16()) {  // https://zynamics.fogbugz.com/default.asp?2792
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
        base = "sword (@bug!)";
        break;  // @bug: this should probably be a value retrieved from
                // somewhere
      case 7:
        base = "bx";
        break;
    }
  } else {
    base = GetSibBase(instruction, operand);
    index = GetSibIndex(instruction, operand);
  }
  const int scale = GetSibScale(operand);

  std::string variable_name = GetVariableName(address, operand_num);

  Expression* expression = nullptr;
  expressions->push_back(expression = Expression::Create(
                             expression,
                             GetSizePrefix(GetOperandByteSize(operand)), 0,
                             Expression::TYPE_SIZEPREFIX, 0));
  expressions->push_back(
      expression = Expression::Create(expression, GetSegmentSelector(operand),
                                      0, Expression::TYPE_OPERATOR, 0));
  expressions->push_back(
      expression = Expression::Create(expression, "[", 0,
                                      Expression::TYPE_DEREFERENCE, 0));
  if (!index.empty()) {
    expressions->push_back(
        expression = Expression::Create(expression, "+", 0,
                                        Expression::TYPE_OPERATOR, 0));
  }
  Expression* temp = nullptr;
  expressions->push_back(temp =
                             Expression::Create(expression, base, 0,
                                                Expression::TYPE_REGISTER, 0));
  if (!variable_name.empty() && IsStackVariable(address, operand_num)) {
    flow_graph->AddExpressionSubstitution(address, operand_num, temp->GetId(),
                                          base + "+" + variable_name);
  }

  if (!index.empty()) {
    if (scale) {
      Expression* parent = nullptr;
      expressions->push_back(
          parent = Expression::Create(expression, "*", 0,
                                      Expression::TYPE_OPERATOR, 1));
      expressions->push_back(
          Expression::Create(parent, index, 0, Expression::TYPE_REGISTER, 0));
      expressions->push_back(Expression::Create(
          parent, "", 1 << scale, Expression::TYPE_IMMEDIATE_INT, 1));
    } else {
      expressions->push_back(Expression::Create(expression, index, 0,
                                                Expression::TYPE_REGISTER, 1));
    }
  }

  type_system->AddTypeSubstitution(address, operand_num, temp->GetId());
}

// Creates a tree for expressions of the form:
// section:[base + index*scale + disp]
// The tree will look like:
// size -> segment -> [ -> + --> base
//                           \-> * --> index
//                           \     \-> scale
//                           \-> disp
void HandleDisplacementExpression(const Address address,
                                  const insn_t& instruction,
                                  const op_t& operand, uint8_t operand_num,
                                  Expressions* expressions,
                                  TypeSystem* type_system) {
  const std::string base = GetSibBase(instruction, operand);
  const std::string index = GetSibIndex(instruction, operand);
  const int scale = GetSibScale(operand);
  const Address immediate = GetSibImmediate(operand);
  int8_t pos = 0;

  Expression* expression = nullptr;
  Expression* register_expression = nullptr;
  expressions->push_back(expression = Expression::Create(
                             expression,
                             GetSizePrefix(GetOperandByteSize(operand)), 0,
                             Expression::TYPE_SIZEPREFIX, pos));
  expressions->push_back(
      expression = Expression::Create(expression, GetSegmentSelector(operand),
                                      0, Expression::TYPE_OPERATOR, pos));
  expressions->push_back(
      expression = Expression::Create(expression, "[", 0,
                                      Expression::TYPE_DEREFERENCE, pos));
  expressions->push_back(
      expression = Expression::Create(expression, "+", 0,
                                      Expression::TYPE_OPERATOR, pos));
  expressions->push_back(
      register_expression = Expression::Create(expression, base, 0,
                                               Expression::TYPE_REGISTER, pos));

  Expression::Type expression_type = Expression::TYPE_IMMEDIATE_INT;
  std::string variable_name = GetVariableName(address, operand_num);
  if (variable_name.empty()) {
    const Name name = GetName(address, immediate, operand_num, true);
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

  type_system->AddDisplacedTypeSubstitution(address, immediate, operand_num,
                                            register_expression->GetId());
}

void HandleMemoryExpression(const Address address, const insn_t& instruction,
                            const op_t& operand, uint8_t operand_num,
                            Expressions* expressions, FlowGraph* flow_graph,
                            TypeSystem* type_system) {
  const Address immediate = toEA(instruction.cs, operand.addr);
  const Name name = GetName(address, immediate, operand_num, true);
  const std::string index = GetSibIndex(instruction, operand);
  const int scale = GetSibScale(operand);

  // 1) lookup type ID by name
  // 2) write entry to flow graph AND member_types(!)
  Expression* expression = nullptr;
  expressions->push_back(expression = Expression::Create(
                             expression,
                             GetSizePrefix(GetOperandByteSize(operand)), 0,
                             Expression::TYPE_SIZEPREFIX, 0));
  if (name.empty()) {
    const std::string global =
        GetGlobalStructureName(address, immediate, operand_num);
    if (!global.empty()) {
      flow_graph->AddExpressionSubstitution(address, operand_num,
                                            expression->GetId(), global);
    }
  }

  expressions->push_back(
      expression = Expression::Create(expression, GetSegmentSelector(operand),
                                      0, Expression::TYPE_OPERATOR, 0));
  expressions->push_back(
      expression = Expression::Create(expression, "[", 0,
                                      Expression::TYPE_DEREFERENCE, 0));
  Expression* parent = expression;

  if (scale && !index.empty()) {
    if (immediate || !name.empty()) {
      expressions->push_back(
          parent = Expression::Create(expression, "+", 0,
                                      Expression::TYPE_OPERATOR, 0));
      expressions->push_back(Expression::Create(
          parent, name.name, immediate,
          name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type, 0));
    }
    expressions->push_back(parent = Expression::Create(
                               parent, "*", 0, Expression::TYPE_OPERATOR, 1));
    expressions->push_back(
        Expression::Create(parent, index, 0, Expression::TYPE_REGISTER, 0));
    expressions->push_back(Expression::Create(
        parent, "", 1 << scale, Expression::TYPE_IMMEDIATE_INT, 1));
  } else {
    expressions->push_back(
        expression = Expression::Create(
            parent, name.name, immediate,
            name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type, 0));
    type_system->AddTypeSubstitution(address, operand_num, expression->GetId());
    type_system->CreateMemoryTypeInstance(
        address, operand_num, expression->GetId(), immediate, GetInstanceName);
  }
}

void HandleImmediate(const Address address, const op_t& operand,
                     uint8_t operand_num, Expressions* expressions,
                     TypeSystem* type_system) {
  Address immediate = operand.value;
  Name name = GetName(address, operand.value, operand_num, true);
  if (name.empty()) {
    name.name = GetGlobalStructureName(address, immediate, operand_num);
    name.type = Expression::TYPE_GLOBALVARIABLE;
  }
  // By default IDA will perform a sign/zero extension, returning a 32 bit
  // operand size even when the encoding only contains an 8 bit value. This is
  // consistent with what the CPU actually computes, but not with the intel
  // opcode specification. BeaEngine follows the intel convention, leading to
  // lots of spurious diffs between the two disassemblers. This here is an
  // attempt at undoing the sign extension.
  size_t operand_size = GetOperandByteSize(operand);
  if (operand.offb && cmd.size - operand.offb < operand_size) {
    // Immediates are typically the last bytes of an instruction.
    operand_size = cmd.size - operand.offb;
    // Mask the last n bytes of the original immediate.
    Address mask =
        std::numeric_limits<Address>::max() >> (64 - 8 * operand_size);
    immediate &= mask;
  }
  Expression* expression = nullptr;
  expressions->push_back(
      expression = Expression::Create(expression, GetSizePrefix(operand_size),
                                      0, Expression::TYPE_SIZEPREFIX, 0));
  expressions->push_back(
      expression = Expression::Create(
          expression, name.name, immediate,
          name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type, 0));
  type_system->CreateTypeInstance(address, operand_num, expression->GetId(),
                                  immediate, GetInstanceName);
}

Operands ParseOperandsIdaMetaPc(CallGraph& /*call_graph*/,
                                FlowGraph& flow_graph, TypeSystem* type_system,
                                const Address address) {
  Operands operands;
  uint8_t skipped = 0;
  for (uint8_t operand_position = 0; operand_position < UA_MAXOP;
       ++operand_position) {
    Expressions expressions;
    const insn_t& instruction = cmd;
    const op_t& operand = cmd.Operands[operand_position];
    if (operand.type == o_void) {
      break;
    }
    if (!(operand.flags & OF_SHOW)) {  // skip hidden expressions
      ++skipped;
      continue;
    }

    Expression* expression = nullptr;
    switch (operand.type) {
      case o_trreg:   // Test register
      case o_dbreg:   // Debug register
      case o_crreg:   // Control register
      case o_mmxreg:  // MMX register
      case o_xmmreg:  // SSE register
      case o_ymmreg:  // AVX register
      case o_zmmreg:  // AVX-512 register
      case o_fpreg:   // Floating point register
      {
        expressions.push_back(expression = Expression::Create(
                                  expression,
                                  GetSizePrefix(GetOperandByteSize(operand)), 0,
                                  Expression::TYPE_SIZEPREFIX, 0));
        expressions.push_back(expression = Expression::Create(
                                  expression, GetExtendedRegisterName(operand),
                                  0, Expression::TYPE_REGISTER, 0));
        break;
      }
      case o_reg:  // register
        expressions.push_back(expression = Expression::Create(
                                  expression,
                                  GetSizePrefix(GetOperandByteSize(operand)), 0,
                                  Expression::TYPE_SIZEPREFIX, 0));
        expressions.push_back(
            expression = Expression::Create(
                expression,
                GetRegisterName(operand.reg, GetOperandByteSize(operand)), 0,
                Expression::TYPE_REGISTER, 0));
        break;
      case o_mem:  // direct memory reference
        HandleMemoryExpression(address, instruction, operand,
                               operand_position - skipped, &expressions,
                               &flow_graph, type_system);
        break;

      case o_phrase:  // Memory Ref [Base Reg + Index Reg]    phrase
        HandlePhraseExpression(&expressions, &flow_graph, type_system, address,
                               instruction, operand,
                               operand_position - skipped);
        break;

      case o_displ:  // array dereference [Base Reg + Index Reg + Displacement]
        HandleDisplacementExpression(address, instruction, operand,
                                     operand_position - skipped, &expressions,
                                     type_system);
        break;

      case o_imm:  // Immediate value
        HandleImmediate(address, operand, operand_position - skipped,
                        &expressions, type_system);
        break;

      case o_far:   // Immediate Far Address (CODE)
      case o_near:  // Immediate Near Address (CODE)
      {
        const Address immediate = operand.addr;
        Name name =
            GetName(address, immediate, operand_position - skipped, true);
        if (name.empty()) {
          name.name = GetGlobalStructureName(address, immediate,
                                             operand_position - skipped);
          name.type = Expression::TYPE_GLOBALVARIABLE;
        }

        expressions.push_back(expression = Expression::Create(
                                  expression, GetSizePrefix(GetSegmentSize()),
                                  0, Expression::TYPE_SIZEPREFIX, 0));
        expressions.push_back(
            expression = Expression::Create(
                expression, name.name, immediate,
                name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type,
                0));
        break;
      }

      default:
        LOG(INFO) << StringPrintf(
            "warning: unknown operand type %d at %08" PRIx64,
            static_cast<int>(operand.type), address);
        break;
    }

    operands.push_back(Operand::CreateOperand(expressions));
  }

  Operands(operands).swap(operands);
  return operands;
}

Instruction ParseInstructionIdaMetaPc(CallGraph& call_graph,
                                      FlowGraph& flow_graph,
                                      TypeSystem* type_system,
                                      const Address address) {
  char buffer[128];
  memset(buffer, 0, sizeof(buffer));
  if (!IsCode(address) ||
      !ua_mnem(static_cast<ea_t>(address), buffer, sizeof(buffer))) {
    return Instruction(address);
  }
  std::string mnemonic(buffer);
  if (mnemonic.empty()) {
    return Instruction(address);
  }

  const Address next_instruction = address + cmd.size;
  const flags_t next_flags = getFlags(static_cast<ea_t>(next_instruction));

  // add suffix from hidden operand
  if (IsStringInstruction(mnemonic)) {
    for (size_t operand_position = 0;
         operand_position < UA_MAXOP &&
             cmd.Operands[operand_position].type != o_void;
         ++operand_position) {
      if (mnemonic == "outs" && operand_position != 1) {
        continue;
      }
      const op_t& operand = cmd.Operands[operand_position];
      if (!(operand.flags & OF_SHOW)) {  // hidden operand, get suffix from it
        if (operand.dtyp == dt_byte) {
          mnemonic += "b";
        } else if (operand.dtyp == dt_word) {
          mnemonic += "w";
        } else if (operand.dtyp == dt_dword) {
          mnemonic += "d";
        } else {
          // default add machine word size suffix. IDA sometimes omits the
          // suffix otherwise.
          mnemonic += inf.is_64bit() ? "d" : "w";
        }
        break;
      }
    }
  }

  // add prefix (if any) to string instructions
  if (cmd.auxpref & aux_lock) {
    mnemonic = "lock " + mnemonic;
  }
  if (cmd.auxpref & aux_rep) {
    if (cmd.itype == NN_scas || cmd.itype == NN_cmps) {
      mnemonic = "repe " + mnemonic;
    } else {
      mnemonic = "rep " + mnemonic;
    }
  }
  if (cmd.auxpref & aux_repne) {
    mnemonic = "repne " + mnemonic;
  }

  return Instruction(
      address, isFlow(next_flags) ? next_instruction : 0, cmd.size, mnemonic,
      ParseOperandsIdaMetaPc(call_graph, flow_graph, type_system, address));
}
