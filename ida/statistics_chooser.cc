#include "third_party/zynamics/bindiff/ida/statistics_chooser.h"

#include <cstring>
#include <vector>

#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/bindiff/ida/main_plugin.h"

namespace security::bindiff {

const void* StatisticsChooser::get_obj_id(size_t* len) const {
  *len = strlen(kTitle);
  return kTitle;
}

size_t StatisticsChooser::get_count() const {
  return Plugin::instance()->results()->GetNumStatistics();
}

void StatisticsChooser::get_row(qstrvec_t* cols, int* /* icon_ */,
                                chooser_item_attrs_t* /* attrs */,
                                size_t n) const {
  auto statistic = Plugin::instance()->results()->GetStatisticDescription(n);
  (*cols)[0] = statistic.name.c_str();
  (*cols)[1] = (statistic.is_count ? absl::StrCat(statistic.count)
                                   : absl::StrCat(statistic.value))
                   .c_str();
}

}  // namespace security::bindiff
