package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.comparators;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.enums.ESortOrder;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.sorter.ISortableTreeNode;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import java.util.Comparator;

public class CombinedTreeNodeAdressComparator implements Comparator<ISortableTreeNode> {
  private final ESortOrder order;

  public CombinedTreeNodeAdressComparator(final ESortOrder order) {
    Preconditions.checkNotNull(order);

    this.order = order;
  }

  @Override
  public int compare(final ISortableTreeNode o1, final ISortableTreeNode o2) {
    int value = 0;

    final IAddress priAddr_o1 = o1.getAddress(ESide.PRIMARY);
    final IAddress secAddr_o1 = o1.getAddress(ESide.SECONDARY);

    final IAddress priAddr_o2 = o2.getAddress(ESide.PRIMARY);
    final IAddress secAddr_o2 = o2.getAddress(ESide.SECONDARY);

    if (priAddr_o1 == null && priAddr_o2 == null) {
      value = secAddr_o1.compareTo(secAddr_o2);
    } else if (priAddr_o1 == null) {
      value = 1;
    } else if (priAddr_o2 == null) {
      value = -1;
    } else {
      value = priAddr_o1.compareTo(priAddr_o2);
    }

    if (order == ESortOrder.DESCENDING) {
      value *= -1;
    }

    return value;
  }
}
