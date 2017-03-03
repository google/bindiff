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

#include "third_party/zynamics/binexport/ida/generic.h"

#include <sstream>

#include "third_party/zynamics/binexport/ida/pro_forward.h"  // NOLINT
#include <bytes.hpp>                                         // NOLINT
#include <ida.hpp>                                           // NOLINT
#include <ua.hpp>                                            // NOLINT

#include "strings/strutil.h"
#include "third_party/zynamics/binexport/ida/names.h"
#include "third_party/zynamics/binexport/instruction.h"

Instruction ParseInstructionIdaGeneric(Address address,
                                       CallGraph* /* call_graph */,
                                       FlowGraph* /* flow_graph */,
                                       TypeSystem* /* type_system */) {
  if (!IsCode(address) || !decode_insn(static_cast<ea_t>(address))) {
    return Instruction(address);
  }

  enum { kBufferSize = 8192 };
  char buffer[kBufferSize] = {0};
  if (!generate_disasm_line(static_cast<ea_t>(address), buffer, kBufferSize)) {
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

  // parse mnemonic using the embedded tags
  const char* start = 0;
  for (const char* i = buffer; i != buffer + kBufferSize && *i; ++i) {
    if (*i == COLOR_INSN) {  // we are looking for a "\1\5" combination
      start = i + 1;
      break;
    }
  }
  const char* end = 0;
  for (const char* i = start; i && i != buffer + kBufferSize && *i; ++i) {
    if (*i == COLOR_INSN) {  // we are looking for a "\2\5" combination
      end = i - 1;
      break;
    }
  }
  if (!start || !end) {
    return Instruction(address);
  }

  const std::string mnemonic(start, end);

  // remove color tags and use the rest of the string as a single operand
  const size_t length = tag_remove(buffer, buffer, kBufferSize);
  buffer[length] = '\0';

  std::string operand(buffer + mnemonic.size(), length - mnemonic.size());
  StripWhitespace(&operand);

  Operands operands;
  Expressions expressions;
  if (!operand.empty()) {
    expressions.push_back(
        Expression::Create(0, operand, 0, Expression::TYPE_SYMBOL, 0));
    operands.push_back(Operand::CreateOperand(expressions));
  }
  return Instruction(address, next_instruction, cmd.size, mnemonic, operands);
}
