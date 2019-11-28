package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.operators;

public class OrCriterionPanel extends AbstractOperatorPanel {
  public OrCriterionPanel() {
    super();
  }

  @Override
  public String getBorderTitle() {
    return "OR Operator";
  }

  @Override
  public String getInvalidInfoString() {
    return "OR operator needs at least two child conditions or operators.";
  }

  @Override
  public String getValidInfoString() {
    return "OR Operator is valid.";
  }
}
