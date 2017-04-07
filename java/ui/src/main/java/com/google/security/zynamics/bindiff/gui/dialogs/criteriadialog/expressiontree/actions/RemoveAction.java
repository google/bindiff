package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.actions;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.ExpressionTreeActionProvider;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.nodes.AbstractCriteriumTreeNode;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.tree.TreePath;

public class RemoveAction extends AbstractAction {
  private final AbstractCriteriumTreeNode node;

  private final ExpressionTreeActionProvider actionProvider;

  public RemoveAction(
      final AbstractCriteriumTreeNode node, final ExpressionTreeActionProvider actionProvider) {
    super("Remove");

    this.node = node;
    this.actionProvider = actionProvider;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    actionProvider.remove(new TreePath(node.getPath()));
  }
}
