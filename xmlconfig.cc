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

#include "third_party/zynamics/bindiff/xmlconfig.h"

#include <algorithm>
#include <functional>
#include <fstream>
#include <memory>
#include <string>

#include "third_party/absl/memory/memory.h"
#include "third_party/absl/strings/ascii.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/tinyxpath/node_set.h"
#include "third_party/tinyxpath/tinyxml.h"
#include "third_party/tinyxpath/xpath_expression.h"
#include "third_party/tinyxpath/xpath_static.h"
#include "third_party/zynamics/bindiff/utility.h"
#include "third_party/zynamics/binexport/util/canonical_errors.h"
#include "third_party/zynamics/binexport/util/status.h"
#include "third_party/zynamics/binexport/util/status_macros.h"

namespace security::bindiff {
namespace {

not_absl::StatusOr<std::string> ReadFileToString(const std::string& filename) {
  std::ifstream stream;
  stream.open(filename,
              std::ifstream::in | std::ifstream::ate | std::ifstream::binary);
  const auto size = static_cast<size_t>(stream.tellg());
  if (!stream) {
    return not_absl::NotFoundError(absl::StrCat("File not found: ", filename));
  }
  stream.seekg(0);
  std::string data(size, '\0');
  stream.read(&data[0], size);
  if (!stream) {
    return not_absl::InternalError(
        absl::StrCat("I/O error reading file: ", filename));
  }
  return data;
}

}  // namespace

XmlConfig::XmlConfig() = default;

XmlConfig::~XmlConfig() = default;

XmlConfig& XmlConfig::operator=(XmlConfig&&) = default;

not_absl::Status XmlConfig::LoadFromString(const std::string& data) {
  document_ = absl::make_unique<TiXmlDocument>();
  if (!data.empty()) {
    document_->Parse(data.c_str());
  }
  return not_absl::OkStatus();
}

not_absl::Status XmlConfig::LoadFromFile(const std::string& filename) {
  NA_ASSIGN_OR_RETURN(std::string data, ReadFileToString(filename));
  return LoadFromString(data);
}

not_absl::Status XmlConfig::LoadFromFileWithDefaults(
    const std::string& filename, const std::string& defaults) {
  auto status = LoadFromFile(filename);
  if (!status.ok()) {
    return LoadFromString(defaults);
  }
  return status;
}

TiXmlDocument* XmlConfig::document() { return document_.get(); }

const TiXmlDocument* XmlConfig::document() const { return document_.get(); }

template <typename CacheT, typename ConvertT>
typename CacheT::mapped_type ReadValue(
    CacheT* cache, TiXmlDocument* document, const std::string& key,
    ConvertT convert_func, const typename CacheT::mapped_type& default_value) {
  auto it = cache->find(key);
  if (it != cache->end()) {
    return it->second;
  }

  if (!document) {
    return default_value;
  }
  auto& cache_entry = (*cache)[key];
  try {
    TinyXPath::xpath_processor processor(document->RootElement(), key.c_str());
    const std::string temp(processor.S_compute_xpath().c_str());
    cache_entry = temp.empty() ? default_value : convert_func(temp);
  } catch (...) {
    cache_entry = default_value;
  }
  return cache_entry;
}

int XmlConfig::ReadInt(const std::string& key, int default_value) const {
  return ReadValue(
      &int_cache_, document_.get(), key,
      [](const std::string& value) -> int { return std::stoi(value); },
      default_value);
}

double XmlConfig::ReadDouble(const std::string& key,
                             double default_value) const {
  return ReadValue(
      &double_cache_, document_.get(), key,
      [](const std::string& value) -> double { return std::stod(value); },
      default_value);
}

std::string XmlConfig::ReadString(const std::string& key,
                                  const std::string& default_value) const {
  // TODO(cblichmann): We cannot tell the difference between an empty string
  //                   result and an error (no) result.
  return ReadValue(
      &string_cache_, document_.get(), key,
      [](const std::string& value) -> const std::string& { return value; },
      default_value);
}

bool XmlConfig::ReadBool(const std::string& key, bool default_value) const {
  // Note: An empty string in the config file for this setting will always
  // result in the default value to be returned.
  return ReadValue(
      &bool_cache_, document_.get(), key,
      [](const std::string& value) -> bool {
        return absl::AsciiStrToLower(value) == "true";
      },
      default_value);
}

std::vector<std::string> XmlConfig::ReadStrings(
    const std::string& key,
    const std::vector<std::string>& default_value) const {
  std::vector<std::string> result;
  TinyXPath::xpath_processor processor(document_->RootElement(), key.c_str());

  TinyXPath::expression_result er = processor.er_compute_xpath();
  int num_nodes = er.e_type != TinyXPath::e_node_set
                      ? 0
                      : er.nsp_get_node_set()->u_get_nb_node_in_set();
  for (int i = 0; i < num_nodes; ++i) {
    bool is_attribute = false;
    const TiXmlBase* node = nullptr;
    processor.v_get_xpath_base(i, node, is_attribute);
    if (is_attribute) {
      result.push_back(dynamic_cast<const TiXmlAttribute*>(node)->Value());
    } else {
      result.push_back(dynamic_cast<const TiXmlNode*>(node)->Value());
    }
  }

  if (num_nodes > 0) {
    // Work around leak in TinyXPath's node_set destructor
    er.nsp_get_node_set()->TinyXPath::node_set::~node_set();
  }
  return !result.empty() ? result : default_value;
}

}  // namespace security::bindiff
