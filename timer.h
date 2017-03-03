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

#ifndef THIRD_PARTY_ZYNAMICS_BINEXPORT_TIMER_H_
#define THIRD_PARTY_ZYNAMICS_BINEXPORT_TIMER_H_

#include <chrono>  // NOLINT

// Minimal restartable timer class.
template <typename ClockT = std::chrono::steady_clock>
class Timer {
 public:
  // Returns the number of seconds elapsed since construction or the last call
  // to restart().
  double elapsed() {
    return static_cast<double>(
               std::chrono::duration_cast<std::chrono::nanoseconds>(
                   ClockT::now() - start_)
                   .count()) /
           std::chrono::nanoseconds(std::chrono::seconds(1)).count();
  }

  void restart() { start_ = ClockT::now(); }

 private:
  std::chrono::time_point<ClockT> start_ = ClockT::now();
};

#endif  // THIRD_PARTY_ZYNAMICS_BINEXPORT_TIMER_H_
