package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.comparators;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.ESortOrder;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.sorter.ISortableTreeNode;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import java.util.Comparator;

public class SingleTreeNodeAdressComparator implements Comparator<ISortableTreeNode> {
  private final ESortOrder order;

  public SingleTreeNodeAdressComparator(final ESortOrder order) {
    this.order = Preconditions.checkNotNull(order);
  }

  @Override
  public int compare(final ISortableTreeNode o1, final ISortableTreeNode o2) {
    int value = 0;

    final IAddress addr1 = o1.getAddress();
    final IAddress addr2 = o2.getAddress();

    value = addr1.compareTo(addr2);

    if (order == ESortOrder.DESCENDING) {
      value *= -1;
    }

    return value;
  }
}
