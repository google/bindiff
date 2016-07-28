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

// Dumps the disassembly from BinExport files.

#include <stdio.h>  // fileno()
#include <ctime>
#include <memory>
#include <string>

#include <binexport2.pb.h>  // NOLINT

#include "base/logging.h"
#include "strings/strutil.h"
#include "third_party/zynamics/binexport/types.h"

Address GetInstructionAddress(const BinExport2& proto, int index) {
  auto& instruction = proto.instruction(index);
  if (instruction.has_address()) {
    return instruction.address();
  }
  int delta = 0;
  for (--index; index >= 0; --index) {
    delta += proto.instruction(index).raw_bytes().size();
    if (proto.instruction(index).has_address()) {
      return proto.instruction(index).address() + delta;
    }
  }
  LOG(QFATAL) << "Invalid instruction index";
  return 0;  // Not reached.
}

void RenderExpression(const BinExport2& proto,
                      const BinExport2::Operand& operand, int index,
                      string* output) {
  const auto& expression = proto.expression(operand.expression_index(index));
  const auto& symbol = expression.symbol();
  switch (expression.type()) {
    case BinExport2::Expression::OPERATOR: {
      vector<int> children;
      children.reserve(4);  // Default maximum on x86
      for (int i = index + 1;
           i < operand.expression_index_size() &&
           proto.expression(operand.expression_index(i)).parent_index() ==
               operand.expression_index(index);
           i++) {
        children.push_back(operand.expression_index(i));
      }
      auto num_children = children.size();
      if (symbol == "{") {  // ARM Register lists
        StrAppend(output, "{");
        for (int i = 0; i < num_children; i++) {
          RenderExpression(proto, operand, index + 1 + i, output);
          if (i != num_children - 1) {
            StrAppend(output, ",");
          }
        }
        StrAppend(output, "}");
      } else if (num_children == 1) {
        // Only a single child, treat expression as prefix operator (like
        // 'ss:').
        StrAppend(output, symbol);
        RenderExpression(proto, operand, index + 1, output);
      } else if (num_children > 1) {
        // Multiple children, treat expression as infix operator ('+' or '*').
        // TODO(cblichmann): Deal with negative numbers.
        RenderExpression(proto, operand, index + 1, output);
        for (int i = 1; i < num_children; i++) {
          StrAppend(output, symbol);
          RenderExpression(proto, operand, index + 1 + i, output);
        }
      }
      break;
    }
    case BinExport2::Expression::SYMBOL:
    case BinExport2::Expression::REGISTER:
      StrAppend(output, symbol);
      break;
    case BinExport2::Expression::SIZE_PREFIX: {
      StringPiece architecture_name(
          proto.meta_information().architecture_name());
      bool long_mode = architecture_name.ends_with("64");
      if ((long_mode && symbol != "b8") || (!long_mode && symbol != "b4")) {
        StrAppend(output, symbol, " ");
      }
      RenderExpression(proto, operand, index + 1, output);
      break;
    }
    case BinExport2::Expression::DEREFERENCE:
      StrAppend(output, "[");
      if (index + 1 < operand.expression_index_size()) {
        RenderExpression(proto, operand, index + 1, output);
      }
      StrAppend(output, "]");
      break;
    case BinExport2::Expression::IMMEDIATE_INT:
    case BinExport2::Expression::IMMEDIATE_FLOAT:
    default:
      StrAppend(output, "0x", strings::Hex(expression.immediate()));
      break;
  }
}

void DumpBinExport2(const BinExport2& proto) {
  {
    const auto& meta = proto.meta_information();
    printf("Original executable name: %s\n", meta.executable_name().c_str());
    printf("Executable Id:            %s\n", meta.executable_id().c_str());
    printf("Architecture:             %s\n", meta.architecture_name().c_str());
    std::time_t timestamp = meta.timestamp();
    char formatted[100] = {0};
    CHECK(std::strftime(formatted, sizeof(formatted), "%Y-%m-%d %H:%M:%S",
                        std::localtime(&timestamp)));
    printf("Creation time (local):    %s\n", formatted);
  }

  const auto& call_graph = proto.call_graph();
  printf("\nFunctions:\n");
  constexpr const char kFunctionType[] = "nlit!";
  std::map<Address, string> function_names;
  for (int i = 0; i < call_graph.vertex_size(); ++i) {
    const auto& vertex = call_graph.vertex(i);
    const auto address = vertex.address();

    string name(vertex.demangled_name());
    if (name.empty()) {
      name = vertex.mangled_name();
    }
    if (name.empty()) {
      name = StrCat("sub_", strings::Hex(address, strings::ZERO_PAD_8));
    }
    string line(StrCat(strings::Hex(address, strings::ZERO_PAD_8), " ",
                       string(1, kFunctionType[vertex.type()]), " ", name));
    function_names[address] = name;
    printf("%s\n", line.c_str());
  }

  for (const auto& flow_graph : proto.flow_graph()) {
    auto function_address = GetInstructionAddress(
        proto, proto.basic_block(flow_graph.entry_basic_block_index())
                   .instruction_index(0)
                   .begin_index());
    string info(StrCat(strings::Hex(function_address, strings::ZERO_PAD_8),
                       " ; ", function_names[function_address]));
    printf("\n%s\n", info.c_str());

    Address computed_instruction_address = 0;
    int last_instruction_index = 0;
    for (const auto& basic_block_index : flow_graph.basic_block_index()) {
      const auto& basic_block = proto.basic_block(basic_block_index);
      QCHECK(basic_block.instruction_index_size());

      for (const auto& instruction_index_range :
           basic_block.instruction_index()) {
        Address basic_block_address = 0;
        const int begin_index = instruction_index_range.begin_index();
        const int end_index = instruction_index_range.has_end_index()
                                  ? instruction_index_range.end_index()
                                  : begin_index + 1;
        for (int i = begin_index; i < end_index; ++i) {
          const auto& instruction = proto.instruction(i);
          Address instruction_address = computed_instruction_address;
          if (last_instruction_index != i - 1 || instruction.has_address()) {
            instruction_address = GetInstructionAddress(proto, i);
          }

          if (i == begin_index) {
            basic_block_address = instruction_address;
            string line(
                StrCat(strings::Hex(basic_block_address, strings::ZERO_PAD_8),
                       " ; ----------------------------------------"));
            printf("%s\n", line.c_str());
          }

          string disassembly(
              StrCat("                ",
                     proto.mnemonic(instruction.mnemonic_index()).name(), " "));
          for (int i = 0; i < instruction.operand_index_size(); i++) {
            const auto& operand = proto.operand(instruction.operand_index(i));
            for (int j = 0; j < operand.expression_index_size(); j++) {
              const auto& expression =
                  proto.expression(operand.expression_index(j));
              if (!expression.has_parent_index()) {
                RenderExpression(proto, operand, j, &disassembly);
              }
            }
            if (i != instruction.operand_index_size() - 1) {
              StrAppend(&disassembly, ", ");
            }
          }

          string line(
              StrCat(strings::Hex(instruction_address, strings::ZERO_PAD_8),
                     " ", disassembly));
          printf("%s\n", line.c_str());

          const auto& raw_bytes = instruction.raw_bytes();
          computed_instruction_address = instruction_address + raw_bytes.size();
          last_instruction_index = i;
        }
      }
    }
  }
}

int main(int argc, char* argv[]) {
  if (argc != 2) {
    fprintf(stderr, "Usage: %s BINEXPORT2\n", argv[0]);
    return EXIT_FAILURE;
  }

  std::unique_ptr<FILE, int (*)(FILE*)> file(fopen(argv[1], "rb"), fclose);
  if (!file) {
    perror("could not open file");
    return EXIT_FAILURE;
  }

  BinExport2 proto;
  if (!proto.ParseFromFileDescriptor(fileno(file.get()))) {
    fprintf(stderr, "Failed to parse BinExport v2 data\n");
    return EXIT_FAILURE;
  }

  DumpBinExport2(proto);
  return EXIT_SUCCESS;
}

