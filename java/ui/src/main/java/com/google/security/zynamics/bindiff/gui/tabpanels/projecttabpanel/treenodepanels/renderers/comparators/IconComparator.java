package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.comparators;

import com.google.common.base.Preconditions;
import java.util.Comparator;
import javax.swing.Icon;

public class IconComparator implements Comparator<Icon> {
  @Override
  public int compare(final Icon o1, final Icon o2) {
    Preconditions.checkNotNull(o1);
    Preconditions.checkNotNull(o2);

    return o1.toString().compareTo(o2.toString());
  }
}
