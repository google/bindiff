// Copyright 2011-2021 Google LLC
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
