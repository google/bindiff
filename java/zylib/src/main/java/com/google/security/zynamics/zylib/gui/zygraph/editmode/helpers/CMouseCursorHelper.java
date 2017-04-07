// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.editmode.helpers;

import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;

import java.awt.Cursor;


public class CMouseCursorHelper {
  private static Cursor DEFAULT_CURSOR = Cursor.getDefaultCursor();
  private static Cursor MOVE_CURSOR = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
  private static Cursor HAND_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

  public static void setDefaultCursor(final AbstractZyGraph<?, ?> graph) {
    if (graph.getViewCursor() != DEFAULT_CURSOR) {
      graph.setViewCursor(DEFAULT_CURSOR);
    }
  }

  public static void setHandCursor(final AbstractZyGraph<?, ?> graph) {
    if (graph.getViewCursor() != HAND_CURSOR) {
      graph.setViewCursor(HAND_CURSOR);
    }
  }

  public static void setMoveCursor(final AbstractZyGraph<?, ?> graph) {
    if (graph.getViewCursor() != MOVE_CURSOR) {
      graph.setViewCursor(MOVE_CURSOR);
    }
  }
}
