#include "third_party/zynamics/bindiff/ida/unmatched_functions_chooser.h"

#include <cstring>
#include <vector>

#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/bindiff/ida/main_plugin.h"
#include "third_party/zynamics/bindiff/ida/ui.h"
#include "third_party/zynamics/binexport/util/format.h"

namespace security::bindiff {
namespace internal {

void UnmatchedDescriptionToIdaRow(const Results::UnmatchedDescription& desc,
                                  qstrvec_t* cols, int* /* icon */,
                                  chooser_item_attrs_t* /* attrs */) {
  (*cols)[0] = binexport::FormatAddress(desc.address).c_str();
  (*cols)[1] = desc.name.c_str();
  (*cols)[2] = absl::StrCat(desc.basic_block_count).c_str();
  (*cols)[3] = absl::StrCat(desc.instruction_count).c_str();
  (*cols)[4] = absl::StrCat(desc.edge_count).c_str();
}

}  // namespace internal

size_t UnmatchedFunctionsChooserPrimary::get_count() const {
  return Plugin::instance()->results()->GetNumUnmatchedPrimary();
}

ea_t UnmatchedFunctionsChooserPrimary::get_ea(size_t n) const {
  return Plugin::instance()->results()->GetMatchDescription(n).address_primary;
}

Results::UnmatchedDescription UnmatchedFunctionsChooserPrimary::GetDescription(
    size_t index) const {
  return Plugin::instance()->results()->GetUnmatchedDescriptionPrimary(index);
}

size_t UnmatchedFunctionsChooserSecondary::get_count() const {
  return Plugin::instance()->results()->GetNumUnmatchedSecondary();
}

Results::UnmatchedDescription
UnmatchedFunctionsChooserSecondary::GetDescription(size_t index) const {
  return Plugin::instance()->results()->GetUnmatchedDescriptionSecondary(index);
}

size_t UnmatchedFunctionsAddMatchChooserPrimary::get_count() const {
  return Plugin::instance()->results()->GetNumUnmatchedPrimary();
}

Results::UnmatchedDescription
UnmatchedFunctionsAddMatchChooserPrimary::GetDescription(size_t index) const {
  return Plugin::instance()->results()->GetUnmatchedDescriptionPrimary(index);
}

size_t UnmatchedFunctionsAddMatchChooserSecondary::get_count() const {
  return Plugin::instance()->results()->GetNumUnmatchedSecondary();
}

Results::UnmatchedDescription
UnmatchedFunctionsAddMatchChooserSecondary::GetDescription(size_t index) const {
  return Plugin::instance()->results()->GetUnmatchedDescriptionSecondary(index);
}

}  // namespace security::bindiff
