package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.actions;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionCreator;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion.CriterionWrapper;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.ExpressionTreeActionProvider;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;

public class AddConditionAction extends AbstractAction {
  private final CriterionCreator condition;

  private final ExpressionTreeActionProvider actionProvider;

  public AddConditionAction(
      final CriterionCreator condition, final ExpressionTreeActionProvider actionProvider) {
    super(condition.getCriterionDescription());

    this.condition = condition;
    this.actionProvider = actionProvider;
  }

  public AddConditionAction(
      final JComboBox<CriterionWrapper> selectionBox,
      final ExpressionTreeActionProvider actionProvider) {
    this.condition = ((CriterionWrapper) selectionBox.getSelectedItem()).getObject();
    this.actionProvider = actionProvider;

    if (condition != null) {
      putValue(NAME, condition.getCriterionDescription());
    }
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    actionProvider.appendCriterion(condition.createCriterion());
  }
}
