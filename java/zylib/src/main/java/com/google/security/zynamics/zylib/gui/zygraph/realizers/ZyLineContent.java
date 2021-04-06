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

package com.google.security.zynamics.zylib.gui.zygraph.realizers;

import com.google.common.base.Preconditions;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class describes a single row of content in a standard line node.
 * 
 * Note that this class can only work with monospaced fonts. Using other fonts results in undefined
 * behavior.
 * 
 * Note that there are a number of functions that modify part of the line. An example for such a
 * function is setFont(position, length, font). The length argument of all of these functions can be
 * -1. This means that the modification is performed on all characters between the start position
 * and the end of the line.
 */
public class ZyLineContent {
  /**
   * Used to generate text layouts from the line text.
   */
  private static final FontRenderContext m_fontContext = new FontRenderContext(null, true, true);

  /**
   * Default alpha channel of the highlighter
   */
  private static final Composite DEFAULT_COMPOSITE = AlphaComposite.getInstance(
      AlphaComposite.SRC_OVER, 0.3f);

  /**
   * Standard alpha channel of the highlighter
   */
  private static final Composite NORMAL_COMPOSITE = AlphaComposite.getInstance(
      AlphaComposite.SRC_OVER, 1.0f);

  /**
   * Default border stroke
   */
  private static final Stroke DEFAULT_BORDER_STROKE = new BasicStroke(1.2f);

  /**
   * Standard stroke
   */
  private static final Stroke NORMAL_STROKE = new BasicStroke(1.0f);

  /**
   * Text that is displayed in the line.
   */
  private String m_text;

  /**
   * Text that is displayed in the line including all format information.
   */
  private AttributedString m_atext;

  /**
   * Assumed default character width that is used to calculate the line bounds.
   */
  private double m_charWidth;

  /**
   * Assumed default character height that is used to calculate the line bounds.
   */
  private double m_charHeight;

  /**
   * Text layout that is used to draw the line.
   */
  private TextLayout m_textLayout;

  /**
   * Highlighting information that is used when the line is drawn.
   * 
   * ATTENTION: Note that at any time this list must be sorted in the order in which the
   * highlighting information is used during the drawing phase.
   */
  private final ArrayList<CHighlighting> m_highlighting = new ArrayList<CHighlighting>();

  private final List<IZyEditableObject> m_lineObjects = new ArrayList<IZyEditableObject>();

  private final IZyEditableObject m_model;

  private Color m_backgroundColor = null;

  private final List<ObjectWrapper> m_objects = new ArrayList<ObjectWrapper>();

  public ZyLineContent(final String text, final Font font, final IZyEditableObject model) {
    this(text, font, new ArrayList<CStyleRunData>(), model);
  }

  public ZyLineContent(final String text, final Font font,
      final List<CStyleRunData> textColorStyleRun, final IZyEditableObject model) {
    Preconditions.checkNotNull(text, "Error: Text argument can't be null");
    Preconditions.checkNotNull(textColorStyleRun, "Error: Text color style run can't be null.");

    m_model = model;

    regenerateLine(text, font, textColorStyleRun);
  }

  public ZyLineContent(final String text, final IZyEditableObject model) {
    this(text, null, model);
  }

  /**
   * The functions that modify part of the line take a parameter length that describes how many
   * characters are changed. This parameter can be -1 to mean "until the end of the line". This
   * function takes the given length and converts it into the real number of characters to be
   * modified.
   * 
   * @param position Character index where the modification begins.
   * @param length Number of characters to modify.
   * 
   * @return The real number of characters to modify.
   */
  private int calculateRealLength(final int position, final int length) {
    return length != -1 ? length : m_text.length() - position;
  }

  /**
   * Highlights the line.
   * 
   * @param gfx The graphics context where the highlighting is drawn.
   * @param x The x position where the highlighting begins.
   * @param y The y position where the highlighting begins.
   * @param width The width of the highlighting area.
   * @param height The height of the highlighting area.
   * @param color The color of the highlighting area.
   */
  private void drawHighlighting(final Graphics2D gfx, final double x, final double y,
      final double width, final double height, final Color color) {
    gfx.setColor(color);

    final int roundedX = (int) Math.round(x);
    final int roundedY = (int) Math.round(y);
    final int roundedHeight = (int) Math.round(height);
    final int roundedWidth = (int) Math.round(width);

    gfx.setComposite(DEFAULT_COMPOSITE);

    // Draw the inner part of the highlighting area
    gfx.fillRoundRect(roundedX, roundedY, roundedWidth, roundedHeight, 10, 10);

    // Draw the border of the highlighting area
    gfx.setColor(color.darker());
    gfx.setStroke(DEFAULT_BORDER_STROKE);
    gfx.drawRoundRect(roundedX, roundedY, roundedWidth, roundedHeight, 10, 10);

    // Reset the setting changes
    gfx.setComposite(NORMAL_COMPOSITE);
    gfx.setStroke(NORMAL_STROKE);
  }

  private void regenerateLine(final String text, final Font font,
      final List<CStyleRunData> textColorStyleRun) {
    m_text = Preconditions.checkNotNull(text, "Error: text argument can not be null");
    Preconditions.checkNotNull(textColorStyleRun,
        "Error: textColorStyleRun argument can not be null");

    m_atext = new AttributedString(text);

    if (!isEmpty()) {
      if (font != null) {
        m_atext.addAttribute(TextAttribute.FONT, font);
      }

      // After the line is created we can process the accumulated style information.
      for (final CStyleRunData data : textColorStyleRun) {
        final int position = data.getStart();

        final int realLength = calculateRealLength(position, data.getLength());

        validatePartialLineArguments(position, realLength);

        m_atext.addAttribute(TextAttribute.FOREGROUND, data.getColor(), position, position
            + realLength);

        if (data.getLineObject() != null) {
          m_lineObjects.add(data.getLineObject());
        }

        if (data.getObject() != null) {
          setObject(position, realLength, data.getObject());
        }
      }

      m_textLayout = new TextLayout(m_atext.getIterator(), m_fontContext);
    }

    if (font != null) {
      updateCharBounds(font);
    }
  }

  /**
   * Updates the information that is used to calculate the line bounds.
   * 
   * @param font The font which is taken to calculate the bounds.
   */
  private void updateCharBounds(final Font font) {
    m_charWidth =
        font.getStringBounds(m_text, m_fontContext).getWidth()
            / getTextLayout().getCharacterCount();
    m_charHeight = font.getLineMetrics(m_text, m_fontContext).getHeight();
  }

  /**
   * Takes the position and length arguments passed to partial modification functions and checks
   * them for validity. If one or both of the arguments are invalid, an
   * {@link IllegalArgumentException} is thrown.
   * 
   * @param position Position argument to be checked.
   * @param length Length argument to be checked.
   */
  private void validatePartialLineArguments(final int position, final int length) {
    Preconditions.checkArgument(
        (position >= 0) && (position < m_text.length()),
        "Error: Position argument is out of bounds (Position: %s, Length: %s/%s)",
        position,
        length,
        m_text.length());
    Preconditions.checkArgument(
        (length > 0) || ((position + length) <= m_text.length()),
        "Error: Length argument is out of bounds (Position: %s, Length: %s)",
        position,
        length);
  }

  /**
   * Removes all highlighting information of the given level.
   * 
   * @param level The highlighting level to clear.
   */
  public synchronized boolean clearHighlighting(final int level) {
    if (m_highlighting.isEmpty()) {
      return false;
    }

    for (final CHighlighting highlighting : new ArrayList<CHighlighting>(m_highlighting)) {
      if (highlighting.getLevel() == level) {
        m_highlighting.remove(highlighting);
        return true;
      }
    }

    return false;
  }

  /**
   * Draws the line onto a graphics context.
   * 
   * @param gfx The graphics context to draw on.
   * @param x The x coordinate where the line is placed.
   * @param y The y coordinate where the line is placed.
   */
  public synchronized void draw(final Graphics2D gfx, final float x, final float y) {
    if (!isEmpty()) {
      m_textLayout.draw(gfx, x, y);

      for (final CHighlighting highlighting : new ArrayList<>(m_highlighting)) {
        final double bpX = (x + highlighting.getStart()) - 2.;
        final double bpY = (y - m_charHeight) + 4;
        final double bpW = highlighting.getEnd() + 4;
        final double bpH = m_charHeight - 1;

        drawHighlighting(gfx, bpX, bpY, bpW, bpH, highlighting.getColor());
      }
    }
  }

  public Color getBackgroundColor() {
    return m_backgroundColor;
  }

  public List<CStyleRunData> getBackgroundStyleRunData(final int start, final int end) {
    Preconditions.checkState((start >= 0) && (start <= end) && (start < m_text.length()),
        "Illegal start value.");
    Preconditions.checkState((end >= 0) && (end >= start) && (end < m_text.length()),
        "Illegal end value.");

    final List<CStyleRunData> styleRun = new ArrayList<CStyleRunData>();

    final AttributedCharacterIterator iterator = m_atext.getIterator();
    iterator.setIndex(start);

    Color lastColor = null;
    int attributeStart = start;

    for (int i = start; i <= end; ++i) {
      final Color color = (Color) iterator.getAttribute(TextAttribute.BACKGROUND);

      if (((color != null) && !color.equals(lastColor))
          || ((lastColor != null) && !lastColor.equals(color))) {
        if (lastColor != null) {
          final CStyleRunData data =
              new CStyleRunData(attributeStart, (i - attributeStart) + 1, lastColor);
          styleRun.add(data);
        }

        lastColor = color;
        attributeStart = i;
      }

      iterator.next();
    }

    final CStyleRunData data =
        new CStyleRunData(attributeStart, (end - attributeStart) + 1, lastColor);
    styleRun.add(data);

    return styleRun;
  }

  /**
   * Returns the bounds of the line.
   * 
   * @return The bounds of the line.
   */
  public Rectangle2D getBounds() {
    if (isEmpty()) {
      final AttributedString dummyString = new AttributedString(" ");
      final TextLayout dummyLayout = new TextLayout(dummyString.getIterator(), m_fontContext);

      return dummyLayout.getBounds();
    }

    return new Rectangle2D.Double(0, 0, m_charWidth * m_text.length(), m_charHeight);
  }

  public double getCharWidth() {
    return m_charWidth;
  }

  /**
   * Returns the {@link IZyEditableObject} at the position given as argument or the
   * {@link IZyEditableObject} at the position + 1 if at position there was no object.
   * 
   * @param xPos to position to look for the {@link IZyEditableObject} at.
   * @return The {@link IZyEditableObject} if found else null.
   */
  public IZyEditableObject getLineFragmentObjectAt(final int xPos) {
    for (final IZyEditableObject object : m_lineObjects) {
      if ((xPos >= object.getStart()) && (xPos < (object.getStart() + object.getLength()))) {
        return object;
      }
    }

    // if caret is directly behind an editable object
    for (final IZyEditableObject object : m_lineObjects) {
      if ((xPos >= object.getStart()) && ((xPos - 1) < (object.getStart() + object.getLength()))) {
        return object;
      }
    }

    return null;
  }

  public List<IZyEditableObject> getLineFragmentObjectList() {
    return m_lineObjects;
  }

  public IZyEditableObject getLineObject() {
    return m_model;
  }

  public Object getObject(final int position) {
    final ObjectWrapper wrapper = getObjectWrapper(position);
    return wrapper == null ? null : wrapper.getObject();
  }

  public ObjectWrapper getObjectWrapper(final int position) {
    for (final ObjectWrapper wrapper : m_objects) {
      if ((position >= wrapper.getStart())
          && (position < (wrapper.getStart() + wrapper.getLength()))) {
        return wrapper;
      }
    }

    return null;
  }

  /**
   * Returns the text of the line.
   * 
   * @return The text of the line.
   */
  public String getText() {
    return m_text;
  }

  public String getText(final IZyEditableObject editabeObject) {
    Preconditions.checkNotNull(editabeObject, "Error: editabeObject argument can not be null");

    final int start = editabeObject.getStart();
    int end = editabeObject.getEnd();

    if (start >= m_text.length()) {
      return "";
    }

    if (end >= m_text.length()) {
      end = m_text.length();
    }

    return m_text.substring(start, end);
  }

  /**
   * Returns the text layout of the line
   * 
   * @return The text layout of the line
   */
  public TextLayout getTextLayout() {
    if (isEmpty()) {
      final AttributedString dummyString = new AttributedString("+");

      return new TextLayout(dummyString.getIterator(), m_fontContext);
    } else {
      return new TextLayout(m_atext.getIterator(), m_fontContext);
    }
  }

  public boolean hasHighlighting(final int level) {
    if (isEmpty()) {
      return false;
    }

    if (m_highlighting.isEmpty()) {
      return false;
    }

    for (final CHighlighting highlighting : new ArrayList<CHighlighting>(m_highlighting)) {
      if (highlighting.getLevel() == level) {
        return true;
      }
    }

    return false;
  }

  public boolean isEditable(final int position) {
    return getLineFragmentObjectAt(position) != null;
  }

  public boolean isEmpty() {
    return "".equals(m_text) || (m_text == null);
  }

  /**
   * Changes the background color of the line.
   * 
   * @param color The new background color.
   */
  public void setBackgroundColor(final Color color) {
    m_backgroundColor = Preconditions.checkNotNull(color, "Error: Color argument can't be null");

    if (!isEmpty()) {
      m_atext.addAttribute(TextAttribute.BACKGROUND, color);

      m_textLayout = new TextLayout(m_atext.getIterator(), m_fontContext);
    }
  }

  /**
   * Changes the background color of a part of the line.
   * 
   * @param position The index of the first character that gets the new background color.
   * @param length Number of characters that get the new background color.
   * @param color The new background color.
   */
  public void setBackgroundColor(final int position, final int length, final Color color) {
    if (!isEmpty()) {
      final int realLength = calculateRealLength(position, length);

      validatePartialLineArguments(position, realLength);

      // needs to be tested: if color = null, no text background is set? Thats what i expect!
      m_atext.addAttribute(TextAttribute.BACKGROUND, color, position, position + realLength);

      m_textLayout = new TextLayout(m_atext.getIterator(), m_fontContext);
    }
  }

  /**
   * Changes the font that is used to display the whole line.
   * 
   * Note that the font must be a monospaced font.
   * 
   * @param font The new font to be used.
   */
  public void setFont(final Font font) {
    Preconditions.checkNotNull(font, "Error: Font argument can't be null");

    if (!isEmpty()) {
      m_atext.addAttribute(TextAttribute.FONT, font);

      m_textLayout = new TextLayout(m_atext.getIterator(), m_fontContext);

      updateCharBounds(font);
    }
  }

  /**
   * Changes the font that is used to display part of the line.
   * 
   * Note that the font must be a monospaced font.
   * 
   * @param position The index of the first character that gets the new font.
   * @param length Number of characters that get the new font.
   * @param font The new font to be used.
   */
  public void setFont(final int position, final int length, final Font font) {
    Preconditions.checkNotNull(font, "Error: Font argument can't be null");

    if (!isEmpty()) {
      final int realLength = calculateRealLength(position, length);

      validatePartialLineArguments(position, realLength);

      Preconditions.checkNotNull(font, "Error: Font argument can't be null");

      m_atext.addAttribute(TextAttribute.FONT, font, position, position + realLength);

      m_textLayout = new TextLayout(m_atext.getIterator(), m_fontContext);

      updateCharBounds(font);
    }
  }

  /**
   * Adds a new highlighting setting to the line.
   * 
   * @param level The highlighting level.
   * @param color The highlighting color.
   */
  public synchronized boolean setHighlighting(final int level, final Color color) {
    Preconditions.checkNotNull(color, "Error: Color argument can't be null");

    if (isEmpty()) {
      return false;
    }

    for (final CHighlighting highlighting : new ArrayList<CHighlighting>(m_highlighting)) {
      if (highlighting.getLevel() == level) {
        if (highlighting.getColor().equals(color)) {
          return false;
        } else {
          m_highlighting.remove(highlighting);
          break;
        }
      }
    }

    m_highlighting.add(new CHighlighting(level, 0, m_text.length() * m_charWidth, color));

    Collections.sort(m_highlighting);

    return true;
  }

  /**
   * Changes the highlighting for a part of the line.
   * 
   * @param position The index of the first character that is highlighted.
   * @param length Number of characters that are highlighted.
   * @param level The highlighting level.
   * @param color The highlighting color.
   */
  public synchronized void setHighlighting(final int position, final int length, final int level,
      final Color color) {
    Preconditions.checkNotNull(color, "Error: color argument can not be null");

    if (!isEmpty()) {
      final int realLength = calculateRealLength(position, length);

      validatePartialLineArguments(position, realLength);

      if (hasHighlighting(level)) {
        clearHighlighting(level);
      }

      m_highlighting.add(new CHighlighting(level, position * m_charWidth, realLength * m_charWidth,
          color));

      Collections.sort(m_highlighting);
    }
  }

  /**
   * Adds a newly generated ObjectWrapper Object to the list of objects stored in the class.
   * 
   * @param start the start of the objects string.
   * @param length the end of the objects string.
   * @param object the object itself.
   */
  public void setObject(final int start, final int length, final Object object) {
    Preconditions.checkNotNull(object, "Error: object argument can not be null");
    m_objects.add(new ObjectWrapper(start, length, object));
  }

  /**
   * Changes the text color of the line.
   * 
   * @param color The new text color.
   */
  public void setTextColor(final Color color) {
    Preconditions.checkNotNull(color, "Error: Color argument can't be null");

    if (!isEmpty()) {
      m_atext.addAttribute(TextAttribute.FOREGROUND, color);

      m_textLayout = new TextLayout(m_atext.getIterator(), m_fontContext);
    }
  }

  /**
   * Changes the text color of a part of the line.
   * 
   * @param position The index of the first character that gets the new font color.
   * @param length Number of characters that get the new font color.
   * @param color The new text color.
   */
  public void setTextColor(final int position, final int length, final Color color) {
    if (!isEmpty()) {
      final int realLength = calculateRealLength(position, length);

      validatePartialLineArguments(position, realLength);

      m_atext.addAttribute(TextAttribute.FOREGROUND, color, position, position + realLength);

      m_textLayout = new TextLayout(m_atext.getIterator(), m_fontContext);
    }
  }

  @Override
  public String toString() {
    return getText();
  }

  /**
   * Represents an object within a line with a defined start index and length. The start index
   * specifies the nth character in the string representing the line content.
   * 
   * @author jannewger@google.com (Jan Newger)
   * 
   */
  public static class ObjectWrapper {
    private final int start;
    private final int length;
    private final Object object;

    public ObjectWrapper(final int start, final int length, final Object object) {
      this.start = start;
      this.length = length;
      this.object = object;
    }

    public int getLength() {
      return length;
    }

    public Object getObject() {
      return object;
    }

    public int getStart() {
      return start;
    }
  }
}
