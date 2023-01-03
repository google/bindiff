// Copyright 2011-2023 Google LLC
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

package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionCreator;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressionmodel.CriterionTree;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.nodes.AbstractCriterionTreeNode;
import com.google.security.zynamics.zylib.gui.jtree.TreeHelpers;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

public class JCriterionTree extends JTree {
  private final InternalMouseListener mouseListener = new InternalMouseListener();

  private final JCriterionTreeModel model;

  private TreePath currentCriterionPath = null;

  public JCriterionTree(final CriterionTree ctree, final List<CriterionCreator> criteria) {
    model = new JCriterionTreeModel(this, ctree, criteria);

    setRootVisible(true);
    setModel(model);
    setCellRenderer(new TreeNodeRenderer());

    addMouseListener(mouseListener);

    updateUI();
  }

  private void showPopupMenu(final MouseEvent event) {
    final AbstractCriterionTreeNode selectedNode =
        (AbstractCriterionTreeNode) TreeHelpers.getNodeAt(this, event.getX(), event.getY());

    if (selectedNode != null) {
      final JPopupMenu menu = selectedNode.getPopupMenu();

      if (menu != null) {
        menu.show(this, event.getX(), event.getY());
      }
    }
  }

  public void delete() {
    removeMouseListener(mouseListener);
  }

  public TreePath getCurrentCriterionPath() {
    return currentCriterionPath;
  }

  @Override
  public JCriterionTreeModel getModel() {
    return model;
  }

  public void setCurrentCriterionPath(final TreePath path) {
    currentCriterionPath = path;
  }

  private class InternalMouseListener extends MouseAdapter {
    @Override
    public void mousePressed(final MouseEvent event) {
      currentCriterionPath = getPathForLocation(event.getX(), event.getY());

      if (event.isPopupTrigger()) {
        showPopupMenu(event);
      }
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
      currentCriterionPath = getPathForLocation(event.getX(), event.getY());

      if (event.isPopupTrigger()) {
        showPopupMenu(event);
      }
    }
  }
}
