// Copyright 2011-2023 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.zylib.gui.tables;

import com.google.common.base.Preconditions;
import java.awt.Window;
import javax.swing.JOptionPane;
import javax.swing.JTable;

/** Class that can extend the popup menu of a JTable component with a Search option. */
public class CTableSearcher {
  private final JTable m_Table;
  private final Window m_Frame;
  private int m_StartRow = 0;
  private final String m_title;

  /**
   * Extends a popup menu of a JTable component with a Search menu.
   *
   * @param frame the parent frame that is used as the parent frame of the input box.
   * @param windowTitle the title of the input box
   * @param table the table to search through
   */
  public CTableSearcher(
      final Window frame, final String windowTitle, final JTable table, final int startRow) {
    Preconditions.checkNotNull(frame, "Internal Error: Parent window can't be null");

    Preconditions.checkNotNull(windowTitle, "Internal Error: Window title can't be null");

    Preconditions.checkNotNull(table, "Internal Error: Table can't be null");

    m_Table = table;
    m_Frame = frame;
    m_title = windowTitle;

    m_StartRow = startRow;
  }

  public void search() {
    String searchText = "";

    do {
      m_Frame.repaint();
      // searchText = JOptionPane.showInputDialog( m_Frame, "Search: ", searchText );

      searchText =
          (String)
              JOptionPane.showInputDialog(
                  m_Frame, "Search", m_title, JOptionPane.QUESTION_MESSAGE, null, null, searchText);

      if ((searchText != null) && (searchText.length() > 0)) {
        if (!search(searchText)) {
          JOptionPane.showMessageDialog(
              m_Frame, "Search string not found", m_title, JOptionPane.ERROR_MESSAGE);
        }
      }
    } while ((searchText != null) && (searchText.length() > 0));
  }

  public boolean search(final String searchText) {
    final int nrOfColumns = m_Table.getModel().getColumnCount();
    final int nrOfRows = m_Table.getRowCount();
    for (int row = 0; row < nrOfRows; ++row) {
      for (int column = 0; column < nrOfColumns; ++column) {
        final Object cell = m_Table.getModel().getValueAt((row + m_StartRow) % nrOfRows, column);
        final String text = cell != null ? cell.toString() : "";
        // TODO make this a config option, i.e. search case sensitive
        if (text.toLowerCase().contains(searchText.toLowerCase())) {
          m_StartRow = (row + m_StartRow) % nrOfRows;
          m_Table.setRowSelectionInterval(m_StartRow, m_StartRow);
          m_Table.scrollRectToVisible(m_Table.getCellRect(m_StartRow, 0, true));
          ++m_StartRow;

          return true;
        }
      }
    }

    return false;
  }
}
