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

  double ReadDouble(const string& key, double default_value);
  int ReadInt(const string& key, int default_value);
  string ReadString(const string& key, const string& default_value);
  bool ReadBool(const string& key, bool default_value);

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
  void SetData(const string& data);

  static std::unique_ptr<XmlConfig> LoadFromString(const string& data);
  static std::unique_ptr<XmlConfig> LoadFromFile(const string& filename);

  static const string& SetDefaultFilename(string filename);
  static const string& GetDefaultFilename();

 private:
  template <typename T>
  struct CacheValue {
    using value_type = T;
    T value;
    bool modified;
  };

  XmlConfig();

  static string* default_filename_;

  std::unique_ptr<TiXmlDocument> document_;
  string filename_;
  bool modified_ = false;
  std::map<string, CacheValue<double>> double_cache_;
  std::map<string, CacheValue<int>> int_cache_;
  std::map<string, CacheValue<string>> string_cache_;
  std::map<string, CacheValue<bool>> bool_cache_;

  void WriteNode(const string& key, const string& value);
};

#endif  // XMLCONFIG_H_
