package com.google.security.zynamics.bindiff.enums;

public enum EJumpType {
  JUMP_TRUE,
  JUMP_FALSE,
  UNCONDITIONAL,
  SWITCH;

  public static final int getOrdinal(final EJumpType type) {
    switch (type) {
      case JUMP_TRUE:
        return 0;
      case JUMP_FALSE:
        return 1;
      case UNCONDITIONAL:
        return 2;
      case SWITCH:
        return 3;
      default:
        throw new IllegalArgumentException("Unknown jump type");
    }
  }

  public static EJumpType getType(final int ordinal) {
    switch (ordinal) {
      case 0:
        return JUMP_TRUE;
      case 1:
        return JUMP_FALSE;
      case 2:
        return UNCONDITIONAL;
      case 3:
        return SWITCH;
      default:
        throw new IllegalArgumentException("Unknown jump type");
    }
  }
}
