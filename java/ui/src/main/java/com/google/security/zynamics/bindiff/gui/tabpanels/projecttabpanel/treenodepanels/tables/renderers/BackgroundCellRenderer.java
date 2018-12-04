package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.renderers;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.renderers.BackgroundIcon;
import com.google.security.zynamics.zylib.date.DateHelpers;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Date;
import javax.swing.JTable;

public class BackgroundCellRenderer extends AbstractTableCellRenderer {
  private final Font font;
  private final Color backgroundColor;
  private final Color textColor;
  private final int horizontalAlignment;
  private boolean isInaccessible = false;

  public BackgroundCellRenderer(
      Font font, Color backgroundColor, Color textColor, int horizontalAlignment) {
    this.font = font;
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

    Font font = this.font;
    if (isBoldFont(table, row)) {
      font = font.deriveFont(Font.BOLD);
    }
    setFont(font);

    final String text;
    if (value instanceof String) {
      text = (String) value;
      isInaccessible = text.equals("");
    } else if (value instanceof Double) {
      text = Double.toString((Double) value);
      isInaccessible = (Double) value == -1;
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
            isInaccessible ? NON_ACCESSIBLE_TEXT : text,
            horizontalAlignment,
            textColor,
            isInaccessible ? NON_ACCESSIBLE_COLOR : backgroundColor,
            table.getSelectionBackground(),
            selected,
            0 - 1,
            0,
            table.getColumnModel().getColumn(column).getWidth() - 1,
            table.getRowHeight() - 1));

    return this;
  }
}
