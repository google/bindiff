// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours;

import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.UndoHistroy.CUndoManager;

public class CHomeKeyBehavior extends CAbstractKeyBehavior {
  public CHomeKeyBehavior(final CUndoManager undoManager) {
    super(undoManager);
  }

  @Override
  protected void initUndoHistory() {
    // Do nothing
  }

  @Override
  protected void updateCaret() {
    if (!isShiftPressed() && !isCtrlPressed()) {
      setCaret(0, 0, getCaretMousePressedY(), 0, 0, getCaretMouseReleasedY());
    } else if (isShiftPressed() && !isCtrlPressed()) {
      setCaret(getCaretStartPosX(), getCaretMousePressedX(), getCaretMousePressedY(), 0, 0,
          getCaretMouseReleasedY());
    } else if (!isShiftPressed() && isCtrlPressed()) {
      setCaret(0, 0, 0, 0, 0, 0);
    } else if (isShiftPressed() && isCtrlPressed()) {
      setCaret(getCaretStartPosX(), getCaretMousePressedX(), getCaretMousePressedY(), 0, 0, 0);
    }
  }

  @Override
  protected void updateClipboard() {
    // Do nothing
  }

  @Override
  protected void updateLabelContent() {
    return;
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
