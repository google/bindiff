package com.google.security.zynamics.bindiff.graph.helpers;

import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class GraphColorizer {
  private static final LinkedHashSet<Color> recentColors = new LinkedHashSet<>();

  public static void colorizeInvisibleNodes(final BinDiffGraph<?, ?> graph, final Color color) {
    for (final ZyGraphNode<?> node : graph.getNodes()) {
      if (!node.isVisible()) {
        node.getRawNode().setColor(color);
      }
    }
  }

  public static void colorizeSelectedNodes(final BinDiffGraph<?, ?> graph, final Color color) {
    for (final ZyGraphNode<?> node : graph.getSelectedNodes()) {
      node.getRawNode().setColor(color);
    }
  }

  public static void colorizeUnselectedNodes(final BinDiffGraph<?, ?> graph, final Color color) {
    for (final ZyGraphNode<?> node : graph.getNodes()) {
      if (!node.isSelected()) {
        node.getRawNode().setColor(color);
      }
    }
  }

  public static List<Color> getRecentColors() {
    return new ArrayList<>(recentColors);
  }

  public static void setRecentColors(final List<Color> colors) {
    recentColors.clear();
    recentColors.addAll(colors);
  }
}
