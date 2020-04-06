// Copyright 2011-2020 Google LLC
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
// util/task/statusor.h. This will become obsolete and will be replaced once
// Abseil releases absl::Status.

#ifndef UTIL_STATUSOR_H_
#define UTIL_STATUSOR_H_

#include "base/logging.h"
#include "third_party/absl/status/status.h"
#include "third_party/absl/types/variant.h"

namespace not_absl {

template <typename T>
class StatusOr {
 public:
  explicit StatusOr()
      : variant_(absl::Status(absl::StatusCode::kUnknown, "Unknown error")) {}

  StatusOr(const absl::Status& status) : variant_{status} {
    if (status.ok()) {
      LOG(FATAL) << "Cannot instantiate StatusOr with Status::OkStatus()";
    }
  }

  StatusOr(const T& value) : variant_{value} {}
  StatusOr(T&& value) : variant_{std::move(value)} {}

  StatusOr(const StatusOr&) = default;
  StatusOr& operator=(const StatusOr& other) = default;

  StatusOr(StatusOr&& other) = default;

  StatusOr& operator=(StatusOr&& other) = default;

  bool ok() const { return absl::holds_alternative<T>(variant_); }

  absl::Status status() const {
    return ok() ? absl::OkStatus() : absl::get<absl::Status>(variant_);
  }

  const T& ValueOrDie() const& {
    if (!ok()) {
      LOG(FATAL) << "Object does not have a usable value";
    }
    return absl::get<T>(variant_);
  }

  T& ValueOrDie() & {
    if (!ok()) {
      LOG(FATAL) << "Object does not have a usable value";
    }
    return absl::get<T>(variant_);
  }

  T ValueOrDie() && {
    if (!ok()) {
      LOG(FATAL) << "Object does not have a usable value";
    }
    T tmp(std::move(absl::get<T>(variant_)));
    return std::move(tmp);
  }

 private:
  absl::variant<absl::Status, T> variant_;
};

}  // namespace not_absl

#endif  // UTIL_STATUSOR_H_
