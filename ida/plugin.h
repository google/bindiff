#ifndef IDA_PLUGIN_H_
#define IDA_PLUGIN_H_

#include <cstddef>

// clang-format off
#include "third_party/zynamics/binexport/ida/begin_idasdk.inc"  // NOLINT
#include <loader.hpp>                                           // NOLINT
#include "third_party/zynamics/binexport/ida/end_idasdk.inc"    // NOLINT
// clang-format on

namespace security {
namespace binexport {

// Simple CRTP-style template that can be used to implement IDA Pro plugins.
// Since IDA plugins are singletons, this design allows to avoid global
// variables for keeping plugins state.
template <typename T>
class IdaPlugin {
 public:
  virtual ~IdaPlugin() = default;

  IdaPlugin(const IdaPlugin&) = delete;
  IdaPlugin& operator=(const IdaPlugin&) = delete;

  static T* instance() {
    static auto* instance = new T();
    return instance;
  }

  virtual int Init() { return PLUGIN_OK; }
  virtual bool Run(size_t argument) = 0;
  virtual void Terminate() {}

 protected:
  IdaPlugin() = default;
};

}  // namespace binexport
}  // namespace security

#endif  // IDA_PLUGIN_H_
