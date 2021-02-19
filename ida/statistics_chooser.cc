// Copyright 2011-2021 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
