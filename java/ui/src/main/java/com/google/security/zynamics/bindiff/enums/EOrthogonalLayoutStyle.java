package com.google.security.zynamics.bindiff.enums;

public enum EOrthogonalLayoutStyle {
  NORMAL,
  TREE;

  public static EOrthogonalLayoutStyle getEnum(final int style) {
    return style == 0 ? NORMAL : TREE;
  }

  public static int getOrdinal(final EOrthogonalLayoutStyle style) {
    return style == NORMAL ? 0 : 1;
  }
}
