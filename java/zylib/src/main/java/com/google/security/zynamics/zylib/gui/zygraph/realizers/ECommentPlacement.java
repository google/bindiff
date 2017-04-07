// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.realizers;

public enum ECommentPlacement {
  ABOVE_LINE, BEHIND_LINE;

  public static int getOrdinal(final ECommentPlacement placement) {
    return placement == ECommentPlacement.ABOVE_LINE ? 0 : 1;
  }

  public static ECommentPlacement valueOf(final int ordinal) {
    if (ordinal == 0) {
      return ECommentPlacement.ABOVE_LINE;
    } else if (ordinal == 1) {
      return ECommentPlacement.BEHIND_LINE;
    }

    throw new IllegalStateException("Error: Unknown ordinal value.");
  }
}
