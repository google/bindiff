// Copyright 2011-2019 Google LLC. All Rights Reserved.
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

#include "third_party/zynamics/binexport/ida/plugin.h"

namespace security::binexport {

class Plugin : public IdaPlugin<Plugin> {
 public:
  static constexpr char kComment[] =
      "Export to SQL RE-DB, BinDiff binary or text dump";
  static constexpr char kHotKey[] = "";

  int Init() override;
  bool Run(size_t argument) override;
  void Terminate() override;

  bool alsologtostderr() const { return alsologtostderr_; }

 private:
  bool alsologtostderr_ = false;
};

}  // namespace security::binexport

#endif  // IDA_MAIN_PLUGIN_H_
