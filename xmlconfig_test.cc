#include "third_party/zynamics/bindiff/xmlconfig.h"

#include <gtest/gtest.h>

#include <string>

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
  EXPECT_EQ(config->ReadInt("/doc/@version", /* default_value = */ -1), 1);
  EXPECT_EQ(config->ReadString("/doc/value/@setting",
                               /* default_value = */ "<no_value>"),
            "Test");
  EXPECT_EQ(config->ReadBool("/doc/settings/entry[@key='bool']/@value",
                             /* default_value = */ false),
            true);
  EXPECT_EQ(config->ReadDouble("/doc/settings/entry[@key='double']/@value",
                               /* default_value = */ 2.71828),
            3.14159);
  EXPECT_EQ(config->ReadInt("/doc/settings/entry[@key='int']/@value",
                            /* default_value = */ 47),
            42);
  EXPECT_EQ(config->ReadString("/doc/settings/entry[@key='string']/@value",
                               /* default_value = */ "<no_value>"),
            "A string");
}

}  // namespace
