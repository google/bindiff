package com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.actions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class PrintViewAction extends AbstractAction {
  private final ViewTabPanelFunctions controller;

  private final BinDiffGraph<?, ?> graph;

  public PrintViewAction(final ViewTabPanelFunctions controller, final BinDiffGraph<?, ?> graph) {
    this.controller = Preconditions.checkNotNull(controller);
    this.graph = Preconditions.checkNotNull(graph);
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    controller.printView(graph);
  }
}
