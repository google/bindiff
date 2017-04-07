// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.disassembly;

public enum ViewType {
  Native, NonNative;

  public static ViewType parseInt(final int value) {
    if ((value - 1) == Native.ordinal()) {
      return Native;
    } else if ((value - 1) == NonNative.ordinal()) {
      return NonNative;
    } else {
      throw new IllegalStateException("Internal Error: Unknown view type");
    }
  }
}
