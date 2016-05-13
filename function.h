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

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_FUNCTION_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_FUNCTION_H_

#include <cstdint>
#include <map>
#include <unordered_set>

#include "third_party/zynamics/binexport/basic_block.h"
#include "third_party/zynamics/binexport/edge.h"
#include "third_party/zynamics/binexport/types.h"

class CallGraph;
class Function;
class FlowGraph;

typedef std::map<Address, Function*> Functions;

class Function {
 public:
  typedef std::vector<FlowGraphEdge> Edges;

  enum FunctionType : uint8_t {
    TYPE_NONE = 123,
    TYPE_STANDARD = 0,
    TYPE_LIBRARY = 1,
    TYPE_IMPORTED = 2,
    TYPE_THUNK = 3,
    TYPE_INVALID = 4,
  };

  enum Name {
    MANGLED = 0,
    DEMANGLED = 1
  };

  static const char* GetTypeName(FunctionType type);

  explicit Function(Address entry_point);
  Function(const Function& other);
  ~Function();

  // Deletes basic blocks and edges, but leaves entry point and name intact.
  void Clear();

  void AddBasicBlock(BasicBlock* basic_block);
  void AddEdge(const FlowGraphEdge& edge);
  void SortGraph();
  void FixEdges();

  // Returns the set of loop edges as determined by the Dominator Tree algorithm
  // by Lengauer and Tarjan. Edges will be returned sorted by source address,
  // which is the same order they are stored in the graph itself.
  void GetBackEdges(std::vector<Edges::const_iterator>* back_edges) const;

  Address GetEntryPoint() const;

  void SetType(FunctionType type);

  // Returns the function type (if assigned) as-is if raw is set to true. If
  // raw is false or the function has not been assigned any type, extra
  // heuristics are applied; returning TYPE_THUNK for functions with entry
  // point address 0, THUNK_IMPORTED if it has no basic blocks and
  // TYPE_STANDARD otherwise.
  // TODO(cblichmann): Split into two functions: GetType() and GetRawType().
  FunctionType GetType(bool raw) const;

  bool IsImported() const;

  std::string GetModuleName() const;
  void SetModuleName(const std::string& name);
  void SetName(const std::string& name, const std::string& demangled_name);
  std::string GetName(Name type) const;
  bool HasRealName() const;

  const Edges& GetEdges() const;
  const BasicBlocks& GetBasicBlocks() const;
  const BasicBlock* GetBasicBlockForAddress(Address address) const;

  void Render(std::ostream* stream, const CallGraph& call_graph,
              const FlowGraph& flow_graph) const;

  int GetLibraryIndex() const {
    return library_index_;
  }

  void SetLibraryIndex(int library_index) {
    library_index_ = library_index;
  }


 private:
  int GetBasicBlockIndexForAddress(Address address) const;
  BasicBlock* GetMutableBasicBlockForAddress(Address address);

  typedef std::unordered_set<std::string> StringCache;

  static StringCache string_cache_;
  static int instance_count_;

  Address entry_point_;
  BasicBlocks basic_blocks_;
  Edges edges_;
  std::string name_;
  std::string demangled_name_;
  const std::string* module_name_;
  FunctionType type_;
  int library_index_;
};

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_FUNCTION_H_
