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

// Dumps the disassembly from BinExport files.

#include <cstdio>  // fileno()
#include <ctime>
#include <memory>
#include <string>
#include <vector>

#include "third_party/absl/container/flat_hash_map.h"
#include "third_party/absl/log/check.h"
#include "third_party/absl/log/initialize.h"
#include "third_party/absl/log/log.h"
#include "third_party/absl/strings/match.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/strings/str_format.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/absl/time/time.h"
#include "third_party/zynamics/binexport/binexport.h"
#include "third_party/zynamics/binexport/binexport2.pb.h"
#include "third_party/zynamics/binexport/util/filesystem.h"
#include "third_party/zynamics/binexport/util/format.h"

namespace security::binexport {
namespace {

void RenderExpression(const BinExport2& proto,
                      const BinExport2::Operand& operand, int index,
                      std::string* output) {
  const int expression_index = operand.expression_index(index);
  const auto& expression = proto.expression(expression_index);
  const auto& expression_symbol = expression.symbol();
  const bool long_mode =
      absl::EndsWith(proto.meta_information().architecture_name(), "64");
  switch (expression.type()) {
    case BinExport2::Expression::OPERATOR: {
      std::vector<int> children;
      children.reserve(4);  // Default maximum on x86
      for (int i = index + 1;
           i < operand.expression_index_size() &&
           proto.expression(operand.expression_index(i)).parent_index() ==
               expression_index;
           ++i) {
        children.push_back(i);
      }
      auto num_children = children.size();
      if (expression_symbol == "{") {  // ARM Register lists
        absl::StrAppend(output, "{");
        for (int i = 0; i < num_children; ++i) {
          RenderExpression(proto, operand, children[i], output);
          if (i != num_children - 1) {
            absl::StrAppend(output, ",");
          }
        }
        absl::StrAppend(output, "}");
      } else if (num_children == 1) {
        // Only a single child, treat expression as prefix operator (like
        // 'ss:').
        absl::StrAppend(output, expression_symbol);
        RenderExpression(proto, operand, children[0], output);
      } else if (num_children > 1) {
        // Multiple children, treat expression as infix operator ('+' or '*').
        for (int i = 0; i < num_children; ++i) {
          RenderExpression(proto, operand, children[i], output);
          if (i != num_children - 1) {
            const auto& child_expression =
                proto.expression(operand.expression_index(children[i + 1]));
            const auto child_type = child_expression.type();
            if (expression_symbol == "+" &&
                (child_type == BinExport2::Expression::IMMEDIATE_INT ||
                 child_type == BinExport2::Expression::IMMEDIATE_FLOAT)) {
              const int64_t child_immediate =
                  long_mode
                      ? child_expression.immediate()
                      : static_cast<int32_t>(child_expression.immediate());
              if (child_immediate < 0 && child_expression.symbol().empty()) {
                continue;  // Don't render anything or we'll get: eax+-12
              }
              if (child_immediate == 0) {
                ++i;  // Skip "+0"
                continue;
              }
            }
            absl::StrAppend(output, expression_symbol);
          }
        }
      }
      break;
    }
    case BinExport2::Expression::SYMBOL:
    case BinExport2::Expression::REGISTER:
      absl::StrAppend(output, expression_symbol);
      break;
    case BinExport2::Expression::SIZE_PREFIX: {
      if ((long_mode && expression_symbol != "b8") ||
          (!long_mode && expression_symbol != "b4")) {
        absl::StrAppend(output, expression_symbol, " ");
      }
      RenderExpression(proto, operand, index + 1, output);
      break;
    }
    case BinExport2::Expression::DEREFERENCE:
      absl::StrAppend(output, "[");
      if (index + 1 < operand.expression_index_size()) {
        RenderExpression(proto, operand, index + 1, output);
      }
      absl::StrAppend(output, "]");
      break;
    case BinExport2::Expression::IMMEDIATE_INT:
    case BinExport2::Expression::IMMEDIATE_FLOAT:
      if (expression_symbol.empty()) {
        const int64_t expression_immediate =
            long_mode ? expression.immediate()
                      : static_cast<int32_t>(expression.immediate());
        if (expression_immediate <= 9) {
          absl::StrAppend(output, expression_immediate);
        } else {
          absl::StrAppend(output, "0x", absl::Hex(expression_immediate));
        }
      } else {
        absl::StrAppend(output, expression_symbol);
      }
      break;
  }
}

void DumpBinExport2(const BinExport2& proto) {
  {
    const auto& meta = proto.meta_information();
    absl::PrintF("Original executable name: %s\n", meta.executable_name());
    absl::PrintF("Executable Id:            %s\n", meta.executable_id());
    absl::PrintF("Architecture:             %s\n", meta.architecture_name());
    absl::PrintF("Creation time (local):    %s\n",
                 absl::FormatTime(absl::FromTimeT(meta.timestamp())));
  }

  // Read address comments.
  absl::flat_hash_map<int, int> comment_index_map;
  // NOLINTNEXTLINE(build/deprecated)
  for (const auto& reference : proto.address_comment()) {
    comment_index_map[reference.instruction_index()] =
        reference.string_table_index();
  }

  const auto& call_graph = proto.call_graph();
  absl::PrintF("\nFunctions:\n");
  constexpr const char kFunctionType[] = "nlit!";
  absl::flat_hash_map<Address, std::string> function_names;
  for (int i = 0; i < call_graph.vertex_size(); ++i) {
    const auto& vertex = call_graph.vertex(i);
    const auto address = vertex.address();

    auto name = vertex.demangled_name();
    if (name.empty()) {
      name = vertex.mangled_name();
    }
    if (name.empty()) {
      name = absl::StrCat("sub_", FormatAddress(address));
    }
    auto line =
        absl::StrCat(FormatAddress(address), " ",
                     std::string(1, kFunctionType[vertex.type()]), " ", name);
    function_names[address] = name;
    absl::PrintF("%s\n", line);
  }

  for (const auto& flow_graph : proto.flow_graph()) {
    auto function_address = GetInstructionAddress(
        proto, proto.basic_block(flow_graph.entry_basic_block_index())
                   .instruction_index(0)
                   .begin_index());
    const auto info = absl::StrCat(FormatAddress(function_address), " ; ",
                                   function_names[function_address]);
    absl::PrintF("\n%s\n", info);

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
            const auto line =
                absl::StrCat(FormatAddress(basic_block_address),
                             " ; ----------------------------------------");
            absl::PrintF("%s\n", line);
          }

          auto disassembly = absl::StrCat(
              "                ",
              proto.mnemonic(instruction.mnemonic_index()).name(), " ");
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
              absl::StrAppend(&disassembly, ", ");
            }
          }

          std::string line = absl::StrCat(FormatAddress(instruction_address),
                                          " ", disassembly);
          absl::PrintF("%s", line.c_str());
          if (instruction.comment_index_size() > 0) {
            const auto indent = line.size();
            for (int i = 0; i < instruction.comment_index_size(); ++i) {
              int comment_index = instruction.comment_index(i);
              const auto& proto_comment = proto.comment(comment_index);
              comment_index = proto_comment.string_table_index();
              absl::PrintF("%*s%s\n", (i > 0 ? indent : 0) + 3, " ; ",
                           proto.string_table(comment_index));
            }
          } else {
            absl::PrintF("\n");
          }

          const auto& raw_bytes = instruction.raw_bytes();
          computed_instruction_address = instruction_address + raw_bytes.size();
          last_instruction_index = i;
        }
      }
    }
  }
}

}  // namespace
}  // namespace security::binexport

int main(int argc, char* argv[]) {
  absl::InitializeLog();

  if (argc != 2) {
    absl::FPrintF(stderr, "Usage: %s BINEXPORT2\n", Basename(argv[0]));
    return EXIT_FAILURE;
  }

  std::unique_ptr<FILE, void (*)(FILE*)> file(fopen(argv[1], "rb"),
                                              [](FILE* fp) { fclose(fp); });
  if (!file) {
    perror("could not open file");
    return EXIT_FAILURE;
  }

  BinExport2 proto;
  if (!proto.ParseFromFileDescriptor(fileno(file.get()))) {
    absl::FPrintF(stderr, "Failed to parse BinExport v2 data\n");
    return EXIT_FAILURE;
  }

  security::binexport::DumpBinExport2(proto);
  return EXIT_SUCCESS;
}
