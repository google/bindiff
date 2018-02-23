#ifndef IDA_UNMATCHED_FUNCTIONS_CHOOSER_H_
#define IDA_UNMATCHED_FUNCTIONS_CHOOSER_H_

#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <kernwin.hpp>                                          // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT

#include "third_party/absl/base/macros.h"
#include "third_party/zynamics/bindiff/ida/results.h"

class UnmatchedFunctionsChooserBase : public chooser_multi_t {
 protected:
  UnmatchedFunctionsChooserBase(const char* title, Results* results)
      : chooser_multi_t(CH_KEEP | CH_ATTRS, ABSL_ARRAYSIZE(kColumnWidths),
                        kColumnWidths, kColumnNames, title),
        title_(title),
        results_(results) {}

  Results* results_;

 private:
  static constexpr const int kColumnWidths[] = {
      10 | CHCOL_HEX,  // EA
      30,              // Name
      5 | CHCOL_DEC,   // Basic Blocks
      6 | CHCOL_DEC,   // Instructions
      5 | CHCOL_DEC,   // Edges
  };
  static constexpr const char* const kColumnNames[] = {
      "EA", "Name", "Basic Blocks", "Instructions", "Edges",
  };

  virtual Results::UnmatchedDescription GetDescription(size_t index) const = 0;

  const void* get_obj_id(size_t* len) const override;
  void get_row(qstrvec_t* cols, int* icon_, chooser_item_attrs_t* attrs,
               size_t n) const override;

  string title_;
};

class UnmatchedFunctionsChooserPrimary : public UnmatchedFunctionsChooserBase {
 public:
  explicit UnmatchedFunctionsChooserPrimary(Results* results)
      : UnmatchedFunctionsChooserBase("Primary Unmatched", results) {}

 private:
  size_t get_count() const override;
  Results::UnmatchedDescription GetDescription(size_t index) const override;
};

class UnmatchedFunctionsChooserSecondary
    : public UnmatchedFunctionsChooserBase {
 public:
  explicit UnmatchedFunctionsChooserSecondary(Results* results)
      : UnmatchedFunctionsChooserBase("Secondary Unmatched", results) {}

 private:
  size_t get_count() const override;
  Results::UnmatchedDescription GetDescription(size_t index) const override;
};

#endif  // IDA_UNMATCHED_FUNCTIONS_CHOOSER_H_
