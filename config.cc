#include "third_party/zynamics/bindiff/config.h"

#include <string>

#include "base/logging.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/bindiff/utility.h"
#include "third_party/zynamics/bindiff/xmlconfig.h"
#include "third_party/zynamics/binexport/util/canonical_errors.h"
#include "third_party/zynamics/binexport/util/filesystem.h"
#include "third_party/zynamics/binexport/util/status.h"
#include "third_party/zynamics/binexport/util/status_macros.h"

namespace security {
namespace bindiff {

not_absl::Status InitConfig() {
  constexpr char kBinDiff[] = "BinDiff";
  constexpr char kConfigName[] = "bindiff_core.xml";

  std::string path;
  NA_ASSIGN_OR_RETURN(path, GetOrCreateAppDataDirectory(kBinDiff));
  const std::string user_path = JoinPath(path, kConfigName);

  std::string common_path;
  auto common_path_or = GetCommonAppDataDirectory(kBinDiff);
  const bool have_common_path = common_path_or.ok();
  if (have_common_path) {
    common_path = JoinPath(std::move(common_path_or).ValueOrDie(), kConfigName);
  }

  XmlConfig user_config;
  XmlConfig common_config;
  // Try to read user's local config
  const bool have_user_config = user_config.LoadFromFile(user_path).ok();
  // Try to read machine config
  const bool have_common_config =
      have_common_path && common_config.LoadFromFile(common_path).ok();

  bool use_common_config = false;
  if (have_user_config && have_common_config) {
    use_common_config = user_config.ReadInt("/BinDiff/@configVersion", 0) <
                        common_config.ReadInt("/BinDiff/@configVersion", 0);
    LOG_IF(WARNING, use_common_config)
        << "User config version is out of date, using per-machine config";
  } else if (have_user_config) {
    use_common_config = false;
  } else if (have_common_config) {
    use_common_config = true;
  } else {
    return not_absl::NotFoundError("Missing configuration file");
  }

  auto* config = GetConfig();
  if (use_common_config) {
    *config = std::move(common_config);
    std::remove(user_path.c_str());
    not_absl::Status status = CopyFile(common_path, user_path);
    if (!status.ok()) {
      LOG(ERROR) << "Cannot copy per-machine config: "
                 << std::string(status.message());
    }
  } else {
    *config = std::move(user_config);
  }
  return not_absl::OkStatus();
}

XmlConfig* GetConfig() {
  static auto* config = new XmlConfig{};
  return config;
}

}  // namespace bindiff
}  // namespace security
