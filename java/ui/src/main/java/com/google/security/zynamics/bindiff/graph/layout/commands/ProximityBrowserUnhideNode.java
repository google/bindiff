package com.google.security.zynamics.bindiff.graph.layout.commands;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.exceptions.GraphLayoutException;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.layout.LayoutCommandHelper;
import com.google.security.zynamics.bindiff.graph.listeners.GraphsIntermediateListeners;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.settings.GraphProximityBrowsingSettings;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.proximity.ProximityRangeCalculator;
import com.google.security.zynamics.zylib.types.common.ICommand;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import y.base.Node;

import java.util.HashSet;
import java.util.Set;

public class ProximityBrowserUnhideNode implements ICommand {
  private final BinDiffGraph<ZyGraphNode<?>, ?> graph;
  private final ZyGraphNode<?> nodeToShow;

  public ProximityBrowserUnhideNode(
      final BinDiffGraph<ZyGraphNode<?>, ?> graph, final ZyGraphNode<?> nodeToShow) {
    this.graph = Preconditions.checkNotNull(graph);
    this.nodeToShow = Preconditions.checkNotNull(nodeToShow);
  }

  private static void updateVisibility(
      final BinDiffGraph<ZyGraphNode<?>, ?> graph, final Set<ZyGraphNode<?>> allNodesToShow) {
    for (final ZyGraphNode<? extends IViewNode<?>> node : allNodesToShow) {
      if (node.isVisible()) {
        continue;
      }

      if (node instanceof SingleDiffNode) {
        final Node combinedYNode = ((SingleDiffNode) node).getCombinedDiffNode().getNode();
        final Node superYNode = ((SingleDiffNode) node).getSuperDiffNode().getNode();

        if (!graph.getCombinedGraph().getNode(combinedYNode).getRawNode().isVisible()) {
          graph.getCombinedGraph().getNode(combinedYNode).getRawNode().setVisible(true);
        }

        if (!graph.getSuperGraph().getNode(superYNode).getRawNode().isVisible()) {
          graph.getSuperGraph().getNode(superYNode).getRawNode().setVisible(true);
        }
      } else if (node instanceof CombinedDiffNode) {
        final Node combinedYNode = ((CombinedDiffNode) node).getNode();
        final Node superYNode = ((CombinedDiffNode) node).getSuperDiffNode().getNode();

        if (!graph.getCombinedGraph().getNode(combinedYNode).getRawNode().isVisible()) {
          graph.getCombinedGraph().getNode(combinedYNode).getRawNode().setVisible(true);
        }
        if (!graph.getSuperGraph().getNode(superYNode).getRawNode().isVisible()) {
          graph.getSuperGraph().getNode(superYNode).getRawNode().setVisible(true);
        }
      }
    }
  }

  public static void executeStatic(
      final BinDiffGraph<ZyGraphNode<?>, ?> graph, final ZyGraphNode<?> nodeToShow)
      throws GraphLayoutException {
    final Set<ZyGraphNode<?>> nodesToShow = new HashSet<>();
    nodesToShow.add(nodeToShow);

    final GraphProximityBrowsingSettings settings = graph.getSettings().getProximitySettings();
    final Set<ZyGraphNode<?>> allNodesToShow =
        ProximityRangeCalculator.getNeighbors(
            graph,
            nodesToShow,
            settings.getProximityBrowsingChildren(),
            settings.getProximityBrowsingParents());

    // TODO: Show node visibility warning dialog;
    updateVisibility(graph, allNodesToShow);
    ProximityNodeClickedUpdater.updateProximityNodes(graph);

    GraphViewUpdater.updateViews(graph);
    GraphsIntermediateListeners.notifyIntermediateVisibilityListeners(graph);

    if (LayoutCommandHelper.isAutolayout(graph)) {
      GraphLayoutUpdater.executeStatic(graph, true);
    }
  }

  @Override
  public void execute() throws GraphLayoutException {
    executeStatic(graph, nodeToShow);
  }
}
