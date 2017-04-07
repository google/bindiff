package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.ExtendedMatchedFunctionViewsTableModel;
import com.google.security.zynamics.zylib.general.ClipboardHelpers;
import com.google.security.zynamics.zylib.gui.tables.CTableSorter;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class CopySelectionToClipboardAction extends AbstractAction {
  private final AbstractTable table;

  public CopySelectionToClipboardAction(final AbstractTable table) {
    this.table = Preconditions.checkNotNull(table);
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
