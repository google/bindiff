package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.popupmenu;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.AddDiffAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.NewDiffAction;
import com.google.security.zynamics.bindiff.project.Workspace;
import com.google.security.zynamics.bindiff.project.WorkspaceAdapter;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class NoNodePopupMenu extends JPopupMenu {
  private final WorkspaceTabPanelFunctions controller;

  private final JMenuItem newDiff;
  private final JMenuItem addDiff;

  private final InternalWorkspaceListener workspaceListener = new InternalWorkspaceListener();

  public NoNodePopupMenu(final WorkspaceTabPanelFunctions controller) {
    this.controller = Preconditions.checkNotNull(controller);

    newDiff = GuiUtils.buildMenuItem("New Diff...", 'N', new NewDiffAction(controller));

    addDiff = GuiUtils.buildMenuItem("Add Existing Diff...", 'A', new AddDiffAction(controller));

    add(newDiff);
    add(addDiff);

    enableMenu(false);

    controller.getWorkspace().addListener(workspaceListener);
  }

  private void enableMenu(final boolean enable) {
    newDiff.setEnabled(enable);
    addDiff.setEnabled(enable);
  }

  public void dispose() {
    controller.getWorkspace().addListener(workspaceListener);
  }

  private class InternalWorkspaceListener extends WorkspaceAdapter {
    @Override
    public void closedWorkspace() {
      enableMenu(false);
    }

    @Override
    public void loadedWorkspace(final Workspace workspace) {
      enableMenu(true);
    }
  }
}
