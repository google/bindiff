#ifndef TEST_UTIL_H_
#define TEST_UTIL_H_

#ifndef GOOGLE
#include <gmock/gmock.h>
#include <gtest/gtest.h>
#else
#include "gmock/gmock.h"
#include "gtest/gtest.h"
#endif

namespace security::bindiff {

// Loads a BinDiff default configuration suitable for testing. This helps to
// avoid using the the built-in default configuration as it has name matching
// enabled, which can make tests pointless (both binaries contain full symbols
// and BinDiff will simply match everything based on that).
// Intended to be calles from a test suite's SetUpTestSuite().
void ApplyDefaultConfigForTesting();

}  // namespace security::bindiff

#endif  // TEST_UTIL_H_
