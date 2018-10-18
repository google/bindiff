#include "third_party/zynamics/binexport/util/format.h"

#include <chrono>  // NOLINT
#include <cinttypes>
#include <cstdio>

#include "third_party/absl/strings/str_cat.h"

namespace security {
namespace binexport {

string FormatAddress(Address address) {
  // TODO(cblichmann): Use absl::Format once it becomes available.
  if (address <= 0xFFFFFFFF) {
    string result(9, '0');
    // Note: need to cast to standard integer type for the PRIX macros to work.
    std::snprintf(&result[0], 9, "%08" PRIX32,  // NOLINT(runtime/printf)
                  static_cast<uint32_t>(address));
    result.resize(8);
    return result;
  }
  string result(17, '0');
  std::snprintf(&result[0], 17, "%016" PRIX64,  // NOLINT(runtime/printf)
                static_cast<uint64_t>(address));
  result.resize(16);
  return result;
}

string HumanReadableDuration(double seconds) {
  string result;

  absl::Duration remainder;
  int64 full_hours =
      absl::IDivDuration(absl::Seconds(seconds), absl::Hours(1), &remainder);
  int64 full_minutes =
      absl::IDivDuration(remainder, absl::Minutes(1), &remainder);
  int64 full_seconds =
      absl::IDivDuration(remainder, absl::Seconds(1), &remainder);
  int64 full_msec = absl::ToInt64Milliseconds(remainder);

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
  return result;
}

string HumanReadableDuration(absl::Duration duration) {
  return HumanReadableDuration(absl::ToDoubleSeconds(duration));
}

}  // namespace binexport
}  // namespace security
