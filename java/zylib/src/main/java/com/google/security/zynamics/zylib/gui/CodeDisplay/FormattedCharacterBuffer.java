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

// TODO(thomasdullien): Change the package name to lowercase prior to submitting
// after LGTM for the code has been obtained. I don't want to do this now since
// it will break all code review continuity.
package com.google.security.zynamics.zylib.gui.CodeDisplay;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.text.AttributedString;

/**
 * A class for a "pseudo-terminal" buffer - e.g. an x * y sized character buffer which allows each
 * character to be highlighted in a different way (and have a different font associated with it).
 */
public class FormattedCharacterBuffer {
  /** The buffer to keep the actual data that should be displayed. */
  private char[] charBuffer = new char[0];
  /** Array of references to the font for each character. */
  private Font[] perCharFonts = new Font[0];
  private static final Font DEFAULT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
  /** Foreground and background color for each character. */
  private Color[] perCharForegroundColor = new Color[0];
  private Color defaultForegroundColor = Color.BLACK;

  private Color[] perCharBackgroundColor = new Color[0];
  private Color defaultBackgroundColor = Color.WHITE;

  private int numberOfLinesInBuffer;
  private int numberOfColumnsInBuffer;

  public FormattedCharacterBuffer(int lines, int columns) {
    numberOfLinesInBuffer = lines;
    numberOfColumnsInBuffer = columns;
    charBuffer = new char[numberOfLinesInBuffer * numberOfColumnsInBuffer];
    perCharFonts = new Font[numberOfLinesInBuffer * numberOfColumnsInBuffer];
    perCharForegroundColor = new Color[numberOfLinesInBuffer * numberOfColumnsInBuffer];
    perCharBackgroundColor = new Color[numberOfLinesInBuffer * numberOfColumnsInBuffer];
    clear();
  }

  public void clear() {
    for (int i = 0; i < numberOfLinesInBuffer * numberOfColumnsInBuffer; i++) {
      charBuffer[i] = ' ';
      perCharFonts[i] = DEFAULT_FONT;
      perCharForegroundColor[i] = defaultForegroundColor;
      perCharBackgroundColor[i] = defaultBackgroundColor;
    }
  }

  /**
   * Create a single-line formatted character buffer. Newlines are converted to underscores.
   */
  public FormattedCharacterBuffer(String data, Font font, Color foreground, Color background) {
    this(/* Single line per default */ 1, data.length());
    for (int i = 0; i < numberOfLinesInBuffer * numberOfColumnsInBuffer; i++) {
      charBuffer[i] = (data.charAt(i) == '\n') ? '_' : data.charAt(i);
      perCharFonts[i] = font;
      perCharForegroundColor[i] = foreground;
      perCharBackgroundColor[i] = background;
    }
  }

  public int getNumberOfColumns() {
    return numberOfColumnsInBuffer;
  }

  public int getNumberOfLines() {
    return numberOfLinesInBuffer;
  }

  public FormattedCharacterBuffer setBackgroundColor(Color background) {
    for (int index = 0; index < numberOfLinesInBuffer * numberOfColumnsInBuffer; index++) {
      perCharBackgroundColor[index] = background;
    }
    return this;
  }

  public void setAt(int lineIndex,
      int columnIndex,
      char value,
      Font font,
      Color foreground,
      Color background) {
    int writeIndex = (lineIndex * numberOfColumnsInBuffer) + columnIndex;
    charBuffer[writeIndex] = value;
    perCharFonts[writeIndex] = font;
    perCharForegroundColor[writeIndex] = foreground;
    perCharBackgroundColor[writeIndex] = background;
  }

  /**
   * Bulk-copy a smaller FormattedCharacterBuffer into the current one at a given location in the
   * given buffer.
   */
  public boolean copyInto(int lineNum, int columnNum, FormattedCharacterBuffer other) {
    // Disallow the copying outside of the right lines/columns. This is in fact a common occurrence
    // when the buffer is updated from the model, therefore no exception is thrown here.
    if ((lineNum + other.getNumberOfLines() > numberOfLinesInBuffer)
        || (columnNum + other.getNumberOfColumns() > numberOfColumnsInBuffer)) {
      return false;
    }
    for (int lineIndex = 0; lineIndex < other.getNumberOfLines(); lineIndex++) {
      for (int columnIndex = 0; columnIndex < other.getNumberOfColumns(); columnIndex++) {
        int readIndex = (lineIndex * other.getNumberOfColumns()) + columnIndex;
        char value = other.charBuffer[readIndex];
        Font font = other.perCharFonts[readIndex];
        Color foreground = other.perCharForegroundColor[readIndex];
        Color background = other.perCharBackgroundColor[readIndex];

        setAt(lineNum + lineIndex, columnNum + columnIndex, value, font, foreground, background);
      }
    }
    return true;
  }

  /**
   * Returns a particular line in the current FormattedCharacterBuffer.
   */
  public FormattedCharacterBuffer getLine(int lineIndex) {
    FormattedCharacterBuffer result = new FormattedCharacterBuffer(1 /* one line only */,
        numberOfColumnsInBuffer);
    for (int column = 0; column < getNumberOfColumns(); column++) {
      int readIndex = (lineIndex * getNumberOfColumns()) + column;
      char value = charBuffer[readIndex];
      Font font = perCharFonts[readIndex];
      Color foreground = perCharForegroundColor[readIndex];
      Color background = perCharBackgroundColor[readIndex];

      result.setAt(0, column, value, font, foreground, background);
    }
    return result;
  }

  /**
   * Paints the current buffer onto a given graphics context, skipping the first N character
   * columns.
   */
  protected void paintBuffer(final Graphics2D context, int x, int y, int skipColumns) {
    context.setFont(perCharFonts[0]);
    FontMetrics fontMetrics = context.getFontMetrics();
    // Clear the context with the default background color.
    context.fillRect(0, 0, numberOfColumnsInBuffer * fontMetrics.charWidth('a'),
        numberOfLinesInBuffer * fontMetrics.getHeight());

    for (int i = 0; i < numberOfLinesInBuffer; i++) {
      AttributedString stringToDraw = getAttributedStringForLine(i, skipColumns);
      // the +1 for the line index is necessary to avoid cutting off first line.
      context.drawString(stringToDraw.getIterator(), x,
          y + fontMetrics.getHeight() * (i + 1));
    }
  }

  /** Returns the AttributedString to be drawn, skipping the first skip characters. */
  private AttributedString getAttributedStringForLine(int lineIndex, int skip) {
    String newLine = new String(charBuffer, (numberOfColumnsInBuffer * lineIndex) + skip,
        numberOfColumnsInBuffer - skip);
    AttributedString attributedString = new AttributedString(newLine);

    int lineStart = (lineIndex * numberOfColumnsInBuffer) + skip;
    for (int index = 0; index < numberOfColumnsInBuffer - skip; index++) {
      attributedString.addAttribute(java.awt.font.TextAttribute.FONT,
          perCharFonts[lineStart + index], index, index + 1);
      attributedString.addAttribute(java.awt.font.TextAttribute.BACKGROUND,
          perCharBackgroundColor[lineStart + index], index, index + 1);
      attributedString.addAttribute(java.awt.font.TextAttribute.FOREGROUND,
          perCharForegroundColor[lineStart + index], index, index + 1);
    }
    return attributedString;
  }

  /** Rarely-used function to return the contents of the buffer as string */
  public String getCharBufferAsString() {
    StringBuilder buffer =
        new StringBuilder(numberOfLinesInBuffer * numberOfColumnsInBuffer);
    for (int lineIndex = 0; lineIndex < numberOfLinesInBuffer; lineIndex++) {
      buffer.append(
          charBuffer, numberOfColumnsInBuffer * lineIndex, numberOfColumnsInBuffer);
      buffer.append("\n");
    }
    return buffer.toString();
  }
}
