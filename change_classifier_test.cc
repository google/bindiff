#include "third_party/zynamics/bindiff/change_classifier.h"

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

TEST(ChangeClassifierTest, ChangeDescription) {
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
