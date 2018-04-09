#ifndef IDA_VISUAL_DIFF_H_
#define IDA_VISUAL_DIFF_H_

#include <functional>
#include "third_party/absl/strings/string_view.h"
#include "third_party/zynamics/binexport/types.h"

namespace security {
namespace bindiff {

bool SendGuiMessage(int retries, absl::string_view gui_dir,
                    absl::string_view server, uint16_t port,
                    absl::string_view arguments,
                    std::function<void()> callback);

}  // namespace bindiff
}  // namespace security

#endif  // VISUAL_DIFF_H_
