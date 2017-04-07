package com.google.security.zynamics.bindiff.gui.dialogs.directorydiff;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;

public class CheckboxCellEditor extends DefaultCellEditor implements ItemListener {
  private JCheckBox checkbox;

  public CheckboxCellEditor() {
    super(new JCheckBox());
  }

  @Override
  public Object getCellEditorValue() {
    checkbox.removeItemListener(this);

    return checkbox;
  }

  @Override
  public Component getTableCellEditorComponent(
      final JTable table,
      final Object value,
      final boolean isSelected,
      final int row,
      final int column) {
    if (value == null) {
      return null;
    }

    checkbox = (JCheckBox) value;
    checkbox.addItemListener(this);

    return (Component) value;
  }

  @Override
  public void itemStateChanged(final ItemEvent e) {
    super.fireEditingStopped();
  }
}
