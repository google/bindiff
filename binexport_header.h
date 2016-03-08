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

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_BINEXPORT_HEADER_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_BINEXPORT_HEADER_H_

#include <stdint.h>
#include <iostream>  // NOLINT(readability/streams)
#include <vector>

#ifdef GOOGLE
#include "util/task/statusor.h"
#endif

class File;

// A custom header written as a prefix to every .BinExport file. See
// binexport.proto for an explanation. Please note that this file is supposed to
// be portable.
struct BinExportHeader {
  explicit BinExportHeader(uint32_t num_flow_graphs);
  BinExportHeader();

  // Load the header of a .BinExport file from a regular std::istream. This is
  // needed for compatibility with the rest of the zynamics codebase.
  static BinExportHeader ParseFromStream(std::istream* file);

  // Write the header to a regular std::ostream. Throws on error.
  void SerializeToOstream(std::ostream* file) const;


  struct FlowgraphOffset {
    uint64_t address;
    uint32_t offset;
  };

  uint32_t meta_offset;
  uint32_t call_graph_offset;
  uint32_t num_flow_graphs;
  std::vector<FlowgraphOffset> flow_graph_offsets;
};

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_BINEXPORT_HEADER_H_

