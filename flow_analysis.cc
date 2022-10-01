// Copyright 2011-2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#include "third_party/zynamics/binexport/flow_analysis.h"

#include <exception>
#include <iostream>
#include <map>
#include <set>

#include "third_party/zynamics/binexport/call_graph.h"
#include "third_party/zynamics/binexport/flow_graph.h"
#include "third_party/zynamics/binexport/virtual_memory.h"

namespace {

void UpdateInstructionInDegree(Instructions* instructions,
                               const FlowGraph& flow_graph) {
  // Increase in-degree count of normal flow instructions.
  for (auto i = instructions->begin(); i != instructions->end(); ++i) {
    auto j = GetNextInstruction(instructions, i);
    // If the instruction flows then add an edge to the following instruction.
    if (j != instructions->end()) {
      j->AddInEdge();
    }
  }

  // Increase in-degree count of jmp targets.
  for (const auto& edge : flow_graph.GetEdges()) {
    auto j = GetInstruction(instructions, edge.target);
    if (j != instructions->end()) {
      j->AddInEdge();
    } else {
      // TODO(soerenme) When does this happen? If we have hit a stop block
      // instruction? Otherwise all target instructions should be valid and in
      // here...
    }
  }
}

}  // namespace

void ReconstructFlowGraph(Instructions* instructions,
                          const FlowGraph& flow_graph,
                          CallGraph* call_graph) {
  UpdateInstructionInDegree(instructions, flow_graph);

  // Add all instructions with an in-degree count of 0 as function entry points.
  for (const auto& instruction : *instructions) {
    // The IsPossibleFunction check should only be necessary for IDA
    // and shouldn't be required anyways if we have a valid instruction...
    if (!instruction.HasFlag(FLAG_INVALID) && instruction.GetInDegree() == 0) {
      call_graph->AddFunction(instruction.GetAddress());
    }
  }
}
