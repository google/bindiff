#include "third_party/zynamics/bindiff/ida/matched_functions_chooser.h"

#include <cstring>
#include <vector>

#include "third_party/absl/strings/str_cat.h"
#include "third_party/zynamics/bindiff/ida/ui.h"

constexpr const int MatchedFunctionsChooser::kColumnWidths[];
constexpr const char* const MatchedFunctionsChooser::kColumnNames[];
constexpr const char MatchedFunctionsChooser::kTitle[];

const void* MatchedFunctionsChooser::get_obj_id(size_t* len) const {
  *len = strlen(kTitle);
  return kTitle;
}

size_t MatchedFunctionsChooser::get_count() const {
  return results_ ? results_->GetNumMatches() : 0;
}

void MatchedFunctionsChooser::get_row(qstrvec_t* cols, int* icon_,
                                      chooser_item_attrs_t* attrs,
                                      size_t n) const {
  if (!results_) {
    return;
  }
  Results::MatchDescription match = results_->GetMatchDescription(n);

  // TODO(cblichmann): Implement the formatting hints from the line comments.
  (*cols)[0] = std::to_string(match.similarity).c_str();  // %.2f
  (*cols)[1] = std::to_string(match.confidence).c_str();  // %.2f
  (*cols)[2] = GetChangeDescription(match.change_type).c_str();
  (*cols)[3] = absl::StrCat(absl::Hex(match.address_primary, absl::kZeroPad8))
                   .c_str();  // HEX_ADDRESS
  (*cols)[4] = match.name_primary.c_str();
  (*cols)[5] = absl::StrCat(absl::Hex(match.address_secondary, absl::kZeroPad8))
                   .c_str();  // HEX_ADDRESS
  (*cols)[6] = match.name_secondary.c_str();
  (*cols)[7] = match.comments_ported ? "X" : " ";
  (*cols)[8] =
      match.algorithm_name.substr(match.algorithm_name.size() > 10 ? 10 : 0)
          .c_str();
  (*cols)[9] = std::to_string(match.basic_block_count).c_str();           // %5d
  (*cols)[10] = std::to_string(match.basic_block_count_primary).c_str();  // %5d
  (*cols)[11] =
      std::to_string(match.basic_block_count_secondary).c_str();          // %5d
  (*cols)[12] = std::to_string(match.instruction_count).c_str();          // %6d
  (*cols)[13] = std::to_string(match.instruction_count_primary).c_str();  // %6d
  (*cols)[14] =
      std::to_string(match.instruction_count_secondary).c_str();     // %6d
  (*cols)[15] = std::to_string(match.edge_count).c_str();            // %5d
  (*cols)[16] = std::to_string(match.edge_count_primary).c_str();    // %5d
  (*cols)[17] = std::to_string(match.edge_count_secondary).c_str();  // %5d
  attrs->color = GetMatchColor(match.similarity);
  if (match.manual) {
    attrs->flags |= CHITEM_BOLD;
  }
}
