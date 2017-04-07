package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.comparators;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.PercentageTwoBarExtendedCellData;
import java.util.Comparator;

public class PercentageTwoBarExtendedCellDataComparator
    implements Comparator<PercentageTwoBarExtendedCellData> {
  @Override
  public int compare(
      final PercentageTwoBarExtendedCellData o1, final PercentageTwoBarExtendedCellData o2) {
    final double value = o1.getSortByValue() - o2.getSortByValue();

    if (value > 0) {
      return 1;
    } else if (value < 0) {
      return -1;
    }
    return 0;
  }
}
