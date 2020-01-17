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

#ifndef CONFIG_H_
#define CONFIG_H_

#include "third_party/zynamics/bindiff/xmlconfig.h"
#include "third_party/zynamics/binexport/util/status.h"

namespace security::bindiff {

// Initializes the config file. First tries to read per-user configuration and
// falls back to per-machine configuration if not found.
not_absl::Status InitConfig();

// Returns the current application global configuration. If InitConfig() has not
// been called, this returns an empty configuration.
XmlConfig* GetConfig();

}  // namespace security::bindiff

#endif  // CONFIG_H_
