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

#include <initializer_list>
#include <utility>

#include "base/logging.h"
#include "third_party/absl/base/attributes.h"
#include "third_party/absl/status/status.h"
#include "third_party/absl/types/variant.h"

namespace not_absl {

template <typename T>
class ABSL_MUST_USE_RESULT StatusOr {
  template <typename U>
  friend class StatusOr;

 public:
  using element_type = T;

  explicit StatusOr() : variant_(absl::UnknownError("")) {}

  StatusOr(const StatusOr&) = default;
  StatusOr& operator=(const StatusOr&) = default;

  StatusOr(StatusOr&&) = default;
  StatusOr& operator=(StatusOr&&) = default;

  // Not implemented:
  // template <typename U> StatusOr(const StatusOr<U>& other)
  // template <typename U> StatusOr(StatusOr<U>&& other)

  template <typename U>
  StatusOr& operator=(const StatusOr<U>& other) {
    if (other.ok()) {
      variant_ = other.value();
    } else {
      variant_ = other.status();
    }
    return *this;
  }

  template <typename U>
  StatusOr& operator=(StatusOr<U>&& other) {
    if (other.ok()) {
      variant_ = std::move(other).value();
    } else {
      variant_ = std::move(other).status();
    }
    return *this;
  }

  StatusOr(const T& value) : variant_(value) {}

  StatusOr(const absl::Status& status) : variant_(status) { EnsureNotOk(); }

  // Not implemented:
  // template <typename U = T> StatusOr& operator=(U&& value)

  StatusOr(T&& value) : variant_(std::move(value)) {}

  StatusOr(absl::Status&& value) : variant_(std::move(value)) {}

  StatusOr& operator=(absl::Status&& status) {
    variant_ = std::move(status);
    EnsureNotOk();
  }

  template <typename... Args>
  explicit StatusOr(absl::in_place_t, Args&&... args)
      : StatusOr(T(std::forward<Args>(args)...)) {}

  template <typename U, typename... Args>
  explicit StatusOr(absl::in_place_t, std::initializer_list<U> ilist,
                    Args&&... args)
      : StatusOr(ilist, U(std::forward<Args>(args)...)) {}

  explicit operator bool() const { return ok(); }

  ABSL_MUST_USE_RESULT bool ok() const {
    return absl::holds_alternative<T>(variant_);
  }

  const absl::Status& status() const& {
    static const auto* ok_status = new absl::Status();
    return ok() ? *ok_status : absl::get<absl::Status>(variant_);
  }

  absl::Status status() && {
    return ok() ? absl::OkStatus()
                : std::move(absl::get<absl::Status>(variant_));
  }

  const T& value() const& {
    EnsureOk();
    return absl::get<T>(variant_);
  }

  T& value() & {
    EnsureOk();
    return absl::get<T>(variant_);
  }

  const T&& value() const&& {
    EnsureOk();
    return absl::get<T>(std::move(variant_));
  }

  T&& value() && {
    EnsureOk();
    return absl::get<T>(std::move(variant_));
  }

  const T& ValueOrDie() const& {
    EnsureOk();
    return absl::get<T>(variant_);
  }

  T& ValueOrDie() & {
    EnsureOk();
    return absl::get<T>(variant_);
  }

  T&& ValueOrDie() && {
    EnsureOk();
    return absl::get<T>(std::move(variant_));
  }

  const T& operator*() const& {
    EnsureOk();
    return absl::get<T>(variant_);
  }

  T& operator*() & {
    EnsureOk();
    return absl::get<T>(variant_);
  }

  const T&& operator*() const&& {
    EnsureOk();
    return absl::get<T>(std::move(variant_));
  }

  T&& operator*() && {
    EnsureOk();
    return absl::get<T>(std::move(variant_));
  }

  const T* operator->() const {
    EnsureOk();
    return &absl::get<T>(variant_);
  }

  T* operator->() {
    EnsureOk();
    return &absl::get<T>(variant_);
  }

  template <typename U>
  T value_or(U&& default_value) const& {
    if (ok()) {
      return absl::get<T>(variant_);
    }
    return std::forward<U>(default_value);
  }

  template <typename U>
  T value_or(U&& default_value) && {
    if (ok()) {
      return absl::get<T>(std::move(variant_));
    }
    return std::forward<U>(default_value);
  }

  void IgnoreError() const { /* no-op */
  }

  template <typename... Args>
  T& emplace(Args&&... args) {
    return variant_.template emplace<T>(std::forward<Args>(args)...);
  }

  template <typename U, typename... Args>
  T& emplace(std::initializer_list<U> ilist, Args&&... args) {
    return variant_.template emplace<T>(ilist, std::forward<Args>(args)...);
  }

 private:
  void EnsureOk() const {
    if (!ok()) {
      // GoogleTest needs this error message for death tests to work. Not using
      // LOG_IF() and the use of the additional string conversion are because
      // the LOG macro below is from protobuf.
      LOG(FATAL) << "Attempting to fetch value instead of handling error "
                 << std::string(status().message());
    }
  }

  void EnsureNotOk() const {
    if (ok()) {
      LOG(FATAL)
          << "An OK status is not a valid constructor argument to StatusOr<T>";
    }
  }

  absl::variant<absl::Status, T> variant_;
};

}  // namespace not_absl

#endif  // UTIL_STATUSOR_H_
