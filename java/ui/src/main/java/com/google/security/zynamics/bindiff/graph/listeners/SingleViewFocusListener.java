package com.google.security.zynamics.bindiff.graph.listeners;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.ViewTabPanelFunctions;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SingleViewFocusListener extends MouseAdapter {
  private final SingleGraph graph;

  private final ViewTabPanelFunctions controller;

  protected SingleViewFocusListener(
      final ViewTabPanelFunctions controller, final SingleGraph graph) {
    this.controller = Preconditions.checkNotNull(controller);
    this.graph = Preconditions.checkNotNull(graph);

    addListener();
  }

  public void addListener() {
    graph.getView().getCanvasComponent().addMouseListener(this);
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
    controller.setViewFocus(graph.getSide());
  }

  public void removeListener() {
    graph.getView().getCanvasComponent().removeMouseListener(this);
  }
}
