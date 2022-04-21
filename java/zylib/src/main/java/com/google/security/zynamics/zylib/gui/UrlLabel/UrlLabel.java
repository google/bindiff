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

package com.google.security.zynamics.zylib.gui.UrlLabel;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JLabel;
import javax.swing.border.Border;

/**
 * Label class that can be used to launch URLs from Java.
 * 
 * Taken from http://forum.java.sun.com/thread.jspa?threadID=613854&messageID=9959263
 */
public class UrlLabel extends JLabel {
  private static final long serialVersionUID = 1L;

  private static MouseListener linker = new MouseAdapter() {
    @Override
    public void mouseClicked(final MouseEvent event) {
      final UrlLabel self = (UrlLabel) event.getSource();

      if (self.url == null) {
        return;
      }

      try {
        Desktop.getDesktop().browse(self.url.toURI());
      } catch (final URISyntaxException e) {
        // TODO: This should be properly logged
        System.out.println(e);
      } catch (final IOException e) {
        // TODO: This should be properly logged
        System.out.println(e);
      }
    }

    @Override
    public void mouseEntered(final MouseEvent event) {
      event.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
  };

  private URL url;

  public UrlLabel(final String label) {
    super(label);
    setForeground(Color.BLUE);
    addMouseListener(linker);
  }

  public UrlLabel(final String label, final String tip) {
    this(label);
    setToolTipText(tip);
  }

  public UrlLabel(final String label, final String tip, final URL url) {
    this(label, url);
    setToolTipText(tip);
  }

  public UrlLabel(final String label, final URL url) {
    this(label);
    this.url = url;
  }

  @Override
  public void paint(final Graphics g) {
    super.paint(g);

    final Border border = getBorder();

    int realLeft = 0;
    int realWidth = getWidth();

    if (border != null) {
      final Insets insets = border.getBorderInsets(this);

      realWidth -= insets.right;
      realWidth -= insets.left;
      realLeft += insets.left;
    }

    g.drawLine(realLeft, getHeight() - 2, realWidth, getHeight() - 2);
  }

  public void setUrl(final URL url) {
    this.url = url;
  }
}
