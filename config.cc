#include "third_party/zynamics/bindiff/config.h"

#include <string>

#include "base/logging.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/bindiff/utility.h"
#include "third_party/zynamics/bindiff/xmlconfig.h"
#include "third_party/zynamics/binexport/util/canonical_errors.h"
#include "third_party/zynamics/binexport/util/filesystem.h"
#include "third_party/zynamics/binexport/util/status.h"

namespace security {
namespace bindiff {

not_absl::Status InitConfig() {
  const string config_filename = "bindiff_core.xml";

  const string user_path = absl::StrCat(
      GetDirectory(PATH_APPDATA, "BinDiff", /*create=*/true), config_filename);
  const string common_path = absl::StrCat(
      GetDirectory(PATH_COMMONAPPDATA, "BinDiff", /*create=*/false),
      config_filename);

  XmlConfig user_config;
  XmlConfig common_config;
  // Try to read user's local config
  bool have_user_config = user_config.LoadFromFile(user_path).ok();
  // Try to read machine config
  bool have_common_config = common_config.LoadFromFile(common_path).ok();

  bool use_common_config = false;
  if (have_user_config && have_common_config) {
    use_common_config = user_config.ReadInt("/BinDiff/@configVersion", 0) <
                        common_config.ReadInt("/BinDiff/@configVersion", 0);
    LOG(WARNING) << "BinDiff user config version is out of date, using "
                    "per-machine config";
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
                 << string(status.message());
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
