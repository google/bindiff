// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.tables;

import com.google.common.base.Strings;
import com.google.security.zynamics.zylib.general.ClipboardHelpers;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JTable;

public class CopySelectionAction extends AbstractAction {
  private final JTable table;

  public CopySelectionAction(final JTable table) {
    super("Copy Selection");
    this.table = table;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final int[] rows = table.getSelectedRows();

    final int cols = table.getColumnCount();

    final int[] maximums = new int[cols];

    for (final int row : rows) {
      for (int i = 0; i < cols; i++) {
        if (table.getValueAt(row, i) != null) {
          final int maximum = table.getValueAt(row, i).toString().length();
          if (maximum > maximums[i]) {
            maximums[i] = maximum;
          }
        }
      }
    }

    final StringBuffer sb = new StringBuffer();

    for (final int row : rows) {
      for (int i = 0; i < cols; i++) {
        if (table.getValueAt(row, i) != null) {
          final String value = table.getValueAt(row, i).toString();

          final int differenceToMaximum = maximums[i] - value.length();

          sb.append(value);
          sb.append(Strings.repeat(" ", differenceToMaximum));

          if (i != (cols - 1)) {
            sb.append("  ");
          }
        }
      }

      sb.append("\n");
    }

    ClipboardHelpers.copyToClipboard(sb.toString());
  }
}
