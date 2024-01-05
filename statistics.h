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

#ifndef STATISTICS_H_
#define STATISTICS_H_

#include <array>
#include <cstdint>
#include <string>
#include <vector>

#include "third_party/absl/strings/string_view.h"

namespace security::bindiff {

class Counts {
 public:
  enum Kind {
    kBasicBlockMatchesLibrary = 0,
    kBasicBlockMatchesNonLibrary,
    kBasicBlocksPrimaryLibrary,
    kBasicBlocksPrimaryNonLibrary,
    kBasicBlocksSecondaryLibrary,
    kBasicBlocksSecondaryNonLibrary,
    kFlowGraphEdgeMatchesLibrary,
    kFlowGraphEdgeMatchesNonLibrary,
    kFlowGraphEdgesPrimaryLibrary,
    kFlowGraphEdgesPrimaryNonLibrary,
    kFlowGraphEdgesSecondaryLibrary,
    kFlowGraphEdgesSecondaryNonLibrary,
    kFunctionMatchesLibrary,
    kFunctionMatchesNonLibrary,
    kFunctionsPrimaryLibrary,
    kFunctionsPrimaryNonLibrary,
    kFunctionsSecondaryLibrary,
    kFunctionsSecondaryNonLibrary,
    kInstructionMatchesLibrary,
    kInstructionMatchesNonLibrary,
    kInstructionsPrimaryLibrary,
    kInstructionsPrimaryNonLibrary,
    kInstructionsSecondaryLibrary,
    kInstructionsSecondaryNonLibrary,

    kUiEntrySize,
    // These are not shown in the UI
    kBasicBlocksLibrary = kUiEntrySize,
    kBasicBlocksNonLibrary,
    kEdgesLibrary,
    kEdgesNonLibrary,
    kFunctionsLibrary,
    kFunctionsNonLibrary,
    kInstructionsLibrary,
    kInstructionsNonLibrary,

    kNumCountEntries,  // Always last
  };

  constexpr const uint64_t& operator[](Kind key) const {
    return counts_[static_cast<uint64_t>(key)];
  }

  constexpr uint64_t& operator[](Kind key) {
    return counts_[static_cast<uint64_t>(key)];
  }

  static constexpr absl::string_view GetDisplayName(Kind key) {
    switch (key) {
      case kBasicBlockMatchesLibrary:
        return "Basic Block Matches (Library)";
      case kBasicBlockMatchesNonLibrary:
        return "Basic Block Matches (Non-Library)";
      case kBasicBlocksLibrary:
        return "Basic Blocks (Library)";
      case kBasicBlocksNonLibrary:
        return "Basic Blocks (Non-Library)";
      case kBasicBlocksPrimaryLibrary:
        return "Basic Blocks Primary (Library)";
      case kBasicBlocksPrimaryNonLibrary:
        return "Basic Blocks Primary (Non-Library)";
      case kBasicBlocksSecondaryLibrary:
        return "Basic Blocks Secondary (Library)";
      case kBasicBlocksSecondaryNonLibrary:
        return "Basic Blocks Secondary (Non-Library)";
      case kEdgesLibrary:
        return "Edges (Library)";
      case kEdgesNonLibrary:
        return "Edges (Non-Library)";
      case kFlowGraphEdgeMatchesLibrary:
        return "Flow Graph Edge Matches (Library)";
      case kFlowGraphEdgeMatchesNonLibrary:
        return "Flow Graph Edge Matches (Non-Library)";
      case kFlowGraphEdgesPrimaryLibrary:
        return "Flow Graph Edges Primary (Library)";
      case kFlowGraphEdgesPrimaryNonLibrary:
        return "Flow Graph Edges Primary (Non-Library)";
      case kFlowGraphEdgesSecondaryLibrary:
        return "Flow Graph Edges Secondary (Library)";
      case kFlowGraphEdgesSecondaryNonLibrary:
        return "Flow Graph Edges Secondary (Non-Library)";
      case kFunctionMatchesLibrary:
        return "Function Matches (Library)";
      case kFunctionMatchesNonLibrary:
        return "Function Matches (Non-Library)";
      case kFunctionsLibrary:
        return "Functions (Library)";
      case kFunctionsNonLibrary:
        return "Functions (Non-Library)";
      case kFunctionsPrimaryLibrary:
        return "Functions Primary (Library)";
      case kFunctionsPrimaryNonLibrary:
        return "Functions Primary (Non-Library)";
      case kFunctionsSecondaryLibrary:
        return "Functions Secondary (Library)";
      case kFunctionsSecondaryNonLibrary:
        return "Functions Secondary (Non-Library)";
      case kInstructionMatchesLibrary:
        return "Instruction Matches (Library)";
      case kInstructionMatchesNonLibrary:
        return "Instruction Matches (Non-Library)";
      case kInstructionsLibrary:
        return "Instructions (Library)";
      case kInstructionsNonLibrary:
        return "Instructions (Non-Library)";
      case kInstructionsPrimaryLibrary:
        return "Instructions Primary (Library)";
      case kInstructionsPrimaryNonLibrary:
        return "Instructions Primary (Non-Library)";
      case kInstructionsSecondaryLibrary:
        return "Instructions Secondary (Library)";
      case kInstructionsSecondaryNonLibrary:
        return "Instructions Secondary (Non-Library)";
      case kNumCountEntries:
        break;
    }
    return "";
  }

  std::pair<absl::string_view, uint64_t> GetEntry(int index) const {
    if (index >= 0 && index < kNumCountEntries) {
      return {GetDisplayName(static_cast<Kind>(index)), counts_[index]};
    }
    return {"", 0};
  }

  void clear() { counts_.fill(0); }

  static constexpr size_t ui_entry_size() { return kUiEntrySize; }

 private:
  std::array<uint64_t, kNumCountEntries> counts_ = {0};
};

}  // namespace security::bindiff

#endif  // STATISTICS_H_
