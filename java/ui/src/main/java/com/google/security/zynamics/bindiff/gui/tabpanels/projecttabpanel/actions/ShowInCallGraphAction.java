package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;

public class ShowInCallGraphAction extends AbstractAction {
  private final AbstractTable table;

  public ShowInCallGraphAction(final AbstractTable table) {
    this.table = Preconditions.checkNotNull(table);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final Set<Pair<IAddress, IAddress>> viewAddrPairs = new HashSet<>();
    for (final int row : table.getSelectedRows()) {
      viewAddrPairs.add(AbstractTable.getViewAddressPair(table, row));
    }

    final Diff diff = table.getDiff();
    table.getController().showInCallGraph(diff, viewAddrPairs);
  }
}
