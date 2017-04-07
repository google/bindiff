package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class OpenCallGraphViewAction extends AbstractAction {
  private final WorkspaceTabPanelFunctions controller;
  private final Diff diff;

  public OpenCallGraphViewAction(final WorkspaceTabPanelFunctions controller, final Diff diff) {
    this.controller = Preconditions.checkNotNull(controller);
    this.diff = Preconditions.checkNotNull(diff);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    controller.openCallgraphView(controller.getMainWindow(), diff);
  }
}
