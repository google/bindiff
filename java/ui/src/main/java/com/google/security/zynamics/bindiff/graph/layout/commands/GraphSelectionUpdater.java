package com.google.security.zynamics.bindiff.graph.layout.commands;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.exceptions.GraphLayoutException;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.SuperGraph;
import com.google.security.zynamics.bindiff.graph.filter.GraphNodeFilter;
import com.google.security.zynamics.bindiff.graph.filter.GraphNodeFilter.Criterium;
import com.google.security.zynamics.bindiff.graph.layout.LayoutCommandHelper;
import com.google.security.zynamics.bindiff.graph.listeners.GraphsIntermediateListeners;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperDiffNode;
import com.google.security.zynamics.bindiff.graph.settings.GraphProximityBrowsingSettings;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.IEdgeCallback;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.proximity.ProximityRangeCalculator;
import com.google.security.zynamics.zylib.types.common.ICommand;
import com.google.security.zynamics.zylib.types.common.IterationMode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JOptionPane;

public final class GraphSelectionUpdater implements ICommand {
  private final BinDiffGraph<ZyGraphNode<?>, ?> referenceGraph;

  public GraphSelectionUpdater(final BinDiffGraph<ZyGraphNode<?>, ?> referenceGraph) {
    Preconditions.checkNotNull(referenceGraph);
    Preconditions.checkArgument(
        !((BinDiffGraph<?, ?>) referenceGraph instanceof SuperGraph),
        "Reference graph cannot be a super graph");

    this.referenceGraph = referenceGraph;
  }

  private static <
          EdgeType extends ZyGraphEdge<?, ?, ?>,
          GraphType extends AbstractZyGraph<? extends ZyGraphNode<?>, EdgeType>>
      void selectRawEdges(final GraphType graph) {
    final Collection<EdgeType> edgesToSelect = new ArrayList<>();

    graph.iterateEdges(
        new IEdgeCallback<EdgeType>() {
          @Override
          public IterationMode nextEdge(final EdgeType edge) {
            if (edge.getSource().getRawNode().isSelected()
                || edge.getTarget().getRawNode().isSelected()) {
              edgesToSelect.add(edge);
            }

            return IterationMode.CONTINUE;
          }
        });

    graph.getGraph().unselectEdges();

    for (final ZyGraphEdge<?, ?, ?> edge : edgesToSelect) {
      edge.getRawEdge().setSelected(true);
      graph.getGraph().getRealizer(edge.getEdge()).setSelected(true);
    }
  }

  private static void selectRawGraph(
      final CombinedGraph combinedGraph,
      final Collection<CombinedDiffNode> nodesToSelect,
      final Collection<CombinedDiffNode> nodesToUnselect) {
    combinedGraph.selectNodes(nodesToSelect, nodesToUnselect);
    selectRawNodes(combinedGraph);
    selectRawEdges(combinedGraph);
  }

  private static void selectRawGraph(
      final SingleGraph singleGraph,
      final Collection<SingleDiffNode> nodesToSelect,
      final Collection<SingleDiffNode> nodesToUnselect) {
    singleGraph.selectNodes(nodesToSelect, nodesToUnselect);
    selectRawNodes(singleGraph);
    selectRawEdges(singleGraph);
  }

  private static void selectRawGraph(
      final SuperGraph superGraph,
      final Collection<SuperDiffNode> nodesToSelect,
      final Collection<SuperDiffNode> nodesToUnselect) {
    superGraph.selectNodes(nodesToSelect, nodesToUnselect);
    selectRawNodes(superGraph);
    selectRawEdges(superGraph);
  }

  private static <
          GraphType extends
              AbstractZyGraph<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>>>
      void selectRawNodes(final GraphType graph) {
    for (final ZyGraphNode<?> node : graph.getSelectedNodes()) {
      if (node instanceof CombinedDiffNode) {
        ((CombinedDiffNode) node).getRawNode().setSelected(true);
      }
      if (node instanceof SuperDiffNode) {
        ((SuperDiffNode) node).getRawNode().setSelected(true);
      }
    }
  }

  private static boolean showVisibilityWarningDialog(
      final BinDiffGraph<ZyGraphNode<? extends IViewNode<?>>, ?> graph) {
    final int childDepth =
        graph.getSettings().getProximitySettings().getProximityBrowsingChildren();
    final int parentDepth =
        graph.getSettings().getProximitySettings().getProximityBrowsingParents();

    final Collection<ZyGraphNode<? extends IViewNode<?>>> visibleNodes =
        GraphNodeFilter.filterNodes(graph, Criterium.VISIBLE);
    final Collection<ZyGraphNode<? extends IViewNode<?>>> selectedNodes =
        GraphNodeFilter.filterNodes(graph, Criterium.SELECTED_VISIBLE);

    final Set<ZyGraphNode<? extends IViewNode<?>>> neighbours;
    neighbours =
        ProximityRangeCalculator.getNeighbors(graph, selectedNodes, childDepth, parentDepth);

    final Set<ZyGraphNode<?>> visibleNeighbours = new HashSet<>();

    int invisibleNeighboursCounter = 0;
    for (final ZyGraphNode<? extends IViewNode<?>> neighbour : neighbours) {
      if (!neighbour.isVisible()) {
        ++invisibleNeighboursCounter;
      } else {
        visibleNeighbours.add(neighbour);
      }
    }

    visibleNodes.removeAll(selectedNodes);
    visibleNodes.removeAll(visibleNeighbours);
    final int nodesToHideCounter = visibleNodes.size();
    final int moreNodeToShowCounter = invisibleNeighboursCounter - nodesToHideCounter;
    final int threshold = graph.getSettings().getLayoutSettings().getVisibilityWarningThreshold();

    if (moreNodeToShowCounter >= threshold) {
      final int answer =
          CMessageBox.showYesNoQuestion(
              BinDiffGraph.getParentWindow(graph),
              String.format(
                  "The selected operation makes %d more nodes visible. Do you want to continue?",
                  moreNodeToShowCounter));

      return answer == JOptionPane.YES_OPTION;
    }

    return true;
  }

  private static void synchronizeNodesSelection(
      final CombinedGraph combinedGraph, final Set<CombinedDiffNode> selectedNodes) {
    final Collection<CombinedDiffNode> toSelectCombined = new ArrayList<>();
    final Collection<CombinedDiffNode> toUnselectCombined = new ArrayList<>();

    final Collection<SuperDiffNode> toSelectSuper = new ArrayList<>();
    final Collection<SuperDiffNode> toUnselectSuper = new ArrayList<>();

    final Collection<SingleDiffNode> toSelectPrimarySingle = new ArrayList<>();
    final Collection<SingleDiffNode> toUnselectPrimarySingle = new ArrayList<>();
    final Collection<SingleDiffNode> toSelectSecondarySingle = new ArrayList<>();
    final Collection<SingleDiffNode> toUnselectSecondarySingle = new ArrayList<>();

    for (final CombinedDiffNode combinedNode : combinedGraph.getNodes()) {
      final boolean isSelected = selectedNodes.contains(combinedNode);

      final SingleDiffNode primaryNode = combinedNode.getPrimaryDiffNode();
      final SingleDiffNode secondaryNode = combinedNode.getSecondaryDiffNode();

      final SuperDiffNode superNode = combinedNode.getSuperDiffNode();

      final Collection<SuperDiffNode> workSuper = isSelected ? toSelectSuper : toUnselectSuper;
      final Collection<CombinedDiffNode> workCombined =
          isSelected ? toSelectCombined : toUnselectCombined;
      final Collection<SingleDiffNode> workPrimarySingle =
          isSelected ? toSelectPrimarySingle : toUnselectPrimarySingle;
      final Collection<SingleDiffNode> workSecondarySingle =
          isSelected ? toSelectSecondarySingle : toUnselectSecondarySingle;

      workCombined.add(combinedNode);
      workSuper.add(superNode);
      if (primaryNode != null) {
        workPrimarySingle.add(primaryNode);
      }
      if (secondaryNode != null) {
        workSecondarySingle.add(secondaryNode);
      }
    }

    combinedGraph.selectNodes(toSelectCombined, toUnselectCombined);
    selectRawEdges(combinedGraph);

    selectRawGraph(combinedGraph.getSuperGraph(), toSelectSuper, toUnselectSuper);
    selectRawGraph(combinedGraph.getPrimaryGraph(), toSelectPrimarySingle, toUnselectPrimarySingle);
    selectRawGraph(
        combinedGraph.getSecondaryGraph(), toSelectSecondarySingle, toUnselectSecondarySingle);

    updateAllGraphViews(combinedGraph);
  }

  private static void synchronizeNodesSelection(
      final SingleGraph singleGraph, final Set<SingleDiffNode> selectedNodes) {
    final ESide side = singleGraph.getSide(); // Are we primary or secondary ?

    final Collection<CombinedDiffNode> toSelectCombined = new ArrayList<>();
    final Collection<CombinedDiffNode> toUnselectCombined = new ArrayList<>();

    final Collection<SuperDiffNode> toSelectSuper = new ArrayList<>();
    final Collection<SuperDiffNode> toUnselectSuper = new ArrayList<>();

    final Collection<SingleDiffNode> toSelectSingleOther = new ArrayList<>();
    final Collection<SingleDiffNode> toUnselectSingleOther = new ArrayList<>();

    for (final CombinedDiffNode combinedNode : singleGraph.getCombinedGraph().getNodes()) {
      final SingleDiffNode thisNode =
          side == ESide.PRIMARY
              ? combinedNode.getPrimaryDiffNode()
              : combinedNode.getSecondaryDiffNode();
      final SingleDiffNode otherSideNode =
          side == ESide.SECONDARY
              ? combinedNode.getPrimaryDiffNode()
              : combinedNode.getSecondaryDiffNode();

      final SuperDiffNode superNode = combinedNode.getSuperDiffNode();

      final boolean select =
          (thisNode == null && otherSideNode.isSelected())
              || (thisNode != null && selectedNodes.contains(thisNode));

      final Collection<SuperDiffNode> workSelectionSuper = select ? toSelectSuper : toUnselectSuper;
      final Collection<CombinedDiffNode> workSelectionCombined =
          select ? toSelectCombined : toUnselectCombined;
      final Collection<SingleDiffNode> workSelectionSingleOther =
          select ? toSelectSingleOther : toUnselectSingleOther;

      workSelectionCombined.add(combinedNode);
      workSelectionSuper.add(superNode);
      if (otherSideNode != null) {
        workSelectionSingleOther.add(otherSideNode);
      }
    }

    selectRawGraph(singleGraph.getOtherSideGraph(), toSelectSingleOther, toUnselectSingleOther);
    selectRawGraph(singleGraph.getCombinedGraph(), toSelectCombined, toUnselectCombined);
    selectRawGraph(singleGraph.getSuperGraph(), toSelectSuper, toUnselectSuper);

    selectRawEdges(singleGraph);

    updateAllGraphViews(singleGraph);
  }

  private static void updateAllGraphViews(final BinDiffGraph<?, ?> combinedGraph) {
    combinedGraph.getPrimaryGraph().getGraph().updateViews();
    combinedGraph.getSecondaryGraph().getGraph().updateViews();
    combinedGraph.getSuperGraph().getGraph().updateViews();
    combinedGraph.getCombinedGraph().getGraph().updateViews();
  }

  private static boolean willChangeNodeVisibility(
      final BinDiffGraph<ZyGraphNode<? extends IViewNode<?>>, ?> graph,
      final Set<ZyGraphNode<? extends IViewNode<?>>> selectedNodes) {
    final GraphProximityBrowsingSettings settings = graph.getSettings().getProximitySettings();

    if (settings.getProximityBrowsing() && !settings.getProximityBrowsingFrozen()) {
      final int childDepth = settings.getProximityBrowsingChildren();
      final int parentDepth = settings.getProximityBrowsingParents();

      final Set<ZyGraphNode<? extends IViewNode<?>>> neighbours;
      neighbours =
          ProximityRangeCalculator.getNeighbors(graph, selectedNodes, childDepth, parentDepth);

      for (final ZyGraphNode<? extends IViewNode<?>> node : graph.getNodes()) {
        if (node.isVisible()) {
          if (!selectedNodes.contains(node) && !neighbours.contains(node)) {
            return true;
          }
        } else {
          if (selectedNodes.contains(node) || neighbours.contains(node)) {
            return true;
          }
        }
      }
    }

    return false;
  }

  public static void executeStatic(final BinDiffGraph<ZyGraphNode<? extends IViewNode<?>>, ?> graph)
      throws GraphLayoutException {
    if (graph.getSettings().isSync()) {
      if ((BinDiffGraph<?, ?>) graph instanceof SingleGraph) {
        synchronizeNodesSelection(
            (SingleGraph) (BinDiffGraph<?, ?>) graph,
            ((SingleGraph) (BinDiffGraph<?, ?>) graph).getSelectedNodes());
      } else if ((BinDiffGraph<?, ?>) graph instanceof CombinedGraph) {
        synchronizeNodesSelection(
            (CombinedGraph) (BinDiffGraph<?, ?>) graph,
            ((CombinedGraph) (BinDiffGraph<?, ?>) graph).getSelectedNodes());
      }

      updateAllGraphViews(graph);
    } else {
      if ((BinDiffGraph<?, ?>) graph instanceof CombinedGraph) {
        final CombinedGraph combinedGraph = (CombinedGraph) (BinDiffGraph<?, ?>) graph;
        final Set<CombinedDiffNode> nodesToSelect = combinedGraph.getSelectedNodes();
        final Set<CombinedDiffNode> nodesToUnselect = new HashSet<>();
        nodesToUnselect.addAll(combinedGraph.getNodes());
        nodesToUnselect.removeAll(nodesToSelect);

        combinedGraph.selectNodes(nodesToSelect, nodesToUnselect);
      }

      selectRawEdges((BinDiffGraph<?, ?>) graph);
    }

    GraphsIntermediateListeners.notifyIntermediateSelectionListeners(graph);

    if (LayoutCommandHelper.hasSelectedNodes(graph)
        && willChangeNodeVisibility(graph, graph.getSelectedNodes())) {
      if (showVisibilityWarningDialog(graph)) {
        ProximityBrowserUpdater.executeStatic(graph);

        if (LayoutCommandHelper.isAutolayout(graph)
            && !LayoutCommandHelper.isProximityBrowsingFrozen(graph)) {
          GraphLayoutUpdater.executeStatic(graph, true);
        }
      }
    }
  }

  @Override
  public void execute() throws GraphLayoutException {
    GraphSelectionUpdater.executeStatic(referenceGraph);
  }
}
