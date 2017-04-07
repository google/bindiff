package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class JumpToAddressAction extends AbstractAction {
  private final ViewTabPanelFunctions controller;
  private final ESide side;

  public JumpToAddressAction(final ViewTabPanelFunctions controller, final ESide side) {
    this.controller = Preconditions.checkNotNull(controller);
    this.side = Preconditions.checkNotNull(side);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    controller.setCaretIntoJumpToAddressField(side);
  }
}
