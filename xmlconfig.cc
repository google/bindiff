#include "third_party/zynamics/bindiff/xmlconfig.h"

#include <algorithm>
#include <functional>
#include <fstream>
#include <memory>
#include <string>

#include "third_party/absl/memory/memory.h"
#include "third_party/absl/strings/ascii.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/tinyxpath/xpath_static.h"
#include "third_party/zynamics/bindiff/utility.h"
#include "third_party/zynamics/binexport/util/canonical_errors.h"
#include "third_party/zynamics/binexport/util/status.h"
#include "third_party/zynamics/binexport/util/status_macros.h"

namespace security {
namespace bindiff {
namespace {

not_absl::StatusOr<string> ReadFileToString(const string& filename) {
  std::ifstream stream;
  stream.open(filename,
              std::ifstream::in | std::ifstream::ate | std::ifstream::binary);
  const auto size = static_cast<size_t>(stream.tellg());
  if (!stream) {
    return not_absl::NotFoundError(absl::StrCat("File not found: ", filename));
  }
  stream.seekg(0);
  string data(size, '\0');
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

not_absl::Status XmlConfig::LoadFromString(const string& data) {
  document_ = absl::make_unique<TiXmlDocument>();
  if (!data.empty()) {
    document_->Parse(data.c_str());
  }
  return not_absl::OkStatus();
}

not_absl::Status XmlConfig::LoadFromFile(
    const string& filename) {
  NA_ASSIGN_OR_RETURN(string data, ReadFileToString(filename));
  return LoadFromString(data);
}

not_absl::Status XmlConfig::LoadFromFileWithDefaults(const string& filename,
                                                     const string& defaults) {
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
    CacheT* cache, TiXmlDocument* document, const string& key,
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
    const string temp(processor.S_compute_xpath().c_str());
    cache_entry = temp.empty() ? default_value : convert_func(temp);
  } catch (...) {
    cache_entry = default_value;
  }
  return cache_entry;
}

int XmlConfig::ReadInt(const string& key, int default_value) const {
  return ReadValue(
      &int_cache_, document_.get(), key,
      [](const string& value) -> int { return std::stoi(value); },
      default_value);
}

double XmlConfig::ReadDouble(const string& key, double default_value) const {
  return ReadValue(
      &double_cache_, document_.get(), key,
      [](const string& value) -> double { return std::stod(value); },
      default_value);
}

string XmlConfig::ReadString(const string& key,
                             const string& default_value) const {
  // TODO(cblichmann): We cannot tell the difference between an empty string
  //                   result and an error (no) result.
  return ReadValue(
      &string_cache_, document_.get(), key,
      [](const string& value) -> const string& { return value; },
      default_value);
}

bool XmlConfig::ReadBool(const string& key, bool default_value) const {
  // Note: An empty string in the config file for this setting will always
  // result in the default value to be returned.
  return ReadValue(
      &bool_cache_, document_.get(), key,
      [](const string& value) -> bool {
        return absl::AsciiStrToLower(value) == "true";
      },
      default_value);
}

}  // namespace bindiff
}  // namespace security
