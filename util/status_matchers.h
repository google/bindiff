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

#ifndef UTIL_STATUS_MATCHERS_H_
#define UTIL_STATUS_MATCHERS_H_

#include <type_traits>

#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "third_party/absl/status/status.h"
#include "third_party/absl/types/optional.h"
#include "third_party/zynamics/binexport/util/status_macros.h"
#include "third_party/zynamics/binexport/util/statusor.h"

#define NA_ASSERT_OK_AND_ASSIGN(lhs, rexpr) \
  NA_ASSERT_OK_AND_ASSIGN_IMPL(             \
      NA_MACROS_IMPL_CONCAT(_sapi_statusor, __LINE__), lhs, rexpr)

#define NA_ASSERT_OK_AND_ASSIGN_IMPL(statusor, lhs, rexpr) \
  auto statusor = (rexpr);                                 \
  ASSERT_THAT(statusor.status(), ::not_absl::IsOk());      \
  lhs = std::move(statusor).ValueOrDie();

namespace not_absl {
namespace internal {

class IsOkMatcher {
 public:
  template <typename StatusT>
  bool MatchAndExplain(const StatusT& status_container,
                       ::testing::MatchResultListener* listener) const {
    if (!status_container.ok()) {
      *listener << "which is not OK";
      return false;
    }
    return true;
  }

  void DescribeTo(std::ostream* os) const { *os << "is OK"; }

  void DescribeNegationTo(std::ostream* os) const { *os << "is not OK"; }
};

class StatusIsMatcher {
 public:
  StatusIsMatcher(const StatusIsMatcher&) = default;

  StatusIsMatcher(absl::StatusCode code,
                  absl::optional<absl::string_view> message)
      : code_(code), message_(message) {}

  template <typename T>
  bool MatchAndExplain(const T& value,
                       ::testing::MatchResultListener* listener) const {
    auto status = GetStatus(value);
    if (code_ != status.code()) {
      *listener << "whose error code is "
                << absl::StatusCodeToString(status.code());
      return false;
    }
    if (message_.has_value() && status.message() != message_.value()) {
      *listener << "whose error message is '" << message_.value() << "'";
      return false;
    }
    return true;
  }

  void DescribeTo(std::ostream* os) const {
    *os << "has a status code that is " << absl::StatusCodeToString(code_);
    if (message_.has_value()) {
      *os << ", and has an error message that is '" << message_.value() << "'";
    }
  }

  void DescribeNegationTo(std::ostream* os) const {
    *os << "has a status code that is not " << absl::StatusCodeToString(code_);
    if (message_.has_value()) {
      *os << ", and has an error message that is not '" << message_.value()
          << "'";
    }
  }

 private:
  template <typename StatusT,
            typename std::enable_if<
                !std::is_void<decltype(std::declval<StatusT>().code())>::value,
                int>::type = 0>
  static const StatusT& GetStatus(const StatusT& status) {
    return status;
  }

  template <typename StatusOrT,
            typename StatusT = decltype(std::declval<StatusOrT>().status())>
  static StatusT GetStatus(const StatusOrT& status_or) {
    return status_or.status();
  }

  const absl::StatusCode code_;
  const absl::optional<std::string> message_;
};

}  // namespace internal

inline ::testing::PolymorphicMatcher<internal::IsOkMatcher> IsOk() {
  return ::testing::MakePolymorphicMatcher(internal::IsOkMatcher{});
}

inline ::testing::PolymorphicMatcher<internal::StatusIsMatcher> StatusIs(
    absl::StatusCode code,
    absl::optional<absl::string_view> message = absl::nullopt) {
  return ::testing::MakePolymorphicMatcher(
      internal::StatusIsMatcher(code, message));
}

}  // namespace not_absl

#endif  // UTIL_STATUS_MATCHERS_H_
