// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.ColorGrid;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;

import javax.swing.JPanel;

import com.google.common.base.Preconditions;

public class ColorGrid extends JPanel {
  private static final long serialVersionUID = -5981626927250840656L;

  private static final int squareSize = 40;

  private final List<Color> m_colors;

  private final int m_columns;

  public ColorGrid(final List<Color> colors, final int columns) {
    m_colors = Preconditions.checkNotNull(colors, "Error: colors argument can not be null");
    m_columns = columns;

    final int rows = (m_colors.size() / m_columns) + ((m_colors.size() % m_columns) == 0 ? 0 : 1);

    setPreferredSize(new Dimension(columns * squareSize, rows * squareSize));
  }

  public Color getColorAt(final int x, final int y) {
    final int row = y / squareSize;
    final int col = x / squareSize;

    if (((row * m_columns) + col) < m_colors.size()) {
      return m_colors.get((row * m_columns) + col);
    }

    return null;
  }

  @Override
  public void paint(final Graphics g) {
    final int rows = (m_colors.size() / m_columns) + ((m_colors.size() % m_columns) == 0 ? 0 : 1);

    for (int i = 0; i < rows; i++) {
      for (int j = 0; (j < m_columns) && (((i * m_columns) + j) < m_colors.size()); j++) {
        g.setColor(m_colors.get((i * m_columns) + j));
        g.fillRect(j * squareSize, i * squareSize, squareSize, squareSize);
        g.setColor(Color.BLACK);
        g.drawRect(j * squareSize, i * squareSize, squareSize, squareSize);
      }
    }
  }
}
