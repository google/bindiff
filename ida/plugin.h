// Copyright 2011-2022 Google LLC
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

#ifndef IDA_PLUGIN_H_
#define IDA_PLUGIN_H_

#include <cstddef>

// clang-format off
#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <loader.hpp>                                           // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT
// clang-format on

namespace security::binexport {

// Simple CRTP-style template that can be used to implement IDA Pro plugins.
// Since IDA plugins are singletons, this design allows to avoid global
// variables for keeping plugin state.
template <typename T>
class IdaPlugin {
 public:
  using LoadStatus = decltype(PLUGIN_OK);

  virtual ~IdaPlugin() = default;

  IdaPlugin(const IdaPlugin&) = delete;
  IdaPlugin& operator=(const IdaPlugin&) = delete;

  static T* instance() {
    static auto* instance = new T();
    return instance;
  }

  virtual LoadStatus Init() { return PLUGIN_OK; }
  virtual bool Run(size_t argument) = 0;
  virtual void Terminate() {}

 protected:
  IdaPlugin() = default;
};

}  // namespace security::binexport

#endif  // IDA_PLUGIN_H_
