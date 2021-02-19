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

public class PercentageTwoBarIcon implements Icon {
  private String leftText;
  private String totalText;
  private String rightText;

  private int leftValue;
  private int rightValue;

  private int width;
  private final int height;
  private final int xOffset;
  private final int yOffset;

  private final Color leftBarColor;
  private final Color rightBarColor;
  private final Color emptyBarColor;
  private final Color leftTextColor;
  private final Color totalTextColor;
  private final Color rightTextColor;
  private final Color selectionColor;

  private final boolean selected;

  private boolean showAdditionalPercetageValues = false;

  public PercentageTwoBarIcon(
      final PercentageTwoBarCellData data,
      final Color leftBar,
      final Color rightBar,
      final Color emptyBar,
      final Color leftText,
      final Color totalText,
      final Color rightText,
      final Color selectionColor,
      final boolean selected,
      final int xOffset,
      final int yOffset,
      final int width,
      final int height) {
    this.totalText = Integer.toString(data.getTotalBarValue());
    this.leftText = Integer.toString(data.getLeftBarValue());
    this.rightText = Integer.toString(data.getRightBarValue());
    this.leftValue = data.getLeftBarValue();
    this.rightValue = data.getRightBarValue();
    this.selected = selected;
    this.width = width;
    this.height = height;
    this.xOffset = xOffset;
    this.yOffset = yOffset;
    this.leftBarColor = leftBar;
    this.rightBarColor = rightBar;
    this.emptyBarColor = emptyBar;
    this.leftTextColor = leftText;
    this.totalTextColor = totalText;
    this.rightTextColor = rightText;
    this.selectionColor = selectionColor;
  }

  private void buildTexts() {
    leftText = Integer.toString(leftValue);
    rightText = Integer.toString(rightValue);
    if (showAdditionalPercetageValues && leftValue + rightValue > 0) {
      final double pl = leftValue / (double) (leftValue + rightValue) * 100.;
      final double pr = 100. - pl;
      leftText += String.format(" (%.1f%s)", pl, "%");
      rightText = String.format("(%.1f%s) %s", pr, "%", rightText);
    }
  }

  private void drawText(final Graphics2D gfx) {
    final Color oldColor = gfx.getColor();

    final Composite oldComposite = gfx.getComposite();
    gfx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    final Rectangle2D rectLeft = gfx.getFontMetrics(gfx.getFont()).getStringBounds(leftText, gfx);
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

      gfx.setColor(leftTextColor);
      gfx.drawString(leftText, xLeft, yT);

      gfx.setColor(totalTextColor);
      gfx.drawString(totalText, xTotal, yT);

      gfx.setColor(rightTextColor);
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

    if (leftValue + rightValue != 0) {
      final int lengthLeft = (width - 2) * leftValue / (leftValue + rightValue);
      gfx.setColor(leftBarColor);
      gfx.fillRect(xP, yP, rightValue == 0. ? width : lengthLeft, height);

      if (rightValue != 0) {
        gfx.setColor(rightBarColor);
        gfx.fillRect(xP + lengthLeft + 1, yP, width - lengthLeft - 1, height);
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
      gfx.fillRect(xP + 1, yP + 1, width, height);

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

      gfx.setColor(totalTextColor);
      gfx.drawString(totalText, xTotal, yT);

      gfx.setComposite(oldComposite);
    }

    gfx.setColor(oldColor);

    g.translate(-xP, -yP);
  }

  public void setWidth(final int width) {
    this.width = width;
  }

  public void showAdditionalPercetageValues(final boolean show) {
    showAdditionalPercetageValues = show;

    buildTexts();
  }

  public void updateData(final int leftValue, final int rightValue) {
    final PercentageTwoBarCellData data = new PercentageTwoBarCellData(leftValue, rightValue);

    totalText = Integer.toString(data.getTotalBarValue());
    leftText = Integer.toString(data.getLeftBarValue());
    rightText = Integer.toString(data.getRightBarValue());
    this.leftValue = data.getLeftBarValue();
    this.rightValue = data.getRightBarValue();
  }
}
