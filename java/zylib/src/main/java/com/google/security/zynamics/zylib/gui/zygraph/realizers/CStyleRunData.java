// Copyright 2011-2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
