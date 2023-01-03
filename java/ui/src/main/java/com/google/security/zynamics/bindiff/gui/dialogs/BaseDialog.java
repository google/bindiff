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

package com.google.security.zynamics.bindiff.gui.dialogs;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class BaseDialog extends JDialog {
  public BaseDialog(final Window parent, final String title) {
    super(parent, ModalityType.APPLICATION_MODAL);
    init();

    setTitle(title);
  }

  public BaseDialog(
      final Window parent, final String title, final int minWidth, final int minHeight) {
    this(parent, title);

    setSize(minWidth, minHeight);
    setMinimumSize(new Dimension(minWidth, minHeight));
  }

  private void init() {
    getRootPane()
        .registerKeyboardAction(
            new ActionListener() {
              @Override
              public void actionPerformed(final ActionEvent unused) {
                setVisible(false);
                dispose();
              }
            },
            "doEscape",
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
  }

  @Override
  public void setVisible(final boolean visible) {
    if (visible) {
      UIManager.put("TabbedPane.tabAreaInsets", new Insets(2, 2, 0, 2));
      UIManager.put("TabbedPane.contentBorderInsets", new Insets(4, 2, 3, 2));

      SwingUtilities.updateComponentTreeUI(this);
    }
    super.setVisible(visible);
  }
}
