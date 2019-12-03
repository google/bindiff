// Copyright 2011-2019 Google LLC. All Rights Reserved.
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

#include "third_party/zynamics/binexport/util/format.h"

#include "gmock/gmock.h"
#include "gtest/gtest.h"

using testing::StrEq;

namespace security::binexport {
namespace {

TEST(FormatUtilTest, FormatAddress) {
  EXPECT_THAT(FormatAddress(0x08), StrEq("00000008"));
  EXPECT_THAT(FormatAddress(0x59DE50), StrEq("0059DE50"));
  EXPECT_THAT(FormatAddress(0x00000001004940B0), StrEq("00000001004940B0"));
  EXPECT_THAT(FormatAddress(0x7FF00000004926F4), StrEq("7FF00000004926F4"));
}

TEST(FormatUtilTest, HumanReadableDuration) {
  EXPECT_THAT(HumanReadableDuration(3600), StrEq("1h"));
  EXPECT_THAT(HumanReadableDuration(1800), StrEq("30m"));
  EXPECT_THAT(HumanReadableDuration(45), StrEq("45s"));
  EXPECT_THAT(HumanReadableDuration(10.123), StrEq("10.12s"));
  EXPECT_THAT(HumanReadableDuration(9045.123), StrEq("2h 30m 45.12s"));
  EXPECT_THAT(HumanReadableDuration(9000.123), StrEq("2h 30m 0.12s"));
}

}  // namespace
}  // namespace security::binexport
