// Copyright 2011-2020 Google LLC
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

#ifndef CALLGRAPH_H_
#define CALLGRAPH_H_

#include <iosfwd>
#include <set>

#include "third_party/absl/container/btree_set.h"
#include "third_party/absl/container/node_hash_map.h"
#include "third_party/absl/container/node_hash_set.h"
#include "third_party/zynamics/binexport/comment.h"
#include "third_party/zynamics/binexport/library_manager.h"
#include "third_party/zynamics/binexport/types.h"

class FlowGraph;
class Function;

struct EdgeInfo {
  explicit EdgeInfo(Function* function, Address source, Address target)
      : function_(function), source_(source), target_(target) {}

  Function* function_;
  Address source_;
  Address target_;
  int source_basic_block_id_ = -1;
};

class CallGraph {
 public:
  // This set should be sorted.
  using FunctionEntryPoints = absl::btree_set<Address>;

  using Edges = std::vector<EdgeInfo>;
  using StringReferences = absl::node_hash_map<Address, size_t>;

  CallGraph();

  CallGraph(const CallGraph&) = delete;
  CallGraph& operator=(const CallGraph&) = delete;

  ~CallGraph();

  void AddFunction(Address address);
  FunctionEntryPoints& GetFunctions();
  const FunctionEntryPoints& GetFunctions() const;
  void AddEdge(Address source, Address target);
  void ScheduleEdgeAdd(Function* function, Address source, Address target);
  void SortEdges();
  void CommitEdges();
  Edges::const_iterator GetEdges(Address source) const;
  const Edges& GetEdges() const;
  Edges& GetEdges();

  // Note that this method appends a comment to the end of the comment vector.
  // Other functions, in particular GetComments(Address), expect the vector to
  // be sorted. So be careful and re-sort comments after adding to them.
  void AddComment(Address address, size_t operand, const std::string& comment,
                  Comment::Type type, bool repeatable);
  const Comments& GetComments() const;
  Comments& GetComments();
  std::pair<Comments::const_iterator, Comments::const_iterator> GetComments(
      Address address) const;
  void AddStringReference(Address address, const std::string& ref);
  size_t GetStringReference(Address address) const;
  static const std::string* CacheString(const std::string& text);
  void Render(std::ostream* stream, const FlowGraph& flow_graph) const;
  int DeleteInvalidFunctions(FlowGraph* flow_graph);
  void PostProcessComments();

  LibraryManager* GetLibraryManager() { return &library_manager_; }
  const LibraryManager& GetLibraryManager() const { return library_manager_; }

 private:
  using StringCache = absl::node_hash_set<std::string>;
  void FoldComments();

  FunctionEntryPoints functions_;
  Edges edges_;
  Edges temp_edges_;
  Comments comments_;
  StringReferences string_references_;
  LibraryManager library_manager_;
  static StringCache string_cache_;
  static int instance_count_;
};

bool operator<(const EdgeInfo& one, const EdgeInfo& two);
bool operator==(const EdgeInfo& one, const EdgeInfo& two);

#endif  // CALLGRAPH_H_
