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

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_NAMES_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_NAMES_H_

#include <cstring>
#include <map>
#include <string>

#include "third_party/zynamics/binexport/comment.h"
#include "third_party/zynamics/binexport/entry_point.h"
#include "third_party/zynamics/binexport/instruction.h"
#include "third_party/zynamics/binexport/types.h"

class CallGraph;
class FlowGraph;
class Writer;
class insn_t;
class op_t;

struct Name {
  Name(const string& name, Expression::Type type)
      : name(name), type(type) {}

  bool empty() const {
    return name.empty() || type == Expression::TYPE_INVALID;
  }

  string name;
  Expression::Type type;
};

using ModuleMap = std::map<Address, string>;

void AnalyzeFlowIda(EntryPoints* entryPoints, const ModuleMap* modules,
                    Writer* writer, detego::Instructions* instructions,
                    FlowGraph* flowGraph, CallGraph* callGraph);

string GetRegisterName(size_t index, size_t segment_size);
string GetVariableName(const insn_t& instruction, uint8 operand_num);
string GetGlobalStructureName(Address address, Address instance_address,
                                   uint8 operand_num);
Name GetName(Address address, Address immediate, uint8 operand_num,
             bool user_names_only);
string GetName(Address address, bool user_names_only);
string GetModuleName();
string GetArchitectureName();
int GetArchitectureBitness();

string GetSizePrefix(const size_t size_in_bytes);
size_t GetOperandByteSize(const insn_t& instruction, const op_t& operand);

size_t GetSegmentSize(const Address address);
int GetOriginalIdaLine(const Address address, string* line);
string GetMnemonic(const Address address);
ModuleMap InitModuleMap();
Address GetImageBase();
bool IsCode(Address address);
bool IsPossibleFunction(Address address);
bool IsStructVariable(Address address, uint8 operand_num);
bool IsStackVariable(Address address, uint8 operand_num);
void GetComments(const insn_t& instruction,
                 Comments* comments);  // Cached in callgraph!

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_NAMES_H_
