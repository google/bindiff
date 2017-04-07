// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.comboboxes.memorybox;

import javax.swing.DefaultComboBoxModel;

public class JMemoryBoxModel extends DefaultComboBoxModel<String> {

  private final int m_maximum;

  public JMemoryBoxModel(final int maximum) {
    m_maximum = maximum;
  }

  public void add(final String string) {
    removeElement(string);
    insertElementAt(string, 0);

    while (getSize() > m_maximum) {
      removeElementAt(m_maximum);
    }

    setSelectedItem(string);
  }
}
