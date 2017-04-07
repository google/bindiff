package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.recursion;

public enum RecursionState {
  IS_RECURSION,
  IS_NOT_RECURSION;

  @Override
  public String toString() {
    return this == IS_RECURSION ? "recursive" : "not recursive";
  }
}
