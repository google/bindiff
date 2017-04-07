package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.treenodes;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.WorkspaceTree;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.WorkspaceTreeModel;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

public abstract class AbstractTreeNode extends DefaultMutableTreeNode {
  private final WorkspaceTabPanelFunctions controller;

  private final Diff diff;

  public AbstractTreeNode(final WorkspaceTabPanelFunctions controller, final Diff diff) {
    this.controller = Preconditions.checkNotNull(controller);
    this.diff = diff;
  }

  protected abstract void createChildren();

  protected abstract void delete();

  protected WorkspaceTabPanelFunctions getController() {
    return controller;
  }

  protected WorkspaceTree getTree() {
    return controller.getWorkspaceTree();
  }

  protected WorkspaceTreeModel getTreeModel() {
    return controller.getWorkspaceTree().getModel();
  }

  public void deleteChildren() {
    for (int i = 0; i < getChildCount(); i++) {
      final AbstractTreeNode child = (AbstractTreeNode) getChildAt(i);

      child.delete();
    }

    removeAllChildren();
  }

  public abstract void doubleClicked();

  public abstract Component getComponent();

  public Diff getDiff() {
    return diff;
  }

  public abstract Icon getIcon();

  public abstract JPopupMenu getPopupMenu();

  @Override
  public RootNode getRoot() {
    return controller.getWorkspaceTree().getModel().getRoot();
  }

  @Override
  public abstract String toString();
}
