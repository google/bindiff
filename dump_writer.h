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

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_DUMP_WRITER_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_DUMP_WRITER_H_

#include "third_party/zynamics/binexport/writer.h"

#include <fstream>

class DumpWriter : public Writer {
 public:
  explicit DumpWriter(std::ostream& stream);
  explicit DumpWriter(const std::string& file_name);

  util::Status Write(const CallGraph& call_graph, const FlowGraph& flow_graph,
                     const Instructions& instructions,
                     const AddressReferences& address_references,
                     const TypeSystem* type_system,
                     const AddressSpace& address_space) override;

 private:
  std::ofstream file_;
  std::ostream& stream_;
};

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_DUMP_WRITER_H_
