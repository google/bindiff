package com.google.security.zynamics.bindiff.graph;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.EGraphType;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.exceptions.GraphLayoutException;
import com.google.security.zynamics.bindiff.graph.builders.ViewFlowGraphBuilder;
import com.google.security.zynamics.bindiff.graph.edges.SingleDiffEdge;
import com.google.security.zynamics.bindiff.graph.edges.SuperViewEdge;
import com.google.security.zynamics.bindiff.graph.editmode.SingleGraphEditMode;
import com.google.security.zynamics.bindiff.graph.layout.LayoutCommandHelper;
import com.google.security.zynamics.bindiff.graph.layout.commands.ProximityBrowserUnhideNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffBasicBlockNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperViewNode;
import com.google.security.zynamics.bindiff.graph.realizers.CodeNodeRealizerUpdater;
import com.google.security.zynamics.bindiff.graph.realizers.SingleEdgeRealizer;
import com.google.security.zynamics.bindiff.graph.searchers.GraphSearcher;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
import com.google.security.zynamics.bindiff.gui.tabpanels.viewtabpanel.selectionhistory.SelectionHistory;
import com.google.security.zynamics.bindiff.project.matches.FunctionMatchData;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedFlowGraph;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedJump;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawJump;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.gui.zygraph.edges.ZyEdgeData;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.ZyNodeData;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.ZyGraph2DView;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.ZyEditMode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyNormalNodeRealizer;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import y.base.Edge;
import y.base.Node;
import y.view.Graph2D;

public class SingleGraph extends BinDiffGraph<SingleDiffNode, SingleDiffEdge> {
  private SelectionHistory selectionHistory =
      new SelectionHistory(this, GraphSettings.MAX_SELECTION_UNDO_CACHE);

  private GraphSearcher graphSearcher = new GraphSearcher();

  private final IAddress functionAddress;

  private final ESide side;

  public SingleGraph(
      final ZyGraph2DView view,
      final IAddress address,
      final LinkedHashMap<Node, SingleDiffNode> nodeMap,
      final LinkedHashMap<Edge, SingleDiffEdge> edgeMap,
      final GraphSettings settings,
      final ESide side,
      final EGraphType graphType) {
    super(view, nodeMap, edgeMap, settings, graphType);

    Preconditions.checkNotNull(side);

    functionAddress = address;
    this.side = side;
  }

  public static SingleDiffEdge buildDiffEdge(
      final SingleGraph diffGraph, final SuperViewEdge<? extends SuperViewNode> rawSuperJump)
      throws GraphLayoutException {
    SingleDiffEdge diffEdge = null;
    Edge yEdge = null;

    @SuppressWarnings("unchecked")
    final RawCombinedJump<RawCombinedBasicBlock> rawCombinedJump =
        (RawCombinedJump<RawCombinedBasicBlock>) rawSuperJump.getCombinedEdge();

    final RawBasicBlock rawSourceNode = rawCombinedJump.getSource().getRawNode(diffGraph.getSide());
    final RawBasicBlock rawTargetNode = rawCombinedJump.getTarget().getRawNode(diffGraph.getSide());

    final RawJump rawJump =
        diffGraph.getSide() == ESide.PRIMARY
            ? rawCombinedJump.getPrimaryEdge()
            : rawCombinedJump.getSecondaryEdge();

    if (rawJump == null) {
      return null;
    }

    SingleEdgeRealizer edgeRealizer = null;

    if (rawSourceNode != null && rawTargetNode != null && rawJump != null) {
      final ZyLabelContent edgeContent = new ZyLabelContent(null);
      edgeRealizer = new SingleEdgeRealizer(edgeContent, null, diffGraph.getSettings());

      final SingleDiffNode sourceNode = diffGraph.getNode(rawSourceNode);
      final SingleDiffNode targetNode = diffGraph.getNode(rawTargetNode);

      final boolean srcVisible = sourceNode.getRawNode().isVisible();
      final boolean tarVisible = targetNode.getRawNode().isVisible();
      sourceNode.getRawNode().setVisible(true);
      targetNode.getRawNode().setVisible(true);

      yEdge = diffGraph.getGraph().createEdge(sourceNode.getNode(), targetNode.getNode());

      diffEdge =
          new SingleDiffEdge(
              sourceNode, targetNode, yEdge, edgeRealizer, rawJump, diffGraph.getSide());
      SingleDiffNode.link(sourceNode, targetNode);
      edgeRealizer.setUserData(new ZyEdgeData<>(diffEdge));

      sourceNode.getRawNode().setVisible(srcVisible);
      targetNode.getRawNode().setVisible(tarVisible);

      diffGraph.addEdgeToMappings(diffEdge);

      // TODO: Keep proximity nodes instead of resolving them here.
      if (!srcVisible || !tarVisible) {
        final BinDiffGraph<?, ?> graph = diffGraph;
        @SuppressWarnings("unchecked")
        final BinDiffGraph<ZyGraphNode<?>, ?> castedGraph = (BinDiffGraph<ZyGraphNode<?>, ?>) graph;

        final boolean autoLayout = LayoutCommandHelper.deactiveAutoLayout(castedGraph);
        try {
          ProximityBrowserUnhideNode.executeStatic(
              castedGraph, srcVisible ? (ZyGraphNode<?>) sourceNode : (ZyGraphNode<?>) targetNode);
        } finally {
          LayoutCommandHelper.activateAutoLayout(castedGraph, autoLayout);
        }
      }
    }

    return diffEdge;
  }

  public static SingleDiffNode buildDiffNode(
      final SingleGraph singleGraph,
      final FunctionMatchData functionMatch,
      final RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
          flowgraph,
      final RawCombinedBasicBlock combinedBasicblock) {
    SingleDiffNode diffNode = null;
    ZyLabelContent nodeContent = null;

    final Graph2D graph2D = singleGraph.getGraph();
    final RawBasicBlock rawBasicblock = combinedBasicblock.getRawNode(singleGraph.getSide());

    if (rawBasicblock != null) {
      nodeContent =
          ViewFlowGraphBuilder.buildSingleBasicblockLabelContent(
              functionMatch, flowgraph, combinedBasicblock, singleGraph.getSide());
      final ZyNormalNodeRealizer<SingleDiffNode> nodeRealizer =
          new ZyNormalNodeRealizer<>(nodeContent);

      final CodeNodeRealizerUpdater updater = new CodeNodeRealizerUpdater();
      nodeRealizer.setUpdater(updater);
      updater.setRealizer(nodeRealizer);

      final Node yNode = graph2D.createNode();
      diffNode =
          new SingleDiffBasicBlockNode(yNode, nodeRealizer, rawBasicblock, singleGraph.getSide());

      nodeRealizer.setUserData(new ZyNodeData<>(diffNode));
    }

    return diffNode;
  }

  @Override
  protected ZyEditMode<SingleDiffNode, SingleDiffEdge> createEditMode() {
    return new SingleGraphEditMode(this);
  }

  @Override
  public void dispose() {
    selectionHistory.dispose();

    super.dispose();

    graphSearcher.clearResults();

    selectionHistory = null;
    graphSearcher = null;
  }

  public IAddress getFunctionAddress() {
    return functionAddress;
  }

  @Override
  public GraphSearcher getGraphSearcher() {
    return graphSearcher;
  }

  public SingleGraph getOtherSideGraph() {
    return side == ESide.PRIMARY ? getSecondaryGraph() : getPrimaryGraph();
  }

  @Override
  public Set<SingleDiffNode> getSelectedNodes() {
    return new HashSet<>(
        CollectionHelpers.filter(
            super.getMappings().getNodes(),
            new ICollectionFilter<SingleDiffNode>() {
              @Override
              public boolean qualifies(final SingleDiffNode item) {
                return item.isSelected();
              }
            }));
  }

  public SelectionHistory getSelectionHistory() {
    return selectionHistory;
  }

  public ESide getSide() {
    return side;
  }
}
