package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class LoadDiffAction extends AbstractAction {
  private final WorkspaceTabPanelFunctions controller;

  public LoadDiffAction(final WorkspaceTabPanelFunctions controller) {
    this.controller = Preconditions.checkNotNull(controller);
  }

  @Override
  public void actionPerformed(final ActionEvent arg0) {
    controller.loadDiff(null);
  }
}
