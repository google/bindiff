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

#include "third_party/zynamics/binexport/function.h"

#include <cinttypes>
#include <iomanip>
#include <iostream>
#include <ostream>
#include <sstream>

#include <boost/graph/compressed_sparse_row_graph.hpp>  // NOLINT
#include <boost/graph/dominator_tree.hpp>               // NOLINT
#include <boost/graph/iteration_macros.hpp>             // NOLINT

#include "base/logging.h"
#include "base/stringprintf.h"
#include "third_party/zynamics/binexport/call_graph.h"

int Function::instance_count_ = 0;
Function::StringCache Function::string_cache_;

Function::Function(Address entry_point)
    : entry_point_(entry_point),
      module_name_(nullptr),
      type_(TYPE_NONE),
      library_index_(-1) {
  ++instance_count_;
}

Function::Function(const Function& other)
    : entry_point_(other.entry_point_),
      basic_blocks_(other.basic_blocks_),
      edges_(other.edges_),
      name_(other.name_),
      demangled_name_(other.demangled_name_),
      module_name_(other.module_name_),
      type_(other.type_),
      library_index_(other.library_index_) {
  ++instance_count_;
}

Function::~Function() {
  if (--instance_count_ == 0) {
    StringCache().swap(string_cache_);
    BasicBlock::DestroyCache();
  }
}

void Function::Render(std::ostream* stream, const CallGraph& call_graph,
                      const FlowGraph& flow_graph) const {
  *stream << std::hex << std::setfill('0') << std::uppercase << std::setw(8)
          << GetEntryPoint() << "    " << GetModuleName()
          << (GetModuleName().empty() ? "" : ".") << GetName(DEMANGLED) << "\n";
  for (const auto& basic_block_ptr : basic_blocks_) {
    basic_block_ptr->Render(stream, call_graph, flow_graph);
    *stream << "\n";
  }

  for (const auto& edge : edges_) {
    *stream << std::hex << std::setfill('0') << std::uppercase << std::setw(8)
            << edge.source << " -> " << std::hex << std::setfill('0')
            << std::uppercase << std::setw(8) << edge.target << " "
            << edge.GetTypeName() << "\n";
  }
  if (!edges_.empty()) {
    *stream << "\n";
  }

  *stream << "---------------------------\n";
}

Address Function::GetEntryPoint() const { return entry_point_; }

void Function::AddBasicBlock(BasicBlock* basic_block) {
  basic_blocks_.push_back(basic_block);
}

void Function::SortGraph() {
  std::sort(basic_blocks_.begin(), basic_blocks_.end(),
            [](const BasicBlock* one, const BasicBlock* two) {
              return one->GetEntryPoint() < two->GetEntryPoint();
            });
  basic_blocks_.erase(std::unique(basic_blocks_.begin(), basic_blocks_.end()),
                      basic_blocks_.end());

  std::sort(edges_.begin(), edges_.end());
}

void Function::Clear() {
  basic_blocks_.clear();
  edges_.clear();
}

void Function::AddEdge(const FlowGraphEdge& edge) { edges_.push_back(edge); }

void Function::SetType(FunctionType type) { type_ = type; }

Function::FunctionType Function::GetType(bool raw) const {
  if ((type_ != TYPE_NONE) || raw) {
    return type_;
  }
  if (GetEntryPoint() == 0) {
    return TYPE_THUNK;
  }
  if (basic_blocks_.empty()) {
    return TYPE_IMPORTED;
  }
  return TYPE_STANDARD;
}

const char* Function::GetTypeName(FunctionType type) {
  switch (type) {
    case TYPE_STANDARD:
      return "regular";
    case TYPE_LIBRARY:
      return "library";
    case TYPE_IMPORTED:
      return "imported";
    case TYPE_THUNK:
      return "thunk";
    case TYPE_INVALID:
      return "invalid";
    case TYPE_NONE:
    default:
      return "none";
  }
}

void Function::SetName(const std::string& name,
                       const std::string& demangled_name) {
  name_ = name;
  if (name != demangled_name) {
    demangled_name_ = demangled_name;
  } else {
    demangled_name_ = std::string();
  }
}

std::string Function::GetName(Name type) const {
  if (HasRealName()) {
    return type == MANGLED || demangled_name_.empty() ? name_ : demangled_name_;
  }

  std::stringstream stream;
  stream << "sub_" << std::hex << std::uppercase << GetEntryPoint();
  return stream.str();
}

bool Function::HasRealName() const { return !name_.empty(); }

const Function::Edges& Function::GetEdges() const { return edges_; }

const BasicBlocks& Function::GetBasicBlocks() const { return basic_blocks_; }

BasicBlock* Function::GetMutableBasicBlockForAddress(Address address) {
  const int index = GetBasicBlockIndexForAddress(address);
  if (index < basic_blocks_.size()) {
    return basic_blocks_[index];
  }
  return nullptr;
}

const BasicBlock* Function::GetBasicBlockForAddress(Address address) const {
  return const_cast<Function*>(this)->GetMutableBasicBlockForAddress(address);
}

int Function::GetBasicBlockIndexForAddress(Address address) const {
  if (basic_blocks_.empty()) {
    return basic_blocks_.size();
  }

  auto pivot =
      std::lower_bound(basic_blocks_.begin(), basic_blocks_.end(), address,
                       [](const BasicBlock* basic_block, Address address) {
                         return basic_block->GetEntryPoint() < address;
                       });
  if (pivot != basic_blocks_.end() && (*pivot)->GetEntryPoint() == address) {
    return std::distance(basic_blocks_.begin(), pivot);
  }

  BasicBlocks::const_reverse_iterator left(pivot);
  BasicBlocks::const_iterator right(pivot);
  for (; left != basic_blocks_.rend() || right != basic_blocks_.end();) {
    if (left != basic_blocks_.rend()) {
      const auto* basic_block = *left;
      if (basic_block->GetLastAddress() == address ||
          basic_block->GetInstruction(address) != basic_block->end()) {
        return basic_blocks_.size() - 1 -
               std::distance(basic_blocks_.rbegin(), left);
      }
      ++left;
    }
    if (right != basic_blocks_.end()) {
      const auto* basic_block = *right;
      if (basic_block->GetLastAddress() == address ||
          basic_block->GetInstruction(address) != basic_block->end()) {
        return std::distance(basic_blocks_.begin(), right);
      }
      ++right;
    }
  }

  LOG(WARNING) << StringPrintf("No basic block for %08" PRIx64 " in %08" PRIx64,
                               address, GetEntryPoint());

  return basic_blocks_.size();
}

bool Function::IsImported() const { return GetType(false) == TYPE_IMPORTED; }

std::string Function::GetModuleName() const {
  return module_name_ ? *module_name_ : "";
}

void Function::SetModuleName(const std::string& name) {
  module_name_ = &*string_cache_.insert(name).first;
}

// We'll sometimes get branch instructions with a branch target that's outside
// valid memory. This leads to address references in the address_references
// set but missing flow graph edges. We check for this type of inconsistency
// here.
void Function::FixEdges() {
  edges_.erase(std::remove_if(edges_.begin(), edges_.end(),
                              [this](const FlowGraphEdge& edge) {
                                const bool source_missing =
                                    !GetBasicBlockForAddress(edge.source);
                                const bool target_missing =
                                    !GetBasicBlockForAddress(edge.target);
                                return source_missing || target_missing;
                              }),
               edges_.end());
}

void Function::GetBackEdges(
    std::vector<Edges::const_iterator>* back_edges) const {
  typedef boost::compressed_sparse_row_graph<
      boost::bidirectionalS, boost::property<boost::vertex_index_t, int>,
      boost::property<boost::edge_index_t, int>> LocalGraph;
  typedef boost::graph_traits<LocalGraph>::vertex_descriptor LocalVertex;
  typedef std::pair<LocalVertex, LocalVertex> LocalEdge;
  typedef std::vector<LocalEdge> LocalEdges;

  back_edges->clear();
  LocalEdges local_edges;
  local_edges.reserve(edges_.size());
  for (const auto& edge : edges_) {
    auto source_index = GetBasicBlockIndexForAddress(edge.source);
    CHECK(source_index < basic_blocks_.size() &&
          basic_blocks_[source_index]->GetLastAddress() == edge.source);
    auto target_index = GetBasicBlockIndexForAddress(edge.target);
    CHECK(target_index < basic_blocks_.size() &&
          basic_blocks_[target_index]->GetEntryPoint() == edge.target);
    local_edges.emplace_back(source_index, target_index);
  }
  // TODO(user) Use edges_are_sorted (they are, after all, already sorted
  // by source index)
  const LocalGraph graph(boost::edges_are_unsorted_multi_pass,
                         local_edges.begin(), local_edges.end(),
                         basic_blocks_.size());
  std::vector<LocalVertex> dominator_nodes(
      num_vertices(graph), boost::graph_traits<LocalGraph>::null_vertex());
  boost::lengauer_tarjan_dominator_tree(
      graph,
      boost::vertex(GetBasicBlockIndexForAddress(GetEntryPoint()), graph),
      make_iterator_property_map(dominator_nodes.begin(),
                                 get(boost::vertex_index, graph)));
  BGL_FORALL_EDGES(edge, graph, LocalGraph) {
    const auto source = boost::source(edge, graph);
    const auto target = boost::target(edge, graph);
    bool is_loop = source == target;  // Self edges are always loops.
    if (!is_loop) {
      // Traverse the Dominator Tree until we either reach a non-dominated node
      // or arrive back at the node we started from.
      for (auto node = dominator_nodes[source];
           node != boost::graph_traits<LocalGraph>::null_vertex();
           node = dominator_nodes[node]) {
        if (node == target) {
          is_loop = true;
          break;
        }
      }
    }

    if (is_loop) {
      const auto edge_index = get(boost::edge_index, graph, edge);
      auto detego_edge = edges_.begin() + edge_index;
      const auto source_address = basic_blocks_[source]->GetLastAddress();
      const auto target_address = basic_blocks_[target]->GetEntryPoint();
      if (detego_edge->source != source_address ||
          detego_edge->target != target_address) {
        // TODO(user) Figure out what's going on here. Most of the time
        //     edge.idx will point to the correct original edge. However,
        //     sometimes it does not, in which case I perform this expensive
        //     search instead.
        detego_edge = std::lower_bound(
            edges_.begin(), edges_.end(),
            FlowGraphEdge(source_address, target_address,
                          FlowGraphEdge::TYPE_TRUE),
            [](const FlowGraphEdge& one, const FlowGraphEdge& two) {
              return one.source == two.source ? one.target < two.target
                                              : one.source < two.source;
            });
      }
      CHECK(detego_edge->source == source_address);
      CHECK(detego_edge->target == target_address);
      back_edges->emplace_back(detego_edge);
    }
  }
}
