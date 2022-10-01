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

#include "third_party/zynamics/binexport/ida/arm.h"

#include <cinttypes>
#include <string>

// clang-format off
#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <idp.hpp>                                              // NOLINT
#include <bytes.hpp>                                            // NOLINT
#include <ida.hpp>                                              // NOLINT
#include <ua.hpp>                                               // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT
// clang-format on

#include "third_party/absl/log/log.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/binexport/ida/names.h"
#include "third_party/zynamics/binexport/util/format.h"

namespace security::binexport {

// The condition code of the instruction will be kept in instruction.segpref:
#define ARM_cond segpref

namespace {

enum {
  aux_cond = 0x0001,      // set condition codes (S postfix is required)
  aux_byte = 0x0002,      // byte transfer (B postfix is required)
  aux_npriv = 0x0004,     // non-privileged transfer (T postfix is required)
  aux_regsh = 0x0008,     // shift count is held in a register (see o_shreg)
  aux_negoff = 0x0010,    // memory offset is negated in LDR,STR
  aux_wback = 0x0020,     // write back (! postfix is required)
  aux_wbackldm = 0x0040,  // write back for LDM/STM (! postfix is required)
  aux_postidx = 0x0080,   // post-indexed mode in LDR,STR
  aux_ltrans = 0x0100,    // long transfer in LDC/STC (L postfix is required)
  aux_wimm = 0x0200,      // thumb32 wide encoding of immediate constant
  aux_sb = 0x0400,        // signed byte (SB postfix)
  aux_sh = 0x0800,        // signed halfword (SH postfix)
  aux_h = 0x1000,         // halfword (H postfix)
  aux_p = 0x2000,         // priviledged (P postfix)
  aux_coproc = 0x4000,    // coprocessor instruction
  aux_wide = 0x8000,      // thumb32 instruction (.W suffix)
  aux_pac = 0x10000,      // Pointer Authentication Code (PAC_ flags)
  aux_ns = 0x20000,       // non-secure branch (NS suffix)
  aux_thumb32 = 0x40000,  // aux_wide sometimes turned off for pretty-printing
};

// Defined in incomplete IDA SDK header module/arm/arm.hpp
enum shift_t {
  LSL,
  LSR,
  ASR,
  ROR,
  RRX,
  MSL,
  UXTB,
  UXTH,
  UXTW,
  UXTX,
  SXTB,
  SXTH,
  SXTW,
  SXTX,
};

// Returns the string representation for a given barrel shifter type.
std::string GetShift(size_t shift_type) {
  switch (shift_type) {
    case LSL:
      return "LSL";
    case LSR:
      return "LSR";
    case ASR:
      return "ASR";
    case ROR:
      return "ROR";
    case RRX:
      return "RRX";
    case MSL:
      return "MSL";
    case UXTB:
      return "UXTB";
    case UXTH:
      return "UXTH";
    case UXTW:
      return "UXTW";
    case UXTX:
      return "UXTX";  // Same as LSL
    case SXTB:
      return "SXTB";
    case SXTH:
      return "SXTH";
    case SXTW:
      return "SXTW";
    case SXTX:
      return "SXTX";
    default:
      return absl::StrCat("<shift-", shift_type, ">");
  }
}

// Returns the name for a co processor register in the form "c" + register id.
std::string GetCoprocessorRegisterName(size_t register_id) {
  return absl::StrCat("c", register_id);
}

// Returns the name of a co processor in the form "p" + processor id.
std::string GetCoprocessorName(size_t processor_id) {
  return absl::StrCat("p", processor_id);
}

}  // namespace

Operands DecodeOperandsArm(const insn_t& instruction) {
  bool co_processor = (instruction.auxpref & aux_coproc) != 0;

  Operands operands;
  for (int operand_position = 0;
       operand_position < UA_MAXOP &&
       instruction.ops[operand_position].type != o_void;
       ++operand_position) {
    Expressions expressions;
    const op_t& operand = instruction.ops[operand_position];

    Expression* expression = nullptr;
    size_t operand_size = GetOperandByteSize(instruction, operand);
    switch (operand.type) {
      case o_void:  // No operand
        break;
      case o_reg: {  // Register
        if (operand_size != 0) {
          expression =
              Expression::Create(expression, GetSizePrefix(operand_size), 0,
                                 Expression::TYPE_SIZEPREFIX, 0);
          expressions.push_back(expression);
        }
        if (instruction.auxpref & aux_wbackldm) {
          expression = Expression::Create(expression, "!", 0,
                                          Expression::TYPE_OPERATOR, 0);
          expressions.push_back(expression);
        }
        expression = Expression::Create(
            expression, GetRegisterName(operand.reg, operand_size), 0,
            Expression::TYPE_REGISTER, 0);
        expressions.push_back(expression);
        break;
      }
      case o_mem: {  // Direct memory reference
        const Address immediate = operand.addr;
        const Name name =
            GetName(instruction.ea, immediate, operand_position, false);
        if (operand_size != 0) {
          expression =
              Expression::Create(expression, GetSizePrefix(operand_size), 0,
                                 Expression::TYPE_SIZEPREFIX, 0);
          expressions.push_back(expression);
        }
        expression = Expression::Create(expression, "[", 0,
                                        Expression::TYPE_DEREFERENCE, 0);
        expressions.push_back(expression);
        expressions.push_back(
            expression = Expression::Create(
                expression, name.name, immediate,
                name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type, 0));
        break;
      }
      case o_phrase:
        // LDR     R0, [R0,R1]
        // o_phrase: the second register is held in secreg (specflag1)
        //           the shift type is in shtype (specflag2)
        //           the shift counter is in shcnt (value)
        if (operand_size != 0) {
          expression =
              Expression::Create(expression, GetSizePrefix(operand_size), 0,
                                 Expression::TYPE_SIZEPREFIX, 0);
          expressions.push_back(expression);
        }
        if (instruction.auxpref & aux_wback) {
          expression = Expression::Create(expression, "!", 0,
                                          Expression::TYPE_OPERATOR, 0);
          expressions.push_back(expression);
        }
        expression = Expression::Create(expression, "[", 0,
                                        Expression::TYPE_DEREFERENCE, 0);
        expressions.push_back(expression);
        expression = Expression::Create(expression, ",", 0,
                                        Expression::TYPE_OPERATOR, 0);
        expressions.push_back(expression);
        expressions.push_back(Expression::Create(
            expression, GetRegisterName(operand.reg, operand_size), 0,
            Expression::TYPE_REGISTER, 0));

        if (operand.value) {  // Shift
          expression = Expression::Create(
              expression, GetShift(static_cast<size_t>(operand.specflag2)), 0,
              Expression::TYPE_OPERATOR, 1);
          expressions.push_back(expression);
          expressions.push_back(Expression::Create(
              expression,
              GetRegisterName(static_cast<size_t>(operand.specflag1),
                              operand_size),
              0, Expression::TYPE_REGISTER, 0));
          expressions.push_back(
              Expression::Create(expression, "", operand.value,
                                 Expression::TYPE_IMMEDIATE_INT, 1));
        } else {
          expressions.push_back(Expression::Create(
              expression,
              GetRegisterName(static_cast<size_t>(operand.specflag1),
                              operand_size),
              0, Expression::TYPE_REGISTER, 1));
        }
        break;
      case o_displ: {
        const Address offset = operand.value;
        if (operand_size) {
          expression =
              Expression::Create(expression, GetSizePrefix(operand_size), 0,
                                 Expression::TYPE_SIZEPREFIX, 0);
          expressions.push_back(expression);
        }
        if (instruction.auxpref & aux_wback) {
          expression = Expression::Create(expression, "!", 0,
                                          Expression::TYPE_OPERATOR, 0);
          expressions.push_back(expression);
        }
        expression = Expression::Create(expression, "[", 0,
                                        Expression::TYPE_DEREFERENCE, 0);
        expressions.push_back(expression);
        if (operand.addr) {
          const Name name =
              GetName(instruction.ea, operand.addr, operand_position, false);
          expression = Expression::Create(expression, ",", 0,
                                          Expression::TYPE_OPERATOR, 0);
          expressions.push_back(expression);
          expressions.push_back(Expression::Create(
              expression, GetRegisterName(operand.reg, operand_size), 0,
              Expression::TYPE_REGISTER, 0));
          expressions.push_back(Expression::Create(
              expression, name.name, operand.addr,
              name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type, 1));
        } else {
          if (offset) {
            expression = Expression::Create(expression, "+", 0,
                                            Expression::TYPE_OPERATOR, 0);
            expressions.push_back(expression);
          }
          expressions.push_back(Expression::Create(
              expression, GetRegisterName(operand.reg, operand_size), 0,
              Expression::TYPE_REGISTER, 0));
          if (offset) {
            const Name name =
                GetName(instruction.ea, offset, operand_position, false);
            expressions.push_back(Expression::Create(
                expression, name.name, 0,
                name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type, 1));
          }
        }
        break;
      }
      case o_imm:  // Immediate value
        if (operand_size) {
          expression =
              Expression::Create(expression, GetSizePrefix(operand_size), 0,
                                 Expression::TYPE_SIZEPREFIX, 0);
          expressions.push_back(expression);
        }
        if (co_processor) {
          const Address immediate = operand.value;
          const Name name =
              GetName(instruction.ea, immediate, operand_position, false);
          expression = Expression::Create(expression, ",", 0,
                                          Expression::TYPE_OPERATOR, 0);
          expressions.push_back(expression);
          expressions.push_back(Expression::Create(
              expression,
              GetCoprocessorName(static_cast<size_t>(operand.specflag1)), 0,
              Expression::TYPE_REGISTER, 0));
          expressions.push_back(Expression::Create(
              expression, name.name, immediate,
              name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type, 1));
          co_processor = false;
        } else {
          const Address immediate = operand.value;
          const Name name =
              GetName(instruction.ea, immediate, operand_position, false);
          expression = Expression::Create(
              expression, name.name, immediate,
              name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type, 0);
          expressions.push_back(expression);
        }
        break;
      case o_far:     // Immediate Far Address  (CODE)
      case o_near: {  // Immediate Near Address (CODE)
        const Address immediate = operand.addr;
        const Name name =
            GetName(instruction.ea, immediate, operand_position, false);
        if (operand_size) {
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
      case o_idpspec0: {
        // o_shreg shifted register
        // op.reg - register
        // #define shtype specflag2 // op.shtype - shift type
        // #define shreg specflag1  // op.shreg - shift register
        // #define shcnt value      // op.shcnt - shift counter
        const ushort register_index = operand.reg;
        const char shift_type = operand.specflag2;
        const char shift_register = operand.specflag1;
        const uval_t shift_count = operand.value;
        if (operand_size) {
          expression =
              Expression::Create(expression, GetSizePrefix(operand_size), 0,
                                 Expression::TYPE_SIZEPREFIX, 0);
          expressions.push_back(expression);
        }
        expression = Expression::Create(expression, GetShift(shift_type), 0,
                                        Expression::TYPE_OPERATOR, 0);
        expressions.push_back(expression);
        expressions.push_back(Expression::Create(
            expression, GetRegisterName(register_index, operand_size), 0,
            Expression::TYPE_REGISTER, 0));
        if (shift_type == 4 /* RRX */) {
          // No further expression because it always rotates by one bit only.
          break;
        }
        if (shift_count) {
          expressions.push_back(Expression::Create(
              expression, "", shift_count, Expression::TYPE_IMMEDIATE_INT, 1));
        } else {
          expressions.push_back(Expression::Create(
              expression,
              GetRegisterName(shift_register,
                              GetOperandByteSize(instruction, operand)),
              0, Expression::TYPE_REGISTER, 1));
        }
        break;
      }
      case o_idpspec1:
        // #define o_reglist o_idpspec1 // Register list (for LDM/STM)
        // #define reglist specval      // The list is in op.specval
        // #define uforce specflag1     // PSR & force user bit
        if (operand_size) {
          expression =
              Expression::Create(expression, GetSizePrefix(operand_size), 0,
                                 Expression::TYPE_SIZEPREFIX, 0);
          expressions.push_back(expression);
        }
        expression = Expression::Create(expression, "{", 0,
                                        Expression::TYPE_OPERATOR, 0);
        expressions.push_back(expression);
        for (ea_t i = 0; i < 32; ++i) {
          if (operand.specval & ea_t(1 << i)) {
            expressions.push_back(Expression::Create(
                expression,
                GetRegisterName(static_cast<size_t>(i),
                                GetOperandByteSize(instruction, operand)),
                0, Expression::TYPE_REGISTER, static_cast<uint8_t>(i)));
          }
        }
        break;
      case o_idpspec2:
        // Coprocessor register list (for CDP)
        // #define CRd reg
        // #define CRn specflag1
        // #define CRm specflag2
        expression = Expression::Create(expression, ",", 0,
                                        Expression::TYPE_OPERATOR, 0);
        expressions.push_back(expression);
        expressions.push_back(Expression::Create(
            expression, GetCoprocessorRegisterName(operand.reg), 0,
            Expression::TYPE_REGISTER, 0));
        expressions.push_back(Expression::Create(
            expression, GetCoprocessorRegisterName(operand.specflag1), 0,
            Expression::TYPE_REGISTER, 1));
        expressions.push_back(Expression::Create(
            expression, GetCoprocessorRegisterName(operand.specflag2), 0,
            Expression::TYPE_REGISTER, 2));
        break;
      case o_idpspec3:  // Coprocessor register (for LDC/STC)
        expression = Expression::Create(expression,
                                        GetCoprocessorRegisterName(operand.reg),
                                        0, Expression::TYPE_REGISTER, 0);
        expressions.push_back(expression);
        break;
      case o_idpspec4:
        // Floating point register, Precision depends on 'dtyp'
        // #define o_fpreglist     o_idpspec4      // Floating point register
        // list
        // #define fpregstart      reg             // First register
        // #define fpregcnt        value           // number of registers
        if (operand_size) {
          expression =
              Expression::Create(expression, GetSizePrefix(operand_size), 0,
                                 Expression::TYPE_SIZEPREFIX, 0);
          expressions.push_back(expression);
        }
        expression = Expression::Create(expression, "{", 0,
                                        Expression::TYPE_OPERATOR, 0);
        expressions.push_back(expression);

        for (uval_t i = 0; i < operand.value; ++i) {
          expressions.push_back(Expression::Create(
              expression,
              GetRegisterName(static_cast<size_t>(operand.reg + i),
                              GetOperandByteSize(instruction, operand)),
              0, Expression::TYPE_REGISTER, static_cast<uint8_t>(i)));
        }
        break;
      case o_idpspec5:
        // For the current implementation of the IDA ARM processor module
        // it is guaranteed that o_idpspec5 is a String.
        // Used for DMB, DSB, SETEND, MRS, MSR, CPSID/CPSIE
        expressions.push_back(Expression::Create(
            expression, reinterpret_cast<const char*>(&operand.value), 0,
            Expression::TYPE_OPERATOR, 0));
        break;
      case o_idpspec5 + 1:
        // Arbitrary text stored in the operand structure starting at the
        // 'value' field up to 16 bytes (with terminating zero)
        LOG(INFO) << absl::StrCat("Warning: text storage not supported at ",
                                  FormatAddress(instruction.ea));
        break;
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

Instruction ParseInstructionIdaArm(const insn_t& instruction,
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
                     mnemonic, DecodeOperandsArm(instruction));
}

}  // namespace security::binexport
