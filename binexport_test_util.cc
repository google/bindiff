#include "third_party/zynamics/binexport/binexport_test_util.h"

#include "third_party/absl/memory/memory.h"
#include "third_party/zynamics/binexport/binexport2.proto.h"

namespace security {
namespace binexport {

std::unique_ptr<proto2::util::MessageDifferencer> CreateDifferencer() {
  auto differencer = absl::make_unique<proto2::util::MessageDifferencer>();
  auto* desc = BinExport2::Meta::descriptor();
  differencer->IgnoreField(desc->FindFieldByName("timestamp"));
  differencer->IgnoreField(desc->FindFieldByName("executable_name"));
  differencer->IgnoreField(desc->FindFieldByName("executable_id"));

  desc = BinExport2::Operand::descriptor();
  differencer->IgnoreField(desc->FindFieldByName("expression_index"));

  desc = BinExport2::Instruction::descriptor();
  differencer->IgnoreField(desc->FindFieldByName("operand_index"));
  return differencer;
}

}  // namespace binexport
}  // namespace security
