package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanelManager;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanel;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import java.awt.event.ActionEvent;
import java.util.LinkedHashSet;
import javax.swing.AbstractAction;
import javax.swing.JTabbedPane;

public class CloseViewAction extends AbstractAction {
  private final ViewTabPanelFunctions controller;
  private ViewTabPanel viewPanel;

  public CloseViewAction(final ViewTabPanel viewPanel) {
    this.controller = null;
    this.viewPanel = Preconditions.checkNotNull(viewPanel);
  }

  public CloseViewAction(final ViewTabPanelFunctions controller) {
    this.controller = Preconditions.checkNotNull(controller);
    this.viewPanel = null;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    execute();
  }

  public void execute() {
    if (controller != null) {
      final JTabbedPane tabbedPane = controller.getTabPanelManager().getTabbedPane();
      viewPanel =
          tabbedPane.getSelectedIndex() > 0
              ? (ViewTabPanel) tabbedPane.getSelectedComponent()
              : null;
    }
    if (viewPanel != null) {
      final TabPanelManager tabPanelManager = viewPanel.getController().getTabPanelManager();
      final WorkspaceTabPanelFunctions controller =
          tabPanelManager.getWorkspaceTabPanel().getController();

      final LinkedHashSet<ViewTabPanel> tabPanelsToClose = new LinkedHashSet<>();
      tabPanelsToClose.add(viewPanel);
      controller.closeViews(tabPanelsToClose);
    }
  }
}
