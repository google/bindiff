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

import com.google.security.zynamics.zylib.general.ClipboardHelpers;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyEditableObject;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.UndoHistroy.CUndoManager;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent;

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
          lineContent
              .getText()
              .substring(lineFragmentObject.getStart(), lineFragmentObject.getEnd());

      m_isAboveComment = isAboveLineComment(y);
      m_isBehindComment = isBehindLineComment(x, y);
      m_isLabelComment = isLabelComment(y);

      if (isComment(x, y)) {
        text = getMultiLineComment(y);
      }

      udpateUndolist(
          getLabelContent(),
          lineContent.getLineObject().getPersistentModel(),
          m_editableObject,
          text,
          isAboveLineComment(y),
          isBehindLineComment(x, y),
          isLabelComment(y),
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
          lineContent
              .getText()
              .substring(lineFragmentObject.getStart(), lineFragmentObject.getEnd());

      if (isComment(x, y)) {
        text = getMultiLineComment(y);
      }

      udpateUndolist(
          getLabelContent(),
          lineContent.getLineObject().getPersistentModel(),
          m_editableObject,
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
