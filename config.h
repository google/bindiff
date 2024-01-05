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

#ifndef CONFIG_H_
#define CONFIG_H_

#include "third_party/absl/base/attributes.h"
#include "third_party/absl/status/status.h"
#include "third_party/absl/status/statusor.h"
#include "third_party/absl/strings/string_view.h"

#undef ERROR  // Windows.h
#include "third_party/zynamics/bindiff/bindiff_config.pb.h"

namespace security::bindiff::config {

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

// Merges configuration settings. Similar to Protobuf's MergeFrom(), but
// intelligently merges the matching algorithms, where order and uniqueness
// matter.
void MergeInto(const Config& from, Config& config);

}  // namespace security::bindiff::config

#endif  // CONFIG_H_
