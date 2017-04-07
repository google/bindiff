// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.comboboxes.memorybox;

import javax.swing.JComboBox;

import com.google.common.base.Preconditions;

public class JMemoryBox extends JComboBox<String> {

  private final JMemoryBoxModel m_model;

  public JMemoryBox(final int maximum) {
    Preconditions.checkArgument(maximum > 0, "Error: Maximum argument must be positive");

    m_model = new JMemoryBoxModel(maximum);

    setModel(m_model);
    setEditable(true);
  }

  public void add(final String text) {
    m_model.add(Preconditions.checkNotNull(text, "Error: Text argument can not be null"));
  }
}
