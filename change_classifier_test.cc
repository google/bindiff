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
