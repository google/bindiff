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
