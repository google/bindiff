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

#include "third_party/zynamics/binexport/dump_writer.h"

#include <iomanip>
#include <limits>

#include "third_party/zynamics/binexport/call_graph.h"
#include "third_party/zynamics/binexport/flow_graph.h"

DumpWriter::DumpWriter(std::ostream& stream) : stream_(stream) {}

DumpWriter::DumpWriter(const std::string& file_name)
    : file_(file_name.c_str()), stream_(file_) {}

util::Status DumpWriter::Write(const CallGraph& call_graph,
                               const FlowGraph& flow_graph,
                               const Instructions& instructions,
                               const AddressReferences& address_references,
                               const TypeSystem* type_system,
                               const AddressSpace& address_space) {

  stream_ << std::endl;
  call_graph.Render(&stream_, flow_graph);
  stream_ << std::endl;
  flow_graph.Render(&stream_, call_graph);
  stream_ << std::endl;
  return ::util::OkStatus();
}
