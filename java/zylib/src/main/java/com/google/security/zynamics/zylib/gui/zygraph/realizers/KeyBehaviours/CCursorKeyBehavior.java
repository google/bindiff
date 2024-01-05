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

import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.UndoHistroy.CUndoManager;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent;
import java.awt.event.KeyEvent;

public class CCursorKeyBehavior extends CAbstractKeyBehavior {
  public CCursorKeyBehavior(final CUndoManager undoManager) {
    super(undoManager);
  }

  private int getXdelta() {
    int delta = 0;

    if (getEvent().getKeyCode() == KeyEvent.VK_LEFT) {
      delta = -1;
    } else if (getEvent().getKeyCode() == KeyEvent.VK_RIGHT) {
      delta = +1;
    }

    return delta;
  }

  private int getYdelta() {
    int delta = 0;

    if (getEvent().getKeyCode() == KeyEvent.VK_UP) {
      delta = -1;
    } else if (getEvent().getKeyCode() == KeyEvent.VK_DOWN) {
      delta = +1;
    }

    return delta;
  }

  protected void handleNotShiftAndCtrl(final int xdelta, final int ydelta) {
    final ZyLabelContent labelContent = getLabelContent();

    int caretStartPos_X = getCaretStartPosX();
    int mousePressed_X = getCaretMousePressedX();
    final int mousePressed_Y = getCaretMousePressedY();

    int caretEndPos_X = getCaretEndPosX();
    int mouseReleased_X = getCaretMouseReleasedX();
    final int mouseReleased_Y = getCaretMouseReleasedY();

    if (xdelta != 0) {
      final ZyLineContent lineContent = labelContent.getLineContent(mouseReleased_Y);

      final String s = lineContent.getText() + " ";

      if (xdelta > 0) {
        final boolean firstIsSpace = s.charAt(caretEndPos_X) == ' ';

        int endindex = 0;

        for (int i = caretEndPos_X; i < s.length(); ++i) {
          endindex = i;
          if (firstIsSpace) {
            if (s.charAt(i) != ' ') {
              break;
            }
          } else {
            if (s.charAt(i) == ' ') {
              break;
            }
          }
        }

        if ((lineContent.getText().endsWith("\n") || lineContent.getText().endsWith("\r"))
            && (lineContent.getText().length() == endindex)) {
          endindex -= 1;
        }

        caretStartPos_X = endindex;
        mousePressed_X = endindex;
        mouseReleased_X = endindex;
        caretEndPos_X = endindex;
      } else if (xdelta < 0) {
        if (caretEndPos_X == 0) {
          return;
        }
        final boolean firstIsSpace = s.charAt(caretEndPos_X - 1) == ' ';

        int startindex = caretEndPos_X - 1;

        for (int i = caretEndPos_X - 1; i >= 0; --i) {
          if (firstIsSpace) {
            if (s.charAt(i) != ' ') {
              break;
            }
          } else {
            if (s.charAt(i) == ' ') {
              break;
            }
          }
          startindex = i;
        }

        caretStartPos_X = startindex;
        mousePressed_X = startindex;
        mouseReleased_X = startindex;
        caretEndPos_X = startindex;
      }

      mouseReleased_X = correctMouseReleasedX(mouseReleased_X, mouseReleased_Y, mousePressed_Y);

      setCaret(
          caretStartPos_X,
          mousePressed_X,
          mousePressed_Y,
          caretEndPos_X,
          mouseReleased_X,
          mouseReleased_Y);
    } else if (ydelta != 0) {
      handleNotShiftAndNotCtrl(0, ydelta);
    }
  }

  protected void handleNotShiftAndNotCtrl(final int xdelta, final int ydelta) {
    final ZyLabelContent labelContent = getLabelContent();

    int caretStartPos_X = getCaretStartPosX();
    int mousePressed_X = getCaretMousePressedX();
    int mousePressed_Y = getCaretMousePressedY();

    int caretEndPos_X = getCaretEndPosX();
    int mouseReleased_X = getCaretMouseReleasedX();
    int mouseReleased_Y = getCaretMouseReleasedY();

    if (isSelection() && (xdelta != 0) && (ydelta == 0)) {
      if ((mousePressed_Y <= mouseReleased_Y) && (mousePressed_X <= mouseReleased_X)) {
        if (xdelta < 0) {
          mouseReleased_X = mousePressed_X;
          caretEndPos_X = caretStartPos_X;
          mouseReleased_Y = mousePressed_Y;
        } else {
          mousePressed_X = mouseReleased_X;
          caretStartPos_X = mouseReleased_X;
          mousePressed_Y = mouseReleased_Y;
        }
      } else {
        if (xdelta > 0) {
          mouseReleased_X = mousePressed_X;
          caretEndPos_X = caretStartPos_X;
          mouseReleased_Y = mousePressed_Y;
        } else {
          mousePressed_X = mouseReleased_X;
          caretStartPos_X = mouseReleased_X;
          mousePressed_Y = mouseReleased_Y;
        }
      }
    } else {
      mousePressed_X = mouseReleased_X;
      mousePressed_Y = mouseReleased_Y;

      mousePressed_Y += ydelta;

      if (mousePressed_Y < 0) {
        mousePressed_Y = 0;
      }
      if (mousePressed_Y > (labelContent.getLineCount() - 1)) {
        mousePressed_Y = labelContent.getLineCount() - 1;
      }
      mouseReleased_Y = mousePressed_Y;

      mousePressed_X += xdelta;

      if (mousePressed_X < 0) {
        mousePressed_X = 0;
      }

      if (mousePressed_X
          > (labelContent.getLineContent(mouseReleased_Y).getTextLayout().getCharacterCount()
              - 1)) {
        mousePressed_X =
            labelContent.getLineContent(mouseReleased_Y).getTextLayout().getCharacterCount();
      }

      caretEndPos_X = mousePressed_X;

      mouseReleased_X = mousePressed_X;
      caretStartPos_X = caretEndPos_X;
    }

    mouseReleased_X = correctMouseReleasedX(mouseReleased_X, mouseReleased_Y, mousePressed_Y);

    setCaret(
        caretStartPos_X,
        mousePressed_X,
        mousePressed_Y,
        caretEndPos_X,
        mouseReleased_X,
        mouseReleased_Y);
  }

  protected void handleShiftAndCtrl(final int xdelta, final int ydelta) {
    if (ydelta != 0) {
      handleShiftAndNotCtrl(0, ydelta);
    } else if (xdelta != 0) {
      final ZyLabelContent labelContent = getLabelContent();
      final ZyLineContent lineContent = labelContent.getLineContent(getCaretMouseReleasedY());

      int caretStartPos_X = getCaretStartPosX();
      final int mousePressed_X = getCaretMousePressedX();
      final int mousePressed_Y = getCaretMousePressedY();

      int caretEndPos_X = getCaretEndPosX();
      int mouseReleased_X = getCaretMouseReleasedX();
      final int mouseReleased_Y = getCaretMouseReleasedY();

      final String s = lineContent.getText() + " ";

      if (xdelta > 0) {
        final boolean firstIsSpace = s.charAt(caretEndPos_X) == ' ';
        int endindex = 0;
        for (int i = caretEndPos_X; i < s.length(); ++i) {
          endindex = i;
          if (firstIsSpace) {
            if (s.charAt(i) != ' ') {
              break;
            }
          } else {
            if (s.charAt(i) == ' ') {

              break;
            }
          }
        }

        if ((lineContent.getText().endsWith("\n") || lineContent.getText().endsWith("\r"))
            && (lineContent.getText().length() == endindex)) {
          endindex -= 1;
        }

        caretStartPos_X = mousePressed_X;

        mouseReleased_X = endindex;
        caretEndPos_X = endindex;

        final boolean noReturn = getCaretEndPosX() == lineContent.getText().length();
        final boolean withReturn =
            lineContent.getText().endsWith("\n")
                && (getCaretEndPosX() == (lineContent.getText().length() - 1));
        final boolean withCReturn =
            lineContent.getText().endsWith("\r")
                && (getCaretEndPosX() == (lineContent.getText().length() - 1));

        if (noReturn || withReturn || withCReturn) {
          mouseReleased_X = getMaxLineLength(getCaretMousePressedY(), getCaretMouseReleasedY());
        }
      } else if (xdelta < 0) {
        if (caretEndPos_X == 0) {
          return;
        }
        final boolean firstIsSpace = s.charAt(caretEndPos_X - 1) == ' ';
        int startindex = caretEndPos_X - 1;
        for (int i = caretEndPos_X - 1; i >= 0; --i) {
          if (firstIsSpace) {
            if (s.charAt(i) != ' ') {
              break;
            }
          } else {
            if (s.charAt(i) == ' ') {
              break;
            }
          }
          startindex = i;
        }
        caretStartPos_X = mousePressed_X;

        mouseReleased_X = startindex;
        caretEndPos_X = startindex;
      }

      mouseReleased_X = correctMouseReleasedX(mouseReleased_X, mouseReleased_Y, mousePressed_Y);

      setCaret(
          caretStartPos_X,
          mousePressed_X,
          mousePressed_Y,
          caretEndPos_X,
          mouseReleased_X,
          mouseReleased_Y);
    }
  }

  protected void handleShiftAndNotCtrl(final int xDelta, final int yDelta) {
    final ZyLabelContent labelContent = getLabelContent();

    int caretStartPos_X = getCaretStartPosX();
    final int mousePressed_X = getCaretMousePressedX();
    final int mousePressed_Y = getCaretMousePressedY();

    int caretEndPos_X = getCaretEndPosX();
    int mouseReleased_X = getCaretMouseReleasedX();
    int mouseReleased_Y = getCaretMouseReleasedY();

    final int linecount = labelContent.getLineCount();

    if ((xDelta != 0) || (yDelta != 0)) {
      mouseReleased_Y += yDelta;
      if (mouseReleased_Y < 0) {
        mouseReleased_Y = 0;
      }
      if (mouseReleased_Y > (linecount - 1)) {
        mouseReleased_Y = linecount - 1;
      }

      mouseReleased_X += xDelta;
      if (mouseReleased_X < 0) {
        mouseReleased_X = 0;
      }

      int lp = mousePressed_Y;
      int lr = mouseReleased_Y;

      if (lp > lr) {
        lp = mouseReleased_Y;
        lr = mousePressed_Y;
      }

      int maxlength = 0;
      for (int y = lp; y <= lr; ++y) {
        maxlength = Math.max(maxlength, labelContent.getLineContent(y).getText().length());
      }

      if (mouseReleased_X > maxlength) {
        mouseReleased_X = maxlength;
      }

      if (mouseReleased_X
          <= labelContent.getLineContent(mouseReleased_Y).getTextLayout().getCharacterCount()) {
        caretEndPos_X = mouseReleased_X;
      } else {
        caretEndPos_X =
            labelContent.getLineContent(mouseReleased_Y).getTextLayout().getCharacterCount();
      }

      caretStartPos_X = mousePressed_X;
    }

    mouseReleased_X = correctMouseReleasedX(mouseReleased_X, mouseReleased_Y, mousePressed_Y);

    setCaret(
        caretStartPos_X,
        mousePressed_X,
        mousePressed_Y,
        caretEndPos_X,
        mouseReleased_X,
        mouseReleased_Y);
  }

  @Override
  protected void initUndoHistory() {}

  @Override
  protected void updateCaret() {
    final int xDelta = getXdelta();
    final int yDelta = getYdelta();

    if (!isShiftPressed() && !isCtrlPressed()) {
      handleNotShiftAndNotCtrl(xDelta, yDelta);
    } else if (isShiftPressed() && !isCtrlPressed()) {
      handleShiftAndNotCtrl(xDelta, yDelta);
    } else if (!isShiftPressed() && isCtrlPressed()) {
      handleNotShiftAndCtrl(xDelta, yDelta);
    } else if (isShiftPressed() && isCtrlPressed()) {
      handleShiftAndCtrl(xDelta, yDelta);
    }
  }

  @Override
  protected void updateClipboard() {}

  @Override
  protected void updateLabelContent() {
    return;
  }

  @Override
  protected void updateSelection() {}

  @Override
  protected void updateUndoHistory() {}
}
