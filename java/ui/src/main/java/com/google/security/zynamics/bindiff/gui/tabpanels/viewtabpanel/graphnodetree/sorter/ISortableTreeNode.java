package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.graphnodetree.sorter;

import com.google.security.zynamics.bindiff.enums.EFunctionType;
import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.enums.EMatchType;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.zylib.disassembly.IAddress;

public interface ISortableTreeNode {
  IAddress getAddress();

  IAddress getAddress(ESide side);

  String getFunctionName();

  EFunctionType getFunctionType();

  EMatchState getMatchState();

  EMatchType getMatchType();

  boolean isSelected();

  boolean isVisible();
}
