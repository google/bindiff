package com.google.security.zynamics.bindiff.graph.layout;

import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;

public class LayoutCommandHelper {
  public static void activateAutoLayout(final BinDiffGraph<?, ?> graph, final boolean autoLayout) {
    graph.getSettings().getLayoutSettings().setAutomaticLayouting(autoLayout);
  }

  public static boolean deactiveAutoLayout(final BinDiffGraph<?, ?> graph) {
    final boolean wasAutolayout = isAutolayout(graph);
    graph.getSettings().getLayoutSettings().setAutomaticLayouting(false);

    return wasAutolayout;
  }

  public static boolean hasSelectedNodes(final BinDiffGraph<?, ?> graph) {
    final GraphSettings settings = graph.getSettings();

    if (settings.isSync()) {
      return graph.getSuperGraph().getSelectedNodes().size() > 0;
    }

    return graph.getSelectedNodes().size() > 0;
  }

  public static boolean isAutolayout(final BinDiffGraph<?, ?> graph) {
    return graph.getSettings().getLayoutSettings().getAutomaticLayouting();
  }

  public static boolean isProximityBrowsing(final BinDiffGraph<?, ?> graph) {
    return graph.getSettings().getProximitySettings().getProximityBrowsing();
  }

  public static boolean isProximityBrowsingFrozen(final BinDiffGraph<?, ?> graph) {
    return graph.getSettings().getProximitySettings().getProximityBrowsingFrozen();
  }

  public static boolean isSnychron(final BinDiffGraph<?, ?> graph) {
    return graph.getSettings().isSync();
  }
}
