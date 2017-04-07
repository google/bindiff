package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.ExtendedMatchedFunctionViewsTableModel;
import com.google.security.zynamics.zylib.general.ClipboardHelpers;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class CopyValueToClipboardAction extends AbstractAction {
  private final AbstractTable table;
  private final int hitRowIndex;
  private final int hitColumnIndex;

  public CopyValueToClipboardAction(
      final AbstractTable table, final int hitRowIndex, final int hitColumnIndex) {
    this.table = Preconditions.checkNotNull(table);
    this.hitRowIndex = hitRowIndex;
    this.hitColumnIndex = hitColumnIndex;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    String value = "";
    final Object obj = table.getModel().getValueAt(hitRowIndex, hitColumnIndex);
    if (obj == ExtendedMatchedFunctionViewsTableModel.ADDED_ICON) {
      value = "+";
    } else if (obj == ExtendedMatchedFunctionViewsTableModel.REMOVED_ICON) {
      value = "-";
    } else {
      value = table.getModel().getValueAt(hitRowIndex, hitColumnIndex).toString();
    }

    ClipboardHelpers.copyToClipboard(value);
  }
}
