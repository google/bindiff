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

#include "third_party/zynamics/binexport/util/status.h"

#include "third_party/absl/strings/str_cat.h"

namespace not_absl {
namespace internal {

std::string CodeEnumToString(StatusCode code) {
  switch (code) {
    case StatusCode::kOk:
      return "OK";
    case StatusCode::kCancelled:
      return "CANCELLED";
    case StatusCode::kUnknown:
      return "UNKNOWN";
    case StatusCode::kInvalidArgument:
      return "INVALID_ARGUMENT";
    case StatusCode::kDeadlineExceeded:
      return "DEADLINE_EXCEEDED";
    case StatusCode::kNotFound:
      return "NOT_FOUND";
    case StatusCode::kAlreadyExists:
      return "ALREADY_EXISTS";
    case StatusCode::kPermissionDenied:
      return "PERMISSION_DENIED";
    case StatusCode::kUnauthenticated:
      return "UNAUTHENTICATED";
    case StatusCode::kResourceExhausted:
      return "RESOURCE_EXHAUSTED";
    case StatusCode::kFailedPrecondition:
      return "FAILED_PRECONDITION";
    case StatusCode::kAborted:
      return "ABORTED";
    case StatusCode::kOutOfRange:
      return "OUT_OF_RANGE";
    case StatusCode::kUnimplemented:
      return "UNIMPLEMENTED";
    case StatusCode::kInternal:
      return "INTERNAL";
    case StatusCode::kUnavailable:
      return "UNAVAILABLE";
    case StatusCode::kDataLoss:
      return "DATA_LOSS";
  }
  return "UNKNOWN";
}

}  // namespace internal

Status::Status() : error_code_{static_cast<int>(StatusCode::kOk)} {}

Status::Status(Status&& other)
    : error_code_(other.error_code_), message_(std::move(other.message_)) {
  other.Set(StatusCode::kUnknown, "");
}

Status& Status::operator=(Status&& other) {
  error_code_ = other.error_code_;
  message_ = std::move(other.message_);
  other.Set(StatusCode::kUnknown, "");
  return *this;
}

std::string Status::ToString() const {
  return ok() ? "OK"
              : absl::StrCat("generic::",
                             internal::CodeEnumToString(
                                 static_cast<StatusCode>(error_code_)),
                             ": ", message_);
}

Status OkStatus() { return Status{}; }

std::ostream& operator<<(std::ostream& os, const Status& status) {
  return os << status.ToString();
}

}  // namespace not_absl
