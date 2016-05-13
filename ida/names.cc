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

#include "third_party/zynamics/binexport/ida/names.h"

#include <chrono>  // NOLINT
#include <cinttypes>
#include <iomanip>
#include <sstream>
#include <string>
#include <tuple>

#include "third_party/zynamics/binexport/ida/pro_forward.h"  // NOLINT
#include <idp.hpp>                                           // NOLINT
#include <allins.hpp>                                        // NOLINT
#include <enum.hpp>                                          // NOLINT
#include <frame.hpp>                                         // NOLINT
#include <ida.hpp>                                           // NOLINT
#include <name.hpp>                                          // NOLINT
#include <segment.hpp>                                       // NOLINT
#include <struct.hpp>                                        // NOLINT
#include <typeinf.hpp>                                       // NOLINT
#include <ua.hpp>                                            // NOLINT

#include "base/logging.h"
#include "base/stringprintf.h"
#include "third_party/zynamics/binexport/address_references.h"
#include "third_party/zynamics/binexport/base_types.h"
#include "third_party/zynamics/binexport/call_graph.h"
#include "third_party/zynamics/binexport/filesystem_util.h"
#include "third_party/zynamics/binexport/flow_analyzer.h"
#include "third_party/zynamics/binexport/flow_graph.h"
#include "third_party/zynamics/binexport/ida/arm.h"
#include "third_party/zynamics/binexport/ida/dalvik.h"
#include "third_party/zynamics/binexport/ida/generic.h"
#include "third_party/zynamics/binexport/ida/metapc.h"
#include "third_party/zynamics/binexport/ida/mips.h"
#include "third_party/zynamics/binexport/ida/ppc.h"
#include "third_party/zynamics/binexport/ida/types_container.h"
#include "third_party/zynamics/binexport/type_system.h"
#include "third_party/zynamics/binexport/virtual_memory.h"
#include "third_party/zynamics/binexport/writer.h"
#include "third_party/zynamics/binexport/x86_nop.h"

enum Architecture {
  kX86 = 0,
  kArm,
  kPpc,
  kMips,
  kGeneric,
  kDalvik
};

bool IsCode(Address address) {
  const uchar segment = segtype(address);
  return isCode(getFlags(address)) &&
         (segment == SEG_CODE || segment == SEG_NORM ||
          // Some processor modules label code segments with SEG_DATA.
          ((ph.id == PLFM_DALVIK || ph.id == PLFM_M32R) &&
           segment == SEG_DATA));
}

// We allow functions that IDA doesn't know about but only in code segments.
bool IsPossibleFunction(Address address, const ModuleMap& modules) {
  return IsCode(address) || get_func(address) != 0 ||
         modules.find(address) != modules.end();
}

Address GetImageBase() {
  // @bug: IDA's get_imagebase is buggy for ELF and returns 0 all the time.
  return get_imagebase();
}

Architecture GetArchitecture() {
  std::string architecture(inf.procName);
  if (architecture == "metapc") {
    return kX86;
  }
  if (architecture == "ARM") {
    return kArm;
  }
  if (architecture == "PPC") {
    return kPpc;
  }
  if (architecture == "mipsb" || architecture == "mipsl" ||
      architecture == "mipsr" || architecture == "mipsrl" ||
      architecture == "r5900b" || architecture == "r5900l") {
    return kMips;
  }
  if (architecture == "dalvik") {
    return kDalvik;
  }
  return kGeneric;
}

std::string GetArchitectureName() {
  std::string architecture;
  switch (GetArchitecture()) {
    case kX86:
      architecture = "x86";
      break;
    case kArm:
      architecture = "ARM";
      break;
    case kPpc:
      architecture = "PowerPC";
      break;
    case kMips:
      architecture = "MIPS";
      break;
    case kDalvik:
      architecture = "Dalvik";
      break;
    case kGeneric:
      architecture = "GENERIC";
      break;
    default:
      throw std::runtime_error("unsupported processor");
  }
  if (inf.is_64bit()) {
    architecture += "-64";
  } else if (inf.is_32bit()) {
    architecture += "-32";
  } else {
    architecture += "-32";
  }
  return architecture;
}

int GetArchitectureBitness() { return inf.is_64bit() ? 64 : 32; }

std::string GetModuleName() {
  char path_buffer[QMAXPATH] = {0};
  get_input_file_path(path_buffer, QMAXPATH);
  return GetFilename(path_buffer);
}

// we have an unconditional jump
// iff we have exactly one reference (either code or data (for imported
// functions)) in particular, note that there may not be a flow reference to
// the next instruction
bool IsUnconditionalJump(ea_t address) {
  if (is_indirect_jump_insn(address) && GetArchitecture() == kX86) {
    return true;
  }
  if (GetArchitecture() == kMips &&
      (cmd.itype == MIPS_b || cmd.itype == MIPS_bal || cmd.itype == MIPS_jalr ||
       cmd.itype == MIPS_j || cmd.itype == MIPS_jr || cmd.itype == MIPS_jal ||
       cmd.itype == MIPS_jalx)) {
    return true;
  }

  size_t count = 0;
  xrefblk_t referencesOut;
  for (bool ok = referencesOut.first_from(address, XREF_ALL); ok;
       ok = referencesOut.next_from(), ++count) {
    if (referencesOut.iscode && referencesOut.type != fl_JF &&
        referencesOut.type != fl_JN) {
      // we have a code reference that is neither jump short nor jump far
      return false;
    }
  }
  return count == 1;
}

// create a map "function address" -> "module name"
ModuleMap InitModuleMap() {
  ModuleMap modules;
  // IDA crashes if you access import_node after loading an IDB and closing it
  // again. So we should cop out early if no IDB is currently loaded. I have
  // contacted hex rays for information on how to detect this state.
  if (!netnode::inited()) {
    return modules;
  }

  // TODO(user) We should probably rewrite this code using
  // get_import_module_qty and get_import_module_name and enum_import_names.
  const netnode& imported_modules = import_node;
  for (uval_t module_num = imported_modules.alt1st(); module_num != BADNODE;
       module_num = imported_modules.altnxt(module_num)) {
    enum { BUFFER_SIZE = MAXSTR };
    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);
    std::string module_name("unnamed module");
    if (imported_modules.supstr(module_num, buffer, BUFFER_SIZE) > 0) {
      module_name = buffer;
    } else {
      continue;  // do not enter non-imported modules into the list
    }

    // get the module node (in fact, netnode is just a 32-bit number)
    const netnode module_node = imported_modules.altval(module_num);

    // for all imported by ORDINAL functions
    for (uval_t ordinal = module_node.alt1st(); ordinal != BADNODE;
         ordinal = module_node.altnxt(ordinal)) {
      const ea_t address = module_node.altval(ordinal);
      modules[address] = module_name;
    }

    // for all imported by NAME functions
    for (ea_t address = module_node.sup1st(); address != BADADDR;
         address = module_node.supnxt(address)) {
      modules[address] = module_name;
    }
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

int GetOriginalIdaLine(const Address address, char* buffer,
                       size_t buffer_size) {
  generate_disasm_line((ea_t)address, buffer, buffer_size, 0);
  return tag_remove(buffer, buffer, buffer_size);
}

std::string GetMnemonic(const Address address) {
  enum { kBufferSize = 32 };
  char buffer[kBufferSize] = {0};
  ua_mnem(address, buffer, kBufferSize);
  return buffer;
}

bool IsCall(ea_t address) { return is_call_insn(address); }

// Utility function to render hex values as shown in IDA.
std::string IdaHexify(int value) {
  std::stringstream stream;
  if (value < 10)
    stream << std::dec << value;
  else
    stream << std::hex << std::uppercase << value << "h";
  return stream.str();
}

std::string GetSizePrefix(const size_t size_in_bytes) {
  return "b" + std::to_string(size_in_bytes);
}

size_t GetOperandByteSize(const op_t& operand) {
  switch (operand.dtyp) {
    case dt_byte:
      return 1;  // 8 bit
    case dt_code:
    case dt_word:
      return 2;  // 16 bit
    case dt_dword:
    case dt_float:
      return 4;  // 32 bit
    case dt_fword:
      return 6;  // 48 bit
    case dt_double:
    case dt_qword:
      return 8;  // 64 bit
    case dt_byte16:
      return 16;  // 128 bit
    case dt_byte32:
      return 32;  // 256 bit
    case dt_byte64:
      return 64;  // 512 bit
    case dt_tbyte:
      return ph.tbyte_size;  // variable size (ph.tbyte_size)
    default: {
      std::stringstream stream;
      stream << __FUNCTION__ << ": Invalid operand type ("
             << static_cast<int>(operand.dtyp) << ") at address " << std::hex
             << cmd.ea << ".";
      throw std::runtime_error(stream.str());
    }
  }
}

// in bytes
size_t GetSegmentSize(const Address address) {
  const segment_t* segment = getseg(address);
  // fucking IDA: 0 = 16, 1 = 32, 2 = 64
  return (16 << segment->bitness) >> 3;
}

bool IsCodeSegment(const Address address) {
  if (const segment_t* segment = getseg(address)) {
    return segment->type != SEG_XTRN && segment->type != SEG_DATA;
  }
  return false;
}

// @bug: this only returns the first string ref - there may be several
std::string GetStringReference(ea_t address) {
  xrefblk_t xrefs;
  if (xrefs.first_from(address, XREF_DATA) == 0) {
    return "";
  }

  while (!xrefs.iscode) {
    if ((xrefs.type == dr_O) && (isASCII(getFlags(xrefs.to)))) {
      size_t length = get_max_ascii_length(xrefs.to, ASCSTR_C);
      if (length == 2) {
        length = get_max_ascii_length(xrefs.to, ASCSTR_UNICODE);
      }

      std::string value(length, ' ');
      for (size_t i = 0; i < length; ++i) {
        value[i] = get_byte(xrefs.to + i);
      }
      return value;
    }

    if (xrefs.next_from() == 0) {
      break;
    }
  }
  return "";
}

bool IsStackVariable(Address address, uint8_t operand_num) {
  flags_t flags = get_flags_novalue(static_cast<ea_t>(address));
  return isStkvar(flags, operand_num);
}

bool IsStructVariable(Address address, uint8_t operand_num) {
  flags_t flags = get_flags_novalue(static_cast<ea_t>(address));
  return (operand_num == 0 && isStroff0(flags)) ||
         (operand_num == 1 && isStroff1(flags));
}

// Returns the variable name of either a stack variable or a
// structure variable at the given position.
std::string GetVariableName(Address address, uint8_t operand_num) {
  if (!IsStackVariable(address, operand_num)) {
    return "";
  }

  const member_t* stack_variable =
      get_stkvar(cmd.Operands[operand_num], cmd.Operands[operand_num].addr, 0);
  if (!stack_variable) {
    return "";
  }

  qstring ida_name(get_struc_name(stack_variable->id));
  std::string name(ida_name.c_str(), ida_name.length());

  // The parsing is in here because IDA puts in some strange segment prefix or
  // something like that.
  name = name.substr(name.find('.', 4) + 1);
  if (name[0] != ' ') {
    func_t* function = get_func(address);
    if (!function) {
      return name;
    }

    const ea_t offset =
        calc_stkvar_struc_offset(function, address, operand_num);
    if (offset == BADADDR) {
      return name;
    }

    std::stringstream stream;

    // The following comment is from the Python exporter:
    // 4 is the value of the stack pointer register SP/ESP in x86. This should
    // not break other archs but needs to be here or otherwise would need to
    // override the whole method in metapc...
    tid_t id = 0;
    adiff_t disp = 0;
    adiff_t delta = 0;
    if (get_struct_operand(address, operand_num, &id, &disp, &delta) &&
        cmd.Operands[operand_num].reg == 4) {
      int delta = get_spd(function, address);
      delta = -delta - function->frregs;
      if (delta) {
        stream << IdaHexify(delta) << "+";
      }

      // TODO(user): This must be recursive for nested structs.
      if (const struc_t* structure = get_struc(id)) {
        if (const member_t* member = get_member(structure, disp)) {
          qstring ida_name(get_member_name2(member->id));
          std::string member_name(ida_name.c_str(), ida_name.length());
          stream << name << "." << member_name << disp;
          if (delta) {
            stream << (delta > 0 ? "+" : "") << delta;
          }
          return stream.str();
        }
      }
    } else {
      return name;
    }

    stream << name;
    if (const int delta = offset - stack_variable->soff) {
      stream << "+0x" << std::hex << delta;
    }
    return stream.str();
  }
  return "";
}

std::string GetGlobalStructureName(Address address, Address instance_address,
                                   uint8_t operand_num) {
  std::string instance_name = "";
  tid_t id[MAXSTRUCPATH];
  memset(id, 0, sizeof(id));
  adiff_t disp = 0;
  adiff_t delta = 0;

  int num_structs = get_struct_operand(address, operand_num, id, &disp, &delta);
  if (num_structs > 0) {
    // Special case for the first index - this may be an instance name instead
    // of a type name.
    const struc_t* structure = get_struc(id[0]);
    if (structure) {
      // First try to get a global variable instance name.
      // Second, fall back to just the structure type name.
      qstring ida_name;
      if (get_true_name(&ida_name, instance_address - disp) ||
          get_struc_name(&ida_name, id[0])) {
        instance_name.assign(ida_name.c_str(), ida_name.length());
      }
    }

    // TODO(user): Array members won't be resolved properly. disp will point
    //                 into the array, making get_member calls fail.
    for (const member_t* member = get_member(structure, disp);
         member != nullptr;
         member = get_member(structure, disp -= member->soff)) {
      qstring ida_name(get_member_name2(member->id));
      instance_name += "." + std::string(ida_name.c_str(), ida_name.length());
      structure = get_sptr(member);
    }
  }
  return instance_name;
}

std::string GetName(Address address, bool user_names_only) {
  if (!user_names_only || has_user_name(getFlags(static_cast<ea_t>(address)))) {
    qstring ida_name(get_true_name(static_cast<ea_t>(address)));
    return std::string(ida_name.c_str(), ida_name.length());
  }
  return "";
}

std::string GetDemangledName(Address address) {
  if (has_user_name(getFlags(static_cast<ea_t>(address)))) {
    qstring ida_name(get_short_name(static_cast<ea_t>(address)));
    return std::string(ida_name.c_str(), ida_name.length());
  }
  return "";
}

std::string GetRegisterName(size_t index, size_t segment_size) {
  enum { kBufferSize = MAXSTR };
  char buffer[kBufferSize];
  memset(buffer, 0, kBufferSize);
  if (get_reg_name(index, segment_size, buffer, kBufferSize) != -1) {
    return buffer;
  }
  // Do not return empty string due to assertion fail in database_writer.cc
  return "<bad register>";
}

Name GetName(Address /* address */, Address immediate,
             uint8_t /* operand_num */, bool user_names_only) {
  Expression::Type type = Expression::TYPE_INVALID;
  const std::string name = GetName(immediate, user_names_only);
  if (!name.empty()) {
    xrefblk_t xref;
    for (bool ok = xref.first_to(immediate, XREF_ALL);
         ok && type == Expression::TYPE_INVALID; ok = xref.next_to()) {
      if (!xref.iscode) {
        type = Expression::TYPE_GLOBALVARIABLE;
      }
      switch (xref.type) {
        case fl_JN:
        case fl_JF:
          type = Expression::TYPE_JUMPLABEL;
          break;
        case fl_CN:
        case fl_CF:
          type = Expression::TYPE_FUNCTION;
          break;
      }
    }
  }
  return Name(name, type);
}

void AnalyzeFlow(const Address address, Instruction* instruction,
                 FlowGraph* flow_graph, CallGraph* call_graph,
                 AddressReferences* address_references,
                 EntryPointAdder* entry_point_adder, const ModuleMap& modules) {
  const bool unconditional_jump = IsUnconditionalJump(address);
  int num_out_edges = 0;
  xrefblk_t xref;
  for (bool ok = xref.first_from(address, XREF_ALL); ok && xref.iscode;
       ok = xref.next_from()) {
    if (xref.type == fl_JN || xref.type == fl_JF) {
      ++num_out_edges;
    } else if (unconditional_jump && xref.type == fl_F) {
      // special case for weird IDA behaviour (fogbugz #4623):
      // We had a switch jump statement (jmp[eax*4]) in flash11c.idb that
      // had one unconditional outgoing edge (correct) and a second
      // codeflow edge (incorrect! An unconditional jump should never have
      // regular codeflow set).
      // This is a workaround for that particular situation.
      ++num_out_edges;
    }
  }

  bool handled = false;
  if (num_out_edges > 1) {  // switch jump table
    ea_t table_address = std::numeric_limits<ea_t>::max();
    for (bool ok = xref.first_from(address, XREF_ALL); ok && xref.iscode;
         ok = xref.next_from()) {
      flow_graph->AddEdge(
          FlowGraphEdge(address, xref.to, FlowGraphEdge::TYPE_SWITCH));
      address_references->emplace_back(
          address, GetSourceExpressionId(*instruction, xref.to), xref.to,
          TYPE_SWITCH);
      entry_point_adder->Add(xref.to, EntryPoint::Source::JUMP_TABLE);
      table_address = std::min(table_address, xref.to);
      handled = true;
    }
    // add a data reference to first address in switch table
    address_references->emplace_back(
        address, GetSourceExpressionId(*instruction, table_address),
        table_address, TYPE_DATA);
  } else {  // normal xref
    for (bool ok = xref.first_from(address, XREF_ALL); ok && xref.iscode;
         ok = xref.next_from()) {
      // regular code flow
      if (xref.type == fl_F || instruction->GetNextInstruction() == xref.to) {
        // We need the || above because IDA gives me xref type unknown for old
        // idbs.
        if (instruction->GetNextInstruction() != xref.to) {
          LOG(INFO) << StringPrintf("warning: %08" PRIx64,
                                    instruction->GetAddress())
                    << " flow xref target != address + instruction size (or "
                       "instruction is missing flow flag). IDB disassembly is "
                       "likely erroneous.";
        }
        entry_point_adder->Add(xref.to, EntryPoint::Source::CODE_FLOW);
      } else if (xref.type == fl_CN || xref.type == fl_CF) {
        // call targets
        if (IsPossibleFunction(xref.to, modules)) {
          call_graph->AddFunction(xref.to);
          call_graph->AddEdge(address, xref.to);
          entry_point_adder->Add(xref.to, EntryPoint::Source::CALL_TARGET);
        }
        instruction->SetFlag(FLAG_CALL, true);
        address_references->emplace_back(
            address, GetSourceExpressionId(*instruction, xref.to), xref.to,
            TYPE_CALL_DIRECT);
        handled = true;
      } else if (xref.type == fl_JN || xref.type == fl_JF) {
        // jump targets
        if (IsPossibleFunction(xref.to, modules) && xref.type == fl_JF) {
          call_graph->AddEdge(address, xref.to);
        }
        // MIPS adds an extra instruction _after_ the jump that'll
        // always be executed. Thus we need to branch from that for flow
        // graph reconstruction to work...
        const Address source_address = GetArchitecture() == kMips
                                           ? instruction->GetNextInstruction()
                                           : address;
        if (unconditional_jump) {
          flow_graph->AddEdge(FlowGraphEdge(source_address, xref.to,
                                            FlowGraphEdge::TYPE_UNCONDITIONAL));
          address_references->emplace_back(
              address, GetSourceExpressionId(*instruction, xref.to), xref.to,
              TYPE_UNCONDITIONAL);
          handled = true;
        } else {
          Address next_address = instruction->GetNextInstruction();
          if (GetArchitecture() == kMips) {
            // look ahead one instruction
            decode_insn(static_cast<ea_t>(next_address));
            next_address += cmd.size;
            // reset global cmd structure to original instruction
            decode_insn(static_cast<ea_t>(address));
          }

          flow_graph->AddEdge(FlowGraphEdge(source_address, next_address,
                                            FlowGraphEdge::TYPE_FALSE));
          flow_graph->AddEdge(
              FlowGraphEdge(source_address, xref.to, FlowGraphEdge::TYPE_TRUE));
          address_references->emplace_back(
              address, GetSourceExpressionId(*instruction, next_address),
              next_address, TYPE_FALSE);
          address_references->emplace_back(
              address, GetSourceExpressionId(*instruction, xref.to), xref.to,
              TYPE_TRUE);
          handled = true;
        }
        entry_point_adder->Add(xref.to, EntryPoint::Source::JUMP_DIRECT);
      } else {
        LOG(INFO) << StringPrintf("unknown xref %08llx", address);
      }
    }
  }

  if (!handled) {
    if (IsCall(address)) {  // call to imported function or call [offset+eax*4]
      for (bool ok = xref.first_from(address, XREF_DATA); ok;
           ok = xref.next_from()) {
        if (IsPossibleFunction(xref.to, modules)) {
          call_graph->AddFunction(xref.to);
          call_graph->AddEdge(address, xref.to);
          entry_point_adder->Add(xref.to, EntryPoint::Source::CALL_TARGET);
        }
        instruction->SetFlag(FLAG_CALL, true);
        address_references->emplace_back(
            address, GetSourceExpressionId(*instruction, xref.to), xref.to,
            TYPE_CALL_INDIRECT);
      }
    } else {  // assume data reference as in "push aValue"
      for (bool ok = xref.first_from(address, XREF_DATA); ok;
           ok = xref.next_from()) {
        if (!GetStringReference(address).empty()) {
          address_references->emplace_back(
              address, GetSourceExpressionId(*instruction, xref.to), xref.to,
              TYPE_DATA_STRING);
        } else {
          address_references->emplace_back(
              address, GetSourceExpressionId(*instruction, xref.to), xref.to,
              TYPE_DATA);
        }
      }
    }
  }
}

std::string GetBytes(const Instruction& instruction) {
  std::string bytes(instruction.GetSize(), '\0');
  get_many_bytes(static_cast<ea_t>(instruction.GetAddress()), &(bytes[0]),
                 instruction.GetSize());
  return bytes;
}

bool idaapi HasNoValue(flags_t flags, void* /* ud */) {
  return !hasValue(flags);
}

// Returns the raw bytes that are contained within a segment.
std::vector<Byte> GetSectionBytes(ea_t segment_start_address) {
  std::vector<Byte> bytes;
  const segment_t* ida_segment = getseg(segment_start_address);
  if (ida_segment && isLoaded(ida_segment->startEA)) {
    const ea_t undefined_bytes =
        nextthat(ida_segment->startEA, ida_segment->endEA, HasNoValue,
                 nullptr /* user data */);
    bytes.resize(
        (undefined_bytes == BADADDR ? ida_segment->endEA : undefined_bytes) -
        ida_segment->startEA);
    get_many_bytes(ida_segment->startEA, &bytes[0], bytes.size());
  }
  return bytes;
}

int GetPermissions(const segment_t* ida_segment) {
  int flags = 0;
  flags |= ida_segment->perm & SEGPERM_READ ? AddressSpace::kRead : 0;
  flags |= ida_segment->perm & SEGPERM_WRITE ? AddressSpace::kWrite : 0;
  flags |= ida_segment->perm & SEGPERM_EXEC ? AddressSpace::kExecute : 0;
  return flags;
}

void AnalyzeFlowIda(EntryPoints* entry_points, const ModuleMap* modules,
                    Writer* writer, detego::Instructions* instructions,
                    FlowGraph* flow_graph, CallGraph* call_graph) {
  auto time_start(std::chrono::system_clock::now());
  AddressReferences address_references;

  // Add initial entry points as functions.
  for (const auto& entry_point : *entry_points) {
    if ((entry_point.IsFunctionPrologue() || entry_point.IsExternal() ||
         entry_point.IsCallTarget()) &&
        IsPossibleFunction(entry_point.address_, *modules)) {
      call_graph->AddFunction(entry_point.address_);
    }
  }

  AddressSpace address_space;
  AddressSpace flags;
  for (int i = 0; i < get_segm_qty(); ++i) {
    const segment_t* segment = getnseg(i);
    address_space.AddMemoryBlock(segment->startEA,
                                 GetSectionBytes(segment->startEA),
                                 GetPermissions(segment));
    flags.AddMemoryBlock(
        segment->startEA,
        AddressSpace::MemoryBlock(size_t(segment->endEA - segment->startEA)),
        GetPermissions(segment));
  }

  LOG(INFO) << "gathering types";
  IdaTypesContainer types;
  types.GatherTypes();
  TypeSystem type_system(types, address_space);

  Instruction::SetBitness(GetArchitectureBitness());
  Instruction::SetGetBytesCallback(&GetBytes);
  Instruction::SetMemoryFlags(&flags);
  typedef Instruction (*InstructionParser)(CallGraph&, FlowGraph&,
                                           TypeSystem* /*type_system*/,
                                           const Address /*address*/);
  InstructionParser ParseInstruction = nullptr;
  bool mark_x86_nops = false;
  switch (GetArchitecture()) {
    case kX86:
      ParseInstruction = &ParseInstructionIdaMetaPc;
      mark_x86_nops = true;
      break;
    case kArm:
      ParseInstruction = &ParseInstructionIdaArm;
      break;
    case kPpc:
      ParseInstruction = &ParseInstructionIdaPpc;
      break;
    case kMips:
      ParseInstruction = &ParseInstructionIdaMips;
      break;
    case kDalvik:
      ParseInstruction = &ParseInstructionIdaDalvik;
      break;
    case kGeneric:
    default:
      ParseInstruction = &ParseInstructionIdaGeneric;
      break;
  }

  LOG(INFO) << "flow analysis";
  EntryPointAdder entry_point_adder(entry_points, "flow analysis");
  while (!entry_points->empty()) {
    const Address address = entry_points->back().address_;
    entry_points->pop_back();

    if (!flags.IsValidAddress(address) || (flags[address] & FLAG_VISITED)) {
      continue;  // Do not analyze an address twice.
    }
    flags[address] |= FLAG_VISITED;

    Instruction new_instruction =
        ParseInstruction(*call_graph, *flow_graph, &type_system, address);
    if (new_instruction.HasFlag(FLAG_INVALID)) {
      continue;
    }
    AnalyzeFlow(address, &new_instruction, flow_graph, call_graph,
                &address_references, &entry_point_adder, *modules);
    call_graph->AddStringReference(address, GetStringReference(address));
    GetComments(address, &call_graph->GetComments());

    if (mark_x86_nops) {
      // FLAG_NOP is only important when reconstructing functions, thus we can
      // set if after AnalyzeFlow().
      const auto& new_instruction_bytes = new_instruction.GetBytes();
      new_instruction.SetFlag(FLAG_NOP, IsNopX86(new_instruction_bytes.data(),
                                                 new_instruction_bytes.size()));
    }

    instructions->push_back(new_instruction);
  }

  LOG(INFO) << "sorting instructions";
  SortInstructions(instructions);

  LOG(INFO) << "reconstructing flow graphs";
  std::sort(address_references.begin(), address_references.end());
  // TODO(user): Remove duplicates if any.
  ReconstructFlowGraph(instructions, *flow_graph, call_graph);

  LOG(INFO) << "reconstructing functions";
  flow_graph->ReconstructFunctions(instructions, call_graph);

  // Must be called after simplifyFlowGraphs since that will sometimes
  // remove source basic blocks for an edge. Only happens when IDA completely
  // fucked up its disassembly.
  // see: https://zynamics.fogbugz.com/default.asp?2304#12584
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
    const std::string module = GetModuleName(address, *modules);
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
    types.CreateFunctionPrototype(function);
  }

  auto time_now = std::chrono::system_clock::now();
  std::chrono::duration<double> processing_time(time_now - time_start);
  time_start = time_now;

  LOG(INFO) << "writing...";
  writer->Write(*call_graph, *flow_graph, *instructions, address_references,
                &type_system, address_space);

  Operand::EmptyCache();
  Expression::EmptyCache();

  std::chrono::duration<double> writing_time(std::chrono::system_clock::now() -
                                             time_start);

  LOG(INFO) << GetModuleName()
            << StringPrintf(": %.2fs processing, %.2fs writing",
                            processing_time.count(), writing_time.count());
}

void GetRegularComments(Address address, Comments* comments) {
  enum { kBufferSize = 4096 };
  char buffer[kBufferSize] = {0};
  if (get_cmt(address, false, buffer, kBufferSize) > 0) {
    comments->emplace_back(address, UA_MAXOP + 1,
                           CallGraph::CacheString(buffer), Comment::REGULAR,
                           false);
  }
  if (get_cmt(address, true, buffer, kBufferSize) > 0) {
    comments->emplace_back(address, UA_MAXOP + 2,
                           CallGraph::CacheString(buffer), Comment::REGULAR,
                           true);
  }
}

void GetEnumComments(Address address,
                     Comments* comments) {  // @bug: there is an get_enum_cmt
                                            // function in IDA as well!
  unsigned char serial;
  if (isEnum0(getFlags(address))) {
    if (int id = get_enum_id(address, 0, &serial) != BADNODE) {
      qstring ida_name(get_enum_name(id));
      comments->emplace_back(address, 0,
                             CallGraph::CacheString(std::string(
                                 ida_name.c_str(), ida_name.length())),
                             Comment::ENUM, false);
    }
  }
  if (isEnum1(getFlags(address))) {
    if (int id = get_enum_id(address, 1, &serial) != BADNODE) {
      qstring ida_name(get_enum_name(id));
      comments->emplace_back(address, 1,
                             CallGraph::CacheString(std::string(
                                 ida_name.c_str(), ida_name.length())),
                             Comment::ENUM, false);
    }
  }
}

// Taken from IDA SDK 6.1 nalt.hpp. ExtraGet has since been deprecated.
inline ssize_t ExtraGet(ea_t ea, int what, char* buf, size_t bufsize) {
  return netnode(ea).supstr(what, buf, bufsize);
}

void GetLineComments(Address address, Comments* comments) {
  char buffer[4096];
  const size_t buffer_size = sizeof(buffer) / sizeof(buffer[0]);

  // anterior comments
  std::string comment;
  for (int i = 0; ExtraGet(address, E_PREV + i, buffer, buffer_size) != -1; ++i)
    comment += buffer + std::string("\n");
  if (!comment.empty()) {
    comment = comment.substr(0, comment.size() - 1);
    comments->emplace_back(address, UA_MAXOP + 3,
                           CallGraph::CacheString(comment), Comment::ANTERIOR,
                           false);
  }
  // posterior comments
  comment.clear();
  for (int i = 0; ExtraGet(address, E_NEXT + i, buffer, buffer_size) != -1; ++i)
    comment += buffer + std::string("\n");
  if (!comment.empty()) {
    comment = comment.substr(0, comment.size() - 1);
    comments->emplace_back(address, UA_MAXOP + 4,
                           CallGraph::CacheString(comment), Comment::POSTERIOR,
                           false);
  }
}

void GetFunctionComments(Address address, Comments* comments) {
  if (func_t* function = get_func(address)) {
    if (function->startEA == address) {
      char* comment = get_func_cmt(function, false);
      if (comment) {
        comments->emplace_back(address, UA_MAXOP + 5,
                               CallGraph::CacheString(comment),
                               Comment::FUNCTION, false);
      }
      qfree(reinterpret_cast<void*>(comment));
      comment = get_func_cmt(function, true);
      if (comment) {
        comments->emplace_back(address, UA_MAXOP + 6,
                               CallGraph::CacheString(comment),
                               Comment::FUNCTION, true);
      }
      qfree(reinterpret_cast<void*>(comment));
    }
  }
}

void GetLocationNames(Address address, Comments* comments) {
  func_t* function = get_func(address);
  if (function && function->startEA == address &&
      has_user_name(getFlags(address))) {
    // TODO(cblichmann): get_short_name -> use demangled names for display,
    //                   but port mangled names.
    qstring ida_name(get_true_name(address));
    comments->emplace_back(address, UA_MAXOP + 7,
                           CallGraph::CacheString(std::string(
                               ida_name.c_str(), ida_name.length())),
                           Comment::LOCATION, false);
  }
}

void GetGlobalReferences(Address address, Comments* comments) {
  size_t count = 0;
  xrefblk_t xb;
  for (bool ok = xb.first_from(address, XREF_DATA); ok; ok = xb.next_from(),
            ++count) {  // xb.to - contains the referenced address
    qstring ida_name(get_true_name(xb.to));
    if (ida_name.empty() || has_dummy_name(getFlags(address))) {
      continue;
    }

    // This stores the instance pointer
    comments->emplace_back(address, UA_MAXOP + 1024 + count,
                           CallGraph::CacheString(std::string(
                               ida_name.c_str(), ida_name.length())),
                           Comment::GLOBALREFERENCE, false);
  }
}

class FunctionCache {
 public:
  explicit FunctionCache(func_t* function) : function(function) {
    if (!function) {
      return;
    }

    struc_t* frame = get_frame(function);
    if (!frame) {
      return;
    }

    // @bug: IDA sometimes returns excessively large offsets (billions)
    //       we must prevent looping forever in those cases
    size_t lastSuccess = 0;
    const size_t maxOffset =
        std::min(static_cast<size_t>(get_max_offset(frame)),
                 static_cast<size_t>(1024 * 64));
    for (size_t i = 0; i < maxOffset && lastSuccess - i < 1024;) {
      const member_t* member = get_member(frame, i);
      if (!member || is_special_member(member->id)) {
        ++i;
        continue;
      }

      const ea_t offset = member->soff;
      qstring ida_name(get_member_name2(member->id));
      if (!ida_name.empty()) {
        i += std::max(static_cast<asize_t>(1), get_member_size(member));
        lastSuccess = i;
        continue;
      }

      local_vars[offset].assign(ida_name.c_str(), ida_name.length());
      i += std::max(static_cast<asize_t>(1), get_member_size(member));
      lastSuccess = i;
    }
  }

  func_t* function;
  std::map<ea_t, std::string> local_vars;
};

void GetLocalReferences(ea_t address, Comments* comments) {
  static FunctionCache cache(nullptr);

  func_t* function = get_func(address);
  if (!function) {
    return;
  }

  if (cache.function != function) {
    cache = FunctionCache(function);
  }

  for (size_t operand_num = 0; operand_num < UA_MAXOP; ++operand_num) {
    const ea_t offset =
        calc_stkvar_struc_offset(function, address, operand_num);
    if (offset == BADADDR) {
      continue;
    }

    // one of the operands references a local variable
    if (cache.local_vars.find(offset) != cache.local_vars.end()) {
      comments->emplace_back(address, UA_MAXOP + 2048 + operand_num,
                             CallGraph::CacheString(cache.local_vars[offset]),
                             Comment::LOCALREFERENCE, false);
    }
  }
}

void GetComments(Address address, Comments* comments) {
  GetRegularComments(address, comments);
  GetEnumComments(address, comments);
  GetLineComments(address, comments);
  GetFunctionComments(address, comments);
  GetLocationNames(address, comments);
  GetGlobalReferences(address, comments);
  GetLocalReferences(address, comments);
}
