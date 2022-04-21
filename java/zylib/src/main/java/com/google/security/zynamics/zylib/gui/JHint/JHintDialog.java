// Copyright 2011-2022 Google LLC
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

public class JHintDialog extends JDialog {
  private static final long serialVersionUID = -6233942484161880642L;

  public JHintDialog(final Window parent, final String message) {
    super(parent);

    setResizable(false);

    setLayout(new BorderLayout());
    setAlwaysOnTop(true);
    setUndecorated(true);

    final JPanel innerPanel = new JPanel(new BorderLayout());
    innerPanel.setBorder(new LineBorder(Color.BLACK));

    final JTextArea textField = new JTextArea(message);

    textField.setEditable(false);

    innerPanel.add(textField);

    add(innerPanel);

    pack();
  }
}
