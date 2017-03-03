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

#include "third_party/zynamics/binexport/ida/arm.h"

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

// The condition code of instruction will be kept in cmd.segpref:
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
  aux_ltrans = 0x0100,  // long transfer in LDC/STC (L postfix is required)
  aux_wimm = 0x0200,    // thumb32 wide encoding of immediate constant
  aux_sb = 0x0400,      // signed byte (SB postfix)
  aux_sh = 0x0800,      // signed halfword (SH postfix)
  aux_h = 0x1000,       // halfword (H postfix)
  aux_p = 0x2000,       // priviledged (P postfix)
  aux_coproc = 0x4000,  // coprocessor instruction
  aux_wide = 0x8000,    // thumb32 instruction (.W suffix)
};

enum neon_datatype_t ENUM_SIZE(char){
    DT_NONE = 0, DT_8,  DT_16,  DT_32,  DT_64,  DT_S8,  DT_S16, DT_S32,
    DT_S64,      DT_U8, DT_U16, DT_U32, DT_U64, DT_I8,  DT_I16, DT_I32,
    DT_I64,      DT_P8, DT_P16, DT_F16, DT_F32, DT_F64,
};

enum shift_t {
  LSL,  // logical left         LSL #0 - don't shift
  LSR,  // logical right        LSR #0 means LSR #32
  ASR,  // arithmetic right     ASR #0 means ASR #32
  ROR,  // rotate right         ROR #0 means RRX
  RRX,  // extended rotate right

  // ARMv8 shifts
  MSL,  // masked shift left (ones are shifted in from the right)

  // Extending register operations.
  UXTB,
  UXTH,
  UXTW,
  UXTX,  // Alias for LSL
  SXTB,
  SXTH,
  SXTW,
  SXTX,
};

// Returns the string representation for a given barrel shifter type.
const char* GetShift(size_t shift_type) {
  // TODO(cblichmann): We should be using shift_t from arm.hpp
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
      return "UXTX";  // Same as LSL.
    case SXTB:
      return "SXTB";
    case SXTH:
      return "SXTH";
    case SXTW:
      return "SXTW";
    case SXTX:
      return "SXTX";
    default:
      throw std::runtime_error("unsupported shift type: " +
                               std::to_string(shift_type) +
                               " in ARM->getShift()");
  }
}

// Returns the name for a co processor register in the form "c" + register id.
std::string GetCoprocessorRegisterName(size_t register_id) {
  return "c" + std::to_string(register_id);
}

// Returns the name of a co processor in the form "p" + processor id.
std::string GetCoprocessorName(size_t processor_id) {
  return "p" + std::to_string(processor_id);
}

}  // namespace

Operands DecodeOperandsArm(const Address address) {
  bool co_processor = cmd.auxpref & aux_coproc ? true : false;

  Operands operands;
  for (uint8_t operand_position = 0;
       operand_position < UA_MAXOP &&
       cmd.Operands[operand_position].type != o_void;
       ++operand_position) {
    Expressions expressions;
    const op_t& operand = cmd.Operands[operand_position];

    Expression* expression = NULL;
    switch (operand.type) {
      case o_void: {
        // no operand
        break;
      }
      case o_reg: {
        // register
        expressions.push_back(expression = Expression::Create(
                                  expression,
                                  GetSizePrefix(GetOperandByteSize(operand)), 0,
                                  Expression::TYPE_SIZEPREFIX, 0));
        if (cmd.auxpref & aux_wbackldm) {
          expressions.push_back(
              expression = Expression::Create(expression, "!", 0,
                                              Expression::TYPE_OPERATOR, 0));
        }
        expressions.push_back(
            expression = Expression::Create(
                expression,
                GetRegisterName(operand.reg, GetOperandByteSize(operand)), 0,
                Expression::TYPE_REGISTER, 0));
        break;
      }
      case o_mem: {
        // direct memory reference
        const Address immediate = operand.addr;
        const Name name = GetName(address, immediate, operand_position, false);
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
      case o_phrase: {
        // LDR     R0, [R0,R1]
        // o_phrase: the second register is held in secreg (specflag1)
        //           the shift type is in shtype (specflag2)
        //           the shift counter is in shcnt (value)
        expressions.push_back(expression = Expression::Create(
                                  expression,
                                  GetSizePrefix(GetOperandByteSize(operand)), 0,
                                  Expression::TYPE_SIZEPREFIX, 0));
        if (cmd.auxpref & aux_wback) {
          expressions.push_back(
              expression = Expression::Create(expression, "!", 0,
                                              Expression::TYPE_OPERATOR, 0));
        }
        expressions.push_back(
            expression = Expression::Create(expression, "[", 0,
                                            Expression::TYPE_DEREFERENCE, 0));
        expressions.push_back(
            expression = Expression::Create(expression, ",", 0,
                                            Expression::TYPE_OPERATOR, 0));
        expressions.push_back(Expression::Create(
            expression,
            GetRegisterName(operand.reg, GetOperandByteSize(operand)), 0,
            Expression::TYPE_REGISTER, 0));

        if (operand.value) {  // shift
          expressions.push_back(expression = Expression::Create(
                                    expression, GetShift(operand.specflag2), 0,
                                    Expression::TYPE_OPERATOR, 1));
          expressions.push_back(Expression::Create(
              expression,
              GetRegisterName(operand.specflag1, GetOperandByteSize(operand)),
              0, Expression::TYPE_REGISTER, 0));
          expressions.push_back(
              Expression::Create(expression, "", operand.value,
                                 Expression::TYPE_IMMEDIATE_INT, 1));
        } else {
          expressions.push_back(Expression::Create(
              expression,
              GetRegisterName(operand.specflag1, GetOperandByteSize(operand)),
              0, Expression::TYPE_REGISTER, 1));
        }
        break;
      }
      case o_displ: {
        const Address offset = operand.value;

        expressions.push_back(expression = Expression::Create(
                                  expression,
                                  GetSizePrefix(GetOperandByteSize(operand)), 0,
                                  Expression::TYPE_SIZEPREFIX, 0));
        if (cmd.auxpref & aux_wback)
          expressions.push_back(
              expression = Expression::Create(expression, "!", 0,
                                              Expression::TYPE_OPERATOR, 0));
        expressions.push_back(
            expression = Expression::Create(expression, "[", 0,
                                            Expression::TYPE_DEREFERENCE, 0));
        if (operand.addr) {
          const Name name =
              GetName(address, operand.addr, operand_position, false);
          expressions.push_back(
              expression = Expression::Create(expression, ",", 0,
                                              Expression::TYPE_OPERATOR, 0));
          expressions.push_back(Expression::Create(
              expression,
              GetRegisterName(operand.reg, GetOperandByteSize(operand)), 0,
              Expression::TYPE_REGISTER, 0));
          expressions.push_back(Expression::Create(
              expression, name.name, operand.addr,
              name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type, 1));
        } else {
          if (offset) {
            expressions.push_back(
                expression = Expression::Create(expression, "+", 0,
                                                Expression::TYPE_OPERATOR, 0));
          }
          expressions.push_back(Expression::Create(
              expression,
              GetRegisterName(operand.reg, GetOperandByteSize(operand)), 0,
              Expression::TYPE_REGISTER, 0));
          if (offset) {
            const Name name = GetName(address, offset, operand_position, false);
            expressions.push_back(Expression::Create(
                expression, name.name, 0,
                name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type,
                1));
          }
        }
        break;
      }
      case o_imm: {
        // immediate value
        if (co_processor) {
          const Address immediate = operand.value;
          const Name name =
              GetName(address, immediate, operand_position, false);

          expressions.push_back(expression = Expression::Create(
                                    expression,
                                    GetSizePrefix(GetOperandByteSize(operand)),
                                    0, Expression::TYPE_SIZEPREFIX, 0));
          expressions.push_back(
              expression = Expression::Create(expression, ",", 0,
                                              Expression::TYPE_OPERATOR, 0));
          expressions.push_back(Expression::Create(
              expression, GetCoprocessorName(operand.specflag1), 0,
              Expression::TYPE_REGISTER, 0));
          expressions.push_back(Expression::Create(
              expression, name.name, immediate,
              name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type, 1));
          co_processor = false;
        } else {
          const Address immediate = operand.value;
          const Name name =
              GetName(address, immediate, operand_position, false);

          expressions.push_back(expression = Expression::Create(
                                    expression,
                                    GetSizePrefix(GetOperandByteSize(operand)),
                                    0, Expression::TYPE_SIZEPREFIX, 0));
          expressions.push_back(
              expression = Expression::Create(
                  expression, name.name, immediate,
                  name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type,
                  0));
        }
        break;
      }
      case o_far:  // immediate Far Address  (CODE)
      case o_near: {
        // Immediate Near Address (CODE)
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
      case o_idpspec0: {
        // o_shreg shifted register
        // op.reg - register
        // #define shtype specflag2 // op.shtype - shift type
        // #define shreg specflag1  // op.shreg - shift register
        // #define shcnt value      // op.shcnt - shift counter
        const ushort registerIndex = operand.reg;
        const char shiftType = operand.specflag2;
        const char shiftRegister = operand.specflag1;
        const uval_t shiftCount = operand.value;
        expressions.push_back(expression = Expression::Create(
                                  expression,
                                  GetSizePrefix(GetOperandByteSize(operand)), 0,
                                  Expression::TYPE_SIZEPREFIX, 0));
        expressions.push_back(
            expression = Expression::Create(expression, GetShift(shiftType), 0,
                                            Expression::TYPE_OPERATOR, 0));
        expressions.push_back(Expression::Create(
            expression,
            GetRegisterName(registerIndex, GetOperandByteSize(operand)), 0,
            Expression::TYPE_REGISTER, 0));
        if (shiftType == 4) {
          // == RRX, no further expression because it
          // always rotates by one bit only
          break;
        }
        if (shiftCount) {
          expressions.push_back(Expression::Create(
              expression, "", shiftCount, Expression::TYPE_IMMEDIATE_INT, 1));
        } else {
          expressions.push_back(Expression::Create(
              expression,
              GetRegisterName(shiftRegister, GetOperandByteSize(operand)), 0,
              Expression::TYPE_REGISTER, 1));
        }
        break;
      }
      case o_idpspec1: {
        // #define o_reglist o_idpspec1 // Register list (for LDM/STM)
        // #define reglist specval      // The list is in op.specval
        // #define uforce specflag1     // PSR & force user bit
        expressions.push_back(expression = Expression::Create(
                                  expression,
                                  GetSizePrefix(GetOperandByteSize(operand)), 0,
                                  Expression::TYPE_SIZEPREFIX, 0));
        expressions.push_back(
            expression = Expression::Create(expression, "{", 0,
                                            Expression::TYPE_OPERATOR, 0));
        for (ea_t i = 0; i < 32; ++i) {
          if (operand.specval & ea_t(1 << i)) {
            expressions.push_back(Expression::Create(
                expression, GetRegisterName(static_cast<size_t>(i),
                                            GetOperandByteSize(operand)),
                0, Expression::TYPE_REGISTER, static_cast<uint8_t>(i)));
          }
        }
        break;
      }
      case o_idpspec2: {
        // Coprocessor register list (for CDP)
        // #define CRd reg
        // #define CRn specflag1
        // #define CRm specflag2
        expressions.push_back(
            expression = Expression::Create(expression, ",", 0,
                                            Expression::TYPE_OPERATOR, 0));
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
      }
      case o_idpspec3: {  // Coprocessor register (for LDC/STC)
        expressions.push_back(expression = Expression::Create(
                                  expression,
                                  GetCoprocessorRegisterName(operand.reg), 0,
                                  Expression::TYPE_REGISTER, 0));
        break;
      }
      case o_idpspec4: {
        // Floating point register, Precision depends on 'dtyp'
        // #define o_fpreglist     o_idpspec4      // Floating point register
        // list
        // #define fpregstart      reg             // First register
        // #define fpregcnt        value           // number of registers

        expressions.push_back(expression = Expression::Create(
                                  expression,
                                  GetSizePrefix(GetOperandByteSize(operand)), 0,
                                  Expression::TYPE_SIZEPREFIX, 0));
        expressions.push_back(
            expression = Expression::Create(expression, "{", 0,
                                            Expression::TYPE_OPERATOR, 0));

        for (uval_t i = 0; i < operand.value; ++i) {
          expressions.push_back(Expression::Create(
              expression, GetRegisterName(static_cast<size_t>(operand.reg + i),
                                          GetOperandByteSize(operand)),
              0, Expression::TYPE_REGISTER, static_cast<uint8_t>(i)));
        }
        break;
      }
      case o_idpspec5: {
        // For the current implementation of the IDA ARM processor module
        // it is guaranteed that o_idpspec5 is a String.
        // Used for DMB, DSB, SETEND, MRS, MSR, CPSID/CPSIE
        const char* text = (const char*)&operand.value;
        expressions.push_back(Expression::Create(expression, text, 0,
                                                 Expression::TYPE_OPERATOR, 0));
        break;
      }
      case o_idpspec5 + 1: {
        // Arbitrary text stored in the operand structure starting at the
        // 'value' field up to 16 bytes (with terminating zero)
        LOG(INFO) << StringPrintf(
            "warning: text storage not yet supported (%08" PRIx64 ")", address);
        break;
      }
      default: {
        LOG(INFO) << StringPrintf(
            "warning: unknown operand type %d at %08" PRIx64,
            static_cast<int>(operand.type), address);
        break;
      }
    }
    operands.push_back(Operand::CreateOperand(expressions));
  }

  Operands(operands).swap(operands);
  return operands;
}

Instruction ParseInstructionIdaArm(Address address, CallGraph* /* call_graph */,
                                   FlowGraph* /* flow_graph */,
                                   TypeSystem* /* type_system */) {
  // Part of this code comes directly from support@hex-rays.com (Igor).
  // It was changed according to our needs but reflects basically what IDA
  // does.

  // const insn_t & instruction = cmd;
  // size_t iType = cmd.itype;
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

  char precision = 0;
  switch (cmd.itype) {
    case ARM_fldms:
    case ARM_fstms:
      precision = 'S';
      break;
    case ARM_fldmd:
    case ARM_fstmd:
      precision = 'D';
      break;
    case ARM_fldmx:
    case ARM_fstmx:
      precision = 'X';
      break;
  }

  if (cmd.itype == ARM_it) {
    unsigned int first_cond = (cmd.ARM_cond & 0x1);

    switch (cmd.insnpref) {
      case 0x0:
        break;

      // <x>    <y>    <z>    mask[3]        mask[2]        mask[1]
      // mask[0]
      // T    T    T    firstcond[0]    firstcond[0]    firstcond[0]    1
      // E    E    E    NOT firstcond[0]  NOT firstcond[0]  NOT firstcond[0]  1
      case 0x1:
        first_cond == 0x1 ? mnemonic += "EEE " : mnemonic += "TTT ";
        break;

      // <x>    <y>    <z>    mask[3]        mask[2]        mask[1]
      // mask[0]
      // T    T    omitted firstcond[0]    firstcond[0]    1          0
      // E    E    omitted NOT firstcond[0]  NOT firstcond[0]  1          0
      case 0x2:
        first_cond == 0x1 ? mnemonic += "EE " : mnemonic += "TT ";
        break;

      // <x>    <y>    <z>    mask[3]        mask[2]        mask[1]
      // mask[0]
      // E    E    T    NOT firstcond[0]  NOT firstcond[0]  firstcond[0]    1
      // T    T    E    firstcond[0]    firstcond[0]    NOT firstcond[0]  1
      case 0x3:
        first_cond == 0x1 ? mnemonic += "EET " : mnemonic += "TTE ";
        break;

      // <x>    <y>    <z>    mask[3]        mask[2]        mask[1]
      // mask[0]
      // T    omitted omitted firstcond[0]    1          0          0
      // E    omitted omitted NOT  firstcond[0]  1          0          0
      case 0x4:
        first_cond == 0x1 ? mnemonic += "E " : mnemonic += "T ";
        break;

      // <x>    <y>    <z>    mask[3]        mask[2]        mask[1]
      // mask[0]
      // T    E    T    firstcond[0]    NOT firstcond[0]  firstcond[0]    1
      // E    T    E    NOT firstcond[0]  firstcond[0]    NOT firstcond[0]  1
      case 0x5:
        first_cond == 0x1 ? mnemonic += "ETE " : mnemonic += "TET ";
        break;

      // <x>    <y>    <z>    mask[3]        mask[2]        mask[1]
      // mask[0]
      // E    T    omitted NOT  firstcond[0]  firstcond[0]    1          0
      // T    E    omitted firstcond[0]    NOT firstcond[0]  1          0
      case 0x6:
        first_cond == 0x1 ? mnemonic += "ET " : mnemonic += "TE ";
        break;

      // <x>    <y>    <z>    mask[3]        mask[2]        mask[1]
      // mask[0]
      // E    T    T    NOT firstcond[0]  firstcond[0]    firstcond[0]    1
      // T    E    E    firstcond[0]    NOT firstcond[0]  NOT firstcond[0]  1
      case 0x7:
        first_cond == 0x1 ? mnemonic += "ETT " : mnemonic += "TEE ";
        break;

      // <x>    <y>    <z>    mask[3]        mask[2]        mask[1]
      // mask[0]
      // omitted omitted omitted  1          0          0          0
      case 0x8:
        mnemonic += " ";
        break;

      // <x>    <y>    <z>    mask[3]        mask[2]        mask[1]
      // mask[0]
      // E    T    T    NOT firstcond[0]  firstcond[0]    firstcond[0]    1
      // T    E    E    firstcond[0]    NOT firstcond[0]  NOT firstcond[0]  1
      case 0x9:
        first_cond == 0x1 ? mnemonic += "TEE " : mnemonic += "ETT ";
        break;

      // <x>    <y>    <z>    mask[3]        mask[2]        mask[1]
      // mask[0]
      // E    T    omitted NOT  firstcond[0]  firstcond[0]    1          0
      // T    E    omitted firstcond[0]    NOT firstcond[0]  1          0
      case 0xa:
        first_cond == 0x1 ? mnemonic += "TE " : mnemonic += "ET ";
        break;

      // <x>    <y>    <z>    mask[3]        mask[2]        mask[1]
      // mask[0]
      // T    E    T    firstcond[0]    NOT firstcond[0]  firstcond[0]    1
      // E    T    E    NOT firstcond[0]  firstcond[0]    NOT firstcond[0]  1
      case 0xb:
        first_cond == 0x1 ? mnemonic += "TET " : mnemonic += "ETE ";
        break;

      // <x>    <y>    <z>    mask[3]        mask[2]        mask[1]
      // mask[0]
      // T    E    T    firstcond[0]    NOT firstcond[0]  firstcond[0]    1
      // E    T    E    NOT firstcond[0]  firstcond[0]    NOT firstcond[0]  1
      case 0xc:
        first_cond == 0x1 ? mnemonic += "T " : mnemonic += "E ";
        break;

      // <x>    <y>    <z>    mask[3]        mask[2]        mask[1]
      // mask[0]
      // E    E    T    NOT firstcond[0]  NOT firstcond[0]  firstcond[0]    1
      // T    T    E    firstcond[0]    firstcond[0]    NOT firstcond[0]  1
      case 0xd:
        first_cond == 0x1 ? mnemonic += "TTE " : mnemonic += "EET ";
        break;

      // <x>    <y>    <z>    mask[3]        mask[2]        mask[1]
      // mask[0]
      // T    T    omitted firstcond[0]    firstcond[0]    1          0
      // E    E    omitted NOT firstcond[0]  NOT firstcond[0]  1          0
      case 0xe:
        first_cond == 0x1 ? mnemonic += "TT " : mnemonic += "EE ";
        break;

      // <x>    <y>    <z>    mask[3]        mask[2]        mask[1]
      // mask[0]
      // T    T    T    firstcond[0]    firstcond[0]    firstcond[0]    1
      // E    E    E    NOT firstcond[0]  NOT firstcond[0]  NOT firstcond[0]  1
      case 0xf:
        first_cond == 0x1 ? mnemonic += "TTT " : mnemonic += "EEE ";
        break;
      default:
        break;
    }
  }

  switch (cmd.ARM_cond) {
    case 0x0:
      mnemonic += "EQ";
      break;
    case 0x1:
      mnemonic += "NE";
      break;
    case 0x2:
      mnemonic += "CS";
      break;
    case 0x3:
      mnemonic += "CC";
      break;
    case 0x4:
      mnemonic += "MI";
      break;
    case 0x5:
      mnemonic += "PL";
      break;
    case 0x6:
      mnemonic += "VS";
      break;
    case 0x7:
      mnemonic += "VC";
      break;
    case 0x8:
      mnemonic += "HI";
      break;
    case 0x9:
      mnemonic += "LS";
      break;
    case 0xA:
      mnemonic += "GE";
      break;
    case 0xB:
      mnemonic += "LT";
      break;
    case 0xC:
      mnemonic += "GT";
      break;
    case 0xD:
      mnemonic += "LE";
      break;
    case 0xF:
      mnemonic += "NV";
      break;
    default:
      break;
  }

  // add prefix (if any) to string instructions
  if (cmd.auxpref & aux_cond) mnemonic += "S";
  if (cmd.auxpref & aux_byte) mnemonic += "B";
  if (cmd.auxpref & aux_npriv) mnemonic += "T";
  if (cmd.auxpref & aux_ltrans) mnemonic += "L";
  if (cmd.auxpref & aux_sb) mnemonic += "SB";
  if (cmd.auxpref & aux_sh) mnemonic += "SH";
  if (cmd.auxpref & aux_h) mnemonic += "H";
  if (cmd.auxpref & aux_p) mnemonic += "H";

  if (cmd.itype == ARM_ldm || cmd.itype == ARM_stm || cmd.itype == ARM_fldmd ||
      cmd.itype == ARM_fstmd || cmd.itype == ARM_fldms ||
      cmd.itype == ARM_fstms || cmd.itype == ARM_fldmx ||
      cmd.itype == ARM_fstmx || cmd.itype == ARM_vldm ||
      cmd.itype == ARM_vstm || cmd.itype == ARM_rfe || cmd.itype == ARM_srs ||
      precision != 0) {
    size_t n = (cmd.auxpref & aux_negoff) ? 0 : 2;
    if ((cmd.auxpref & aux_postidx) == 0) n |= 1;
    if (cmd.itype == ARM_ldm || cmd.itype == ARM_rfe) n |= 4;
    static const char* const other[] = {"DA", "DB", "IA", "IB",
                                        "DA", "DB", "IA", "IB"};
    static const char* const stack[] = {"ED", "FD", "EA", "FA",
                                        "FA", "EA", "FD", "ED"};
    const op_t& operand = cmd.Operands[0];
    if (GetRegisterName(operand.reg, GetOperandByteSize(operand)) ==
            std::string("SP") &&
        cmd.itype != ARM_srs) {
      mnemonic += stack[n];
    } else {
      mnemonic += other[n];
    }
    if (precision != 0) {
      mnemonic += precision;
    }
  }

  if (cmd.itype == ARM_ldrd || cmd.itype == ARM_strd) {
    mnemonic += "D";
  }

  if (cmd.auxpref & aux_wide) {
    if (cmd.auxpref & aux_wimm) {
      mnemonic += "W";
    } else {
      mnemonic += ".W";
    }
  }

  if (cmd.insnpref & 0x80) {
    switch (neon_datatype_t(cmd.insnpref & 0x7F)) {
      case 0x0:
        break;  // DT_NONE = 0,
      case 0x1:
        mnemonic += ".8";
        break;  // DT_8,
      case 0x2:
        mnemonic += ".16";
        break;  // DT_16,
      case 0x3:
        mnemonic += ".32";
        break;  // DT_32,
      case 0x4:
        mnemonic += ".64";
        break;  // DT_64,
      case 0x5:
        mnemonic += ".S8";
        break;  // DT_S8,
      case 0x6:
        mnemonic += ".S16";
        break;  // DT_S16,
      case 0x7:
        mnemonic += ".S32";
        break;  // DT_S32,
      case 0x8:
        mnemonic += ".S64";
        break;  // DT_S64,
      case 0x9:
        mnemonic += ".U8";
        break;  // DT_U8,
      case 0xa:
        mnemonic += ".U16";
        break;  // DT_U16,
      case 0xb:
        mnemonic += ".U32";
        break;  // DT_U32,
      case 0xc:
        mnemonic += ".U64";
        break;  // DT_U64,
      case 0xd:
        mnemonic += ".I8";
        break;  // DT_I8,
      case 0xe:
        mnemonic += ".I16";
        break;  // DT_I16,
      case 0xf:
        mnemonic += ".I32";
        break;  // DT_I32,
      case 0x10:
        mnemonic += ".I64";
        break;  // DT_I64,
      case 0x11:
        mnemonic += ".P8";
        break;  // DT_P8,
      case 0x12:
        mnemonic += ".P16";
        break;  // DT_P16,
      case 0x13:
        mnemonic += ".F16";
        break;  // DT_F16,
      case 0x14:
        mnemonic += ".F32";
        break;  // DT_F32,
      case 0x15:
        mnemonic += ".F64";
        break;  // DT_F64,
      default:
        break;
    }
  }

  if (cmd.itype == ARM_vcvt || cmd.itype == ARM_vcvtr ||
      cmd.itype == ARM_vcvtb || cmd.itype == ARM_vcvtt) {
    switch (neon_datatype_t(cmd.Op1.specflag1)) {
      case 0x0:
        break;  // DT_NONE = 0,
      case 0x1:
        mnemonic += ".8";
        break;  // DT_8,
      case 0x2:
        mnemonic += ".16";
        break;  // DT_16,
      case 0x3:
        mnemonic += ".32";
        break;  // DT_32,
      case 0x4:
        mnemonic += ".64";
        break;  // DT_64,
      case 0x5:
        mnemonic += ".S8";
        break;  // DT_S8,
      case 0x6:
        mnemonic += ".S16";
        break;  // DT_S16,
      case 0x7:
        mnemonic += ".S32";
        break;  // DT_S32,
      case 0x8:
        mnemonic += ".S64";
        break;  // DT_S64,
      case 0x9:
        mnemonic += ".U8";
        break;  // DT_U8,
      case 0xa:
        mnemonic += ".U16";
        break;  // DT_U16,
      case 0xb:
        mnemonic += ".U32";
        break;  // DT_U32,
      case 0xc:
        mnemonic += ".U64";
        break;  // DT_U64,
      case 0xd:
        mnemonic += ".I8";
        break;  // DT_I8,
      case 0xe:
        mnemonic += ".I16";
        break;  // DT_I16,
      case 0xf:
        mnemonic += ".I32";
        break;  // DT_I32,
      case 0x10:
        mnemonic += ".I64";
        break;  // DT_I64,
      case 0x11:
        mnemonic += ".P8";
        break;  // DT_P8,
      case 0x12:
        mnemonic += ".P16";
        break;  // DT_P16,
      case 0x13:
        mnemonic += ".F16";
        break;  // DT_F16,
      case 0x14:
        mnemonic += ".F32";
        break;  // DT_F32,
      case 0x15:
        mnemonic += ".F64";
        break;  // DT_F64,
      default:
        break;
    }
  }

  return Instruction(address, next_instruction, cmd.size, mnemonic,
                     DecodeOperandsArm(address));
}
