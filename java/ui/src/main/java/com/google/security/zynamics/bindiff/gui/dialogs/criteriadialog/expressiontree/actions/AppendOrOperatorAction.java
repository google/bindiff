package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.actions;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.ExpressionTreeActionProvider;
import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.operators.OrCriterium;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class AppendOrOperatorAction extends AbstractAction {
  private final ExpressionTreeActionProvider actionProvider;

  public AppendOrOperatorAction(final ExpressionTreeActionProvider actionProvider) {
    super("Append OR");

    this.actionProvider = actionProvider;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    actionProvider.appendCriterium(new OrCriterium());
  }
}
