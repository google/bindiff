package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.actions;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.ExpressionTreeActionProvider;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.operators.AndCriterion;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class InsertAndOperatorAction extends AbstractAction {
  private final ExpressionTreeActionProvider actionProvider;

  public InsertAndOperatorAction(final ExpressionTreeActionProvider actionProvider) {
    super("Insert AND");

    this.actionProvider = actionProvider;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    actionProvider.insertCriterion(new AndCriterion());
  }
}
