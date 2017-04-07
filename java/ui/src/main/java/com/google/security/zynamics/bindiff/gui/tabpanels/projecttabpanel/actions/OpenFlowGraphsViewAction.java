package com.google.security.zynamics.bindiff.gui.tabpanels.projecttabpanel.actions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class OpenFlowGraphsViewAction extends AbstractAction {
  private final ViewTabPanelFunctions controller;
  private final ZyGraphNode<?> node;

  public OpenFlowGraphsViewAction(
      final ViewTabPanelFunctions controller, final ZyGraphNode<?> node) {
    super("Open Flow Graphs View");

    this.controller = Preconditions.checkNotNull(controller);
    this.node = Preconditions.checkNotNull(node);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    controller.openFlowgraphsViews(node);
  }
}
