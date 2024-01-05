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

#include "third_party/zynamics/bindiff/groundtruth_writer.h"

#include <fstream>
#include <iomanip>

#include "third_party/absl/status/status.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/bindiff/differ.h"
#include "third_party/zynamics/binexport/util/format.h"

namespace security::bindiff {

using binexport::FormatAddress;

GroundtruthWriter::GroundtruthWriter(const std::string& filename)
    : filename_(filename) {}

GroundtruthWriter::GroundtruthWriter(const std::string& filename,
                                     const FixedPointInfos& fixed_point_infos,
                                     const FlowGraphInfos& primary,
                                     const FlowGraphInfos& secondary)
    : filename_(filename),
      fixed_point_infos_(&fixed_point_infos),
      primary_(&primary),
      secondary_(&secondary) {}

absl::Status GroundtruthWriter::Write(const CallGraph& /* call_graph1 */,
                                      const CallGraph& /* call_graph2 */,
                                      const FlowGraphs& /* flow_graphs1 */,
                                      const FlowGraphs& /* flow_graphs2 */,
                                      const FixedPoints& fixed_points) {
  std::ofstream out_file(filename_.c_str());
  if (fixed_point_infos_) {
    // Note: Not using the arguments below but instead relying on the stored
    // fixed point infos. This allows to save from loaded results. But it is
    // not a very nice solution - this should instead load results fully and
    // make the two cases equivalent. This was not done originally to allow
    // loading large results generated with the 64bit stand alone differ.
    for (const FixedPointInfo& info : *fixed_point_infos_) {
      out_file << absl::StrCat(
          FormatAddress(info.primary), " ", FormatAddress(info.secondary), " ",
          *primary_->find(info.primary)->second.name, " ",
          *secondary_->find(info.secondary)->second.name, "\n");
    }
    return absl::OkStatus();
  }
  for (const FixedPoint& fixed_point : fixed_points) {
    const FlowGraph* primary = fixed_point.GetPrimary();
    const FlowGraph* secondary = fixed_point.GetSecondary();
    out_file << absl::StrCat(
        FormatAddress(primary->GetEntryPointAddress()), " ",
        FormatAddress(secondary->GetEntryPointAddress()), " ",
        primary->GetName(), " ", secondary->GetName(), "\n");
  }
  return absl::OkStatus();
}

}  // namespace security::bindiff
