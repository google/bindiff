// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.layouters;


public enum OrthogonalOrientation {
  VERTICAL, HORIZONTAL;

  public static OrthogonalOrientation parseInt(final int orientation) {
    if (orientation == VERTICAL.ordinal()) {
      return VERTICAL;
    } else if (orientation == HORIZONTAL.ordinal()) {
      return HORIZONTAL;
    } else {
      throw new IllegalStateException("Internal Error: Unknown orientation");
    }
  }
}
