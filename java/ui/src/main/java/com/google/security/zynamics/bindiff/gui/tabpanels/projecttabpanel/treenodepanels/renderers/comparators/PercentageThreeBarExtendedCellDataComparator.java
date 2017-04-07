package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.comparators;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.PercentageThreeBarExtendedCellData;
import java.util.Comparator;

public class PercentageThreeBarExtendedCellDataComparator
    implements Comparator<PercentageThreeBarExtendedCellData> {
  @Override
  public int compare(
      final PercentageThreeBarExtendedCellData o1, final PercentageThreeBarExtendedCellData o2) {
    final double value = o1.getSortByValue() - o2.getSortByValue();

    if (value > 0) {
      return 1;
    } else if (value < 0) {
      return -1;
    }
    return 0;
  }
}
