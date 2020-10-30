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

#include "third_party/zynamics/bindiff/config.h"

#ifdef GOOGLE
#include "net/proto2/util/public/json_util.h"
#else
#include <google/protobuf/util/json_util.h>
#endif

#include <fstream>
#include <string>

#include "base/logging.h"
#include "third_party/absl/base/attributes.h"
#include "third_party/absl/status/status.h"
#include "third_party/absl/strings/numbers.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/bindiff/config_defaults.h"
#include "third_party/zynamics/bindiff/version.h"
#include "third_party/zynamics/bindiff/xmlconfig.h"
#include "third_party/zynamics/binexport/util/filesystem.h"
#include "third_party/zynamics/binexport/util/process.h"
#include "third_party/zynamics/binexport/util/status_macros.h"

#ifdef GOOGLE
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

namespace security::bindiff {
namespace config {

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
        config->MergeFrom(*common_config);
      }
    }

    if (absl::StatusOr<std::string> user_path =
            GetOrCreateAppDataDirectory(kBinDiffName);
        user_path.ok()) {
      if (absl::StatusOr<Config> user_config =
              LoadFromFile(JoinPath(*user_path, kConfigName));
          user_config.ok()) {
        config->MergeFrom(*user_config);
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
#ifdef GOOGLE
  NA_RETURN_IF_ERROR(JsonStringToMessage(data, &config, options));
#else
  if (auto status = JsonStringToMessage(
          google::protobuf::StringPiece(data.data(), data.size()), &config,
          options);
      !status.ok()) {
    return absl::UnknownError(std::string(status.error_message()));
  }
#endif
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

}  // namespace config

absl::Status InitConfig() {
  constexpr char kConfigName[] = "bindiff.xml";

  std::string path;
  NA_ASSIGN_OR_RETURN(path, GetOrCreateAppDataDirectory(kBinDiffName));
  const std::string user_path = JoinPath(path, kConfigName);

  std::string common_path;
  auto common_path_or = GetCommonAppDataDirectory(kBinDiffName);
  const bool have_common_path = common_path_or.ok();
  if (have_common_path) {
    common_path = JoinPath(std::move(common_path_or).value(), kConfigName);
  }

  XmlConfig user_config;
  XmlConfig common_config;
  // Try to read user's local config
  const bool have_user_config = user_config.LoadFromFile(user_path).ok();
  // Try to read machine config
  const bool have_common_config =
      have_common_path && common_config.LoadFromFile(common_path).ok();

  bool use_common_config = false;
  if (have_user_config && have_common_config) {
    use_common_config = user_config.ReadInt("/bindiff/@config-version", 0) <
                        common_config.ReadInt("/bindiff/@config-version", 0);
    LOG_IF(WARNING, use_common_config)
        << "User config version is out of date, using per-machine config";
  } else if (have_user_config) {
    use_common_config = false;
  } else if (have_common_config) {
    use_common_config = true;
  } else {
    return absl::NotFoundError("Missing configuration file");
  }

  auto* config = GetConfig();
  if (use_common_config) {
    *config = std::move(common_config);
    std::remove(user_path.c_str());
    if (absl::Status status = CopyFile(common_path, user_path); !status.ok()) {
      LOG(ERROR) << "Cannot copy per-machine config: "
                 << std::string(status.message());
    }
  } else {
    *config = std::move(user_config);
  }
  return absl::OkStatus();
}

XmlConfig* GetConfig() {
  static auto* config = new XmlConfig{};
  return config;
}

}  // namespace security::bindiff
