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

#include "third_party/zynamics/bindiff/writer.h"

namespace security::bindiff {

void ChainWriter::Write(const CallGraph& call_graph1,
                        const CallGraph& call_graph2,
                        const FlowGraphs& flow_graphs1,
                        const FlowGraphs& flow_graphs2,
                        const FixedPoints& fixed_points) {
  for (auto& writer : writers_) {
    writer->Write(call_graph1, call_graph2, flow_graphs1, flow_graphs2,
                  fixed_points);
  }
}

void ChainWriter::Add(std::shared_ptr<Writer> writer) {
  writers_.push_back(writer);
}

bool ChainWriter::IsEmpty() const { return writers_.empty(); }

}  // namespace security::bindiff
