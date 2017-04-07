package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.gui.tabpanels.TabPanelManager;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanel;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;

public class CloseCallGraphViewAction extends AbstractAction {
  private final WorkspaceTabPanelFunctions controller;
  private final Diff diff;

  public CloseCallGraphViewAction(final WorkspaceTabPanelFunctions controller, final Diff diff) {
    this.controller = Preconditions.checkNotNull(controller);
    this.diff = Preconditions.checkNotNull(diff);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final TabPanelManager tabManager =
        controller.getMainWindow().getController().getTabPanelManager();
    final ViewTabPanel panel = tabManager.getTabPanel(null, null, diff);

    if (panel != null) {
      final Set<ViewTabPanel> views = new HashSet<>();
      views.add(panel);
      controller.closeViews(views);
    }
  }
}
