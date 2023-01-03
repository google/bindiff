// Copyright 2011-2023 Google LLC
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
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ECommentPlacement;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyEditableObject;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.UndoHistroy.CUndoManager;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent;
import java.awt.Point;

public class CDelKeyBehavior extends CAbstractKeyBehavior {
  private boolean m_wasSelection = false;

  private IZyEditableObject m_editableObject;

  private boolean m_isAboveComment;
  private boolean m_isBehindComment;
  private boolean m_isLabelComment;

  public CDelKeyBehavior(final CUndoManager undoManager) {
    super(undoManager);
  }

  private Point getMultiCommentEndPoint(final int lineXPos, final int lineYPos) {
    if (isComment(lineXPos, lineYPos)) {
      final ZyLineContent lineContent = getLineContent(lineYPos);

      int x =
          lineContent
              .getLineFragmentObjectList()
              .get(lineContent.getLineFragmentObjectList().size() - 1)
              .getEnd();

      int y = lineYPos;

      for (int index = lineYPos + 1;
          index <= getLabelContent().getLastLineIndexOfModelAt(lineYPos);
          ++index) {
        final ZyLineContent nextLineContent = getLineContent(index);

        final int editableObjectSize = nextLineContent.getLineFragmentObjectList().size();

        final IZyEditableObject firstEditableObject = nextLineContent.getLineFragmentObjectAt(0);

        if (firstEditableObject != null) {
          if (firstEditableObject.isCommentDelimiter()) {
            x = nextLineContent.getLineFragmentObjectAt(editableObjectSize - 1).getEnd();

            y = index;
          }
        } else {
          // End of above line comment
          break;
        }
      }

      return new Point(x, y);
    }

    return null;
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
    m_wasSelection = false;
  }

  @Override
  protected void updateClipboard() {
    if (isShiftPressed() && !isCtrlPressed() && !isAltPressed()) {
      final String clipboardText = getSelectedText();

      ClipboardHelpers.copyToClipboard(clipboardText);
    }
  }

  @Override
  protected void updateLabelContent() {
    if (m_wasSelection) {
      return;
    }

    final int x = getCaretEndPosX();
    final int y = getCaretMouseReleasedY();

    final ZyLineContent lineContent = getLineContent(y);
    final IZyEditableObject editableObject = lineContent.getLineFragmentObjectAt(x);

    if (editableObject == null) {
      return;
    }

    if (isComment(x, y)) {
      // Caret end was within a comment, when key was pressed.

      if (editableObject.isCommentDelimiter()) {
        return;
      }

      final Point commentEndPoint = getMultiCommentEndPoint(x, y);

      if (y == commentEndPoint.y) {
        if ((x == commentEndPoint.x)
            || ((x == (commentEndPoint.x - 1)) && lineContent.getText().endsWith("\n"))) {
          return;
        }
      }

      final int textCursor = x - editableObject.getStart();
      final String text =
          lineContent.getText().substring(editableObject.getStart(), editableObject.getEnd());

      String changedText = text;

      if (textCursor < text.length()) {
        changedText =
            String.format("%s%s", text.substring(0, textCursor), text.substring(textCursor + 1));
      } else if (isComment(x, y)) {
        if (y < (getLabelContent().getLineCount() - 1)) {
          final ZyLineContent nextLineContent = getLineContent(y + 1);
          if (nextLineContent.getLineObject() == lineContent.getLineObject()) {
            final IZyEditableObject object = nextLineContent.getLineFragmentObjectAt(0);
            if ((object != null) && object.isCommentDelimiter()) {
              changedText = text.substring(0, textCursor - 1);
            }
          }
        }
      }

      if (isComment(x, y)) {
        changedText = getMultilineComment(y, changedText);
        if (!changedText.endsWith("\r")) {
          changedText += "\r";
        }
      }

      editableObject.update(changedText);

      if (isLabelComment(y)) {
        lineContent.getLineObject().update(changedText);
      } else if (isAboveLineComment(y)) {
        lineContent.getLineObject().updateComment(changedText, ECommentPlacement.ABOVE_LINE);
      } else if (isBehindLineComment(x, y)) {
        lineContent.getLineObject().updateComment(changedText, ECommentPlacement.BEHIND_LINE);
      }

      getLabelContent()
          .getLineEditor()
          .recreateLabelLines(getLabelContent(), lineContent.getLineObject().getPersistentModel());
    } else {
      // Caret end was within an editable object, but NOT a comment.

      throw new RuntimeException("Not implemented yet.");
    }
  }

  @Override
  protected void updateSelection() {
    m_wasSelection = isSelection();

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
