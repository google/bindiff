#ifndef XMLCONFIG_H_
#define XMLCONFIG_H_

#include <iosfwd>
#include <map>
#include <string>
#include <utility>

class TiXmlDocument;

class XmlConfig {
 public:
  XmlConfig();
  XmlConfig(const std::string& filename, const std::string& root_element);
  explicit XmlConfig(std::istream& file);
  explicit XmlConfig(const char* data);
  virtual ~XmlConfig();

  const double& ReadDouble(const std::string& key, const double& default_value);
  const int& ReadInt(const std::string& key, const int& default_value);
  const std::string& ReadString(const std::string& key,
                                const std::string& default_value);
  const bool& ReadBool(const std::string& key, const bool& default_value);

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
  typedef std::map<std::string, std::pair<double, bool>> DoubleCache;
  typedef std::map<std::string, std::pair<int, bool>> IntCache;
  typedef std::map<std::string, std::pair<std::string, bool>> StringCache;
  typedef std::map<std::string, std::pair<bool, bool>> BoolCache;

  static std::string default_filename_;
  TiXmlDocument* document_;
  std::string filename_;
  bool modified_;
  DoubleCache double_cache_;
  IntCache int_cache_;
  StringCache string_cache_;
  BoolCache bool_cache_;

  void WriteNode(const std::string& key, const std::string& value);

  // Don't allow copies.
  const XmlConfig& operator=(const XmlConfig&);
  XmlConfig(const XmlConfig&);
};

#endif  // XMLCONFIG_H_
