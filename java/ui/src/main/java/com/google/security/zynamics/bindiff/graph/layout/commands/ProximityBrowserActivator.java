package com.google.security.zynamics.bindiff.graph.layout.commands;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.exceptions.GraphLayoutException;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.layout.LayoutCommandHelper;
import com.google.security.zynamics.zylib.types.common.ICommand;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

public class ProximityBrowserActivator implements ICommand {
  private final BinDiffGraph<ZyGraphNode<?>, ?> graph;

  public ProximityBrowserActivator(final BinDiffGraph<ZyGraphNode<?>, ?> graph) {
    this.graph = Preconditions.checkNotNull(graph);
  }

  public static void executeStatic(final BinDiffGraph<ZyGraphNode<?>, ?> graph)
      throws GraphLayoutException {
    graph.getCombinedGraph().getProximityBrowser().addSettingsListener();
    graph.getSuperGraph().getProximityBrowser().addSettingsListener();

    try {
      graph.getSettings().getProximitySettings().setProximityBrowsing(true);
    } finally {
      graph.getSuperGraph().getProximityBrowser().removeSettingsListener();
      graph.getCombinedGraph().getProximityBrowser().removeSettingsListener();
    }

    if (!LayoutCommandHelper.isProximityBrowsingFrozen(graph)) {
      if (LayoutCommandHelper.hasSelectedNodes(graph)) {
        ProximityBrowserUpdater.executeStatic(graph);

        if (graph.getSettings().getLayoutSettings().getAutomaticLayouting()
            && !graph.getSettings().getProximitySettings().getProximityBrowsingFrozen()) {
          GraphLayoutUpdater.executeStatic(graph, true);
        }
      }

      GraphViewUpdater.updateViews(graph);
    }
  }

  @Override
  public void execute() throws GraphLayoutException {
    executeStatic(graph);
  }
}
