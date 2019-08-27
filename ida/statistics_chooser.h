#ifndef IDA_STATISTICS_CHOOSER_H_
#define IDA_STATISTICS_CHOOSER_H_

#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <kernwin.hpp>                                          // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT

#include "base/logging.h"
#include "third_party/absl/base/macros.h"

namespace security {
namespace bindiff {

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
      10,              // Key
      20 | CHCOL_DEC,  // Value
  };
  static constexpr const char* const kColumnNames[] = {"Name", "Value"};
  static constexpr const char kTitle[] = "Statistics";

  const void* get_obj_id(size_t* len) const override;
  size_t get_count() const override;
  void get_row(qstrvec_t* cols, int* icon_, chooser_item_attrs_t* attrs,
               size_t n) const override;
};

}  // namespace bindiff
}  // namespace security

#endif  // IDA_STATISTICS_CHOOSER_H_
