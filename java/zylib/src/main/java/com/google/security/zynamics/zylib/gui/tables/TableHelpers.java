// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.tables;

import java.awt.event.MouseEvent;

import javax.swing.JTable;

/**
 * Contains helper functions for common JTable operations.
 */
public class TableHelpers {
  /**
   * Converts between sorted row indices and raw row indices of the underlying data provider.
   * 
   * @param sorter Table sorter used the conversion.
   * @param sortedRows Array of sorted row indices.
   * 
   * @return Array of corresponding raw row indices.
   */
  @Deprecated
  public static int[] normalizeRows(final CTableSorter sorter, final int[] sortedRows) {
    final int[] rawRows = new int[sortedRows.length];

    for (int i = 0; i < sortedRows.length; i++) {
      rawRows[i] = sorter.modelIndex(sortedRows[i]);
    }

    return rawRows;
  }

  /**
   * Selects a table row depending on a mouse event.
   * 
   * @param table The table.
   * @param event The mouse event.
   */
  public static void selectClickedRow(final JTable table, final MouseEvent event) {
    final int row = table.rowAtPoint(event.getPoint());

    if (row == -1) {
      return;
    }

    table.setRowSelectionInterval(row, row);
  }
}
