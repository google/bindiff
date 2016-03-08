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

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_WRITER_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_WRITER_H_

#include "third_party/zynamics/binexport/address_references.h"
#include "third_party/zynamics/binexport/comment.h"
#include "third_party/zynamics/binexport/instruction.h"
#include "util/task/status.h"

class BasicBlock;
class CallGraph;
class FlowGraph;
class Function;
class TypeSystem;

namespace BinExport {
class Callgraph;
class Flowgraph;
class Meta;
};

class Writer {
 public:
  Writer() = default;
  virtual ~Writer() = default;

  virtual util::Status Write(const CallGraph& call_graph,
                             const FlowGraph& flow_graph,
                             const detego::Instructions& instructions,
                             const AddressReferences& address_references,
                             const TypeSystem* type_system,
                             const AddressSpace& address_space) = 0;

 private:
  Writer(const Writer&) = delete;
  const Writer& operator=(const Writer&) = delete;
};

// Stores the call graph in a proto buffer as used in the vxclass .bindiff
// bigtable and the .BinExport binary format.
// full_export controls whether demangled function names should be saved in
// addition to mangled names. The bigtable doesn't need them and thus can be
// more compact.
void WriteCallgraphToProto(const CallGraph& call_graph,
                           const FlowGraph& flow_graph,
                           BinExport::Callgraph* call_graph_proto,
                           bool full_export);

// Stores all flow graphs in a proto buffer as used in the vxclass .bindiff
// bigtable and the .BinExport binary format.
// full_export controls whether mnemonics, operands and comments should be
// saved. These are not required for vxclass use and thus can be omitted from
// the bigtable. Note that even without the mnemonics we still have a unique
// "prime" identifying what instruction it is. So instruction matching still
// works.
void WriteFlowgraphsToProto(const CallGraph& call_graph,
                            const FlowGraph& flow_graph,
                            const Function& function,
                            const AddressReferences& references,
                            Comments::const_iterator* comment,
                            BinExport::Flowgraph* flow_graph_proto,
                            bool full_export);

// Stores the parts of the BinExport::Meta proto buffer that are shared between
// the vxclass bigtable and the .BinExport binary file. These are the
// instruction, function, basicblock and edge counts. The rest of the data
// must be filled in by the caller because it is not available within this
// function and differs between vxclass and binary use anyways.
void WriteMetainformationToProto(const CallGraph& call_graph,
                                 const FlowGraph& flow_graph,
                                 BinExport::Meta* meta_information,
                                 bool full_export);

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_WRITER_H_
