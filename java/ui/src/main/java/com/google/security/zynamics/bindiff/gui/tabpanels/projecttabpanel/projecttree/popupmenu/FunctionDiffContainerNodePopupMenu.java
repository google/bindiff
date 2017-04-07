package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.projecttree.popupmenu;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanelManager;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.CloseFunctionDiffsAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.CloseFunctionDiffsViewsAction;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions.LoadFunctionDiffsAction;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.utils.GuiUtils;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

public class FunctionDiffContainerNodePopupMenu extends JPopupMenu {
  public FunctionDiffContainerNodePopupMenu(final WorkspaceTabPanelFunctions controller) {
    Preconditions.checkNotNull(controller);

    add(
        GuiUtils.buildMenuItem(
            "Open Function Diffs",
            new LoadFunctionDiffsAction(controller),
            isOpenDiffsEnabled(controller)));

    add(
        GuiUtils.buildMenuItem(
            "Close Function Diffs",
            new CloseFunctionDiffsAction(controller),
            isCloseDiffsEnabled(controller)));

    add(new JSeparator());

    add(
        GuiUtils.buildMenuItem(
            "Close Function Diff Views",
            new CloseFunctionDiffsViewsAction(controller),
            isCloseViewsEnabled(controller)));
  }

  private boolean isCloseDiffsEnabled(final WorkspaceTabPanelFunctions controller) {
    for (final Diff diff : controller.getWorkspace().getDiffList(true)) {
      if (diff.isLoaded()) {
        return true;
      }
    }

    return false;
  }

  private boolean isCloseViewsEnabled(final WorkspaceTabPanelFunctions controller) {
    final TabPanelManager tabPanelManager =
        controller.getMainWindow().getController().getTabPanelManager();
    return tabPanelManager.getViewTabPanels(true).size() > 0;
  }

  private boolean isOpenDiffsEnabled(final WorkspaceTabPanelFunctions controller) {
    for (final Diff diff : controller.getWorkspace().getDiffList(true)) {
      if (!diff.isLoaded()) {
        return true;
      }
    }

    return false;
  }
}
