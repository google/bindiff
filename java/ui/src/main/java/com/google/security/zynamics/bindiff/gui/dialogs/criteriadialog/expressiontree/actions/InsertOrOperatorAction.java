package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.actions;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.ExpressionTreeActionProvider;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.operators.OrCriterion;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class InsertOrOperatorAction extends AbstractAction {
  private final ExpressionTreeActionProvider actionProvider;

  public InsertOrOperatorAction(final ExpressionTreeActionProvider actionProvider) {
    super("Insert OR");

    this.actionProvider = actionProvider;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    actionProvider.insertCriterion(new OrCriterion());
  }
}
