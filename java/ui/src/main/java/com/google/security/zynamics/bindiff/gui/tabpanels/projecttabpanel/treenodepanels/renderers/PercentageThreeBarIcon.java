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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.renderers.AbstractTableCellRenderer;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import javax.swing.Icon;

public class PercentageThreeBarIcon implements Icon {
  private final String leftText;
  private final String centerText;
  private final String rightText;

  private final int leftValue;
  private final int centerValue;
  private final int rightValue;

  private int width;
  private final int height;
  private final int xOffset;
  private final int yOffset;

  private final Color leftBarColor;
  private final Color centerBarColor;
  private final Color rightBarColor;
  private final Color emptyBarColor;
  private final Color textColor;
  private final Color selectionColor;

  private final boolean selected;

  public PercentageThreeBarIcon(
      final PercentageThreeBarCellData data,
      final Color leftBarColor,
      final Color centerBarColor,
      final Color rightBarColor,
      final Color emptyBarColor,
      final Color textColor,
      final Color selectionColor,
      final boolean selected,
      final int xOffset,
      final int yOffset,
      final int width,
      final int height) {
    if (data.getLeftBarValue() == 0
        && data.getCenterBarValue() == 0
        && data.getRightBarValue() == 0) {
      leftText = "";
      centerText = "";
      rightText = "";
      leftValue = 0;
      centerValue = 1;
      rightValue = 0;

      this.selected = selected;
      this.width = width;
      this.height = height;
      this.xOffset = xOffset;
      this.yOffset = yOffset;
      this.leftBarColor = AbstractTableCellRenderer.NON_ACCESSIBLE_COLOR;
      this.centerBarColor = AbstractTableCellRenderer.NON_ACCESSIBLE_COLOR;
      this.rightBarColor = AbstractTableCellRenderer.NON_ACCESSIBLE_COLOR;
      this.emptyBarColor = AbstractTableCellRenderer.NON_ACCESSIBLE_COLOR;
      this.textColor = textColor;
      this.selectionColor = selectionColor;
    } else if (data.getLeftBarValue() != 0
        && data.getCenterBarValue() == 0
        && data.getRightBarValue() == 0) {
      leftText = "";
      centerText = Integer.toString(data.getLeftBarValue());
      rightText = "";
      leftValue = 0;
      centerValue = data.getLeftBarValue();
      rightValue = 0;

      this.selected = selected;
      this.width = width;
      this.height = height;
      this.xOffset = xOffset;
      this.yOffset = yOffset;
      this.leftBarColor = leftBarColor;
      this.centerBarColor = leftBarColor;
      this.rightBarColor = leftBarColor;
      this.emptyBarColor = emptyBarColor;
      this.textColor = textColor;
      this.selectionColor = selectionColor;
    } else if (data.getLeftBarValue() == 0
        && data.getCenterBarValue() == 0
        && data.getRightBarValue() != 0) {
      leftText = "";
      centerText = Integer.toString(data.getRightBarValue());
      rightText = "";
      leftValue = 0;
      centerValue = data.getRightBarValue();
      rightValue = 0;

      this.selected = selected;
      this.width = width;
      this.height = height;
      this.xOffset = xOffset;
      this.yOffset = yOffset;
      this.leftBarColor = rightBarColor;
      this.centerBarColor = rightBarColor;
      this.rightBarColor = rightBarColor;
      this.emptyBarColor = emptyBarColor;
      this.textColor = textColor;
      this.selectionColor = selectionColor;
    } else {
      leftText = Integer.toString(data.getLeftBarValue());
      centerText = Integer.toString(data.getCenterBarValue());
      rightText = Integer.toString(data.getRightBarValue());
      leftValue = data.getLeftBarValue();
      centerValue = data.getCenterBarValue();
      rightValue = data.getRightBarValue();

      this.selected = selected;
      this.width = width;
      this.height = height;
      this.xOffset = xOffset;
      this.yOffset = yOffset;
      this.leftBarColor = leftBarColor;
      this.centerBarColor = centerBarColor;
      this.rightBarColor = rightBarColor;
      this.emptyBarColor = emptyBarColor;
      this.textColor = textColor;
      this.selectionColor = selectionColor;
    }
  }

  private void drawText(final Graphics2D gfx) {
    final Color oldColor = gfx.getColor();

    final Composite oldComposite = gfx.getComposite();
    gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    final Rectangle2D rectLeft = gfx.getFontMetrics(gfx.getFont()).getStringBounds(leftText, gfx);
    Rectangle2D rectTotal = gfx.getFontMetrics(gfx.getFont()).getStringBounds(centerText, gfx);
    final Rectangle2D rectRight = gfx.getFontMetrics(gfx.getFont()).getStringBounds(rightText, gfx);

    final double widthSum =
        4 + rectLeft.getWidth() + 5 + rectTotal.getWidth() + 5 + rectRight.getWidth() + 4;

    int xLeft = 0;
    int xCenter = 0;
    int xRight = 0;

    final int yT = (int) ((height - rectTotal.getHeight()) / 2 + rectTotal.getHeight()) - 1;

    if (widthSum < width) {
      xLeft = 4;
      xCenter = (int) ((width - rectTotal.getWidth()) / 2) + 1;
      xRight = (int) (width - rectRight.getWidth() - 4);

      gfx.setColor(textColor);
      gfx.drawString(leftText, xLeft, yT);

      gfx.drawString(centerText, xCenter, yT);

      gfx.drawString(rightText, xRight, yT);
    } else {
      final String s = "...";
      rectTotal = gfx.getFontMetrics(gfx.getFont()).getStringBounds(s, gfx);
      xCenter = (int) ((width - rectTotal.getWidth()) / 2) + 1;

      gfx.setColor(Color.BLACK);
      gfx.drawString(s, xCenter, yT);
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

    final int total = leftValue + centerValue + rightValue;

    int lengthLeft = 0;
    int lengthCenter = 0;

    if (total != 0) {
      int offset = 0;

      if (leftValue != 0) {
        lengthLeft = (width - 2) * leftValue / total;
        gfx.setColor(leftBarColor);
        gfx.fillRect(xP, yP, centerValue + rightValue == 0. ? width : lengthLeft, height);

        if (lengthLeft > 1) {
          ++offset;
        }
      }

      if (centerValue != 0) {
        lengthCenter = (width - 1) * centerValue / total;
        gfx.setColor(centerBarColor);
        gfx.fillRect(
            xP + lengthLeft + offset,
            yP,
            rightValue == 0. ? width - lengthLeft - offset : lengthCenter,
            height);

        if (lengthCenter > 1) {
          ++offset;
        }
      }

      if (rightValue != 0) {
        gfx.setColor(rightBarColor);
        gfx.fillRect(
            xP + lengthLeft + lengthCenter + offset,
            yP,
            width - lengthLeft - lengthCenter - offset,
            height);
      }

      if (selected) {
        gfx.setColor(selectionColor);

        gfx.drawRect(xP, yP, width, height);

        final Composite oldComposite = gfx.getComposite();
        gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.60f));

        gfx.fillRect(xP, yP, width, height);

        gfx.setComposite(oldComposite);
      } else {
        gfx.setColor(Color.WHITE);
        gfx.drawRect(xP, yP, width, height);
      }

      drawText(gfx);
    } else {
      gfx.setColor(emptyBarColor);
      gfx.fillRect(xP + 1, yP + 1, width - 1, height - 1);

      if (selected) {
        gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .6f));

        gfx.setColor(selectionColor);
        gfx.fillRect(xP, yP, width, height);

        gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.f));
        gfx.drawRect(xP, yP, width, height);
      }

      final Composite oldComposite = gfx.getComposite();
      gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

      final Rectangle2D rect = gfx.getFontMetrics(gfx.getFont()).getStringBounds("0", gfx);

      final int xTotal = (int) ((width - rect.getWidth()) / 2) + 1;
      final int yT = (int) ((height - rect.getHeight()) / 2 + rect.getHeight()) - 1;

      gfx.setColor(textColor);
      gfx.drawString(centerText, xTotal, yT);

      gfx.setComposite(oldComposite);
    }

    gfx.setColor(oldColor);

    g.translate(-xP, -yP);
  }

  public void setWidth(final int width) {
    this.width = width;
  }
}
