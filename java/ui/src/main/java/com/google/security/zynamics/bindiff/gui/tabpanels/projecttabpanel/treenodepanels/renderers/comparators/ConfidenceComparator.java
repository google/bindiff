// Copyright 2011-2023 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.comparators;

import com.google.security.zynamics.zylib.general.Pair;
import java.util.Comparator;

// TODO(cblichmann): Generalize into PairDoubleComparator
public class ConfidenceComparator implements Comparator<Pair<Double, Double>> {
  @Override
  public int compare(final Pair<Double, Double> o1, final Pair<Double, Double> o2) {
    if (o1.second() > o2.second()) {
      return -1;
    } else if (o1.second() < o2.second()) {
      return 1;
    }

    return 0;
  }
}
