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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;

public class ZyLabelContent implements Iterable<ZyLineContent> {
  private static final int FONTSIZE = 11;
  private static final int LINEHEIGHT = 15;

  private int m_leftPadding = 10;
  private int m_rightPadding = 10;
  private int m_topPadding = 10;
  private int m_bottomPadding = 10;

  private final IZyEditableObject m_model;
  private IZyLineEditor m_lineEditor = null;

  private final ZyCaret m_caret = new ZyCaret(this);

  private boolean m_selectable = true;

  private boolean m_editable = false;

  private boolean m_showCaret = false;

  private Color m_selectionColor = Color.WHITE;

  /**
   * List of line content objects that form the label content.
   */
  private final ArrayList<ZyLineContent> m_content = new ArrayList<ZyLineContent>();

  /**
   * Boolean which indicates if the sloppy paint modus is on or not.
   */
  private boolean m_sloppy = false;

  public ZyLabelContent(final IZyEditableObject nodeModel) {
    m_model = nodeModel;
  }

  public ZyLabelContent(final IZyEditableObject nodeModel, final boolean selectable,
      final boolean editable) {
    m_model = Preconditions.checkNotNull(nodeModel, "Error: Node model can't be null.");
    m_selectable = selectable;
    m_editable = editable;
  }

  private AffineTransform calcSelectionTransformationMatrix(final AffineTransform currentMatrix,
      final double xOffset, final double yOffset, final int linenr) {
    final double scaleX = currentMatrix.getScaleX();
    final double scaleY = currentMatrix.getScaleY();
    final double translateX = currentMatrix.getTranslateX() + (xOffset * scaleX);
    final double translateY =
        currentMatrix.getTranslateY() + ((yOffset + (linenr * getLineHeight())) * scaleY);
    return new AffineTransform(scaleX, 0, 0, scaleY, translateX, translateY);
  }

  /**
   * Adds another line add the end of the label content.
   * 
   * @param line The line to add.
   */
  public void addLineContent(final ZyLineContent line) {
    Preconditions.checkNotNull(line, "Internal Error: Line content can't be null");

    m_content.add(line);
  }

  public void draw(final Graphics2D gfx, final double xpos, final double ypos) {
    if (m_sloppy) {
      return;
    }

    if (!m_selectable || !m_showCaret) {
      final float x = (float) xpos + m_leftPadding;
      float y = (float) ypos + m_topPadding + FONTSIZE;

      for (final ZyLineContent line : getContent()) {
        line.draw(gfx, x, y);

        y += LINEHEIGHT;
      }

      return;
    }

    final AffineTransform affineTrans = gfx.getTransform();
    final Color color = gfx.getColor();

    final float x = (float) xpos + getPaddingLeft();
    final float y = (float) ypos + getPaddingTop() + getFontSize();

    final double lineheight = getLineHeight();

    gfx.setColor(Color.BLACK);

    for (int linenr = 0; linenr < getLineCount(); ++linenr) {
      final TextLayout textLayout = getLineContent(linenr).getTextLayout();

      if (!getLineContent(linenr).isEmpty()) {
        textLayout.draw(gfx, x, (float) (y + (linenr * lineheight)));
      }

      if (linenr == m_caret.getYmousePressed()) {
        gfx.setTransform(calcSelectionTransformationMatrix(affineTrans, x, y, linenr));

        if (getLineContent(linenr).isEmpty()) {
          m_caret.setCaretStartPos(0);
        } else if (m_caret.getCaretStartPos() >= textLayout.getCharacterCount()) {
          m_caret.setCaretStartPos(textLayout.getCharacterCount());
        }

        final Shape[] startCarets = textLayout.getCaretShapes(m_caret.getCaretStartPos());

        gfx.setColor(m_selectionColor.darker());
        gfx.draw(startCarets[0]);

        gfx.setTransform(affineTrans);
      }

      if (linenr == m_caret.getYmouseReleased()) {
        gfx.setTransform(calcSelectionTransformationMatrix(affineTrans, x, y, linenr));

        if (getLineContent(linenr).isEmpty()) {
          m_caret.setCaretEndPos(0);
        } else if (m_caret.getCaretEndPos() >= textLayout.getCharacterCount()) {
          m_caret.setCaretEndPos(textLayout.getCharacterCount());
        }

        final Shape[] endCarets = textLayout.getCaretShapes(m_caret.getCaretEndPos());

        gfx.setColor(m_selectionColor.darker().darker());
        gfx.draw(endCarets[0]);

        gfx.setTransform(affineTrans);
      }
    }

    if (m_caret.getXmousePressed() != m_caret.getXmouseReleased()) {
      int pl = m_caret.getYmousePressed();
      int rl = m_caret.getYmouseReleased();

      if (pl > rl) {
        pl = m_caret.getYmouseReleased();
        rl = m_caret.getYmousePressed();
      }

      int pp = m_caret.getXmousePressed();
      int rp = m_caret.getXmouseReleased();

      if (pp > rp) {
        pp = m_caret.getXmouseReleased();
        rp = m_caret.getXmousePressed();
      }

      gfx.setColor(m_selectionColor);
      gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

      for (int linenr = pl; linenr <= rl; ++linenr) {
        gfx.setTransform(calcSelectionTransformationMatrix(affineTrans, x, y, linenr));

        final TextLayout textLayout = getLineContent(linenr).getTextLayout();

        int rp2 = rp;
        if (pp > textLayout.getCharacterCount()) {
          continue;
        }

        if (getLineContent(linenr).isEmpty()) {
          rp2 = pp;
        } else if (rp > textLayout.getCharacterCount()) {
          rp2 = textLayout.getCharacterCount();
        }

        final Shape selection = textLayout.getLogicalHighlightShape(pp, rp2);
        gfx.fill(selection);
      }

      gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
      gfx.setTransform(affineTrans);
    }

    gfx.setColor(color);
    gfx.setTransform(affineTrans);
  }

  /**
   * Returns the bounds of the label content.
   * 
   * @return The bounds of the label content.
   */
  public Rectangle2D getBounds() {
    double maxWidth = 0;
    double height = m_topPadding + m_bottomPadding;

    for (final ZyLineContent content : getContent()) {
      if (content.getBounds().getWidth() > maxWidth) {
        maxWidth = content.getBounds().getWidth();
      }

      height += LINEHEIGHT;
    }
    return new Rectangle2D.Double(0, 0, maxWidth + m_rightPadding + m_leftPadding, height);
  }

  public ZyCaret getCaret() {
    return m_caret;
  }

  public List<ZyLineContent> getContent() {
    return new ArrayList<ZyLineContent>(m_content);
  }

  public int getFirstLineIndexOfModelAt(final int lineYPos) {
    final ZyLineContent lineContent = getLineContent(lineYPos);

    if (lineContent != null) {
      final IZyEditableObject lineModel = lineContent.getLineObject();

      int indexCopy = lineYPos;

      while (--indexCopy >= 0) {
        if (m_content.get(indexCopy).getLineObject() != lineModel) {
          return indexCopy + 1;
        }
      }
    }

    return lineYPos; // - 1
  }

  // TODO: Get rid of this function because the font size is not constant between lines
  public int getFontSize() {
    return FONTSIZE;
  }

  public int getLastLineIndexOfModelAt(final int lineYPos) {
    final ZyLineContent lineContent = getLineContent(lineYPos);

    if (lineContent != null) {
      final IZyEditableObject lineModel = lineContent.getLineObject();

      int indexCopy = lineYPos;

      while (++indexCopy < m_content.size()) {
        if (m_content.get(indexCopy).getLineObject() != lineModel) {
          return indexCopy - 1;
        }
      }

      return m_content.size() - 1;
    }

    return lineYPos;
  }

  /**
   * Returns the line content of the given line.
   * 
   * @param line The line index.
   * 
   * @return The line content of that line.
   */
  public ZyLineContent getLineContent(final int line) {
    return m_content.get(line);
  }

  /**
   * Returns the number of lines in the label content.
   * 
   * @return The number of lines in the label content.
   */
  public int getLineCount() {
    return m_content.size();
  }

  public IZyLineEditor getLineEditor() {
    return m_lineEditor;
  }

  // TODO: Get rid of this function because the font size is not constant between lines
  public double getLineHeight() {
    return LINEHEIGHT;
  }

  public int getLineIndex(final ZyLineContent line) {
    return m_content.indexOf(line);
  }

  public IZyEditableObject getModel() {
    return m_model;
  }

  public int getNonPureCommentLineIndexOfModelAt(final int lineYPos) {
    // e.g. if it's a basicblock it the instruction line index

    final ZyLineContent lineContent = getLineContent(lineYPos);

    if (lineContent != null) {
      final int firstModelLine = getFirstLineIndexOfModelAt(lineYPos);
      final int lastModelLine = getLastLineIndexOfModelAt(lineYPos);

      for (int index = firstModelLine; index <= lastModelLine; index++) {
        final ZyLineContent curLineContent = getLineContent(index);

        final List<IZyEditableObject> lineObjects = curLineContent.getLineFragmentObjectList();

        if ((curLineContent.getLineObject() != null)
            && curLineContent.getLineObject().isPlaceholder()) {
          continue;
        }

        if (lineObjects.size() == 0) {
          return index;
        }

        if (lineObjects.get(0).isCommentDelimiter() && (lineObjects.get(0).getStart() > 0)) {
          return index;
        }
      }
    }

    return -1;
  }

  /**
   * Returns the padding on the left side of the content.
   * 
   * @return The padding on the left side of the content.
   */
  public int getPaddingLeft() {
    return m_leftPadding;
  }

  /**
   * Returns the padding at the top side of the label content
   * 
   * @return The padding at the top side of the label content
   */
  public int getPaddingTop() {
    return m_topPadding;
  }

  public int getRightPadding() {
    return m_rightPadding;
  }

  public String getSelectedText() {
    if (!m_caret.isSelection()) {
      return "";
    }

    int lp = m_caret.getYmousePressed();
    int lr = m_caret.getYmouseReleased();

    if (lp > lr) {
      lp = m_caret.getYmouseReleased();
      lr = m_caret.getYmousePressed();
    }

    int pp = m_caret.getXmousePressed();
    int pr = m_caret.getXmouseReleased();

    if (pp > m_caret.getXmouseReleased()) {
      pp = m_caret.getXmouseReleased();
      pr = m_caret.getXmousePressed();
    }

    final StringBuilder selectedString = new StringBuilder();
    for (int y = lp; y <= lr; ++y) {
      final String line = getLineContent(y).getText();
      if ((line == null) || (line.length() < 1) || (line.length() < pp)) {
        selectedString.append("\n");
        continue;
      }

      if (line.length() < pr) {
        selectedString.append(line.substring(pp));
        if (!"\n".equals(line) && (y < lr)) {
          selectedString.append("\n");
        }
        continue;
      }

      selectedString.append(line.substring(pp, pr));
      if (!"\n".equals(line) && (y < lr)) {
        selectedString.append("\n");
      }

    }
    return selectedString.toString();
  }

  public void insertLine(final ZyLineContent insertLine, final int insertIndex) {
    final ArrayList<ZyLineContent> newContent = new ArrayList<ZyLineContent>();

    if (insertIndex >= m_content.size()) {
      m_content.add(insertLine);
    } else {
      int index = 0;

      for (final ZyLineContent line : getContent()) {
        if (index++ == insertIndex) {
          newContent.add(insertLine);
        }

        newContent.add(line);
      }

      m_content.clear();
      m_content.addAll(newContent);
    }
  }

  public boolean isEditable() {
    return m_editable;
  }

  public boolean isSelectable() {
    return m_selectable;
  }

  @Override
  public Iterator<ZyLineContent> iterator() {
    return m_content.iterator();
  }

  public void removeLine(final int removeIndex) {
    m_content.remove(removeIndex);
  }

  public void selectAll(final IZyNodeRealizer r) {
    m_caret.selectAll();
    r.repaint();
  }

  public void setEditable(final boolean editable) {
    if ((m_model == null) || (m_lineEditor == null)) {
      return;
    }

    m_editable = editable;

    if (m_editable) {
      m_selectable = true;
    }
  }

  public void setLineEditor(final IZyLineEditor creator) {
    m_lineEditor = creator;
  }

  public void setPadding(final int top, final int left, final int bottom, final int right) {
    m_leftPadding = left;
    m_rightPadding = right;
    m_topPadding = top;
    m_bottomPadding = bottom;
  }

  public void setPaddingLeft(final int padding) {
    m_leftPadding = padding;

  }

  public void setRightPadding(final int right) {
    m_rightPadding = right;
  }

  public void setSelectable(final boolean selectionMode) {
    if (m_model == null) {
      return;
    }

    m_selectable = selectionMode;
  }

  public void setSelectionColor(final Color c) {
    m_selectionColor = c;
  }

  public void setSloppy(final boolean isSloppy) {
    m_sloppy = isSloppy;
  }

  public void showCaret(final boolean show) {
    m_showCaret = show;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    for (final ZyLineContent content : m_content) {
      builder.append(content.toString());
      builder.append('\n');
    }
    return builder.toString();
  }

  public void updateContentSelectionColor(final Color fillColor, final boolean isSelected) {
    final Color nodeSelectionColor = fillColor.darker();

    if (isSelected) {
      setSelectionColor(nodeSelectionColor.darker());
    } else {
      setSelectionColor(nodeSelectionColor);
    }
  }
}
