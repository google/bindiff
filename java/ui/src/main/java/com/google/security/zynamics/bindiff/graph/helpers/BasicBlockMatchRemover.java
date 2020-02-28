// Copyright 2011-2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.bindiff.graph.helpers;

import com.google.security.zynamics.bindiff.enums.EDiffViewMode;
import com.google.security.zynamics.bindiff.enums.EJumpType;
import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.exceptions.GraphLayoutException;
import com.google.security.zynamics.bindiff.graph.BinDiffGraph;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.GraphsContainer;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.SuperGraph;
import com.google.security.zynamics.bindiff.graph.builders.ViewFlowGraphBuilder;
import com.google.security.zynamics.bindiff.graph.edges.CombinedDiffEdge;
import com.google.security.zynamics.bindiff.graph.edges.SingleDiffEdge;
import com.google.security.zynamics.bindiff.graph.edges.SuperDiffEdge;
import com.google.security.zynamics.bindiff.graph.edges.SuperViewEdge;
import com.google.security.zynamics.bindiff.graph.labelcontent.lineeditor.BasicBlockContentEditor;
import com.google.security.zynamics.bindiff.graph.layout.LayoutCommandHelper;
import com.google.security.zynamics.bindiff.graph.layout.commands.GraphLayoutUpdater;
import com.google.security.zynamics.bindiff.graph.layout.commands.ProximityBrowserUnhideNode;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffBasicBlockNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperViewNode;
import com.google.security.zynamics.bindiff.graph.realizers.CodeNodeRealizerUpdater;
import com.google.security.zynamics.bindiff.graph.realizers.CombinedEdgeRealizer;
import com.google.security.zynamics.bindiff.graph.realizers.CombinedNodeRealizer;
import com.google.security.zynamics.bindiff.graph.realizers.SingleEdgeRealizer;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
import com.google.security.zynamics.bindiff.project.matches.FunctionMatchData;
import com.google.security.zynamics.bindiff.project.matches.MatchData;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedFlowGraph;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedJump;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawFlowGraph;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawJump;
import com.google.security.zynamics.bindiff.project.userview.FlowGraphViewData;
import com.google.security.zynamics.bindiff.project.userview.ViewManager;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.gui.zygraph.edges.ZyEdgeData;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.ZyNodeData;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyNormalNodeRealizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import y.base.Edge;
import y.base.Node;
import y.view.Graph2D;

/** Utility class to remove basic block matches from BinDiff graphs. */
public class BasicBlockMatchRemover {
  private static CombinedDiffEdge buildDiffEdge(
      final CombinedGraph diffGraph,
      final SuperViewEdge<? extends SuperViewNode> rawSuperJump,
      final SuperDiffEdge superDiffEdge)
      throws GraphLayoutException {
    @SuppressWarnings("unchecked")
    final RawCombinedJump<RawCombinedBasicBlock> rawCombinedJump =
        (RawCombinedJump<RawCombinedBasicBlock>) rawSuperJump.getCombinedEdge();

    final ZyLabelContent combinedEdgeContent = new ZyLabelContent(null);
    final CombinedEdgeRealizer combinedEdgeRealizer =
        new CombinedEdgeRealizer(combinedEdgeContent, null, diffGraph.getSettings());

    final CombinedDiffNode srcCombinedDiffNode = diffGraph.getNode(rawCombinedJump.getSource());
    final CombinedDiffNode tarCombinedDiffNode = diffGraph.getNode(rawCombinedJump.getTarget());

    final Node srcCombinedYNode = srcCombinedDiffNode.getNode();
    final Node tarCombinedYNode = tarCombinedDiffNode.getNode();

    final boolean srcVisible = srcCombinedDiffNode.getRawNode().isVisible();
    final boolean tarVisible = tarCombinedDiffNode.getRawNode().isVisible();
    srcCombinedDiffNode.getRawNode().setVisible(true);
    tarCombinedDiffNode.getRawNode().setVisible(true);

    Edge yCombinedEdge = diffGraph.getGraph().createEdge(srcCombinedYNode, tarCombinedYNode);

    final CombinedDiffEdge combinedDiffEdge =
        new CombinedDiffEdge(
            srcCombinedDiffNode,
            tarCombinedDiffNode,
            yCombinedEdge,
            combinedEdgeRealizer,
            rawCombinedJump,
            superDiffEdge);
    CombinedDiffNode.link(srcCombinedDiffNode, tarCombinedDiffNode);
    combinedEdgeRealizer.setUserData(new ZyEdgeData<>(combinedDiffEdge));

    srcCombinedDiffNode.getRawNode().setVisible(srcVisible);
    tarCombinedDiffNode.getRawNode().setVisible(tarVisible);

    diffGraph.addEdgeToMappings(combinedDiffEdge);

    if (!srcVisible || !tarVisible) {
      final BinDiffGraph<?, ?> graph = diffGraph;
      @SuppressWarnings("unchecked")
      final BinDiffGraph<ZyGraphNode<?>, ?> castedGraph = (BinDiffGraph<ZyGraphNode<?>, ?>) graph;

      final boolean autoLayout = LayoutCommandHelper.deactiveAutoLayout(castedGraph);
      try {
        ProximityBrowserUnhideNode.executeStatic(
            castedGraph,
            srcVisible
                ? (ZyGraphNode<?>) srcCombinedDiffNode
                : (ZyGraphNode<?>) tarCombinedDiffNode);
      } finally {
        LayoutCommandHelper.activateAutoLayout(castedGraph, autoLayout);
      }
    }

    return combinedDiffEdge;
  }

  private static SingleDiffEdge buildDiffEdge(
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

  private static SuperDiffEdge buildDiffEdge(
      final SuperGraph diffGraph,
      final SuperViewEdge<? extends SuperViewNode> rawSuperJump,
      final SingleDiffEdge primaryDiffEdge,
      final SingleDiffEdge secondaryDiffEdge) {
    final ZyLabelContent superEdgeContent = new ZyLabelContent(null);
    final ZyEdgeRealizer<SuperDiffEdge> superEdgeRealizer =
        new ZyEdgeRealizer<>(superEdgeContent, null);

    SuperDiffNode srcSuperDiffNode = null;
    SuperDiffNode tarSuperDiffNode = null;

    if (primaryDiffEdge != null) {
      srcSuperDiffNode = primaryDiffEdge.getSource().getSuperDiffNode();
      tarSuperDiffNode = primaryDiffEdge.getTarget().getSuperDiffNode();
    } else if (secondaryDiffEdge != null) {
      srcSuperDiffNode = secondaryDiffEdge.getSource().getSuperDiffNode();
      tarSuperDiffNode = secondaryDiffEdge.getTarget().getSuperDiffNode();
    }

    final Node srcSuperYNode = srcSuperDiffNode.getNode();
    final Node tarSuperYNode = tarSuperDiffNode.getNode();

    final boolean srcVisible = srcSuperDiffNode.getRawNode().isVisible();
    final boolean tarVisible = tarSuperDiffNode.getRawNode().isVisible();
    srcSuperDiffNode.getRawNode().setVisible(true);
    tarSuperDiffNode.getRawNode().setVisible(true);

    Edge ySuperEdge = diffGraph.getGraph().createEdge(srcSuperYNode, tarSuperYNode);
    final SuperDiffEdge superDiffEdge =
        new SuperDiffEdge(
            srcSuperDiffNode,
            tarSuperDiffNode,
            ySuperEdge,
            superEdgeRealizer,
            rawSuperJump,
            primaryDiffEdge,
            secondaryDiffEdge);
    SuperDiffNode.link(srcSuperDiffNode, tarSuperDiffNode);
    superEdgeRealizer.setUserData(new ZyEdgeData<>(superDiffEdge));

    srcSuperDiffNode.getRawNode().setVisible(srcVisible);
    tarSuperDiffNode.getRawNode().setVisible(tarVisible);

    diffGraph.addEdgeToMappings(superDiffEdge);

    return superDiffEdge;
  }

  private static CombinedDiffNode buildDiffNode(
      final CombinedGraph combinedGraph,
      final SingleDiffNode primaryDiffNode,
      final SingleDiffNode secondaryDiffNode,
      final SuperDiffNode superDiffNode,
      final RawCombinedBasicBlock combinedBasicblock) {
    ZyLabelContent primaryNodeContent = null;
    if (primaryDiffNode != null) {
      primaryNodeContent = primaryDiffNode.getRealizer().getNodeContent();
    }

    ZyLabelContent secondaryNodeContent = null;
    if (secondaryDiffNode != null) {
      secondaryNodeContent = secondaryDiffNode.getRealizer().getNodeContent();
    }

    final CombinedNodeRealizer combinedNodeRealizer =
        new CombinedNodeRealizer(primaryNodeContent, secondaryNodeContent);

    final CodeNodeRealizerUpdater updater = new CodeNodeRealizerUpdater();
    combinedNodeRealizer.setUpdater(updater);
    updater.setRealizer(combinedNodeRealizer);

    final Node yCombinedNode = combinedGraph.getGraph().createNode();
    final CombinedDiffNode combinedDiffNode =
        new CombinedDiffNode(
            yCombinedNode, combinedNodeRealizer, combinedBasicblock, superDiffNode);
    combinedNodeRealizer.setUserData(new ZyNodeData<>(combinedDiffNode));

    return combinedDiffNode;
  }

  private static SingleDiffNode buildDiffNode(
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
          ViewFlowGraphBuilder.buildSingleBasicBlockLabelContent(
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

  private static SuperDiffNode buildDiffNode(
      final SuperGraph superGraph,
      final SingleDiffNode primaryDiffNode,
      final SingleDiffNode secondaryDiffNode,
      final SuperViewNode superBasicblock) {
    final ZyLabelContent superNodeContent = new ZyLabelContent(null);
    final ZyNormalNodeRealizer<SuperDiffNode> superNodeRealizer =
        new ZyNormalNodeRealizer<>(superNodeContent);

    final Node ySuperNode = superGraph.getGraph().createNode();

    final SuperDiffNode superDiffNode =
        new SuperDiffNode(
            ySuperNode, superNodeRealizer, superBasicblock, primaryDiffNode, secondaryDiffNode);
    superNodeRealizer.setUserData(new ZyNodeData<>(superDiffNode));

    return superDiffNode;
  }

  private static RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
      getRawCombinedFlowgraph(final GraphsContainer graphs, final CombinedDiffNode node) {
    final IAddress priAddress =
        ((RawCombinedBasicBlock) node.getRawNode()).getPrimaryFunctionAddress();
    final IAddress secAddress =
        ((RawCombinedBasicBlock) node.getRawNode()).getSecondaryFunctionAddress();

    final ViewManager viewManager = graphs.getDiff().getViewManager();
    final FlowGraphViewData viewData = viewManager.getFlowGraphViewData(priAddress, secAddress);

    return viewData.getCombinedRawGraph();
  }

  private static void insertNewDiffEdges(
      final GraphsContainer graphs, final SuperDiffNode newSuperDiffNode)
      throws GraphLayoutException {
    final SuperGraph superGraph = graphs.getSuperGraph();
    final CombinedGraph combinedGraph = graphs.getCombinedGraph();
    final SingleGraph priGraph = graphs.getPrimaryGraph();
    final SingleGraph secGraph = graphs.getSecondaryGraph();

    final SuperViewNode superViewNode = newSuperDiffNode.getRawNode();
    final Set<SuperViewEdge<? extends SuperViewNode>> newRawSuperJumps = new HashSet<>();
    newRawSuperJumps.addAll(superViewNode.getIncomingEdges());
    newRawSuperJumps.addAll(superViewNode.getOutgoingEdges());

    for (final SuperViewEdge<? extends SuperViewNode> newRawSuperJump : newRawSuperJumps) {
      // build jumps
      final SingleDiffEdge primaryDiffEdge = buildDiffEdge(priGraph, newRawSuperJump);
      final SingleDiffEdge secondaryDiffEdge = buildDiffEdge(secGraph, newRawSuperJump);
      final SuperDiffEdge superDiffEdge =
          buildDiffEdge(superGraph, newRawSuperJump, primaryDiffEdge, secondaryDiffEdge);
      final CombinedDiffEdge combinedDiffEdge =
          buildDiffEdge(combinedGraph, newRawSuperJump, superDiffEdge);

      // each edge gets it's representatives of the parallel views
      ZyEdgeRealizer<SingleDiffEdge> priEdgeRealizer = null;
      ZyEdgeRealizer<SingleDiffEdge> secEdgeRealizer = null;

      superDiffEdge.setCombinedDiffEdge(combinedDiffEdge);
      if (primaryDiffEdge != null) {
        primaryDiffEdge.setCombinedDiffEdge(combinedDiffEdge);
        priEdgeRealizer = primaryDiffEdge.getRealizer();
      }
      if (secondaryDiffEdge != null) {
        secondaryDiffEdge.setCombinedDiffEdge(combinedDiffEdge);
        secEdgeRealizer = secondaryDiffEdge.getRealizer();
      }

      // colorize and stylize jumps
      @SuppressWarnings("unchecked")
      final RawCombinedJump<RawCombinedBasicBlock> rawCombinedJump =
          (RawCombinedJump<RawCombinedBasicBlock>) newRawSuperJump.getCombinedEdge();
      ViewFlowGraphBuilder.colorizeJumps(rawCombinedJump, priEdgeRealizer, secEdgeRealizer);
      ViewFlowGraphBuilder.stylizeJumps(
          rawCombinedJump, combinedDiffEdge.getRealizer(), priEdgeRealizer, secEdgeRealizer);
    }
  }

  private static void insertNewDiffNodes(
      final GraphsContainer graphs,
      final RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
          combinedRawFlowgraph,
      final SuperViewNode newRawSuperBasicblock)
      throws GraphLayoutException {
    final RawCombinedBasicBlock newRawCombinedBasicblock =
        (RawCombinedBasicBlock) newRawSuperBasicblock.getCombinedNode();
    final ESide side =
        newRawCombinedBasicblock.getRawNode(ESide.PRIMARY) != null
            ? ESide.PRIMARY
            : ESide.SECONDARY;

    final CombinedGraph combinedGraph = graphs.getCombinedGraph();
    final SingleGraph singleGraph =
        side == ESide.PRIMARY ? graphs.getPrimaryGraph() : graphs.getSecondaryGraph();
    final SuperGraph superGraph = graphs.getSuperGraph();

    // create new diff nodes
    final MatchData matches = graphs.getDiff().getMatches();
    final FunctionMatchData functionMatch =
        matches.getFunctionMatch(
            side == ESide.PRIMARY
                ? newRawCombinedBasicblock.getPrimaryFunctionAddress()
                : newRawCombinedBasicblock.getSecondaryFunctionAddress(),
            side);

    final SingleDiffNode newSingleDiffNode =
        buildDiffNode(singleGraph, functionMatch, combinedRawFlowgraph, newRawCombinedBasicblock);
    final SuperDiffNode newSuperDiffNode =
        buildDiffNode(
            superGraph,
            side == ESide.PRIMARY ? newSingleDiffNode : null,
            side == ESide.SECONDARY ? newSingleDiffNode : null,
            newRawSuperBasicblock);
    final CombinedDiffNode newCombinedDiffNode =
        buildDiffNode(
            combinedGraph,
            side == ESide.PRIMARY ? newSingleDiffNode : null,
            side == ESide.SECONDARY ? newSingleDiffNode : null,
            newSuperDiffNode,
            newRawCombinedBasicblock);

    // each node gets it's representatives of the parallel views
    // update node mappings
    // set node content editors
    newSuperDiffNode.setCombinedDiffNode(newCombinedDiffNode);
    newSingleDiffNode.setCombinedDiffNode(newCombinedDiffNode);
    singleGraph.addNodeToMappings(newSingleDiffNode);
    final BasicBlockContentEditor nodeEditor =
        new BasicBlockContentEditor(functionMatch, graphs, side);
    newSingleDiffNode.getRealizer().getNodeContent().setLineEditor(nodeEditor);

    combinedGraph.addNodeToMappings(newCombinedDiffNode);
    superGraph.addNodeToMappings(newSuperDiffNode);

    // colorize new basicblock node's background and border color
    ViewFlowGraphBuilder.colorizeBasicBlocks(functionMatch, newRawCombinedBasicblock);
    ViewFlowGraphBuilder.colorizeCombinedNodeLineBorders(
        combinedGraph.getNodes(),
        combinedGraph.getPrimaryGraph().getFunctionAddress(),
        combinedGraph.getSecondaryGraph().getFunctionAddress());
    ViewFlowGraphBuilder.colorizeSingleNodeLineBorders(
        singleGraph.getNodes(), singleGraph.getFunctionAddress());

    // refresh super node's width and height
    superGraph.refreshSuperNodeSize(
        graphs.getPrimaryGraph(), graphs.getSecondaryGraph(), newSuperDiffNode);

    // create new diff edges and add to graphs
    insertNewDiffEdges(graphs, newSuperDiffNode);
  }

  private static void insertNewRawNodes(
      final RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
          combinedRawFlowgraph,
      final RawFlowGraph rawFlowgraph,
      final Set<SuperViewEdge<? extends SuperViewNode>> oldRawSuperJumps,
      final SuperViewNode oldRawSuperBasicBlock,
      final RawBasicBlock newRawBasicBlock,
      final RawCombinedBasicBlock newRawCombinedBasicBlock,
      final SuperViewNode newRawSuperBasicBlock) {
    // Add new raw basic blocks to graphs
    combinedRawFlowgraph.addNode(newRawCombinedBasicBlock);
    rawFlowgraph.addNode(newRawBasicBlock);

    // iterate over all the old edges and create corresponding new ones
    // this edges are already removed out of the graphs
    final ESide side = rawFlowgraph.getSide();
    for (final SuperViewEdge<? extends SuperViewNode> oldRawSuperJump : oldRawSuperJumps) {
      if (oldRawSuperJump.getSingleEdge(side) == null) {
        continue;
      }

      SuperViewNode superSource = oldRawSuperJump.getSource();
      RawCombinedBasicBlock combinedSource = (RawCombinedBasicBlock) superSource.getCombinedNode();
      RawBasicBlock source = combinedSource.getRawNode(side);

      SuperViewNode superTarget = oldRawSuperJump.getTarget();
      RawCombinedBasicBlock combinedTarget = (RawCombinedBasicBlock) superTarget.getCombinedNode();
      RawBasicBlock target = combinedTarget.getRawNode(side);

      // Get target basic blocks
      if (superTarget == oldRawSuperBasicBlock) {
        superTarget = newRawSuperBasicBlock;
        combinedTarget = newRawCombinedBasicBlock;
        target = newRawBasicBlock;
      }

      // Get source basic blocks - no else in case of recursive basic blocks
      if (superSource == oldRawSuperBasicBlock) {
        superSource = newRawSuperBasicBlock;
        combinedSource = newRawCombinedBasicBlock;
        source = newRawBasicBlock;
      }

      // Create and add raw jump
      final EJumpType type = ((RawJump) oldRawSuperJump.getSingleEdge(side)).getJumpType();
      final RawJump rawJump = new RawJump(source, target, type);
      rawFlowgraph.addEdge(rawJump);

      // Create and add combined raw jump
      final RawCombinedJump<RawCombinedBasicBlock> newRawCombinedJump =
          new RawCombinedJump<>(
              combinedSource,
              combinedTarget,
              side == ESide.PRIMARY ? rawJump : null,
              side == ESide.SECONDARY ? rawJump : null);
      combinedRawFlowgraph.addEdge(newRawCombinedJump);

      // Create and link super raw jump (see super view edge ctor)
      new SuperViewEdge<>(newRawCombinedJump, superSource, superTarget);
    }
  }

  protected static void syncBasicBlockVisibility(
      final GraphsContainer graphs, final CombinedDiffNode node) {
    final GraphSettings settings = graphs.getSettings();
    if (settings.isAsync()) {
      final boolean autoLayout = LayoutCommandHelper.deactiveAutoLayout(graphs.getCombinedGraph());
      try {
        if (settings.getDiffViewMode() == EDiffViewMode.COMBINED_VIEW) {
          node.getPrimaryRawNode().setVisible(true);
          node.getSecondaryRawNode().setVisible(true);
          node.getSuperRawNode().setVisible(true);
        } else if (settings.getDiffViewMode() == EDiffViewMode.NORMAL_VIEW) {
          node.getRawNode().setVisible(true);
          node.getPrimaryRawNode().setVisible(true);
          node.getSecondaryRawNode().setVisible(true);
          node.getSuperRawNode().setVisible(true);
        }
      } finally {
        LayoutCommandHelper.activateAutoLayout(graphs.getCombinedGraph(), autoLayout);
      }
    }
  }

  public static void doSynchronizedLayout(final CombinedGraph combinedGraph)
      throws GraphLayoutException {
    if (LayoutCommandHelper.isAutolayout(combinedGraph)) {
      final GraphSettings settings = combinedGraph.getSettings();
      if (settings.isSync() || settings.getDiffViewMode() == EDiffViewMode.COMBINED_VIEW) {
        GraphLayoutUpdater.executeStatic(combinedGraph, true);
      } else if (settings.getDiffViewMode() == EDiffViewMode.NORMAL_VIEW) {
        if (settings.getFocus() == ESide.PRIMARY) {
          GraphLayoutUpdater.executeStatic(combinedGraph.getPrimaryGraph(), true);
        }
        if (settings.getFocus() == ESide.SECONDARY) {
          GraphLayoutUpdater.executeStatic(combinedGraph.getSecondaryGraph(), true);
        }
      }
    }
  }

  public static List<CombinedDiffNode> getAffectedCombinedNodes(final BinDiffGraph<?, ?> graph) {
    final List<CombinedDiffNode> combinedNodes = new ArrayList<>();

    if (graph instanceof CombinedGraph) {
      for (final CombinedDiffNode node : ((CombinedGraph) graph).getSelectedNodes()) {
        if (node.getRawNode().getMatchState() == EMatchState.MATCHED) {
          combinedNodes.add(node);
        }
      }
    } else if (graph instanceof SingleGraph) {
      for (final SingleDiffNode node : ((SingleGraph) graph).getSelectedNodes()) {
        if (node.getRawNode().getMatchState() == EMatchState.MATCHED) {
          if (node.isSelected()
              && node.isVisible()
              && node.getOtherSideDiffNode().isSelected()
              && node.getOtherSideDiffNode().isVisible()) {
            combinedNodes.add(node.getCombinedDiffNode());
          }
        }
      }
    }

    if (combinedNodes.size() != 0) {
      return combinedNodes;
    }

    return null;
  }

  public static List<CombinedDiffNode> getAffectedCombinedNodes(
      final BinDiffGraph<?, ?> graph, final ZyGraphNode<?> clickedNode) {
    if (clickedNode.isSelected()) {
      return getAffectedCombinedNodes(graph);
    }

    return null;
  }

  private static void removeBasicBlockMatch(
      final GraphsContainer graphs, final RawCombinedBasicBlock oldRawCombinedBasicBlock) {
    final MatchData matches = graphs.getDiff().getMatches();
    final FunctionMatchData functionMatch =
        matches.getFunctionMatch(
            oldRawCombinedBasicBlock.getPrimaryFunctionAddress(), ESide.PRIMARY);
    functionMatch.removeBasicblockMatch(graphs.getDiff(), oldRawCombinedBasicBlock);
  }

  // TODO: On cancel, restore FunctionDiffData (reload from database) in order to update the
  // workspace counts
  // TODO: Do not forget to update the an open callgraph view if a basicblock has been removed (or
  // added)
  // - change node color if necessary
  // - change calls from matched to unmatched color
  // - change proximity nodes of combined
  // - change calls from unmatched to matched color (as soon as newly assigned basicblock comments
  // can be diffed)
  public static void removeBasicBlockMatch(
      final GraphsContainer graphs, final CombinedDiffNode oldCombinedDiffNode)
      throws GraphLayoutException {
    graphs.getCombinedGraph().getIntermediateListeners().blockZyLibVisibilityListeners();
    graphs.getCombinedGraph().getIntermediateListeners().blockZyLibSelectionListeners();

    try {
      final SuperDiffNode oldSuperDiffNode = oldCombinedDiffNode.getSuperDiffNode();
      final SingleDiffNode oldPrimaryDiffNode = oldCombinedDiffNode.getPrimaryDiffNode();
      final SingleDiffNode oldSecondaryDiffNode = oldCombinedDiffNode.getSecondaryDiffNode();

      final RawCombinedBasicBlock oldRawCombinedBasicBlock =
          (RawCombinedBasicBlock) oldCombinedDiffNode.getRawNode();
      final SuperViewNode oldRawSuperBasicBlock = oldSuperDiffNode.getRawNode();
      final RawBasicBlock oldRawPrimaryBasicBlock = (RawBasicBlock) oldPrimaryDiffNode.getRawNode();
      final RawBasicBlock oldRawSecondaryBasicBlock =
          (RawBasicBlock) oldSecondaryDiffNode.getRawNode();

      final RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
          combinedRawFlowGraph = getRawCombinedFlowgraph(graphs, oldCombinedDiffNode);
      final RawFlowGraph priRawFlowGraph = combinedRawFlowGraph.getPrimaryFlowgraph();
      final RawFlowGraph secRawFlowGraph = combinedRawFlowGraph.getSecondaryFlowgraph();

      // Sync visibility in async view mode
      syncBasicBlockVisibility(graphs, oldCombinedDiffNode);

      // Store old incoming and outgoing super jumps
      final Set<SuperViewEdge<? extends SuperViewNode>> oldRawSuperJumps = new HashSet<>();
      oldRawSuperJumps.addAll(oldRawSuperBasicBlock.getIncomingEdges());
      oldRawSuperJumps.addAll(oldRawSuperBasicBlock.getOutgoingEdges());

      // Delete raw nodes and edges
      combinedRawFlowGraph.removeNode(oldRawCombinedBasicBlock);
      oldRawSuperBasicBlock.removeNode();
      priRawFlowGraph.removeNode(oldRawPrimaryBasicBlock);
      secRawFlowGraph.removeNode(oldRawSecondaryBasicBlock);

      // Delete diff nodes and edges including possibly affected proximity nodes
      graphs.getCombinedGraph().deleteNode(oldCombinedDiffNode);
      graphs.getSuperGraph().deleteNode(oldSuperDiffNode);
      graphs.getPrimaryGraph().deleteNode(oldPrimaryDiffNode);
      graphs.getSecondaryGraph().deleteNode(oldSecondaryDiffNode);

      // Create new raw basic blocks
      final RawBasicBlock newRawPrimaryBasicBlock =
          oldRawPrimaryBasicBlock.clone(EMatchState.PRIMARY_UNMATCHED);
      final RawBasicBlock newRawSecondaryBasicBlock =
          oldRawSecondaryBasicBlock.clone(EMatchState.SECONDRAY_UNMATCHED);
      final RawCombinedBasicBlock newRawCombinedPriUnmatchedBasicBlock =
          new RawCombinedBasicBlock(
              newRawPrimaryBasicBlock, null, null, oldRawPrimaryBasicBlock.getFunctionAddr(), null);
      final RawCombinedBasicBlock newRawCombinedSecUnmatchedBasicBlock =
          new RawCombinedBasicBlock(
              null,
              newRawSecondaryBasicBlock,
              null,
              null,
              oldRawSecondaryBasicBlock.getFunctionAddr());
      final SuperViewNode newRawSuperPriUnmatchedBasicBlock =
          new SuperViewNode(newRawCombinedPriUnmatchedBasicBlock);
      final SuperViewNode newRawSuperSecUnmatchedBasicBlock =
          new SuperViewNode(newRawCombinedSecUnmatchedBasicBlock);

      // Add new raw basic blocks to raw graphs
      // Create new raw jumps and add jumps to raw graph
      // New primary unmatched nodes and edges
      insertNewRawNodes(
          combinedRawFlowGraph,
          priRawFlowGraph,
          oldRawSuperJumps,
          oldRawSuperBasicBlock,
          newRawPrimaryBasicBlock,
          newRawCombinedPriUnmatchedBasicBlock,
          newRawSuperPriUnmatchedBasicBlock);
      // New secondary unmatched nodes and edges
      insertNewRawNodes(
          combinedRawFlowGraph,
          secRawFlowGraph,
          oldRawSuperJumps,
          oldRawSuperBasicBlock,
          newRawSecondaryBasicBlock,
          newRawCombinedSecUnmatchedBasicBlock,
          newRawSuperSecUnmatchedBasicBlock);

      // Remove diff basic block match from function match data
      removeBasicBlockMatch(graphs, oldRawCombinedBasicBlock);

      // Create new diff nodes and add to diff graphs
      // Create new diff edges and add to diff graphs
      // New primary unmatched nodes and edges
      insertNewDiffNodes(graphs, combinedRawFlowGraph, newRawSuperPriUnmatchedBasicBlock);
      // New secondary unmatched nodes and edges
      insertNewDiffNodes(graphs, combinedRawFlowGraph, newRawSuperSecUnmatchedBasicBlock);

      // Notify intermediate listener to update graph node tree, selection history and main menu of
      // this view
      graphs
          .getDiff()
          .getMatches()
          .notifyBasicBlockMatchRemovedListener(
              newRawPrimaryBasicBlock.getFunctionAddr(),
                  newRawSecondaryBasicBlock.getFunctionAddr(),
              newRawPrimaryBasicBlock.getAddress(), newRawSecondaryBasicBlock.getAddress());
    } finally {
      graphs.getCombinedGraph().getIntermediateListeners().freeZyLibVisibilityListeners();
      graphs.getCombinedGraph().getIntermediateListeners().freeZyLibSelectionListeners();
    }

    // Layout graphs
    doSynchronizedLayout(graphs.getCombinedGraph());
  }
}
