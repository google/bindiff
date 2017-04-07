// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph;

import java.text.ParseException;

public enum MouseWheelAction {
  ZOOM, SCROLL;

  public static MouseWheelAction parseInt(final int action) throws ParseException {
    if (action == ZOOM.ordinal()) {
      return ZOOM;
    } else if (action == SCROLL.ordinal()) {
      return SCROLL;
    } else {
      throw new ParseException("Error: Invalid action", 0);
    }
  }
}
