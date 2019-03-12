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

#include <string>

#include <gmock/gmock.h>
#include <gtest/gtest.h>
#include "third_party/zynamics/binexport/util/status_matchers.h"

using ::testing::Eq;
using ::testing::IsEmpty;
using ::testing::IsFalse;
using ::testing::IsTrue;
using ::testing::Ne;
using ::testing::Not;

namespace not_absl {
namespace {

constexpr char kErrorMessage1[] = "Bad foo argument";
constexpr char kErrorMessage2[] = "Internal foobar error";

TEST(StatusTest, OkSuccess) { EXPECT_THAT(OkStatus(), IsOk()); }

TEST(StatusTest, OkFailure) {
  Status status{StatusCode::kInvalidArgument, kErrorMessage1};
  EXPECT_THAT(status, Not(IsOk()));
}

TEST(StatusTest, GetErrorCodeOkStatus) {
  EXPECT_THAT(OkStatus().code(), Eq(StatusCode::kOk));
}

TEST(StatusTest, GetErrorCodeNonOkStatus) {
  Status status{StatusCode::kInvalidArgument, kErrorMessage1};
  EXPECT_THAT(status.code(), Eq(StatusCode::kInvalidArgument));
}

TEST(StatusTest, GetErrorMessageOkStatus) {
  EXPECT_THAT(OkStatus().error_message(), IsEmpty());
}

TEST(StatusTest, GetErrorMessageNonOkStatus) {
  Status status{StatusCode::kInvalidArgument, kErrorMessage1};
  EXPECT_THAT(status.error_message(), Eq(kErrorMessage1));
}

TEST(StatusTest, ToStringOkStatus) {
  Status status = OkStatus();
  std::string error_code_name = internal::CodeEnumToString(status.code());

  // The ToString() representation for an ok Status should contain the error
  // code name.
  std::string status_rep = status.ToString();
  EXPECT_THAT(status_rep.find(error_code_name), Ne(std::string::npos));
}

TEST(StatusTest, ToStringNonOkStatus) {
  Status status{StatusCode::kInvalidArgument, kErrorMessage1};
  std::string error_code_name = internal::CodeEnumToString(status.code());
  constexpr char kErrorSpaceName[] = "generic";
  // The format of ToString() is subject to change for a non-ok Status, but it
  // should contain the error space name, the error code name, and the error
  // message.
  std::string status_rep = status.ToString();
  EXPECT_THAT(status_rep.find(kErrorSpaceName), Ne(std::string::npos));
  EXPECT_THAT(status_rep.find(error_code_name), Ne(std::string::npos));
  EXPECT_THAT(status_rep.find(std::string(status.error_message())),
              Ne(std::string::npos));
}

TEST(StatusTest, Equality) {
  Status ok_status = OkStatus();
  Status error_status(StatusCode::kInvalidArgument, kErrorMessage1);

  EXPECT_THAT(ok_status == ok_status, IsTrue());
  EXPECT_THAT(error_status == error_status, IsTrue());
  EXPECT_THAT(ok_status == error_status, IsFalse());
}

TEST(StatusTest, Inequality) {
  Status ok_status = OkStatus();
  Status invalid_arg_status(StatusCode::kInvalidArgument, kErrorMessage1);
  Status internal_status(StatusCode::kInternal, kErrorMessage2);

  EXPECT_THAT(ok_status != ok_status, IsFalse());
  EXPECT_THAT(invalid_arg_status != invalid_arg_status, IsFalse());

  EXPECT_THAT(ok_status != invalid_arg_status, IsTrue());
  EXPECT_THAT(invalid_arg_status != ok_status, IsTrue());

  EXPECT_THAT(invalid_arg_status != internal_status, IsTrue());
  EXPECT_THAT(internal_status != invalid_arg_status, IsTrue());
}

TEST(StatusTest, IsPositiveTest) {
  EXPECT_THAT(OkStatus().code(), Eq(StatusCode::kOk));

  Status invalid_arg_status(StatusCode::kInvalidArgument, kErrorMessage1);
  EXPECT_THAT(invalid_arg_status.code(), Eq(StatusCode::kInvalidArgument));
}

TEST(StatusTest, IsNegativeTest) {
  // Verify correctness of Is() within an error space.
  Status invalid_arg_status(StatusCode::kInvalidArgument, kErrorMessage1);
  EXPECT_THAT(invalid_arg_status.code(), Ne(StatusCode::kOk));
}

TEST(StatusTest, StatusIsMatcher) {
  EXPECT_THAT(OkStatus(), StatusIs(StatusCode::kOk));

  Status invalid_arg_status(StatusCode::kInvalidArgument, kErrorMessage1);
  EXPECT_THAT(invalid_arg_status, StatusIs(StatusCode::kInvalidArgument));
}

TEST(StatusTest, IsOkMatcher) {
  EXPECT_THAT(OkStatus(), IsOk());

  // Negation of IsOk() matcher.
  Status einval_status(StatusCode::kInvalidArgument, kErrorMessage1);
  EXPECT_THAT(einval_status, Not(IsOk()));
}

TEST(StatusTest, MoveConstructorTest) {
  Status invalid_arg_status(StatusCode::kInvalidArgument, kErrorMessage1);
  EXPECT_THAT(invalid_arg_status, StatusIs(StatusCode::kInvalidArgument));

  Status that(std::move(invalid_arg_status));

  EXPECT_THAT(that, StatusIs(StatusCode::kInvalidArgument));
  // NOLINTNEXTLINE
  EXPECT_THAT(invalid_arg_status, StatusIs(StatusCode::kUnknown));
}

TEST(StatusTest, MoveAssignmentTestNonOk) {
  Status invalid_arg_status(StatusCode::kInvalidArgument, kErrorMessage1);
  EXPECT_THAT(invalid_arg_status, StatusIs(StatusCode::kInvalidArgument));

  Status that(StatusCode::kCancelled, kErrorMessage2);
  that = std::move(invalid_arg_status);

  EXPECT_THAT(that, StatusIs(StatusCode::kInvalidArgument));
  // NOLINTNEXTLINE
  EXPECT_THAT(invalid_arg_status, StatusIs(StatusCode::kUnknown));
}

TEST(StatusTest, MoveAssignmentTestOk) {
  Status invalid_arg_status(StatusCode::kInvalidArgument, kErrorMessage1);
  EXPECT_THAT(invalid_arg_status, StatusIs(StatusCode::kInvalidArgument));

  Status ok = OkStatus();
  invalid_arg_status = std::move(ok);

  EXPECT_THAT(invalid_arg_status, StatusIs(StatusCode::kOk, ""));
  // NOLINTNEXTLINE
  EXPECT_THAT(ok, StatusIs(StatusCode::kUnknown));
}

TEST(StatusTest, CopyConstructorTestOk) {
  Status that(OkStatus());

  EXPECT_THAT(that, IsOk());
  EXPECT_TRUE(that.error_message().empty());
}

TEST(StatusTest, CopyConstructorTestNonOk) {
  Status invalid_arg_status(StatusCode::kInvalidArgument, kErrorMessage1);
  EXPECT_THAT(invalid_arg_status, StatusIs(StatusCode::kInvalidArgument));

  Status that(invalid_arg_status);

  EXPECT_THAT(that, StatusIs(StatusCode::kInvalidArgument));
}

}  // namespace
}  // namespace not_absl
