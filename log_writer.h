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

#ifndef LOG_WRITER_H_
#define LOG_WRITER_H_

#include <string>

#include "third_party/absl/status/status.h"
#include "third_party/zynamics/bindiff/writer.h"

namespace security::bindiff {

// Writes a human readable log file for debugging purposes.
class ResultsLogWriter : public Writer {
 public:
  explicit ResultsLogWriter(const std::string& filename);

  absl::Status Write(const CallGraph& call_graph1, const CallGraph& call_graph2,
                     const FlowGraphs& flow_graphs1,
                     const FlowGraphs& flow_graphs2,
                     const FixedPoints& fixed_points) override;

 private:
  std::string filename_;
};

}  // namespace security::bindiff

#endif  // LOG_WRITER_H_
