#ifndef IDA_STATISTICS_CHOOSER_H_
#define IDA_STATISTICS_CHOOSER_H_

#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <kernwin.hpp>                                          // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT

#include "third_party/absl/base/macros.h"
#include "third_party/zynamics/bindiff/ida/results.h"

class StatisticsChooser : public chooser_t {
 public:
  explicit StatisticsChooser(Results* results)
      : chooser_t(CH_KEEP | CH_ATTRS, ABSL_ARRAYSIZE(kColumnWidths),
                  kColumnWidths, kColumnNames, kTitle),
        results_(results) {}

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

  Results* results_;
};

#endif  // IDA_STATISTICS_CHOOSER_H_
