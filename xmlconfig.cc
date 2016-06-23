#include "third_party/zynamics/bindiff/xmlconfig.h"

#include <algorithm>
#include <iostream>
#include <locale>
#include <sstream>
#include <stdexcept>
#include <string>

#include "third_party/tinyxpath/xpath_static.h"
#include "third_party/zynamics/binexport/filesystem_util.h"
#include "third_party/zynamics/zylibcpp/utility/utility.h"

namespace {

// Case insensitive compare.
bool EqualsIgnoreCase(std::string one, std::string two) {
  if (two.size() != one.size()) {
    return false;
  }

  for (size_t i(0); i < one.size(); ++i) {
    if (std::tolower(one[i], std::locale()) !=
        std::tolower(two[i], std::locale())) {
      return false;
    }
  }

  return true;
}

}  // namespace

std::string XmlConfig::default_filename_;

const std::string& XmlConfig::SetDefaultFilename(const std::string& filename) {
  return default_filename_ = filename;
}

const std::string& XmlConfig::GetDefaultFilename() {
  return default_filename_;
}

XmlConfig::XmlConfig(const std::string& filename,
                     const std::string& root_element)
    : document_(0),
      filename_(filename),
      modified_(false) {
  Init(filename, root_element);
}

XmlConfig::XmlConfig(std::istream& file)
    : document_(0), filename_(""), modified_(false) {
  file.seekg(0, std::ios_base::end);
  const std::streamsize size(file.tellg());
  file.seekg(0, std::ios_base::beg);
  if (!size) {
    throw std::runtime_error("Config file not found");
  }
  std::vector<char> data(size);
  file.read(&data[0], size);
  document_ = new TiXmlDocument();
  document_->Parse(&data[0]);
}

XmlConfig::XmlConfig(const char* data)
    : document_(new TiXmlDocument()), filename_(""), modified_(false) {
  document_->Parse(data);
}

XmlConfig::XmlConfig() : document_(0), filename_(""), modified_(false) {}

XmlConfig::~XmlConfig() {
  try {
    Save();
  } catch (...) {
    // Do not let exception escape destructor.
  }
  delete document_;
  document_ = 0;
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

  for (auto i = int_cache_.begin(); i != int_cache_.end(); ++i) {
    i->second.second = false;
  }
  for (auto i = double_cache_.begin(); i != double_cache_.end(); ++i) {
    i->second.second = false;
  }
  for (auto i = bool_cache_.begin(); i != bool_cache_.end(); ++i) {
    i->second.second = false;
  }
  for (auto i = string_cache_.begin(); i != string_cache_.end(); ++i) {
    i->second.second = false;
  }
}

void XmlConfig::Init(const std::string& filename, const std::string&) {
  if (!FileExists(filename)) {
    throw std::runtime_error(
        (std::string("File not found: ") + filename).c_str());
  }

  filename_ = filename;
  delete document_;
  document_ = new TiXmlDocument(filename.c_str());
  if (!document_->LoadFile()) {
    const std::string msg(
        __FUNCTION__ + std::string(": ") + document_->ErrorDesc() + " '"
            + filename + "'");
    delete document_;
    document_ = 0;
    throw std::runtime_error(msg.c_str());
  }
  for (IntCache::iterator i(int_cache_.begin()); i != int_cache_.end(); ++i) {
    i->second.second = false;
  }
  for (DoubleCache::iterator i(double_cache_.begin()); i != double_cache_.end();
       ++i) {
    i->second.second = false;
  }
  for (BoolCache::iterator i(bool_cache_.begin()); i != bool_cache_.end();
       ++i) {
    i->second.second = false;
  }
  for (StringCache::iterator i(string_cache_.begin()); i != string_cache_.end();
       ++i) {
    i->second.second = false;
  }
}

void XmlConfig::SetSaveFileName(const std::string& filename) {
  if (filename_ == filename)
    return;
  filename_ = filename;
  modified_ = true;
}

void XmlConfig::Save() {
  if (!modified_ || filename_.empty()) {
    return;
  }
  try {
    for (auto i = int_cache_.begin(); i != int_cache_.end(); ++i) {
      if (i->second.second) {
        WriteInt(i->first, i->second.first);
      }
    }
    for (auto i = double_cache_.begin(); i != double_cache_.end(); ++i) {
      if (i->second.second) {
        WriteDouble(i->first, i->second.first);
      }
    }
    for (auto i = bool_cache_.begin(); i != bool_cache_.end(); ++i) {
      if (i->second.second) {
        WriteBool(i->first, i->second.first);
      }
    }
    for (auto i = string_cache_.begin(); i != string_cache_.end(); ++i) {
      if (i->second.second) {
        WriteString(i->first, i->second.first);
      }
    }
    document_->SaveFile(filename_.c_str());
  } catch (...) {
    throw std::runtime_error(std::string("Error saving config file to ") +
                             filename_);
  }
}

const int& XmlConfig::ReadInt(const std::string& key,
                              const int& default_value) {
  auto i = int_cache_.find(key);
  if (i != int_cache_.end() && i->second.second) {
    return i->second.first;
  }

  if (!document_) {
    return default_value;
  }

  try {
    TinyXPath::xpath_processor processor(document_->RootElement(), key.c_str());
    const std::string temp(processor.S_compute_xpath().c_str());
    if (temp.empty()) {
      int_cache_[key] = std::make_pair(default_value, true);
    } else {
      int_cache_[key] = std::make_pair(std::stoul(temp), true);
    }
  } catch (...) {
    int_cache_[key] = std::make_pair(default_value, true);
  }
  return int_cache_[key].first;
}

const double& XmlConfig::ReadDouble(const std::string& key,
                                    const double& default_value) {
  auto i = double_cache_.find(key);
  if (i != double_cache_.end() && i->second.second) {
    return i->second.first;
  }

  if (!document_) {
    return default_value;
  }

  try {
    TinyXPath::xpath_processor processor(document_->RootElement(), key.c_str());
    const std::string temp(processor.S_compute_xpath().c_str());
    if (temp.empty()) {
      double_cache_[key] = std::make_pair(default_value, true);
    } else {
      double_cache_[key] = std::make_pair(std::stod(temp), true);
    }
  } catch(...) {
    double_cache_[key] = std::make_pair(default_value, true);
  }
  return double_cache_[key].first;
}

const std::string& XmlConfig::ReadString(const std::string& key,
                                         const std::string& default_value) {
  auto i = string_cache_.find(key);
  if (i != string_cache_.end()) {
    return i->second.first;
  }

  if (!document_) {
    return default_value;
  }

  try {
    // TODO(soerenme) This is broken, we cannot tell the difference between an
    //     empty string result and an error (no) result.
    TinyXPath::xpath_processor processor(document_->RootElement(), key.c_str());
    const std::string temp(processor.S_compute_xpath().c_str());
    if (temp.empty()) {
      string_cache_[key] = std::make_pair(default_value, true);
    } else {
      string_cache_[key] = std::make_pair(temp, true);
    }
  } catch(...) {
    string_cache_[key] = std::make_pair(default_value, true);
  }
  return string_cache_[key].first;
}

const bool& XmlConfig::ReadBool(const std::string& key,
                                const bool& default_value) {
  auto i = bool_cache_.find(key);
  if (i != bool_cache_.end()) {
    return i->second.first;
  }

  if (!document_) {
    return default_value;
  }

  try {
    // TODO(soerenme) This is broken, we cannot tell the difference between an
    //     empty string result and an error (no) result.
    TinyXPath::xpath_processor processor(document_->RootElement(), key.c_str());
    const std::string temp(processor.S_compute_xpath().c_str());
    bool_cache_[key] = std::make_pair(EqualsIgnoreCase(temp, "true"), true);
  } catch(...) {
    bool_cache_[key] = std::make_pair(default_value, true);
  }
  return bool_cache_[key].first;
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
  std::ostringstream stream;
  stream << value;
  WriteNode(key, stream.str());
  double_cache_[key] = std::make_pair(value, true);
}

void XmlConfig::WriteInt(const std::string& key, int value) {
  std::ostringstream stream;
  stream << value;
  WriteNode(key, stream.str());
  int_cache_[key] = std::make_pair(value, true);
}

void XmlConfig::WriteBool(const std::string& key, bool value) {
  WriteNode(key, value ? "true" : "false");
  bool_cache_[key] = std::make_pair(value, true);
}

void XmlConfig::Dump(const std::string& filename) const {
  document_->SaveFile(filename.c_str());
}
