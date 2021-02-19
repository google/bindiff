// Copyright 2011-2021 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.comparators;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.enums.ESortOrder;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.sorter.ISortableTreeNode;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import java.util.Comparator;

public class CombinedTreeNodeAdressComparator implements Comparator<ISortableTreeNode> {
  private final ESortOrder order;

  public CombinedTreeNodeAdressComparator(final ESortOrder order) {
    checkNotNull(order);

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
