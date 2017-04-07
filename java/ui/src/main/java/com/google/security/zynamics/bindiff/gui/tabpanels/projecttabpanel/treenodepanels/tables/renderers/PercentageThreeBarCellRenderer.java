package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.renderers;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.PercentageThreeBarCellData;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.PercentageThreeBarIcon;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;

public class PercentageThreeBarCellRenderer extends AbstractTableCellRenderer {
  private final Color leftBarColor;
  private final Color centerBarColor;
  private final Color rightBarColor;
  private final Color emptyBarColor;
  private final Color textColor;

  public PercentageThreeBarCellRenderer(
      final Color leftBarColor,
      final Color centerBarColor,
      final Color rightBarColor,
      final Color emptyBarColor,
      final Color textColor) {
    this.leftBarColor = leftBarColor;
    this.centerBarColor = centerBarColor;
    this.rightBarColor = rightBarColor;
    this.emptyBarColor = emptyBarColor;
    this.textColor = textColor;
  }

  @Override
  public Component getTableCellRendererComponent(
      final JTable table,
      final Object value,
      final boolean selected,
      final boolean focused,
      final int row,
      final int column) {
    buildAndSetToolTip(table, row);

    setFont(!isBoldFont(table, row) ? NORMAL_FONT : BOLD_FONT);

    if (value instanceof PercentageThreeBarCellData) {
      setIcon(
          new PercentageThreeBarIcon(
              (PercentageThreeBarCellData) value,
              leftBarColor,
              centerBarColor,
              rightBarColor,
              emptyBarColor,
              textColor,
              table.getSelectionBackground(),
              selected,
              0 - 1,
              0,
              table.getColumnModel().getColumn(column).getWidth() - 2,
              table.getRowHeight() - 2));
    }

    return this;
  }
}
