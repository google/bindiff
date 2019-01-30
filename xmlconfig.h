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

namespace security {
namespace bindiff {

class XmlConfig {
 public:
  XmlConfig();
  virtual ~XmlConfig();

  XmlConfig& operator=(XmlConfig&&);

  // Initializes the configuration from an XML string.
  not_absl::Status LoadFromString(const string& data);

  not_absl::Status LoadFromFile(const string& filename);
  not_absl::Status LoadFromFileWithDefaults(const string& filename,
                                            const string& defaults);

  double ReadDouble(const string& key, double default_value) const;
  int ReadInt(const string& key, int default_value) const;
  string ReadString(const string& key, const string& default_value) const;
  bool ReadBool(const string& key, bool default_value) const;

  // Access to the underlying raw document object.
  TiXmlDocument* document();
  const TiXmlDocument* document() const;

 private:
  std::unique_ptr<TiXmlDocument> document_;
  mutable absl::flat_hash_map<string, double> double_cache_;
  mutable absl::flat_hash_map<string, int> int_cache_;
  mutable absl::flat_hash_map<string, string> string_cache_;
  mutable absl::flat_hash_map<string, bool> bool_cache_;
};

}  // namespace bindiff
}  // namespace security

#endif  // XMLCONFIG_H_
