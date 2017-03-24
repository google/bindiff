#ifndef XMLCONFIG_H_
#define XMLCONFIG_H_

#include <iosfwd>
#include <map>
#include <string>
#include <utility>

class TiXmlDocument;

class XmlConfig {
 public:
  XmlConfig() = default;
  XmlConfig(const std::string& filename, const std::string& root_element);
  explicit XmlConfig(const char* data);

  XmlConfig(const XmlConfig&) = delete;
  const XmlConfig& operator=(const XmlConfig&) = delete;

  virtual ~XmlConfig();

  double ReadDouble(const std::string& key, const double& default_value);
  int ReadInt(const std::string& key, const int& default_value);
  std::string ReadString(const std::string& key,
                         const std::string& default_value);
  bool ReadBool(const std::string& key, const bool& default_value);

  void WriteDouble(const std::string& key, double value);
  void WriteInt(const std::string& key, int value);
  void WriteString(const std::string& key, const std::string& value);
  void WriteBool(const std::string& key, bool value);

  void Dump(const std::string& filename) const;
  void Init(const std::string& filename, const std::string& root_element);
  void SetSaveFileName(const std::string& filename);
  void Save();
  TiXmlDocument* GetDocument();
  const TiXmlDocument* GetDocument() const;

  // Re-initializes the config file with the given content.
  void SetData(const char* data);

  static const std::string& SetDefaultFilename(const std::string& filename);
  static const std::string& GetDefaultFilename();

 private:
  using DoubleCache = std::map<std::string, std::pair<double, bool>>;
  using IntCache = std::map<std::string, std::pair<int, bool>>;
  using StringCache = std::map<std::string, std::pair<std::string, bool>>;
  using BoolCache = std::map<std::string, std::pair<bool, bool>>;

  static std::string default_filename_;
  TiXmlDocument* document_ = nullptr;
  std::string filename_;
  bool modified_ = false;
  DoubleCache double_cache_;
  IntCache int_cache_;
  StringCache string_cache_;
  BoolCache bool_cache_;

  void WriteNode(const std::string& key, const std::string& value);
};

#endif  // XMLCONFIG_H_
