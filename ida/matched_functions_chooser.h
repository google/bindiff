#ifndef IDA_MATCHED_FUNCTIONS_CHOOSER_H_
#define IDA_MATCHED_FUNCTIONS_CHOOSER_H_

#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <kernwin.hpp>                                          // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT

#include "base/logging.h"
#include "third_party/absl/base/macros.h"
#include "third_party/zynamics/bindiff/ida/results.h"

namespace security {
namespace bindiff {

class MatchedFunctionsChooser : public chooser_multi_t {
 public:
  // Action names
  static constexpr const char kDeleteAction[] = "bindiff:match_delete";
  static constexpr const char kViewFlowGraphsAction[] =
      "bindiff:view_flow_graphs";
  static constexpr const char kImportSymbolsCommentsAction[] =
      "bindiff:import_symbols_comments";
  static constexpr const char kImportSymbolsCommentsExternalAction[] =
      "bindiff:import_symbols_comments_external";
  static constexpr const char kConfirmMatchesAction[] =
      "bindiff:confirm_matches";
  static constexpr const char kCopyPrimaryAddressAction[] =
      "bindiff:copy_primary_address";
  static constexpr const char kCopySecondaryAddressAction[] =
      "bindiff:copy_secondary_address";

  explicit MatchedFunctionsChooser(Results* results)
      : chooser_multi_t{CH_ATTRS, ABSL_ARRAYSIZE(kColumnWidths), kColumnWidths,
                        kColumnNames, kTitle},
        results_{ABSL_DIE_IF_NULL(results)} {}

  // Attaches the chooser actions to IDA's popup menu. To be called from a HT_UI
  // notification point.
  static bool AttachActionsToPopup(TWidget* widget, TPopupMenu* popup_handle);

  // Refreshes the display of this chooser if visible. Does nothing otherwise.
  static void Refresh() { refresh_chooser(kTitle); }

 private:
  static constexpr const int kColumnWidths[] = {
    5 | CHCOL_DEC,   // Similarity
    3 | CHCOL_DEC,   // Confidence
    4,               // Change
    10 | CHCOL_HEX,  // EA Primary
    30,              // Name Primary
    10 | CHCOL_HEX,  // EA Secondary
    30,              // Name Secondary
    1,               // Comments Ported
    28,              // Algorithm
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

  // Default action if the user pressed the enter key/double clicked on a match.
  ea_t get_ea(size_t n) const override;

  chooser_t::cbres_t refresh(sizevec_t* sel) override;

  Results* results_;
};

}  // namespace bindiff
}  // namespace security

#endif  // IDA_MATCHED_FUNCTIONS_CHOOSER_H_
