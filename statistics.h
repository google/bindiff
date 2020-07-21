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
        return "basicBlock matches (library)";
      case kBasicBlockMatchesNonLibrary:
        return "basicBlock matches (non-library)";
      case kBasicBlocksLibrary:
        return "basicBlocks (library)";
      case kBasicBlocksNonLibrary:
        return "basicBlocks (non-library)";
      case kBasicBlocksPrimaryLibrary:
        return "basicBlocks primary (library)";
      case kBasicBlocksPrimaryNonLibrary:
        return "basicBlocks primary (non-library)";
      case kBasicBlocksSecondaryLibrary:
        return "basicBlocks secondary (library)";
      case kBasicBlocksSecondaryNonLibrary:
        return "basicBlocks secondary (non-library)";
      case kEdgesLibrary:
        return "edges (library)";
      case kEdgesNonLibrary:
        return "edges (non-library)";
      case kFlowGraphEdgeMatchesLibrary:
        return "flowGraph edge matches (library)";
      case kFlowGraphEdgeMatchesNonLibrary:
        return "flowGraph edge matches (non-library)";
      case kFlowGraphEdgesPrimaryLibrary:
        return "flowGraph edges primary (library)";
      case kFlowGraphEdgesPrimaryNonLibrary:
        return "flowGraph edges primary (non-library)";
      case kFlowGraphEdgesSecondaryLibrary:
        return "flowGraph edges secondary (library)";
      case kFlowGraphEdgesSecondaryNonLibrary:
        return "flowGraph edges secondary (non-library)";
      case kFunctionMatchesLibrary:
        return "function matches (library)";
      case kFunctionMatchesNonLibrary:
        return "function matches (non-library)";
      case kFunctionsLibrary:
        return "functions (library)";
      case kFunctionsNonLibrary:
        return "functions (non-library)";
      case kFunctionsPrimaryLibrary:
        return "functions primary (library)";
      case kFunctionsPrimaryNonLibrary:
        return "functions primary (non-library)";
      case kFunctionsSecondaryLibrary:
        return "functions secondary (library)";
      case kFunctionsSecondaryNonLibrary:
        return "functions secondary (non-library)";
      case kInstructionMatchesLibrary:
        return "instruction matches (library)";
      case kInstructionMatchesNonLibrary:
        return "instruction matches (non-library)";
      case kInstructionsLibrary:
        return "instructions (library)";
      case kInstructionsNonLibrary:
        return "instructions (non-library)";
      case kInstructionsPrimaryLibrary:
        return "instructions primary (library)";
      case kInstructionsPrimaryNonLibrary:
        return "instructions primary (non-library)";
      case kInstructionsSecondaryLibrary:
        return "instructions secondary (library)";
      case kInstructionsSecondaryNonLibrary:
        return "instructions secondary (non-library)";
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
