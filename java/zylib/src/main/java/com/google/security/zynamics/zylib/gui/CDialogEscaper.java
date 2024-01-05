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

package com.google.security.zynamics.zylib.gui;

import com.google.common.base.Preconditions;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

// TODO(cblichmann): Using this class is weird, it should be just a static method.
public class CDialogEscaper {
  /**
   * Registers a keyboard action for the passed dialog such that it will close when the "ESC" key is
   * pressed.
   *
   * @param dialog The Dialog which can now be closed by the "ESC" Key.
   */
  public CDialogEscaper(final JDialog dialog) {
    Preconditions.checkNotNull(dialog, "Error: dialog argument can not be null");

    // Allow the user to close the dialog with the ESC key.
    dialog
        .getRootPane()
        .registerKeyboardAction(
            new ActionListener() {
              @Override
              public void actionPerformed(final ActionEvent unused) {
                dialog.setVisible(false);
                dialog.dispose();
              }
            },
            "doEscape",
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
  }
}
