#include "third_party/zynamics/bindiff/ida/matched_functions_chooser.h"

#include <cstring>
#include <vector>

#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/strings/str_format.h"
#include "third_party/zynamics/bindiff/ida/results.h"
#include "third_party/zynamics/bindiff/ida/statistics_chooser.h"
#include "third_party/zynamics/bindiff/ida/ui.h"
#include "third_party/zynamics/bindiff/ida/unmatched_functions_chooser.h"
#include "third_party/zynamics/binexport/util/format.h"

namespace security {

using binexport::FormatAddress;

namespace bindiff {

constexpr const char DeleteMatchesAction::kName[];
constexpr const char DeleteMatchesAction::kLabel[];
constexpr const char DeleteMatchesAction::kShortCut[];
constexpr const char* DeleteMatchesAction::kTooltip;

int idaapi DeleteMatchesAction::activate(action_activation_ctx_t* context) {
  const auto& ida_selection = context->chooser_selection;
  if (!results_ || ida_selection.empty()) {
    return 0;
  }
  not_absl::Status status = results_->DeleteMatches(
      absl::MakeConstSpan(&ida_selection.front(), ida_selection.size()));
  if (!status.ok()) {
    const std::string message(status.message());
    LOG(INFO) << "Error: " << message;
    warning("Error: %s\n", message.c_str());
    return 0;
  }
  // Need to refresh choosers
  MatchedFunctionsChooser::Refresh();
  UnmatchedFunctionsChooserPrimary::Refresh();
  UnmatchedFunctionsChooserSecondary::Refresh();
  StatisticsChooser::Refresh();
  return 1;
}

constexpr const int MatchedFunctionsChooser::kColumnWidths[];
constexpr const char* const MatchedFunctionsChooser::kColumnNames[];
constexpr const char MatchedFunctionsChooser::kTitle[];

MatchedFunctionsChooser::MatchedFunctionsChooser(Results* results)
    : chooser_multi_t{CH_ATTRS | CH_CAN_DEL, ABSL_ARRAYSIZE(kColumnWidths),
                      kColumnWidths, kColumnNames, kTitle},
      results_{ABSL_DIE_IF_NULL(results)} {
  popup_names[POPUP_DEL] = DeleteMatchesAction::kLabel;
}

bool MatchedFunctionsChooser::AttachActionsToPopup(TWidget* widget,
                                                   TPopupMenu* popup_handle) {
  qstring title;
  if (get_widget_type(widget) != BWN_CHOOSER ||
      !get_widget_title(&title, widget) || title != kTitle) {
    return false;
  }
  for (const auto& action : {
           // Note: Do not attach DeleteMatchesAction here, as this is invoked
           //       in del().
           ViewFlowGraphsAction::kName,
           ImportSymbolsCommentsAction::kName,
           ImportSymbolsCommentsExternalAction::kName,
           ConfirmMatchesAction::kName,
           CopyPrimaryAddressAction::kName,
           CopySecondaryAddressAction::kName,
       }) {
    attach_action_to_popup(widget, popup_handle, action);
  }
  return true;
}

void MatchedFunctionsChooser::RegisterActions() {
  for (const auto& action : {
           DeleteMatchesAction::MakeActionDesc(),
           ViewFlowGraphsAction::MakeActionDesc(),
           ImportSymbolsCommentsAction::MakeActionDesc(),
           ImportSymbolsCommentsExternalAction::MakeActionDesc(),
           CopyPrimaryAddressAction::MakeActionDesc(),
           CopySecondaryAddressAction::MakeActionDesc(),
       }) {
    register_action(action);
  }
}

bool idaapi MatchedFunctionsChooser::init() {
  DeleteMatchesAction::instance()->results_ = results_;
  return true;
}

void idaapi MatchedFunctionsChooser::closed() {
  DeleteMatchesAction::instance()->results_ = nullptr;
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

ea_t idaapi MatchedFunctionsChooser::get_ea(size_t n) const {
  return results_->GetMatchDescription(n).address_primary;
}

chooser_t::cbres_t idaapi MatchedFunctionsChooser::del(sizevec_t* sel) {
  if (sel->empty()) {
    return NOTHING_CHANGED;
  }
  action_activation_ctx_t ctx;
  ctx.chooser_selection = *sel;
  return DeleteMatchesAction::instance()->PerformActivate(&ctx) == 0
             ? NOTHING_CHANGED
             : SELECTION_CHANGED;
}

chooser_t::cbres_t MatchedFunctionsChooser::refresh(sizevec_t* sel) {
  if (sel && !sel->empty() && results_->should_reset_selection()) {
    sel->resize(1);  // Only keep the first item in selection
    results_->set_should_reset_selection(false);
  }
  return ALL_CHANGED;
}

constexpr const char ViewFlowGraphsAction::kName[];
constexpr const char ViewFlowGraphsAction::kLabel[];
constexpr const char ViewFlowGraphsAction::kShortCut[];
constexpr const char* ViewFlowGraphsAction::kTooltip;

constexpr const char ImportSymbolsCommentsAction::kName[];
constexpr const char ImportSymbolsCommentsAction::kLabel[];
constexpr const char ImportSymbolsCommentsAction::kShortCut[];
constexpr const char* ImportSymbolsCommentsAction::kTooltip;

constexpr const char ImportSymbolsCommentsExternalAction::kName[];
constexpr const char ImportSymbolsCommentsExternalAction::kLabel[];
constexpr const char ImportSymbolsCommentsExternalAction::kShortCut[];
constexpr const char* ImportSymbolsCommentsExternalAction::kTooltip;

constexpr const char ConfirmMatchesAction::kName[];
constexpr const char ConfirmMatchesAction::kLabel[];
constexpr const char ConfirmMatchesAction::kShortCut[];
constexpr const char* ConfirmMatchesAction::kTooltip;

constexpr const char CopyPrimaryAddressAction::kName[];
constexpr const char CopyPrimaryAddressAction::kLabel[];
constexpr const char CopyPrimaryAddressAction::kShortCut[];
constexpr const char* CopyPrimaryAddressAction::kTooltip;

constexpr const char CopySecondaryAddressAction::kName[];
constexpr const char CopySecondaryAddressAction::kLabel[];
constexpr const char CopySecondaryAddressAction::kShortCut[];
constexpr const char* CopySecondaryAddressAction::kTooltip;

}  // namespace bindiff
}  // namespace security
