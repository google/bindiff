package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.misc.EPercentageBarSortType;
import com.google.security.zynamics.zylib.general.Quad;

public class PercentageThreeBarExtendedCellData {
  private final Quad<Integer, Integer, Integer, Integer> data;

  private final EPercentageBarSortType sortRelevance;

  public PercentageThreeBarExtendedCellData(
      final int leftValue,
      final int centerValue,
      final int innerCenterValue,
      final int rightValue) {
    this(
        leftValue,
        centerValue,
        innerCenterValue,
        rightValue,
        EPercentageBarSortType.SORT_BY_CENTER_VALUE);
  }

  public PercentageThreeBarExtendedCellData(
      final int leftValue,
      final int centerValue,
      final int innerCenterValue,
      final int rightValue,
      final EPercentageBarSortType sort) {
    data = new Quad<>(leftValue, centerValue, innerCenterValue, rightValue);

    sortRelevance = sort == null ? EPercentageBarSortType.SORT_BY_CENTER_VALUE : sort;
  }

  public double getCenterBarPercent() {
    return data.second() / getTotalBarValue() * 100.;
  }

  public String getCenterBarString(final boolean showPercent) {
    String fs = "%d";
    fs = String.format(fs, getCenterBarValue());

    if (showPercent) {
      fs += "%s(%.1f)";
      fs = String.format(fs, getCenterBarPercent());
    }

    return fs;
  }

  public int getCenterBarValue() {
    return data.second();
  }

  public double getInnerCenterBarPercent() {
    return data.third() / getTotalBarValue() * 100.;
  }

  public int getInnerCenterBarValue() {
    return data.third();
  }

  public double getLeftBarPercent() {
    return data.first() / getTotalBarValue() * 100.;
  }

  public String getLeftBarString(final boolean showPercent) {
    String fs = "%d";
    fs = String.format(fs, getLeftBarValue());

    if (showPercent) {
      fs += "%s(%.1f)";
      fs = String.format(fs, getLeftBarPercent());
    }

    return fs;
  }

  public int getLeftBarValue() {
    return data.first();
  }

  public double getRightBarPercent() {
    return data.fourth() / getTotalBarValue() * 100.;
  }

  public String getRightBarString(final boolean showPercent) {
    String fs = "%d";
    fs = String.format(fs, getRightBarValue());

    if (showPercent) {
      fs += "%s(%.1f)";
      fs = String.format(fs, getRightBarPercent());
    }

    return fs;
  }

  public int getRightBarValue() {
    return data.fourth();
  }

  public double getSortByValue() {
    if (sortRelevance == EPercentageBarSortType.SORT_BY_TOTAL_SUM) {
      return data.first() + data.second() + data.fourth();
    } else if (sortRelevance == EPercentageBarSortType.SORT_BY_LEFT_AND_CENTER_SUM) {
      return data.first() + data.second();
    } else if (sortRelevance == EPercentageBarSortType.SORT_BY_RIGHT_AND_CENTER_SUM) {
      return data.second() + data.fourth();
    } else if (sortRelevance == EPercentageBarSortType.SORT_BY_LEFT_VALUE) {
      return data.first();
    } else if (sortRelevance == EPercentageBarSortType.SORT_BY_CENTER_VALUE) {
      return data.second();
    } else if (sortRelevance == EPercentageBarSortType.SORT_BY_RIGHT_VALUE) {
      return data.fourth();
    } else if (sortRelevance == EPercentageBarSortType.SORT_BY_INNERCENTER) {
      return data.third();
    }

    return getTotalBarValue();
  }

  public int getTotalBarValue() {
    return data.first() + data.second() + data.fourth();
  }
}
