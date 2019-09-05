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

// This file is a custom fork of the version in Asylo. This will become obsolete
// and will be replaced once Abseil releases absl::Status.

#include "third_party/zynamics/binexport/util/statusor.h"

#include <memory>
#include <string>
#include <vector>

#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "third_party/zynamics/binexport/util/status_matchers.h"

using ::testing::Eq;
using ::testing::IsFalse;
using ::testing::Not;

namespace not_absl {
namespace {

constexpr auto kErrorCode = StatusCode::kInvalidArgument;
constexpr char kErrorMessage[] = "Invalid argument";

const int kIntElement = 47;
constexpr char kStringElement[] = "47 is 42, corrected for inflation";

// A data type without a default constructor.
struct Foo {
  int bar;
  std::string baz;

  explicit Foo(int value) : bar(value), baz(kStringElement) {}
};

// A data type with dynamically-allocated data.
struct HeapAllocatedObject {
  int* value;

  HeapAllocatedObject() {
    value = new int;
    *value = kIntElement;
  }

  HeapAllocatedObject(const HeapAllocatedObject& other) { *this = other; }

  HeapAllocatedObject& operator=(const HeapAllocatedObject& other) {
    value = new int;
    *value = *other.value;
    return *this;
  }

  HeapAllocatedObject(HeapAllocatedObject&& other) { *this = std::move(other); }

  HeapAllocatedObject& operator=(HeapAllocatedObject&& other) {
    value = other.value;
    other.value = nullptr;
    return *this;
  }

  ~HeapAllocatedObject() { delete value; }
};

// Constructs a Foo.
struct FooCtor {
  using value_type = Foo;

  Foo operator()() { return Foo(kIntElement); }
};

// Constructs a HeapAllocatedObject.
struct HeapAllocatedObjectCtor {
  using value_type = HeapAllocatedObject;

  HeapAllocatedObject operator()() { return HeapAllocatedObject(); }
};

// Constructs an integer.
struct IntCtor {
  using value_type = int;

  int operator()() { return kIntElement; }
};

// Constructs a std::string.
struct StringCtor {
  using value_type = std::string;

  std::string operator()() { return std::string(kStringElement); }
};

// Constructs a vector of strings.
struct StringVectorCtor {
  using value_type = std::vector<std::string>;

  std::vector<std::string> operator()() {
    return {kStringElement, kErrorMessage};
  }
};

bool operator==(const Foo& lhs, const Foo& rhs) {
  return (lhs.bar == rhs.bar) && (lhs.baz == rhs.baz);
}

bool operator==(const HeapAllocatedObject& lhs,
                const HeapAllocatedObject& rhs) {
  return *lhs.value == *rhs.value;
}

// Returns an rvalue reference to the StatusOr<T> object pointed to by
// |statusor|.
template <typename T>
StatusOr<T>&& MoveStatusOr(StatusOr<T>* statusor) {
  return std::move(*statusor);
}

// A test fixture is required for typed tests.
template <typename T>
class StatusOrTest : public ::testing::Test {};

using TestTypes = ::testing::Types<IntCtor, FooCtor, StringCtor,
                                   StringVectorCtor, HeapAllocatedObjectCtor>;
TYPED_TEST_CASE(StatusOrTest, TestTypes);

// Verify that the default constructor for StatusOr constructs an object with a
// non-ok status.
TYPED_TEST(StatusOrTest, ConstructorDefault) {
  StatusOr<typename TypeParam::value_type> statusor;
  EXPECT_THAT(statusor.ok(), IsFalse());
  EXPECT_THAT(statusor.status().code(), Eq(StatusCode::kUnknown));
}

// Verify that StatusOr can be constructed from a Status object.
TYPED_TEST(StatusOrTest, ConstructorStatus) {
  StatusOr<typename TypeParam::value_type> statusor{
      Status{kErrorCode, kErrorMessage}};

  EXPECT_THAT(statusor.ok(), IsFalse());
  EXPECT_THAT(statusor.status().ok(), IsFalse());
  EXPECT_THAT(statusor.status(), Eq(Status(kErrorCode, kErrorMessage)));
}

// Verify that StatusOr can be constructed from an object of its element type.
TYPED_TEST(StatusOrTest, ConstructorElementConstReference) {
  auto value = TypeParam()();
  StatusOr<typename TypeParam::value_type> statusor{value};

  ASSERT_THAT(statusor, IsOk());
  ASSERT_THAT(statusor.status(), IsOk());
  EXPECT_THAT(statusor.ValueOrDie(), Eq(value));
}

// Verify that StatusOr can be constructed from an rvalue reference of an object
// of its element type.
TYPED_TEST(StatusOrTest, ConstructorElementRValue) {
  auto value = TypeParam()();
  auto value_copy(value);
  StatusOr<typename TypeParam::value_type> statusor(std::move(value));

  ASSERT_THAT(statusor, IsOk());
  ASSERT_THAT(statusor.status(), IsOk());

  // Compare to a copy of the original value, since the original was moved.
  EXPECT_THAT(statusor.ValueOrDie(), Eq(value_copy));
}

// Verify that StatusOr can be copy-constructed from a StatusOr with a non-ok
// status.
TYPED_TEST(StatusOrTest, CopyConstructorNonOkStatus) {
  StatusOr<typename TypeParam::value_type> statusor1 =
      Status{kErrorCode, kErrorMessage};
  StatusOr<typename TypeParam::value_type> statusor2{statusor1};

  EXPECT_THAT(statusor1.ok(), Eq(statusor2.ok()));
  EXPECT_THAT(statusor1.status(), Eq(statusor2.status()));
}

// Verify that StatusOr can be copy-constructed from a StatusOr with an ok
// status.
TYPED_TEST(StatusOrTest, CopyConstructorOkStatus) {
  StatusOr<typename TypeParam::value_type> statusor1{TypeParam()()};
  StatusOr<typename TypeParam::value_type> statusor2{statusor1};

  EXPECT_THAT(statusor1.ok(), Eq(statusor2.ok()));
  ASSERT_THAT(statusor2, IsOk());
  EXPECT_THAT(statusor1.ValueOrDie(), Eq(statusor2.ValueOrDie()));
}

// Verify that copy-assignment of a StatusOr with a non-ok is working as
// expected.
TYPED_TEST(StatusOrTest, CopyAssignmentNonOkStatus) {
  StatusOr<typename TypeParam::value_type> statusor1{
      Status(kErrorCode, kErrorMessage)};
  StatusOr<typename TypeParam::value_type> statusor2{TypeParam()()};

  // Invoke the copy-assignment operator.
  statusor2 = statusor1;
  EXPECT_THAT(statusor1.ok(), Eq(statusor2.ok()));
  EXPECT_THAT(statusor1.status(), Eq(statusor2.status()));
}

// Verify that copy-assignment of a StatusOr with an ok status is working as
// expected.
TYPED_TEST(StatusOrTest, CopyAssignmentOkStatus) {
  StatusOr<typename TypeParam::value_type> statusor1{TypeParam()()};
  StatusOr<typename TypeParam::value_type> statusor2{
      Status(kErrorCode, kErrorMessage)};

  // Invoke the copy-assignment operator.
  statusor2 = statusor1;
  EXPECT_THAT(statusor1.ok(), Eq(statusor2.ok()));
  ASSERT_THAT(statusor2, IsOk());
  EXPECT_THAT(statusor1.ValueOrDie(), Eq(statusor2.ValueOrDie()));
}

// Verify that StatusOr can be move-constructed from a StatusOr with a non-ok
// status.
TYPED_TEST(StatusOrTest, MoveConstructorNonOkStatus) {
  Status status{kErrorCode, kErrorMessage};
  StatusOr<typename TypeParam::value_type> statusor1{status};
  StatusOr<typename TypeParam::value_type> statusor2{std::move(statusor1)};

  // Verify that the status of the donor object was updated.
  EXPECT_THAT(statusor1.ok(), IsFalse());  // NOLINT
  // NOLINTNEXTLINE
  EXPECT_THAT(statusor1.status(), StatusIs(StatusCode::kUnknown, ""));

  // Verify that the destination object contains the status previously held by
  // the donor.
  EXPECT_THAT(statusor2.ok(), IsFalse());
  EXPECT_THAT(statusor2.status(), Eq(status));
}

// Verify that StatusOr can be move-constructed from a StatusOr with an ok
// status.
TYPED_TEST(StatusOrTest, MoveConstructorOkStatus) {
  auto value = TypeParam()();
  StatusOr<typename TypeParam::value_type> statusor1{value};
  StatusOr<typename TypeParam::value_type> statusor2{std::move(statusor1)};

  // The destination object should possess the value previously held by the
  // donor.
  ASSERT_THAT(statusor2, IsOk());
  EXPECT_THAT(statusor2.ValueOrDie(), Eq(value));
}

// Verify that move-assignment from a StatusOr with a non-ok status is working
// as expected.
TYPED_TEST(StatusOrTest, MoveAssignmentOperatorNonOkStatus) {
  Status status(kErrorCode, kErrorMessage);
  StatusOr<typename TypeParam::value_type> statusor1{status};
  StatusOr<typename TypeParam::value_type> statusor2{TypeParam()()};

  // Invoke the move-assignment operator.
  statusor2 = std::move(statusor1);

  // Verify that the status of the donor object was updated.
  EXPECT_THAT(statusor1.ok(), IsFalse());  // NOLINT
  // NOLINTNEXTLINE
  EXPECT_THAT(statusor1.status(), StatusIs(StatusCode::kUnknown, ""));

  // Verify that the destination object contains the status previously held by
  // the donor.
  EXPECT_THAT(statusor2.ok(), IsFalse());
  EXPECT_THAT(statusor2.status(), Eq(status));
}

// Verify that move-assignment from a StatusOr with an ok status is working as
// expected.
TYPED_TEST(StatusOrTest, MoveAssignmentOperatorOkStatus) {
  auto value = TypeParam()();
  StatusOr<typename TypeParam::value_type> statusor1{value};
  StatusOr<typename TypeParam::value_type> statusor2{
      Status{kErrorCode, kErrorMessage}};

  // Invoke the move-assignment operator.
  statusor2 = std::move(statusor1);

  // The destination object should possess the value previously held by the
  // donor.
  ASSERT_THAT(statusor2, IsOk());
  EXPECT_THAT(statusor2.ValueOrDie(), Eq(value));
}

// Verify that the sapi::IsOk() gMock matcher works with StatusOr<T>.
TYPED_TEST(StatusOrTest, IsOkMatcher) {
  auto value = TypeParam()();
  StatusOr<typename TypeParam::value_type> statusor{value};

  EXPECT_THAT(statusor, IsOk());

  statusor = StatusOr<typename TypeParam::value_type>{
      Status{kErrorCode, kErrorMessage}};
  EXPECT_THAT(statusor, Not(IsOk()));
}

// Tests for move-only types. These tests use std::unique_ptr<> as the
// test type, since it is valuable to support this type in the Asylo infra.
// These tests are not part of the typed test suite for the following reasons:
//  * std::unique_ptr<> cannot be used as a type in tests that expect
//   the test type to support copy operations.
//  * std::unique_ptr<> provides an equality operator that checks equality of
//   the underlying ptr. Consequently, it is difficult to generalize existing
//   tests that verify ValueOrDie() functionality using equality comparisons.

// Verify that a StatusOr object can be constructed from a move-only type.
TEST(StatusOrTest, InitializationMoveOnlyType) {
  std::string* str = new std::string{kStringElement};
  std::unique_ptr<std::string> value(str);
  StatusOr<std::unique_ptr<std::string>> statusor(std::move(value));

  ASSERT_THAT(statusor, IsOk());
  EXPECT_THAT(statusor.ValueOrDie().get(), Eq(str));
}

// Verify that a StatusOr object can be move-constructed from a move-only type.
TEST(StatusOrTest, MoveConstructorMoveOnlyType) {
  std::string* str = new std::string{kStringElement};
  std::unique_ptr<std::string> value{str};
  StatusOr<std::unique_ptr<std::string>> statusor1{std::move(value)};
  StatusOr<std::unique_ptr<std::string>> statusor2{std::move(statusor1)};

  // The destination object should possess the value previously held by the
  // donor.
  ASSERT_THAT(statusor2, IsOk());
  EXPECT_THAT(statusor2.ValueOrDie().get(), Eq(str));
}

// Verify that a StatusOr object can be move-assigned to from a StatusOr object
// containing a move-only type.
TEST(StatusOrTest, MoveAssignmentMoveOnlyType) {
  std::string* str = new std::string{kStringElement};
  std::unique_ptr<std::string> value{str};
  StatusOr<std::unique_ptr<std::string>> statusor1(std::move(value));
  StatusOr<std::unique_ptr<std::string>> statusor2(
      Status(kErrorCode, kErrorMessage));

  // Invoke the move-assignment operator.
  statusor2 = std::move(statusor1);

  // The destination object should possess the value previously held by the
  // donor.
  ASSERT_THAT(statusor2, IsOk());
  EXPECT_THAT(statusor2.ValueOrDie().get(), Eq(str));
}

// Verify that a value can be moved out of a StatusOr object via ValueOrDie().
TEST(StatusOrTest, ValueOrDieMovedValue) {
  std::string* str = new std::string{kStringElement};
  std::unique_ptr<std::string> value{str};
  StatusOr<std::unique_ptr<std::string>> statusor{std::move(value)};

  std::unique_ptr<std::string> moved_value = std::move(statusor).ValueOrDie();
  EXPECT_THAT(moved_value.get(), Eq(str));
  EXPECT_THAT(*moved_value, Eq(kStringElement));
}

}  // namespace
}  // namespace not_absl
