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

#ifndef IDA_MATCHED_FUNCTIONS_CHOOSER_H_
#define IDA_MATCHED_FUNCTIONS_CHOOSER_H_

#include <functional>

// clang-format off
#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <kernwin.hpp>                                          // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT
// clang-format on

#include "base/logging.h"
#include "third_party/absl/base/macros.h"
#include "third_party/zynamics/binexport/ida/ui.h"

namespace security::bindiff {

class MatchedFunctionsChooser : public chooser_multi_t {
 public:
  MatchedFunctionsChooser();

  // Attaches the chooser actions to IDA's popup menu. To be called from a HT_UI
  // notification point.
  static bool AttachActionsToPopup(TWidget* widget, TPopupMenu* popup_handle);

  // Registers chooser-specific actions.
  static void RegisterActions();

  // Refreshes the display of this chooser if visible. Does nothing otherwise.
  static void Refresh() { refresh_chooser(kTitle); }

  // Closes this chooser if visible.
  static void Close() { close_chooser(kTitle); }

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

  const void* idaapi get_obj_id(size_t* len) const override;
  size_t idaapi get_count() const override;
  void idaapi get_row(qstrvec_t* cols, int* icon_, chooser_item_attrs_t* attrs,
                      size_t n) const override;

  // Default action if the user pressed the enter key/double clicked on a match.
  ea_t idaapi get_ea(size_t n) const override;

  chooser_t::cbres_t idaapi del(sizevec_t* sel) override;

  chooser_t::cbres_t idaapi refresh(sizevec_t* sel) override;
};

class DeleteMatchesAction : public ActionHandler<DeleteMatchesAction> {
 public:
  static constexpr const char kName[] = "bindiff:match_delete";
  static constexpr const char kLabel[] = "~D~elete match(es)";
  static constexpr const char kShortCut[] = "";  // Placeholder, IDA uses "DEL"
  static constexpr const char* kTooltip = nullptr;

 private:
  friend class MatchedFunctionsChooser;

  int idaapi activate(action_activation_ctx_t* context) override;
};

class ViewCallGraphAction : public ActionHandler<ViewCallGraphAction> {
 public:
  static constexpr const char kName[] = "bindiff:view_call_graphs";
  static constexpr const char kLabel[] = "View context in call ~g~raphs";
  static constexpr const char kShortCut[] = "";
  static constexpr const char* kTooltip = nullptr;

 private:
  int idaapi activate(action_activation_ctx_t* context) override;
};

class ViewFlowGraphsAction : public ActionHandler<ViewFlowGraphsAction> {
 public:
  static constexpr const char kName[] = "bindiff:view_flow_graphs";
  static constexpr const char kLabel[] = "~V~iew flow graphs";
  static constexpr const char kShortCut[] = "";
  static constexpr const char* kTooltip = nullptr;

 private:
  int idaapi activate(action_activation_ctx_t* context) override;
};

class ImportSymbolsCommentsAction
    : public ActionHandler<ImportSymbolsCommentsAction> {
 public:
  static constexpr const char kName[] = "bindiff:import_symbols_comments";
  static constexpr const char kLabel[] = "Im~p~ort symbols/comments";
  static constexpr const char kShortCut[] = "";
  static constexpr const char* kTooltip = nullptr;

 private:
  int idaapi activate(action_activation_ctx_t* context) override;
};

class ImportSymbolsCommentsExternalAction
    : public ActionHandler<ImportSymbolsCommentsExternalAction> {
 public:
  static constexpr const char kName[] =
      "bindiff:import_symbols_comments_external";
  static constexpr const char kLabel[] =
      "Import symbols/comments as ~e~xternal library";
  static constexpr const char kShortCut[] = "";
  static constexpr const char* kTooltip = nullptr;

 private:
  int idaapi activate(action_activation_ctx_t* context) override;
};

class ConfirmMatchesAction : public ActionHandler<ConfirmMatchesAction> {
 public:
  static constexpr const char kName[] = "bindiff:confirm_matches";
  static constexpr const char kLabel[] = "~C~onfirm match(es)";
  static constexpr const char kShortCut[] = "";
  static constexpr const char* kTooltip = nullptr;

 private:
  int idaapi activate(action_activation_ctx_t* context) override;
};

class CopyPrimaryAddressAction
    : public ActionHandler<CopyPrimaryAddressAction> {
 public:
  static constexpr const char kName[] = "bindiff:copy_primary_address";
  static constexpr const char kLabel[] = "Copy ~p~rimary address";
  static constexpr const char kShortCut[] = "";
  static constexpr const char* kTooltip = nullptr;

 private:
  int idaapi activate(action_activation_ctx_t* context) override;
};

class CopySecondaryAddressAction
    : public ActionHandler<CopySecondaryAddressAction> {
 public:
  static constexpr const char kName[] = "bindiff:copy_secondary_address";
  static constexpr const char kLabel[] = "Copy ~s~econdary address";
  static constexpr const char kShortCut[] = "";
  static constexpr const char* kTooltip = nullptr;

 private:
  int idaapi activate(action_activation_ctx_t* context) override;
};

}  // namespace security::bindiff

#endif  // IDA_MATCHED_FUNCTIONS_CHOOSER_H_
