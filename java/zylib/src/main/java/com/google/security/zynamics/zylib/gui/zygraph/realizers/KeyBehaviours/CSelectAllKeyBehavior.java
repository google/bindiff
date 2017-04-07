// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours;

import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.UndoHistroy.CUndoManager;

public class CSelectAllKeyBehavior extends CAbstractKeyBehavior {
  public CSelectAllKeyBehavior(final CUndoManager undoManager) {
    super(undoManager);
  }

  @Override
  protected void initUndoHistory() {
    // Do nothing
  }

  @Override
  protected void updateCaret() {
    final int lastLineIndex = getLabelContent().getLineCount() - 1;

    int lineMaxX = 0;

    for (final ZyLineContent lineContent : getLabelContent()) {
      lineMaxX = Math.max(lineContent.getText().length(), lineMaxX);
    }

    final ZyLineContent lastLineContent = getLineContent(lastLineIndex);

    final int lastLineLength = lastLineContent.getText().length();

    setCaret(0, 0, 0, lastLineLength, lineMaxX, lastLineIndex);
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
