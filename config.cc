// Copyright 2011-2023 Google LLC
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

#include "third_party/zynamics/bindiff/config.h"

#ifdef BINDIFF_GOOGLE
#include "third_party/protobuf/util/json_util.h"
#else
#include <google/protobuf/util/json_util.h>
#endif

#include <algorithm>
#include <fstream>
#include <string>
#include <vector>

#include "third_party/absl/container/flat_hash_set.h"
#include "third_party/absl/status/status.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/bindiff/config_defaults.h"
#include "third_party/zynamics/bindiff/version.h"
#include "third_party/zynamics/binexport/util/filesystem.h"
#include "third_party/zynamics/binexport/util/process.h"
#include "third_party/zynamics/binexport/util/status_macros.h"

#ifdef BINDIFF_GOOGLE
namespace google::protobuf {
namespace util = ::proto2::util;
}  // namespace google::protobuf
#endif

using google::protobuf::util::JsonOptions;
using google::protobuf::util::JsonParseOptions;
using google::protobuf::util::JsonStringToMessage;
using google::protobuf::util::MessageToJsonString;
using security::binexport::GetCommonAppDataDirectory;
using security::binexport::GetOrCreateAppDataDirectory;

namespace security::bindiff::config {

namespace {

absl::StatusOr<std::string> ReadFileToString(const std::string& filename) {
  std::ifstream stream(filename,
                       std::ios::in | std::ios::ate | std::ios::binary);
  const auto size = static_cast<size_t>(stream.tellg());
  if (!stream) {
    return absl::NotFoundError(absl::StrCat("File not found: ", filename));
  }
  stream.seekg(0);
  std::string data(size, '\0');
  stream.read(&data[0], size);
  if (!stream) {
    return absl::InternalError(
        absl::StrCat("I/O error reading file: ", filename));
  }
  return data;
}

}  // namespace

const Config& Defaults() {
  // Intentionally crash if the embedded default string does not parse.
  static auto* defaults = new Config(*LoadFromJson(kDefaultJson));
  return *defaults;
}

Config& Proto() {
  static auto* config = []() -> Config* {
    auto* config = new Config(Defaults());

    if (absl::StatusOr<std::string> common_path =
            GetCommonAppDataDirectory(kBinDiffName);
        common_path.ok()) {
      if (absl::StatusOr<Config> common_config =
              LoadFromFile(JoinPath(*common_path, kConfigName));
          common_config.ok()) {
        config::MergeInto(*common_config, *config);
      }
    }

    if (absl::StatusOr<std::string> user_path =
            GetOrCreateAppDataDirectory(kBinDiffName);
        user_path.ok()) {
      if (absl::StatusOr<Config> user_config =
              LoadFromFile(JoinPath(*user_path, kConfigName));
          user_config.ok()) {
        config::MergeInto(*user_config, *config);
      }
    }

    return config;
  }();
  return *config;
}

absl::StatusOr<Config> LoadFromJson(absl::string_view data) {
  Config config;

  JsonParseOptions options;
  options.ignore_unknown_fields = true;
  NA_RETURN_IF_ERROR(JsonStringToMessage(data, &config, options));
  return config;
}

absl::StatusOr<Config> LoadFromFile(const std::string& filename) {
  NA_ASSIGN_OR_RETURN(std::string data, ReadFileToString(filename));
  return LoadFromJson(data);
}

std::string AsJsonString(const Config& config) {
  std::string data;
  JsonOptions options;
  options.add_whitespace = true;
  options.always_print_primitive_fields = true;
  options.preserve_proto_field_names = true;
  // For the Config proto, this should never fail to serialize.
  if (!MessageToJsonString(config, &data, options).ok()) {
    return "";
  }
  return data;
}

absl::Status SaveUserConfig(const Config& config) {
  NA_ASSIGN_OR_RETURN(const std::string path,
                      GetOrCreateAppDataDirectory(kBinDiffName));
  const std::string filename = JoinPath(path, kConfigName);

  const std::string data = AsJsonString(config);
  if (data.empty()) {
    return absl::InternalError("Serialization error");
  }

  std::ofstream stream(filename,
                       std::ios::out | std::ios::trunc | std::ios::binary);
  stream.write(&data[0], data.size());
  stream.close();
  if (!stream) {
    return absl::UnknownError(
        absl::StrCat("I/O error writing file: ", filename));
  }
  return absl::OkStatus();
}

void MergeInto(const Config& from, Config& config) {
  // Keep this code in sync with the implementation in `Config.java`.

  // Move away problematic fields
  auto function_matching = std::move(*config.mutable_function_matching());
  auto basic_block_matching = std::move(*config.mutable_basic_block_matching());

  // Let Protobuf handle the actual merge
  config.MergeFrom(from);

  absl::flat_hash_set<absl::string_view> names;
  for (const auto& step : config.function_matching()) {
    names.insert(step.name());
  }
  if (names.empty() || names.size() != config.function_matching_size()) {
    // Duplicate or no algorithms, restore original list
    *config.mutable_function_matching() = std::move(function_matching);
  }
  names.clear();
  for (const auto& step : config.basic_block_matching()) {
    names.insert(step.name());
  }
  if (names.empty() || names.size() != config.basic_block_matching_size()) {
    *config.mutable_basic_block_matching() = std::move(basic_block_matching);
  }
}

}  // namespace security::bindiff::config
