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

#include "third_party/zynamics/binexport/ida/dalvik.h"

#include <cinttypes>
#include <cstring>
#include <string>

// clang-format off
#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <frame.hpp>                                            // NOLINT
#include <funcs.hpp>                                            // NOLINT
#include <ida.hpp>                                              // NOLINT
#include <idp.hpp>                                              // NOLINT
#include <lines.hpp>                                            // NOLINT
#include <nalt.hpp>                                             // NOLINT
#include <segment.hpp>                                          // NOLINT
#include <strlist.hpp>                                          // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT
// clang-format on

#include "base/logging.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/binexport/ida/names.h"

namespace security::binexport {
namespace {

#define dex_o_reg o_reg
#define dex_o_imm o_imm
#define dex_o_target o_near
#define dex_o_string o_idpspec1
#define dex_o_type o_idpspec2
#define dex_o_field o_idpspec3
#define dex_o_meth o_idpspec4

#define dex_regpair specflag1   // Register pair, not a single register
#define dex_fieldoff specflag1  // Byte offset, not a field index
#define dex_vtindex specflag1   // vtable slot, not a method id
struct dex_header_item {
  uint8_t magic[8];
  uint32_t checksum;
  uint8_t signature[20];
  uint32_t file_size;
  uint32_t header_size;
  uint32_t endian_tag;
  uint32_t link_size;
  uint32_t link_off;
  uint32_t map_off;
  uint32_t string_ids_size;
  uint32_t string_ids_off;
  uint32_t type_ids_size;
  uint32_t type_ids_off;
  uint32_t proto_ids_size;
  uint32_t proto_ids_off;
  uint32_t field_ids_size;
  uint32_t field_ids_off;
  uint32_t method_ids_size_;
  uint32_t method_ids_off_;
  uint32_t class_defs_size_;
  uint32_t class_defs_off_;
  uint32_t data_size_;
  uint32_t data_off_;
};

// Returns the ULEB128 encoded value at the specified linear address.
//
// If bytes_read is non-zero, this function also returns the number of bytes
// read  into this output parameter.
uint32_t GetUleb128(Address ea, uint8_t* bytes_read = 0) {
  Address start_ea = ea;
  uint8_t cur_byte = get_byte(static_cast<ea_t>(ea++));
  uint32_t result = cur_byte;

  // Shortcut for small values
  if (cur_byte >= 0x80) {
    cur_byte = get_byte(static_cast<ea_t>(ea++));

    // Mask out highest bit of result, then OR with significant bits of
    // current byte
    result = (result & 0x7f) | ((cur_byte & 0x7f) << 7);

    if (cur_byte & 0x80) {
      cur_byte = get_byte(static_cast<ea_t>(ea++));
      result |= (cur_byte & 0x7f) << 14;
      if (cur_byte & 0x80) {
        cur_byte = get_byte(static_cast<ea_t>(ea++));
        result |= (cur_byte & 0x7f) << 21;
      }
      if (cur_byte & 0x80) {
        cur_byte = get_byte(static_cast<ea_t>(ea++));
        // Note: The original Dalvik implementation tolerates garbage
        //       in the highest four bits of the last byte.
        result |= (cur_byte & 0x7f) << 28;
      }
    }
  }
  if (bytes_read != 0) {
    *bytes_read = static_cast<uint8_t>(ea - start_ea);
  }
  return result;
}

// Returns the C-style string starting at the specified linear address.
std::string GetString(Address ea) {
  std::string result;
  result.reserve(16);

  for (;;) {
    uint8_t b(get_byte(static_cast<ea_t>(ea++)));
    if (b == 0) {
      break;
    }
    result.append(1, b);
  }
  return result;
}

std::string GetDexString(Address ea) {
  uint8_t len;
  GetUleb128(ea, &len);
  return GetString(ea + len);
}

dex_header_item& GetDexHeader() {
  static dex_header_item header;
  static bool header_init_done(false);
  if (!header_init_done) {
    memset(&header, 0, sizeof(dex_header_item));
    get_bytes(&header, sizeof(dex_header_item), header.link_off);
    header_init_done = true;
  }
  return header;
}

std::string GetTypeNameByIndex(size_t index) {
  dex_header_item& header(GetDexHeader());
  uint32_t type_idx(get_dword(header.type_ids_off + index * sizeof(uint32_t)));
  uint32_t str_off(
      get_dword(header.string_ids_off + type_idx * sizeof(uint32_t)));
  return GetDexString(str_off);
}

}  // namespace

Operands ParseOperandsIdaDalvik(const insn_t& instruction,
                                CallGraph* /* call_graph */,
                                FlowGraph* flow_graph) {
  Operands operands;
  for (uint8_t i = 0; i < UA_MAXOP && instruction.ops[i].type != o_void; ++i) {
    Expressions expressions;
    const op_t& operand(instruction.ops[i]);

    Expression* expr = nullptr;
    switch (operand.type) {
      case o_void:      // No operand
      case o_phrase:    // Not used with Dalvik
      case o_mem:       // Not used with Dalvik
      case o_displ:     // Not used with Dalvik
      case o_far:       // Not used with Dalvik
      case o_idpspec0:  // Not used with Dalvik
      case o_idpspec5:  // Not used with Dalvik
        break;

      case dex_o_reg: {
        // Get canonical register name for operand
        std::string regName(GetRegisterName(
            operand.reg, GetOperandByteSize(instruction, operand)));

        if (operand.dex_regpair) {
          // Register pairs encode 64-bit values
          expr =
              Expression::Create(expr, "b8", 0, Expression::TYPE_SIZEPREFIX, 0);
          expressions.push_back(expr);
          std::string regName2(GetRegisterName(
              operand.reg + 1, GetOperandByteSize(instruction, operand)));
          expr = Expression::Create(expr, regName + ":" + regName2, 0,
                                    Expression::TYPE_REGISTER, 0);
          expressions.push_back(expr);
        } else {
          // Regular registers are always 4 bytes on Dalvik
          expr =
              Expression::Create(expr, "b4", 0, Expression::TYPE_SIZEPREFIX, 0);
          expressions.push_back(expr);
          expr = Expression::Create(expr, regName, 0, Expression::TYPE_REGISTER,
                                    0);
          expressions.push_back(expr);
        }

        // If the register has been renamed, add an expression substitution
        regvar_t* rv(find_regvar(get_func(instruction.ea), instruction.ea,
                                 regName.c_str()));
        if (rv != 0)
          flow_graph->AddExpressionSubstitution(
              instruction.ea, static_cast<uint8_t>(i), expr->GetId(), rv->user);
        break;
      }
      case dex_o_imm: {
        expr = Expression::Create(
            expr, GetSizePrefix(GetOperandByteSize(instruction, operand)), 0,
            Expression::TYPE_SIZEPREFIX, 0);
        expressions.push_back(expr);

        const Address immediate(static_cast<Address>(operand.value));
        const Name name(GetName(instruction.ea, immediate, i, false));
        expr = Expression::Create(
            expr, name.name, immediate,
            name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type, 0);
        expressions.push_back(expr);

        break;
      }
      case dex_o_target: {
        const Address immediate(static_cast<Address>(operand.addr));
        Name name(GetName(instruction.ea, immediate, i, false));
        if (name.empty()) {
          LOG(INFO) << absl::StrCat(absl::Hex(instruction.ea, absl::kZeroPad8),
                                    ": dex_o_target: empty name");
          name.name = GetGlobalStructureName(instruction.ea, immediate, i);
          name.type = Expression::TYPE_GLOBALVARIABLE;
        }

        expr = Expression::Create(
            expr, GetSizePrefix(GetOperandByteSize(instruction, operand)), 0,
            Expression::TYPE_SIZEPREFIX, 0);
        expressions.push_back(expr);

        expr = Expression::Create(
            expr, name.name, immediate,
            name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type, 0);
        expressions.push_back(expr);

        break;
      }
      case dex_o_string: {
        // TODO(cblichmann) Check if we should use the size of a single
        //                  character (the type "pointed to").
        // TODO(cblichmann) Add string as a comment.
        expr = Expression::Create(
            expr, GetSizePrefix(GetOperandByteSize(instruction, operand)), 0,
            Expression::TYPE_SIZEPREFIX, 0);
        expressions.push_back(expr);

        expr =
            Expression::Create(expr, "[", 0, Expression::TYPE_DEREFERENCE, 0);
        expressions.push_back(expr);

        const Address immediate(static_cast<Address>(operand.addr));
        Name name(GetName(instruction.ea, immediate, i, false));
        expr = Expression::Create(
            expr, name.name, immediate,
            name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type, 0);
        expressions.push_back(expr);

        break;
      }
      case dex_o_type: {
        std::string str(GetTypeNameByIndex(static_cast<size_t>(operand.value)));

        expr = Expression::Create(
            expr, GetSizePrefix(GetOperandByteSize(instruction, operand)), 0,
            Expression::TYPE_SIZEPREFIX, 0);
        expressions.push_back(expr);

        const Address immediate(static_cast<Address>(operand.addr));
        expr = Expression::Create(expr, str, immediate, Expression::TYPE_SYMBOL,
                                  0);
        expressions.push_back(expr);

        break;
      }
      case dex_o_field: {
        expr = Expression::Create(
            expr, GetSizePrefix(GetOperandByteSize(instruction, operand)), 0,
            Expression::TYPE_SIZEPREFIX, 0);
        expressions.push_back(expr);

        const Address immediate(static_cast<Address>(operand.addr));
        Name name(GetName(instruction.ea, immediate, i, false));
        expr = Expression::Create(
            expr, name.name, immediate,
            name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type, 0);
        expressions.push_back(expr);

        break;
      }
      case dex_o_meth: {
        const Address immediate(static_cast<Address>(
            operand.addr != 0 ? operand.addr : operand.specval));
        // immediate == 0 => reference to imported method

        Name name(GetName(instruction.ea, immediate, i, false));
        expr = Expression::Create(
            expr, name.name, immediate,
            name.empty() ? Expression::TYPE_IMMEDIATE_INT : name.type, 0);
        expressions.push_back(expr);

        break;
      }
      default:
        LOG(INFO) << absl::StrCat("warning: unknown operand type ",
                                  operand.type, " at ",
                                  absl::Hex(instruction.ea, absl::kZeroPad8));
        break;
    }
    operands.push_back(Operand::CreateOperand(expressions));
  }

  // Shrink to fit
  Operands(operands).swap(operands);
  return operands;
}

Instruction ParseInstructionIdaDalvik(const insn_t& instruction,
                                      CallGraph* call_graph,
                                      FlowGraph* flow_graph, TypeSystem*) {
  // If the address contains no code of if the text representation could not
  // be generated, return an empty instruction. Do the same if the mnemonic
  // is empty.
  if (!IsCode(instruction.ea)) {
    return Instruction(instruction.ea);
  }
  std::string mnemonic = GetMnemonic(instruction.ea);
  if (mnemonic.empty()) {
    return Instruction(instruction.ea);
  }

  // Look for CODE xrefs that denote code flow and use its address as next
  // instruction. If the instruction does not flow (for example jumps), use
  // 0 as the address of the next address instruction.
  Address nextInstruction(0);
  xrefblk_t xref;
  bool haveXref(xref.first_from(static_cast<ea_t>(instruction.ea), XREF_ALL));
  while (haveXref && xref.iscode) {
    if (xref.type == fl_F) {
      nextInstruction = xref.to;
      break;
    }
    haveXref = xref.next_from();
  }

  return Instruction(
      instruction.ea, nextInstruction, instruction.size, mnemonic,
      ParseOperandsIdaDalvik(instruction, call_graph, flow_graph));
}

}  // namespace security::binexport
