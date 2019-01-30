#ifndef CONFIG_H_
#define CONFIG_H_

#include "third_party/zynamics/bindiff/xmlconfig.h"
#include "third_party/zynamics/binexport/util/status.h"

namespace security {
namespace bindiff {

// Initializes the config file. First tries to read per-user configuration and
// falls back to per-machine configuration if not found.
not_absl::Status InitConfig();

// Returns the current application global configuration. If InitConfig() has not
// been called, this returns an empty configuration.
XmlConfig* GetConfig();

}  // namespace bindiff
}  // namespace security

#endif  // CONFIG_H_
