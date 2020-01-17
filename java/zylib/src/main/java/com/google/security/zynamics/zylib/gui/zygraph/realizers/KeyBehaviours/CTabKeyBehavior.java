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

import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyEditableObject;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.UndoHistroy.CUndoManager;

import java.util.List;


public class CTabKeyBehavior extends CAbstractKeyBehavior {
  public CTabKeyBehavior(final CUndoManager undoManager) {
    super(undoManager);
  }

  @Override
  protected void initUndoHistory() {
    // Do nothing
  }

  @Override
  protected void updateCaret() {
    if (!isAltPressed() && !isCtrlPressed()) {
      int caretX = getCaretEndPosX();
      int caretY = getCaretMousePressedY();

      ZyLineContent lineContent = getLineContent(caretY);
      final IZyEditableObject editObject = lineContent.getLineFragmentObjectAt(caretX);

      final List<IZyEditableObject> editObjects = lineContent.getLineFragmentObjectList();
      IZyEditableObject nextEditObject = null;

      if (!isShiftPressed()) {
        // shift is not pressed

        if (editObject != null) {
          if (!isComment(caretX, caretY)) {
            final int editObjectIndex = lineContent.getLineFragmentObjectList().indexOf(editObject);

            if (editObjectIndex < (editObjects.size() - 1)) {
              nextEditObject = editObjects.get(editObjectIndex + 1);
            }
          }
        } else {
          for (final IZyEditableObject obj : editObjects) {
            if (obj.getStart() > caretX) {
              nextEditObject = obj;

              break;
            }
          }
        }

        int tempY = caretY;
        if (isComment(caretX, caretY)) {
          final int lastCommentLine = getLabelContent().getLastLineIndexOfModelAt(tempY);
          final int noneCommentLine = getLabelContent().getNonPureCommentLineIndexOfModelAt(tempY);

          if (noneCommentLine != -1) {
            if (tempY < noneCommentLine) {
              tempY = noneCommentLine - 1;
            } else {
              tempY = lastCommentLine;
            }
          } else {
            tempY = getLabelContent().getLineCount() - 1;
          }
        }

        if (nextEditObject == null) {
          boolean first = true;
          while (((nextEditObject == null) && (tempY != caretY)) || first) {
            first = false;

            tempY = tempY == (getLabelContent().getLineCount() - 1) ? 0 : ++tempY;

            final ZyLineContent nextLineContent = getLineContent(tempY);

            final List<IZyEditableObject> nextEditObjects =
                nextLineContent.getLineFragmentObjectList();

            if (nextEditObjects.size() > 0) {
              nextEditObject = nextEditObjects.get(0);
            }
          }
        }

        if (nextEditObject != null) {
          caretX = nextEditObject.getStart();

          if (nextEditObject.isCommentDelimiter()) {
            caretX = nextEditObject.getEnd();
          }

          caretY = tempY;

          setCaret(caretX, caretX, caretY, caretX, caretX, caretY);
        }
      } else {
        // shift is pressed

        if (editObject != null) {
          if (!isComment(caretX, caretY)) {
            final int editObjectIndex = lineContent.getLineFragmentObjectList().indexOf(editObject);

            if (editObjectIndex < 1) {
              nextEditObject = editObjects.get(editObjectIndex - 1);
            }
          }
        } else {
          for (int index = editObjects.size() - 1; index >= 0; --index) {
            final IZyEditableObject obj = editObjects.get(index);

            if (obj.getEnd() < caretX) {
              nextEditObject = obj;

              break;
            }
          }
        }

        int tempY = caretY;
        if (isComment(caretX, caretY)) {
          final int firstCommentLine = getLabelContent().getFirstLineIndexOfModelAt(tempY);
          final int noneCommentLine = getLabelContent().getNonPureCommentLineIndexOfModelAt(tempY);

          if (noneCommentLine != -1) {
            if (tempY < noneCommentLine) {
              tempY = firstCommentLine;
            } else {
              tempY = noneCommentLine;
            }
          }
        }

        if (nextEditObject == null) {
          boolean first = true;
          while (((nextEditObject == null) && (tempY != caretY)) || first) {
            first = false;

            tempY = tempY == 0 ? getLabelContent().getLineCount() - 1 : --tempY;

            final ZyLineContent nextLineContent = getLineContent(tempY);

            final List<IZyEditableObject> nextEditObjects =
                nextLineContent.getLineFragmentObjectList();

            if (nextEditObjects.size() > 0) {
              nextEditObject = nextEditObjects.get(nextEditObjects.size() - 1);
            }
          }
        }

        if (nextEditObject != null) {
          caretX = nextEditObject.getStart();

          if (nextEditObject.isCommentDelimiter()) {
            caretX = nextEditObject.getEnd();
          }

          caretY = tempY;

          if (isComment(caretX, caretY)) {
            final int firstCommentLine = getLabelContent().getFirstLineIndexOfModelAt(tempY);
            final int noneCommentLine =
                getLabelContent().getNonPureCommentLineIndexOfModelAt(tempY);

            if ((caretY <= noneCommentLine) || (noneCommentLine < 0)) {
              caretY = firstCommentLine;
            } else {
              caretY = noneCommentLine;
            }

            lineContent = getLineContent(caretY);
            nextEditObject = lineContent.getLineFragmentObjectAt(caretX);

            if ((nextEditObject != null) && nextEditObject.isCommentDelimiter()) {
              caretX = nextEditObject.getEnd();
            }
          }

          setCaret(caretX, caretX, caretY, caretX, caretX, caretY);
        }
      }
    }
  }

  @Override
  protected void updateClipboard() {
    // Do nothing
  }

  @Override
  protected void updateLabelContent() {
    // Do nothing
  }

  @Override
  protected void updateSelection() {
    // Do nothing
  }

  @Override
  protected void updateUndoHistory() {
    // Do nothing
  }
}
