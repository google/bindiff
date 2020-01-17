// Copyright 2011-2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
