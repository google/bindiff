package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.treenodes.AbstractTreeNode;

public interface IWorkspaceTreeListener {
  void changedSelection(final AbstractTreeNode diff);
}
