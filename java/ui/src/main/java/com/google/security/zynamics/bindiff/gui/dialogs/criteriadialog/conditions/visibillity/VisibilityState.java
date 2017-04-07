package com.google.security.zynamics.bindiff.gui.dialogs.criteriadialog.conditions.visibillity;

public enum VisibilityState {
  VISIBLE,
  UNVISIBLE;

  @Override
  public String toString() {
    return this == VISIBLE ? "visible" : "invisible";
  }
}
