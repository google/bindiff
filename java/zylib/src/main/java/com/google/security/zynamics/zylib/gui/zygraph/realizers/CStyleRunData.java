// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.realizers;

import java.awt.Color;

import com.google.common.base.Preconditions;

/**
 * Small helper class that is used to keep track of text colors.
 */
public class CStyleRunData {
  private final int m_start;

  private final int m_length;

  private final Color m_color;

  private final IZyEditableObject m_lineObject;

  private Object m_object;

  public CStyleRunData(final int start, final int length, final Color color) {
    this(start, length, color, null);
  }

  public CStyleRunData(final int start, final int length, final Color color,
      final IZyEditableObject lineObject) {
    Preconditions.checkArgument(length != 0, "Error: Invalid style run length");

    m_start = start;
    m_length = length;
    m_color = color;
    m_lineObject = lineObject;
  }

  public CStyleRunData(final int start, final int length, final Color color, final Object object) {
    this(start, length, color, null);

    m_object = object;
  }

  public Color getColor() {
    return m_color;
  }

  public int getEnd() {
    return m_start + m_length;
  }

  public int getLength() {
    return m_length;
  }

  public IZyEditableObject getLineObject() {
    return m_lineObject;
  }

  public Object getObject() {
    return m_object;
  }

  public int getStart() {
    return m_start;
  }
}
