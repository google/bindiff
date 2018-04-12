package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.renderers;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.BackgroundIcon;
import com.google.security.zynamics.zylib.date.DateHelpers;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import java.awt.Color;
import java.awt.Component;
import java.util.Date;
import javax.swing.JTable;

public class BackgroundCellRenderer extends AbstractTableCellRenderer {
  final Color backgroundColor;
  final Color textColor;
  final int horizontalAlignment;
  boolean isNotAccessable = false;

  public BackgroundCellRenderer(
      final Color backgroundColor, final Color textColor, final int horizontalAlignment) {
    this.backgroundColor = backgroundColor;
    this.textColor = textColor;
    this.horizontalAlignment = horizontalAlignment;
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

    final String text;
    if (value instanceof String) {
      text = (String) value;
      isNotAccessable = text.equals("");
    } else if (value instanceof Double) {
      text = Double.toString((Double) value);
      isNotAccessable = (Double) value == -1;
    } else if (value instanceof Integer) {
      text = Integer.toString((Integer) value);
    } else if (value instanceof IAddress) {
      text = ((IAddress) value).toHexString();
    } else if (value instanceof Date) {
      text = DateHelpers.formatDateTime((Date) value);
    } else {
      throw new IllegalArgumentException(
          String.format(
              "Value must be a String, IAddress, Double Integer or Date. (%s)",
              value.getClass().toString()));
    }

    setIcon(
        new BackgroundIcon(
            isNotAccessable ? NON_ACCESSIBLE_TEXT : text,
            horizontalAlignment,
            textColor,
            isNotAccessable ? NON_ACCESSIBLE_COLOR : backgroundColor,
            table.getSelectionBackground(),
            selected,
            0 - 1,
            0,
            table.getColumnModel().getColumn(column).getWidth() - 1,
            table.getRowHeight() - 1));

    return this;
  }
}
