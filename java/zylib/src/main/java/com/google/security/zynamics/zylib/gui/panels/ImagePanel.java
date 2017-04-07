// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.panels;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {
  private static final long serialVersionUID = -4190301726730485967L;
  private final Image m_image;

  public ImagePanel(final Image image) {
    this.m_image = image;

    setSize(image.getWidth(null), image.getHeight(null));
    setPreferredSize(getSize());

    setBackground(Color.RED);
  }

  @Override
  public void paint(final Graphics g) {
    g.drawImage(m_image, 0, 0, this);
  }
}
