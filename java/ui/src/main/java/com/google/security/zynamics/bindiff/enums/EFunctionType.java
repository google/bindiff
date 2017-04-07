package com.google.security.zynamics.bindiff.enums;

public enum EFunctionType {
  NORMAL,
  LIBRARY,
  THUNK,
  IMPORTED,
  UNKNOWN,
  MIXED;

  public static int getOrdinal(final EFunctionType functionType) {
    switch (functionType) {
      case NORMAL:
        return 0;
      case LIBRARY:
        return 1;
      case THUNK:
        return 2;
      case IMPORTED:
        return 3;
      case UNKNOWN:
        return 4;
      case MIXED:
        return 5;
    }

    throw new IllegalArgumentException("Unknown raw function type.");
  }

  public static EFunctionType getType(final int ordinal) {
    switch (ordinal) {
      case 0:
        return NORMAL;
      case 1:
        return LIBRARY;
      case 2:
        return THUNK;
      case 3:
        return IMPORTED;
      case 4:
        return UNKNOWN;
      case 5:
        return MIXED;
      default: // fall out
    }

    throw new IllegalArgumentException("Unknown raw function type.");
  }

  public static String getTypeChar(final EFunctionType functionType) {
    switch (functionType) {
      case NORMAL:
        return "N";
      case LIBRARY:
        return "L";
      case THUNK:
        return "T";
      case IMPORTED:
        return "I";
      case UNKNOWN:
        return "U";
      case MIXED:
        return "Mixed Function Match";
    }

    throw new IllegalArgumentException("Unknown raw function type.");
  }

  @Override
  public String toString() {
    switch (this) {
      case NORMAL:
        return "Normal";
      case LIBRARY:
        return "Library";
      case THUNK:
        return "Thunk";
      case IMPORTED:
        return "Imported";
      case UNKNOWN:
        return "Unknown";
      case MIXED:
        return "Mixed Function Match";
    }

    throw new IllegalArgumentException("Unknown raw function type.");
  }
}
