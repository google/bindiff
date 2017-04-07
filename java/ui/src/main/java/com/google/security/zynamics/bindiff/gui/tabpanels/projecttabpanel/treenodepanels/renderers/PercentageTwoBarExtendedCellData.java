package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.misc.EPercentageBarSortType;
import com.google.security.zynamics.zylib.general.Triple;
import java.awt.Toolkit;

public class PercentageTwoBarExtendedCellData {
  private final EPercentageBarSortType sortRelevance;

  private Triple<Integer, Integer, Integer> data;

  public PercentageTwoBarExtendedCellData(
      final int leftValue, final int innerLeftValue, final int rightvalue) {
    this(leftValue, innerLeftValue, rightvalue, EPercentageBarSortType.SORT_BY_CENTER_VALUE);
  }

  public PercentageTwoBarExtendedCellData(
      final int leftValue,
      final int innerLeftValue,
      final int rightValue,
      final EPercentageBarSortType sort) {
    data = new Triple<>(leftValue, innerLeftValue, rightValue);

    sortRelevance = sort == null ? EPercentageBarSortType.SORT_BY_CENTER_VALUE : sort;
  }

  public double getInnerLeftBarPercent() {
    return data.second() / (double) getTotalBarValue() * 100.;
  }

  public int getInnerLeftBarValue() {
    return data.second();
  }

  public double getLeftBarPercent() {
    return data.first() / (double) getTotalBarValue() * 100.;
  }

  public String getLeftBarString(final boolean showPercent, final boolean showInner) {
    String fsInner = "";
    if (showInner) {
      fsInner = String.format("%d", getInnerLeftBarValue());

      if (showPercent && Toolkit.getDefaultToolkit().getScreenSize().width > 1024) {
        fsInner = String.format("%s (%.1f%s)", fsInner, getInnerLeftBarPercent(), "%");
      }

      fsInner += " / ";
    }

    String fsOuter = String.format("%d", getLeftBarValue());
    if (showPercent) {
      fsOuter = String.format("%s (%.1f%s)", fsOuter, getLeftBarPercent(), "%");
    }

    return fsInner + fsOuter;
  }

  public int getLeftBarValue() {
    return data.first();
  }

  public double getRightBarPercent() {
    return data.third() / (double) getTotalBarValue() * 100.;
  }

  public String getRightBarString(final boolean showPercent) {
    String fs = "%d";
    fs = String.format(fs, getRightBarValue());

    if (showPercent) {
      fs = String.format("%s (%.1f%s)", fs, getRightBarPercent(), "%");
    }

    return fs;
  }

  public int getRightBarValue() {
    return data.third();
  }

  public double getSortByValue() {
    if (sortRelevance == EPercentageBarSortType.SORT_BY_LEFT_VALUE) {
      return data.first();
    }
    if (sortRelevance == EPercentageBarSortType.SORT_BY_RIGHT_VALUE) {
      return data.third();
    }
    if (sortRelevance == EPercentageBarSortType.SORT_BY_INNERRIGHT) {
      return data.second();
    }
    return getTotalBarValue();
  }

  public int getTotalBarValue() {
    final int total = data.first() + data.third();

    return total == 0 ? 1 : total;
  }

  public void updateData(final int leftValue, final int innerLeftValue, final int rightValue) {
    data = new Triple<>(leftValue, innerLeftValue, rightValue);
  }
}
