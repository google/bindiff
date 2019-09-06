#ifndef IDA_MAIN_PLUGIN_H_
#define IDA_MAIN_PLUGIN_H_

#include "third_party/zynamics/binexport/ida/plugin.h"
#include "third_party/zynamics/binexport/version.h"

namespace security {
namespace binexport {

class Plugin : public IdaPlugin<Plugin> {
 public:
  static constexpr char kName[] = "BinExport " BINEXPORT_RELEASE;
  static constexpr char kCopyright[] =
      "(c)2004-2011 zynamics GmbH, (c)2011-2019 Google LLC.";
  static constexpr char kComment[] =
      "Export to SQL RE-DB, BinDiff binary or text dump";
  static constexpr char kHotKey[] = "";

  int Init() override;
  bool Run(size_t argument) override;
  void Terminate() override;

  bool alsologtostderr() const { return alsologtostderr_; }

 private:
  bool alsologtostderr_ = false;
};

}  // namespace binexport
}  // namespace security

#endif  // IDA_MAIN_PLUGIN_H_
