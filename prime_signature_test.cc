#include "third_party/zynamics/bindiff/prime_signature.h"

#include <cmath>

#ifndef GOOGLE
#include <gmock/gmock.h>
#include <gtest/gtest.h>
#else
#include "gmock/gmock.h"
#include "gtest/gtest.h"
#include "testing/base/public/benchmark.h"
#endif

#include "third_party/absl/container/flat_hash_set.h"

using ::testing::Eq;
using ::testing::Ne;
using ::testing::SizeIs;

namespace security::bindiff {
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

TEST(PrimeSignatureTest, GetPrimeDistinctX86Mnemonics) {
  // A few X86 instructions. Make sure they don't map to the same value.
  absl::flat_hash_set<uint32_t> distinct_mnemonics = {
      GetPrime("add"), GetPrime("sub"),
      GetPrime("xor"), GetPrime("aeskeygenassist"),
      GetPrime("mov"), GetPrime("vfnmsubss"),
  };
  EXPECT_THAT(distinct_mnemonics, SizeIs(6));
}

TEST(PrimeSignatureTest, GetPrimeCheckCollision) {
  // b/124334881: These should not have the same hash
  EXPECT_THAT(GetPrime("ITTEE NETEE NE"), Ne(GetPrime("ITETT LSETT LS")));
}

#ifdef GOOGLE
void BM_IPow32(::benchmark::State& state) {
  const uint32_t exp = state.range(0);
  uint32_t base = 102345;
  for (const auto _ : state) {
    ::testing::DoNotOptimize(base);
    const auto result = IPow32(base, exp);
    ::testing::DoNotOptimize(result);
  }
}

void BM_pow(::benchmark::State& state) {
  const uint32_t exp = state.range(0);
  uint32_t base = 102345;
  for (const auto _ : state) {
    ::testing::DoNotOptimize(base);
    const auto result = pow(base, exp);
    ::testing::DoNotOptimize(result);
  }
}

BENCHMARK(BM_pow)->DenseRange(0, 64);
BENCHMARK(BM_IPow32)->DenseRange(0, 64);
#endif

}  // namespace
}  // namespace security::bindiff
