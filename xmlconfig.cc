#include "third_party/zynamics/bindiff/xmlconfig.h"

#include <algorithm>
#include <functional>
#include <iosfwd>
#include <iostream>
#include <locale>
#include <sstream>
#include <stdexcept>
#include <string>

#include "third_party/tinyxpath/xpath_static.h"
#include "third_party/zynamics/bindiff/utility.h"
#include "third_party/zynamics/binexport/filesystem_util.h"

namespace {

// Case insensitive compare.
bool EqualsIgnoreCase(std::string one, std::string two) {
  if (two.size() != one.size()) {
    return false;
  }

  for (size_t i = 0; i < one.size(); ++i) {
    if (std::tolower(one[i], std::locale()) !=
        std::tolower(two[i], std::locale())) {
      return false;
    }
  }
  return true;
}

std::string ReadFileToString(const std::string& filename) {
  std::ifstream stream(filename);
  stream.seekg(0, std::ios_base::end);
  const auto size = static_cast<size_t>(stream.tellg());
  stream.seekg(0, std::ios_base::beg);
  if (!size) {
    throw std::runtime_error("Config file is empty");
  }
  std::string data(size, '\0');
  stream.read(&data[0], size);
  return data;
}

}  // namespace

std::string XmlConfig::default_filename_;

XmlConfig::XmlConfig() : document_(new TiXmlDocument()) {}

std::unique_ptr<XmlConfig> XmlConfig::LoadFromString(const std::string& data) {
  std::unique_ptr<XmlConfig> config(new XmlConfig());
  config->document_->Parse(data.c_str());
  return config;
}

std::unique_ptr<XmlConfig> XmlConfig::LoadFromFile(
    const std::string& filename) {
  std::unique_ptr<XmlConfig> config(new XmlConfig());
  config->Init(filename);
  return config;
}

const std::string& XmlConfig::SetDefaultFilename(const std::string& filename) {
  return default_filename_ = filename;
}

const std::string& XmlConfig::GetDefaultFilename() {
  return default_filename_;
}

XmlConfig::~XmlConfig() {
  try {
    Save();
  } catch (...) {
    // Do not let exception escape destructor.
  }
  delete document_;
  document_ = nullptr;
}

TiXmlDocument* XmlConfig::GetDocument() {
  return document_;
}

const TiXmlDocument* XmlConfig::GetDocument() const {
  return document_;
}

void XmlConfig::SetData(const char* data) {
  delete document_;
  document_ = new TiXmlDocument();
  filename_ = "";
  modified_ = false;
  document_->Parse(data);

  for (auto& entry : int_cache_) {
    entry.second.second = false;
  }
  for (auto& entry : double_cache_) {
    entry.second.second = false;
  }
  for (auto& entry : bool_cache_) {
    entry.second.second = false;
  }
  for (auto& entry : string_cache_) {
    entry.second.second = false;
  }
}

void XmlConfig::Init(const std::string& filename) {
  if (!FileExists(filename)) {
    throw std::runtime_error(
        (std::string("File not found: ") + filename).c_str());
  }
  SetData(ReadFileToString(filename).c_str());
  filename_ = filename;
}

void XmlConfig::SetSaveFileName(const std::string& filename) {
  if (filename_ == filename) {
    return;
  }
  filename_ = filename;
  modified_ = true;
}

template <typename CacheT, typename WriteT>
void SaveCacheContents(CacheT* cache, WriteT write_func) {
  for (const auto& entry : *cache) {
    if (entry.second.second) {
      write_func(entry.first, entry.second.first);
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
    throw std::runtime_error(std::string("Error saving config file to ") +
                             filename_);
  }
}

template <typename CacheT, typename ConvertT>
typename CacheT::mapped_type::first_type ReadValue(
    CacheT* cache, TiXmlDocument* document, const std::string& key,
    ConvertT convert_func,
    const typename CacheT::mapped_type::first_type& default_value) {
  auto i = cache->find(key);
  if (i != cache->end() && i->second.second) {
    return i->second.first;
  }

  if (!document) {
    return default_value;
  }
  auto& cache_entry = (*cache)[key];
  try {
    TinyXPath::xpath_processor processor(document->RootElement(), key.c_str());
    const std::string temp(processor.S_compute_xpath().c_str());
    cache_entry = {temp.empty() ? default_value : convert_func(temp), true};
  } catch (...) {
    cache_entry = {default_value, true};
  }
  return cache_entry.first;
}

int XmlConfig::ReadInt(const std::string& key, const int& default_value) {
  return ReadValue(
      &int_cache_, document_, key,
      [](const std::string& value) -> int { return std::stoi(value); },
      default_value);
}

double XmlConfig::ReadDouble(const std::string& key,
                             const double& default_value) {
  return ReadValue(
      &double_cache_, document_, key,
      [](const std::string& value) -> double { return std::stod(value); },
      default_value);
}

std::string XmlConfig::ReadString(const std::string& key,
                                  const std::string& default_value) {
  // TODO(cblichmann): This is broken, we cannot tell the difference between an
  //                   empty string result and an error (no) result.
  return ReadValue(
      &string_cache_, document_, key,
      [](const std::string& value) -> const std::string& { return value; },
      default_value);
}

bool XmlConfig::ReadBool(const std::string& key, const bool& default_value) {
  // Note: An empty string in the config file for this setting will always
  // result in the default value to be returned.
  return ReadValue(&bool_cache_, document_, key,
                   [](const std::string& value) -> bool {
                     return EqualsIgnoreCase(value, "true");
                   },
                   default_value);
}

void XmlConfig::WriteNode(const std::string& key, const std::string& value) {
  if (!document_ || !document_->RootElement()) {
    // Must not raise a noisy error since it's called on global app
    // start/shutdown.
    std::cerr << "XmlConfig::writeNode: Document is null!" << std::endl;
    return;
  }
  TiXmlNode* node = TinyXPath::XNp_xpath_node(document_->RootElement(),
                                              key.c_str());
  if (node) {
    if (node->NoChildren()) {
      node->LinkEndChild(new TiXmlText(value.c_str()));
    } else {
      node->FirstChild()->SetValue(value.c_str());
    }
  } else {
    TiXmlAttribute* attribute = TinyXPath::XAp_xpath_attribute(
        document_->RootElement(), key.c_str());
    if (!attribute) {
      throw std::runtime_error("Config file entry not found");
    }
    attribute->SetValue(value.c_str());
  }
  modified_ = true;
}

void XmlConfig::WriteString(const std::string& key, const std::string& value) {
  WriteNode(key, value);
  string_cache_[key] = std::make_pair(value, true);
}

void XmlConfig::WriteDouble(const std::string& key, double value) {
  WriteNode(key, std::to_string(value));
  double_cache_[key] = std::make_pair(value, true);
}

void XmlConfig::WriteInt(const std::string& key, int value) {
  WriteNode(key, std::to_string(value));
  int_cache_[key] = std::make_pair(value, true);
}

void XmlConfig::WriteBool(const std::string& key, bool value) {
  WriteNode(key, value ? "true" : "false");
  bool_cache_[key] = std::make_pair(value, true);
}

void XmlConfig::Dump(const std::string& filename) const {
  document_->SaveFile(filename.c_str());
}
