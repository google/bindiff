// Copyright 2011-2021 Google LLC
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

// This file is a custom fork of util/task/status_macros.h. This will become
// obsolete and will be replaced once Abseil releases absl::Status.

#ifndef UTIL_STATUS_MACROS_H_
#define UTIL_STATUS_MACROS_H_

#include "third_party/absl/base/optimization.h"
#include "third_party/absl/status/status.h"
#include "third_party/absl/status/statusor.h"

// Internal helper for concatenating macro values.
#define NA_MACROS_IMPL_CONCAT_INNER_(x, y) x##y
#define NA_MACROS_IMPL_CONCAT(x, y) NA_MACROS_IMPL_CONCAT_INNER_(x, y)

#define NA_RETURN_IF_ERROR(expr)                       \
  do {                                                 \
    const auto _status_to_verify = (expr);             \
    if (ABSL_PREDICT_FALSE(!_status_to_verify.ok())) { \
      return _status_to_verify;                        \
    }                                                  \
  } while (false)

#define NA_ASSIGN_OR_RETURN(lhs, rexpr) \
  NA_ASSIGN_OR_RETURN_IMPL(             \
      NA_MACROS_IMPL_CONCAT(_not_absl_statusor, __LINE__), lhs, rexpr)

#define NA_ASSIGN_OR_RETURN_IMPL(statusor, lhs, rexpr) \
  auto statusor = (rexpr);                             \
  if (ABSL_PREDICT_FALSE(!statusor.ok())) {            \
    return statusor.status();                          \
  }                                                    \
  lhs = std::move(statusor).value();

#endif  // UTIL_STATUS_MACROS_H_
