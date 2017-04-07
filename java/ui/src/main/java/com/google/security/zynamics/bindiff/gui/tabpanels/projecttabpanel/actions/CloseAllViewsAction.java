package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanelManager;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanel;
import java.awt.event.ActionEvent;
import java.util.LinkedHashSet;
import javax.swing.AbstractAction;

public class CloseAllViewsAction extends AbstractAction {
  private final WorkspaceTabPanelFunctions controller;
  private final ViewTabPanel dontClosePanel;

  public CloseAllViewsAction(final WorkspaceTabPanelFunctions controller) {
    this(controller, null);
  }

  public CloseAllViewsAction(
      final WorkspaceTabPanelFunctions controller, final ViewTabPanel dontClosePanel) {
    this.controller = Preconditions.checkNotNull(controller);
    this.dontClosePanel = dontClosePanel;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final LinkedHashSet<ViewTabPanel> tabPanelsToClose = new LinkedHashSet<>();

    final TabPanelManager tabPanelManager =
        controller.getMainWindow().getController().getTabPanelManager();
    for (final ViewTabPanel tabPanel : tabPanelManager.getViewTabPanels()) {
      if (dontClosePanel != null && dontClosePanel == tabPanel) {
        continue;
      }
      tabPanelsToClose.add(tabPanel);
    }

    controller.closeViews(tabPanelsToClose);
  }
}
