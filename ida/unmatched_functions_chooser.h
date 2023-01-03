// Copyright 2011-2023 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#ifndef IDA_UNMATCHED_FUNCTIONS_CHOOSER_H_
#define IDA_UNMATCHED_FUNCTIONS_CHOOSER_H_

#include "third_party/absl/base/macros.h"
#include "third_party/zynamics/bindiff/ida/results.h"
// clang-format off
#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <kernwin.hpp>                                          // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT
// clang-format on

namespace security::bindiff {
namespace internal {

static constexpr const int kUnmatchedChooserColumnWidths[] = {
    10 | CHCOL_HEX,  // EA
    30,              // Name
    5 | CHCOL_DEC,   // Basic Blocks
    6 | CHCOL_DEC,   // Instructions
    5 | CHCOL_DEC,   // Edges
};

static constexpr const char* const kUnmatchedChooserColumnNames[] = {
    "EA", "Name", "Basic Blocks", "Instructions", "Edges",
};

void UnmatchedDescriptionToIdaRow(const Results::UnmatchedDescription& desc,
                                  qstrvec_t* cols, int* icon,
                                  chooser_item_attrs_t* attrs);

}  // namespace internal

// Base class for non-modal multi-selection choosers for displaying unmatched
// functions. This is using CRTP to prevent code duplication in the static
// methods.
template <typename ChooserT>
class UnmatchedChooserMultiBase : public chooser_multi_t {
 public:
  // Attaches the chooser actions to IDA's popup menu. To be called from a HT_UI
  // notification point.
  static bool AttachActionsToPopup(TWidget* widget, TPopupMenu* popup_handle);

  // Refreshes the display of this chooser if visible. Does nothing otherwise.
  static void Refresh() { refresh_chooser(ChooserT::kTitle); }

  // Closes this chooser if visible.
  static void Close() { close_chooser(ChooserT::kTitle); }

  UnmatchedChooserMultiBase()
      : chooser_multi_t{
            CH_ATTRS, ABSL_ARRAYSIZE(internal::kUnmatchedChooserColumnWidths),
            internal::kUnmatchedChooserColumnWidths,
            internal::kUnmatchedChooserColumnNames, ChooserT::kTitle} {}

 private:
  virtual Results::UnmatchedDescription GetDescription(size_t index) const = 0;

  const void* get_obj_id(size_t* len) const override {
    *len = strlen(ChooserT::kTitle);
    return ChooserT::kTitle;
  }

  void get_row(qstrvec_t* cols, int* icon_, chooser_item_attrs_t* attrs,
               size_t n) const override {
    internal::UnmatchedDescriptionToIdaRow(GetDescription(n), cols, icon_,
                                           attrs);
  }
};

template <typename ChooserT>
inline bool UnmatchedChooserMultiBase<ChooserT>::AttachActionsToPopup(
    TWidget* widget, TPopupMenu* popup_handle) {
  qstring title;
  if (get_widget_type(widget) != BWN_CHOOSER ||
      !get_widget_title(&title, widget) || title != ChooserT::kTitle) {
    return false;
  }
  for (const auto& action :
       {ChooserT::kCopyAddressAction, ChooserT::kAddMatchAction}) {
    attach_action_to_popup(widget, popup_handle, action);
  }
  return true;
}

class UnmatchedFunctionsChooserPrimary
    : public UnmatchedChooserMultiBase<UnmatchedFunctionsChooserPrimary> {
 public:
  // Action names
  static constexpr const char kCopyAddressAction[] =
      "bindiff:primary_unmatched_copy_address";
  static constexpr const char kAddMatchAction[] =
      "bindiff:primary_unmatched_add_match";

  using UnmatchedChooserMultiBase::UnmatchedChooserMultiBase;

 private:
  friend class UnmatchedChooserMultiBase<UnmatchedFunctionsChooserPrimary>;

  static constexpr const char kTitle[] = "Primary Unmatched";

  size_t get_count() const override;

  // Default action if the user pressed the enter key/double clicked on a match.
  ea_t get_ea(size_t n) const override;

  Results::UnmatchedDescription GetDescription(size_t index) const override;
};

class UnmatchedFunctionsChooserSecondary
    : public UnmatchedChooserMultiBase<UnmatchedFunctionsChooserSecondary> {
 public:
  // Action names
  static constexpr const char kCopyAddressAction[] =
      "bindiff:secondary_unmatched_copy_address";
  static constexpr const char kAddMatchAction[] =
      "bindiff:secondary_unmatched_add_match";

  using UnmatchedChooserMultiBase::UnmatchedChooserMultiBase;

 private:
  friend class UnmatchedChooserMultiBase<UnmatchedFunctionsChooserSecondary>;

  static constexpr const char kTitle[] = "Secondary Unmatched";

  size_t get_count() const override;

  Results::UnmatchedDescription GetDescription(size_t index) const override;
};

// Base class for the modal choosers that allow selecting an unmachted function
// to assign a manual match to. The derived classes show the same data but do
// not feature a context menu.
template <typename ChooserT>
class UnmatchedChooserBase : public chooser_t {
 public:
  UnmatchedChooserBase()
      : chooser_t{CH_MODAL | CH_KEEP,
                  ABSL_ARRAYSIZE(internal::kUnmatchedChooserColumnWidths),
                  internal::kUnmatchedChooserColumnWidths,
                  internal::kUnmatchedChooserColumnNames, ChooserT::kTitle} {}

 private:
  virtual Results::UnmatchedDescription GetDescription(size_t index) const = 0;

  void get_row(qstrvec_t* cols, int* icon_, chooser_item_attrs_t* attrs,
               size_t n) const override {
    internal::UnmatchedDescriptionToIdaRow(GetDescription(n), cols, icon_,
                                           attrs);
  }
};

class UnmatchedFunctionsAddMatchChooserPrimary
    : public UnmatchedChooserBase<UnmatchedFunctionsAddMatchChooserPrimary> {
 public:
  using UnmatchedChooserBase::UnmatchedChooserBase;

 private:
  friend class UnmatchedChooserBase<UnmatchedFunctionsAddMatchChooserPrimary>;

  static constexpr const char kTitle[] = "Select unmatched function in primary";

  size_t get_count() const override;

  Results::UnmatchedDescription GetDescription(size_t index) const override;
};

class UnmatchedFunctionsAddMatchChooserSecondary
    : public UnmatchedChooserBase<UnmatchedFunctionsAddMatchChooserSecondary> {
 public:
  using UnmatchedChooserBase::UnmatchedChooserBase;

 private:
  friend class UnmatchedChooserBase<UnmatchedFunctionsAddMatchChooserSecondary>;

  static constexpr const char kTitle[] =
      "Select unmatched function in secondary";

  size_t get_count() const override;

  Results::UnmatchedDescription GetDescription(size_t index) const override;
};

}  // namespace security::bindiff

#endif  // IDA_UNMATCHED_FUNCTIONS_CHOOSER_H_
