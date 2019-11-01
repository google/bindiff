package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.Triple;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import java.awt.event.ActionEvent;
import java.util.LinkedHashSet;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

public class OpenMultipeFlowGraphsViewsAction extends AbstractAction {
  private static final int OPEN_VIEWS_WARNING_THRESHOLD = 10;

  private final AbstractTable table;

  public OpenMultipeFlowGraphsViewsAction(final AbstractTable table) {
    this.table = Preconditions.checkNotNull(table);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final WorkspaceTabPanelFunctions controller = table.getController();
    final LinkedHashSet<Triple<Diff, IAddress, IAddress>> viewsLoadData = new LinkedHashSet<>();

    final int[] selectedRowIndicies = table.getSelectedRows();

    final LinkedHashSet<Diff> diffsToLoad = new LinkedHashSet<>();
    for (int index = 0; index < selectedRowIndicies.length; ++index) {
      final int selectedRow = selectedRowIndicies[index];
      final Diff diff = AbstractTable.getRowDiff(table, selectedRow);

      if (!diff.isLoaded()) {
        diffsToLoad.add(diff);
      }
    }

    controller.loadFunctionDiffs(diffsToLoad);

    for (int index = 0; index < selectedRowIndicies.length; ++index) {
      final int selectedRow = selectedRowIndicies[index];
      final Diff diff = AbstractTable.getRowDiff(table, selectedRow);

      final Pair<IAddress, IAddress> viewAddrPair =
          AbstractTable.getViewAddressPair(table, selectedRow);

      viewsLoadData.add(Triple.make(diff, viewAddrPair.first(), viewAddrPair.second()));
    }

    int answer = JOptionPane.YES_OPTION;
    if (viewsLoadData.size() > OPEN_VIEWS_WARNING_THRESHOLD) {
      answer =
          CMessageBox.showYesNoQuestion(
              controller.getMainWindow(),
              String.format(
                  "This operation will open more than %d views. Continue?",
                  OPEN_VIEWS_WARNING_THRESHOLD));
    }

    if (answer == JOptionPane.YES_OPTION) {
      controller.openFlowGraphViews(controller.getMainWindow(), viewsLoadData);
    }
  }
}
