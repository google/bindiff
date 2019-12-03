#include "third_party/zynamics/binexport/util/format.h"

#include <chrono>  // NOLINT
#include <cinttypes>
#include <cstdio>

#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/strings/str_format.h"

namespace security::binexport {

std::string FormatAddress(Address address) {
  if (address <= 0xFFFFFFFF) {
    return absl::StrFormat("%08X", address);
  }
  return absl::StrFormat("%016X", address);
}

std::string HumanReadableDuration(double seconds) {
  std::string result;

  absl::Duration remainder;
  int64_t full_hours =
      absl::IDivDuration(absl::Seconds(seconds), absl::Hours(1), &remainder);
  int64_t full_minutes =
      absl::IDivDuration(remainder, absl::Minutes(1), &remainder);
  int64_t full_seconds =
      absl::IDivDuration(remainder, absl::Seconds(1), &remainder);
  int64_t full_msec = absl::ToInt64Milliseconds(remainder);

  bool need_space = false;
  if (full_hours > 0) {
    absl::StrAppend(&result, full_hours, "h");
    need_space = true;
  }
  if (full_minutes > 0) {
    absl::StrAppend(&result, (need_space ? " " : ""), full_minutes, "m");
    need_space = true;
  }
  if (full_seconds > 0 || full_msec > 0) {
    absl::StrAppend(&result, (need_space ? " " : ""), full_seconds);
    if (full_msec > 0) {
      absl::StrAppend(&result, ".", full_msec / 10);
    }
    absl::StrAppend(&result, "s");
  }
  if (result.empty()) {
    absl::StrAppend(&result, "0s");
  }
  return result;
}

std::string HumanReadableDuration(absl::Duration duration) {
  return HumanReadableDuration(absl::ToDoubleSeconds(duration));
}

}  // namespace security::binexport
