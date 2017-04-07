// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.layouters;


public enum OrthogonalStyle {
  NORMAL, TREE;

  public static OrthogonalStyle parseInt(final int style) {
    if (style == NORMAL.ordinal()) {
      return NORMAL;
    } else if (style == TREE.ordinal()) {
      return TREE;
    } else {
      throw new IllegalStateException("Error: Invalid style " + style);
    }
  }
}
