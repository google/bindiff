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

package com.google.security.zynamics.bindiff.gui.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import javax.swing.Icon;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/**
 * A custom UI class for tabbed panes that provides a look and feel roughly similar to the Google
 * Chrome Material Design tabs.
 */
public class MaterialChromeTabbedPaneUI extends BasicTabbedPaneUI {

  protected int tabOverlap = 0;

  /** A tabbed layout that applies a small overlap to the tabs. */
  protected class MaterialChromeTabbedPaneLayout extends TabbedPaneLayout {
    @Override
    protected void padSelectedTab(int tabPlacement, int selectedIndex) {
      /* Do nothing */
    }

    @Override
    protected void calculateTabRects(int tabPlacement, int tabCount) {
      super.calculateTabRects(TOP, tabCount);
      for (int i = 0; i < tabCount; i++) {
        rects[i].x -= i * tabOverlap;
      }
    }
  }

  @Override
  protected void installDefaults() {
    super.installDefaults();
    tabInsets = new Insets(4, 20, 4, 16);
    selectedTabPadInsets = tabInsets;
    tabOverlap = tabInsets.right;
    tabAreaInsets = new Insets(4, 0, 0, 0);
    contentBorderInsets = new Insets(0, 0, 0, 0);
  }

  @Override
  protected LayoutManager createLayoutManager() {
    return new MaterialChromeTabbedPaneLayout();
  }

  @Override
  protected int getTabLabelShiftX(int tabPlacement, int tabIndex, boolean isSelected) {
    return 0;
  }

  @Override
  protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected) {
    return 0;
  }

  @Override
  protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
    final Rectangle iconRect = new Rectangle();
    final Rectangle textRect = new Rectangle();
    final int tabCount = tabPane.getTabCount();

    // Paint tabRuns of tabs from back to front
    for (int i = runCount - 1; i >= 0; i--) {
      final int start = tabRuns[i];
      final int next = tabRuns[(i == runCount - 1) ? 0 : i + 1];
      final int end = (next != 0 ? next - 1 : tabCount - 1);
      for (int j = end; j >= start; j--) {
        if (j != selectedIndex) {
          paintTab(g, rects, j, iconRect, textRect);
        }
      }
    }
    // Always paint selected tab last, as it overlaps the others.
    if (selectedIndex >= 0) {
      paintTab(g, rects, selectedIndex, iconRect, textRect);
    }
  }

  private static Color darker(final Color c) {
    final double factor = 0.9;
    return new Color(
        (int) (c.getRed() * factor),
        (int) (c.getGreen() * factor),
        (int) (c.getBlue() * factor),
        c.getAlpha());
  }

  /**
   * Paints a single tab.
   *
   * @param g the graphics object to use for rendering
   * @param rects an array with pre-calculated tab metrics
   * @param tabIndex the index of the tab to paint
   * @param iconRect the bounds for an optional icon
   * @param textRect the bounds for the tab label, if applicable
   */
  protected void paintTab(
      final Graphics g,
      final Rectangle[] rects,
      final int tabIndex,
      final Rectangle iconRect,
      final Rectangle textRect) {
    Rectangle tabRect = rects[tabIndex];
    int selectedIndex = tabPane.getSelectedIndex();
    boolean isSelected = selectedIndex == tabIndex;

    final Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHints(
        new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));

    final GeneralPath path = new GeneralPath();
    path.moveTo(tabRect.x, tabRect.y + tabRect.height); // Bottom left
    path.curveTo(
        tabRect.x,
        tabRect.y + tabRect.height,
        tabRect.x + 8,
        tabRect.y + tabRect.height,
        tabRect.x + 8,
        tabRect.y + tabRect.height - 8);
    path.lineTo(tabRect.x + 8, tabRect.y + 8); // Left line
    path.curveTo(
        tabRect.x + 8,
        tabRect.y + 8,
        tabRect.x + 8,
        tabRect.y,
        tabRect.x + 16,
        tabRect.y); // Top left
    path.lineTo(tabRect.x + tabRect.width - 16, tabRect.y); // Top line
    path.curveTo(
        tabRect.x + tabRect.width - 16,
        tabRect.y,
        tabRect.x + tabRect.width - 8,
        tabRect.y,
        tabRect.x + tabRect.width - 8,
        tabRect.y + 8); // Top right
    path.lineTo(tabRect.x + tabRect.width - 8, tabRect.y + tabRect.height - 8); // Right line
    path.curveTo(
        tabRect.x + tabRect.width - 8,
        tabRect.y + tabRect.height - 8,
        tabRect.x + tabRect.width - 8,
        tabRect.y + tabRect.height,
        tabRect.x + tabRect.width,
        tabRect.y + tabRect.height); // Bottom right
    path.closePath(); // Bottom line

    Color tabColor = tabPane.getBackgroundAt(tabIndex);
    if (!isSelected) {
      tabColor = darker(tabColor);
    }
    g2d.setColor(tabColor);
    g2d.fill(path);

    g2d.setColor(darkShadow);
    if (isSelected) {
      g2d.draw(path);
    } else if (tabIndex > 0) {
      g2d.drawLine(
          tabRect.x + 8, tabRect.y + 8, tabRect.x + 8, tabRect.y + tabRect.height - 8); // Left line
    }

    final String title = tabPane.getTitleAt(tabIndex);
    final Font font = tabPane.getFont();
    final FontMetrics metrics = g.getFontMetrics(font);
    final Icon icon = getIconForTab(tabIndex);

    if (tabPane.getTabComponentAt(tabIndex) == null) {
      layoutLabel(TOP, metrics, tabIndex, title, icon, tabRect, iconRect, textRect, isSelected);
      final String clippedTitle =
          BasicGraphicsUtils.getClippedString(null, metrics, title, textRect.width);
      paintText(g, TOP, font, metrics, tabIndex, clippedTitle, textRect, isSelected);
      paintIcon(g, TOP, tabIndex, icon, iconRect, isSelected);
    }
  }
}
