// Copyright 2011-2020 Google LLC
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
