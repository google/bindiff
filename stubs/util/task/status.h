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

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_UTIL_TASK_STATUS_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_UTIL_TASK_STATUS_H_

#include <google/protobuf/stubs/status.h>

namespace absl {

// A struct that provides identical syntax to Abseil's enum class.
struct StatusCode {
  StatusCode() = delete;
  static constexpr auto kOk = ::google::protobuf::util::error::OK;
  static constexpr auto kCancelled = ::google::protobuf::util::error::CANCELLED;
  static constexpr auto kUnknown = ::google::protobuf::util::error::UNKNOWN;
  static constexpr auto kInvalidArgument =
      ::google::protobuf::util::error::INVALID_ARGUMENT;
  static constexpr auto kDeadlineExceeded =
      ::google::protobuf::util::error::DEADLINE_EXCEEDED;
  static constexpr auto kNotFound = ::google::protobuf::util::error::NOT_FOUND;
  static constexpr auto kAlreadyExists =
      ::google::protobuf::util::error::ALREADY_EXISTS;
  static constexpr auto kPermissionDenied =
      ::google::protobuf::util::error::PERMISSION_DENIED;
  static constexpr auto kResourceExhausted =
      ::google::protobuf::util::error::RESOURCE_EXHAUSTED;
  static constexpr auto kFailedPrecondition =
      ::google::protobuf::util::error::FAILED_PRECONDITION;
  static constexpr auto kAborted = ::google::protobuf::util::error::ABORTED;
  static constexpr auto kOutOfRange =
      ::google::protobuf::util::error::OUT_OF_RANGE;
  static constexpr auto kUnimplemented =
      ::google::protobuf::util::error::UNIMPLEMENTED;
  static constexpr auto kInternal = ::google::protobuf::util::error::INTERNAL;
  static constexpr auto kUnavailable =
      ::google::protobuf::util::error::UNAVAILABLE;
  static constexpr auto kDataLoss = ::google::protobuf::util::error::DATA_LOSS;
  static constexpr auto kUnauthenticated =
      ::google::protobuf::util::error::UNAUTHENTICATED;
};

}  // namespace absl

namespace util {

// Map the namespace from the Protocol Buffers.
using namespace ::google::protobuf::util;  // NOLINT(build/namespaces)

inline Status OkStatus() { return Status{}; }

}  // namespace util

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_UTIL_TASK_STATUS_H_
