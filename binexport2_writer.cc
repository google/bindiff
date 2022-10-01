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

// New BinExport protocol buffer based file format. Should be more complete and
// more compact than the original one.
//
// Example rates for item
// 000026157fb0ada54135ef1f182585fc3edbca4769b9ea3629d6cda9161dc566:
// 1.2M Nov 15 16:37 000026.BinExport
// 199K Nov 15 16:37 000026.BinExport.gz
// 500K Nov 15 16:37 000026.BinExport2
// 107K Nov 15 16:37 000026.BinExport2.gz
//
// So the new format is smaller than the original by about a factor ~2, despite
// the fact that the original completely omitted operands! Both formats compress
// equally well by another factor of ~5.

#include "third_party/zynamics/binexport/binexport2_writer.h"

#include <algorithm>
#include <array>
#include <cinttypes>
#include <codecvt>
#include <fstream>
#include <locale>
#include <string>

#include "third_party/absl/container/flat_hash_map.h"
#include "third_party/absl/log/check.h"
#include "third_party/absl/log/log.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/absl/time/clock.h"
#include "third_party/absl/time/time.h"
#include "third_party/zynamics/binexport/binexport2.pb.h"
#include "third_party/zynamics/binexport/call_graph.h"
#include "third_party/zynamics/binexport/flow_graph.h"
#include "third_party/zynamics/binexport/function.h"
#include "third_party/zynamics/binexport/util/status_macros.h"

namespace security::binexport {
namespace {

// Sorts by descending occurrence count then by mnemonic string. Don't be
// confused by operator > - this function is called as an operator less-than.
bool SortMnemonicsByOccurrenceCount(
    const std::pair<std::string, int32_t>& one,
    const std::pair<std::string, int32_t>& two) {
  if (one.second != two.second) {
    return one.second > two.second;
  }
  return one.first < two.first;
}

// Sorts by mnemonic string.
bool SortMnemonicsAlphabetically(const std::pair<std::string, int32_t>& one,
                                 const std::pair<std::string, int32_t>& two) {
  return one.first < two.first;
}

// Stores unique mnemonics in result proto. Returns a vector of mnemonics sorted
// lexicographically for quick lookups. Every mnemonic maps to its index in the
// result proto array.
void WriteMnemonics(const Instructions& instructions,
                    std::vector<std::pair<std::string, int32_t>>* mnemonics,
                    BinExport2* proto) {
  // Get a histogram of mnemonics. Sort that histogram by descending occurrence
  // count. Store mnemonics in result proto buffer. Remember the indices
  // assigned in the proto array. Sort the index vector by mnemonic string to
  // allow for quick binary search by string and mapping from string to index.
  absl::flat_hash_map<std::string, int32_t> mnemonic_histogram;
  for (const auto& instruction : instructions) {
    if (instruction.HasFlag(FLAG_INVALID)) {
      continue;
    }
    ++mnemonic_histogram[instruction.GetMnemonic()];
  }
  mnemonics->reserve(mnemonic_histogram.size());
  for (auto& mnemonic : mnemonic_histogram) {
    mnemonics->push_back(std::move(mnemonic));
  }
  std::sort(mnemonics->begin(), mnemonics->end(),
            &SortMnemonicsByOccurrenceCount);
  proto->mutable_mnemonic()->Reserve(mnemonics->size());
  for (auto& mnemonic : *mnemonics) {
    mnemonic.second = proto->mnemonic_size();  // Remember current index.
    proto->add_mnemonic()->set_name(mnemonic.first);
  }
  std::sort(mnemonics->begin(), mnemonics->end(), &SortMnemonicsAlphabetically);
}

// Translates from the internal expression type to the expression type used by
// the BinExport2 proto format.
BinExport2::Expression::Type ExpressionTypeToProtoType(Expression::Type type) {
  switch (type) {
    case Expression::TYPE_SYMBOL:
      return BinExport2::Expression::SYMBOL;
    case Expression::TYPE_IMMEDIATE_INT:
      return BinExport2::Expression::IMMEDIATE_INT;
    case Expression::TYPE_IMMEDIATE_FLOAT:
      return BinExport2::Expression::IMMEDIATE_FLOAT;
    case Expression::TYPE_OPERATOR:
      return BinExport2::Expression::OPERATOR;
    case Expression::TYPE_REGISTER:
      return BinExport2::Expression::REGISTER;
    case Expression::TYPE_SIZEPREFIX:
      return BinExport2::Expression::SIZE_PREFIX;
    case Expression::TYPE_DEREFERENCE:
      return BinExport2::Expression::DEREFERENCE;

    // Only used by IDA, we map these to simple symbols.
    case Expression::TYPE_GLOBALVARIABLE:
    case Expression::TYPE_JUMPLABEL:
    case Expression::TYPE_STACKVARIABLE:
    case Expression::TYPE_FUNCTION:
      return BinExport2::Expression::SYMBOL;

    default:
      LOG(QFATAL) << "Invalid expression type: " << type;
      return BinExport2::Expression::IMMEDIATE_INT;  // Not reached
  }
}

bool SortExpressionsById(const Expression* one, const Expression* two) {
  return one->GetId() < two->GetId();
}

// Stores expression trees.
void WriteExpressions(BinExport2* proto) {
  std::vector<const Expression*> expressions;
  expressions.reserve(Expression::GetExpressions().size());
  for (const auto& expression_cache_entry : Expression::GetExpressions()) {
    expressions.push_back(&expression_cache_entry.second);
  }
  std::sort(expressions.begin(), expressions.end(), &SortExpressionsById);

  proto->mutable_expression()->Reserve(expressions.size());
  for (const Expression* expression : expressions) {
    // Proto expressions use a zero based index, C++ expressions are one based.
    DCHECK_EQ(expression->GetId() - 1, proto->expression_size());
    const auto& symbol = expression->GetSymbol();
    DCHECK(!symbol.empty() || expression->IsImmediate());
    BinExport2::Expression* proto_expression(proto->add_expression());
    if (!symbol.empty()) {
      proto_expression->set_symbol(symbol);
    }
    if (expression->GetParent() != nullptr) {
      proto_expression->set_parent_index(expression->GetParent()->GetId() - 1);
    }
    if (expression->IsImmediate()) {
      proto_expression->set_immediate(expression->GetImmediate());
    }
    if (expression->IsRelocation()) {
      proto_expression->set_is_relocation(true);
    }
    const auto type = ExpressionTypeToProtoType(expression->GetType());
    if (type != BinExport2::Expression::IMMEDIATE_INT) {
      // Only store if different from default value.
      proto_expression->set_type(type);
    }
  }
}

bool SortOperandsById(const Operand* one, const Operand* two) {
  return one->GetId() < two->GetId();
}

// Stores per operand expression trees.
void WriteOperands(BinExport2* proto) {
  std::vector<const Operand*> operands;
  operands.reserve(Operand::GetOperands().size());
  for (const auto& operand_cache_entry : Operand::GetOperands()) {
    operands.push_back(&operand_cache_entry.second);
  }
  std::sort(operands.begin(), operands.end(), &SortOperandsById);

  proto->mutable_operand()->Reserve(operands.size());
  for (const Operand* operand : operands) {
    // Proto expressions use a zero based index, C++ expressions are one based.
    QCHECK_EQ(operand->GetId() - 1, proto->operand_size());
    BinExport2::Operand* proto_operand(proto->add_operand());
    proto_operand->mutable_expression_index()->Reserve(
        operand->GetExpressionCount());
    const auto* previous_expression = *(operand->begin());
    for (const auto* expression : *operand) {
      QCHECK(expression->GetParent() != previous_expression->GetParent() ||
             expression->GetPosition() >= previous_expression->GetPosition());
      proto_operand->add_expression_index(expression->GetId() - 1);
      previous_expression = expression;
    }
  }
}

// Binary search for the given mnemonic. It is a fatal error to look for a
// mnemonic that is not actually contained in the set.
int32_t GetMnemonicIndex(
    const std::vector<std::pair<std::string, int32_t>>& mnemonics,
    const std::string& mnemonic) {
  const auto it = lower_bound(mnemonics.begin(), mnemonics.end(),
                              std::make_pair(mnemonic, 0));
  QCHECK(it != mnemonics.end()) << "Unknown mnemonic: " << mnemonic;
  QCHECK_EQ(mnemonic, it->first);
  return it->second;
}

// Find call targets for the given instruction and store them in the protocol
// buffer.
void WriteCallTargets(Address instruction_address,
                      const AddressReferences& address_references,
                      BinExport2::Instruction* proto_instruction) {
  const AddressReferences::const_iterator reference =
      lower_bound(address_references.begin(), address_references.end(),
                  AddressReference(instruction_address, std::make_pair(-1, -1),
                                   0, TYPE_CALL_DIRECT));
  for (AddressReferences::const_iterator i = reference;
       i != address_references.end() && i->source_ == instruction_address;
       ++i) {
    if (!i->IsCall()) {
      continue;
    }
    proto_instruction->add_call_target(i->target_);
  }
}

void WriteInstructions(
    const FlowGraph& flow_graph, const Instructions& instructions,
    const std::vector<std::pair<std::string, int32_t>>& mnemonics,
    const AddressReferences& address_references,
    std::vector<std::pair<Address, int32_t>>* instruction_indices,
    BinExport2* proto) {
  QCHECK(std::is_sorted(address_references.begin(), address_references.end()));
  proto->mutable_instruction()->Reserve(instructions.size());
  const Instruction* previous_instruction(nullptr);
  for (const Instruction& instruction : instructions) {
    if (instruction.HasFlag(FLAG_INVALID)) {
      previous_instruction = nullptr;
      continue;
    }
    instruction_indices->push_back(
        std::make_pair(instruction.GetAddress(), proto->instruction_size()));
    BinExport2::Instruction* proto_instruction(proto->add_instruction());
    QCHECK_EQ(instruction.GetSize(), instruction.GetBytes().size());
    // Write the full instruction address iff:
    // - there is no previous instruction
    // - the previous instruction doesn't have code flow into the current one
    // - the previous instruction overlaps the current one
    // - the current instruction is a function entry point
    if (previous_instruction == nullptr || !previous_instruction->IsFlow() ||
        previous_instruction->GetAddress() + previous_instruction->GetSize() !=
            instruction.GetAddress() ||
        flow_graph.GetFunction(instruction.GetAddress())) {
      proto_instruction->set_address(instruction.GetAddress());
    }
    proto_instruction->set_raw_bytes(instruction.GetBytes());
    if (const auto index =
            GetMnemonicIndex(mnemonics, instruction.GetMnemonic())) {
      // Only store if different from default value.
      proto_instruction->set_mnemonic_index(index);
    }
    proto_instruction->mutable_operand_index()->Reserve(
        instruction.GetOperandCount());
    for (const auto* operand : instruction) {
      QCHECK_GT(operand->GetId(), 0);
      proto_instruction->add_operand_index(operand->GetId() - 1);
    }
    WriteCallTargets(instruction.GetAddress(), address_references,
                     proto_instruction);
    previous_instruction = &instruction;
  }
  std::sort(instruction_indices->begin(), instruction_indices->end());
}

void WriteBasicBlocks(
    const Instructions& instructions,
    const std::vector<std::pair<Address, int32_t>>& instruction_indices,
    BinExport2* proto) {
  CHECK((instruction_indices.empty() && BasicBlock::blocks().empty()) ||
        (!instruction_indices.empty() && !BasicBlock::blocks().empty()));
  proto->mutable_basic_block()->Reserve(BasicBlock::blocks().size());
  auto instruction_index_it = instruction_indices.begin();
  int id = 0;
  for (auto& basic_block : BasicBlock::blocks()) {
    // Normally, cache elements should not be modified as changing the objects
    // might change their ordering. However, we are only modifying id here
    // which doesn't affect the order.
    BinExport2::BasicBlock proto_basic_block;
    bool basic_block_is_invalid = false;
    int begin_index = -1, end_index = -1;
    for (const auto& instruction : *basic_block.second) {
      // The whole basic block is invalid if it contains a single invalid
      // instruction.
      if (instruction.HasFlag(FLAG_INVALID)) {
        basic_block_is_invalid = true;
        break;
      }
      if (instruction_index_it == instruction_indices.end() ||
          instruction.GetAddress() != instruction_index_it->first) {
        instruction_index_it =
            lower_bound(instruction_indices.begin(), instruction_indices.end(),
                        std::make_pair(instruction.GetAddress(), 0));
      }
      QCHECK(instruction_index_it != instruction_indices.end());
      QCHECK_EQ(instruction_index_it->first, instruction.GetAddress());
      const int instruction_index = instruction_index_it->second;
      ++instruction_index_it;

      if (begin_index < 0) {
        begin_index = instruction_index;
        end_index = begin_index + 1;
      } else if (instruction_index != end_index) {
        // Sequence is broken, store an interval.
        BinExport2::BasicBlock::IndexRange* index_range(
            proto_basic_block.add_instruction_index());
        index_range->set_begin_index(begin_index);
        if (end_index != begin_index + 1) {
          // We omit the end index in the single instruction interval case.
          index_range->set_end_index(end_index);
        }
        begin_index = instruction_index;
        end_index = begin_index + 1;
      } else {
        // Sequence is unbroken, remember end_index.
        end_index = instruction_index + 1;
      }
    }
    BinExport2::BasicBlock::IndexRange* index_range(
        proto_basic_block.add_instruction_index());
    index_range->set_begin_index(begin_index);
    if (end_index != begin_index + 1) {
      // We omit the end index in the single instruction interval case.
      index_range->set_end_index(end_index);
    }
    if (!basic_block_is_invalid) {
      basic_block.second->set_id(id++);
      *proto->add_basic_block() = proto_basic_block;
    }
  }
}

// Translates from the internal flow graph edge type to the edge type used by
// the protocol buffer.
BinExport2::FlowGraph::Edge::Type FlowGraphEdgeTypeToProtoType(
    FlowGraphEdge::Type type) {
  switch (type) {
    case FlowGraphEdge::TYPE_TRUE:
      return BinExport2::FlowGraph::Edge::CONDITION_TRUE;
    case FlowGraphEdge::TYPE_FALSE:
      return BinExport2::FlowGraph::Edge::CONDITION_FALSE;
    case FlowGraphEdge::TYPE_UNCONDITIONAL:
      return BinExport2::FlowGraph::Edge::UNCONDITIONAL;
    case FlowGraphEdge::TYPE_SWITCH:
      return BinExport2::FlowGraph::Edge::SWITCH;
    default:
      LOG(QFATAL) << "Invalid flow graph edge type: " << type;
      return BinExport2::FlowGraph::Edge::UNCONDITIONAL;  // Not reached
  }
}

void WriteFlowGraphs(const FlowGraph& flow_graph, BinExport2* proto) {
  proto->mutable_flow_graph()->Reserve(flow_graph.GetFunctions().size());
  for (const auto& address_to_function : flow_graph.GetFunctions()) {
    const Function& function = *address_to_function.second;
    if (function.GetBasicBlocks().empty() ||
        function.GetType(true /* raw type */) == Function::TYPE_INVALID) {
      continue;  // Skip empty flow graphs, they only exist as call graph nodes.
    }

    BinExport2::FlowGraph* proto_flow_graph = proto->add_flow_graph();
    proto_flow_graph->mutable_basic_block_index()->Reserve(
        function.GetBasicBlocks().size());
    for (const BasicBlock* basic_block : function.GetBasicBlocks()) {
      if (basic_block->GetEntryPoint() == function.GetEntryPoint()) {
        proto_flow_graph->set_entry_basic_block_index(basic_block->id());
      }
      proto_flow_graph->add_basic_block_index(basic_block->id());
    }
    QCHECK_GE(proto_flow_graph->entry_basic_block_index(), 0);
    QCHECK_EQ(proto_flow_graph->basic_block_index_size(),
              function.GetBasicBlocks().size());

    std::vector<Function::Edges::const_iterator> back_edges;
    function.GetBackEdges(&back_edges);
    auto back_edge = back_edges.begin();
    proto_flow_graph->mutable_edge()->Reserve(function.GetEdges().size());
    for (const FlowGraphEdge& edge : function.GetEdges()) {
      BinExport2::FlowGraph::Edge* proto_edge = proto_flow_graph->add_edge();
      const BasicBlock* source = function.GetBasicBlockForAddress(edge.source);
      CHECK(source != nullptr);
      const BasicBlock* target = function.GetBasicBlockForAddress(edge.target);
      CHECK(target != nullptr);
      proto_edge->set_source_basic_block_index(source->id());
      proto_edge->set_target_basic_block_index(target->id());

      const auto type = FlowGraphEdgeTypeToProtoType(edge.type);
      if (type != BinExport2::FlowGraph::Edge::UNCONDITIONAL) {
        // Only store if different from default value.
        proto_edge->set_type(type);
      }

      // Advance the back edge iterator. Note that back edges and regular edges
      // are sorted the same way, so we can iterate through the vectors in lock
      // step.
      for (; back_edge != back_edges.end() &&
             (*back_edge)->source < edge.source &&
             (*back_edge)->target < edge.target;
           ++back_edge) {
      }
      if (back_edge != back_edges.end() &&
          (*back_edge)->source == edge.source &&
          (*back_edge)->target == edge.target) {
        proto_edge->set_is_back_edge(true);
        ++back_edge;
      }
    }
  }
}

// Translates from BinDetego internal call graph function type to the function
// type used by the protocol buffer.
BinExport2::CallGraph::Vertex::Type CallGraphVertexTypeToProtoType(
    Function::FunctionType type) {
  switch (type) {
    case Function::TYPE_STANDARD:
      return BinExport2::CallGraph::Vertex::NORMAL;
    case Function::TYPE_LIBRARY:
      return BinExport2::CallGraph::Vertex::LIBRARY;
    case Function::TYPE_IMPORTED:
      return BinExport2::CallGraph::Vertex::IMPORTED;
    case Function::TYPE_THUNK:
      return BinExport2::CallGraph::Vertex::THUNK;
    case Function::TYPE_INVALID:
      return BinExport2::CallGraph::Vertex::INVALID;
    default:
      LOG(QFATAL) << "Invalid call graph vertex type: " << type;
      return BinExport2::CallGraph::Vertex::NORMAL;  // Not reached
  }
}

// Used for binary searching the call graph vertex array for a particular
// function.
bool SortByAddress(const BinExport2::CallGraph::Vertex& one,
                   const BinExport2::CallGraph::Vertex& two) {
  return one.address() < two.address();
}

// Functions in the original call_graph are sorted by address and added
// sequentially to the protocol buffer. Hence we can binary search for a
// particular address.
// It is a fatal error to look for an address that is not actually contained in
// the graph.
int32_t GetVertexIndex(const BinExport2::CallGraph& call_graph,
                       uint64_t address) {
  BinExport2::CallGraph::Vertex vertex;
  vertex.set_address(address);
  const auto& it =
      std::lower_bound(call_graph.vertex().begin(), call_graph.vertex().end(),
                       vertex, &SortByAddress);
  QCHECK(it != call_graph.vertex().end())
      << "Can't find a call graph node for: "
      << absl::StrCat(absl::Hex(address, absl::kZeroPad8));
  QCHECK_EQ(address, it->address())
      << "Can't find a call graph node for: "
      << absl::StrCat(absl::Hex(address, absl::kZeroPad8));
  return it - call_graph.vertex().begin();
}

void WriteCallGraph(const CallGraph& call_graph, const FlowGraph& flow_graph,
                    BinExport2* proto) {
  BinExport2::CallGraph* proto_call_graph(proto->mutable_call_graph());
  proto_call_graph->mutable_vertex()->Reserve(flow_graph.GetFunctions().size());
  // Create used libraries list.
  std::vector<const LibraryManager::LibraryRecord*> used_libraries;
  call_graph.GetLibraryManager().GetUsedLibraries(&used_libraries);
  absl::flat_hash_map<int, int> use_index;
  for (int i = 0; i < used_libraries.size(); ++i) {
    use_index[used_libraries[i]->library_index] = i;
  }

  // Used for verifying that functions are sorted by address.
  uint64_t previous_entry_point_address = 0;
  absl::flat_hash_map<std::string, int32_t> module_index;
  for (const auto& function_it : flow_graph.GetFunctions()) {
    const Function& function(*function_it.second);
    QCHECK_GE(function.GetEntryPoint(), previous_entry_point_address);
    previous_entry_point_address = function.GetEntryPoint();
    QCHECK(call_graph.GetFunctions().find(function.GetEntryPoint()) !=
           call_graph.GetFunctions().end());
    BinExport2::CallGraph::Vertex* proto_function(
        proto_call_graph->add_vertex());
    proto_function->set_address(function.GetEntryPoint());
    const auto vertex_type =
        CallGraphVertexTypeToProtoType(function.GetType(false));
    if (vertex_type != BinExport2::CallGraph::Vertex::NORMAL) {
      // Only store if different from default value.
      proto_function->set_type(vertex_type);
    }
    if (function.HasRealName()) {
      proto_function->set_mangled_name(function.GetName(Function::MANGLED));
      if (function.GetName(Function::DEMANGLED) !=
          function.GetName(Function::MANGLED)) {
        proto_function->set_demangled_name(
            function.GetName(Function::DEMANGLED));
      }
    }
    int library_index = function.GetLibraryIndex();
    if (library_index != -1) {
      // We serialize use index, not library index (as the latter refers to the
      // array of all known libraries).
      proto_function->set_library_index(use_index[library_index]);
    }
    const std::string& module = function.GetModuleName();
    if (!module.empty()) {
      auto it = module_index.emplace(module, module_index.size());
      proto_function->set_module_index(it.first->second);
    }
  }

  if (!module_index.empty()) {
    proto->mutable_module()->Reserve(module_index.size());
    // We are O(N^2) here by number of classes, shouldn't be a big deal.
    for (int i = 0; i < module_index.size(); ++i) {
      auto* module = proto->add_module();
      module->set_name(
          std::find_if(module_index.begin(), module_index.end(),
                       [i](const std::pair<std::string, int32_t>& kv) -> bool {
                         return kv.second == i;
                       })
              ->first);
    }
  }

  proto_call_graph->mutable_edge()->Reserve(call_graph.GetEdges().size());
  for (const EdgeInfo& edge : call_graph.GetEdges()) {
    BinExport2::CallGraph::Edge* proto_edge(proto_call_graph->add_edge());
    CHECK(edge.function_ != nullptr);
    const uint64_t source_address(edge.function_->GetEntryPoint());
    const uint64_t target_address(edge.target_);
    proto_edge->set_source_vertex_index(
        GetVertexIndex(*proto_call_graph, source_address));
    proto_edge->set_target_vertex_index(
        GetVertexIndex(*proto_call_graph, target_address));
  }

  proto->mutable_library()->Reserve(used_libraries.size());
  for (const auto* used : used_libraries) {
    auto* library = proto->add_library();
    library->set_name(used->name);
    library->set_is_static(used->IsStatic());
  }
}

void WriteStrings(
    const AddressReferences& address_references,
    const AddressSpace& address_space,
    const std::vector<std::pair<Address, int32_t>>& instruction_indices,
    BinExport2* proto) {
  absl::flat_hash_map<std::string, int> string_to_string_index;
  for (const auto& reference : address_references) {
    if (reference.kind_ != TYPE_DATA_STRING &&
        reference.kind_ != TYPE_DATA_WIDE_STRING) {
      continue;
    }
    // String length must be > 0.
    if (reference.size_ == 0) {
      continue;
    }
    const auto instruction =
        lower_bound(instruction_indices.begin(), instruction_indices.end(),
                    std::make_pair(reference.source_, 0));
    // Only add strings and string references if there is an instruction
    // actually referencing the string.
    if (instruction == instruction_indices.end() ||
        instruction->first != reference.source_) {
      continue;
    }

    std::string content;
    // In case of block_size_left > reference.size_ we should probably check if
    // the next memory block can be joined with the current one and provide some
    // solution to read cross-block strings.
    const auto block = address_space.GetMemoryBlock(reference.target_);
    if (block == address_space.data().end()) {
      continue;
    }
    const Address block_address = reference.target_ - block->first;
    const int block_size_left = block->second.size() - block_address;

    if (reference.kind_ == TYPE_DATA_STRING) {
      content = std::string(
          reinterpret_cast<const char*>(&address_space[reference.target_]),
          std::min(reference.size_, block_size_left));

      // Replace control characters and everything above 0x7f with a plain
      // white-space. This is for compatibility with the Google version of
      // BinExport.
      constexpr std::array<uint8_t, 256> kReplaceInvalid = {
          1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1,
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1,
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      };
      for (auto& c : content) {
        if (kReplaceInvalid[c]) {
          c = ' ';
        }
      }
    } else {
      std::u16string wide_source(
          reinterpret_cast<const char16_t*>(&address_space[reference.target_]),
          std::min(reference.size_, block_size_left) / sizeof(char16_t));
      std::wstring_convert<std::codecvt_utf8_utf16<char16_t>, char16_t> convert;
      content = convert.to_bytes(wide_source);
    }

    auto it =
        string_to_string_index.try_emplace(content, proto->string_table_size());
    // Deduplicate strings.
    if (it.second != false) {
      proto->add_string_table(it.first->first);
    }
    auto* proto_string_reference = proto->add_string_reference();
    proto_string_reference->set_instruction_index(instruction->second);
    proto_string_reference->set_instruction_operand_index(
        reference.source_operand_);
    proto_string_reference->set_operand_expression_index(
        reference.source_expression_);
    proto_string_reference->set_string_table_index(it.first->second);
  }
}

void WriteDataReferences(
    const AddressReferences& address_references,
    const AddressSpace& address_space,
    const std::vector<std::pair<Address, int32_t>>& instruction_indices,
    BinExport2* proto) {
  // Cache address -> instruction mapping.
  absl::flat_hash_map<Address, int32_t> address_to_index;
  for (const auto& index : instruction_indices) {
    address_to_index[index.first] = index.second;
  }
  for (const auto& reference : address_references) {
    if (reference.kind_ != TYPE_DATA) {
      continue;
    }
    // Invalid reference.
    if (reference.target_ == 0) {
      continue;
    }
    const auto instruction = address_to_index.find(reference.source_);
    // Only add data references if there is a referring instruction.
    if (instruction == address_to_index.end()) {
      continue;
    }
    if (address_space.IsValidAddress(reference.target_)) {
      auto* proto_data_reference = proto->add_data_reference();
      proto_data_reference->set_instruction_index(instruction->second);
      proto_data_reference->set_address(reference.target_);
    }
  }
}

// Translates from the internal comment type to the one used by the BinExport2
// proto.
BinExport2::Comment::Type CommentTypeToProtoType(Comment::Type type) {
  switch (type) {
    case Comment::REGULAR:
      return BinExport2::Comment::DEFAULT;
    case Comment::ENUM:
      return BinExport2::Comment::ENUM;
    case Comment::LOCATION:
      return BinExport2::Comment::LOCATION;
    case Comment::GLOBAL_REFERENCE:
      return BinExport2::Comment::GLOBAL_REFERENCE;
    case Comment::LOCAL_REFERENCE:
      return BinExport2::Comment::LOCAL_REFERENCE;
    case Comment::STRUCTURE:
      // Not currently exported
      return BinExport2::Comment::DEFAULT;
    case Comment::ANTERIOR:
      return BinExport2::Comment::ANTERIOR;
    case Comment::POSTERIOR:
      return BinExport2::Comment::POSTERIOR;
    case Comment::FUNCTION:
      return BinExport2::Comment::FUNCTION;
    default:
      LOG(QFATAL) << "Invalid comment type: " << type;
      return BinExport2::Comment::DEFAULT;  // Not reached
  }
}

void WriteComments(
    const CallGraph& call_graph,
    const std::vector<std::pair<Address, int32_t>>& instruction_indices,
    BinExport2* proto) {
  absl::flat_hash_map<const std::string*, int> comment_to_index;
  for (const Comment& comment : call_graph.GetComments()) {
    const auto [new_comment_it, inserted] =
        comment_to_index.emplace(comment.comment, proto->string_table_size());
    if (inserted) {
      proto->add_string_table(*comment.comment);
    }
    const auto instruction_it =
        lower_bound(instruction_indices.begin(), instruction_indices.end(),
                    std::make_pair(comment.address, 0));
    QCHECK(instruction_it != instruction_indices.end());

    const Address instruction_index = instruction_it->second;
    const int string_table_index = new_comment_it->second;

    // Write rich comment structure for BinDiff.
    const int comment_index = proto->comment_size();
    auto* proto_comment = proto->add_comment();
    proto_comment->set_instruction_index(instruction_index);
    proto_comment->set_string_table_index(string_table_index);
    proto_comment->set_type(CommentTypeToProtoType(comment.type));
    proto_comment->set_repeatable(comment.repeatable);

    // The IDA specific code encodes the comment type in the operand number.
    // That's because BinDiff internally adds all comments into a global cache
    // keyed by a (address, operand_id)-tuple. So all comments for an address
    // need a unique operand_id.
    // Do not leak this implementation detail into the written protobuf.
    constexpr int kMaxOp = 8;  // Same as IDA's UA_MAXOP
    int operand_num = comment.operand_num;
    if (operand_num >= kMaxOp) {
      if (comment.type == Comment::GLOBAL_REFERENCE) {
        operand_num -= 1024;  // See ida/names.cc GetGlobalReferences()
      } else if (comment.type == Comment::LOCAL_REFERENCE) {
        operand_num -= 2048;  // See ida/names.cc GetLocalReferences()
      }
    }
    // Operand numbers only need to be written if the comment applies to an
    // actual operand.
    if (operand_num >= 0 && operand_num < kMaxOp) {
      proto_comment->set_instruction_operand_index(operand_num);
    }

    // Add back reference to comment to instruction
    proto->mutable_instruction(instruction_index)
        ->add_comment_index(comment_index);
  }
}

void WriteSections(const AddressSpace& address_space, BinExport2* proto) {
  for (const auto& data : address_space.data()) {
    auto* section = proto->add_section();
    section->set_address(data.first);
    section->set_size(data.second.size());
    section->set_flag_r(address_space.IsReadable(data.first));
    section->set_flag_w(address_space.IsWritable(data.first));
    section->set_flag_x(address_space.IsExecutable(data.first));
  }
}

// Writes a binary protocol buffer to the specified filename.
absl::Status WriteProtoToFile(const std::string& filename, BinExport2* proto) {
  std::ofstream stream(filename, std::ios::binary | std::ios::out);
  if (!proto->SerializeToOstream(&stream)) {
    return absl::UnknownError(
        absl::StrCat("error serializing data to: '", filename, ""));
  }
  return absl::OkStatus();
}
}  // namespace

BinExport2Writer::BinExport2Writer(const std::string& result_filename,
                                   const std::string& executable_filename,
                                   const std::string& executable_hash,
                                   const std::string& architecture)
    : filename_(result_filename),
      executable_filename_(executable_filename),
      executable_hash_(executable_hash),
      architecture_(architecture) {}

absl::Status BinExport2Writer::WriteToProto(
    const CallGraph& call_graph, const FlowGraph& flow_graph,
    const Instructions& instructions,
    const AddressReferences& address_references,
    const AddressSpace& address_space, BinExport2* proto) const {
  auto* meta_information = proto->mutable_meta_information();
  meta_information->set_executable_name(executable_filename_);
  meta_information->set_executable_id(executable_hash_);
  meta_information->set_architecture_name(architecture_);
  meta_information->set_timestamp(absl::ToUnixSeconds(absl::Now()));

  WriteExpressions(proto);
  WriteOperands(proto);
  {
    std::vector<std::pair<std::string, int32_t>> mnemonics;
    WriteMnemonics(instructions, &mnemonics, proto);
    std::vector<std::pair<Address, int32_t>> instruction_indices;
    WriteInstructions(flow_graph, instructions, mnemonics, address_references,
                      &instruction_indices, proto);
    WriteBasicBlocks(instructions, instruction_indices, proto);
    WriteComments(call_graph, instruction_indices, proto);
    WriteStrings(address_references, address_space, instruction_indices, proto);
    // TODO(cblichmann): Write expression_substitution.
    WriteDataReferences(address_references, address_space, instruction_indices,
                        proto);
  }
  WriteFlowGraphs(flow_graph, proto);
  WriteCallGraph(call_graph, flow_graph, proto);
  WriteSections(address_space, proto);

  return absl::OkStatus();
}

absl::Status BinExport2Writer::Write(
    const CallGraph& call_graph, const FlowGraph& flow_graph,
    const Instructions& instructions,
    const AddressReferences& address_references,
    const AddressSpace& address_space) {
  LOG(INFO) << "Writing to: \"" << filename_ << "\".";

  BinExport2 proto;
  NA_RETURN_IF_ERROR(WriteToProto(call_graph, flow_graph, instructions,
                                  address_references, address_space, &proto));
  return WriteProtoToFile(filename_, &proto);
}

}  // namespace security::binexport
