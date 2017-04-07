// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.realizers;

import java.awt.Color;

public class CHighlighting implements Comparable<CHighlighting> {
  /**
   * Helper class to manage highlighting information of the line.
   */
  private final double m_start;

  private final double m_end;

  private final int m_level;

  private final Color m_color;

  public CHighlighting(final int level, final double start, final double end, final Color color) {
    m_level = level;
    m_start = start;
    m_end = end;
    m_color = color;
  }

  @Override
  public int compareTo(final CHighlighting o) {
    return m_level - o.m_level;
  }

  public Color getColor() {
    return m_color;
  }

  public double getEnd() {
    return m_end;
  }

  public int getLevel() {
    return m_level;
  }

  public double getStart() {
    return m_start;
  }
}
