package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.renderers;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.PercentageTwoBarCellData;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.PercentageTwoBarIcon;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;

public class PercentageTwoBarCellRenderer extends AbstractTableCellRenderer {
  private final Color leftBarColor;
  private final Color rightBarColor;
  private final Color emptyBarColor;
  private final Color leftTextColor;
  private final Color totalTextColor;
  private final Color rightTextColor;

  public PercentageTwoBarCellRenderer(
      final Color leftBar,
      final Color rightBar,
      final Color emptyBarColor,
      final Color leftText,
      final Color totalText,
      final Color rightText) {
    leftBarColor = leftBar;
    rightBarColor = rightBar;
    this.emptyBarColor = emptyBarColor;
    leftTextColor = leftText;
    totalTextColor = totalText;
    rightTextColor = rightText;
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

    if (value instanceof PercentageTwoBarCellData) {
      setIcon(
          new PercentageTwoBarIcon(
              (PercentageTwoBarCellData) value,
              leftBarColor,
              rightBarColor,
              emptyBarColor,
              leftTextColor,
              totalTextColor,
              rightTextColor,
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
