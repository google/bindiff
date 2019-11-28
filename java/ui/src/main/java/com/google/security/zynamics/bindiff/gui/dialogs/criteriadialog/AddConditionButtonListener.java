package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionType;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.ExpressionTreeActionProvider;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.JCriterionTree;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.actions.AddConditionAction;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.nodes.AbstractCriterionTreeNode;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.tree.TreePath;

/** Listener class that handles clicks on the Add Condition button. */
public class AddConditionButtonListener extends AbstractAction {
  private final JCriterionTree jtree;
  private final ConditionBox selectionBox;
  private final ExpressionTreeActionProvider actionProvider;

  public AddConditionButtonListener(
      final JCriterionTree criterionTree,
      final ConditionBox box,
      final ExpressionTreeActionProvider actionProvider) {
    this.jtree = criterionTree;
    this.selectionBox = box;
    this.actionProvider = actionProvider;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final TreePath path = jtree.getSelectionPath();

    final boolean isRoot = path.getPathCount() == 1;
    if (!isRoot
        && ((AbstractCriterionTreeNode) path.getLastPathComponent()).getCriterion().getType()
            == CriterionType.CONDITION) {
      jtree.setCurrentCriterionPath(path.getParentPath());
    } else {
      jtree.setCurrentCriterionPath(path);
    }

    new AddConditionAction(selectionBox, actionProvider).actionPerformed(event);
  }
}
