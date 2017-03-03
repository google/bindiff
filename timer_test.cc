// Copyright 2011-2017 Google Inc. All Rights Reserved.
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

#include "third_party/zynamics/binexport/timer.h"

#include <gtest/gtest.h>

// Minimal steady clock implementation for testing. Time monotonically
// increases in units of 5 seconds (until the underlying time_point
// representation wraps around).
struct MockSteadyClock {
  typedef std::chrono::seconds duration;
  typedef duration::rep rep;
  typedef duration::period period;
  typedef std::chrono::time_point<MockSteadyClock, duration> time_point;

  static constexpr bool is_steady = true;

  static time_point now() noexcept {
    static time_point current_time;
    current_time += std::chrono::seconds(5);
    return current_time;
  }
};

TEST(TimerTest, Ints) {
  Timer<MockSteadyClock> timer;
  EXPECT_EQ(timer.elapsed(), 5);
  EXPECT_EQ(timer.elapsed(), 10);
  timer.restart();
  EXPECT_EQ(timer.elapsed(), 5);
}
