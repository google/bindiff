package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.popup;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.CloseViewsAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.CopySelectionToClipboardAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.CopyValueToClipboardAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.DeleteFunctionDiffViewAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.OpenMultipeFlowGraphsViewsAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

public class FunctionDiffFlowGraphsViewTablePopupMenu extends JPopupMenu {
  public FunctionDiffFlowGraphsViewTablePopupMenu(
      final AbstractTable table, final int hitRowIndex, final int hitColumnIndex) {
    add(
        GuiUtils.buildMenuItem(
            "Open Function Diff Views",
            new OpenMultipeFlowGraphsViewsAction(table),
            ViewPopupHelper.isEnabled(table, hitRowIndex, true)));

    add(
        GuiUtils.buildMenuItem(
            "Close Function Diff Views",
            new CloseViewsAction(table, hitRowIndex),
            ViewPopupHelper.isEnabled(table, hitRowIndex, false)));

    add(new JSeparator());

    add(
        GuiUtils.buildMenuItem(
            "Delete Function Diff View", new DeleteFunctionDiffViewAction(table, hitRowIndex)));

    add(new JSeparator());

    add(
        GuiUtils.buildMenuItem(
            "Copy Selection", new CopySelectionToClipboardAction(table), table.hasSelection()));

    add(
        GuiUtils.buildMenuItem(
            "Copy Value", new CopyValueToClipboardAction(table, hitRowIndex, hitColumnIndex)));
  }
}
