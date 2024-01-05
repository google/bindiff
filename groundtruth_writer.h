// Copyright 2011-2024 Google LLC
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

// Save BinDiff results to a simple text based "ground truth" format for use in
// regression tests. This code is non-Google3 as it is being used from within
// the BinDiff IDA plugin, hence the use of std streams vs the Google3 file API.
//
// The output format is an ASCII text file containing a pair of hex addresses
// followed by a pair of function names per line.
// Example:
// 000472E0 00043C1C ERR_add_error_data ERR_add_error_data
// Basic block and instruction matches are ignored completely.

#ifndef GROUNDTRUTH_WRITER_H_
#define GROUNDTRUTH_WRITER_H_

#include <string>

#include "third_party/absl/status/status.h"
#include "third_party/zynamics/bindiff/reader.h"
#include "third_party/zynamics/bindiff/writer.h"

namespace security::bindiff {

// Writes a text file which is suitable for easy text comparison to a known good
// file.
class GroundtruthWriter : public Writer {
 public:
  explicit GroundtruthWriter(const std::string& filename);

  // Special constructor, only used in BinDiff plugin so we are able to save
  // from loaded results. Not used anywhere else.
  GroundtruthWriter(const std::string& filename,
                    const FixedPointInfos& fixed_point_infos,
                    const FlowGraphInfos& primary,
                    const FlowGraphInfos& secondary);

  absl::Status Write(const CallGraph& call_graph1, const CallGraph& call_graph2,
                     const FlowGraphs& flow_graphs1,
                     const FlowGraphs& flow_graphs2,
                     const FixedPoints& fixed_points) override;

 private:
  std::string filename_;
  const FixedPointInfos* fixed_point_infos_ = nullptr;
  const FlowGraphInfos* primary_ = nullptr;
  const FlowGraphInfos* secondary_ = nullptr;
};

}  // namespace security::bindiff

#endif  // GROUNDTRUTH_WRITER_H_
