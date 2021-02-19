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

#ifndef BINEXPORT2_WRITER_H_
#define BINEXPORT2_WRITER_H_

#include "third_party/zynamics/binexport/writer.h"

class BinExport2;

namespace security::binexport {

class BinExport2Writer : public Writer {
 public:
  // Note: This writer expects executable_hash to be hex encoded, not the raw
  //       bytes of the digest.
  BinExport2Writer(const std::string& result_filename,
                   const std::string& executable_filename,
                   const std::string& executable_hash,
                   const std::string& architecture);

  absl::Status Write(const CallGraph& call_graph, const FlowGraph& flow_graph,
                     const Instructions& instructions,
                     const AddressReferences& address_references,
                     const TypeSystem*,
                     const AddressSpace& address_space) override;

  absl::Status WriteToProto(const CallGraph& call_graph,
                            const FlowGraph& flow_graph,
                            const Instructions& instructions,
                            const AddressReferences& address_references,
                            const AddressSpace& address_space,
                            BinExport2* proto) const;

 private:
  std::string filename_;
  std::string executable_filename_;
  std::string executable_hash_;
  std::string architecture_;
};

}  // namespace security::binexport

#endif  // BINEXPORT2_WRITER_H_
