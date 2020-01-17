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

package com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.general.ClipboardHelpers;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ECommentPlacement;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyEditableObject;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyCaret;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.UndoHistroy.CUndoManager;
import com.google.security.zynamics.zylib.strings.StringHelper;

public abstract class CAbstractKeyBehavior {
  private ZyLabelContent m_labelContent = null;

  private final CUndoManager m_undoManager;

  private KeyEvent m_event = null;

  private boolean m_alt;
  private boolean m_ctrl;
  private boolean m_shift;

  public CAbstractKeyBehavior(final CUndoManager undoManager) {
    m_undoManager = Preconditions.checkNotNull(undoManager, "Error: Undo manager can't be null.");
  }

  private ZyCaret getCaret() {
    return m_labelContent.getCaret();
  }

  private String getSingleLineCommentText(final ZyLineContent lineContent) {
    final StringBuilder commentText = new StringBuilder();

    final String lineText = lineContent.getText();

    boolean hasDelimiter = false;
    for (final IZyEditableObject lineObject : lineContent.getLineFragmentObjectList()) {
      if (lineObject.isCommentDelimiter()) {
        hasDelimiter = true;

        continue;
      }

      if (hasDelimiter) {
        final String subString = lineText.substring(lineObject.getStart(), lineObject.getEnd());

        commentText.append(subString);
      }
    }

    return commentText.toString();
  }

  private void setModifier(final KeyEvent event) {
    m_ctrl = false;
    m_shift = false;
    m_alt = false;

    if (event.getModifiersEx() == (InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK)) {
      m_ctrl = true;
      m_shift = true;
    } else if (event.getModifiersEx() == (InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK)) {
      m_ctrl = true;
      m_alt = true;
    } else if (event.getModifiersEx() == (InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK)) {
      m_alt = true;
      m_shift = true;
    } else if (event.isAltDown()) {
      m_alt = true;
    } else if (event.isControlDown()) {
      m_ctrl = true;
    } else if (event.isShiftDown()) {
      m_shift = true;
    }
  }

  private void updateLabelSize() {
    final ZyLabelContent labelContent = getLabelContent();

    final IZyEditableObject labelModel = labelContent.getModel();

    labelContent.getLineEditor().recreateLabelLines(labelContent, labelModel);
  }

  protected int correctMouseReleasedX(final int mouseReleased_X, final int mouseReleased_Y,
      final int mousePressed_Y) {
    final int lastReleasedXPos = getLastLineXPos(mouseReleased_Y);

    if ((mouseReleased_X > lastReleasedXPos) && (mouseReleased_Y == mousePressed_Y)) {
      return lastReleasedXPos;
    }

    return mouseReleased_X;
  }

  protected void deleteSelection() {
    if (isSelection() && isDeleteableSelection()) {
      int mouseStartX = Math.min(getCaretMousePressedX(), getCaretMouseReleasedX());
      final int mouseEndX = Math.max(getCaretMousePressedX(), getCaretMouseReleasedX());

      int mouseStartY = Math.min(getCaretMousePressedY(), getCaretMouseReleasedY());
      final int mouseEndY = Math.max(getCaretMousePressedY(), getCaretMouseReleasedY());

      int caretStartX = Math.min(getCaretStartPosX(), getCaretEndPosX());

      final int firstModelLine = m_labelContent.getFirstLineIndexOfModelAt(mouseStartY);
      final int noneCommentLine = m_labelContent.getNonPureCommentLineIndexOfModelAt(mouseStartY);
      final int lastModelLine = m_labelContent.getLastLineIndexOfModelAt(mouseStartY);

      int firstObjectLine = firstModelLine;
      int lastObjectLine = lastModelLine;

      if (noneCommentLine != -1) // It's is not a label comment.
      {
        if (mouseStartY < noneCommentLine) // It's a above line comment.
        {
          lastObjectLine = noneCommentLine - 1;
        } else
        // Its' a behind line comment.
        {
          firstObjectLine = noneCommentLine;
        }
      }

      StringBuilder changedText = new StringBuilder();

      ZyLineContent lineContent = getLineContent(mouseStartY);
      IZyEditableObject editObject = lineContent.getLineFragmentObjectAt(mouseStartX);

      if (editObject == null) {
        for (int lineIndex = firstObjectLine; lineIndex <= lastObjectLine; ++lineIndex) {
          final ZyLineContent curLineContent = getLineContent(lineIndex);
          editObject = curLineContent.getLineFragmentObjectAt(mouseStartX);

          if (editObject != null) {
            break;
          }
        }
      }

      if ((firstObjectLine == lastObjectLine) && !isComment(caretStartX, mouseStartY)) {
        // It's a single line editable object, but not a comment.

        if (editObject.isCommentDelimiter()) {
          mouseStartX = editObject.getEnd();
          caretStartX = editObject.getEnd();
          editObject = lineContent.getLineFragmentObjectAt(caretStartX);
        }

        final String text = lineContent.getText();

        changedText =
            new StringBuilder(String.format("%s%s",
                text.substring(editObject.getStart(), mouseStartX), text.substring(mouseEndX)));
      } else {
        // It's multi line editable object.

        for (int lineIndex = firstObjectLine; lineIndex <= lastObjectLine; ++lineIndex) {
          // Iterates over each line

          final ZyLineContent curLineContent = getLineContent(lineIndex);
          IZyEditableObject curEditObject = curLineContent.getLineFragmentObjectAt(mouseStartX);

          if (curEditObject == null) {
            curEditObject = curLineContent.getLineFragmentObjectAt(getLastLineXPos(lineIndex));
          }

          int tempMouseStartX = mouseStartX;
          int tempMouseEndX = mouseEndX;

          if (curEditObject.isCommentDelimiter()) {
            curEditObject = curLineContent.getLineFragmentObjectAt(curEditObject.getEnd());
            tempMouseStartX = curEditObject.getStart();

            if (mouseEndX < tempMouseStartX) {
              tempMouseEndX = tempMouseStartX;
            }
          }

          final String text = curLineContent.getText();
          final int curEndX = Math.min(text.length(), tempMouseEndX);

          if ((lineIndex >= mouseStartY) && (lineIndex <= mouseEndY)
              && (text.length() > tempMouseStartX)) {
            final String afterDeletion =
                String.format("%s%s", text.substring(curEditObject.getStart(), tempMouseStartX),
                    text.substring(curEndX));

            if (!afterDeletion.equals("\n") && !afterDeletion.equals("\r")) {
              changedText.append(afterDeletion);
            }
          } else {
            changedText.append(curLineContent.getText(curEditObject));
          }
        }
      }

      if (changedText.toString().endsWith("\n")) {
        changedText = new StringBuilder(changedText.substring(0, changedText.length() - 1));
      }

      if (!changedText.toString().endsWith("\r")) {
        changedText.append("\r");
      }

      // Get new valid caret position.
      if ((editObject != null) && editObject.isCommentDelimiter()) {
        caretStartX = editObject.getEnd();
        mouseStartX = editObject.getEnd();
      }

      for (int lineIndex = firstObjectLine; lineIndex <= lastObjectLine; ++lineIndex) {
        lineContent = getLineContent(lineIndex);

        if ((lineContent.getText().length() >= mouseStartX) && (mouseStartY <= lineIndex)) {
          mouseStartY = lineIndex;
          caretStartX = mouseStartX;

          break;
        }
      }

      // Update object, recreate lines and set caret.
      if (editObject != null) {
        editObject.update(changedText.toString());
        getLabelContent().getLineEditor().recreateLabelLines(getLabelContent(),
            editObject.getPersistentModel());
      }
      setCaret(caretStartX, mouseStartX, mouseStartY, caretStartX, mouseStartX, mouseStartY);
    }
  }

  protected int getCaretEndPosX() {
    return getCaret().getCaretEndPos();
  }

  protected int getCaretMousePressedX() {
    return getCaret().getXmousePressed();
  }

  protected int getCaretMousePressedY() {
    return getCaret().getYmousePressed();
  }

  protected int getCaretMouseReleasedX() {
    return getCaret().getXmouseReleased();
  }

  protected int getCaretMouseReleasedY() {
    return getCaret().getYmouseReleased();
  }

  protected int getCaretStartPosX() {
    return getCaret().getCaretStartPos();
  }

  protected KeyEvent getEvent() {
    return m_event;
  }

  protected ZyLabelContent getLabelContent() {
    return m_labelContent;
  }

  protected int getLastLineXPos(final int lineYPos) {
    final ZyLineContent lineContent = getLineContent(lineYPos);

    final String text = lineContent.getText();

    if (text.endsWith("\n") || text.endsWith("\r")) {
      return text.length() - 1;
    }

    return text.length();
  }

  protected ZyLineContent getLineContent(final int line) {
    return m_labelContent.getLineContent(line);
  }

  protected int getMaxLineLength(final int startYIndex, final int endYIndex) {
    return getCaret().getMaxLineLength(startYIndex, endYIndex);
  }

  protected String getMultilineComment(final int lineYPos, final String changedLine) {
    final StringBuilder commentText = new StringBuilder();

    final int nonCommentLine = m_labelContent.getNonPureCommentLineIndexOfModelAt(lineYPos);
    final int firstModelLine = m_labelContent.getFirstLineIndexOfModelAt(lineYPos);
    final int lastModelLine = m_labelContent.getLastLineIndexOfModelAt(lineYPos);

    int startIndex = isAboveLineComment(lineYPos) ? firstModelLine : nonCommentLine;
    int endIndex = isAboveLineComment(lineYPos) ? nonCommentLine - 1 : lastModelLine;

    if (nonCommentLine == -1) {
      startIndex = firstModelLine;
      endIndex = lastModelLine;
    }

    for (int index = startIndex; index <= endIndex; ++index) {
      if (index == lineYPos) {
        commentText.append(changedLine);
      } else {
        final ZyLineContent curLineContent = m_labelContent.getLineContent(index);

        final String lineText = getSingleLineCommentText(curLineContent);

        commentText.append(lineText);
      }
    }

    return commentText.toString().equals("") ? changedLine : commentText.toString();
  }

  protected String getMultiLineComment(final int lineYPos) {
    final StringBuilder commentText = new StringBuilder();

    final int nonCommentLine = m_labelContent.getNonPureCommentLineIndexOfModelAt(lineYPos);
    final int firstModelLine = m_labelContent.getFirstLineIndexOfModelAt(lineYPos);
    final int lastModelLine = m_labelContent.getLastLineIndexOfModelAt(lineYPos);

    int startIndex = isAboveLineComment(lineYPos) ? firstModelLine : nonCommentLine;
    int endIndex = isAboveLineComment(lineYPos) ? nonCommentLine - 1 : lastModelLine;

    if (nonCommentLine < 0) {
      startIndex = firstModelLine;
      endIndex = lastModelLine;
    }

    for (int index = startIndex; index <= endIndex; ++index) {
      final ZyLineContent curLineContent = m_labelContent.getLineContent(index);

      final String lineText = getSingleLineCommentText(curLineContent);

      commentText.append(lineText);
    }

    return commentText.toString();
  }

  protected ZyLineContent getNextModelLineContent(final int lineYPos) {
    final ZyLineContent lineContent = m_labelContent.getLineContent(lineYPos);
    final IZyEditableObject lineObject = lineContent.getLineObject();

    for (int index = lineYPos + 1; index < m_labelContent.getLineCount(); ++index) {
      final ZyLineContent nextLineContent = m_labelContent.getLineContent(index);

      if (nextLineContent == null) {
        return null;
      }

      final IZyEditableObject nextLineObject = nextLineContent.getLineObject();

      if ((lineObject != nextLineObject) && !nextLineObject.isPlaceholder()) {
        return nextLineContent;
      }
    }

    return null;
  }

  protected int getNextModelLineIndex(final int lineYPos) {
    final ZyLineContent lineContent = getNextModelLineContent(lineYPos);

    for (int index = lineYPos + 1; index < m_labelContent.getLineCount(); ++index) {
      if ((lineContent != null) && !lineContent.getLineObject().isPlaceholder()) {
        return getLabelContent().getLineIndex(lineContent);
      }
    }

    return -1;
  }

  protected String getSelectedText() {
    if (!isSelection()) {
      return "";
    }

    final int yStart = Math.min(getCaretMousePressedY(), getCaretMouseReleasedY());
    final int yEnd = Math.max(getCaretMousePressedY(), getCaretMouseReleasedY());

    final int xStart = Math.min(getCaretMousePressedX(), getCaretMouseReleasedX());
    final int xEnd = Math.max(getCaretMousePressedX(), getCaretMouseReleasedX());

    final StringBuilder clipboardText = new StringBuilder();

    for (int lineIndex = yStart; lineIndex <= yEnd; ++lineIndex) {
      final ZyLineContent lineContent = getLineContent(lineIndex);

      final int lineLength = lineContent.getText().length();

      if (xStart < lineLength) {
        int xTempEnd = xEnd;

        if (xEnd > lineLength) {
          xTempEnd = lineLength;
        }

        final String lineText = lineContent.getText();

        String fragment = lineText.substring(xStart, xTempEnd);

        if (!fragment.endsWith("\n") && (lineIndex != yEnd)) {
          fragment += "\n";
        }

        if (fragment.endsWith("\r") || ((lineIndex == yEnd) && fragment.endsWith("\n"))) {
          fragment = fragment.substring(0, fragment.length() - 1);
        }

        clipboardText.append(fragment);

      } else if (lineIndex != yEnd) {
        clipboardText.append("\n");
      }
    }

    return clipboardText.toString();
  }

  protected abstract void initUndoHistory();

  protected boolean isAboveLineComment(final int lineYPos) {
    final int nonCommentLineYPos = getLabelContent().getNonPureCommentLineIndexOfModelAt(lineYPos);

    if (nonCommentLineYPos < 0) {
      return false;
    }

    if (nonCommentLineYPos <= lineYPos) {
      return false;
    }

    return true;
  }

  protected boolean isAltPressed() {
    return m_alt;
  }

  protected boolean isBehindLineComment(final int lineXPos, final int lineYPos) {
    final int nonCommentLineYPos = getLabelContent().getNonPureCommentLineIndexOfModelAt(lineYPos);

    if (nonCommentLineYPos < 0) {
      return false;
    }

    if (nonCommentLineYPos < lineYPos) {
      return true;
    }

    if (nonCommentLineYPos == lineYPos) {
      final ZyLineContent lineContent = getLabelContent().getLineContent(lineYPos);

      if (lineContent == null) {
        return false;
      }

      for (final IZyEditableObject obj : lineContent.getLineFragmentObjectList()) {
        if (obj.isCommentDelimiter()) {
          return obj.getStart() <= lineXPos;
        }
      }
    }

    return false;
  }

  protected boolean isComment(final int lineXPos, final int lineYPos) {
    final ZyLineContent lineContent = getLabelContent().getLineContent(lineYPos);

    if (lineContent != null) {
      boolean commentDelimiterFound = false;

      for (final IZyEditableObject obj : lineContent.getLineFragmentObjectList()) {
        if (obj.isCommentDelimiter()) {
          commentDelimiterFound = true;
        }

        if ((lineXPos >= obj.getStart()) && commentDelimiterFound) {
          return true;
        }
      }
    }

    return false;
  }

  protected boolean isCtrlPressed() {
    return m_ctrl;
  }

  protected boolean isDeleteableSelection() {
    final int mouseStartY = Math.min(getCaretMousePressedY(), getCaretMouseReleasedY());
    final int mouseEndY = Math.max(getCaretMousePressedY(), getCaretMouseReleasedY());

    final int caretStartX = Math.min(getCaretStartPosX(), getCaretEndPosX());
    final int caretEndX = Math.max(getCaretStartPosX(), getCaretEndPosX());

    final ZyLineContent firstLineContent = getLineContent(mouseStartY);
    final ZyLineContent lastLineContent = getLineContent(mouseEndY);

    final IZyEditableObject firstEditObject = firstLineContent.getLineFragmentObjectAt(caretStartX);

    final IZyEditableObject lastEditObject = firstLineContent.getLineFragmentObjectAt(caretEndX);

    if (firstLineContent.getLineObject() != lastLineContent.getLineObject()) {
      return false;
    }

    if (isComment(caretStartX, mouseStartY)) {
      if (mouseEndY > mouseStartY) {
        final int noneCommentLine = m_labelContent.getNonPureCommentLineIndexOfModelAt(mouseStartY);

        if (noneCommentLine != -1) {
          if ((mouseStartY < noneCommentLine) && (mouseEndY >= noneCommentLine)) {
            return false;
          } else if ((mouseStartY >= noneCommentLine) && (mouseEndY < noneCommentLine)) {
            return false;
          }
        }
      }
    } else {
      if ((firstEditObject != lastEditObject)
          || ((firstEditObject == null) && (lastEditObject == null))) {
        return false;
      }
    }

    return true;
  }

  protected boolean isEditable(final int lineXPos, final int lineYPos) {
    final ZyLineContent lineContent = getLabelContent().getLineContent(lineYPos);

    return lineContent.isEditable(lineXPos);
  }

  protected boolean isLabelComment(final int lineYPos) {
    return getLabelContent().getNonPureCommentLineIndexOfModelAt(lineYPos) == -1;
  }

  protected boolean isSelection() {
    return getCaret().isSelection();
  }

  protected boolean isShiftPressed() {
    return m_shift;
  }

  protected Point pasteClipboardText() {
    int caretX = getCaretEndPosX();
    int caretY = getCaretMouseReleasedY();

    boolean isNewComment = false;

    final ZyLineContent lineContent = getLineContent(caretY);
    IZyEditableObject editObject = lineContent.getLineFragmentObjectAt(caretX);

    if ((editObject == null) && (caretX == lineContent.getText().length())) {
      final int nonModelLineY = getLabelContent().getNonPureCommentLineIndexOfModelAt(caretY);

      if ((nonModelLineY != -1) && (nonModelLineY == caretY)) {
        editObject = lineContent.getLineObject();

        isNewComment = true;
      }
    }

    if (editObject != null) {
      String insertText = ClipboardHelpers.getClipboardString();

      if (editObject.isCommentDelimiter()) {
        caretX = editObject.getEnd();

        editObject = lineContent.getLineFragmentObjectAt(caretX);

        if (editObject == null) {
          return null;
        }
      }

      if (isComment(caretX, caretY)) {
        // Insert into existing comment

        final int insertedLineCount = StringHelper.count(insertText, '\n');
        final int lastLineBreak = insertText.lastIndexOf("\n");

        final int textCursor = caretX - editObject.getStart();
        final String lineText =
            lineContent.getText().substring(editObject.getStart(), editObject.getEnd());

        String changedText =
            String.format("%s%s%s", lineText.substring(0, textCursor), insertText,
                lineText.substring(textCursor));

        changedText = getMultilineComment(caretY, changedText);

        editObject.update(changedText);

        getLabelContent().getLineEditor().recreateLabelLines(getLabelContent(),
            editObject.getPersistentModel());

        caretX += insertText.length() - lastLineBreak - 1;
        caretY += insertedLineCount;
      } else if (isNewComment) {
        // Create new comment

        editObject.updateComment(insertText, ECommentPlacement.BEHIND_LINE);

        if (!isLabelComment(caretY)) {
          getLabelContent().getLineEditor().recreateLabelLines(getLabelContent(),
              editObject.getPersistentModel());

          final int insertedLineCount = StringHelper.count(insertText, '\n');

          caretY += insertedLineCount;

          final ZyLineContent lastInsertedLineContent = getLineContent(caretY);

          caretX += lastInsertedLineContent.getText().length() - 1;
        }
      } else {
        // Insert into non comment editable object

        insertText = insertText.replace("\n", "");
        insertText = insertText.replace("\r", "");

        final int tempCaretX = caretX;

        caretX = caretX + insertText.length();

        final String lineText = lineContent.getText();

        String changedText = lineText.substring(editObject.getStart(), editObject.getEnd());

        final int insertXpos = tempCaretX - editObject.getStart();

        changedText =
            String.format("%s%s%s", lineText.substring(editObject.getStart(), insertXpos),
                insertText, lineText.substring(caretX, editObject.getEnd()));

        editObject.update(changedText);
      }
    }

    return new Point(caretX, caretY);
  }

  protected void redo() {
    m_undoManager.redo();
  }

  protected void setCaret(final int caretStartPos_X, final int mousePressed_X,
      final int mousePressed_Y, final int caretEndPos_X, final int mouseReleased_X,
      final int mouseReleased_Y) {
    getCaret().setCaret(caretStartPos_X, mousePressed_X, mousePressed_Y, caretEndPos_X,
        mouseReleased_X, mouseReleased_Y);
  }

  protected void udpateUndolist(final ZyLabelContent labelContent, final Object persistantModel,
      final IZyEditableObject editableObject, final String changedText,
      final boolean isAboveLineComment, final boolean isBehindLineComment,
      final boolean isLabelComment, final int caretStartX, final int caretMousePressedX,
      final int caretMousePressedY, final int caretEndX, final int caretMouseReleasedX,
      final int caretMouseReleasedY) {
    m_undoManager.addUndoState(labelContent, persistantModel, editableObject, changedText,
        isAboveLineComment, isBehindLineComment, isLabelComment, caretStartX, caretMousePressedX,
        caretMousePressedY, caretEndX, caretMouseReleasedX, caretMouseReleasedY);
  }

  protected void undo() {
    m_undoManager.undo();
  }

  protected abstract void updateCaret();

  protected abstract void updateClipboard();

  protected abstract void updateLabelContent();

  protected abstract void updateSelection();

  protected abstract void updateUndoHistory();

  public void keyPressed(final ZyLabelContent labelContent, final KeyEvent event) {
    m_labelContent = labelContent;

    m_event = event;

    setModifier(event);

    if (labelContent.isEditable()) {
      initUndoHistory();
    }

    updateClipboard();
    updateSelection();

    if (labelContent.isEditable()) {
      updateLabelContent();
    }

    updateCaret();

    if (labelContent.isEditable()) {
      updateUndoHistory();
      updateLabelSize();
    }

    m_alt = false;
    m_shift = false;
    m_ctrl = false;
  }
}
