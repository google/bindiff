#ifndef XMLCONFIG_H_
#define XMLCONFIG_H_

#include <map>
#include <memory>
#include <string>
#include <utility>

#include "third_party/zynamics/binexport/types.h"

class TiXmlDocument;

class XmlConfig {
 public:
  XmlConfig(const XmlConfig&) = delete;
  const XmlConfig& operator=(const XmlConfig&) = delete;

  virtual ~XmlConfig();

  double ReadDouble(const string& key, const double& default_value);
  int ReadInt(const string& key, const int& default_value);
  string ReadString(const string& key, const string& default_value);
  bool ReadBool(const string& key, const bool& default_value);

  void WriteDouble(const string& key, double value);
  void WriteInt(const string& key, int value);
  void WriteString(const string& key, const string& value);
  void WriteBool(const string& key, bool value);

  void Dump(const string& filename) const;
  void SetSaveFileName(const string& filename);
  void Save();
  TiXmlDocument* GetDocument();
  const TiXmlDocument* GetDocument() const;

  // Re-initializes the config file with the given content.
  void SetData(const char* data);

  static std::unique_ptr<XmlConfig> LoadFromString(const string& data);
  static std::unique_ptr<XmlConfig> LoadFromFile(const string& filename);

  static const string& SetDefaultFilename(const string& filename);
  static const string& GetDefaultFilename();

 private:
  using DoubleCache = std::map<string, std::pair<double, bool>>;
  using IntCache = std::map<string, std::pair<int, bool>>;
  using StringCache = std::map<string, std::pair<string, bool>>;
  using BoolCache = std::map<string, std::pair<bool, bool>>;

  XmlConfig();
  void Init(const string& filename);

  static string default_filename_;

  TiXmlDocument* document_;
  string filename_;
  bool modified_ = false;
  DoubleCache double_cache_;
  IntCache int_cache_;
  StringCache string_cache_;
  BoolCache bool_cache_;

  void WriteNode(const string& key, const string& value);
};

#endif  // XMLCONFIG_H_
