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
import javax.swing.Icon;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import sun.swing.SwingUtilities2;

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
    tabInsets = new Insets(4, 8, 4, 8);
    selectedTabPadInsets = tabInsets;
    tabOverlap = tabInsets.right;
    tabAreaInsets = new Insets(4, 4, 0, 4);
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

    final int[] xPoints =
        new int[] {
          tabRect.x,
          tabRect.x + tabInsets.left,
          tabRect.x + tabRect.width - tabInsets.right,
          tabRect.x + tabRect.width
        };
    final int[] yPoints =
        new int[] {
          tabRect.y + tabRect.height, tabRect.y, tabRect.y, tabRect.y + tabRect.height,
        };
    Color tabColor = tabPane.getBackgroundAt(tabIndex);
    if (!isSelected) {
      tabColor = darker(tabColor);
    }
    g2d.setColor(tabColor);
    g2d.fillPolygon(xPoints, yPoints, xPoints.length);
    g2d.setColor(darkShadow);
    g2d.drawPolygon(xPoints, yPoints, xPoints.length);

    final String title = tabPane.getTitleAt(tabIndex);
    final Font font = tabPane.getFont();
    final FontMetrics metrics = SwingUtilities2.getFontMetrics(tabPane, g, font);
    final Icon icon = getIconForTab(tabIndex);

    layoutLabel(TOP, metrics, tabIndex, title, icon, tabRect, iconRect, textRect, isSelected);

    if (tabPane.getTabComponentAt(tabIndex) == null) {
      final String clippedTitle =
          SwingUtilities2.clipStringIfNecessary(null, metrics, title, textRect.width);
      paintText(g, TOP, font, metrics, tabIndex, clippedTitle, textRect, isSelected);
      paintIcon(g, TOP, tabIndex, icon, iconRect, isSelected);
    }
  }
}
