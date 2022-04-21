// Copyright 2011-2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
