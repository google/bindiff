#include "third_party/zynamics/bindiff/ida/names.h"

#include <pro.h>      // NOLINT
#include <bytes.hpp>  // NOLINT
#include <name.hpp>   // NOLINT

std::string GetName(Address address) {
  if (has_user_name(getFlags(static_cast<ea_t>(address)))) {
    qstring ida_name(get_true_name(static_cast<ea_t>(address)));
    if (!ida_name.empty()) {
      return std::string(ida_name.c_str(), ida_name.length());
    }
  }
  return "";
}

std::string GetDemangledName(Address address) {
  if (has_user_name(getFlags(static_cast<ea_t>(address)))) {
    qstring ida_name(get_short_name(static_cast<ea_t>(address)));
    if (!ida_name.empty()) {
      return std::string(ida_name.c_str(), ida_name.length());
    }
  }
  return "";
}

// Taken from IDA SDK 6.1 nalt.hpp. ExtraGet has since been deprecated.
inline ssize_t ExtraGet(ea_t ea, int what, char* buf, size_t bufsize) {
  return netnode(ea).supstr(what, buf, bufsize);
}

std::string GetLineComments(Address address, int direction) {
  char buffer[4096];
  const size_t bufferSize = sizeof(buffer) / sizeof(buffer[0]);

  std::string comment;
  if (direction < 0) {
    // anterior comments
    for (int i = 0; ExtraGet(static_cast<ea_t>(address), E_PREV + i, buffer,
                             bufferSize) != -1;
         ++i) {
      comment += buffer + std::string("\n");
    }
  } else if (direction > 0) {
    // posterior comments
    for (int i = 0; ExtraGet(static_cast<ea_t>(address), E_NEXT + i, buffer,
                             bufferSize) != -1;
         ++i) {
      comment += buffer + std::string("\n");
    }
  }
  if (!comment.empty()) {
    comment = comment.substr(0, comment.size() - 1);
  }
  return comment;
}
