// Copyright 2011-2024 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.zylib.gui.zygraph.realizers;

import java.awt.Color;

public class CHighlighting implements Comparable<CHighlighting> {
  /** Helper class to manage highlighting information of the line. */
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
