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

import com.google.security.zynamics.zylib.general.ClipboardHelpers;

import java.awt.Point;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JTable;


public class CopyCellAction extends AbstractAction {
  private static final long serialVersionUID = 7553173268247700514L;

  private final JTable m_table;

  private final int m_x;

  private final int m_y;

  public CopyCellAction(final JTable table, final int x, final int y) {
    super("Copy Cell");

    m_table = table;
    m_x = x;
    m_y = y;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final int col = m_table.columnAtPoint(new Point(m_x, m_y));
    final int row = m_table.rowAtPoint(new Point(m_x, m_y));

    if ((col == -1) || (row == -1)) {
      return;
    }

    ClipboardHelpers.copyToClipboard(m_table.getValueAt(row, col).toString());
  }
}
