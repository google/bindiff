// Copyright 2011-2021 Google LLC
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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import javax.swing.Icon;
import javax.swing.SwingConstants;

public class BackgroundIcon implements Icon {
  private final String text;
  private final int horizontalAlignment;
  private final int height;
  private final int xOffset;
  private final int yOffset;

  private int width;

  private final Color backgroundColor;
  private final Color selectionColor;
  private final Color textColor;

  private final boolean selected;

  public BackgroundIcon(
      final String text,
      final int horizontalAlignment,
      final Color textColor,
      final Color backgroundColor,
      final Color selectionColor,
      final boolean selected,
      final int xOffset,
      final int yOffset,
      final int width,
      final int height) {
    this.text = text;
    this.horizontalAlignment = horizontalAlignment;
    this.textColor = textColor;
    this.backgroundColor = backgroundColor;
    this.selectionColor = selectionColor;
    this.selected = selected;
    this.width = width;
    this.height = height;
    this.xOffset = xOffset;
    this.yOffset = yOffset;
  }

  private void drawText(final Graphics2D gfx) {
    final Color oldColor = gfx.getColor();
    gfx.setColor(textColor);

    final Composite oldComposite = gfx.getComposite();
    gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    Rectangle2D rect = gfx.getFontMetrics(gfx.getFont()).getStringBounds(text, gfx);

    final double widthSum = 4 + rect.getWidth() + 4;

    int xT = 4;

    final int yT = (int) ((height - rect.getHeight()) / 2 + rect.getHeight()) - 2;

    if (widthSum < width) {
      if (horizontalAlignment == SwingConstants.CENTER) {
        xT = (int) ((width - rect.getWidth()) / 2) + 1;
      } else if (horizontalAlignment == SwingConstants.RIGHT) {
        xT = (int) (width - rect.getWidth() - 4);
      }

      gfx.drawString(text, xT, yT);
    } else {
      String s = "...";
      String s2 = text;
      while (s2.length() > 2) {
        s2 = s2.substring(0, s2.length() - 2);
        rect = gfx.getFontMetrics(gfx.getFont()).getStringBounds(s2 + "...", gfx);

        if (rect.getWidth() + 8 < width) {
          s = s2 + "...";
          break;
        }
      }

      xT = 4;
      if (horizontalAlignment == SwingConstants.CENTER) {
        xT = (int) ((width - rect.getWidth()) / 2) + 1;
      } else if (horizontalAlignment == SwingConstants.RIGHT) {
        xT = (int) (width - rect.getWidth() - 4);
      }

      rect = gfx.getFontMetrics(gfx.getFont()).getStringBounds(s, gfx);

      gfx.drawString(s, xT, yT);
    }

    gfx.setComposite(oldComposite);

    gfx.setColor(oldColor);
  }

  @Override
  public int getIconHeight() {
    return height;
  }

  @Override
  public int getIconWidth() {
    return width;
  }

  @Override
  public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
    final int xP = x + xOffset;
    final int yP = y + yOffset;

    g.translate(xP, yP);

    final Graphics2D gfx = (Graphics2D) g;

    final Color oldColor = gfx.getColor();

    gfx.setColor(backgroundColor);
    gfx.fillRect(xP, yP, width, height);

    if (selected) {
      final Composite oldComposite = gfx.getComposite();

      gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .6f));

      gfx.setColor(selectionColor);
      gfx.fillRect(xP, yP, width, height);

      gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.f));
      gfx.drawRect(xP, yP, width - 1, height - 1);

      gfx.setComposite(oldComposite);
    }

    drawText(gfx);

    gfx.setColor(oldColor);

    g.translate(-xP, -yP);
  }

  public void setWidth(final int width) {
    this.width = width;
  }
}
