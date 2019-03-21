#ifndef START_UI_H_
#define START_UI_H_

#include <string>
#include <vector>

#include "third_party/zynamics/binexport/types.h"
#include "third_party/zynamics/binexport/util/status.h"

namespace security {
namespace bindiff {

struct StartUiOptions {
  StartUiOptions& set_java_binary(std::string value) {
    java_binary = std::move(value);
    return *this;
  }

  StartUiOptions& set_java_vm_options(std::string value) {
    java_vm_options = std::move(value);
    return *this;
  }

  StartUiOptions& set_max_heap_size_mb(int value) {
    max_heap_size_mb = value;
    return *this;
  }

  StartUiOptions& set_gui_dir(std::string value) {
    gui_dir = std::move(value);
    return *this;
  }

  std::string java_binary;
  std::string java_vm_options;
  int max_heap_size_mb = -1;  // Default means 75% of physical memory
  std::string gui_dir;
};

// Launches the BinDiff Java UI and immediately returns. Extra command-line
// arguments for the UI can be specified in args, and configuration settings in
// options.
not_absl::Status StartUiWithOptions(std::vector<std::string> extra_args,
                                    const StartUiOptions& options);

}  // namespace bindiff
}  // namespace security

#endif  // START_UI_H_
