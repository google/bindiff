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

// This file and it's implementation provide a custom fork of
// util/task/status.h. This will become obsolete and will be replaced once
// Abseil releases absl::Status.

#ifndef UTIL_STATUS_H_
#define UTIL_STATUS_H_

#include <ostream>
#include <string>

#include "third_party/absl/base/attributes.h"
#include "third_party/absl/meta/type_traits.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/zynamics/binexport/types.h"

namespace not_absl {

enum class StatusCode {
  kOk = 0,
  kCancelled = 1,
  kUnknown = 2,
  kInvalidArgument = 3,
  kDeadlineExceeded = 4,
  kNotFound = 5,
  kAlreadyExists = 6,
  kPermissionDenied = 7,
  kResourceExhausted = 8,
  kFailedPrecondition = 9,
  kAborted = 10,
  kOutOfRange = 11,
  kUnimplemented = 12,
  kInternal = 13,
  kUnavailable = 14,
  kDataLoss = 15,
  kUnauthenticated = 16,
};

namespace internal {

std::string CodeEnumToString(StatusCode code);

}  // namespace internal

class Status {
 public:
  Status();

  template <typename Enum>
  Status(Enum code, absl::string_view message) {
    Set(code, message);
  }

  Status(const Status&) = default;
  Status(Status&& other);

  Status& operator=(const Status&) = default;
  Status& operator=(Status&& other);

  int error_code() const { return error_code_; }
  absl::string_view error_message() const { return message_; }
  absl::string_view message() const { return message_; }
  ABSL_MUST_USE_RESULT bool ok() const { return error_code_ == 0; }
  StatusCode code() const { return static_cast<StatusCode>(error_code_); }

  std::string ToString() const;

  void IgnoreError() const {}

 private:
  template <typename Enum, typename StringViewT>
  void Set(Enum code, StringViewT message) {
    error_code_ = static_cast<int>(code);
    if (error_code_ != 0) {
      message_ = std::string(message);
    } else {
      message_.clear();
    }
  }

  int error_code_;
  std::string message_;
};

Status OkStatus();

inline bool operator==(const Status& lhs, const Status& rhs) {
  return (lhs.error_code() == rhs.error_code()) &&
         (lhs.error_message() == rhs.error_message());
}

inline bool operator!=(const Status& lhs, const Status& rhs) {
  return !(lhs == rhs);
}

std::ostream& operator<<(std::ostream& os, const Status& status);

}  // namespace not_absl

#endif  // UTIL_STATUS_H_
