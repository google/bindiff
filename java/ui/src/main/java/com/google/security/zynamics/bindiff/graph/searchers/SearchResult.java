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
