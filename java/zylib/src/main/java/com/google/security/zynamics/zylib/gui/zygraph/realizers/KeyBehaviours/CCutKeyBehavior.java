// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours;

import com.google.security.zynamics.zylib.general.ClipboardHelpers;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyEditableObject;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.UndoHistroy.CUndoManager;

public class CCutKeyBehavior extends CAbstractKeyBehavior {
  private IZyEditableObject m_editableObject;

  private boolean m_isAboveComment;
  private boolean m_isBehindComment;
  private boolean m_isLabelComment;

  public CCutKeyBehavior(final CUndoManager undoManager) {
    super(undoManager);
  }

  @Override
  protected void initUndoHistory() {
    final int x = getCaretEndPosX();
    final int y = getCaretMouseReleasedY();

    final ZyLineContent lineContent = getLineContent(y);

    final IZyEditableObject lineFragmentObject = lineContent.getLineFragmentObjectAt(x);

    m_editableObject = lineContent.getLineObject();

    if (lineFragmentObject != null) {
      String text =
          lineContent.getText().substring(lineFragmentObject.getStart(),
              lineFragmentObject.getEnd());

      m_isAboveComment = isAboveLineComment(y);
      m_isBehindComment = isBehindLineComment(x, y);
      m_isLabelComment = isLabelComment(y);

      if (isComment(x, y)) {
        text = getMultiLineComment(y);
      }

      udpateUndolist(getLabelContent(), lineContent.getLineObject().getPersistentModel(),
          m_editableObject, text, isAboveLineComment(y), isBehindLineComment(x, y),
          isLabelComment(y), getCaretStartPosX(), getCaretMousePressedX(), getCaretMousePressedY(),
          getCaretEndPosX(), getCaretMouseReleasedX(), getCaretMouseReleasedY());
    }
  }

  @Override
  protected void updateCaret() {
    // Do nothing
  }

  @Override
  protected void updateClipboard() {
    final String clipboardText = getSelectedText();

    ClipboardHelpers.copyToClipboard(clipboardText);
  }

  @Override
  protected void updateLabelContent() {
    // Do nothing
  }

  @Override
  protected void updateSelection() {
    deleteSelection();
  }

  @Override
  protected void updateUndoHistory() {
    final int x = getCaretEndPosX();
    final int y = getCaretMouseReleasedY();

    final ZyLineContent lineContent = getLineContent(y);
    final IZyEditableObject lineFragmentObject = lineContent.getLineFragmentObjectAt(x);

    String text = "";

    if (lineFragmentObject != null) {
      text =
          lineContent.getText().substring(lineFragmentObject.getStart(),
              lineFragmentObject.getEnd());

      if (isComment(x, y)) {
        text = getMultiLineComment(y);
      }

      udpateUndolist(getLabelContent(), lineContent.getLineObject().getPersistentModel(),
          m_editableObject, text, m_isAboveComment, m_isBehindComment, m_isLabelComment,
          getCaretStartPosX(), getCaretMousePressedX(), getCaretMousePressedY(), getCaretEndPosX(),
          getCaretMouseReleasedX(), getCaretMouseReleasedY());
    }
  }
}
