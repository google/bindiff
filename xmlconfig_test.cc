#include "third_party/zynamics/bindiff/xmlconfig.h"

#ifndef GOOGLE
#include <gmock/gmock.h>
#include <gtest/gtest.h>
#else
#include "gmock/gmock.h"
#include "gtest/gtest.h"
#endif

#include <string>

using ::testing::Eq;
using ::testing::StrEq;

namespace security {
namespace bindiff {
namespace {

TEST(XmlConfigTest, BasicFunctionality) {
  auto config(XmlConfig::LoadFromString(R"raw(<?xml version="1.0"?>
<doc version="1">
  <value setting="Test"/>
  <settings>
    <entry key="bool" value="true"/>
    <entry key="double" value="3.14159"/>
    <entry key="int" value="42"/>
    <entry key="string" value="A string"/>
  </settings>
</doc>
)raw"));
  EXPECT_THAT(config->ReadInt("/doc/@version", /*default_value=*/-1), Eq(1));
  EXPECT_THAT(config->ReadString("/doc/value/@setting",
                                 /*default_value=*/"<no_value>"),
              StrEq("Test"));
  EXPECT_THAT(config->ReadInt("/doc/value/@no-such-setting",
                              /*default_value=*/47),
              Eq(47));
  EXPECT_THAT(config->ReadBool("/doc/settings/entry[@key='bool']/@value",
                               /*default_value=*/false),
              Eq(true));
  EXPECT_THAT(config->ReadDouble("/doc/settings/entry[@key='double']/@value",
                                 /*default_value=*/2.71828),
              Eq(3.14159));
  EXPECT_THAT(config->ReadInt("/doc/settings/entry[@key='int']/@value",
                              /*default_value=*/47),
              Eq(42));
  EXPECT_THAT(config->ReadString("/doc/settings/entry[@key='string']/@value",
                                 /*default_value=*/"<no_value>"),
              StrEq("A string"));
}

}  // namespace
}  // namespace bindiff
}  // namespace security
