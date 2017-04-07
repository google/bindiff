package com.google.security.zynamics.bindiff.enums;

public enum EGraphLayout {
  HIERARCHICAL,
  ORTHOGONAL,
  CIRCULAR;

  public static EGraphLayout getEnum(final int style) {
    switch (style) {
      case 0:
        return HIERARCHICAL;
      case 1:
        return ORTHOGONAL;
      case 2:
        return CIRCULAR;
    }

    throw new IllegalArgumentException("Unknown layout style.");
  }

  public static int getOrdinal(final EGraphLayout style) {
    switch (style) {
      case HIERARCHICAL:
        return 0;
      case ORTHOGONAL:
        return 1;
      case CIRCULAR:
        return 2;
    }

    throw new IllegalArgumentException("Unknown layout style.");
  }
}
