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

#include "third_party/absl/base/attributes.h"
#include "third_party/absl/status/status.h"
#include "third_party/absl/status/statusor.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/zynamics/bindiff/xmlconfig.h"

#undef ERROR  // Abseil headers include Windows.h, which defines this
#include "third_party/zynamics/bindiff/bindiff_config.pb.h"

namespace security::bindiff {

namespace config {

inline constexpr char kConfigName[] = "bindiff.json";

// Returns the current application global configuration. On first call,
// initializes from well-known locations in the filesystem, or if no
// configuration is found, with default values.
Config& Proto();

// Returns the default configuration.
const Config& Defaults();

// Loads configuration from a JSON string.
absl::StatusOr<Config> LoadFromJson(absl::string_view data);

absl::StatusOr<Config> LoadFromFile(const std::string& filename);

// Serializes the specified configuration to JSON.
std::string AsJsonString(const Config& config);

// Saves configuration to the per-user configuration directory.
absl::Status SaveUserConfig(const Config& config);

}  // namespace config

// Initializes the config file. First tries to read per-user configuration and
// falls back to per-machine configuration if not found.
ABSL_DEPRECATED("Migrate to JSON config")
absl::Status InitConfig();

// Returns the current application global configuration. If InitConfig() has not
// been called, this returns an empty configuration.
ABSL_DEPRECATED("Migrate to JSON config")
XmlConfig* GetConfig();

}  // namespace security::bindiff

#endif  // CONFIG_H_
