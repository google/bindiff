// Copyright 2011 Google Inc. All Rights Reserved.

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
