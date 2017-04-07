package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.actions;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.expressiontree.ExpressionTreeActionProvider;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class RemoveAllAction extends AbstractAction {
  private final ExpressionTreeActionProvider actionProvider;

  public RemoveAllAction(final ExpressionTreeActionProvider actionProvider) {
    super("Remove All");

    this.actionProvider = actionProvider;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    actionProvider.removeAll();
  }
}
