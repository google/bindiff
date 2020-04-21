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

#include "third_party/zynamics/bindiff/xmlconfig.h"

#include <string>

#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "third_party/zynamics/binexport/util/status_matchers.h"

namespace security::bindiff {
namespace {

using ::not_absl::IsOk;
using ::testing::ElementsAre;
using ::testing::Eq;
using ::testing::StrEq;

TEST(XmlConfigTest, BasicFunctionality) {
  XmlConfig config;
  ASSERT_THAT(config.LoadFromString(R"raw(<?xml version="1.0"?>
<doc version="1">
  <value setting="Test"/>
  <settings>
    <entry key="bool" value="true"/>
    <entry key="double" value="3.14159"/>
    <entry key="int" value="42"/>
    <entry key="string" value="A string"/>
  </settings>
</doc>
)raw"),
              IsOk());

  EXPECT_THAT(config.ReadInt("/doc/@version", /*default_value=*/-1), Eq(1));
  EXPECT_THAT(config.ReadString("/doc/value/@setting",
                                /*default_value=*/"<no_value>"),
              StrEq("Test"));
  EXPECT_THAT(config.ReadInt("/doc/value/@no-such-setting",
                             /*default_value=*/47),
              Eq(47));
  EXPECT_THAT(config.ReadBool("/doc/settings/entry[@key='bool']/@value",
                              /*default_value=*/false),
              Eq(true));
  EXPECT_THAT(config.ReadDouble("/doc/settings/entry[@key='double']/@value",
                                /*default_value=*/2.71828),
              Eq(3.14159));
  EXPECT_THAT(config.ReadInt("/doc/settings/entry[@key='int']/@value",
                             /*default_value=*/47),
              Eq(42));
  EXPECT_THAT(config.ReadString("/doc/settings/entry[@key='string']/@value",
                                /*default_value=*/"<no_value>"),
              StrEq("A string"));

  EXPECT_THAT(config.ReadStrings("/doc/settings/entry/@value",
                                 /*default_value=*/{}),
              ElementsAre("true", "3.14159", "42", "A string"));
}

}  // namespace
}  // namespace security::bindiff
