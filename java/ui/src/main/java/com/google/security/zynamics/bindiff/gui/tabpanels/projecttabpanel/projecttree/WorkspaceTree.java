// Copyright 2011-2020 Google LLC
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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.popupmenu.NoNodePopupMenu;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.treenodes.AbstractTreeNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.treenodes.AllFunctionDiffViewsNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.treenodes.FunctionDiffViewsNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.treenodes.NodeRenderer;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.treenodes.RootNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.FunctionDiffViewsNodeContextPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.FunctionDiffViewsContainerTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.FunctionDiffViewsTable;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.bindiff.project.WorkspaceAdapter;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.jtree.TreeHelpers;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class WorkspaceTree extends JTree {
  private final JPopupMenu popup;

  private final WorkspaceTreeModel workspaceTreeModel;

  private final RootNode rootNode;

  private final WorkspaceTabPanelFunctions controller;

  private final InternalTreeSelectionListener treeSelectionListener =
      new InternalTreeSelectionListener();

  private final InternalMouseListener mouseListener = new InternalMouseListener();

  private final InternalWorkspaceModelListener mainWindowModelListener =
      new InternalWorkspaceModelListener();

  private final ListenerProvider<IWorkspaceTreeListener> listeners = new ListenerProvider<>();

  public WorkspaceTree(final WorkspaceTabPanelFunctions controller) {
    this.controller = Preconditions.checkNotNull(controller);
    this.controller.setWorkspaceTree(this);

    rootNode = new RootNode(this, this.controller);

    popup = new NoNodePopupMenu(this.controller);

    setRootVisible(false);

    workspaceTreeModel = new WorkspaceTreeModel(this, rootNode);
    setModel(workspaceTreeModel);

    setCellRenderer(new NodeRenderer());

    final DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
    selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    setSelectionModel(selectionModel);

    setBorder(new EmptyBorder(2, 4, 2, 2));

    addTreeSelectionListener(treeSelectionListener);
    addMouseListener(mouseListener);

    this.controller.getWorkspace().addListener(mainWindowModelListener);
  }

  public static boolean hasFunctionDiffRelatives(final WorkspaceTree tree, final Diff diff) {
    if (!diff.isFunctionDiff()) {
      return false;
    }

    final TreeNode allFunctionDiffsNode = tree.getModel().getRoot().getChildAt(0);
    if (allFunctionDiffsNode.isLeaf()) {
      return false;
    }

    final File parentFile = diff.getMatchesDatabase().getParentFile();
    for (int index = 0; index < allFunctionDiffsNode.getChildCount(); ++index) {
      final FunctionDiffViewsNode childNode =
          (FunctionDiffViewsNode) allFunctionDiffsNode.getChildAt(index);
      final FunctionDiffViewsNodeContextPanel component = childNode.getComponent();
      final FunctionDiffViewsTable table = (FunctionDiffViewsTable) component.getTables().get(0);

      if (table.getRowCount() > 0
          && parentFile.equals(
              AbstractTable.getRowDiff(table, 0).getMatchesDatabase().getParentFile())) {
        return true;
      }
    }

    return false;
  }

  private void handleDoubleClick(final MouseEvent event) {
    final AbstractTreeNode selectedNode =
        (AbstractTreeNode) TreeHelpers.getNodeAt(this, event.getX(), event.getY());

    if (selectedNode == null) {
      return;
    }

    selectedNode.doubleClicked();
  }

  private void nodeSelected(final AbstractTreeNode node) {
    controller.setTreeNodeContextComponent(node.getComponent());
  }

  private void showPopupMenu(final MouseEvent event) {
    final AbstractTreeNode selectedNode =
        (AbstractTreeNode) TreeHelpers.getNodeAt(this, event.getX(), event.getY());

    if (selectedNode != null) {
      setSelectionPath(new TreePath(((DefaultMutableTreeNode) selectedNode).getPath()));

      final JPopupMenu menu = selectedNode.getPopupMenu();

      if (menu != null) {
        menu.show(this, event.getX(), event.getY());
      }
    } else {
      popup.show(this, event.getX(), event.getY());
    }
  }

  public void addListener(final IWorkspaceTreeListener treeListener) {
    listeners.addListener(treeListener);
  }

  public void closeFunctionDiffs() {
    final AllFunctionDiffViewsNode node = (AllFunctionDiffViewsNode) rootNode.getFirstChild();
    node.deleteChildren();

    updateTree();
  }

  public void dispose() {
    removeTreeSelectionListener(treeSelectionListener);
    removeMouseListener(mouseListener);
    controller.getWorkspace().removeListener(mainWindowModelListener);
  }

  @Override
  public WorkspaceTreeModel getModel() {
    return workspaceTreeModel;
  }

  public void removeListener(final IWorkspaceTreeListener treeListener) {
    listeners.removeListener(treeListener);
  }

  public void updateTree() {
    getModel().nodeStructureChanged(rootNode);
  }

  private class InternalMouseListener extends MouseAdapter {
    @Override
    public void mousePressed(final MouseEvent event) {
      if (event.isPopupTrigger()) {
        showPopupMenu(event);
      } else if (event.getClickCount() == 2) {
        handleDoubleClick(event);
      }
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
      if (event.isPopupTrigger()) {
        showPopupMenu(event);
      }
    }
  }

  private class InternalTreeSelectionListener implements TreeSelectionListener {
    private DefaultMutableTreeNode lastNode = null;

    @Override
    public void valueChanged(final TreeSelectionEvent event) {
      final AbstractTreeNode node = (AbstractTreeNode) getLastSelectedPathComponent();

      if (node != null) {
        nodeSelected(node); // A new node was selected

        lastNode = node;

      } else if (lastNode == null || !TreeHelpers.isAncestor(lastNode, rootNode)) {
        nodeSelected(rootNode); // A node was deleted from the tree
      }

      for (final IWorkspaceTreeListener listener : listeners) {
        listener.changedSelection(node != null ? node : rootNode);
      }
    }
  }

  private class InternalWorkspaceModelListener extends WorkspaceAdapter {
    @Override
    public void addedDiff(final Diff diff) {
      if (!diff.isFunctionDiff()) {
        rootNode.addDiff(diff);
        updateTree();
      } else {
        final AllFunctionDiffViewsNode node = (AllFunctionDiffViewsNode) rootNode.getFirstChild();

        ((FunctionDiffViewsContainerTable) node.getComponent().getTables().get(0)).addRow(diff);

        final TreePath treePath = new TreePath(node.getPath());
        final boolean wasExpanded = isExpanded(treePath);
        final TreePath selectionPath = getSelectionPath();
        node.addDiff(diff);
        updateTree();

        for (int index = 0; index < node.getChildCount(); ++index) {
          final FunctionDiffViewsNode child = (FunctionDiffViewsNode) node.getChildAt(index);
          if (child.getViewDirectory().equals(diff.getMatchesDatabase().getParentFile())) {
            ((FunctionDiffViewsTable) child.getComponent().getTables().get(0)).addRow(diff);
            break;
          }
        }

        if (wasExpanded) {
          expandPath(new TreePath(node.getPath()));
        }
        setSelectionPath(selectionPath);
      }
    }

    @Override
    public void closedWorkspace() {
      rootNode.deleteChildren();
      final TreePath rootPath = new TreePath(rootNode.getPath());
      getSelectionModel().setSelectionPath(rootPath);
      updateTree();
    }

    @Override
    public void loadedWorkspace(final Workspace workspace) {
      rootNode.setWorkspace(workspace);
      getModel().nodeStructureChanged(rootNode);
    }

    @Override
    public void removedDiff(final Diff diff) {
      if (diff.isFunctionDiff()) {
        final AllFunctionDiffViewsNode node = (AllFunctionDiffViewsNode) rootNode.getFirstChild();
        final FunctionDiffViewsContainerTable table =
            (FunctionDiffViewsContainerTable) node.getComponent().getTables().get(0);

        table.removeRow(diff);

        for (int index = 0; index < node.getChildCount(); ++index) {
          final FunctionDiffViewsNode child = (FunctionDiffViewsNode) node.getChildAt(index);
          if (child.getViewDirectory().equals(diff.getMatchesDatabase().getParentFile())) {
            ((FunctionDiffViewsTable) child.getComponent().getTables().get(0)).removeRow(diff);
            break;
          }
        }

        for (int index = 0; index < node.getChildCount(); ++index) {
          final FunctionDiffViewsNode child = (FunctionDiffViewsNode) node.getChildAt(index);
          if (child.getViewDirectory().equals(diff.getMatchesDatabase().getParentFile())) {
            ((FunctionDiffViewsTable) child.getComponent().getTables().get(0)).addRow(diff);
            break;
          }
        }
      }
    }
  }
}
