#ifndef BINEXPORT_TEST_UTIL_H_
#define BINEXPORT_TEST_UTIL_H_

#include <memory>

#ifndef GOOGLE  // MOE:strip_line
#include <google/protobuf/util/message_differencer.h>  // NOLINT
// MOE:begin_strip
#else
#include "google/protobuf/util/message_differencer.h"

namespace google {
namespace protobuf {
namespace util {
// Import OSS name
using ::google::protobuf::util::MessageDifferencer;
}  // namespace util
}  // namespace protobuf
}  // namespace google
#endif
// MOE:end_strip

namespace security {
namespace binexport {

// Creates a MessageDifferencer that can be used to compare two BinExport2
// protos while ignoring Operand/Expression/etc. indices (which are ephemeral
// anyways).
// This allows to compare two otherwise identical BinExport2s even in the face
// of different hash table implementations or where the hash table randomizes
// insertion order on each run (like Abseil's *_hash_map).
std::unique_ptr<google::protobuf::util::MessageDifferencer> CreateDifferencer();

}  // namespace binexport
}  // namespace security

#endif  // BINEXPORT_TEST_UTIL_H_
