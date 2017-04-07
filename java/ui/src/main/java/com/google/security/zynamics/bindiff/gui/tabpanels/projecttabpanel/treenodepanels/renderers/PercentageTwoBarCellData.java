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
