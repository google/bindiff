package com.google.security.zynamics.bindiff.gui.dialogs.directorydiff;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class CheckboxCellRenderer extends JCheckBox implements TableCellRenderer {
  public CheckboxCellRenderer() {
    setHorizontalAlignment(JLabel.CENTER);
  }

  @Override
  public Component getTableCellRendererComponent(
      final JTable table,
      final Object value,
      final boolean isSelected,
      final boolean hasFocus,
      final int row,
      final int column) {
    if (isSelected) {
      setForeground(table.getSelectionForeground());
      setBackground(table.getSelectionBackground());
    } else {
      setForeground(table.getForeground());
      setBackground(table.getBackground());
    }

    setSelected((value != null && ((JCheckBox) value).isSelected()));

    ((JCheckBox) value).setHorizontalAlignment(JLabel.CENTER);

    return this;
  }
}
