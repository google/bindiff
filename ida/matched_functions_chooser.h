#ifndef IDA_MATCHED_FUNCTIONS_CHOOSER_H_
#define IDA_MATCHED_FUNCTIONS_CHOOSER_H_

#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <kernwin.hpp>                                          // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT

#include "third_party/absl/base/macros.h"
#include "third_party/zynamics/bindiff/ida/results.h"

namespace security {
namespace bindiff {

class MatchedFunctionsChooser : public chooser_multi_t {
 public:
  explicit MatchedFunctionsChooser(Results* results)
      : chooser_multi_t(CH_KEEP | CH_ATTRS, ABSL_ARRAYSIZE(kColumnWidths),
                        kColumnWidths, kColumnNames, kTitle),
        results_(results) {}

 private:
  static constexpr const int kColumnWidths[] = {
    5 | CHCOL_DEC,   // Similarity
    3 | CHCOL_DEC,   // Confidence
    3,               // Change
    10 | CHCOL_HEX,  // EA Primary
    30,              // Name Primary
    10 | CHCOL_HEX,  // EA Secondary
    30,              // Name Secondary
    1,               // Comments Ported
    30,              // Algorithm
    5 | CHCOL_DEC,   // Matched Basic Blocks
    5 | CHCOL_DEC,   // Basic Blocks Primary
    5 | CHCOL_DEC,   // Basic Blocks Secondary
    6 | CHCOL_DEC,   // Matched Instructions
    6 | CHCOL_DEC,   // Instructions Primary
    6 | CHCOL_DEC,   // Instructions Secondary
    5 | CHCOL_DEC,   // Matched Edges
    5 | CHCOL_DEC,   // Edges Primary
    5 | CHCOL_DEC,   // Edges Secondary
  };
  static constexpr const char* const kColumnNames[] = {
      "Similarity",           "Confidence",           "Change",
      "EA Primary",           "Name Primary",         "EA Secondary",
      "Name Secondary",       "Comments Ported",      "Algorithm",
      "Matched Basic Blocks", "Basic Blocks Primary", "Basic Blocks Secondary",
      "Matched Instructions", "Instructions Primary", "Instructions Secondary",
      "Matched Edges",        "Edges Primary",        "Edges Secondary",
  };
  static constexpr const char kTitle[] = "Matched Functions";

  const void* get_obj_id(size_t* len) const override;
  size_t get_count() const override;
  void get_row(qstrvec_t* cols, int* icon_, chooser_item_attrs_t* attrs,
               size_t n) const override;

  Results* results_;
};

}  // namespace bindiff
}  // namespace security

#endif  // IDA_MATCHED_FUNCTIONS_CHOOSER_H_
