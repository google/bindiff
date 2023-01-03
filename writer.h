// Copyright 2011-2023 Google LLC
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

#ifndef WRITER_H_
#define WRITER_H_

#include <memory>

#include "third_party/zynamics/bindiff/fixed_points.h"
#include "third_party/zynamics/bindiff/flow_graph.h"
#include "third_party/zynamics/bindiff/graph_util.h"

namespace security::bindiff {

class Writer {
 public:
  virtual void Write(const CallGraph& call_graph1, const CallGraph& call_graph2,
                     const FlowGraphs& flow_graphs1,
                     const FlowGraphs& flow_graphs2,
                     const FixedPoints& fixed_points) = 0;
  virtual ~Writer() = default;

  Writer(const Writer&) = delete;
  Writer& operator=(const Writer&) = delete;

 protected:
  Writer() = default;
};

class ChainWriter : public Writer {
 public:
  virtual void Write(const CallGraph& call_graph1, const CallGraph& call_graph2,
                     const FlowGraphs& flow_graphs1,
                     const FlowGraphs& flow_graphs2,
                     const FixedPoints& fixed_points);

  void Add(std::unique_ptr<Writer> writer);
  bool IsEmpty() const;

 private:
  std::vector<std::unique_ptr<Writer>> writers_;
};

}  // namespace security::bindiff

#endif  // WRITER_H_
