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
