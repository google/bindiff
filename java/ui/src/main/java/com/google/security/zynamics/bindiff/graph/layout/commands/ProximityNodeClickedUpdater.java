// Copyright 2011-2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.bindiff.graph.layout.commands;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.exceptions.GraphLayoutException;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.layout.LayoutCommandHelper;
import com.google.security.zynamics.bindiff.graph.listeners.GraphsIntermediateListeners;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperDiffNode;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.types.common.ICommand;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.helpers.ProximityHelper;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity.ZyProximityNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;
import y.base.Node;

public final class ProximityNodeClickedUpdater implements ICommand {
  private final BinDiffGraph<? extends ZyGraphNode<? extends IViewNode<?>>, ?> graph;

  private final ZyProximityNode<?> proximityNode;

  public ProximityNodeClickedUpdater(
      final BinDiffGraph<? extends ZyGraphNode<? extends IViewNode<?>>, ?> graph,
      final ZyProximityNode<?> proximityNode) {
    this.graph = checkNotNull(graph);
    this.proximityNode = checkNotNull(proximityNode);
  }

  private static Set<ZyGraphNode<? extends IViewNode<?>>> getNodesToShow(
      final BinDiffGraph<? extends ZyGraphNode<? extends IViewNode<?>>, ?> graph,
      final ZyProximityNode<?> proximityNode) {
    final Node yProxiNode = proximityNode.getNode();

    final Set<ZyGraphNode<? extends IViewNode<?>>> diffNodesToShow = new HashSet<>();

    final Node yNode = yProxiNode.neighbors().node();
    final ZyGraphNode<?> diffNode = graph.getNode(yNode);

    if (proximityNode.indegree() == 1) {
      for (final ZyGraphNode<?> child : diffNode.getChildren()) {
        if (!child.isVisible()
            && !ProximityHelper.isProximityNode(graph.getGraph(), child.getNode())) {
          diffNodesToShow.add(child);
        }
      }
    }

    if (proximityNode.outdegree() == 1) {
      for (final ZyGraphNode<?> parent : diffNode.getParents()) {
        if (!parent.isVisible()
            && !ProximityHelper.isProximityNode(graph.getGraph(), parent.getNode())) {
          diffNodesToShow.add(parent);
        }
      }
    }

    return diffNodesToShow;
  }

  private static boolean showProximityNodeClickedVisibilityWarningDialog(
      final BinDiffGraph<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>> graph,
      final ZyProximityNode<?> proximityNode) {
    final int nodeCount = ProximityNodeClickedUpdater.getNodesToShowCount(graph, proximityNode);

    final int threshold = graph.getSettings().getLayoutSettings().getVisibilityWarningThreshold();

    if (nodeCount > threshold) {
      final int answer =
          CMessageBox.showYesNoQuestion(
              BinDiffGraph.getParentWindow(graph),
              String.format(
                  "The selected operation makes %d more nodes visible. Do you want to continue?",
                  nodeCount));

      return answer == JOptionPane.YES_OPTION;
    }

    return true;
  }

  private static void updateVisibility(
      final BinDiffGraph<? extends ZyGraphNode<? extends IViewNode<?>>, ?> graph,
      final ZyProximityNode<?> proximityNode) {
    final Set<ZyGraphNode<? extends IViewNode<?>>> diffNodesToShow =
        getNodesToShow(graph, proximityNode);
    if (graph.getSettings().isSync()) {
      final Set<CombinedDiffNode> combinedNodesToShow = new HashSet<>();
      final Set<SuperDiffNode> superNodesToShow = new HashSet<>();

      for (final ZyGraphNode<? extends IViewNode<?>> node : diffNodesToShow) {
        if (node instanceof SingleDiffNode) {
          combinedNodesToShow.add(((SingleDiffNode) node).getCombinedDiffNode());
          superNodesToShow.add(((SingleDiffNode) node).getSuperDiffNode());
        } else if (node instanceof CombinedDiffNode) {
          combinedNodesToShow.add((CombinedDiffNode) node);
          superNodesToShow.add(((CombinedDiffNode) node).getSuperDiffNode());
        }
      }
      graph.getCombinedGraph().showNodes(combinedNodesToShow, false);
      graph.getSuperGraph().showNodes(superNodesToShow, false);
    } else {
      for (final ZyGraphNode<? extends IViewNode<?>> node : diffNodesToShow) {
        if (node instanceof SingleDiffNode || node instanceof CombinedDiffNode) {
          node.getRawNode().setVisible(true);
        }
      }
    }
  }

  protected static void updateProximityNodes(
      final BinDiffGraph<? extends ZyGraphNode<? extends IViewNode<?>>, ?> graph)
      throws GraphLayoutException {
    if (graph.getSettings().isSync()) {
      graph.getCombinedGraph().getProximityBrowser().deleteProximityBrowsingNodes();
      graph.getSuperGraph().getProximityBrowser().deleteProximityBrowsingNodes();

      final List<CombinedDiffNode> combinedNodes = new ArrayList<>();
      combinedNodes.addAll(graph.getCombinedGraph().getNodes());

      final List<SuperDiffNode> superNodes = new ArrayList<>();
      superNodes.addAll(graph.getSuperGraph().getNodes());

      graph.getCombinedGraph().getProximityBrowser().createProximityBrowsingNodes(combinedNodes);
      graph.getSuperGraph().getProximityBrowser().createProximityBrowsingNodes(superNodes);

      ProximityBrowserUpdater.adoptSuperGraphVisibility(graph.getSuperGraph());

      ProximityBrowserUpdater.deleteAllProximityNodes(graph.getPrimaryGraph());
      ProximityBrowserUpdater.deleteAllProximityNodes(graph.getSecondaryGraph());

      ProximityBrowserUpdater.createProximityNodesAndEdges(graph.getSuperGraph());
    } else {
      ProximityBrowserUpdater.deleteAllProximityNodes(graph);
      if (graph instanceof SingleGraph) {
        ProximityBrowserUpdater.createProximityNodesAndEdges((SingleGraph) graph);
      } else if (graph instanceof CombinedGraph) {
        ProximityBrowserUpdater.createProximityNodesAndEdges((CombinedGraph) graph);
      }
    }
  }

  public static void executeStatic(
      final BinDiffGraph<? extends ZyGraphNode<? extends IViewNode<?>>, ?> graph,
      final ZyProximityNode<?> proximityNode)
      throws GraphLayoutException {
    if (showProximityNodeClickedVisibilityWarningDialog(graph, proximityNode)) {
      updateVisibility(graph, proximityNode);
      updateProximityNodes(graph);

      GraphViewUpdater.updateViews(graph);

      GraphsIntermediateListeners.notifyIntermediateVisibilityListeners(graph);
      if (LayoutCommandHelper.isAutoLayout(graph)) {
        GraphLayoutUpdater.executeStatic(graph, true);
      }
    }
  }

  public static int getNodesToShowCount(
      final BinDiffGraph<? extends ZyGraphNode<? extends IViewNode<?>>, ?> graph,
      final ZyProximityNode<?> proximityNode) {
    return getNodesToShow(graph, proximityNode).size();
  }

  @Override
  public void execute() throws GraphLayoutException {
    executeStatic(graph, proximityNode);
  }
}
