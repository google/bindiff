// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.JHint;

import com.google.security.zynamics.zylib.resources.Constants;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class JHintIcon extends JPanel {
  private static final long serialVersionUID = 6381830838383637854L;

  private static final ImageIcon HELP_ICON = new ImageIcon(Constants.class.getResource("help.png")); //$NON-NLS-1$

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
