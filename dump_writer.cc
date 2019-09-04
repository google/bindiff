// Copyright 2011-2019 Google LLC. All Rights Reserved.
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

namespace security {
namespace binexport {

DumpWriter::DumpWriter(std::ostream& stream) : stream_(stream) {}

DumpWriter::DumpWriter(const std::string& file_name)
    : file_(file_name.c_str()), stream_(file_) {}

not_absl::Status DumpWriter::Write(const CallGraph& call_graph,
                                   const FlowGraph& flow_graph,
                                   const Instructions& instructions,
                                   const AddressReferences& address_references,
                                   const TypeSystem* type_system,
                                   const AddressSpace& address_space) {
// MOE:begin_strip
#ifdef GOOGLE
  // Store to an array and sort by MD index value for compatibility.
  const auto& functions = flow_graph.GetFunctions();
  std::vector<std::pair<double, Address>> md_indices;
  md_indices.reserve(functions.size());
  for (const auto& function : functions) {
    md_indices.emplace_back(function.second->GetMdIndex(), function.first);
  }
  std::sort(md_indices.begin(), md_indices.end(),
            [](const std::pair<double, Address>& p1,
               const std::pair<double, Address>& p2) {
              // Sort by MD index descending, then address ascending.
              return p1.first != p2.first ? p1.first > p2.first
                                          : p1.second < p2.second;
            });

  for (const auto& md_index_pair : md_indices) {
    stream_ << std::hex << std::setfill('0') << std::uppercase << std::setw(8)
            << md_index_pair.second << "\t"
            << std::setprecision(std::numeric_limits<double>::max_digits10)
            << std::setfill(' ') << std::setw(24) << md_index_pair.first
            << "\n";
  }
#endif  // GOOGLE
// MOE:end_strip

  stream_ << std::endl;
  call_graph.Render(&stream_, flow_graph);
  stream_ << std::endl;
  flow_graph.Render(&stream_, call_graph);
  stream_ << std::endl;
  return not_absl::OkStatus();
}

}  // namespace binexport
}  // namespace security
