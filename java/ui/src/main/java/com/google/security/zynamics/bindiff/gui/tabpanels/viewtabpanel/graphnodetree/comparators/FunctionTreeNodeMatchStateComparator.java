package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.comparators;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.enums.EMatchType;
import com.google.security.zynamics.bindiff.enums.ESortOrder;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.sorter.ISortableTreeNode;
import java.util.Comparator;

public class FunctionTreeNodeMatchStateComparator implements Comparator<ISortableTreeNode> {
  private final ESortOrder order;

  public FunctionTreeNodeMatchStateComparator(final ESortOrder order) {
    this.order = Preconditions.checkNotNull(order);
  }

  @Override
  public int compare(final ISortableTreeNode o1, final ISortableTreeNode o2) {
    int value = 0;

    if (o1.getMatchState() == EMatchState.MATCHED && o2.getMatchState() == EMatchState.MATCHED) {
      if (o1.getMatchType() == o2.getMatchType()) {
        return 0;
      }

      if (o1.getMatchType() != EMatchType.IDENTICAL && o2.getMatchType() == EMatchType.IDENTICAL) {
        value = 1;
      } else if (o1.getMatchType() == EMatchType.IDENTICAL
          && o2.getMatchType() != EMatchType.IDENTICAL) {
        value = -1;
      } else if (o1.getMatchType() == EMatchType.INSTRUCTIONS_CHANGED
          && o2.getMatchType() == EMatchType.STRUCTURAL_CHANGED) {
        value = -1;
      } else if (o1.getMatchType() == EMatchType.STRUCTURAL_CHANGED
          && o2.getMatchType() == EMatchType.INSTRUCTIONS_CHANGED) {
        value = 1;
      }

    } else if (o1.getMatchState() != EMatchState.MATCHED
        && o2.getMatchState() == EMatchState.MATCHED) {
      value = 1;
    } else if (o1.getMatchState() == EMatchState.MATCHED
        && o2.getMatchState() != EMatchState.MATCHED) {
      value = -1;
    }

    if (order == ESortOrder.DESCENDING) {
      value *= -1;
    }

    return value;
  }
}
