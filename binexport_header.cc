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

#include "third_party/zynamics/binexport/binexport_header.h"

#include <algorithm>
#include <stdexcept>


BinExportHeader::BinExportHeader(uint32_t num_flow_graphs)
    : meta_offset(0),
      call_graph_offset(0),
      num_flow_graphs(num_flow_graphs),
      flow_graph_offsets(num_flow_graphs, {0, 0}) {}

BinExportHeader::BinExportHeader()
    : BinExportHeader(0 /* num_flow_graphs */) {}

BinExportHeader BinExportHeader::ParseFromStream(std::istream* file) {
  if (!file || file->fail()) {
    throw std::runtime_error("invalid .BinExport input file");
  }

  BinExportHeader result;
  file->read(reinterpret_cast<char*>(&result.meta_offset),
             sizeof(result.meta_offset))
      .read(reinterpret_cast<char*>(&result.call_graph_offset),
            sizeof(result.call_graph_offset));
  if (!result.meta_offset || !result.call_graph_offset) {
    throw std::runtime_error("invalid .BinExport input file");
  }

  file->read(reinterpret_cast<char*>(&result.num_flow_graphs),
             sizeof(result.num_flow_graphs));
  result.flow_graph_offsets.reserve(
      std::min(static_cast<uint32_t>(10000000 /* 10M flow graphs */),
               result.num_flow_graphs + 1));
  for (uint32_t i = 0; i < result.num_flow_graphs; ++i) {
    FlowgraphOffset offset = {0, 0};
    file->read(reinterpret_cast<char*>(&offset.address), sizeof(offset.address))
        .read(reinterpret_cast<char*>(&offset.offset), sizeof(offset.offset));
    result.flow_graph_offsets.push_back(offset);
  }
  const auto pos = file->tellg();
  file->seekg(0, std::ios_base::end);
  result.flow_graph_offsets.push_back(
      {0, static_cast<uint32_t>(file->tellg())});
  file->seekg(pos, std::ios_base::beg);
  if (file->fail()) {
    throw std::runtime_error("invalid .BinExport input file");
  }
  return result;
}

void BinExportHeader::SerializeToOstream(std::ostream* file) const {
  file->write(reinterpret_cast<const char*>(&meta_offset), sizeof(meta_offset))
      .write(reinterpret_cast<const char*>(&call_graph_offset),
             sizeof(call_graph_offset))
      .write(reinterpret_cast<const char*>(&num_flow_graphs),
             sizeof(num_flow_graphs));
  for (const auto& offset : flow_graph_offsets) {
    file->write(reinterpret_cast<const char*>(&offset.address),
                sizeof(offset.address))
        .write(reinterpret_cast<const char*>(&offset.offset),
               sizeof(offset.offset));
  }
  if (!file || file->fail()) {
    throw std::runtime_error("I/O error writing to file");
  }
}
