// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph;

public enum EdgeHidingMode {
  HIDE_NEVER, HIDE_ALWAYS, HIDE_ON_THRESHOLD;

  public static EdgeHidingMode parseInt(final int edgeHidingMode) {
    if (edgeHidingMode == HIDE_NEVER.ordinal()) {
      return HIDE_NEVER;
    } else if (edgeHidingMode == HIDE_ALWAYS.ordinal()) {
      return HIDE_ALWAYS;
    } else if (edgeHidingMode == HIDE_ON_THRESHOLD.ordinal()) {
      return HIDE_ON_THRESHOLD;
    } else {
      throw new IllegalStateException("Error: Invalid edge hiding mode");
    }
  }
}
