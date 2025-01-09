// Copyright 2011-2024 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours;

import com.google.security.zynamics.zylib.gui.zygraph.realizers.ECommentPlacement;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyEditableObject;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.UndoHistory.CUndoManager;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent;

public class CCharKeyBehavior extends CAbstractKeyBehavior {
  private int m_caretX;

  private IZyEditableObject m_editableObject = null;

  private boolean m_isLabelComment = false;
  private boolean m_isAboveComment = false;
  private boolean m_isBehindComment = false;

  private boolean m_wasUneditableSelection;

  public CCharKeyBehavior(final CUndoManager undoManager) {
    super(undoManager);
  }

  @Override
  protected void initUndoHistory() {
    final int x = getCaretEndPosX();
    final int y = getCaretMouseReleasedY();

    final ZyLineContent lineContent = getLineContent(y);
    IZyEditableObject editableObject = lineContent.getLineFragmentObjectAt(x);

    m_isAboveComment = isAboveLineComment(y);
    m_isBehindComment = isBehindLineComment(x, y);
    m_isLabelComment = isLabelComment(y);

    boolean isNewBehindLineComment = false;
    if ((x == lineContent.getText().length()) && !isComment(x, y)) {
      // a new behind line comment will be created

      m_isBehindComment = true;

      isNewBehindLineComment = true;

      editableObject = lineContent.getLineObject();
    }

    m_editableObject = editableObject;

    String text = "";
    if (editableObject != null) {
      if (!isNewBehindLineComment) {
        text = lineContent.getText().substring(editableObject.getStart(), editableObject.getEnd());

        if (isComment(x, y)) {
          text = getMultiLineComment(y);
        }
      }

      updateUndolist(
          getLabelContent(),
          lineContent.getLineObject().getPersistentModel(),
          editableObject,
          text,
          m_isAboveComment,
          m_isBehindComment,
          m_isLabelComment,
          getCaretStartPosX(),
          getCaretMousePressedX(),
          getCaretMousePressedY(),
          getCaretEndPosX(),
          getCaretMouseReleasedX(),
          getCaretMouseReleasedY());
    }
  }

  @Override
  protected void updateCaret() {
    final int y = getCaretMouseReleasedY();

    setCaret(m_caretX, m_caretX, y, m_caretX, m_caretX, y);
  }

  @Override
  protected void updateClipboard() {
    // Do nothing
  }

  @Override
  protected void updateLabelContent() {
    if (m_wasUneditableSelection) {
      m_caretX = getCaretEndPosX();

      return;
    }

    final int x = getCaretEndPosX();
    final int y = getCaretMouseReleasedY();

    m_caretX = x;

    if (isComment(x, y)) {
      // Cursor is within an existing comment

      m_caretX += 1;

      final ZyLineContent lineContent = getLineContent(y);
      final IZyEditableObject editableObject = lineContent.getLineFragmentObjectAt(x);

      if ((editableObject == null) || editableObject.isCommentDelimiter()) {
        return;
      }

      final int textCursor = x - editableObject.getStart();
      final String text =
          lineContent.getText().substring(editableObject.getStart(), editableObject.getEnd());
      final String chr = String.valueOf(getEvent().getKeyChar());

      String changedText =
          String.format("%s%s%s", text.substring(0, textCursor), chr, text.substring(textCursor));

      if (isComment(x, y)) {
        changedText = getMultilineComment(y, changedText);
      }

      editableObject.update(changedText);

      getLabelContent()
          .getLineEditor()
          .recreateLabelLines(getLabelContent(), editableObject.getPersistentModel());
    } else if (isEditable(x, y)) {
      // Editable line fragment object but Not a comment

      throw new RuntimeException("Not implemented yet.");
    } else {
      // Underlying object is NOT editable

      final ZyLineContent lineContent = getLineContent(y);

      if ((x == lineContent.getText().length()) && (lineContent.getLineObject() != null)) {
        // Create new behind line comment

        lineContent
            .getLineObject()
            .updateComment(
                Character.toString(getEvent().getKeyChar()), ECommentPlacement.BEHIND_LINE);

        getLabelContent()
            .getLineEditor()
            .recreateLabelLines(
                getLabelContent(), lineContent.getLineObject().getPersistentModel());

        m_caretX = getLineContent(y).getText().length();
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
    if (m_editableObject != null) {
      // There was an editable object that has been changed

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

        updateUndolist(
            getLabelContent(),
            lineContent.getLineObject().getPersistentModel(),
            editableObject,
            text,
            m_isAboveComment,
            m_isBehindComment,
            m_isLabelComment,
            getCaretStartPosX(),
            getCaretMousePressedX(),
            getCaretMousePressedY(),
            getCaretEndPosX(),
            getCaretMouseReleasedX(),
            getCaretMouseReleasedY());
      }
    }
  }
}
