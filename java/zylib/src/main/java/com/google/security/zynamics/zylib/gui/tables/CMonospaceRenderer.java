// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.tables;

import com.google.security.zynamics.zylib.gui.GuiHelper;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

/**
 * Can be used to render cells using the default monospaced font.
 */
public class CMonospaceRenderer extends JLabel implements TableCellRenderer {
  private static final Font INSTRUCTION_FONT = GuiHelper.getMonospacedFont();

  private Border unselectedBorder = null;
  private Border selectedBorder = null;

  public CMonospaceRenderer() {
    setOpaque(true); // MUST do this for background to show up.
  }

  @Override
  public Component getTableCellRendererComponent(final JTable table, final Object value,
      final boolean isSelected, final boolean hasFocus, final int row, final int column) {
    setFont(INSTRUCTION_FONT);

    setText(value.toString());

    if (isSelected) {
      setBackground(table.getSelectionBackground());

      if (selectedBorder == null) {
        selectedBorder =
            BorderFactory.createMatteBorder(2, 5, 2, 5, table.getSelectionBackground());
      }

      setBorder(selectedBorder);
    } else {
      setBackground(Color.WHITE);

      if (unselectedBorder == null) {
        unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getBackground());
      }

      setBorder(unselectedBorder);
    }

    return this;
  }

}
