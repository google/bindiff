package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.comparators;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.PercentageTwoBarCellData;
import java.util.Comparator;

public class PercentageTwoBarCellDataComparator implements Comparator<PercentageTwoBarCellData> {
  @Override
  public int compare(final PercentageTwoBarCellData o1, final PercentageTwoBarCellData o2) {
    final double value = o1.getSortByValue() - o2.getSortByValue();

    if (value > 0) {
      return 1;
    } else if (value < 0) {
      return -1;
    }
    return 0;
  }
}
