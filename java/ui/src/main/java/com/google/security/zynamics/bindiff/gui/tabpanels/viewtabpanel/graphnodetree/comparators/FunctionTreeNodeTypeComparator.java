package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.comparators;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.EFunctionType;
import com.google.security.zynamics.bindiff.enums.ESortOrder;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.sorter.ISortableTreeNode;
import java.util.Comparator;

public class FunctionTreeNodeTypeComparator implements Comparator<ISortableTreeNode> {
  private final ESortOrder order;

  public FunctionTreeNodeTypeComparator(final ESortOrder order) {
    this.order = Preconditions.checkNotNull(order);
  }

  @Override
  public int compare(final ISortableTreeNode o1, final ISortableTreeNode o2) {
    final Integer t1 = EFunctionType.getOrdinal(o1.getFunctionType());
    final Integer t2 = EFunctionType.getOrdinal(o2.getFunctionType());

    int value = t1.compareTo(t2);

    if (order == ESortOrder.DESCENDING) {
      value *= -1;
    }

    return value;
  }
}
