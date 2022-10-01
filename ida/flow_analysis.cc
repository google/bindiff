// Copyright 2021 Google LLC
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

#include "third_party/zynamics/binexport/ida/flow_analysis.h"

// clang-format off
#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <idp.hpp>                                              // NOLINT
#include <allins.hpp>                                           // NOLINT
#include <enum.hpp>                                             // NOLINT
#include <frame.hpp>                                            // NOLINT
#include <ida.hpp>                                              // NOLINT
#include <lines.hpp>                                            // NOLINT
#include <name.hpp>                                             // NOLINT
#include <segment.hpp>                                          // NOLINT
#include <struct.hpp>                                           // NOLINT
#include <typeinf.hpp>                                          // NOLINT
#include <ua.hpp>                                               // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT
// clang-format on

#include "third_party/absl/log/log.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/binexport/flow_analysis.h"
#include "third_party/zynamics/binexport/ida/arm.h"
#include "third_party/zynamics/binexport/ida/dalvik.h"
#include "third_party/zynamics/binexport/ida/generic.h"
#include "third_party/zynamics/binexport/ida/metapc.h"
#include "third_party/zynamics/binexport/ida/mips.h"
#include "third_party/zynamics/binexport/ida/names.h"
#include "third_party/zynamics/binexport/ida/ppc.h"
#include "third_party/zynamics/binexport/ida/util.h"
#include "third_party/zynamics/binexport/util/format.h"
#include "third_party/zynamics/binexport/util/status_macros.h"
#include "third_party/zynamics/binexport/util/timer.h"
#include "third_party/zynamics/binexport/x86_nop.h"

namespace security::binexport {

ModuleMap InitModuleMap() {
  ModuleMap modules;

  struct ImportData {
    std::string module_name;
    ModuleMap* modules;
  };
  for (int i = 0; i < get_import_module_qty(); ++i) {
    qstring ida_module_name;
    if (!get_import_module_name(&ida_module_name, i)) {
      continue;
    }
    ImportData callback_capture = {ToString(ida_module_name), &modules};
    enum_import_names(
        i,
        static_cast<import_enum_cb_t*>([](ea_t ea, const char* /* name */,
                                          uval_t /* ord */,
                                          void* param) -> int {
          auto& import_data = *static_cast<ImportData*>(param);
          (*import_data.modules)[ea] = import_data.module_name;
          return 1;  // Continue enumeration
        }),
        static_cast<void*>(&callback_capture));
  }
  return modules;
}

std::string GetModuleName(Address address, const ModuleMap& modules) {
  const auto it = modules.find(address);
  if (it != modules.end()) {
    return it->second;
  }
  return "";
}

bool IsCode(Address address) {
  const uchar segment = segtype(address);
  return is_code(get_full_flags(address)) &&
         (segment == SEG_CODE || segment == SEG_NORM ||
          // Some processor modules label code segments with SEG_DATA.
          ((get_ph()->id == PLFM_DALVIK || get_ph()->id == PLFM_M32R) &&
           segment == SEG_DATA));
}

// We allow functions that IDA doesn't know about but only in code segments.
bool IsPossibleFunction(Address address, const ModuleMap& modules) {
  return IsCode(address) || get_func(address) != 0 ||
         modules.find(address) != modules.end();
}

// Returns whether the specified instruction is an unconditional jump.
// Jump is unconditional iff there is exactly one reference (either code or data
// (for imported functions)). In particular, note that there may not be a flow
// reference to the next instruction.
bool IsUnconditionalJump(const insn_t& instruction) {
  if (is_indirect_jump_insn(instruction) && GetArchitecture() == kX86) {
    return true;
  }
  if (GetArchitecture() == kMips &&
      (instruction.itype == MIPS_b || instruction.itype == MIPS_bal ||
       instruction.itype == MIPS_jalr || instruction.itype == MIPS_j ||
       instruction.itype == MIPS_jr || instruction.itype == MIPS_jal ||
       instruction.itype == MIPS_jalx)) {
    return true;
  }

  size_t count = 0;
  xrefblk_t referencesOut;
  for (bool ok = referencesOut.first_from(instruction.ea, XREF_ALL); ok;
       ok = referencesOut.next_from(), ++count) {
    if (referencesOut.iscode && referencesOut.type != fl_JF &&
        referencesOut.type != fl_JN) {
      // we have a code reference that is neither jump short nor jump far
      return false;
    }
  }
  return count == 1;
}

void AnalyzeFlow(const insn_t& ida_instruction, Instruction* instruction,
                 FlowGraph* flow_graph, CallGraph* call_graph,
                 AddressReferences* address_references,
                 EntryPointManager* entry_point_adder,
                 const ModuleMap& modules) {
  const bool unconditional_jump = IsUnconditionalJump(ida_instruction);
  int num_out_edges = 0;
  xrefblk_t xref;
  for (bool ok = xref.first_from(ida_instruction.ea, XREF_ALL);
       ok && xref.iscode; ok = xref.next_from()) {
    if (xref.type == fl_JN || xref.type == fl_JF) {
      ++num_out_edges;
    } else if (unconditional_jump && xref.type == fl_F) {
      // Special case for weird IDA behavior: We had a switch jump statement
      // (jmp[eax*4]) in flash11c.idb that had one unconditional outgoing edge
      // (correct) and a second codeflow edge (incorrect! An unconditional jump
      // should never have regular codeflow set).
      // This is a workaround for that particular situation.
      ++num_out_edges;
    }
  }

  bool handled = false;
  if (num_out_edges > 1) {  // Switch jump table
    ea_t table_address = std::numeric_limits<ea_t>::max();
    for (bool ok = xref.first_from(ida_instruction.ea, XREF_ALL);
         ok && xref.iscode; ok = xref.next_from()) {
      flow_graph->AddEdge(FlowGraphEdge(ida_instruction.ea, xref.to,
                                        FlowGraphEdge::TYPE_SWITCH));
      address_references->emplace_back(
          ida_instruction.ea, GetSourceExpressionId(*instruction, xref.to),
          xref.to, TYPE_SWITCH);
      entry_point_adder->Add(xref.to, EntryPoint::Source::JUMP_TABLE);
      table_address = std::min(table_address, xref.to);
      handled = true;
    }
    // Add a data reference to first address in switch table
    address_references->emplace_back(
        ida_instruction.ea, GetSourceExpressionId(*instruction, table_address),
        table_address, TYPE_DATA);
  } else {  // Normal xref
    for (bool ok = xref.first_from(ida_instruction.ea, XREF_ALL);
         ok && xref.iscode; ok = xref.next_from()) {
      // Regular code flow
      if (xref.type == fl_F || instruction->GetNextInstruction() == xref.to) {
        // We need the || above because IDA gives me xref type unknown for old
        // idbs.
        if (instruction->GetNextInstruction() != xref.to) {
          LOG(INFO) << absl::StrCat(
              "warning: ", FormatAddress(instruction->GetAddress()),
              " flow xref target != address + instruction size (or "
              "instruction is missing flow flag). Disassembly is "
              "likely erroneous.");
        }
        entry_point_adder->Add(xref.to, EntryPoint::Source::CODE_FLOW);
      } else if (xref.type == fl_CN || xref.type == fl_CF) {
        // Call targets
        if (IsPossibleFunction(xref.to, modules)) {
          call_graph->AddFunction(xref.to);
          call_graph->AddEdge(ida_instruction.ea, xref.to);
          entry_point_adder->Add(xref.to, EntryPoint::Source::CALL_TARGET);
        }
        instruction->SetFlag(FLAG_CALL, true);
        address_references->emplace_back(
            ida_instruction.ea, GetSourceExpressionId(*instruction, xref.to),
            xref.to, TYPE_CALL_DIRECT);
        handled = true;
      } else if (xref.type == fl_JN || xref.type == fl_JF) {
        // Jump targets
        if (IsPossibleFunction(xref.to, modules) && xref.type == fl_JF) {
          call_graph->AddEdge(ida_instruction.ea, xref.to);
        }
        // MIPS adds an extra instruction _after_ the jump that'll
        // always be executed. Thus we need to branch from that for flow
        // graph reconstruction to work...
        const Address source_address = GetArchitecture() == kMips
                                           ? instruction->GetNextInstruction()
                                           : ida_instruction.ea;
        if (unconditional_jump) {
          flow_graph->AddEdge(FlowGraphEdge(source_address, xref.to,
                                            FlowGraphEdge::TYPE_UNCONDITIONAL));
          address_references->emplace_back(
              ida_instruction.ea, GetSourceExpressionId(*instruction, xref.to),
              xref.to, TYPE_UNCONDITIONAL);
          handled = true;
        } else {
          Address next_address = instruction->GetNextInstruction();
          if (GetArchitecture() == kMips) {
            // TODO(cblichman): Since this is MIPS, replace below with a
            //                  constant.
            // Look ahead one instruction.
            insn_t next_instruction;
            decode_insn(&next_instruction, next_address);
            next_address += next_instruction.size;
          }

          flow_graph->AddEdge(FlowGraphEdge(source_address, next_address,
                                            FlowGraphEdge::TYPE_FALSE));
          flow_graph->AddEdge(
              FlowGraphEdge(source_address, xref.to, FlowGraphEdge::TYPE_TRUE));
          address_references->emplace_back(
              ida_instruction.ea,
              GetSourceExpressionId(*instruction, next_address), next_address,
              TYPE_FALSE);
          address_references->emplace_back(
              ida_instruction.ea, GetSourceExpressionId(*instruction, xref.to),
              xref.to, TYPE_TRUE);
          handled = true;
        }
        entry_point_adder->Add(xref.to, EntryPoint::Source::JUMP_DIRECT);
      } else {
        LOG(INFO) << absl::StrCat("unknown xref ",
                                  FormatAddress(ida_instruction.ea));
      }
    }
  }

  if (!handled) {
    if (is_call_insn(ida_instruction)) {  // Call to imported function or call
                                          // [offset+eax*4]
      for (bool ok = xref.first_from(ida_instruction.ea, XREF_DATA); ok;
           ok = xref.next_from()) {
        if (IsPossibleFunction(xref.to, modules)) {
          call_graph->AddFunction(xref.to);
          call_graph->AddEdge(ida_instruction.ea, xref.to);
          entry_point_adder->Add(xref.to, EntryPoint::Source::CALL_TARGET);
        }
        instruction->SetFlag(FLAG_CALL, true);
        address_references->emplace_back(
            ida_instruction.ea, GetSourceExpressionId(*instruction, xref.to),
            xref.to, TYPE_CALL_INDIRECT);
      }
    } else {  // Assume data reference as in "push aValue"
      for (bool ok = xref.first_from(ida_instruction.ea, XREF_DATA); ok;
           ok = xref.next_from()) {
        if (!GetStringReference(ida_instruction.ea).empty()) {
          address_references->emplace_back(
              ida_instruction.ea, GetSourceExpressionId(*instruction, xref.to),
              xref.to, TYPE_DATA_STRING);
        } else {
          address_references->emplace_back(
              ida_instruction.ea, GetSourceExpressionId(*instruction, xref.to),
              xref.to, TYPE_DATA);
        }
      }
    }
  }
}

absl::Status AnalyzeFlowIda(EntryPoints* entry_points, const ModuleMap& modules,
                            Writer* writer, detego::Instructions* instructions,
                            FlowGraph* flow_graph, CallGraph* call_graph,
                            FlowGraph::NoReturnHeuristic noreturn_heuristic) {
  Timer<> timer;
  AddressReferences address_references;

  // Add initial entry points as functions.
  for (const auto& entry_point : *entry_points) {
    if ((entry_point.IsFunctionPrologue() || entry_point.IsExternal() ||
         entry_point.IsCallTarget()) &&
        IsPossibleFunction(entry_point.address_, modules)) {
      call_graph->AddFunction(entry_point.address_);
    }
  }

  AddressSpace address_space;
  AddressSpace flags;
  for (int i = 0; i < get_segm_qty(); ++i) {
    const segment_t* segment = getnseg(i);
    address_space.AddMemoryBlock(segment->start_ea,
                                 GetSectionBytes(segment->start_ea),
                                 GetPermissions(segment));
    flags.AddMemoryBlock(
        segment->start_ea,
        AddressSpace::MemoryBlock(size_t(segment->end_ea - segment->start_ea)),
        GetPermissions(segment));
  }

  Instruction::SetBitness(GetArchitectureBitness());
  Instruction::SetGetBytesCallback(&GetBytes);
  Instruction::SetMemoryFlags(&flags);
  std::function<Instruction(const insn_t&, CallGraph*, FlowGraph*)>
      parse_instruction = nullptr;
  bool mark_x86_nops = false;
  switch (GetArchitecture()) {
    case kX86:
      parse_instruction = ParseInstructionIdaMetaPc;
      mark_x86_nops =
          noreturn_heuristic == FlowGraph::NoReturnHeuristic::kNopsAfterCall;
      break;
    case kArm:
      parse_instruction = ParseInstructionIdaArm;
      break;
    case kPpc:
      parse_instruction = ParseInstructionIdaPpc;
      break;
    case kMips:
      parse_instruction = ParseInstructionIdaMips;
      break;
    case kDalvik:
      parse_instruction = ParseInstructionIdaDalvik;
      break;
    case kGeneric:
    default:
      parse_instruction = ParseInstructionIdaGeneric;
      break;
  }

  LOG(INFO) << "flow analysis";
  for (EntryPointManager entry_point_adder(entry_points, "flow analysis");
       !entry_points->empty();) {
    const Address address = entry_points->back().address_;
    entry_points->pop_back();

    if (!flags.IsValidAddress(address) || (flags[address] & FLAG_VISITED)) {
      continue;  // Do not analyze an address twice.
    }
    flags[address] |= FLAG_VISITED;

    insn_t ida_instruction;
    if (decode_insn(&ida_instruction, address) <= 0) {
      continue;
    }
    Instruction new_instruction =
        parse_instruction(ida_instruction, call_graph, flow_graph);
    if (new_instruction.HasFlag(FLAG_INVALID)) {
      continue;
    }
    AnalyzeFlow(ida_instruction, &new_instruction, flow_graph, call_graph,
                &address_references, &entry_point_adder, modules);
    call_graph->AddStringReference(address, GetStringReference(address));
    GetComments(ida_instruction, &call_graph->GetComments());

    if (mark_x86_nops) {
      // FLAG_NOP is only important when reconstructing functions, thus we can
      // set if after AnalyzeFlow().
      new_instruction.SetFlag(FLAG_NOP, IsNopX86(new_instruction.GetBytes()));
    }

    instructions->push_back(new_instruction);
  }

  LOG(INFO) << "sorting instructions";
  SortInstructions(instructions);

  LOG(INFO) << "reconstructing flow graphs";
  std::sort(address_references.begin(), address_references.end());
  // TODO(soerenme): Remove duplicates if any.
  ReconstructFlowGraph(instructions, *flow_graph, call_graph);

  LOG(INFO) << "reconstructing functions";
  flow_graph->ReconstructFunctions(instructions, call_graph,
                                   noreturn_heuristic);

  // Must be called after ReconstructFunctions() since that will sometimes
  // remove source basic blocks for an edge. Only happens when IDA's disassembly
  // is thoroughly broken.
  flow_graph->PruneFlowGraphEdges();

  // Note: PruneFlowGraphEdges might add comments to the callgraph so the
  // post processing must happen afterwards.
  call_graph->PostProcessComments();

  LOG(INFO) << "IDA specific post processing";
  // Ida specific post processing.
  for (auto i = flow_graph->GetFunctions().begin(),
            end = flow_graph->GetFunctions().end();
       i != end; ++i) {
    Function& function = *i->second;
    const Address address = function.GetEntryPoint();
    // - set function name
    const std::string name = GetName(address, true);
    if (!name.empty()) {
      function.SetName(name, GetDemangledName(address));
    }
    // - set function type
    const func_t* ida_func = get_func(address);
    if (ida_func) {
      if ((ida_func->flags & FUNC_THUNK)  // give thunk preference over library
          && function.GetBasicBlocks().size() == 1 &&
          (*function.GetBasicBlocks().begin())->GetInstructionCount() == 1) {
        function.SetType(Function::TYPE_THUNK);
      } else if (ida_func->flags & FUNC_LIB) {
        function.SetType(Function::TYPE_LIBRARY);
      }
    }
    const std::string module = GetModuleName(address, modules);
    if (!module.empty()) {
      function.SetType(Function::TYPE_IMPORTED);
      function.SetModuleName(module);
    }
    if (function.GetType(true) == Function::TYPE_NONE ||
        function.GetType(false) == Function::TYPE_STANDARD) {
      if (function.GetBasicBlocks().empty()) {
        function.SetType(Function::TYPE_IMPORTED);
      } else {
        function.SetType(Function::TYPE_STANDARD);
      }
    }
  }

  const auto processing_time = absl::Seconds(timer.elapsed());
  timer.restart();

  LOG(INFO) << "writing...";
  NA_RETURN_IF_ERROR(writer->Write(*call_graph, *flow_graph, *instructions,
                                   address_references, address_space));

  Operand::EmptyCache();
  Expression::EmptyCache();

  const auto writing_time = absl::Seconds(timer.elapsed());
  LOG(INFO) << absl::StrCat(
      GetModuleName(), ": ", HumanReadableDuration(processing_time),
      " processing, ", HumanReadableDuration(writing_time), " writing");
  return absl::OkStatus();
}

}  // namespace security::binexport
