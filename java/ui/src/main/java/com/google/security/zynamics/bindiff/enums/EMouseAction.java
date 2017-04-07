package com.google.security.zynamics.bindiff.enums;

public enum EMouseAction {
  ZOOM,
  SCROLL;

  public static EMouseAction getEnum(final int action) {
    return action == 0 ? ZOOM : SCROLL;
  }

  public static int getOrdinal(final EMouseAction action) {
    return action == ZOOM ? 0 : 1;
  }
}
