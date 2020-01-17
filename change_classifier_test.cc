// Copyright 2020 Google LLC
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

#include "third_party/zynamics/bindiff/change_classifier.h"

#include "third_party/zynamics/bindiff/test_util.h"

#ifndef GOOGLE
#include <gmock/gmock.h>
#include <gtest/gtest.h>
#else
#include "gmock/gmock.h"
#include "gtest/gtest.h"
#endif

using ::testing::StrEq;

namespace security::bindiff {
namespace {

class ChangeClassifierTest : public ::testing::Test {
 protected:
  static void SetUpTestSuite() { ApplyDefaultConfigForTesting(); }
};

TEST_F(ChangeClassifierTest, ChangeDescription) {
  EXPECT_THAT(GetChangeDescription(CHANGE_NONE), StrEq("-------"));
  EXPECT_THAT(GetChangeDescription(CHANGE_STRUCTURAL | CHANGE_OPERANDS |
                                   CHANGE_ENTRYPOINT | CHANGE_CALLS),
              StrEq("G-O-E-C"));
  EXPECT_THAT(
      GetChangeDescription(CHANGE_STRUCTURAL | CHANGE_INSTRUCTIONS |
                           CHANGE_OPERANDS | CHANGE_BRANCHINVERSION |
                           CHANGE_ENTRYPOINT | CHANGE_LOOPS | CHANGE_CALLS),
      StrEq("GIOJELC"));
}

}  // namespace
}  // namespace security::bindiff
