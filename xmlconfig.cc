#include "third_party/zynamics/bindiff/xmlconfig.h"

#include <algorithm>
#include <functional>
#include <fstream>
#include <stdexcept>
#include <string>

#include "third_party/absl/strings/ascii.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/tinyxpath/xpath_static.h"
#include "third_party/zynamics/bindiff/utility.h"

namespace {

string ReadFileToString(const string& filename) {
  std::ifstream stream;
  stream.exceptions(std::ifstream::failbit);
  stream.open(filename, std::ifstream::in | std::ifstream::ate);
  const auto size = static_cast<size_t>(stream.tellg());
  stream.seekg(0);
  string data(size, '\0');
  stream.read(&data[0], size);
  return data;
}

}  // namespace

string* XmlConfig::default_filename_ = new string();

XmlConfig::XmlConfig() { SetData(/*data=*/""); }

std::unique_ptr<XmlConfig> XmlConfig::LoadFromString(const string& data) {
  std::unique_ptr<XmlConfig> config(new XmlConfig());
  config->SetData(data);
  return config;
}

std::unique_ptr<XmlConfig> XmlConfig::LoadFromFile(const string& filename) {
  auto config = LoadFromString(ReadFileToString(filename));
  config->filename_ = filename;
  return config;
}

const string& XmlConfig::SetDefaultFilename(string filename) {
  *default_filename_ = std::move(filename);
  return *default_filename_;
}

const string& XmlConfig::GetDefaultFilename() { return *default_filename_; }

XmlConfig::~XmlConfig() {
  try {
    Save();
  } catch (...) {
    // Do not let exception escape destructor.
  }
}

TiXmlDocument* XmlConfig::GetDocument() { return document_.get(); }

const TiXmlDocument* XmlConfig::GetDocument() const { return document_.get(); }

void XmlConfig::SetData(const string& data) {
  document_.reset(new TiXmlDocument());
  filename_ = "";
  modified_ = false;
  if (!data.empty()) {
    document_->Parse(data.c_str());
  }

  for (auto& entry : int_cache_) {
    entry.second.modified = false;
  }
  for (auto& entry : double_cache_) {
    entry.second.modified = false;
  }
  for (auto& entry : bool_cache_) {
    entry.second.modified = false;
  }
  for (auto& entry : string_cache_) {
    entry.second.modified = false;
  }
}

void XmlConfig::SetSaveFileName(const string& filename) {
  if (filename_ == filename) {
    return;
  }
  filename_ = filename;
  modified_ = true;
}

template <typename CacheT, typename WriteT>
void SaveCacheContents(CacheT* cache, WriteT write_func) {
  for (const auto& entry : *cache) {
    if (entry.second.modified) {
      write_func(entry.first, entry.second.value);
    }
  }
}

void XmlConfig::Save() {
  if (!modified_ || filename_.empty()) {
    return;
  }
  try {
    using std::placeholders::_1;
    using std::placeholders::_2;
    SaveCacheContents(&int_cache_,
                      std::bind(&XmlConfig::WriteInt, this, _1, _2));
    SaveCacheContents(&double_cache_,
                      std::bind(&XmlConfig::WriteDouble, this, _1, _2));
    SaveCacheContents(&bool_cache_,
                      std::bind(&XmlConfig::WriteBool, this, _1, _2));
    SaveCacheContents(&string_cache_,
                      std::bind(&XmlConfig::WriteString, this, _1, _2));
    document_->SaveFile(filename_.c_str());
  } catch (...) {
    throw std::runtime_error(
        absl::StrCat("Error saving config file: ", filename_));
  }
}

template <typename CacheT, typename ConvertT>
typename CacheT::mapped_type::value_type ReadValue(
    CacheT* cache, TiXmlDocument* document, const string& key,
    ConvertT convert_func,
    const typename CacheT::mapped_type::value_type& default_value) {
  auto i = cache->find(key);
  if (i != cache->end() && i->second.modified) {
    return i->second.value;
  }

  if (!document) {
    return default_value;
  }
  auto& cache_entry = (*cache)[key];
  try {
    TinyXPath::xpath_processor processor(document->RootElement(), key.c_str());
    const string temp(processor.S_compute_xpath().c_str());
    cache_entry = {temp.empty() ? default_value : convert_func(temp), true};
  } catch (...) {
    cache_entry = {default_value, true};
  }
  return cache_entry.value;
}

int XmlConfig::ReadInt(const string& key, int default_value) {
  return ReadValue(&int_cache_, document_.get(), key,
                   [](const string& value) -> int { return std::stoi(value); },
                   default_value);
}

double XmlConfig::ReadDouble(const string& key, double default_value) {
  return ReadValue(
      &double_cache_, document_.get(), key,
      [](const string& value) -> double { return std::stod(value); },
      default_value);
}

string XmlConfig::ReadString(const string& key, const string& default_value) {
  // TODO(cblichmann): This is broken, we cannot tell the difference between an
  //                   empty string result and an error (no) result.
  return ReadValue(&string_cache_, document_.get(), key,
                   [](const string& value) -> const string& { return value; },
                   default_value);
}

bool XmlConfig::ReadBool(const string& key, bool default_value) {
  // Note: An empty string in the config file for this setting will always
  // result in the default value to be returned.
  return ReadValue(&bool_cache_, document_.get(), key,
                   [](const string& value) -> bool {
                     return absl::AsciiStrToLower(value) == "true";
                   },
                   default_value);
}

void XmlConfig::WriteNode(const string& key, const string& value) {
  if (!document_ || !document_->RootElement()) {
    // Must not raise a noisy error since it's called on global app
    // start/shutdown.
    return;
  }
  TiXmlNode* node =
      TinyXPath::XNp_xpath_node(document_->RootElement(), key.c_str());
  if (node) {
    if (node->NoChildren()) {
      node->LinkEndChild(new TiXmlText(value.c_str()));
    } else {
      node->FirstChild()->SetValue(value.c_str());
    }
  } else {
    TiXmlAttribute* attribute =
        TinyXPath::XAp_xpath_attribute(document_->RootElement(), key.c_str());
    if (!attribute) {
      throw std::runtime_error("Config file entry not found");
    }
    attribute->SetValue(value.c_str());
  }
  modified_ = true;
}

void XmlConfig::WriteString(const string& key, const string& value) {
  WriteNode(key, value);
  string_cache_[key] = {value, true};
}

void XmlConfig::WriteDouble(const string& key, double value) {
  WriteNode(key, std::to_string(value));
  double_cache_[key] = {value, true};
}

void XmlConfig::WriteInt(const string& key, int value) {
  WriteNode(key, std::to_string(value));
  int_cache_[key] = {value, true};
}

void XmlConfig::WriteBool(const string& key, bool value) {
  WriteNode(key, value ? "true" : "false");
  bool_cache_[key] = {value, true};
}

void XmlConfig::Dump(const string& filename) const {
  document_->SaveFile(filename.c_str());
}
