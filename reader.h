// Copyright 2011-2020 Google LLC
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

#ifndef READER_H_
#define READER_H_

#include "third_party/absl/container/btree_map.h"
#include "third_party/absl/container/btree_set.h"
#include "third_party/zynamics/bindiff/fixed_points.h"
#include "third_party/zynamics/bindiff/graph_util.h"
#include "third_party/zynamics/binexport/types.h"

namespace security::bindiff {

struct FlowGraphInfo {
  Address address;
  const std::string* name;
  const std::string* demangled_name;
  int basic_block_count;
  int edge_count;
  int instruction_count;
};
using FlowGraphInfos = absl::btree_map<Address, FlowGraphInfo>;

struct FixedPointInfo {
  Address primary;
  Address secondary;
  int basic_block_count;
  int edge_count;
  int instruction_count;
  double similarity;
  double confidence;
  int flags;
  const std::string* algorithm;
  bool evaluate;
  bool comments_ported;

  friend bool operator==(const FixedPointInfo& lhs, const FixedPointInfo& rhs) {
    return lhs.primary == rhs.primary && lhs.secondary == rhs.secondary &&
           lhs.basic_block_count == rhs.basic_block_count &&
           lhs.edge_count == rhs.edge_count &&
           lhs.instruction_count == rhs.instruction_count &&
           lhs.similarity == rhs.similarity &&
           lhs.confidence == rhs.confidence && lhs.flags == rhs.flags &&
           lhs.algorithm == rhs.algorithm && lhs.evaluate == rhs.evaluate &&
           lhs.comments_ported == rhs.comments_ported;
  }

  template <typename H>
  friend H AbslHashValue(H h, const FixedPointInfo& info) {
    return H::combine(std::move(h), info.primary, info.secondary,
                      info.basic_block_count, info.edge_count,
                      info.instruction_count, info.confidence, info.flags,
                      info.algorithm, info.evaluate, info.comments_ported);
  }

  bool IsManual() const;
};
using FixedPointInfos = absl::btree_set<FixedPointInfo>;

bool operator<(const FixedPointInfo& one, const FixedPointInfo& two);

class Reader {
 public:
  explicit Reader();
  virtual ~Reader() = default;

  Reader(const Reader&) = delete;
  Reader& operator=(const Reader&) = delete;

  virtual void Read(CallGraph& call_graph1, CallGraph& call_graph2,
                    FlowGraphInfos& flow_graphs1, FlowGraphInfos& flow_graphs2,
                    FixedPointInfos& fixed_points) = 0;

  double GetSimilarity() const;
  double GetConfidence() const;

 protected:
  double similarity_;
  double confidence_;
};

}  // namespace security::bindiff

#endif  // READER_H_
