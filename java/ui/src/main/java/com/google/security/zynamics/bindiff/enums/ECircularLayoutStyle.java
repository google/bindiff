package com.google.security.zynamics.bindiff.enums;

public enum ECircularLayoutStyle {
  COMPACT,
  ISOLATED,
  SINGLE_CYCLE;

  public static ECircularLayoutStyle getEnum(final int style) {
    switch (style) {
      case 0:
        return COMPACT;
      case 1:
        return ISOLATED;
      case 2:
        return SINGLE_CYCLE;
    }

    throw new IllegalArgumentException("Unknown circular layout style.");
  }

  public static int getOrdinal(final ECircularLayoutStyle style) {
    switch (style) {
      case COMPACT:
        return 0;
      case ISOLATED:
        return 1;
      case SINGLE_CYCLE:
        return 2;
    }

    throw new IllegalArgumentException("Unknown cicular layout style.");
  }
}
