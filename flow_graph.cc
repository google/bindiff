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

#include "third_party/zynamics/binexport/flow_graph.h"

#include <algorithm>
#include <cinttypes>
#include <iterator>
#include <list>
#include <stack>
#include <unordered_map>

#include "base/logging.h"
#include "third_party/absl/container/flat_hash_set.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/binexport/call_graph.h"
#include "third_party/zynamics/binexport/comment.h"
#include "third_party/zynamics/binexport/virtual_memory.h"

FlowGraph::~FlowGraph() {
  for (auto function : functions_) {
    delete function.second;
  }
}

void FlowGraph::Render(std::ostream* stream,
                       const CallGraph& call_graph) const {
  for (const auto& function : functions_) {
    function.second->Render(stream, call_graph, *this);
    *stream << std::endl;
  }
}

void FlowGraph::AddEdge(const FlowGraphEdge& edge) { edges_.push_back(edge); }

const FlowGraph::Edges& FlowGraph::GetEdges() const { return edges_; }

const Function* FlowGraph::GetFunction(Address address) const {
  Functions::const_iterator i = functions_.find(address);
  return i != functions_.end() ? i->second : 0;
}

Function* FlowGraph::GetFunction(Address address) {
  Functions::iterator i = functions_.find(address);
  return i != functions_.end() ? i->second : 0;
}

const Functions& FlowGraph::GetFunctions() const { return functions_; }

Functions& FlowGraph::GetFunctions() { return functions_; }

// Sets FLAG_INVALID on all instructions that are no longer referenced by any
// basic block. Note that we cannot easily delete instructions from the vector
// as they are stored by value and others are pointing to it.
void FlowGraph::MarkOrphanInstructions(Instructions* instructions) const {
  // First: Mark all instructions as invalid.
  for (auto& instruction : *instructions) {
    instruction.SetFlag(FLAG_INVALID, true);
  }
  // Second: Mark instructions that are still being referenced as valid.
  for (auto& function : functions_) {
    for (auto* basic_block : function.second->GetBasicBlocks()) {
      for (auto& instruction : *basic_block) {
        if (instruction.GetMnemonic().empty()) {
          LOG(WARNING) << absl::StrCat(
              absl::Hex(instruction.GetAddress(), absl::kZeroPad8),
              " is reachable from function ",
              absl::Hex(function.second->GetEntryPoint(), absl::kZeroPad8),
              " basic block ",
              absl::Hex(basic_block->GetEntryPoint(), absl::kZeroPad8),
              " but invalid!");
          continue;
        }
        instruction.SetFlag(FLAG_INVALID, false);
      }
    }
  }
}

void FlowGraph::AddExpressionSubstitution(Address address, uint8_t operator_num,
                                          int expression_id,
                                          const std::string& substitution) {
  substitutions_[std::make_tuple(address, operator_num, expression_id)] =
      &*string_cache_.insert(substitution).first;
}

const FlowGraph::Substitutions& FlowGraph::GetSubstitutions() const {
  return substitutions_;
}

// Returns a vector of instruction addresses that start new basic blocks. This
// deals with cases where the end of a basic block is induced from outside the
// basic block, i.e. branches or calls into the instruction sequence.
std::vector<Address> FlowGraph::FindBasicBlockBreaks(
    detego::Instructions* instructions, CallGraph* call_graph) {
  // Find non-returning calls. We simply assume any call followed by an invalid
  // instruction or a multi-byte padding (nop) instruction to be non-returning.
  // For a rationale for the nop instruction, please refer to b/24084521.
  // TODO(soerenme) Mark the target function as non-returning and use that
  // information for all future calls to the function, even if those calls are
  // not followed by an invalid or nop instruction. This is a bit dangerous, as
  // it introduces an order dependency on the disassembly - do we already know
  // the function is non-returning by the time we find a call to it or not?
  for (auto call_instruction = instructions->begin();
       call_instruction != instructions->end(); ++call_instruction) {
    if (!call_instruction->HasFlag(FLAG_CALL)) {
      continue;
    }
    int num_nop_bytes = 0;
    auto last_nop = instructions->end();
    for (auto instruction = GetNextInstruction(instructions, call_instruction);
         instruction != instructions->end();
         instruction = GetNextInstruction(instructions, instruction)) {
      if (instruction->HasFlag(FLAG_INVALID)) {
        call_instruction->SetFlag(FLAG_FLOW, false);
        break;
      }
      if (!instruction->HasFlag(FLAG_NOP)) {
        break;
      }
      num_nop_bytes += instruction->GetSize();
      last_nop = instruction;
    }
    if (num_nop_bytes > 1) {
      // A single byte nop may or may not indicate a non-returning call.
      // http://reverseengineering.stackexchange.com/questions/8030/purpose-of-nop-immediately-after-call-instruction
      call_instruction->SetFlag(FLAG_FLOW, false);

      // If the last nop flows into an instruction with an in-degree of 1 it is
      // the only incoming code flow and a function should be created.
      auto potential_entry_point = GetNextInstruction(instructions, last_nop);
      if (potential_entry_point->GetInDegree() == 1) {
        call_graph->AddFunction(potential_entry_point->GetAddress());
      }
    }
  }

  // Addresses where a basic block break should occur.
  std::vector<Address> basic_block_breaks;

  // Find resynchronization points. Typically there should only ever be one
  // incoming code flow per instruction. Except when we have overlapping
  // instructions. A synchronization point is when two such sequences of
  // overlapping instructions eventually align again and merge into a single
  // instruction stream. We track only 32 bytes to keep the map small and
  // because x86 instructions cannot be larger than that (and presumably no
  // other instruction set either).
  // Since we do this globally without having functions we could end up with
  // basic block breaks that are not warranted by any of the functions in the
  // final disassembly. This is unfortunate, but not dangerous, as the
  // disassembly is still correct, just with spurious edges.
  // Note: we cannot use [flat, node]_hash_map here since we repeatedly iterate
  // over a map from which (possibly) a lot of elements have been erased.
  // This has a complexity of O(num_elements) for unordered_map, but has
  // O(num_deleted_elements + num_elements) complexity for the other maps.
  // TODO(jannewger): performance of the algorithm below is non-ideal and should
  // be improved.
  std::unordered_map<Address, int> incoming_code_flows;
  for (const auto& instruction : *instructions) {
    const Address address = instruction.GetAddress();
    const Address flow_address = instruction.GetNextInstruction();
    for (auto it = incoming_code_flows.begin(), end = incoming_code_flows.end();
         it != end;) {
      if (it->second > 1) {
        // More than one incoming code flow to this instruction -> the preceding
        // instructions must have been overlapping.
        basic_block_breaks.emplace_back(it->first);
      } else if (it->first + 32 > address) {
        // We are still within a 32 bytes trailing window of the current
        // instruction, thus we keep the instruction in the map as it could
        // potentially still overlap.
        break;
      }
      it = incoming_code_flows.erase(it);
    }
    if (instruction.IsFlow()) {
      ++incoming_code_flows[flow_address];
    }
  }

  // If we've had overlapping code in the final bytes of the executable the
  // above loop will miss it. So we deal with those trailing bytes here.
  for (const auto incoming_code_flow : incoming_code_flows) {
    if (incoming_code_flow.second > 1) {
      basic_block_breaks.emplace_back(incoming_code_flow.first);
    }
  }

  // Find branch targets.
  for (const auto& edge : edges_) {
    basic_block_breaks.emplace_back(edge.target);
  }

  // Find call targets (function entry points).
  for (Address entry_point : call_graph->GetFunctions()) {
    basic_block_breaks.emplace_back(entry_point);
  }

  // Make basic block breaks unique and sort them.
  std::sort(basic_block_breaks.begin(), basic_block_breaks.end());
  basic_block_breaks.erase(
      std::unique(basic_block_breaks.begin(), basic_block_breaks.end()),
      basic_block_breaks.end());

  return basic_block_breaks;
}

// Follows flow from every function entry point and collects a global "soup" of
// basic blocks. Not yet attached to any function.
void FlowGraph::CreateBasicBlocks(Instructions* instructions,
                                  CallGraph* call_graph) {
  // Sort edges by source address.
  std::sort(edges_.begin(), edges_.end());
  call_graph->SortEdges();

  // If we split basic blocks we add new synthetic (unconditional) edges. These
  // will be added here.
  Edges new_edges;

  const auto basic_block_breaks =
      FindBasicBlockBreaks(instructions, call_graph);

  // Reset instruction visited flag. Every instruction should belong to exactly
  // one basic block, we use the flag to keep track of this constraint.
  for (auto& instruction : *instructions) {
    instruction.SetFlag(FLAG_VISITED, false);
  }

  absl::flat_hash_set<Address> invalid_functions;
  // Start with every known function entry point address and follow flow from
  // there. Create new functions and add basic blocks and edges to them.
  for (Address entry_point : call_graph->GetFunctions()) {
    if (GetInstruction(instructions, entry_point) == instructions->end()) {
      continue;  // Skip invalid/imported functions.
    }
    std::stack<Address> address_stack;
    address_stack.push(entry_point);

    const int initial_edge_number = new_edges.size();
    int current_bb_number = 0;
    int number_of_instructions = 0;
    // Keep track of basic blocks already added to this function.
    absl::flat_hash_set<Address> function_basic_blocks;
    while (!address_stack.empty()) {
      Address address = address_stack.top();
      address_stack.pop();
      Instructions::iterator instruction =
          GetInstruction(instructions, address);
      if (instruction == instructions->end()) {
        continue;
      }
      CHECK(instruction->GetAddress() == address);
      if (!function_basic_blocks.insert(address).second) {
        // We've already dealt with this basic block.
        continue;
      }
      BasicBlock* basic_block = BasicBlock::Find(address);
      if (basic_block == nullptr) {
        // We need to create a new basic block.
        BasicBlockInstructions basic_block_instructions;
        int bb_instr_cnt = 0;
        bool bb_skip = false;
        do {
          if (++bb_instr_cnt > kMaxFunctionInstructions) {
            bb_skip = true;
            break;
          }
          CHECK(!instruction->HasFlag(FLAG_VISITED));
          instruction->SetFlag(FLAG_VISITED, true);
          basic_block_instructions.AddInstruction(instruction);
          instruction = GetNextInstruction(instructions, instruction);
        } while (instruction != instructions->end() &&
                 !std::binary_search(basic_block_breaks.begin(),
                                     basic_block_breaks.end(),
                                     instruction->GetAddress()));
        basic_block = BasicBlock::Create(&basic_block_instructions);
        number_of_instructions += bb_instr_cnt;
        if (bb_skip) {
          LOG(WARNING) << absl::StrCat("Skipping enormous basic block: ",
                                       absl::Hex(address, absl::kZeroPad8));
          continue;
        }
      } else {
        number_of_instructions += basic_block->GetInstructionCount();
      }
      CHECK(basic_block != nullptr);
      ++current_bb_number;
      // Erases all new_edges for the current entry point (to save some memory)
      // and add entry point to the list of invalid functions, which is
      // processed later (after all entry points). This step saves memory and
      // cpu, because it skips generating results which otherwise would be
      // discarded by the FlowGraph::FinalizeFunctions
      if ((current_bb_number > kMaxFunctionEarlyBasicBlocks) ||
          (number_of_instructions > kMaxFunctionInstructions)) {
        if (initial_edge_number < new_edges.size()) {
          new_edges.erase(new_edges.begin() + initial_edge_number,
                          new_edges.end());
        }
        invalid_functions.insert(entry_point);
        break;
      }

      // Three possibilities:
      // - the basic block ends in a non-code flow instruction
      // - the basic block ends in a branch instruction
      // - the basic block ends before a basic_block_breaks instruction
      CHECK(basic_block != nullptr);
      address = basic_block->GetLastAddress();
      instruction = GetInstruction(instructions, address);
      CHECK(instruction != instructions->end());
      auto edge =
          std::lower_bound(edges_.begin(), edges_.end(), address,
                           [](const FlowGraphEdge& edge, Address address) {
                             return edge.source < address;
                           });
      if (edge != edges_.end() && edge->source == address) {
        // A branch instruction.
        for (; edge != edges_.end() && edge->source == address; ++edge) {
          address_stack.push(edge->target);
        }
      } else if (instruction->IsFlow()) {
        // We must have hit a basic_block_breaks instruction. Add a synthetic
        // unconditional branch edge to the next instruction.
        const Address next_address = instruction->GetNextInstruction();
        address_stack.push(next_address);
        new_edges.emplace_back(address, next_address,
                               FlowGraphEdge::TYPE_UNCONDITIONAL);
      }
    }
  }

  if (!invalid_functions.empty()) {
    LOG(INFO) << "Early erase of " << invalid_functions.size()
              << " invalid functions.";

    // Drops all entry-basic-blocks for "invalid" functions, we keep entry
    // points in the call_graph->functions_, so later
    // FlowGraph::FinalizeFunctions can add "dummy" function entries for call
    // targets which might be wrong, but we can't really tell at this moment.
    // Such invalid call targets will be marked as imported functions in the
    // output binexport.
    for (const Address& addr : invalid_functions) {
      BasicBlock::blocks().erase(addr);
    }
    // Removes all edges that go from/to the invalid_functions. This step might
    // be not necessary, but it reduces amount of data which would be processed
    // later.
    edges_.erase(
        std::remove_if(edges_.begin(), edges_.end(),
                       [&invalid_functions](const FlowGraphEdge& edge) {
                         return (invalid_functions.find(edge.source) !=
                                 invalid_functions.end()) ||
                                (invalid_functions.find(edge.target) !=
                                 invalid_functions.end());
                       }),
        edges_.end());
  }

  // Add the new synthetic edges.
  edges_.insert(edges_.end(), new_edges.begin(), new_edges.end());
  std::sort(edges_.begin(), edges_.end());
}

// Merges basic blocks iff:
// 1) source basic block has exactly 1 out-edge
// 2) target basic block has exactly 1 in-edge
// 3) source basic block != target basic block
// 4) target basic block != function entry point (we want to leave that intact)
void FlowGraph::MergeBasicBlocks(const CallGraph& call_graph) {
  // At this point we have created basic blocks but have not yet added edges to
  // the functions. We perform basic block merging first before finally creating
  // functions.
  auto delete_edge = [this, &call_graph](const FlowGraphEdge& edge) {
    if (edge.type != FlowGraphEdge::TYPE_UNCONDITIONAL) {
      // Only unconditional edges can potentially lead to a
      // merge.
      return false;
    }

    BasicBlock* target_basic_block = BasicBlock::Find(edge.target);
    if (!target_basic_block) {
      DLOG(INFO) << absl::StrCat("No target basic block for edge ",
                                 absl::Hex(edge.source, absl::kZeroPad8),
                                 " -> ",
                                 absl::Hex(edge.target, absl::kZeroPad8));
      return true;
    }

    if (target_basic_block->begin()->GetInDegree() != 1) {
      // We have more than a single in edge => do not delete.
      return false;
    }

    if (call_graph.GetFunctions().count(target_basic_block->GetEntryPoint())) {
      // Target basic block is a function entry point => do not delete.
      return false;
    }

    BasicBlock* source_basic_block = BasicBlock::FindContaining(edge.source);
    if (!source_basic_block) {
      LOG(INFO) << absl::StrCat("No source basic block for edge ",
                                   absl::Hex(edge.source, absl::kZeroPad8),
                                   " -> ",
                                   absl::Hex(edge.target, absl::kZeroPad8));
      return true;
    }

    CHECK(source_basic_block->GetLastAddress() == edge.source);
    if (source_basic_block == target_basic_block) {
      // This is a self loop edge => do not delete.
      return false;
    }

    auto edges = std::equal_range(
        edges_.begin(), edges_.end(), edge,
        [](const FlowGraphEdge& one, const FlowGraphEdge& two) {
          // Consider edges to be equivalent based on their source address only.
          return one.source < two.source;
        });
    if (std::distance(edges.first, edges.second) > 1) {
      // There is more than one edge originating at the source address. Do not
      // merge. While this will typically not happen for unconditional edges it
      // will for synthetic try/catch block edges. First introduced for the DEX
      // disassembler in cl/100567062.
      CHECK(edge.source == edges.first->source);
      return false;
    }

    source_basic_block->AppendBlock(*target_basic_block);
    BasicBlock::blocks().erase(target_basic_block->GetEntryPoint());
    return true;
  };

  // Delete all edges that connect merged basic blocks.
  edges_.erase(std::remove_if(edges_.begin(), edges_.end(), delete_edge),
               edges_.end());
}

void FlowGraph::FinalizeFunctions(CallGraph* call_graph) {
  // We now have a global "soup" of basic blocks and edges. Next we need to
  // follow flow from every function entry point and collect a list of basic
  // blocks and edges per function. While doing so we also link the new function
  // into the call graph.
  for (Address entry_point : call_graph->GetFunctions()) {
    std::stack<Address> address_stack;
    address_stack.push(entry_point);
    std::unique_ptr<Function> function(new Function(entry_point));
    size_t num_instructions = 0;
    // Keep track of basic blocks and edges already added to this function.
    absl::flat_hash_set<Address> function_basic_blocks;
    // TODO(cblichmann): Encountering the same basic block multiple times during
    //                   traversal is expected and ok. Encountering the same
    //                   edge is not. This is just inefficient - why did we add
    //                   it twice in the first place? Only the ARM disassembler
    //                   produces redundant edges atm.
    absl::flat_hash_set<FlowGraphEdge, FlowGraphEdgeHash> done_edges;
    while (!address_stack.empty()) {
      Address address = address_stack.top();
      address_stack.pop();
      if (!function_basic_blocks.insert(address).second) {
        continue;  // Already added to the function.
      }
      if (function_basic_blocks.size() >= kMaxFunctionBasicBlocks ||
          function->GetEdges().size() >= kMaxFunctionEdges ||
          num_instructions >= kMaxFunctionInstructions) {
        break;
      }
      BasicBlock* basic_block = BasicBlock::Find(address);
      if (!basic_block) {
        // A function without a body (imported/invalid).
        continue;
      }
      function->AddBasicBlock(basic_block);
      num_instructions += basic_block->GetInstructionCount();

      const auto source_address = basic_block->GetLastAddress();
      auto edge =
          std::lower_bound(edges_.begin(), edges_.end(), source_address,
                           [](const FlowGraphEdge& edge, Address address) {
                             return edge.source < address;
                           });
      uint64_t dropped_edges = 0;
      for (; edge != edges_.end() && edge->source == source_address; ++edge) {
        if (!BasicBlock::Find(edge->target)) {
          DLOG(INFO) << absl::StrCat(
              "Dropping edge ", absl::Hex(edge->source, absl::kZeroPad8),
              " -> ", absl::Hex(edge->target, absl::kZeroPad8),
              " because the target address is invalid.");
          ++dropped_edges;
          continue;
        }
        if (done_edges.insert(*edge).second) {
          function->AddEdge(*edge);
          address_stack.push(edge->target);
        }
      }
      LOG_IF(INFO, dropped_edges > 0) << absl::StrCat(
          "Dropped ", dropped_edges,
          " edges because the target address is invalid (current basic block ",
          absl::Hex(address, absl::kZeroPad8), ").");
    }
    if (function_basic_blocks.size() >= kMaxFunctionBasicBlocks ||
        function->GetEdges().size() >= kMaxFunctionEdges ||
        num_instructions >= kMaxFunctionInstructions) {
      DLOG(INFO) << absl::StrCat(
          "Discarding excessively large function ",
          absl::Hex(entry_point, absl::kZeroPad8), ": ",
          function_basic_blocks.size(), " basic blocks, ",
          function->GetEdges().size(), " edges, ", num_instructions,
          " instructions (Limit is ", kMaxFunctionBasicBlocks, ", ",
          kMaxFunctionEdges, ", ", kMaxFunctionInstructions, ")");
      function->Clear();
    }
    for (const auto* basic_block : function->GetBasicBlocks()) {
      // Iterate the basic block and add edges to the call graph for every
      // call instruction. The call graph already knows about source and
      // target address, but is not linked to functions yet.
      const auto& call_edges = call_graph->GetEdges();
      for (const auto& instruction : *basic_block) {
        if (instruction.HasFlag(FLAG_CALL)) {
          auto edge = std::lower_bound(
              call_edges.begin(), call_edges.end(), instruction.GetAddress(),
              [](const EdgeInfo& edge, Address address) {
                return edge.source_ < address;
              });
          for (; edge != call_edges.end() &&
                 edge->source_ == instruction.GetAddress();
               ++edge) {
            call_graph->ScheduleEdgeAdd(function.get(), edge->source_,
                                        edge->target_);
          }
        }
      }
    }
    function->SortGraph();
    functions_.insert(std::make_pair(entry_point, function.release()));
  }

  // We don't need them any longer (functions store their own copy).
  Edges().swap(edges_);
  call_graph->CommitEdges();  // Clear temporary edges.
}

// Follow code flow from every function entry point. Create basic blocks and
// functions. If any instruction in a basic block gets executed, all of them
// will be. This means that we break basic blocks iff:
// - the instruction is a branch
// - the instruction is a branch target
// - the instruction is a call target (function entry point)
// - the instruction follows a call and is invalid (we assume a non-returning
//   call)
// - the instruction is a resynchronization point, i.e. a sequence of
//   overlapping instructions merges again at the current one.
void FlowGraph::ReconstructFunctions(Instructions* instructions,
                                     CallGraph* call_graph) {
  CreateBasicBlocks(instructions, call_graph);
  MergeBasicBlocks(*call_graph);
  FinalizeFunctions(call_graph);
}

void FlowGraph::PruneFlowGraphEdges() {
  // Stupid post processing step but IDA sometimes produces broken edges
  // (pointing nowhere) if it incorrectly disassembled a data section.
  for (auto kv : functions_) {
    kv.second->FixEdges();
  }
}
