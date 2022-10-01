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

#include "third_party/zynamics/binexport/ida/mips.h"

#include <cinttypes>
#include <string>

// clang-format off
#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <idp.hpp>                                              // NOLINT
#include <allins.hpp>                                           // NOLINT
#include <bytes.hpp>                                            // NOLINT
#include <ida.hpp>                                              // NOLINT
#include <ua.hpp>                                               // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT
// clang-format on

#include "third_party/absl/log/log.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/binexport/call_graph.h"
#include "third_party/zynamics/binexport/ida/names.h"
#include "third_party/zynamics/binexport/util/format.h"

namespace security::binexport {
namespace {

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

std::string GetFloatingPointRegisterName(const size_t register_id) {
  return "$f" + std::to_string(register_id);
}

Operands DecodeOperandsMips(const insn_t& instruction) {
  Operands operands;
  for (int operand_position = 0;
       operand_position < UA_MAXOP &&
       instruction.ops[operand_position].type != o_void;
       ++operand_position) {
    Expressions expressions;
    // const insn_t & instruction = cmd;
    const op_t& operand = instruction.ops[operand_position];

    Expression* expression = nullptr;
    size_t operand_size = GetOperandByteSize(instruction, operand);
    switch (operand.type) {
      case o_void:  // No operand
        break;
      case o_reg:  // Register
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
      case o_mem: {  // Direct memory reference
        const Address immediate = operand.addr;
        const Name name = GetName(instruction.ea, immediate, operand_position,
                                  false);  // @bug: we don't get =unk_ names yet
        if (operand_size != 0) {
          expression =
              Expression::Create(expression, GetSizePrefix(operand_size), 0,
                                 Expression::TYPE_SIZEPREFIX, 0);
          expressions.push_back(expression);
        }
        expression = Expression::Create(expression, "[", 0,
                                        Expression::TYPE_DEREFERENCE, 0);
        expressions.push_back(expression);
        expression = Expression::Create(
            expression, name.name, immediate,
            name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type, 0);
        expressions.push_back(expression);
        break;
      }
      case o_phrase:  // reg(reg)
        // @todo: test! I have not encountered this case yet!
        if (operand_size != 0) {
          expression =
              Expression::Create(expression, GetSizePrefix(operand_size), 0,
                                 Expression::TYPE_SIZEPREFIX, 0);
          expressions.push_back(expression);
        }
        expression = Expression::Create(expression, "+", 0,
                                        Expression::TYPE_OPERATOR, 0);
        expressions.push_back(expression);
        expressions.push_back(Expression::Create(
            expression, GetRegisterName(operand.reg, operand_size), 0,
            Expression::TYPE_REGISTER, 0));
        expressions.push_back(Expression::Create(
            expression, GetRegisterName(operand.specflag1, operand_size), 0,
            Expression::TYPE_REGISTER, 1));
        break;
      case o_displ: {  // imm(reg)
        const Address immediate = operand.addr;
        const Name name =
            GetName(instruction.ea, operand.addr, operand_position, false);
        if (operand_size != 0) {
          expression =
              Expression::Create(expression, GetSizePrefix(operand_size), 0,
                                 Expression::TYPE_SIZEPREFIX, 0);
          expressions.push_back(expression);
        }
        expression = Expression::Create(expression, "+", 0,
                                        Expression::TYPE_OPERATOR, 0);
        expressions.push_back(expression);
        expressions.push_back(Expression::Create(
            expression, GetRegisterName(operand.reg, operand_size), 0,
            Expression::TYPE_REGISTER, 0));
        expressions.push_back(Expression::Create(
            expression, name.name, immediate,
            name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type, 1));
        break;
      }
      case o_imm: {  // immediate value
        const Address immediate = operand.value;
        const Name name =
            GetName(instruction.ea, immediate, operand_position, false);
        if (operand_size != 0) {
          expression =
              Expression::Create(expression, GetSizePrefix(operand_size), 0,
                                 Expression::TYPE_SIZEPREFIX, 0);
          expressions.push_back(expression);
        }
        expression = Expression::Create(
            expression, name.name, immediate,
            name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type, 0);
        expressions.push_back(expression);
        break;
      }
      case o_far:   // immediate Far Address  (CODE)
      case o_near:  // Immediate Near Address (CODE)
      {
        const Address immediate = operand.addr;
        const Name name =
            GetName(instruction.ea, immediate, operand_position, false);
        if (operand_size != 0) {
          expression =
              Expression::Create(expression, GetSizePrefix(operand_size), 0,
                                 Expression::TYPE_SIZEPREFIX, 0);
          expressions.push_back(expression);
        }
        expression = Expression::Create(
            expression, name.name, immediate,
            name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type, 0);
        expressions.push_back(expression);
        break;
      }
      case o_idpspec0:
        // #define o_cmtimm        o_idpspec0  // commented out immediate number
        LOG(INFO) << absl::StrCat(
            "Warning: commented out immediate number operand type not "
            "supported at ",
            FormatAddress(instruction.ea));

        break;
      case o_idpspec1:
        // #define o_coreg         o_idpspec1  // coprocesor register
        // #define   del           specflag1   //   rsp: del is here
        if (operand_size != 0) {
          expression =
              Expression::Create(expression, GetSizePrefix(operand_size), 0,
                                 Expression::TYPE_SIZEPREFIX, 0);
          expressions.push_back(expression);
        }
        if (instruction.auxpref & 0x5) {
          expression = Expression::Create(
              expression, GetFloatingPointRegisterName(operand.reg), 0,
              Expression::TYPE_REGISTER, 0);
          expressions.push_back(expression);
        } else {
          expression = Expression::Create(
              expression, GetCoprocessorRegisterName(operand.reg), 0,
              Expression::TYPE_REGISTER, 0);
          expressions.push_back(expression);
        }

        break;
      case o_idpspec3:
        // #define o_cc o_idpspec3  // coprocessor condition
        LOG(INFO) << absl::StrCat(
            "Warning: coprocessor condition not yet supported at ",
            FormatAddress(instruction.ea));
        break;
      case o_idpspec2:
      case o_idpspec4:
      case o_idpspec5:
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

Instruction ParseInstructionIdaMips(const insn_t& instruction,
                                    CallGraph* /* call_graph */,
                                    FlowGraph* /* flow_graph */) {
  if (!IsCode(instruction.ea)) {
    return Instruction(instruction.ea);
  }
  std::string mnemonic = GetMnemonic(instruction.ea);
  if (mnemonic.empty()) {
    return Instruction(instruction.ea);
  }

  Address next_instruction = 0;
  xrefblk_t xref;
  for (bool ok = xref.first_from(instruction.ea, XREF_ALL); ok && xref.iscode;
       ok = xref.next_from()) {
    if (xref.type == fl_F) {
      next_instruction = xref.to;
      break;
    }
  }

  return Instruction(instruction.ea, next_instruction, instruction.size,
                     mnemonic, DecodeOperandsMips(instruction));
}

}  // namespace security::binexport
