package com.google.security.zynamics.bindiff.enums;

public enum ELayoutOrientation {
  VERTICAL,
  HORIZONTAL;

  public static ELayoutOrientation getEnum(final int orientation) {
    return orientation == 0 ? VERTICAL : HORIZONTAL;
  }

  public static int getOrdinal(final ELayoutOrientation orientation) {
    return orientation == VERTICAL ? 0 : 1;
  }
}
