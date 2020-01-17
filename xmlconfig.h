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

#ifndef XMLCONFIG_H_
#define XMLCONFIG_H_

#include <map>
#include <memory>
#include <string>
#include <utility>

#include "third_party/absl/container/flat_hash_map.h"
#include "third_party/zynamics/binexport/types.h"
#include "third_party/zynamics/binexport/util/statusor.h"

class TiXmlDocument;

namespace security::bindiff {

class XmlConfig {
 public:
  XmlConfig();
  virtual ~XmlConfig();

  XmlConfig& operator=(XmlConfig&&);

  // Initializes the configuration from an XML string.
  not_absl::Status LoadFromString(const std::string& data);

  not_absl::Status LoadFromFile(const std::string& filename);
  not_absl::Status LoadFromFileWithDefaults(const std::string& filename,
                                            const std::string& defaults);

  double ReadDouble(const std::string& key, double default_value) const;
  int ReadInt(const std::string& key, int default_value) const;
  std::string ReadString(const std::string& key,
                         const std::string& default_value) const;
  bool ReadBool(const std::string& key, bool default_value) const;

  std::vector<std::string> ReadStrings(
      const std::string& key,
      const std::vector<std::string>& default_value) const;

  // Access to the underlying raw document object.
  TiXmlDocument* document();
  const TiXmlDocument* document() const;

 private:
  std::unique_ptr<TiXmlDocument> document_;
  mutable absl::flat_hash_map<std::string, double> double_cache_;
  mutable absl::flat_hash_map<std::string, int> int_cache_;
  mutable absl::flat_hash_map<std::string, std::string> string_cache_;
  mutable absl::flat_hash_map<std::string, bool> bool_cache_;
};

}  // namespace security::bindiff

#endif  // XMLCONFIG_H_
