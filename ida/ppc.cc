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

#include "third_party/zynamics/binexport/ida/ppc.h"

#include <cinttypes>
#include <sstream>
#include <string>

#include "third_party/zynamics/binexport/ida/pro_forward.h"  // NOLINT
#include <bytes.hpp>                                         // NOLINT
#include <ida.hpp>                                           // NOLINT
#include <ua.hpp>                                            // NOLINT

#include "base/logging.h"
#include "base/stringprintf.h"
#include "third_party/zynamics/binexport/call_graph.h"
#include "third_party/zynamics/binexport/ida/names.h"
#include "third_party/zynamics/binexport/instruction.h"

// Various bit definitions:
enum {
  PPC_aux_oe = 0x0001,    // enable modification of OV and SO     o
  PPC_aux_rc = 0x0002,    // update CR                            .
  PPC_aux_aa = 0x0004,    // absolute address                     a
  PPC_aux_lk = 0x0008,    // update LR                            l
  PPC_aux_l = 0x0010,     // 64-bit compare                       lr
  PPC_aux_sh = 0x0020,    // shift is present
  PPC_aux_mb = 0x0040,    // mb is present
  PPC_aux_me = 0x0080,    // me is present
  PPC_aux_lr = 0x0100,    // goto LR
  PPC_aux_ctr = 0x0200,   // goto CTR                             ctr
  PPC_aux_dec = 0x0400,   // decimal immediate
  PPC_aux_plus = 0x0800,  // predict branch to be taken           +
  PPC_aux_minus = 0x1000  // predict branch to be not taken       -
};

std::string GetDeviceControlRegisterName(const Address register_index) {
  std::stringstream name;
  name << "DCR" << std::hex << register_index;
  return name.str();
}

std::string GetUnknownRegisterNameString(const Address address,
                                         const Address register_index) {
  std::stringstream name;
  name << "unknown special purpose register " << register_index
       << " at instruction " << std::hex << address;
  return name.str();
}

std::string GetUnknownRegisterName(const Address register_index) {
  std::stringstream name;
  name << "0x" << std::hex << std::uppercase << register_index;
  return name.str();
}

std::string GetConditionRegisterName(const size_t register_id) {
  return "cr" + std::to_string(register_id);
}

std::string GetSpecialPurposeRegisterName(const Address /*address*/,
                                          const Address register_index) {
  // copied over from Ero's Python exporter:
  //# Special Purpose Registers. Looked up in GDB's source code,
  //# IDA and the Freescale's PowerPC MPC823e manual
  switch (register_index) {
    case 0:
      return "mq";
    case 1:
      return "xer";
    case 4:
      return "rtcu";
    case 5:
      return "rtcl";
    case 8:
      return "lr";
    case 9:
      return "ctr";
    //        case #9: "cnt";  // # IDA defines 9 to be CTR; I looked up
    //              // # this from GDB"s source so I ignore if
    //              // # CNT being 9 too is an error
    case 18:
      return "dsisr";
    case 19:
      return "dar";
    case 22:
      return "dec";
    case 25:
      return "sdr1";
    case 26:
      return "srr0";
    case 27:
      return "srr1";
    case 80:
      return "eie";
    case 81:
      return "eid";
    case 82:
      return "nri";
    case 102:
      return "sp";
    case 144:
      return "cmpa";
    case 145:
      return "cmpb";
    case 146:
      return "cmpc";
    case 147:
      return "cmpd";
    case 148:
      return "icr";
    case 149:
      return "der";
    case 150:
      return "counta";
    case 151:
      return "countb";
    case 152:
      return "cmpe";
    case 153:
      return "cmpf";
    case 154:
      return "cmpg";
    case 155:
      return "cmph";
    case 156:
      return "lctrl1";
    case 157:
      return "lctrl2";
    case 158:
      return "ictrl";
    case 159:
      return "bar";
    case 256:
      return "vrsave";
    case 272:
      return "sprg0";
    case 273:
      return "sprg1";
    case 274:
      return "sprg2";
    case 275:
      return "sprg3";
    case 280:
      return "asr";
    case 282:
      return "ear";
    case 268:
      return "tbl_read";
    case 269:
      return "tbu_read";
    case 284:
      return "tbl_write";
    case 285:
      return "tbu_write";
    case 287:
      return "pvr";
    case 512:
      return "spefscr";
    case 528:
      return "ibat0u";
    case 529:
      return "ibat0l";
    case 530:
      return "ibat1u";
    case 531:
      return "ibat1l";
    case 532:
      return "ibat2u";
    case 533:
      return "ibat2l";
    case 534:
      return "ibat3u";
    case 535:
      return "ibat3l";
    case 536:
      return "dbat0u";
    case 537:
      return "dbat0l";
    case 538:
      return "dbat1u";
    case 539:
      return "dbat1l";
    case 540:
      return "dbat2u";
    case 541:
      return "dbat2l";
    case 542:
      return "dbat3u";
    case 543:
      return "dbat3l";
    case 560:
      return "ic_cst";
    case 561:
      return "ic_adr";
    case 562:
      return "ic_dat";
    case 568:
      return "dc_cst";
    case 569:
      return "dc_adr";
    case 570:
      return "dc_dat";
    case 630:
      return "dpdr";
    case 631:
      return "dpir";
    case 638:
      return "immr";
    case 784:
      return "mi_ctr";
    case 786:
      return "mi_ap";
    case 787:
      return "mi_epn";
    case 789:
      return "mi_twc";
    case 790:
      return "mi_rpn";
    //        case 816: return "mi_cam";
    //        case 817: return "mi_ram0";
    //        case 818: return "mi_ram1";
    case 792:
      return "md_ctr";
    case 793:
      return "m_casid";
    case 794:
      return "md_ap";
    case 795:
      return "md_epn";
    case 796:
      return "m_twb";
    case 797:
      return "md_twc";
    case 798:
      return "md_rpn";
    case 799:
      return "m_tw";
    case 816:
      return "mi_dbcam";
    case 817:
      return "mi_dbram0";
    case 818:
      return "mi_dbram1";
    //        #824: return "md_dbcam";
    case 824:
      return "md_cam";
    //        #825: return "md_dbram0";
    case 825:
      return "md_ram0";
    //        #826: return "md_dbram1";
    case 826:
      return "md_ram1";
    case 936:
      return "ummcr0";
    case 937:
      return "upmc1";
    case 938:
      return "upmc2";
    case 939:
      return "usia";
    case 940:
      return "ummcr1";
    case 941:
      return "upmc3";
    case 942:
      return "upmc4";
    case 944:
      return "zpr";
    case 945:
      return "pid";
    case 952:
      return "mmcr0";
    case 953:
      return "pmc1";
    //        case 953: return "sgr";
    case 954:
      return "pmc2";
    //        #954: return "dcwr";
    case 955:
      return "sia";
    case 956:
      return "mmcr1";
    case 957:
      return "pmc3";
    case 958:
      return "pmc4";
    case 959:
      return "sda";
    case 972:
      return "tbhu";
    case 973:
      return "tblu";
    case 976:
      return "dmiss";
    case 977:
      return "dcmp";
    case 978:
      return "hash1";
    case 979:
      return "hash2";
    //        #979: return "icdbdr";
    //        #980: return "imiss";
    case 980:
      return "esr";
    case 981:
      return "icmp";
    //        #981: return "dear";
    //        case 982: return "rpa";
    case 982:
      return "evpr";
    case 983:
      return "cdbcr";
    case 984:
      return "tsr";
    //        #984: return "602_tcr";
    //        #986: return "403_tcr";
    //        #986: return "ibr";
    case 986:
      return "tcr";
    case 987:
      return "pit";
    case 988:
      return "esasrr";
    //        #988: return "tbhi";
    case 989:
      return "tblo";
    case 990:
      return "srr2";
    //        #990: return "sebr";
    case 991:
      return "srr3";
    //        #991: return "ser";
    case 1008:
      return "hid0";
    //        #1008: return "dbsr";
    case 1009:
      return "hid1";
    case 1010:
      return "iabr";
    //        #1010: return "dbcr";
    case 1012:
      return "iac1";
    case 1013:
      return "dabr";
    //        #1013: return "iac2";
    case 1014:
      return "dac1";
    case 1015:
      return "dac2";
    case 1017:
      return "l2cr";
    case 1018:
      return "dccr";
    //        #1019: return "ictc";
    case 1019:
      return "iccr";
    //        #1020: return "thrm1";
    case 1020:
      return "pbl1";
    //        #1021: return "thrm2";
    case 1021:
      return "pbu1";
    //        #1022: return "thrm3";
    case 1022:
      return "pbl2";
    //        #1022: return "fpecr";
    //        #1022: return "lt";
    case 1023:
      return "pir";
    //        #1023: return "pbu2"
    default:
      return GetUnknownRegisterName(register_index);
  }
}

typedef std::list<const std::string*> TOperandStrings;

Operands DecodeOperandsPpc(const std::string& /* mnemonic */,
                           const Address address) {
  Operands operands;
  for (uint8_t operand_position = 0;
       operand_position < UA_MAXOP &&
           cmd.Operands[operand_position].type != o_void;
       ++operand_position) {
    Expressions expressions;
    const insn_t& instruction = cmd;
    const op_t& operand = cmd.Operands[operand_position];

    Expression* expression = 0;
    switch (operand.type) {
      case o_void:  // no operand
        break;
      case o_idpspec3:  // crfield      x.reg
      {
        expressions.push_back(expression = Expression::Create(
                                  expression,
                                  GetSizePrefix(GetOperandByteSize(operand)), 0,
                                  Expression::TYPE_SIZEPREFIX, 0));
        expressions.push_back(expression = Expression::Create(
                                  expression,
                                  GetConditionRegisterName(operand.reg), 0,
                                  Expression::TYPE_REGISTER, 0));
      } break;
      case o_reg:  // register
      {
        expressions.push_back(expression = Expression::Create(
                                  expression,
                                  GetSizePrefix(GetOperandByteSize(operand)), 0,
                                  Expression::TYPE_SIZEPREFIX, 0));
        expressions.push_back(
            expression = Expression::Create(
                expression,
                GetRegisterName(operand.reg, GetOperandByteSize(operand)), 0,
                Expression::TYPE_REGISTER, 0));
      } break;
      case o_mem:  // direct memory reference
      {
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
      } break;
      case o_phrase: {  // doesn't seem to exist for PPC, Python code doesn't
                        // handle this either
      } break;
      case o_displ: {
        const Name name =
            GetName(address, operand.addr, operand_position, false);
        expressions.push_back(expression = Expression::Create(
                                  expression,
                                  GetSizePrefix(GetOperandByteSize(operand)), 0,
                                  Expression::TYPE_SIZEPREFIX, 0));
        expressions.push_back(
            expression = Expression::Create(expression, "[", 0,
                                            Expression::TYPE_DEREFERENCE, 0));
        expressions.push_back(
            expression = Expression::Create(expression, "+", 0,
                                            Expression::TYPE_OPERATOR, 0));
        expressions.push_back(Expression::Create(
            expression,
            GetRegisterName(operand.reg, GetOperandByteSize(operand)), 0,
            Expression::TYPE_REGISTER, 0));
        expressions.push_back(Expression::Create(
            expression, name.name, operand.addr,
            name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type, 1));
      } break;
      case o_imm:  // immediate value
      {
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
      } break;
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
      } break;
      case o_idpspec0:  // Special purpose register in operand.value
      {
        expressions.push_back(expression = Expression::Create(
                                  expression,
                                  GetSizePrefix(GetOperandByteSize(operand)), 0,
                                  Expression::TYPE_SIZEPREFIX, 0));
        expressions.push_back(
            expression = Expression::Create(
                expression,
                GetSpecialPurposeRegisterName(address, operand.value).c_str(),
                0, Expression::TYPE_REGISTER, 0));
      } break;
      case o_idpspec1:  // two floating point registers, second in specflag1
      {
        expressions.push_back(expression = Expression::Create(
                                  expression,
                                  GetSizePrefix(GetOperandByteSize(operand)), 0,
                                  Expression::TYPE_SIZEPREFIX, 0));
        expressions.push_back(Expression::Create(
            expression,
            GetRegisterName(operand.reg, GetOperandByteSize(operand)), 0,
            Expression::TYPE_REGISTER, 0));
        // Note: IDA returns what is really two operands in a single op. That's
        // why we start a new one here
        operands.push_back(Operand::CreateOperand(expressions));
        expression = 0;
        expressions.clear();
        expressions.push_back(expression = Expression::Create(
                                  expression,
                                  GetSizePrefix(GetOperandByteSize(operand)), 0,
                                  Expression::TYPE_SIZEPREFIX, 0));
        expressions.push_back(Expression::Create(
            expression,
            GetRegisterName(operand.specflag1, GetOperandByteSize(operand)), 0,
            Expression::TYPE_REGISTER, 0));
      } break;
      case o_idpspec2: {
        // #define aux_sh   0x0020      // shift is present
        // #define aux_mb   0x0040      // mb is present
        // #define aux_me   0x0080      // me is present
        // #define o_shmbme o_idpspec2  // SH & MB & ME
        // #define op_sh    reg         // if aux_sh is set, otherwise here is a
        // register
        // #define op_mb    specflag1   // if aux_mb is set
        // #define op_me    specflag2   // if aux_me is set
        if (instruction.auxpref & PPC_aux_sh) {
          expressions.push_back(expression = Expression::Create(
                                    expression,
                                    GetSizePrefix(GetOperandByteSize(operand)),
                                    0, Expression::TYPE_SIZEPREFIX, 0));
          expressions.push_back(Expression::Create(
              expression, "", operand.reg, Expression::TYPE_IMMEDIATE_INT, 0));
        } else {
          expressions.push_back(expression = Expression::Create(
                                    expression,
                                    GetSizePrefix(GetOperandByteSize(operand)),
                                    0, Expression::TYPE_SIZEPREFIX, 0));
          expressions.push_back(Expression::Create(
              expression,
              GetRegisterName(operand.reg, GetOperandByteSize(operand)), 0,
              Expression::TYPE_REGISTER, 0));
        }
        if (instruction.auxpref & PPC_aux_mb) {
          operands.push_back(Operand::CreateOperand(expressions));
          expression = 0;
          expressions.clear();
          expressions.push_back(expression = Expression::Create(
                                    expression,
                                    GetSizePrefix(GetOperandByteSize(operand)),
                                    0, Expression::TYPE_SIZEPREFIX, 1));
          expressions.push_back(
              Expression::Create(expression, "", operand.specflag1,
                                 Expression::TYPE_IMMEDIATE_INT, 0));
        }
        if (instruction.auxpref & PPC_aux_me) {
          operands.push_back(Operand::CreateOperand(expressions));
          expression = 0;
          expressions.clear();
          expressions.push_back(expression = Expression::Create(
                                    expression,
                                    GetSizePrefix(GetOperandByteSize(operand)),
                                    0, Expression::TYPE_SIZEPREFIX, 2));
          expressions.push_back(
              Expression::Create(expression, "", operand.specflag2,
                                 Expression::TYPE_IMMEDIATE_INT, 0));
        }
      } break;
      case o_idpspec4:  // crbit        x.reg
      {  // @bug: IDA display this as 4*cr7+so but I just get a single value in
         // x.reg...
        expressions.push_back(expression = Expression::Create(
                                  expression,
                                  GetSizePrefix(GetOperandByteSize(operand)), 0,
                                  Expression::TYPE_SIZEPREFIX, 0));
        expressions.push_back(
            expression = Expression::Create(expression, "", operand.reg,
                                            Expression::TYPE_IMMEDIATE_INT, 0));
      } break;
      case o_idpspec5: {  // #define o_dcr   o_idpspec5              // Device
                          // control register
        // #define dcrnum  value                   // register number is kept
        // here
        expressions.push_back(expression = Expression::Create(
                                  expression,
                                  GetSizePrefix(GetOperandByteSize(operand)), 0,
                                  Expression::TYPE_SIZEPREFIX, 0));
        expressions.push_back(expression = Expression::Create(
                                  expression,
                                  GetDeviceControlRegisterName(operand.value),
                                  0, Expression::TYPE_REGISTER, 0));
      } break;
      default: {
        LOG(INFO) << StringPrintf(
            "warning: unknown operand type %d at %08" PRIx64,
            static_cast<int>(operand.type), address);
      } break;
    }

    operands.push_back(Operand::CreateOperand(expressions));
  }

  Operands(operands).swap(operands);
  return operands;
}

Instruction ParseInstructionIdaPpc(Address address, CallGraph* /* call_graph */,
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

  if (cmd.auxpref & PPC_aux_oe) mnemonic += "o";
  if (cmd.auxpref & PPC_aux_ctr) mnemonic += "ctr";
  if (cmd.auxpref & PPC_aux_rc) mnemonic += ".";
  if (cmd.auxpref & PPC_aux_lr) mnemonic += "lr";
  if (cmd.auxpref & PPC_aux_lk) mnemonic += "l";
  if (cmd.auxpref & PPC_aux_aa) mnemonic += "a";
  if (cmd.auxpref & PPC_aux_plus) mnemonic += "+";
  if (cmd.auxpref & PPC_aux_minus) mnemonic += "-";

  const Operands operands = DecodeOperandsPpc(mnemonic, address);
  return Instruction(address, next_instruction, cmd.size, mnemonic, operands);
}
