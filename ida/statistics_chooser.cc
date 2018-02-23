#include "third_party/zynamics/bindiff/ida/statistics_chooser.h"

#include <cstring>
#include <vector>

constexpr const int StatisticsChooser::kColumnWidths[];
constexpr const char* const StatisticsChooser::kColumnNames[];
constexpr const char StatisticsChooser::kTitle[];

const void* StatisticsChooser::get_obj_id(size_t* len) const {
  *len = strlen(kTitle);
  return kTitle;
}

size_t StatisticsChooser::get_count() const {
  return results_ ? results_->GetNumStatistics() : 0;
}

void StatisticsChooser::get_row(qstrvec_t* cols, int* icon_,
                                chooser_item_attrs_t* attrs, size_t n) const {
  if (!results_) {
    return;
  }
  Results::StatisticDescription statistic =
      results_->GetStatisticDescription(n);
  (*cols)[0] = statistic.name.c_str();
  (*cols)[1] = (statistic.is_count ? std::to_string(statistic.count)
                                   : std::to_string(statistic.value))
                   .c_str();
}
