package com.google.security.zynamics.bindiff.graph.layout.commands;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.exceptions.GraphLayoutException;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.layout.LayoutCommandHelper;
import com.google.security.zynamics.bindiff.graph.settings.GraphLayoutSettings;
import com.google.security.zynamics.zylib.types.common.ICommand;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

public class GraphLayoutInitializer implements ICommand {
  private final BinDiffGraph<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>> graph;

  public GraphLayoutInitializer(
      final BinDiffGraph<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>> graph) {
    Preconditions.checkNotNull(graph);

    this.graph = graph;
  }

  public static void executeStatic(
      final BinDiffGraph<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>> graph)
      throws GraphLayoutException {
    final GraphLayoutSettings settings = graph.getSettings().getLayoutSettings();

    final boolean animated = settings.getAnimateLayout();
    settings.setAnimateLayout(false);
    try {
      ProximityBrowserInitializer.executeStatic(graph);
      if (LayoutCommandHelper.isAutolayout(graph)) {
        GraphLayoutUpdater.executeStatic(graph, false);
      }
    } finally {
      settings.setAnimateLayout(animated);
    }
  }

  @Override
  public void execute() throws GraphLayoutException {
    executeStatic(graph);
  }
}
