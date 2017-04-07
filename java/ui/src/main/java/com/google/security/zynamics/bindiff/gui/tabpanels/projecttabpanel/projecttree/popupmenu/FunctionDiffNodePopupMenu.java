package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.popupmenu;

import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanelManager;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.CloseFunctionDiffsViewsAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.DeleteFunctionDiffViewsAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.treenodes.FunctionDiffViewsNode;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.FunctionDiffViewsNodeContextPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.FunctionDiffViewsTable;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanel;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

public class FunctionDiffNodePopupMenu extends JPopupMenu {
  public FunctionDiffNodePopupMenu(final FunctionDiffViewsNode viewsNode) {
    final FunctionDiffViewsNodeContextPanel component = viewsNode.getComponent();
    final FunctionDiffViewsTable table = (FunctionDiffViewsTable) component.getTables().get(0);

    add(
        GuiUtils.buildMenuItem(
            "Delete Function Diffs", new DeleteFunctionDiffViewsAction(viewsNode)));

    add(new JSeparator());

    add(
        GuiUtils.buildMenuItem(
            "Close Function Diff Views",
            new CloseFunctionDiffsViewsAction(table),
            isCloseViewsEnabled(table)));
  }

  private boolean isCloseViewsEnabled(final FunctionDiffViewsTable table) {
    final WorkspaceTabPanelFunctions controller = table.getController();
    final TabPanelManager tabPanelManager =
        controller.getMainWindow().getController().getTabPanelManager();

    final Set<Diff> diffsSet = new HashSet<>();
    for (final ViewTabPanel panel : tabPanelManager.getViewTabPanels(true)) {
      diffsSet.add(panel.getDiff());
    }

    for (int row = 0; row < table.getRowCount(); ++row) {
      if (diffsSet.contains(AbstractTable.getRowDiff(table, row))) {
        return true;
      }
    }

    return false;
  }
}
