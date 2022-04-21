// Copyright 2011-2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.comparators;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.enums.ESortOrder;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.sorter.ISortableTreeNode;
import java.util.Comparator;

public class CombinedTreeNodeSideComparator implements Comparator<ISortableTreeNode> {
  private final ESortOrder order;

  public CombinedTreeNodeSideComparator(final ESortOrder order) {
    checkNotNull(order);

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
