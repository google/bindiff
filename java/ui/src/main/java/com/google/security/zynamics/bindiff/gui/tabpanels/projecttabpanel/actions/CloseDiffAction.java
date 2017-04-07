package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.WorkspaceTabPanelFunctions;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;

public class CloseDiffAction extends AbstractAction {
  private final WorkspaceTabPanelFunctions controller;

  public CloseDiffAction(final WorkspaceTabPanelFunctions controller) {
    this.controller = Preconditions.checkNotNull(controller);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final Set<Diff> diffSet = new HashSet<>();
    diffSet.add(controller.getSelectedDiff());
    controller.closeDiffs(diffSet);
  }
}
