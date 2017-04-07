package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.popup;

import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.CloseCallGraphViewAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.CopySelectionToClipboardAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.CopyValueToClipboardAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.OpenCallGraphViewAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.treenodepanels.tables.AbstractTable;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

public class CallGraphViewsPopupMenu extends JPopupMenu {
  public CallGraphViewsPopupMenu(final AbstractTable table, final int column) {
    final WorkspaceTabPanelFunctions controller = table.getController();

    add(
        GuiUtils.buildMenuItem(
            "Open Call Graph",
            new OpenCallGraphViewAction(controller, table.getDiff()),
            ViewPopupHelper.isEnabled(table, 0, true)));

    add(
        GuiUtils.buildMenuItem(
            "Close Call Graph",
            new CloseCallGraphViewAction(controller, table.getDiff()),
            ViewPopupHelper.isEnabled(table, 0, false)));

    add(new JSeparator());

    add(
        GuiUtils.buildMenuItem(
            "Copy Selection", new CopySelectionToClipboardAction(table), table.hasSelection()));

    add(GuiUtils.buildMenuItem("Copy Value", new CopyValueToClipboardAction(table, 0, column)));
  }
}
