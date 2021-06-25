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

#ifndef NAMES_H_
#define NAMES_H_

#include <cstdint>
#include <string>

// clang-format off
#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <ua.hpp>                                               // NOLINT
#include <segment.hpp>                                          // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT
// clang-format on

#include "third_party/absl/types/optional.h"
#include "third_party/zynamics/binexport/comment.h"
#include "third_party/zynamics/binexport/expression.h"
#include "third_party/zynamics/binexport/types.h"

namespace security::binexport {

struct Name {
  Name(const std::string& name, Expression::Type type)
      : name(name), type(type) {}

  bool empty() const {
    return name.empty() || type == Expression::TYPE_INVALID;
  }

  std::string name;
  Expression::Type type = Expression::TYPE_INVALID;
};

std::string GetRegisterName(size_t index, size_t bit_width);

// Returns the variable name of either a stack variable or a structure variable
// at the given position.
std::string GetVariableName(const insn_t& instruction, uint8_t operand_num);

std::string GetGlobalStructureName(Address address, Address instance_address,
                                   uint8_t operand_num);

Name GetName(Address address, Address immediate, uint8_t operand_num,
             bool user_names_only);

std::string GetName(Address address, bool user_names_only = false);

std::string GetDemangledName(Address address);

std::string GetModuleName();

std::string GetBytes(const Instruction& instruction);

// CPU instruction sets that are explicitly supported. These apply to both
// 32-bit and 64-bit variants, where applicable.
enum Architecture {
  kX86 = 0,
  kArm,
  kPpc,
  kMips,
  kGeneric,
  kDalvik,
};

Architecture GetArchitecture();
absl::optional<std::string> GetArchitectureName();

int GetArchitectureBitness();

std::string GetSizePrefix(size_t size_in_bytes);

// Returns the size of an instruction's operand in bytes. Returns 0 for invalid
// or undefined operands.
size_t GetOperandByteSize(const insn_t& instruction, const op_t& operand);

// Returns the size of the segment to which the address belongs in bytes.
size_t GetSegmentSize(const Address address);

int GetOriginalIdaLine(const Address address, std::string* line);
std::string GetMnemonic(const Address address);
Address GetImageBase();

bool IsCode(Address address);
bool IsStructVariable(Address address, uint8_t operand_num);
bool IsStackVariable(Address address, uint8_t operand_num);

// Returns the first string references for the address. Note that there may be
// several string references.
std::string GetStringReference(ea_t address);

std::vector<Byte> GetSectionBytes(ea_t segment_start_address);
int GetPermissions(const segment_t* ida_segment);

void GetComments(const insn_t& instruction,
                 Comments* comments);  // Cached in callgraph!

}  // namespace security::binexport

#endif  // NAMES_H_
