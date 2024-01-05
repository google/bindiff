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

#ifndef IDA_MAIN_PLUGIN_H_
#define IDA_MAIN_PLUGIN_H_

#include <cstdint>
#include <memory>
#include <string>

#include "third_party/zynamics/bindiff/ida/results.h"
#include "third_party/zynamics/binexport/ida/plugin.h"

namespace security::bindiff {

class Plugin : public binexport::IdaPlugin<Plugin> {
 public:
  enum ResultFlags {
    kResultsShowMatched = 1 << 0,
    kResultsShowStatistics = 1 << 1,
    kResultsShowPrimaryUnmatched = 1 << 2,
    kResultsShowSecondaryUnmatched = 1 << 3,
    kResultsShowAll = 0xffffffff
  };

  enum class DiscardResultsKind {
    kDontSave,
    kAskSave,
    kAskSaveCancellable,
  };

  static constexpr char kComment[] =
      "Structural comparison of executable objects";  // Status line
  static constexpr char kHotKey[] = "CTRL-6";

  Results* results() { return results_.get(); }

  bool alsologtostderr() const { return alsologtostderr_; }
  const std::string& log_filename() const { return log_filename_; }

  absl::Status ClearResults();
  bool LoadResults();
  void ShowResults(ResultFlags flags);

  void VisualDiff(uint32_t index, bool call_graph_diff);

  bool DiscardResults(DiscardResultsKind kind);

  bool Init() override;
  void InitActions();
  void InitMenus();

  bool Run(size_t argument) override;

  void Terminate() override;
  void TermMenus();

 private:
  bool init_done_ = false;
  bool alsologtostderr_ = false;
  std::string log_filename_;
  std::unique_ptr<Results> results_;
};

}  // namespace security::bindiff

#endif  // IDA_MAIN_PLUGIN_H_
