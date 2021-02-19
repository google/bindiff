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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.misc.EPercentageBarSortType;
import com.google.security.zynamics.zylib.general.Pair;

public class PercentageTwoBarCellData {
  private final Pair<Integer, Integer> data;

  private final EPercentageBarSortType sortRelevance;

  private final boolean inverted;

  public PercentageTwoBarCellData(final int firstValue, final int secondvalue) {
    this(firstValue, secondvalue, EPercentageBarSortType.SORT_BY_CENTER_VALUE, false);
  }

  public PercentageTwoBarCellData(
      final int firstValue,
      final int secondvalue,
      final EPercentageBarSortType sort,
      final boolean inverted) {
    data = new Pair<>(firstValue, secondvalue);

    sortRelevance = sort == null ? EPercentageBarSortType.SORT_BY_CENTER_VALUE : sort;
    this.inverted = inverted;
  }

  public double getLeftBarPercent() {
    if (inverted) {
      return data.second() / getTotalBarValue() * 100.;
    }
    return data.first() / getTotalBarValue() * 100.;
  }

  public String getLeftBarString(final boolean showPercent) {
    String fs = "%d";
    fs = String.format(fs, inverted ? getRightBarValue() : getLeftBarValue());

    if (showPercent) {
      fs += "%s(%.1f)";
      fs = String.format(fs, inverted ? getRightBarPercent() : getLeftBarPercent());
    }

    return fs;
  }

  public int getLeftBarValue() {
    if (inverted) {
      return data.second();
    }
    return data.first();
  }

  public double getRightBarPercent() {
    if (inverted) {
      return data.first() / getTotalBarValue() * 100.;
    }
    return data.second() / getTotalBarValue() * 100.;
  }

  public String getRightBarString(final boolean showPercent) {
    String fs = "%d";
    fs = String.format(fs, inverted ? getLeftBarValue() : getRightBarValue());

    if (showPercent) {
      fs += "%s(%.1f)";
      fs = String.format(fs, inverted ? getLeftBarPercent() : getRightBarPercent());
    }

    return fs;
  }

  public int getRightBarValue() {
    if (inverted) {
      return data.first();
    }
    return data.second();
  }

  public double getSortByValue() {
    if (sortRelevance == EPercentageBarSortType.SORT_BY_LEFT_VALUE) {
      return data.first();
    }
    if (sortRelevance == EPercentageBarSortType.SORT_BY_RIGHT_VALUE) {
      return data.second();
    }
    return getTotalBarValue();
  }

  public int getTotalBarValue() {
    return data.first() + data.second();
  }
}
