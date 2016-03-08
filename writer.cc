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

#include "third_party/zynamics/binexport/writer.h"

#include <cinttypes>

#include "base/logging.h"
#include "base/stringprintf.h"
#include <binexport.pb.h>  // NOLINT
#include "third_party/zynamics/binexport/call_graph.h"
#include "third_party/zynamics/binexport/flow_graph.h"
#include "third_party/zynamics/binexport/function.h"
#include "third_party/zynamics/binexport/prime_signature.h"

namespace {

uint64_t GetPrime(const BasicBlock& basic_block) {
  uint64_t prime = 0;
  for (const auto& instruction : basic_block) {
    prime += bindetego::GetPrime(instruction.GetMnemonic());
  }
  return prime;
}

uint64_t GetPrime(const Function& function) {
  uint64_t prime = 0;
  for (const auto& basic_block_ptr : function.GetBasicBlocks()) {
    prime += GetPrime(*basic_block_ptr);
  }
  return prime;
}

}  // namespace

void WriteCallgraphToProto(const CallGraph& call_graph,
                           const FlowGraph& flow_graph,
                           BinExport::Callgraph* call_graph_proto,
                           bool full_export) {
  const Functions& functions = flow_graph.GetFunctions();
  for (Functions::const_iterator i = functions.begin(); i != functions.end();
       ++i) {  // vertices
    const Function& function = *i->second;
    BinExport::Callgraph::Vertex* vertex = call_graph_proto->add_vertices();
    vertex->set_address(function.GetEntryPoint());
    vertex->set_prime(GetPrime(function));
    vertex->set_function_type(
        static_cast<BinExport::Callgraph::Vertex::FunctionType>(
            function.GetType()));

    vertex->set_has_real_name(function.HasRealName());
    const std::string& mangled = function.GetName(Function::MANGLED);
    vertex->set_mangled_name(mangled);
    if (full_export) {
      const std::string& demangled = function.GetName(Function::DEMANGLED);
      if (mangled != demangled) vertex->set_demangled_name(demangled);
    }
  }

  // add call edges
  for (CallGraph::Edges::const_iterator i = call_graph.GetEdges().begin();
       i != call_graph.GetEdges().end(); ++i) {  // edges
    const EdgeInfo& edge = *i;
    BinExport::Callgraph::Edge* edgeProto = call_graph_proto->add_edges();
    edgeProto->set_source_function_address(edge.function_->GetEntryPoint());
    edgeProto->set_source_instruction_address(edge.source_);
    edgeProto->set_target_address(edge.target_);
  }
}

void WriteFlowgraphsToProto(const CallGraph& call_graph,
                            const FlowGraph& flow_graph,
                            const Function& function,
                            const AddressReferences& references,
                            Comments::const_iterator* comment,
                            BinExport::Flowgraph* flow_graph_proto,
                            bool full_export) {
  const auto& basic_blocks = function.GetBasicBlocks();
  const Function::Edges& edges = function.GetEdges();

  for (const auto& basic_block_ptr : basic_blocks) {  // basicblock
    BinExport::Flowgraph::Vertex* basic_block_proto =
        flow_graph_proto->add_vertices();
    basic_block_proto->set_prime(GetPrime(*basic_block_ptr));

    for (const auto& instruction : *basic_block_ptr) {  // instruction
      BinExport::Flowgraph::Vertex::Instruction* instruction_proto =
          basic_block_proto->add_instructions();
      const Address instruction_address = instruction.GetAddress();

      instruction_proto->set_address(instruction_address);
      instruction_proto->set_prime(
          bindetego::GetPrime(instruction.GetMnemonic()));
      instruction_proto->set_raw_bytes(instruction.GetBytes());

      if (const size_t string_hash =
          call_graph.GetStringReference(instruction_address))
        instruction_proto->set_string_reference(string_hash);

      AddressReference dummy(instruction_address, std::make_pair(-1, -1), 0,
                             TYPE_CALL_DIRECT);
      const AddressReferences::const_iterator reference =
          std::lower_bound(references.begin(), references.end(), dummy);
      for (AddressReferences::const_iterator i = reference,
          end = references.end();
          i != end && i->source_ == instruction_address; ++i) {
        if (!i->IsCall()) {
          continue;
        }
        instruction_proto->add_call_targets(i->target_);
      }

      if (full_export) {
        instruction_proto->set_mnemonic(instruction.GetMnemonic());
        instruction_proto->set_operands(
          RenderOperands(instruction, flow_graph, true));

        const Comments::const_iterator end = call_graph.GetComments().end();

        // in case a function has "tails", we need to jump back to
        // lower addresses
        if (*comment == end || (*comment)->address_ > instruction_address) {
          *comment = std::lower_bound(call_graph.GetComments().begin(),
                                      end,
                                      Comment(instruction_address, 0),
                                      &SortComments);
        }

        while (*comment != end && (*comment)->address_ < instruction_address) {
          ++*comment;
        }

        for (; *comment != end && (*comment)->address_ == instruction_address;
            ++*comment) {
          // TODO(user) Refactor this into multiple fields instead of a
          //     combined one.
          const Comment& comment_value = **comment;
          size_t flags = comment_value.repeatable_;   // repeatable flag low bit
          flags |= comment_value.type_ << 1;
          flags |= comment_value.operand_num_ << 16;  // operand id in high bits
          BinExport::Flowgraph::Vertex::Instruction::Comment* comment_proto =
              instruction_proto->add_comments();
          comment_proto->set_flags(flags);
          comment_proto->set_comment(*comment_value.comment_);
        }
      }
    }
  }

  for (const auto& edge : edges) {
    const auto* basic_block = function.GetBasicBlockForAddress(edge.source);
    if (!basic_block) {
      LOG(ERROR) << StringPrintf("Missing source basic block at %08" PRIx64
                                 " in %08" PRIx64,
                                 edge.source, function.GetEntryPoint());
      continue;
    }
    BinExport::Flowgraph::Edge* edge_proto = flow_graph_proto->add_edges();
    edge_proto->set_source_address(basic_block->GetEntryPoint());
    edge_proto->set_target_address(edge.target);
    edge_proto->set_type(
        static_cast<BinExport::Flowgraph::Edge::EdgeType>(edge.type));
  }

  flow_graph_proto->set_address(function.GetEntryPoint());
}

void WriteMetainformationToProto(const CallGraph& /* call_graph */,
                                 const FlowGraph& flow_graph,
                                 BinExport::Meta* meta_information,
                                 bool /* full_export */) {
  unsigned int num_functions = 0;
  unsigned int num_basicblocks = 0;
  unsigned int num_edges = 0;
  unsigned int num_instructions = 0;
  const Functions& functions = flow_graph.GetFunctions();
  for (Functions::const_iterator it = functions.begin(), end = functions.end();
       it != end; ++it) {
    const Function& function = *it->second;
    // skip library functions
    if (function.GetType() == Function::TYPE_LIBRARY) {
      continue;
    }

    num_functions++;
    num_edges += function.GetEdges().size();

    for (const auto& basic_block_ptr : function.GetBasicBlocks()) {
      num_basicblocks++;
      num_instructions += basic_block_ptr->GetInstructionCount();
    }
  }

  meta_information->set_num_functions(num_functions);
  meta_information->set_num_basicblocks(num_basicblocks);
  meta_information->set_num_edges(num_edges);
  meta_information->set_num_instructions(num_instructions);
}
