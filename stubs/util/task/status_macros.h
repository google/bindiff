// Copyright 2011-2018 Google LLC. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_UTIL_TASK_STATUS_MACROS_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_UTIL_TASK_STATUS_MACROS_H_

#include <google/protobuf/stubs/status_macros.h>

namespace util {

// Map the namespace from the Protocol Buffers.
using namespace ::google::protobuf::util;  // NOLINT(build/namespaces)

}  // namespace util

// To be able to use the macros, Status needs to be visible (bug in
// Protobuf's status_macros.h).
namespace security {
namespace bindiff {
using ::util::Status;
}  // namespace bindiff
namespace binexport {
using ::util::Status;
}  // namespace binexport
}  // namespace security

// To be able to use the macros, Status needs to be visible (bug in
// status_macros.h).
namespace security {
namespace bindiff {
using ::google::protobuf::util::Status;
}  // namespace bindiff
namespace binexport {
using ::google::protobuf::util::Status;
}  // namespace binexport
}  // namespace security

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_UTIL_TASK_STATUS_MACROS_H_
