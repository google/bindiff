// Copyright 2011-2020 Google LLC
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

#include "third_party/zynamics/bindiff/ida/matched_functions_chooser.h"

#include <cstring>
#include <vector>

#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/strings/str_format.h"
#include "third_party/zynamics/bindiff/ida/main_plugin.h"
#include "third_party/zynamics/bindiff/ida/results.h"
#include "third_party/zynamics/bindiff/ida/statistics_chooser.h"
#include "third_party/zynamics/bindiff/ida/ui.h"
#include "third_party/zynamics/bindiff/ida/unmatched_functions_chooser.h"
#include "third_party/zynamics/binexport/util/format.h"

namespace security::bindiff {

using binexport::FormatAddress;

int idaapi DeleteMatchesAction::activate(action_activation_ctx_t* context) {
  auto* results = Plugin::instance()->results();
  const auto& ida_selection = context->chooser_selection;
  if (!results || ida_selection.empty()) {
    return 0;
  }
  if (not_absl::Status status = results->DeleteMatches(
          absl::MakeConstSpan(&ida_selection.front(), ida_selection.size()));
      !status.ok()) {
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

MatchedFunctionsChooser::MatchedFunctionsChooser()
    : chooser_multi_t{CH_ATTRS | CH_CAN_DEL, ABSL_ARRAYSIZE(kColumnWidths),
                      kColumnWidths, kColumnNames, kTitle} {
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
           //       in del(). When attaching normally, DEL cannot be set as
           //       the accelerator.
           ViewCallGraphAction::kName,
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
           ViewCallGraphAction::MakeActionDesc(),
           ViewFlowGraphsAction::MakeActionDesc(),
           ImportSymbolsCommentsAction::MakeActionDesc(),
           ImportSymbolsCommentsExternalAction::MakeActionDesc(),
           ConfirmMatchesAction::MakeActionDesc(),
           CopyPrimaryAddressAction::MakeActionDesc(),
           CopySecondaryAddressAction::MakeActionDesc(),
       }) {
    register_action(action);
  }
}

const void* MatchedFunctionsChooser::get_obj_id(size_t* len) const {
  *len = strlen(kTitle);
  return kTitle;
}

size_t MatchedFunctionsChooser::get_count() const {
  return Plugin::instance()->results()->GetNumMatches();
}

void MatchedFunctionsChooser::get_row(qstrvec_t* cols, int* /*icon_*/,
                                      chooser_item_attrs_t* attrs,
                                      size_t n) const {
  const auto match = Plugin::instance()->results()->GetMatchDescription(n);
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
  (*cols)[9] = absl::StrCat(match.basic_block_count).c_str();
  (*cols)[10] = absl::StrCat(match.basic_block_count_primary).c_str();
  (*cols)[11] = absl::StrCat(match.basic_block_count_secondary).c_str();
  (*cols)[12] = absl::StrCat(match.instruction_count).c_str();
  (*cols)[13] = absl::StrCat(match.instruction_count_primary).c_str();
  (*cols)[14] = absl::StrCat(match.instruction_count_secondary).c_str();
  (*cols)[15] = absl::StrCat(match.edge_count).c_str();
  (*cols)[16] = absl::StrCat(match.edge_count_primary).c_str();
  (*cols)[17] = absl::StrCat(match.edge_count_secondary).c_str();
  attrs->color = GetMatchColor(match.similarity);
  if (match.manual) {
    attrs->flags |= CHITEM_BOLD;
  }
}

ea_t idaapi MatchedFunctionsChooser::get_ea(size_t n) const {
  return Plugin::instance()->results()->GetMatchDescription(n).address_primary;
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
  auto* results = Plugin::instance()->results();
  if (sel && !sel->empty() && results->should_reset_selection()) {
    sel->resize(1);  // Only keep the first item in selection
    results->set_should_reset_selection(false);
  }
  return ALL_CHANGED;
}

}  // namespace security::bindiff
