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

package com.google.security.zynamics.bindiff.graph.searchers;

import com.google.security.zynamics.zylib.gui.zygraph.realizers.CStyleRunData;
import java.awt.Color;
import java.util.List;

public class SearchResult {
  private final int line;
  private final int index;
  private final int length;

  private final List<CStyleRunData> originalTextBackgroundStyleRun;
  private final Color originalBorderColor;

  private final Object object;
  private final String text;

  private Color objectMarkerColor = Color.WHITE;

  public SearchResult(
      final Object object,
      final int line,
      final int index,
      final int length,
      final String text,
      final List<CStyleRunData> textBackgroundStyleRun,
      final Color originalBorderColor) {
    this.object = object;
    this.line = line;
    this.index = index;
    this.length = length;
    this.text = text;
    this.originalTextBackgroundStyleRun = textBackgroundStyleRun;
    this.originalBorderColor = originalBorderColor;
  }

  public int getLength() {
    return length;
  }

  public int getLine() {
    return line;
  }

  public Object getObject() {
    return object;
  }

  public Color getObjectMarkerColor() {
    return objectMarkerColor;
  }

  public Color getOriginalBorderColor() {
    return originalBorderColor;
  }

  public List<CStyleRunData> getOriginalTextBackgroundStyleRun() {
    return originalTextBackgroundStyleRun;
  }

  public int getPosition() {
    return index;
  }

  public String getText() {
    return text;
  }

  public void setObjectMarkerColor(final Color color) {
    objectMarkerColor = color;
  }
}
