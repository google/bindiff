package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.comparators;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.enums.ESortOrder;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.sorter.ISortableTreeNode;
import java.util.Comparator;

public class CombinedTreeNodeSideComparator implements Comparator<ISortableTreeNode> {
  private final ESortOrder order;

  public CombinedTreeNodeSideComparator(final ESortOrder order) {
    Preconditions.checkNotNull(order);

    this.order = order;
  }

  @Override
  public int compare(final ISortableTreeNode o1, final ISortableTreeNode o2) {
    int value = 0;

    if (o1.getMatchState() == o2.getMatchState()) {
      return 0;
    }

    if (o1.getMatchState() == EMatchState.MATCHED) {
      value = -1;
    } else if (o2.getMatchState() == EMatchState.MATCHED) {
      value = 1;
    } else if (o1.getMatchState() == EMatchState.PRIMARY_UNMATCHED) {
      value = -1;
    } else if (o1.getMatchState() == EMatchState.SECONDRAY_UNMATCHED) {
      value = 1;
    }

    if (order == ESortOrder.DESCENDING) {
      value *= -1;
    }

    return value;
  }
}
