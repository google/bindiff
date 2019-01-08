#include "third_party/zynamics/bindiff/ida/matched_functions_chooser.h"

#include <cstring>
#include <vector>

#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/strings/str_format.h"
#include "third_party/zynamics/bindiff/ida/ui.h"
#include "third_party/zynamics/binexport/util/format.h"

namespace security {

using binexport::FormatAddress;

namespace bindiff {

constexpr const char MatchedFunctionsChooser::kDeleteAction[];
constexpr const char MatchedFunctionsChooser::kViewFlowGraphsAction[];
constexpr const char MatchedFunctionsChooser::kImportSymbolsCommentsAction[];
constexpr const char
    MatchedFunctionsChooser::kImportSymbolsCommentsExternalAction[];
constexpr const char MatchedFunctionsChooser::kConfirmMatchesAction[];
constexpr const char MatchedFunctionsChooser::kCopyPrimaryAddressAction[];
constexpr const char MatchedFunctionsChooser::kCopySecondaryAddressAction[];

constexpr const int MatchedFunctionsChooser::kColumnWidths[];
constexpr const char* const MatchedFunctionsChooser::kColumnNames[];
constexpr const char MatchedFunctionsChooser::kTitle[];

bool MatchedFunctionsChooser::AttachActionsToPopup(TWidget* widget,
                                                   TPopupMenu* popup_handle) {
  qstring title;
  if (get_widget_type(widget) != BWN_CHOOSER ||
      !get_widget_title(&title, widget) || title != kTitle) {
    return false;
  }
  for (const auto& action :
       {kDeleteAction, kViewFlowGraphsAction, kImportSymbolsCommentsAction,
        kImportSymbolsCommentsExternalAction, kConfirmMatchesAction,
        kCopyPrimaryAddressAction, kCopySecondaryAddressAction}) {
    attach_action_to_popup(widget, popup_handle, action);
  }
  return true;
}

const void* MatchedFunctionsChooser::get_obj_id(size_t* len) const {
  *len = strlen(kTitle);
  return kTitle;
}

size_t MatchedFunctionsChooser::get_count() const {
  return results_->GetNumMatches();
}

void MatchedFunctionsChooser::get_row(qstrvec_t* cols, int* /*icon_*/,
                                      chooser_item_attrs_t* attrs,
                                      size_t n) const {
  const auto match = results_->GetMatchDescription(n);
  (*cols)[0] = absl::StrFormat("%.2f", match.similarity).c_str();
  (*cols)[1] = absl::StrFormat("%.2f", match.confidence).c_str();
  (*cols)[2] = GetChangeDescription(match.change_type).c_str();
  (*cols)[3] = FormatAddress(match.address_primary).c_str();
  (*cols)[4] = match.name_primary.c_str();
  (*cols)[5] = FormatAddress(match.address_secondary).c_str();
  (*cols)[6] = match.name_secondary.c_str();
  (*cols)[7] = match.comments_ported ? "X" : " ";
  (*cols)[8] =
      match.algorithm_name.substr(match.algorithm_name.size() > 10 ? 10 : 0)
          .c_str();
  (*cols)[9] = std::to_string(match.basic_block_count).c_str();
  (*cols)[10] = std::to_string(match.basic_block_count_primary).c_str();
  (*cols)[11] = std::to_string(match.basic_block_count_secondary).c_str();
  (*cols)[12] = std::to_string(match.instruction_count).c_str();
  (*cols)[13] = std::to_string(match.instruction_count_primary).c_str();
  (*cols)[14] = std::to_string(match.instruction_count_secondary).c_str();
  (*cols)[15] = std::to_string(match.edge_count).c_str();
  (*cols)[16] = std::to_string(match.edge_count_primary).c_str();
  (*cols)[17] = std::to_string(match.edge_count_secondary).c_str();
  attrs->color = GetMatchColor(match.similarity);
  if (match.manual) {
    attrs->flags |= CHITEM_BOLD;
  }
}

ea_t MatchedFunctionsChooser::get_ea(size_t n) const {
  return results_->GetMatchDescription(n).address_primary;
}

chooser_t::cbres_t MatchedFunctionsChooser::refresh(sizevec_t* sel) {
  if (sel && !sel->empty() && results_->should_reset_selection()) {
    sel->resize(1);  // Only keep the first item in selection
    results_->set_should_reset_selection(false);
  }
  return ALL_CHANGED;
}

}  // namespace bindiff
}  // namespace security
