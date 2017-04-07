package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.WorkspaceTree;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.treenodes.AllFunctionDiffViewsNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.treenodes.RootNode;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.tree.TreePath;

public class CloseFunctionDiffsAction extends AbstractAction {
  private final WorkspaceTabPanelFunctions controller;

  public CloseFunctionDiffsAction(final WorkspaceTabPanelFunctions controller) {
    this.controller = Preconditions.checkNotNull(controller);
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final Set<Diff> functionDiffs = new HashSet<>();
    functionDiffs.addAll(controller.getWorkspace().getDiffList(true));
    controller.closeDiffs(functionDiffs);
    final WorkspaceTree tree = controller.getWorkspaceTree();
    final RootNode rootNode = tree.getModel().getRoot();
    final AllFunctionDiffViewsNode functionDiffsNode =
        (AllFunctionDiffViewsNode) rootNode.getChildAt(0);
    functionDiffsNode.deleteChildren();
    tree.setSelectionPath(new TreePath(functionDiffsNode.getPath()));

    tree.updateTree();
  }
}
