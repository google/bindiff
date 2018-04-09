#ifndef IDA_UI_H_
#define IDA_UI_H_

#include "third_party/zynamics/binexport/types.h"

namespace security {
namespace bindiff {

constexpr double kManualMatch = -1.0;

// Returns a 32-bit RGB color value suitable for colorizing match similarities.
// If passed kManualMatch, returns a color that can be used to mark manual
// matches.
uint32_t GetMatchColor(double value);

// Copies unformatted data to clipboard. Throws on error.
// TODO(cblichmann): Use util::Status instead of exceptions to signal errors.
void CopyToClipboard(const string& data);

}  // namespace bindiff
}  // namespace security

#endif  // IDA_UI_H_
