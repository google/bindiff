// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.JHexPanel;

import java.awt.Color;

public class ColoredRange implements Comparable<ColoredRange> {
  private final Color fcolor;

  private final long start;

  private final int size;

  private final Color bgcolor;

  public ColoredRange(final long start, final int size, final Color fcolor, final Color bgcolor) {
    this.start = start;
    this.size = size;
    this.fcolor = fcolor;
    this.bgcolor = bgcolor;
  }

  @Override
  public int compareTo(final ColoredRange range) {
    return Long.compare(start, range.start);
  }

  public boolean containsOffset(final long offset) {
    return (offset >= start) && (offset < (start + size));
  }

  public Color getBackgroundColor() {
    return bgcolor;
  }

  public Color getColor() {
    return fcolor;
  }

  public int getSize() {
    return size;
  }

  public long getStart() {
    return start;
  }
}
