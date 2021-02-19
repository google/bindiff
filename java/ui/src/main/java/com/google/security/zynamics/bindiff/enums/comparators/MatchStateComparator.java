// Copyright 2011-2021 Google LLC
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

package com.google.security.zynamics.bindiff.enums.comparators;

import com.google.security.zynamics.bindiff.enums.EMatchState;
import java.util.Comparator;

public class MatchStateComparator implements Comparator<EMatchState> {
  @Override
  public int compare(final EMatchState o1, final EMatchState o2) {
    final int i = EMatchState.getOrdinal(o1) - EMatchState.getOrdinal(o2);

    if (i < 0) {
      return 1;
    }
    if (i > 0) {
      return -1;
    }
    return 0;
  }
}
