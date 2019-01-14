#include "third_party/zynamics/bindiff/ida/unmatched_functions_chooser.h"

#include <cstring>
#include <vector>

#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/bindiff/ida/ui.h"

namespace security {
namespace bindiff {
namespace internal {

void UnmatchedDescriptionToIdaRow(const Results::UnmatchedDescription& desc,
                                  qstrvec_t* cols, int* /* icon */,
                                  chooser_item_attrs_t* /* attrs */) {
  (*cols)[0] = FormatAddress(desc.address).c_str();
  (*cols)[1] = desc.name.c_str();
  (*cols)[2] = std::to_string(desc.basic_block_count).c_str();
  (*cols)[3] = std::to_string(desc.instruction_count).c_str();
  (*cols)[4] = std::to_string(desc.edge_count).c_str();
}

}  // namespace internal

constexpr const char UnmatchedFunctionsChooserPrimary::kCopyAddressAction[];
constexpr const char UnmatchedFunctionsChooserPrimary::kAddMatchAction[];
constexpr const char UnmatchedFunctionsChooserPrimary::kTitle[];

size_t UnmatchedFunctionsChooserPrimary::get_count() const {
  return results_->GetNumUnmatchedPrimary();
}

ea_t UnmatchedFunctionsChooserPrimary::get_ea(size_t n) const {
  return results_->GetMatchDescription(n).address_primary;
}

Results::UnmatchedDescription UnmatchedFunctionsChooserPrimary::GetDescription(
    size_t index) const {
  return results_->GetUnmatchedDescriptionPrimary(index);
}

constexpr const char UnmatchedFunctionsChooserSecondary::kCopyAddressAction[];
constexpr const char UnmatchedFunctionsChooserSecondary::kAddMatchAction[];
constexpr const char UnmatchedFunctionsChooserSecondary::kTitle[];

size_t UnmatchedFunctionsChooserSecondary::get_count() const {
  return results_->GetNumUnmatchedSecondary();
}

Results::UnmatchedDescription
UnmatchedFunctionsChooserSecondary::GetDescription(size_t index) const {
  return results_->GetUnmatchedDescriptionSecondary(index);
}

constexpr const char UnmatchedFunctionsAddMatchChooserPrimary::kTitle[];

size_t UnmatchedFunctionsAddMatchChooserPrimary::get_count() const {
  return results_->GetNumUnmatchedPrimary();
}

Results::UnmatchedDescription
UnmatchedFunctionsAddMatchChooserPrimary::GetDescription(size_t index) const {
  return results_->GetUnmatchedDescriptionPrimary(index);
}

constexpr const char UnmatchedFunctionsAddMatchChooserSecondary::kTitle[];

size_t UnmatchedFunctionsAddMatchChooserSecondary::get_count() const {
  return results_->GetNumUnmatchedSecondary();
}

Results::UnmatchedDescription
UnmatchedFunctionsAddMatchChooserSecondary::GetDescription(size_t index) const {
  return results_->GetUnmatchedDescriptionSecondary(index);
}

}  // namespace bindiff
}  // namespace security
