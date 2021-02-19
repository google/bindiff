// Copyright 2011-2021 Google LLC
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

package com.google.security.zynamics.zylib.gui.FileChooser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ToolTipManager;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

public class FileChooserPanel extends JPanel {
  private final JTextField inputField;
  private final JButton browseButton;

  public FileChooserPanel(final String defaultText, final ActionListener listener) {
    this(defaultText, listener, "...", 0, 0, 0);
  }

  public FileChooserPanel(final String defaultText, final ActionListener listener,
      final String buttonText) {
    this(defaultText, listener, buttonText, 0, 0, 0);
  }

  public FileChooserPanel(final String defaultText, final ActionListener listener,
      final String buttonText, final int width, final int height, final int buttonWidth) {
    super(new BorderLayout());

    setBorder(new LineBorder(Color.GRAY));

    inputField = new JTextField(defaultText);
    inputField.setEditable(false);

    if ((width > 0) || (height > 0)) {
      setPreferredSize(new Dimension(width, height));
    }
    final JPanel p1extBt = new JPanel(new BorderLayout());

    browseButton = new JButton(buttonText);
    browseButton.setBorder(new MatteBorder(0, 1, 0, 0, Color.GRAY));

    final Dimension prefSide = browseButton.getPreferredSize();

    browseButton.setPreferredSize(new Dimension(prefSide.width + 15, prefSide.height));

    if (buttonWidth > 0) {
      browseButton.setPreferredSize(new Dimension(buttonWidth, height));
    }

    p1extBt.add(browseButton, BorderLayout.CENTER);
    browseButton.setFocusable(false);
    add(inputField, BorderLayout.CENTER);
    add(p1extBt, BorderLayout.EAST);

    browseButton.addActionListener(listener);

    ToolTipManager.sharedInstance().registerComponent(inputField);
    inputField.setToolTipText(getText());
  }

  public JButton getButton() {
    return browseButton;
  }

  public String getText() {
    return inputField.getText();
  }

  @Override
  public void setEnabled(final boolean enable) {
    browseButton.setEnabled(enable);

    super.setEnabled(enable);
  }

  public void setText(final String text) {
    inputField.setText(text);
  }
}
