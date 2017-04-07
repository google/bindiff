package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class ShowSearchResultsDialogAction extends AbstractAction {
  private final ViewTabPanelFunctions controller;

  public ShowSearchResultsDialogAction(final ViewTabPanelFunctions controller) {
    this.controller = Preconditions.checkNotNull(controller);
  }

  @Override
  public void actionPerformed(final ActionEvent arg0) {
    controller.showSearchResultsDialog();
  }
}
