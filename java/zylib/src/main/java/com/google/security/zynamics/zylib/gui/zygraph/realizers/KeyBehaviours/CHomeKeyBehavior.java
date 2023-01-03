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
      setCaret(
          getCaretStartPosX(),
          getCaretMousePressedX(),
          getCaretMousePressedY(),
          0,
          0,
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
