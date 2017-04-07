package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.comparators;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.ESortOrder;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.sorter.ISortableTreeNode;
import java.util.Comparator;

public class TreeNodeVisibilityComparator implements Comparator<ISortableTreeNode> {
  private final ESortOrder order;

  public TreeNodeVisibilityComparator(final ESortOrder order) {
    this.order = Preconditions.checkNotNull(order);
  }

  @Override
  public int compare(final ISortableTreeNode o1, final ISortableTreeNode o2) {
    int value = 0;

    if (!o1.isVisible() && o2.isVisible()) {
      value = 1;
    } else if (o1.isVisible() && !o2.isVisible()) {
      value = -1;
    }

    if (order == ESortOrder.DESCENDING) {
      value *= -1;
    }

    return value;
  }
}
