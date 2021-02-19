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

package com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours;

import com.google.security.zynamics.zylib.gui.zygraph.realizers.ECommentPlacement;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyEditableObject;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.UndoHistroy.CUndoManager;

import java.util.List;


public class CBackspaceKeyBehavior extends CAbstractKeyBehavior {
  private int m_caretX;
  private int m_caretY;

  private boolean m_wasSelection = false;

  private boolean m_isBehindComment = false;
  private boolean m_isAboveComment = false;
  private boolean m_isLabelComment = false;

  private IZyEditableObject m_editableObject = null;
  private IZyEditableObject m_lineModel = null;

  private boolean m_wasEditablePosition = false;

  public CBackspaceKeyBehavior(final CUndoManager undoManager) {
    super(undoManager);
  }

  private void handleLineComments() {
    int x = getCaretEndPosX();
    final int y = getCaretMouseReleasedY();

    final ZyLineContent lineContent = getLineContent(y);

    if (m_editableObject == null) {
      // Caret is not within an editable object
      return;
    }

    final int firstModelLine = getLabelContent().getFirstLineIndexOfModelAt(y);
    final int nonCommentLine = getLabelContent().getNonPureCommentLineIndexOfModelAt(y);
    final int firstCommentLine =
        (nonCommentLine != -1) && m_isBehindComment ? nonCommentLine : firstModelLine;

    final String lineText = lineContent.getText();
    String changedText = "";

    // TODO: Remove debug code left-over
    // System.out.println("Backspace behavior befor: " + lineText.replace("\r",
    // "<cr>").replace("\n", "<br>"));

    if (!isCommentDelimiter(x, y)) {
      // Caret is within a comment line and there is no delimiter on the caret's left hand side.

      if ((lineText.length() > 0) && (lineText.length() == m_editableObject.getEnd())
          && (x == lineText.length())) {
        final char lastChar = lineText.charAt(lineText.length() - 1);

        if ((lastChar == '\n') || (lastChar == '\r')) {
          x = -1;
        }
      }

      changedText =
          String.format("%s%s", lineText.substring(m_editableObject.getStart(), x - 1),
              lineText.substring(x, m_editableObject.getEnd()));

      m_caretY = y;
      m_caretX = x - 1;

      changedText = getMultilineComment(m_caretY, changedText);
    } else {
      // Caret's left hand side or caret itself is within a comment delimiter

      final List<IZyEditableObject> editableObjectList = lineContent.getLineFragmentObjectList();
      m_editableObject = editableObjectList.get(editableObjectList.size() - 1);

      if (firstCommentLine == y) {
        // Caret is within the first comment line

        if (m_editableObject.getLength() == 1) {
          // First comment line consists of exactly one character. So only line break or line feed
          // can be left. Delete line.

          if (y > 0) {
            m_caretY = y - 1;
            final ZyLineContent prevLineContent = getLineContent(m_caretY);
            m_caretX = prevLineContent.getText().length();
          }

          changedText = getMultilineComment(y, changedText);
        } else {
          // First comment has more than one character. Do nothing.

          m_caretX = getCaretEndPosX();
          m_caretY = getCaretMouseReleasedY();

          return;
        }
      } else {
        // Caret is not within the first comment line. Remove this line delimiter and concatenate
        // this line with the previous if existing.

        final ZyLineContent prevLineContent = getLineContent(y - 1);
        final String prevLineText = prevLineContent.getText();

        if (!prevLineText.isEmpty() && !prevLineContent.getLineObject().isPlaceholder()) {
          final IZyEditableObject prevLineFragmentObject =
              prevLineContent.getLineFragmentObjectAt(prevLineText.length() - 1);
          changedText =
              prevLineText.substring(prevLineFragmentObject.getStart(),
                  prevLineFragmentObject.getEnd() - 1);

          m_caretY = y - 1;
          m_caretX = prevLineText.length() - 1;

          changedText = getMultilineComment(m_caretY, changedText);
        } else {
          m_caretY = y - 1;
          m_caretX = 0;
        }
      }
    }

    if (changedText.endsWith("\n")) {
      changedText = changedText.substring(0, changedText.length() - 1) + "\r";
    }

    // TODO: Remove debug code left-over
    // System.out.println("Backspace behavior after: " + changedText.replace("\r",
    // "<cr>").replace("\n", "<br>"));

    if (m_isAboveComment) {
      m_editableObject.updateComment(changedText, ECommentPlacement.ABOVE_LINE);
    } else if (m_isBehindComment) {
      m_editableObject.updateComment(changedText, ECommentPlacement.BEHIND_LINE);
    } else if (m_isLabelComment) {
      m_editableObject.update(changedText);
    }

    getLabelContent().getLineEditor().recreateLabelLines(getLabelContent(),
        m_editableObject.getPersistentModel());
  }

  private void handleTextFragment() {
    throw new RuntimeException("Not implemneted yet");
  }

  private boolean isCommentDelimiter(final int x, final int y) {
    final ZyLineContent lineContent = getLineContent(y);

    final IZyEditableObject fragment = lineContent.getLineFragmentObjectAt(x);
    IZyEditableObject leftNeighbourFragment =
        x > 0 ? leftNeighbourFragment = lineContent.getLineFragmentObjectAt(x - 1) : null;

    return

    ((fragment != null) && fragment.isCommentDelimiter())
        || ((leftNeighbourFragment != null) && leftNeighbourFragment.isCommentDelimiter());
  }

  @Override
  protected void initUndoHistory() {
    final int x = getCaretEndPosX();
    final int y = getCaretMouseReleasedY();

    final ZyLineContent lineContent = getLineContent(y);

    m_editableObject = lineContent.getLineFragmentObjectAt(x);
    m_lineModel = lineContent.getLineObject();

    if (m_editableObject != null) {
      // Caret is within an editable object.

      m_wasEditablePosition = true;

      String text =
          lineContent.getText().substring(m_editableObject.getStart(), m_editableObject.getEnd());

      m_isAboveComment = isAboveLineComment(y);
      m_isBehindComment = isBehindLineComment(x, y);
      m_isLabelComment = isLabelComment(y);

      if (isComment(x, y)) {
        text = getMultiLineComment(y);
      }

      udpateUndolist(getLabelContent(), m_lineModel.getPersistentModel(), m_editableObject, text,
          m_isAboveComment, m_isBehindComment, m_isLabelComment, getCaretStartPosX(),
          getCaretMousePressedX(), getCaretMousePressedY(), getCaretEndPosX(),
          getCaretMouseReleasedX(), getCaretMouseReleasedY());
    }
  }

  @Override
  protected void updateCaret() {
    setCaret(m_caretX, m_caretX, m_caretY, m_caretX, m_caretX, m_caretY);

    m_wasSelection = false;
  }

  @Override
  protected void updateClipboard() {
    // Do nothing
  }

  @Override
  protected void updateLabelContent() {
    m_caretX = getCaretEndPosX();
    m_caretY = getCaretMouseReleasedY();

    if (!m_wasSelection) {
      if (isComment(m_caretX, m_caretY)) {
        // Caret end was within a comment, when key was pressed

        handleLineComments();
      } else if (getLineContent(m_caretY).getLineFragmentObjectAt(m_caretX) != null) {
        // Caret end was within an non comment editable object, when key was pressed

        handleTextFragment();
      }
    }
  }

  @Override
  protected void updateSelection() {
    m_wasSelection = isSelection();

    deleteSelection();
  }

  @Override
  protected void updateUndoHistory() {
    if (!m_wasEditablePosition) {
      return;
    }

    m_wasEditablePosition = false;

    final int x = getCaretEndPosX();
    int y = getCaretMouseReleasedY();

    ZyLineContent lineContent = getLineContent(y);

    String text = "";

    if ((lineContent == null) || (lineContent.getLineObject() == null)
        || (m_lineModel.getPersistentModel() != lineContent.getLineObject().getPersistentModel())) {
      if (!m_isLabelComment) {
        int offset = 0;

        if ((y + 1) < getLabelContent().getLineCount()) {
          offset = 1;
          lineContent = getLineContent(y + offset);
        }

        if ((m_lineModel.getPersistentModel() != lineContent.getLineObject().getPersistentModel())
            && (y > 0)) {
          offset = -1;

          lineContent = getLineContent(y + offset);
        }

        y += offset;
      }
    } else if (isComment(x, y)) {
      text = getMultiLineComment(y);
    }

    udpateUndolist(getLabelContent(), m_lineModel.getPersistentModel(), m_lineModel, text,
        m_isAboveComment, m_isBehindComment, m_isLabelComment, getCaretStartPosX(),
        getCaretMousePressedX(), getCaretMousePressedY(), getCaretEndPosX(),
        getCaretMouseReleasedX(), getCaretMouseReleasedY());
  }
}
