#include "third_party/zynamics/bindiff/ida/names.h"

#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <bytes.hpp>                                            // NOLINT
#include <lines.hpp>                                            // NOLINT
#include <name.hpp>                                             // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT

#include "third_party/absl/strings/str_cat.h"

namespace security {
namespace bindiff {

string ToString(const qstring& ida_string) {
  return string(ida_string.c_str(), ida_string.length());
}

string GetName(Address address) {
  if (has_user_name(get_full_flags(static_cast<ea_t>(address)))) {
    return ToString(get_name(static_cast<ea_t>(address)));
  }
  return "";
}

string GetDemangledName(Address address) {
  if (has_user_name(get_full_flags(static_cast<ea_t>(address)))) {
    return ToString(get_short_name(static_cast<ea_t>(address)));
  }
  return "";
}

bool GetLineComment(Address address, int n, string* output) {
  qstring ida_comment;
  ssize_t result = get_extra_cmt(&ida_comment, address, n);
  *output = ToString(ida_comment);
  // Note: get_extra_cmt() returns < 0 if there is no line comment for "n".
  //       It is valid for a line comment to be empty, such as when there are
  //       multiple paragraphs of line comments.
  return result >= 0;
}

string GetLineComments(Address address, LineComment kind) {
  string buffer;
  string comment;

  if (kind == LineComment::kAnterior) {
    for (int i = 0; GetLineComment(address, E_PREV + i, &buffer); ++i) {
      absl::StrAppend(&comment, buffer, "\n");
    }
  } else if (kind == LineComment::kPosterior) {
    for (int i = 0; GetLineComment(address, E_NEXT + i, &buffer); ++i) {
      absl::StrAppend(&comment, buffer, "\n");
    }
  }
  if (!comment.empty()) {
    comment = comment.substr(0, comment.size() - 1);
  }
  return comment;
}

}  // namespace bindiff
}  // namespace security
