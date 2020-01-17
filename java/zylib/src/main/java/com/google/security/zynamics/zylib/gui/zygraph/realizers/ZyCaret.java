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

import com.google.common.base.Preconditions;

import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

public class ZyCaret {
  private final ZyLabelContent m_content;

  private int m_mouse_pressed_x = 0;
  private int m_mouse_pressed_y = 0;

  private int m_mouse_released_x = 0;
  private int m_mouse_released_y = 0;

  private int m_caret_endpos_x = 0;
  private int m_caret_startpos_x = 0;

  public ZyCaret(final ZyLabelContent content) {
    Preconditions.checkNotNull(content, "Error: Label content can't be null.");

    m_content = content;
  }

  private int calcCaretPosition(final int hitLine, final double x, final double y) {
    final ZyLineContent content = m_content.getLineContent(hitLine);

    final TextLayout textlayout = m_content.getLineContent(hitLine).getTextLayout();
    final TextHitInfo hitInfo = textlayout.hitTestChar((float) x, (float) y, content.getBounds());
    return hitInfo.getInsertionIndex();
  }

  private int calcHitLine(final double y) {
    final int linecount = m_content.getLineCount();
    final double lineHeight = m_content.getLineHeight();
    int hitline = 0;

    if (y < m_content.getLineContent(0).getBounds().getMinY()) {
      hitline = 0;
    } else if (y > (m_content.getLineContent(linecount - 1).getBounds().getMinY()
        + (linecount * lineHeight))) {
      hitline = linecount - 1;
    } else {
      for (int line = 0; line < linecount; ++line) {
        Rectangle2D bounds = m_content.getLineContent(line).getBounds();
        final double boundY = bounds.getY() + (line * lineHeight) + m_content.getPaddingTop();
        bounds =
            new Rectangle2D.Double(bounds.getX(), boundY, bounds.getWidth(), bounds.getHeight());

        if ((bounds.getY() < y) && ((bounds.getY() + lineHeight) > y)) {
          hitline = line;
          break;
        }
      }
    }
    return hitline;
  }

  private int calcHitPosition(final int caretPosition, final double x, final double y,
      final double zoomFactor) {
    boolean switched = false;

    int lp = m_mouse_pressed_y;
    int lr = m_mouse_released_y;

    if (lp > lr) {
      lp = m_mouse_released_y;
      lr = m_mouse_pressed_y;

      switched = true;
    }

    final int linecount = m_content.getLineCount();
    final double height = (float) m_content.getLineHeight();

    int maxIndex = caretPosition;
    for (int line = lp; line <= lr; line++) {
      double deltaY = 0;
      if (switched) {
        deltaY = height * zoomFactor * line;
      } else {
        deltaY = -(height * zoomFactor * (linecount - line));
      }

      final TextLayout textLayout = m_content.getLineContent(line).getTextLayout();
      final TextHitInfo hitInfo =
          textLayout.hitTestChar((float) x, (float) (y + deltaY), textLayout.getBounds());
      final int insertionIndex = hitInfo.getInsertionIndex();

      if ((caretPosition < insertionIndex) && (insertionIndex > maxIndex)) {
        maxIndex = insertionIndex;
      }
    }

    return maxIndex;
  }

  private int calcSelectedLinesMaxLength() {
    int lp = m_mouse_pressed_y;
    int lr = m_mouse_released_y;

    if (lp > lr) {
      lp = m_mouse_released_y;
      lr = m_mouse_pressed_y;
    }

    int length = 0;
    for (int y = lp; y <= lr; ++y) {
      length = Math.max(length, m_content.getLineContent(y).getText().length());
    }

    return length;
  }

  private int getLastLineXPos(final int lineYPos) {
    final ZyLineContent lineContent = m_content.getLineContent(lineYPos);

    final String text = lineContent.getText();

    if (text.endsWith("\n") || text.endsWith("\r")) {
      return text.length() - 1;
    }

    return text.length();
  }

  public int getCaretEndPos() {
    return m_caret_endpos_x;
  }

  public int getCaretStartPos() {
    return m_caret_startpos_x;
  }

  public int getMaxLineLength(int startY, int endY) {
    int max = 0;

    if (startY > endY) {
      final int temp = startY;
      startY = endY;
      endY = temp;
    }

    for (int lineIndex = startY; lineIndex <= endY; ++lineIndex) {
      final ZyLineContent lineContent = m_content.getLineContent(lineIndex);

      final int length = lineContent.getText().length();

      if (max < length) {
        max = length;
      }
    }

    return max;
  }

  public int getXmousePressed() {
    return m_mouse_pressed_x;
  }

  public int getXmouseReleased() {
    return m_mouse_released_x;
  }

  public int getYmousePressed() {
    return m_mouse_pressed_y;
  }

  public int getYmouseReleased() {
    return m_mouse_released_y;
  }

  public boolean isSelection() {
    final boolean isSingleLineSelection =
        (getXmousePressed() != getXmouseReleased()) && (getYmousePressed() == getYmouseReleased());

    if (isSingleLineSelection) {
      // If only a "\n" or a "\r" is selected, it is no selection and false is returned.

      final int maxIndex = m_content.getLastLineIndexOfModelAt(m_mouse_pressed_y);

      return maxIndex >= m_mouse_pressed_y;
    }

    return (getXmousePressed() != getXmouseReleased())
        || (getYmousePressed() != getYmouseReleased());
  }

  public void selectAll() {
    m_mouse_pressed_x = 0;
    m_mouse_pressed_y = 0;
    m_mouse_released_y = m_content.getLineCount() - 1;
    m_mouse_released_x = calcSelectedLinesMaxLength();
    m_caret_startpos_x = m_mouse_pressed_x;
    m_caret_endpos_x = m_mouse_released_x;
  }

  public void selectLine(final double labelParentY, final double mouseY) {
    final double y = mouseY - labelParentY - m_content.getPaddingTop();

    m_mouse_released_y = calcHitLine(y);
    m_mouse_pressed_y = m_mouse_released_y;
    m_mouse_released_x =
        m_content.getLineContent(m_mouse_pressed_y).getTextLayout().getCharacterCount();
    m_mouse_pressed_x = 0;
    m_caret_endpos_x = m_mouse_released_x;
    m_caret_startpos_x = m_mouse_pressed_x;

  }

  public void selectWord(final double labelParentX, final double labelParentY, final double mouseX,
      final double mouseY, final double zoomFactor) {
    final double x = mouseX - labelParentX - m_content.getPaddingLeft();
    final double y = mouseY - labelParentY - m_content.getPaddingTop();

    m_mouse_released_y = calcHitLine(y);
    m_mouse_pressed_y = m_mouse_released_y;
    m_mouse_released_x = calcHitPosition(m_caret_endpos_x, (float) x, (float) y, zoomFactor);

    final String s = m_content.getLineContent(m_mouse_released_y).getText();

    if (m_mouse_released_x > (s.length() - 1)) {
      return;
    }

    if (s.charAt(m_mouse_released_x) == ' ') {
      return;
    }

    m_caret_endpos_x = s.length();
    for (int charX = m_mouse_released_x; charX < m_caret_endpos_x; ++charX) {
      if (s.charAt(charX) == ' ') {
        m_caret_endpos_x = charX;
        break;
      }
    }

    m_caret_startpos_x = 0;
    for (int charX = m_mouse_released_x; charX >= 0; --charX) {
      if (s.charAt(charX) == ' ') {
        m_caret_startpos_x = charX + 1;
        break;
      }
    }

    m_mouse_pressed_x = m_caret_startpos_x;
    m_mouse_released_x = m_caret_endpos_x;
  }

  public void setCaret(int caretStartPos_X,
      int mousePressed_X,
      final int mousePressed_Y,
      int caretEndPos_X,
      final int mouseReleased_X,
      final int mouseReleased_Y) {
    final int lastPressedXPos = getLastLineXPos(mousePressed_Y);

    if (caretStartPos_X > lastPressedXPos) {
      caretStartPos_X = lastPressedXPos;
    }

    if (mousePressed_X > lastPressedXPos) {
      mousePressed_X = lastPressedXPos;
    }

    final int lastReleasedXPos = getLastLineXPos(mouseReleased_Y);

    if (caretEndPos_X > lastReleasedXPos) {
      caretEndPos_X = lastReleasedXPos;
    }

    m_caret_startpos_x = caretStartPos_X;
    m_mouse_pressed_x = mousePressed_X;
    m_mouse_pressed_y = mousePressed_Y;

    m_caret_endpos_x = caretEndPos_X;
    m_mouse_released_x = mouseReleased_X;
    m_mouse_released_y = mouseReleased_Y;
  }

  public void setCaretEnd(final double labelParentX, final double labelParentY, final double mouseX,
      final double mouseY, final double zoomFactor) {
    final double x = mouseX - labelParentX - m_content.getPaddingLeft();
    final double y = mouseY - labelParentY;

    m_mouse_released_y = calcHitLine(y);
    m_caret_endpos_x = calcCaretPosition(m_mouse_released_y, x, y);
    m_mouse_released_x = calcHitPosition(m_caret_endpos_x, x, y, zoomFactor);
  }

  public void setCaretEndPos(int endPos) {
    final int lastReleasedXPos = getLastLineXPos(m_mouse_released_y);

    if (endPos > lastReleasedXPos) {
      endPos = lastReleasedXPos;
    }

    m_caret_endpos_x = endPos;
  }

  public void setCaretStart(final double labelParentX, final double labelParentY,
      final double mouseX, final double mouseY, final double zoomFactor) {
    final double x = mouseX - labelParentX - m_content.getPaddingLeft();
    final double y = mouseY - labelParentY;

    m_mouse_pressed_y = calcHitLine(y);
    m_caret_startpos_x = calcCaretPosition(m_mouse_pressed_y, x, y);
    m_mouse_pressed_x = calcHitPosition(m_caret_startpos_x, x, y, zoomFactor);
  }

  public void setCaretStartPos(int startPos) {
    final int lastPressedXPos = getLastLineXPos(m_mouse_pressed_y);

    if (startPos > lastPressedXPos) {
      startPos = lastPressedXPos;
    }

    m_caret_startpos_x = startPos;
  }

  public void setXmousePressed(int mouseXPos) {
    final int lastPressedXPos = getLastLineXPos(m_mouse_pressed_y);

    if (mouseXPos > lastPressedXPos) {
      mouseXPos = lastPressedXPos;
    }

    m_mouse_pressed_x = mouseXPos;
  }

  public void setXmouseReleased(int mouseXPos) {
    final int lastReleasedXPos = getLastLineXPos(m_mouse_released_y);

    if (mouseXPos > lastReleasedXPos) {
      mouseXPos = lastReleasedXPos;
    }

    m_mouse_released_x = mouseXPos;
  }

  public void setYmousePressed(final int mouseYPos) {
    m_mouse_pressed_y = mouseYPos;
  }

  public void setYmouseReleased(final int mouseYPos) {
    m_mouse_released_y = mouseYPos;
  }
}
