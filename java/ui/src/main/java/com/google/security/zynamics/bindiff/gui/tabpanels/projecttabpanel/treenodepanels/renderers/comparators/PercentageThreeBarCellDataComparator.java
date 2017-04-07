package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.comparators;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.PercentageThreeBarCellData;
import java.util.Comparator;

public class PercentageThreeBarCellDataComparator
    implements Comparator<PercentageThreeBarCellData> {
  @Override
  public int compare(final PercentageThreeBarCellData o1, final PercentageThreeBarCellData o2) {
    if (o1 == null && o2 == null) {
      return 0;
    } else if (o1 == null) {
      return 1;
    } else if (o2 == null) {
      return -1;
    }

    final double value = o1.getSortByValue() - o2.getSortByValue();

    if (value > 0) {
      return 1;
    } else if (value < 0) {
      return -1;
    }
    return 0;
  }
}
