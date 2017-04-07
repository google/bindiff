// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.layouters;


public enum HierarchicOrientation {
  VERTICAL, HORIZONTAL;

  public static HierarchicOrientation parseInt(final int orientation) {
    if (orientation == VERTICAL.ordinal()) {
      return VERTICAL;
    } else if (orientation == HORIZONTAL.ordinal()) {
      return HORIZONTAL;
    } else {
      throw new IllegalStateException("Internal Error: Invalid orientation value");
    }
  }
}
