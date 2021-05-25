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

#ifndef IDA_STATISTICS_CHOOSER_H_
#define IDA_STATISTICS_CHOOSER_H_

#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <kernwin.hpp>                                          // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT

#include "base/logging.h"
#include "third_party/absl/base/macros.h"

namespace security::bindiff {

class StatisticsChooser : public chooser_t {
 public:
  StatisticsChooser()
      : chooser_t{CH_ATTRS, ABSL_ARRAYSIZE(kColumnWidths), kColumnWidths,
                  kColumnNames, kTitle} {}

  // Refreshes the display of this chooser if visible. Does nothing otherwise.
  static void Refresh() { refresh_chooser(kTitle); }

  // Closes this chooser if visible.
  static void Close() { close_chooser(kTitle); }

 private:
  static constexpr const int kColumnWidths[] = {
      30,              // Key
      20 | CHCOL_DEC,  // Value
  };
  static constexpr const char* const kColumnNames[] = {"Name", "Value"};
  static constexpr const char kTitle[] = "Statistics";

  const void* get_obj_id(size_t* len) const override;
  size_t get_count() const override;
  void get_row(qstrvec_t* cols, int* icon_, chooser_item_attrs_t* attrs,
               size_t n) const override;
};

}  // namespace security::bindiff

#endif  // IDA_STATISTICS_CHOOSER_H_
