// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.layouters;


public enum CircularStyle {
  COMPACT, ISOLATED, SINGLE_CIRCLE;

  public static CircularStyle parseInt(final int style) {
    if (style == COMPACT.ordinal()) {
      return COMPACT;
    } else if (style == ISOLATED.ordinal()) {
      return ISOLATED;
    } else if (style == SINGLE_CIRCLE.ordinal()) {
      return SINGLE_CIRCLE;
    } else {
      throw new IllegalStateException("Error: Invalid style");
    }
  }
}
