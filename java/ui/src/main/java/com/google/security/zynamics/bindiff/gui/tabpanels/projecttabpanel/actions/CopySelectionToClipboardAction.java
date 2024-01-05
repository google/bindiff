// Copyright 2011-2024 Google LLC
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

package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.ExtendedMatchedFunctionViewsTableModel;
import com.google.security.zynamics.zylib.general.ClipboardHelpers;
import com.google.security.zynamics.zylib.gui.tables.CTableSorter;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class CopySelectionToClipboardAction extends AbstractAction {
  private final AbstractTable table;

  public CopySelectionToClipboardAction(final AbstractTable table) {
    this.table = checkNotNull(table);
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final StringBuilder values = new StringBuilder();

    final CTableSorter model = table.getModel();
    for (int row : table.getSelectedRows()) {
      for (int column = 0; column < model.getColumnCount(); ++column) {
        if (column > 0) {
          values.append("\t");
        }

        final Object obj = table.getModel().getValueAt(row, column);
        if (obj == ExtendedMatchedFunctionViewsTableModel.ADDED_ICON) {
          values.append("+");
        } else if (obj == ExtendedMatchedFunctionViewsTableModel.REMOVED_ICON) {
          values.append("-");
        } else {
          values.append(obj.toString());
        }
      }

      values.append("\n");
    }

    ClipboardHelpers.copyToClipboard(values.toString());
  }
}
