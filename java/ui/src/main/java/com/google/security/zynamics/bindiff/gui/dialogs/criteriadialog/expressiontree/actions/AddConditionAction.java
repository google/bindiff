package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.actions;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterium.CriteriumWrapper;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterium.ICriteriumCreator;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.ExpressionTreeActionProvider;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;

public class AddConditionAction extends AbstractAction {
  private final ICriteriumCreator condition;

  private final ExpressionTreeActionProvider actionProvider;

  public AddConditionAction(
      final ICriteriumCreator condition, final ExpressionTreeActionProvider actionProvider) {
    super(condition.getCriteriumDescription());

    this.condition = condition;
    this.actionProvider = actionProvider;
  }

  public AddConditionAction(
      final JComboBox<String> selectionBox, final ExpressionTreeActionProvider actionProvider) {
    this.condition = ((CriteriumWrapper) selectionBox.getSelectedItem()).getObject();
    this.actionProvider = actionProvider;

    if (condition != null) {
      putValue(NAME, condition.getCriteriumDescription());
    }
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    actionProvider.appendCriterium(condition.createCriterium());
  }
}
