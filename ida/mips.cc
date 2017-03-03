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

#include "third_party/zynamics/binexport/ida/mips.h"

#include <cinttypes>
#include <string>

#include "third_party/zynamics/binexport/ida/pro_forward.h"  // NOLINT
#include <idp.hpp>                                           // NOLINT
#include <allins.hpp>                                        // NOLINT
#include <bytes.hpp>                                         // NOLINT
#include <ida.hpp>                                           // NOLINT
#include <ua.hpp>                                            // NOLINT

#include "base/logging.h"
#include "base/stringprintf.h"
#include "third_party/zynamics/binexport/call_graph.h"
#include "third_party/zynamics/binexport/ida/names.h"

std::string GetCoprocessorRegisterName(int index) {
  switch (index) {
    case 0:
      return "Index";
    case 1:
      return "Random";
    case 2:
      return "EntryLo0";
    case 3:
      return "EntryLo1";
    case 4:
      return "Context";
    case 5:
      return "PageMask";
    case 6:
      return "Wired";
    case 7:
      return "Reserved";
    case 8:
      return "BadVAddr";
    case 9:
      return "Count";
    case 10:
      return "EntryHi";
    case 11:
      return "Compare";
    case 12:
      return "Status";
    case 13:
      return "Cause";
    case 14:
      return "EPC";
    case 15:
      return "PRId";
    case 16:
      return "Config";
    case 17:
      return "LLAddr";
    case 18:
      return "WatchLo";
    case 19:
      return "WatchHi";
    case 20:
      return "Reserved";
    case 21:
      return "Reserved";
    case 22:
      return "Reserved";
    case 23:
      return "Debug";
    case 24:
      return "DEPC";
    case 25:
      return "Reserved";
    case 26:
      return "ErrCtl";
    case 27:
      return "Reserved";
    case 28:
      return "TagLo";
    case 29:
      return "Reserved";
    case 30:
      return "ErrorEPC";
    case 31:
      return "DESAVE";
  }
  return "unknown coprocessor register";
}

typedef std::list<const std::string*> TOperandStrings;

std::string GetFloatingPointRegisterName(const size_t register_id) {
  return "$f" + std::to_string(register_id);
}

Operands DecodeOperandsMips(const Address address) {
  Operands operands;
  for (uint8_t operand_position = 0;
       operand_position < UA_MAXOP &&
           cmd.Operands[operand_position].type != o_void;
       ++operand_position) {
    Expressions expressions;
    // const insn_t & instruction = cmd;
    const op_t& operand = cmd.Operands[operand_position];

    Expression* expression = 0;
    switch (operand.type) {
      case o_void:  // no operand
        break;
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
      case o_mem: {  // direct memory reference
        const Address immediate = operand.addr;
        const Name name =
            GetName(address, immediate, operand_position,
                    false);  // @bug: we don't get =unk_ names yet

        expressions.push_back(expression = Expression::Create(
                                  expression,
                                  GetSizePrefix(GetOperandByteSize(operand)), 0,
                                  Expression::TYPE_SIZEPREFIX, 0));
        expressions.push_back(
            expression = Expression::Create(expression, "[", 0,
                                            Expression::TYPE_DEREFERENCE, 0));
        expressions.push_back(
            expression = Expression::Create(
                expression, name.name, immediate,
                name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type,
                0));
        break;
      }
      case o_phrase:  // reg(reg)
        // @todo: test! I have not encountered this case yet!
        expressions.push_back(expression = Expression::Create(
                                  expression,
                                  GetSizePrefix(GetOperandByteSize(operand)), 0,
                                  Expression::TYPE_SIZEPREFIX, 0));
        expressions.push_back(
            expression = Expression::Create(expression, "+", 0,
                                            Expression::TYPE_OPERATOR, 0));
        expressions.push_back(Expression::Create(
            expression,
            GetRegisterName(operand.reg, GetOperandByteSize(operand)), 0,
            Expression::TYPE_REGISTER, 0));
        expressions.push_back(Expression::Create(
            expression,
            GetRegisterName(operand.specflag1, GetOperandByteSize(operand)), 0,
            Expression::TYPE_REGISTER, 1));
        break;
      case o_displ: {  // imm(reg)
        const Address immediate = operand.addr;
        const Name name =
            GetName(address, operand.addr, operand_position, false);
        expressions.push_back(expression = Expression::Create(
                                  expression,
                                  GetSizePrefix(GetOperandByteSize(operand)), 0,
                                  Expression::TYPE_SIZEPREFIX, 0));
        expressions.push_back(
            expression = Expression::Create(expression, "+", 0,
                                            Expression::TYPE_OPERATOR, 0));
        expressions.push_back(Expression::Create(
            expression,
            GetRegisterName(operand.reg, GetOperandByteSize(operand)), 0,
            Expression::TYPE_REGISTER, 0));
        expressions.push_back(Expression::Create(
            expression, name.name, immediate,
            name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type, 1));
        break;
      }
      case o_imm: {  // immediate value
        const Address immediate = operand.value;
        const Name name = GetName(address, immediate, operand_position, false);

        expressions.push_back(expression = Expression::Create(
                                  expression,
                                  GetSizePrefix(GetOperandByteSize(operand)), 0,
                                  Expression::TYPE_SIZEPREFIX, 0));
        expressions.push_back(
            expression = Expression::Create(
                expression, name.name, immediate,
                name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type,
                0));
        break;
      }
      case o_far:   // immediate Far Address  (CODE)
      case o_near:  // Immediate Near Address (CODE)
      {
        const Address immediate = operand.addr;
        const Name name = GetName(address, immediate, operand_position, false);

        expressions.push_back(expression = Expression::Create(
                                  expression,
                                  GetSizePrefix(GetOperandByteSize(operand)), 0,
                                  Expression::TYPE_SIZEPREFIX, 0));
        expressions.push_back(
            expression = Expression::Create(
                expression, name.name, immediate,
                name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type,
                0));
        break;
      }
      case o_idpspec0:
        // #define o_cmtimm        o_idpspec0  // commented out immediate number
        LOG(INFO) << StringPrintf(
            "warning: commented out immediate number operand type not yet "
            "supported (%08" PRIx64 ")",
            address);

        break;
      case o_idpspec1:
        // #define o_coreg         o_idpspec1  // coprocesor register
        // #define   del           specflag1   //   rsp: del is here
        if (cmd.auxpref & 0x5) {
          expressions.push_back(expression = Expression::Create(
                                    expression,
                                    GetSizePrefix(GetOperandByteSize(operand)),
                                    0, Expression::TYPE_SIZEPREFIX, 0));
          expressions.push_back(expression = Expression::Create(
                                    expression,
                                    GetFloatingPointRegisterName(operand.reg),
                                    0, Expression::TYPE_REGISTER, 0));
        } else {
          expressions.push_back(expression = Expression::Create(
                                    expression,
                                    GetSizePrefix(GetOperandByteSize(operand)),
                                    0, Expression::TYPE_SIZEPREFIX, 0));
          expressions.push_back(expression = Expression::Create(
                                    expression,
                                    GetCoprocessorRegisterName(operand.reg), 0,
                                    Expression::TYPE_REGISTER, 0));
        }

        break;
      case o_idpspec3:
        // #define o_cc            o_idpspec3      // coprocesor condition
        LOG(INFO) << StringPrintf(
            "warning: coprocessor condition not yet supported (%08" PRIx64 ")",
            address);
        break;
      case o_idpspec2:
      case o_idpspec4:
      case o_idpspec5:
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

Instruction ParseInstructionIdaMips(Address address,
                                    CallGraph* /* call_graph */,
                                    FlowGraph* /* flow_graph */,
                                    TypeSystem* /* type_system */) {
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

  Address next_instruction = 0;
  xrefblk_t xref;
  for (bool ok = xref.first_from(static_cast<ea_t>(address), XREF_ALL);
       ok && xref.iscode; ok = xref.next_from()) {
    if (xref.type == fl_F) {
      next_instruction = xref.to;
      break;
    }
  }

  enum {
    aux_cop = 0x0003,   // Coprocessor number
    aux_fpu = 0x0004,   // Known FPU instruction?
    aux_dsp32 = 0x0008  // 32-bit displacement?
  };

  if ((cmd.auxpref & aux_fpu) && cmd.segpref != '\0' /* FPU format */) {
    mnemonic += '.';
    mnemonic += cmd.segpref;  // FPU format
    if (cmd.segpref == 'p' /* FPU format */) {
      mnemonic += 's';
    }
  } else if (cmd.itype == MIPS_pmfhl) {
    static const char* const pmfhl_p[] = {".lw", ".uw", ".slw", ".lh", ".sh"};
    int fmt = (get_long(cmd.ea) >> 6) & 0x1F;
    mnemonic += pmfhl_p[fmt];
  }

  return Instruction(address, next_instruction, cmd.size, mnemonic,
                     DecodeOperandsMips(address));
}
