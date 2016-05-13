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

#include "statistics_writer.h"

#include <iomanip>
#include <map>
#include <string>

#include "third_party/zynamics/binexport/call_graph.h"
#include "third_party/zynamics/binexport/flow_graph.h"

StatisticsWriter::StatisticsWriter(std::ostream& stream) : stream_(stream) {}

StatisticsWriter::StatisticsWriter(const std::string& filename)
    : file_(filename.c_str()), stream_(file_) {}

void StatisticsWriter::GenerateStatistics(
    const CallGraph& call_graph, const FlowGraph& flow_graph,
    std::map<std::string, size_t>* statistics_ptr) const {
  std::map<std::string, size_t>& statistics = *statistics_ptr;
  statistics.clear();
  const Functions& functions = flow_graph.GetFunctions();
  statistics["callgraph nodes (functions)"] = functions.size();
  statistics["callgraph edges (calls)"] = call_graph.GetEdges().size();

  static const char* kFunctionTypeNames[] = {
      "functions (standard)", "functions (library)", "functions (imported)",
      "functions (thunk)", "functions (invalid)"};
  statistics[kFunctionTypeNames[0]] = 0;
  statistics[kFunctionTypeNames[1]] = 0;
  statistics[kFunctionTypeNames[2]] = 0;
  statistics[kFunctionTypeNames[3]] = 0;
  statistics[kFunctionTypeNames[4]] = 0;

  static const char* kEdgeTypeNames[] = {
      "flowgraph edges (true)", "flowgraph edges (false)",
      "flowgraph edges (unconditional)", "flowgraph edges (switch)"};
  statistics[kEdgeTypeNames[0]] = 0;
  statistics[kEdgeTypeNames[1]] = 0;
  statistics[kEdgeTypeNames[2]] = 0;
  statistics[kEdgeTypeNames[3]] = 0;

  statistics["functions with real name"] = 0;
  statistics["instructions"] = 0;

  size_t basic_block_count = 0;
  size_t edge_count = 0;
  for (Functions::const_iterator i = functions.begin(); i != functions.end();
       ++i) {
    const Function& function = *i->second;
    assert(function.GetType(false) != Function::TYPE_NONE);
    statistics[kFunctionTypeNames[function.GetType(false)]]++;
    statistics["functions with real name"] += function.HasRealName();

    const auto& basic_blocks = function.GetBasicBlocks();
    const auto& edges = function.GetEdges();
    basic_block_count += basic_blocks.size();
    edge_count += edges.size();

    for (const auto k : basic_blocks) {
      const BasicBlock& basic_block = *k;
      statistics["instructions"] += basic_block.GetInstructionCount();
      for (const auto& instruction : basic_block) {
        statistics["instructions " + instruction.GetMnemonic()]++;
      }
    }

    for (Function::Edges::const_iterator j = edges.begin(); j != edges.end();
         ++j) {
      const FlowGraphEdge& edge = *j;
      assert(edge.type > 0 && edge.type < 5);
      statistics[kEdgeTypeNames[edge.type - 1]]++;
    }
  }

  statistics["flowgraph nodes (basicblocks)"] = basic_block_count;
  statistics["flowgraph edges"] = edge_count;
}

util::Status StatisticsWriter::Write(const CallGraph& call_graph,
                                     const FlowGraph& flow_graph,
                                     const Instructions& /* instructions */,
                                     const AddressReferences&,
                                     const TypeSystem* /* type_system */,
                                     const AddressSpace& /* address_space */) {
  std::map<std::string, size_t> statistics;
  GenerateStatistics(call_graph, flow_graph, &statistics);
  for (std::map<std::string, size_t>::const_iterator i = statistics.begin();
       i != statistics.end(); ++i) {
    const std::string padding(32 - i->first.size(), '.');
    stream_ << i->first << padding << ":" << std::setw(7) << std::dec
            << std::setfill(' ') << i->second << std::endl;
  }

  return util::Status::OK;
}
