package com.google.security.zynamics.bindiff.graph.layout.commands;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.exceptions.GraphLayoutException;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.SuperGraph;
import com.google.security.zynamics.bindiff.graph.edges.SingleDiffEdge;
import com.google.security.zynamics.bindiff.graph.edges.SuperDiffEdge;
import com.google.security.zynamics.bindiff.graph.listeners.GraphsIntermediateListeners;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedViewNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleViewNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperDiffNode;
import com.google.security.zynamics.bindiff.log.Logger;
import com.google.security.zynamics.zylib.gui.zygraph.edges.ZyEdgeData;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.NodeHelpers;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.ZyNodeData;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent;
import com.google.security.zynamics.zylib.gui.zygraph.settings.IProximitySettings;
import com.google.security.zynamics.zylib.types.common.ICommand;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyInfoEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity.ZyProximityNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyProximityNodeRealizer;

import y.base.Edge;
import y.base.Node;
import y.base.NodeCursor;
import y.view.Graph2D;
import y.view.LineType;
import y.view.NodeRealizer;

import java.awt.Font;
import java.util.HashSet;
import java.util.Set;

public final class ProximityBrowserUpdater implements ICommand {
  private final BinDiffGraph<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>>
      referenceGraph;

  public ProximityBrowserUpdater(
      final BinDiffGraph<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>> referenceGraph) {
    this.referenceGraph = Preconditions.checkNotNull(referenceGraph);
  }

  private static void createNewProximityNodeAndEdge(
      final Graph2D combinedGraph2D,
      final CombinedDiffNode combinedDiffNode,
      final int degree,
      final boolean isIncomming) {
    if (degree == 0) {
      return;
    }

    final ZyLabelContent labelcontent = new ZyLabelContent(null);
    labelcontent.addLineContent(
        new ZyLineContent(String.valueOf(degree), new Font("New Courier", Font.PLAIN, 12), null));

    final ZyProximityNodeRealizer<CombinedViewNode> proxiRealizer =
        new ZyProximityNodeRealizer<>(labelcontent);
    final Node node = combinedGraph2D.createNode(proxiRealizer);

    final ZyProximityNode<CombinedViewNode> infoNode =
        new ZyProximityNode<>(node, proxiRealizer, combinedDiffNode, isIncomming);
    final ZyNodeData<ZyProximityNode<CombinedViewNode>> nodeData = new ZyNodeData<>(infoNode);
    proxiRealizer.setUserData(nodeData);

    final ZyEdgeRealizer<ZyInfoEdge> proxiZyEdgeRelalizer =
        new ZyEdgeRealizer<>(new ZyLabelContent(null), null);
    proxiZyEdgeRelalizer.setLineType(LineType.LINE_2);

    final Edge edge =
        combinedGraph2D.createEdge(
            isIncomming ? combinedDiffNode.getNode() : infoNode.getNode(),
            isIncomming ? infoNode.getNode() : combinedDiffNode.getNode(),
            proxiZyEdgeRelalizer);

    final ZyInfoEdge infoEdge =
        new ZyInfoEdge(
            isIncomming ? combinedDiffNode : infoNode,
            isIncomming ? infoNode : combinedDiffNode,
            edge,
            proxiZyEdgeRelalizer);

    final ZyEdgeData<ZyInfoEdge> edgedata = new ZyEdgeData<>(infoEdge);

    proxiZyEdgeRelalizer.setUserData(edgedata);
  }

  private static void createNewProximityNodeAndEdge(
      final Graph2D singleGraph2D,
      final SingleDiffNode singleDiffNode,
      final int degree,
      final boolean isIncomming) {
    if (degree == 0) {
      return;
    }

    final ZyLabelContent labelcontent = new ZyLabelContent(null);
    labelcontent.addLineContent(
        new ZyLineContent(String.valueOf(degree), new Font("New Courier", Font.PLAIN, 12), null));

    final ZyProximityNodeRealizer<SingleViewNode> proxiRealizer =
        new ZyProximityNodeRealizer<>(labelcontent);
    final Node node = singleGraph2D.createNode(proxiRealizer);

    final ZyProximityNode<SingleViewNode> infoNode =
        new ZyProximityNode<>(node, proxiRealizer, singleDiffNode, isIncomming);
    final ZyNodeData<ZyProximityNode<SingleViewNode>> nodeData = new ZyNodeData<>(infoNode);
    proxiRealizer.setUserData(nodeData);

    final ZyEdgeRealizer<ZyInfoEdge> proxiZyEdgeRelalizer =
        new ZyEdgeRealizer<>(new ZyLabelContent(null), null);
    proxiZyEdgeRelalizer.setLineType(LineType.LINE_2);

    final Edge edge =
        singleGraph2D.createEdge(
            isIncomming ? singleDiffNode.getNode() : infoNode.getNode(),
            isIncomming ? infoNode.getNode() : singleDiffNode.getNode(),
            proxiZyEdgeRelalizer);

    final ZyInfoEdge infoEdge =
        new ZyInfoEdge(
            isIncomming ? singleDiffNode : infoNode,
            isIncomming ? infoNode : singleDiffNode,
            edge,
            proxiZyEdgeRelalizer);

    final ZyEdgeData<ZyInfoEdge> edgedata = new ZyEdgeData<>(infoEdge);

    proxiZyEdgeRelalizer.setUserData(edgedata);
  }

  private static void unhideAllNodes(final BinDiffGraph<? extends ZyGraphNode<?>, ?> graph) {
    for (final ZyGraphNode<?> node : graph.getNodes()) {
      if (!node.isVisible()) {
        ((IViewNode<?>) node.getRawNode()).setVisible(true);
      }
    }
  }

  private static <NodeType extends ZyGraphNode<?>> void updateGraphVisibility(
      final BinDiffGraph<NodeType, ?> graph) {
    final Set<NodeType> visibleNodes = new HashSet<>();
    final Set<NodeType> invisibleNodes = new HashSet<>();

    invisibleNodes.addAll(graph.getNodes());
    visibleNodes.addAll(graph.getSelectedNodes());
    invisibleNodes.removeAll(visibleNodes);

    graph.showNodes(visibleNodes, invisibleNodes);
  }

  private static void updateProximityBrowserInAsyncMode(
      final BinDiffGraph<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>> graph)
      throws GraphLayoutException {
    if (graph instanceof CombinedGraph) {
      updateGraphVisibility(graph);
      deleteAllProximityNodes(graph);
      createProximityNodesAndEdges((CombinedGraph) graph);
    } else if (graph instanceof SingleGraph) {
      updateGraphVisibility(graph);
      deleteAllProximityNodes(graph);
      createProximityNodesAndEdges((SingleGraph) graph);
    }
  }

  protected static void adoptSuperGraphVisibility(final SuperGraph superGraph) {
    for (final SuperDiffNode superDiffNode : superGraph.getNodes()) {
      final SingleDiffNode primaryDiffNode = superDiffNode.getPrimaryDiffNode();
      if (primaryDiffNode != null) {
        primaryDiffNode.getRawNode().setVisible(superDiffNode.isVisible());
      }

      final SingleDiffNode secondaryDiffNode = superDiffNode.getSecondaryDiffNode();
      if (secondaryDiffNode != null) {
        secondaryDiffNode.getRawNode().setVisible(superDiffNode.isVisible());
      }
    }

    for (final SuperDiffEdge superDiffEdge : superGraph.getEdges()) {
      final SingleDiffEdge primaryDiffEdge = superDiffEdge.getPrimaryDiffEdge();
      final SingleDiffEdge secondaryDiffEdge = superDiffEdge.getSecondaryDiffEdge();

      if (primaryDiffEdge != null) {
        superDiffEdge.getPrimaryDiffEdge().getRawEdge().setVisible(superDiffEdge.isVisible());
      }
      if (secondaryDiffEdge != null) {
        superDiffEdge.getSecondaryDiffEdge().getRawEdge().setVisible(superDiffEdge.isVisible());
      }
    }
  }

  protected static void createProximityNodesAndEdges(final CombinedGraph combinedGraph) {
    // create single side proximity nodes
    final Graph2D yGraph = combinedGraph.getGraph();
    for (final CombinedDiffNode diffNode : combinedGraph.getNodes()) {
      if (diffNode.isVisible()) {
        final Set<CombinedDiffNode> invisibleParents = new HashSet<>();
        for (final CombinedDiffNode parent : diffNode.getParents()) {
          if (!parent.isVisible()) {
            invisibleParents.add(parent);
          }
        }
        createNewProximityNodeAndEdge(yGraph, diffNode, invisibleParents.size(), false);

        final Set<CombinedDiffNode> invisibleChildren = new HashSet<>();
        for (final CombinedDiffNode child : diffNode.getChildren()) {
          if (!child.isVisible()) {
            invisibleChildren.add(child);
          }
        }
        createNewProximityNodeAndEdge(yGraph, diffNode, invisibleChildren.size(), true);
      }
    }
  }

  protected static void createProximityNodesAndEdges(final SingleGraph singleGraph) {
    // create single side proximity nodes
    final Graph2D yGraph = singleGraph.getGraph();
    for (final SingleDiffNode diffNode : singleGraph.getNodes()) {
      if (diffNode.isVisible()) {
        final Set<SingleDiffNode> invisibleParents = new HashSet<>();
        for (final SingleDiffNode parent : diffNode.getParents()) {
          if (!parent.isVisible()) {
            invisibleParents.add(parent);
          }
        }
        createNewProximityNodeAndEdge(yGraph, diffNode, invisibleParents.size(), false);

        final Set<SingleDiffNode> invisibleChildren = new HashSet<>();
        for (final SingleDiffNode child : diffNode.getChildren()) {
          if (!child.isVisible()) {
            invisibleChildren.add(child);
          }
        }

        createNewProximityNodeAndEdge(yGraph, diffNode, invisibleChildren.size(), true);
      }
    }
  }

  static void createProximityNodesAndEdges(final SuperGraph superGraph) {
    final Graph2D primaryYGraph2D = superGraph.getPrimaryGraph().getGraph();
    final Graph2D secondaryYGraph2D = superGraph.getSecondaryGraph().getGraph();

    // create single side proximity nodes
    if (superGraph.getSettings().getProximitySettings().getProximityBrowsing()) {
      for (final Node superYNode : superGraph.getGraph().getNodeArray()) {
        final NodeRealizer superRealizer = superGraph.getGraph().getRealizer(superYNode);

        if (superRealizer instanceof ZyProximityNodeRealizer<?>) {
          if (superYNode.inDegree() == 0 && superYNode.outDegree() == 1) {
            final Edge superProxiYEdge = superYNode.firstOutEdge();
            final Node superNormalYNode = superProxiYEdge.target();

            final SuperDiffNode superDiffNode = superGraph.getNode(superNormalYNode);

            int primaryDegree = 0;
            final SingleDiffNode primaryDiffNode = superDiffNode.getPrimaryDiffNode();
            if (primaryDiffNode != null) {
              // Calculate the number to display in the relevant proximity node (primary)
              primaryDegree =
                  NodeHelpers.countInvisibleIndegreeNeighbours(primaryDiffNode.getRawNode());
              createNewProximityNodeAndEdge(primaryYGraph2D, primaryDiffNode, primaryDegree, false);
            }

            int secondaryDegree = 0;
            final SingleDiffNode secondaryDiffNode = superDiffNode.getSecondaryDiffNode();
            if (secondaryDiffNode != null) {
              // Calculate the same number for the secondary node
              secondaryDegree =
                  NodeHelpers.countInvisibleIndegreeNeighbours(secondaryDiffNode.getRawNode());
              createNewProximityNodeAndEdge(
                  secondaryYGraph2D, secondaryDiffNode, secondaryDegree, false);
            }
          } else if (superYNode.inDegree() == 1 && superYNode.outDegree() == 0) {
            final Edge superProxiYEdge = superYNode.firstInEdge();
            final Node superNormalYNode = superProxiYEdge.source();

            final SuperDiffNode superDiffNode = superGraph.getNode(superNormalYNode);

            int primaryDegree = 0;
            final SingleDiffNode primaryDiffNode = superDiffNode.getPrimaryDiffNode();
            if (primaryDiffNode != null) {
              primaryDegree =
                  NodeHelpers.countInvisibleOutdegreeNeighbours(primaryDiffNode.getRawNode());
              createNewProximityNodeAndEdge(primaryYGraph2D, primaryDiffNode, primaryDegree, true);
            }

            int secondaryDegree = 0;
            final SingleDiffNode secondaryDiffNode = superDiffNode.getSecondaryDiffNode();
            if (secondaryDiffNode != null) {
              secondaryDegree =
                  NodeHelpers.countInvisibleOutdegreeNeighbours(secondaryDiffNode.getRawNode());
              createNewProximityNodeAndEdge(
                  secondaryYGraph2D, secondaryDiffNode, secondaryDegree, true);
            }
          } else {
            Logger.logSevere(
                "Malformed graph. Super proximity node without incoming or outgoing edge.");
          }
        }
      }
    }
  }

  static void deleteAllProximityNodes(final BinDiffGraph<?, ?> graph) throws GraphLayoutException {
    final Graph2D yGraph = graph.getGraph();

    try {
      yGraph.firePreEvent();

      graph.getProximityBrowser().deleteProximityBrowsingNodes();

      for (final NodeCursor nc = yGraph.nodes(); nc.ok(); nc.next()) {
        if (yGraph.getRealizer(nc.node()) instanceof ZyProximityNodeRealizer<?>) {
          if (nc.node().getGraph() == yGraph) {
            yGraph.removeNode(nc.node());
          } else {
            throw new GraphLayoutException(
                "Delete proximity node failed. Couldn't update proximity browsing.");
          }
        }
      }
    } finally {
      yGraph.firePostEvent();
    }
  }

  public static void executeStatic(
      final BinDiffGraph<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>> graph)
      throws GraphLayoutException {
    try {
      final IProximitySettings settings = graph.getSettings().getProximitySettings();
      if (settings.getProximityBrowsing() && !settings.getProximityBrowsingFrozen()) {
        final boolean autoLayout = graph.getSettings().getLayoutSettings().getAutomaticLayouting();
        graph.getSettings().getLayoutSettings().setAutomaticLayouting(false);

        if (graph.getSettings().isSync()) {
          updateGraphVisibility(graph.getCombinedGraph());
          updateGraphVisibility(graph.getSuperGraph());

          adoptSuperGraphVisibility(graph.getSuperGraph());

          deleteAllProximityNodes(graph.getPrimaryGraph());
          deleteAllProximityNodes(graph.getSecondaryGraph());

          createProximityNodesAndEdges(graph.getSuperGraph());
        } else {
          updateProximityBrowserInAsyncMode(graph);
        }

        GraphsIntermediateListeners.notifyIntermediateVisibilityListeners(graph);

        graph.getSettings().getLayoutSettings().setAutomaticLayouting(autoLayout);
      } else if (!settings.getProximityBrowsing()) {
        try {
          unhideAllNodes(graph.getSuperGraph());
          unhideAllNodes(graph.getPrimaryGraph());
          unhideAllNodes(graph.getSecondaryGraph());
          unhideAllNodes(graph.getCombinedGraph());
        } catch (final Exception e) {
          // FIXME: Never catch all exceptions!
          throw new GraphLayoutException(
              e, "Failed to unhide node. Couldn't update proximity browser.");
        }
      }
    } catch (final GraphLayoutException e) {
      throw e;
    } catch (final Exception e) {
      // FIXME: Never catch all exceptions!
      throw new GraphLayoutException(e, "Failed to update proximity browser.");
    }
  }

  @Override
  public void execute() throws GraphLayoutException {
    executeStatic(referenceGraph);
  }
}
