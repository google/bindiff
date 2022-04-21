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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import javax.swing.Icon;

public class PercentageTwoBarExtendedIcon implements Icon {
  private String leftText;
  private String totalText;
  private String rightText;

  private int leftValue;
  private int innerLeftValue;
  private int rightValue;

  private int width;
  private final int height;
  private final int xOffset;
  private final int yOffset;

  private final Color leftBarColor;
  private final Color innerLeftBarColor;
  private final Color rightBarColor;
  private final Color emptyBarColor;
  private final Color textColor;

  public PercentageTwoBarExtendedIcon(
      final PercentageTwoBarExtendedCellData data,
      final Color leftBarColor,
      final Color innerLeftBarColor,
      final Color rightBarColor,
      final Color emptyBarColor,
      final Color textColor,
      final int xOffset,
      final int yOffset,
      final int width,
      final int height) {
    totalText = Integer.toString(data.getTotalBarValue());
    leftText = data.getLeftBarString(true, true);
    rightText = data.getRightBarString(true);

    leftValue = data.getLeftBarValue();
    innerLeftValue = data.getInnerLeftBarValue();
    rightValue = data.getRightBarValue();

    this.width = width;
    this.height = height;
    this.xOffset = xOffset;
    this.yOffset = yOffset;
    this.leftBarColor = leftBarColor;
    this.innerLeftBarColor = innerLeftBarColor;
    this.rightBarColor = rightBarColor;
    this.emptyBarColor = emptyBarColor;
    this.textColor = textColor;
  }

  private void drawText(final Graphics2D gfx) {
    final Color oldColor = gfx.getColor();

    final Composite oldComposite = gfx.getComposite();
    gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    final Rectangle2D rectLeft =
        gfx.getFontMetrics(gfx.getFont()).getStringBounds(leftText + "----", gfx);
    Rectangle2D rectTotal = gfx.getFontMetrics(gfx.getFont()).getStringBounds(totalText, gfx);
    final Rectangle2D rectRight = gfx.getFontMetrics(gfx.getFont()).getStringBounds(rightText, gfx);

    final double widthSum =
        4 + rectLeft.getWidth() + 5 + rectTotal.getWidth() + 5 + rectRight.getWidth() + 4;

    int xLeft = 0;
    int xTotal = 0;
    int xRight = 0;

    final int yT = (int) ((height - rectTotal.getHeight()) / 2 + rectTotal.getHeight()) - 1;

    if (widthSum < width) {
      xLeft = 4;
      xTotal = (int) ((width - rectTotal.getWidth()) / 2) + 1;
      xRight = (int) (width - rectRight.getWidth() - 4);

      gfx.setColor(textColor);
      gfx.drawString(leftText, xLeft, yT);

      gfx.setColor(textColor);
      gfx.drawString(totalText, xTotal, yT);

      gfx.setColor(textColor);
      gfx.drawString(rightText, xRight, yT);
    } else {
      final String s = "...";
      rectTotal = gfx.getFontMetrics(gfx.getFont()).getStringBounds(s, gfx);
      xTotal = (int) ((width - rectTotal.getWidth()) / 2) + 1;

      gfx.setColor(Color.BLACK);
      gfx.drawString(s, xTotal, yT);
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

    final int total = leftValue + rightValue;
    if (total != 0) {
      int lengthLeft = 0;
      if (leftValue != 0) {
        lengthLeft = (width - 2) * leftValue / total;

        gfx.setColor(leftBarColor);
        gfx.fillRect(xP, yP, rightValue == 0. ? width : lengthLeft, height);

        int lengthInnerLeft = (width - 2) * innerLeftValue / total;

        lengthInnerLeft -= 2;

        if (lengthInnerLeft + 5 > lengthLeft) {
          lengthInnerLeft = lengthLeft - 14;
        }

        if (lengthInnerLeft > 0) {

          gfx.setColor(innerLeftBarColor);
          gfx.fillRect(
              xP + 5, yP + 5, rightValue == 0. ? width - 4 : lengthInnerLeft - 2, height - 7);
        }
      }

      if (rightValue != 0) {
        gfx.setColor(rightBarColor);
        gfx.fillRect(xP + lengthLeft + 1, yP, width - lengthLeft - 1, height);
      }

      gfx.setColor(Color.WHITE);
      gfx.drawRect(xP, yP, width, height);

      drawText(gfx);
    } else {
      gfx.setColor(emptyBarColor);
      gfx.fillRect(xP + 1, yP + 1, width, height);

      final Composite oldComposite = gfx.getComposite();
      gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

      final Rectangle2D rect = gfx.getFontMetrics(gfx.getFont()).getStringBounds("0", gfx);

      final int xTotal = (int) ((width - rect.getWidth()) / 2) + 1;
      final int yT = (int) ((height - rect.getHeight()) / 2 + rect.getHeight()) - 1;

      gfx.setColor(textColor);
      gfx.drawString(totalText, xTotal, yT);

      gfx.setComposite(oldComposite);
    }

    gfx.setColor(oldColor);

    g.translate(-xP, -yP);
  }

  public void setWidth(final int width) {
    this.width = width;
  }

  public void updateData(final int leftValue, final int innerLeftValue, final int rightValue) {
    final PercentageTwoBarExtendedCellData data =
        new PercentageTwoBarExtendedCellData(leftValue, innerLeftValue, rightValue);

    totalText = Integer.toString(data.getTotalBarValue());
    leftText = data.getLeftBarString(true, true);
    rightText = data.getRightBarString(true);

    this.leftValue = data.getLeftBarValue();
    this.innerLeftValue = data.getInnerLeftBarValue();
    this.rightValue = data.getRightBarValue();
  }
}
