// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours;

import com.google.security.zynamics.zylib.gui.zygraph.realizers.KeyBehaviours.UndoHistroy.CUndoManager;

public class CRedoKeyBehavior extends CAbstractKeyBehavior {
  public CRedoKeyBehavior(final CUndoManager undoManager) {
    super(undoManager);
  }

  @Override
  protected void initUndoHistory() {
    // Do nothing
  }

  @Override
  protected void updateCaret() {
    // Do nothing
  }

  @Override
  protected void updateClipboard() {
    // Do nothing
  }

  @Override
  protected void updateLabelContent() {
    redo();
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
