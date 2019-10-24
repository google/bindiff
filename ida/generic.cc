// Copyright 2011-2019 Google LLC. All Rights Reserved.
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

#include "third_party/zynamics/binexport/ida/generic.h"

#include <sstream>

// clang-format off
#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <bytes.hpp>                                            // NOLINT
#include <ida.hpp>                                              // NOLINT
#include <ua.hpp>                                               // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT
// clang-format on

#include "third_party/absl/strings/ascii.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/zynamics/binexport/ida/names.h"

namespace security::binexport {

Instruction ParseInstructionIdaGeneric(const insn_t& instruction,
                                       CallGraph* /* call_graph */,
                                       FlowGraph* /* flow_graph */,
                                       TypeSystem* /* type_system */) {
  if (!IsCode(instruction.ea)) {
    return Instruction(instruction.ea);
  }
  std::string mnemonic = GetMnemonic(instruction.ea);
  if (mnemonic.empty()) {
    return Instruction(instruction.ea);
  }
  std::string line;
  if (!GetOriginalIdaLine(instruction.ea, &line)) {
    return Instruction(instruction.ea);
  }

  Address next_instruction = 0;
  xrefblk_t xref;
  for (bool ok = xref.first_from(static_cast<ea_t>(instruction.ea), XREF_ALL);
       ok && xref.iscode; ok = xref.next_from()) {
    if (xref.type == fl_F) {
      next_instruction = xref.to;
      break;
    }
  }

  // Mnemonic may be padded with whitespace, so we strip the full prefix.
  std::string operand = line.substr(mnemonic.size());
  // Now get rid of surrounding whitespace.
  absl::StripAsciiWhitespace(&mnemonic);
  absl::StripAsciiWhitespace(&operand);

  Operands operands;
  Expressions expressions;
  if (!operand.empty()) {
    expressions.push_back(
        Expression::Create(0, operand, 0, Expression::TYPE_SYMBOL, 0));
    operands.push_back(Operand::CreateOperand(expressions));
  }
  return Instruction(instruction.ea, next_instruction, instruction.size,
                     mnemonic, operands);
}

}  // namespace security::binexport
