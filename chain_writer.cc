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

#include "third_party/zynamics/binexport/chain_writer.h"

ChainWriter::Writers& ChainWriter::GetWriters() { return writers_; }

void ChainWriter::Clear() { Writers().swap(writers_); }

void ChainWriter::AddWriter(std::shared_ptr<Writer> writer) {
  writers_.push_back(writer);
}

util::Status ChainWriter::Write(const CallGraph& call_graph,
                                const FlowGraph& flow_graph,
                                const Instructions& instructions,
                                const AddressReferences& address_references,
                                const TypeSystem* type_system,
                                const AddressSpace& address_space) {
  bool success = true;
  for (auto& writer : writers_) {
    if (!writer->Write(call_graph, flow_graph, instructions, address_references,
                       type_system, address_space)
             .ok()) {
      success = false;
    }
  }
  return success ? ::util::OkStatus()
                 : util::Status(util::error::UNKNOWN,
                                "At least one of the chained writers failed.");
}
