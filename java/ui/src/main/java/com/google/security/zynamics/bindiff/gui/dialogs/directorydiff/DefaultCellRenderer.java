package com.google.security.zynamics.bindiff.gui.dialogs.directorydiff;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class DefaultCellRenderer extends DefaultTableCellRenderer {
  @Override
  public Component getTableCellRendererComponent(
      final JTable table,
      final Object value,
      final boolean isSelected,
      final boolean hasFocus,
      final int row,
      final int column) {
    return super.getTableCellRendererComponent(table, value, false, false, row, column);
  }
}
