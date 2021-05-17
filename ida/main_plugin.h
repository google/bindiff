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

#ifndef IDA_MAIN_PLUGIN_H_
#define IDA_MAIN_PLUGIN_H_

#include <string>

#include "third_party/zynamics/binexport/ida/plugin.h"

namespace security::binexport {

class Plugin : public IdaPlugin<Plugin> {
 public:
  static constexpr char kComment[] =
      "Export to SQL RE-DB, BinDiff binary or text dump";
  static constexpr char kHotKey[] = "";

  LoadStatus Init() override;
  bool Run(size_t argument) override;
  void Terminate() override;

  bool alsologtostderr() const { return alsologtostderr_; }
  const std::string& log_filename() const { return log_filename_; }

  bool x86_noreturn_heuristic() const { return x86_noreturn_heuristic_; }

 private:
  bool alsologtostderr_ = false;
  std::string log_filename_;

  // Whether to use an X86-specific heuristic to identify functions that do not
  // return. See FlowGraph::FindBasicBlockBreaks() for details.
  bool x86_noreturn_heuristic_ = false;
};

}  // namespace security::binexport

#endif  // IDA_MAIN_PLUGIN_H_
