package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.operators;

public class NotCriterionPanel extends AbstractOperatorPanel {
  @Override
  public String getBorderTitle() {
    return "NOT Operator";
  }

  @Override
  public String getInvalidInfoString() {
    return "NOT operator requires exactly one child.";
  }

  @Override
  public String getValidInfoString() {
    return "NOT Operator is valid.";
  }
}
