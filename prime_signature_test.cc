#include "third_party/zynamics/bindiff/prime_signature.h"

#ifndef GOOGLE
#include <gmock/gmock.h>
#include <gtest/gtest.h>
#else
#include "gmock/gmock.h"
#include "gtest/gtest.h"
#endif

using ::testing::Eq;

namespace security {
namespace bindiff {
namespace {

TEST(PrimeSignatureTest, IPow32MathEdgeCases) {
  // Zero exponent
  EXPECT_THAT(IPow32(0, 0), Eq(1));  // Per definition
  EXPECT_THAT(IPow32(1, 0), Eq(1));
  EXPECT_THAT(IPow32(1181, 0), Eq(1));
  EXPECT_THAT(IPow32(1299299, 0), Eq(1));

  // Unity
  EXPECT_THAT(IPow32(1, 2), Eq(1));
  EXPECT_THAT(IPow32(1, 4), Eq(1));
  EXPECT_THAT(IPow32(1, 400), Eq(1));
}

TEST(PrimeSignatureTest, IPow32NonOverflow) {
  EXPECT_THAT(IPow32(2, 4), Eq(16));
  EXPECT_THAT(IPow32(12, 2), Eq(144));

  EXPECT_THAT(IPow32(953, 3), Eq(865523177));
}

TEST(PrimeSignatureTest, IPow32Overflow) {
  EXPECT_THAT(IPow32(953, 48), Eq(1629949057));
  EXPECT_THAT(IPow32(1296829, 3600), Eq(454359873));
}

TEST(PrimeSignatureTest, GetPrimeX86Mnemonics) {
  // A few X86 instructions
  EXPECT_THAT(GetPrime("aeskeygenassist"), Eq(1655998781));
  EXPECT_THAT(GetPrime("mov"), Eq(3449882749));
  EXPECT_THAT(GetPrime("vfnmsubss"), Eq(1672159675));
}

TEST(PrimeSignatureTest, GetPrimeCollision) {
  // TODO(b/124334881): These should not have the same hash
  EXPECT_THAT(GetPrime("ITTEE NETEE NE"), Eq(4141088768));
  EXPECT_THAT(GetPrime("ITETT LSETT LS"), Eq(4141088768));
}

}  // namespace
}  // namespace bindiff
}  // namespace security
