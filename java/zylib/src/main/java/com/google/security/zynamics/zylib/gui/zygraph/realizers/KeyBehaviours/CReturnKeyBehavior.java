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

import com.google.security.zynamics.zylib.gui.zygraph.realizers.ECommentPlacement;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyEditableObject;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.UndoHistroy.CUndoManager;

public class CReturnKeyBehavior extends CAbstractKeyBehavior {
  private int m_caretY = -1;

  private boolean m_wasUneditableSelection;

  public CReturnKeyBehavior(final CUndoManager undoManager) {
    super(undoManager);
  }

  @Override
  protected void initUndoHistory() {
    int x = getCaretEndPosX();
    int y = getCaretMouseReleasedY();

    final ZyLineContent lineContent = getLineContent(y);

    if (isComment(x, y)) {
      // Caret end was within a comment, when return was pressed

      IZyEditableObject lineObject = lineContent.getLineFragmentObjectAt(x);

      String text = "";

      if (lineObject.isCommentDelimiter()) {
        x = lineObject.getEnd();

        lineObject = lineContent.getLineFragmentObjectAt(x);
      }

      text = getMultiLineComment(y);

      udpateUndolist(getLabelContent(), lineContent.getLineObject().getPersistentModel(),
          lineObject, text, isAboveLineComment(y), isBehindLineComment(x, y), isLabelComment(y),
          getCaretStartPosX(), getCaretMousePressedX(), getCaretMousePressedY(), getCaretEndPosX(),
          getCaretMouseReleasedX(), getCaretMouseReleasedY());
    } else {
      // Caret was not within a comment. Note: Line fragments are always single lined

      final ZyLineContent nextModelLineContent = getNextModelLineContent(y);

      y = getNextModelLineIndex(y);

      String text = "";

      boolean isAboveLineComment = false;
      boolean isBehindLineComment = false;
      boolean isLabelComment = false;

      IZyEditableObject editableObject =
          nextModelLineContent == null ? getLabelContent().getModel() : nextModelLineContent
              .getLineObject();

      if ((x == lineContent.getText().length()) && (getCaretMouseReleasedY() != 0)) {
        // Caret is at the end of a non-comment line. A new behind line comment will be created

        editableObject = lineContent.getLineObject();

        isBehindLineComment = true;
      } else if ((nextModelLineContent != null) && !isLabelComment(y)) {
        // There is a next model line, but it's not the label comment

        if (isComment(0, y)) {
          text = getMultiLineComment(y);
        }
        isAboveLineComment = true;
      } else if ((nextModelLineContent != null) && isLabelComment(y)) {
        // There is a next model line content and it's the label comment

        text += getMultiLineComment(y);
        isLabelComment = true;
      } else {
        // There is no next model line content and there is still no label comment appended

        isLabelComment = true;
      }

      if (editableObject == null) {
        return;
      }

      udpateUndolist(getLabelContent(), editableObject.getPersistentModel(), editableObject, text,
          isAboveLineComment, isBehindLineComment, isLabelComment, getCaretStartPosX(),
          getCaretMousePressedX(), getCaretMousePressedY(), getCaretEndPosX(),
          getCaretMouseReleasedX(), getCaretMouseReleasedY());
    }
  }

  @Override
  protected void updateCaret() {
    if ((m_caretY > -1) && isComment(getCaretEndPosX(), m_caretY)) {
      final ZyLineContent nextLineContent = getLineContent(m_caretY);
      final IZyEditableObject lineObject = nextLineContent.getLineFragmentObjectList().get(0);

      int x = 0;
      if (lineObject != null) {
        x = lineObject.getEnd();
      }

      setCaret(x, x, m_caretY, x, x, m_caretY);
    }
  }

  @Override
  protected void updateClipboard() {
    // do nothing
  }

  @Override
  protected void updateLabelContent() {
    if (m_wasUneditableSelection) {
      return;
    }

    int x = getCaretEndPosX();
    final int y = getCaretMouseReleasedY();

    m_caretY = y + 1;

    if (isComment(x, y)) {
      // Caret end was within a comment. Insert a new line into the comment.

      final ZyLineContent lineContent = getLineContent(y);
      IZyEditableObject lineObject = lineContent.getLineFragmentObjectAt(x);

      if (lineObject.isCommentDelimiter()) {
        x = lineObject.getEnd();

        lineObject = lineContent.getLineFragmentObjectAt(x);
      }

      int textCursor = x - lineObject.getStart();
      final String text =
          lineContent.getText().substring(lineObject.getStart(), lineObject.getEnd());

      if (text.endsWith("\r") && (textCursor == text.length())) {
        --textCursor;
      }

      String changedLine =
          String.format("%s%s%s", text.substring(0, textCursor), "\n", text.substring(textCursor));
      changedLine = getMultilineComment(y, changedLine);

      final IZyEditableObject editableObject = lineContent.getLineObject();

      if (editableObject == null) {
        return;
      }

      if (isAboveLineComment(y)) {
        editableObject.updateComment(changedLine, ECommentPlacement.ABOVE_LINE);
      } else if (isBehindLineComment(x, y)) {
        editableObject.updateComment(changedLine, ECommentPlacement.BEHIND_LINE);
      } else if (isLabelComment(y)) {
        editableObject.update(changedLine);
      }

      getLabelContent().getLineEditor().recreateLabelLines(getLabelContent(),
          editableObject.getPersistentModel());
    } else {
      // Caret was not within a comment. Create a new comment.

      final ZyLineContent lineContent = getLineContent(y);

      final ZyLineContent nextModelLineContent = getNextModelLineContent(y);
      m_caretY = getNextModelLineIndex(y);

      if ((x == lineContent.getText().length()) && (y != 0)) {
        // Caret is at the end of a non-comment line, but it is NOT the first line. A new behind
        // line comment will be created.

        m_caretY = y;

        final IZyEditableObject editableObject = lineContent.getLineObject();

        if (editableObject != null) {
          editableObject.updateComment("\r", ECommentPlacement.BEHIND_LINE);
          getLabelContent().getLineEditor().recreateLabelLines(getLabelContent(),
              editableObject.getPersistentModel());
        }
      } else if ((nextModelLineContent != null) && (nextModelLineContent.getLineObject() != null)
          && !isLabelComment(m_caretY)) {
        // There is a next model line, but it's not the label comment. Add a new comment line to the
        // front.

        String changedComment = "\r";

        if (isComment(0, m_caretY)) {
          // There is already a above line comment
          changedComment = "\n" + getMultiLineComment(m_caretY);
        }

        nextModelLineContent.getLineObject().updateComment(changedComment,
            ECommentPlacement.ABOVE_LINE);
        getLabelContent().getLineEditor().recreateLabelLines(getLabelContent(),
            nextModelLineContent.getLineObject().getPersistentModel());
      } else if ((nextModelLineContent != null) && isLabelComment(m_caretY)) {
        // There is a next model line content and it's the label comment. Add a new comment line to
        // the front.

        String changedComment = "\n";

        changedComment += getMultiLineComment(m_caretY);

        getLabelContent().getModel().update(changedComment);
        getLabelContent().getLineEditor().recreateLabelLines(getLabelContent(),
            getLabelContent().getModel().getPersistentModel());
      } else {
        // There is no next model line content and there is still no label comment appended. Create
        // a new label comment.

        // m_caretY = getLabelContent().getLineCount(); // line count is invalid if other sides
        // label comment has more lines than this one

        m_caretY = y + 1;

        getLabelContent().getModel().update("\r");
        getLabelContent().getLineEditor().recreateLabelLines(getLabelContent(),
            getLabelContent().getModel().getPersistentModel());
      }
    }
  }

  @Override
  protected void updateSelection() {
    m_wasUneditableSelection = !isDeleteableSelection() && isSelection();

    deleteSelection();
  }

  @Override
  protected void updateUndoHistory() {
    final int x = getCaretEndPosX();
    final int y = getCaretMouseReleasedY();

    final ZyLineContent lineContent = getLineContent(y);
    final IZyEditableObject editableObject = lineContent.getLineFragmentObjectAt(x);

    if (editableObject != null) {
      String text =
          lineContent.getText().substring(editableObject.getStart(), editableObject.getEnd());

      if (isComment(x, y)) {
        text = getMultiLineComment(y);
      }

      udpateUndolist(getLabelContent(), lineContent.getLineObject().getPersistentModel(),
          editableObject, text, isAboveLineComment(y), isBehindLineComment(x, y),
          isLabelComment(y), getCaretStartPosX(), getCaretMousePressedX(), getCaretMousePressedY(),
          getCaretEndPosX(), getCaretMouseReleasedX(), getCaretMouseReleasedY());
    }
  }
}
