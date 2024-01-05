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

package com.google.security.zynamics.zylib.gui.JHint;

import com.google.security.zynamics.zylib.resources.Constants;
import java.awt.BorderLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class JHintIcon extends JPanel {
  private static final long serialVersionUID = 6381830838383637854L;

  private static final ImageIcon HELP_ICON =
      new ImageIcon(Constants.class.getResource("help.png")); // $NON-NLS-1$

  private final String m_message;

  private JHintDialog m_dialog;

  private static final boolean m_isCursorOverDialog = false;

  public JHintIcon(final String message) {
    super(new BorderLayout());

    m_message = message;

    final JLabel label = new JLabel(HELP_ICON);

    add(label);
    setToolTipText(message);
  }

  public JHintDialog getM_dialog() {
    return m_dialog;
  }

  public String getM_message() {
    return m_message;
  }

  public boolean isM_isCursorOverDialog() {
    return m_isCursorOverDialog;
  }

  public void setM_dialog(final JHintDialog m_dialog) {
    this.m_dialog = m_dialog;
  }
}
