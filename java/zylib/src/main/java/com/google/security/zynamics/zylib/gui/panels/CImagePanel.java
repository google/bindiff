// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.panels;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class CImagePanel extends JPanel {
  private static final long serialVersionUID = 8772470195013064027L;

  private final Image m_image;

  public CImagePanel(final Image image) {
    m_image = image;

    final Dimension size = new Dimension(image.getWidth(null), image.getHeight(null));

    setPreferredSize(size);

    setMinimumSize(size);
    setMaximumSize(size);

    setSize(size);

    setLayout(null);
  }

  public CImagePanel(final String image) {
    this(new ImageIcon(image).getImage());
  }

  @Override
  public void paintComponent(final Graphics g) {
    g.drawImage(m_image, 0, 0, null);
  }
}
