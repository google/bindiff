package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.criterion;

public enum CriterionType {
  CONDITION,
  AND,
  OR,
  NOT;

  public boolean isOperator() {
    return this != CONDITION;
  }
}
