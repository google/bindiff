package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.operators;

public class AndCriterionPanel extends AbstractOperatorPanel {
  public AndCriterionPanel() {
    super();
  }

  @Override
  public String getBorderTitle() {
    return "AND Operator";
  }

  @Override
  public String getInvalidInfoString() {
    return "AND operator needs at least two children.";
  }

  @Override
  public String getValidInfoString() {
    return "AND Operator is valid.";
  }
}
