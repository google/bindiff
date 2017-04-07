// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.imagecombobox;

import javax.swing.ImageIcon;

public class ImageElement {
  private final Object m_object;
  private final ImageIcon m_icon;

  public ImageElement(final Object object, final ImageIcon icon) {
    m_object = object;
    m_icon = icon;
  }

  public ImageIcon getIcon() {
    return m_icon;
  }

  public Object getObject() {
    return m_object;
  }
}
