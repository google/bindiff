// Copyright 2019-2020 Google LLC
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

#ifndef BINARYNINJA_MAIN_PLUGIN_H_
#define BINARYNINJA_MAIN_PLUGIN_H_

// clang-format off
#include "binaryninjaapi.h"  // NOLINT
// clang-format on

namespace security::binexport {

class Plugin {
 public:
  static constexpr char kDescription[] =
      "Export to BinDiff binary or text dump";

  Plugin(const Plugin&) = delete;
  Plugin& operator=(const Plugin&) = delete;

  static Plugin* instance() {
    static auto* instance = new Plugin();
    return instance;
  }

  bool Init();
  void Run(BinaryNinja::BinaryView* view);

  bool alsologtostderr() const { return alsologtostderr_; }

 private:
  Plugin() = default;

  bool alsologtostderr_ = false;
};

}  // namespace security::binexport

#endif  // BINARYNINJA_MAIN_PLUGIN_H_
