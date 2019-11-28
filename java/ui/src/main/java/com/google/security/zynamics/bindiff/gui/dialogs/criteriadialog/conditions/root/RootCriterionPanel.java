package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.root;

import com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.operators.AbstractOperatorPanel;

public class RootCriterionPanel extends AbstractOperatorPanel {
  @Override
  public String getBorderTitle() {
    return "Root Node";
  }

  @Override
  public String getInvalidInfoString() {
    return "Root node needs exactly one child condition or operator.";
  }

  @Override
  public String getValidInfoString() {
    return "Root node is valid.";
  }
}
