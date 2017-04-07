package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterium.CriteriumType;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.ExpressionTreeActionProvider;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.JCriteriumTree;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.actions.AddConditionAction;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.nodes.AbstractCriteriumTreeNode;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.tree.TreePath;

/** Listener class that handles clicks on the Add Condition button. */
public class AddConditionButtonListener extends AbstractAction {
  private final JCriteriumTree jtree;
  private final ConditionBox selectionBox;
  private final ExpressionTreeActionProvider actionProvider;

  public AddConditionButtonListener(
      final JCriteriumTree criteriumTree,
      final ConditionBox box,
      final ExpressionTreeActionProvider actionProvider) {
    this.jtree = criteriumTree;
    this.selectionBox = box;
    this.actionProvider = actionProvider;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final TreePath path = jtree.getSelectionPath();

    final boolean isRoot = path.getPathCount() == 1;
    if (!isRoot
        && ((AbstractCriteriumTreeNode) path.getLastPathComponent()).getCriterium().getType()
            == CriteriumType.CONDITION) {
      jtree.setCurrentCriteriumPath(path.getParentPath());
    } else {
      jtree.setCurrentCriteriumPath(path);
    }

    new AddConditionAction(selectionBox, actionProvider).actionPerformed(event);
  }
}
