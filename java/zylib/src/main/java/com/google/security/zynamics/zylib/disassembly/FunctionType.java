// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.disassembly;

public enum FunctionType {
  // Please note that the order of the items is important because
  // the ordinal value of the enum members is used to sort the
  // functions in the function table.

  NORMAL, LIBRARY, IMPORT, THUNK, ADJUSTOR_THUNK, INVALID, UNKNOWN;

  public static FunctionType parseInt(final int value) {
    switch (value) {
      case 1:
        return NORMAL;
      case 2:
        return LIBRARY;
      case 3:
        return IMPORT;
      case 4:
        return THUNK;
      case 5:
        return ADJUSTOR_THUNK;
      case 6:
        return INVALID;
      case 7:
        return UNKNOWN;
      default:
        throw new IllegalArgumentException("Internal Error: Invalid function type " + value);
    }
  }

  @Override
  public String toString() {
    switch (this) {
      case NORMAL:
        return "Normal";
      case LIBRARY:
        return "Library";
      case IMPORT:
        return "Imported";
      case THUNK:
        return "Thunk";
      case ADJUSTOR_THUNK:
        return "Adjustor Thunk";
      case INVALID:
        return "Invalid";
      case UNKNOWN:
        return "Unknown";
      default:
        throw new IllegalArgumentException("Internal Error: Invalid function type");
    }
  }

}
