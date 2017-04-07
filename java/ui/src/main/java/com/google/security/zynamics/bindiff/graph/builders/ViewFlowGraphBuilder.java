package com.google.security.zynamics.bindiff.graph.builders;

import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.enums.EGraphType;
import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.CombinedGraph;
import com.google.security.zynamics.bindiff.graph.GraphsContainer;
import com.google.security.zynamics.bindiff.graph.SingleGraph;
import com.google.security.zynamics.bindiff.graph.SuperGraph;
import com.google.security.zynamics.bindiff.graph.edges.CombinedDiffEdge;
import com.google.security.zynamics.bindiff.graph.edges.SingleDiffEdge;
import com.google.security.zynamics.bindiff.graph.edges.SuperDiffEdge;
import com.google.security.zynamics.bindiff.graph.edges.SuperViewEdge;
import com.google.security.zynamics.bindiff.graph.labelcontent.DiffLabelContent;
import com.google.security.zynamics.bindiff.graph.labelcontent.editableline.BasicBlockLineObject;
import com.google.security.zynamics.bindiff.graph.labelcontent.lineeditor.BasicBlockContentEditor;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffBasicBlockNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleViewNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperViewNode;
import com.google.security.zynamics.bindiff.graph.realizers.CodeNodeRealizerUpdater;
import com.google.security.zynamics.bindiff.graph.realizers.CombinedEdgeRealizer;
import com.google.security.zynamics.bindiff.graph.realizers.CombinedNodeRealizer;
import com.google.security.zynamics.bindiff.graph.realizers.SingleEdgeRealizer;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.matches.BasicBlockMatchData;
import com.google.security.zynamics.bindiff.project.matches.FunctionMatchData;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedFlowGraph;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedJump;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawJump;
import com.google.security.zynamics.bindiff.resources.Colors;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.zygraph.edges.ZyEdgeData;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.ZyNodeData;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.ZyGraph2DView;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyNormalNodeRealizer;

import y.base.Edge;
import y.base.Node;
import y.view.Graph2D;
import y.view.LineType;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ViewFlowGraphBuilder {
  protected static void addGraphEditors(
      final FunctionMatchData functionMatch, final GraphsContainer graphs) {
    final SingleGraph primaryGraph = graphs.getPrimaryGraph();

    if (primaryGraph != null) {
      for (final SingleDiffNode diffNode : primaryGraph.getNodes()) {
        final BasicBlockContentEditor nodeEditor =
            new BasicBlockContentEditor(functionMatch, graphs, ESide.PRIMARY);

        diffNode.getRealizer().getNodeContent().setLineEditor(nodeEditor);
      }
    }

    final SingleGraph secondaryGraph = graphs.getSecondaryGraph();

    if (secondaryGraph != null) {
      for (final SingleDiffNode diffNode : secondaryGraph.getNodes()) {
        final BasicBlockContentEditor nodeEditor =
            new BasicBlockContentEditor(functionMatch, graphs, ESide.SECONDARY);

        diffNode.getRealizer().getNodeContent().setLineEditor(nodeEditor);
      }
    }
  }

  protected static void buildFlowgraphEdgeMaps(
      final Graph2D primaryGraph2D,
      final Graph2D secondaryGraph2D,
      final Graph2D superGraph2D,
      final Graph2D combinedGraph2D,
      final Map<Edge, SingleDiffEdge> primaryEdgeMap,
      final Map<Edge, SingleDiffEdge> secondaryEdgeMap,
      final Map<Edge, SuperDiffEdge> superEdgeMap,
      final Map<Edge, CombinedDiffEdge> combinedEdgeMap,
      final Map<IAddress, SingleDiffNode> primaryAddrToSingleDiffNodeMap,
      final Map<IAddress, SingleDiffNode> secondaryAddrToSingleDiffNodeMap,
      final Map<Pair<IAddress, IAddress>, SuperDiffNode> addrPairToSuperDiffNodeMap,
      final Map<Pair<IAddress, IAddress>, CombinedDiffNode> addrPairToCombinedDiffNodeMap,
      final GraphSettings settings,
      final RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
          flowgraph) {
    for (final RawCombinedJump<RawCombinedBasicBlock> combinedJump : flowgraph.getEdges()) {
      final RawCombinedBasicBlock combinedSourceNode = combinedJump.getSource();
      final RawCombinedBasicBlock combinedTargetNode = combinedJump.getTarget();

      final RawBasicBlock primaryFunctionSourceNode = combinedSourceNode.getRawNode(ESide.PRIMARY);
      final RawBasicBlock primaryFunctionTargetNode = combinedTargetNode.getRawNode(ESide.PRIMARY);

      // primary jumps
      SingleDiffEdge primaryDiffEdge = null;
      Edge yPrimaryEdge = null;
      final IAddress priSrcAddr = combinedJump.getSource().getAddress(ESide.PRIMARY);
      final IAddress priTarAddr = combinedJump.getTarget().getAddress(ESide.PRIMARY);

      final RawJump primaryJump = combinedJump.getPrimaryEdge();

      SingleEdgeRealizer primaryEdgeRealizer = null;

      if (primaryFunctionSourceNode != null
          && primaryFunctionTargetNode != null
          && primaryJump != null) {
        final ZyLabelContent primaryEdgeContent = new ZyLabelContent(null);
        primaryEdgeRealizer = new SingleEdgeRealizer(primaryEdgeContent, null, settings);

        final SingleDiffNode sourceNode = primaryAddrToSingleDiffNodeMap.get(priSrcAddr);
        final SingleDiffNode targetNode = primaryAddrToSingleDiffNodeMap.get(priTarAddr);

        yPrimaryEdge = primaryGraph2D.createEdge(sourceNode.getNode(), targetNode.getNode());
        primaryDiffEdge =
            new SingleDiffEdge(
                sourceNode,
                targetNode,
                yPrimaryEdge,
                primaryEdgeRealizer,
                primaryJump,
                ESide.PRIMARY);
        SingleDiffNode.link(sourceNode, targetNode);

        primaryEdgeRealizer.setUserData(new ZyEdgeData<>(primaryDiffEdge));

        primaryEdgeMap.put(yPrimaryEdge, primaryDiffEdge);
      }

      // secondary jumps
      SingleDiffEdge secondaryDiffEdge = null;
      Edge ySecondaryEdge = null;
      final RawBasicBlock secondaryFunctionSourceNode =
          combinedSourceNode.getRawNode(ESide.SECONDARY);
      final RawBasicBlock secondaryFunctionTargetNode =
          combinedTargetNode.getRawNode(ESide.SECONDARY);

      final RawJump secondaryJump = combinedJump.getSecondaryEdge();

      final IAddress secSrcAddr = combinedJump.getSource().getAddress(ESide.SECONDARY);
      final IAddress secTarAddr = combinedJump.getTarget().getAddress(ESide.SECONDARY);

      SingleEdgeRealizer secondaryEdgeRealizer = null;

      if (secondaryFunctionSourceNode != null
          && secondaryFunctionTargetNode != null
          && secondaryJump != null) {

        final ZyLabelContent secondaryEdgeContent = new ZyLabelContent(null);

        secondaryEdgeRealizer = new SingleEdgeRealizer(secondaryEdgeContent, null, settings);

        final SingleDiffNode sourceNode = secondaryAddrToSingleDiffNodeMap.get(secSrcAddr);
        final SingleDiffNode targetNode = secondaryAddrToSingleDiffNodeMap.get(secTarAddr);

        ySecondaryEdge = secondaryGraph2D.createEdge(sourceNode.getNode(), targetNode.getNode());
        secondaryDiffEdge =
            new SingleDiffEdge(
                sourceNode,
                targetNode,
                ySecondaryEdge,
                secondaryEdgeRealizer,
                secondaryJump,
                ESide.SECONDARY);
        SingleDiffNode.link(sourceNode, targetNode);

        secondaryEdgeRealizer.setUserData(new ZyEdgeData<>(secondaryDiffEdge));

        secondaryEdgeMap.put(ySecondaryEdge, secondaryDiffEdge);
      }

      // super jumps
      SuperDiffEdge superDiffEdge = null;
      Edge ySuperEdge = null;
      final ZyLabelContent superEdgeContent = new ZyLabelContent(null);
      final ZyEdgeRealizer<SuperDiffEdge> superEdgeRealizer;

      superEdgeRealizer = new ZyEdgeRealizer<>(superEdgeContent, null);

      final SuperDiffNode srcSuperDiffNode =
          addrPairToSuperDiffNodeMap.get(new Pair<>(priSrcAddr, secSrcAddr));
      final SuperDiffNode tarSuperDiffNode =
          addrPairToSuperDiffNodeMap.get(new Pair<>(priTarAddr, secTarAddr));

      final Node srcSuperYNode = srcSuperDiffNode.getNode();
      final Node tarSuperYNode = tarSuperDiffNode.getNode();

      ySuperEdge = superGraph2D.createEdge(srcSuperYNode, tarSuperYNode);

      final SuperViewNode superRawSource = srcSuperDiffNode.getRawNode();
      final SuperViewNode superRawTarget = tarSuperDiffNode.getRawNode();

      final SuperViewEdge<SuperViewNode> superEdge =
          new SuperViewEdge<>(combinedJump, superRawSource, superRawTarget);

      superDiffEdge =
          new SuperDiffEdge(
              srcSuperDiffNode,
              tarSuperDiffNode,
              ySuperEdge,
              superEdgeRealizer,
              superEdge,
              primaryDiffEdge,
              secondaryDiffEdge);
      SuperDiffNode.link(srcSuperDiffNode, tarSuperDiffNode);

      superEdgeRealizer.setUserData(new ZyEdgeData<>(superDiffEdge));

      superEdgeMap.put(ySuperEdge, superDiffEdge);

      // combined jumps
      CombinedDiffEdge combinedDiffEdge = null;
      Edge yCombinedEdge = null;
      final ZyLabelContent combinedEdgeContent = new ZyLabelContent(null);
      final CombinedEdgeRealizer combinedEdgeRealizer;

      combinedEdgeRealizer = new CombinedEdgeRealizer(combinedEdgeContent, null, settings);

      final CombinedDiffNode srcCombinedDiffNode =
          addrPairToCombinedDiffNodeMap.get(new Pair<>(priSrcAddr, secSrcAddr));
      final CombinedDiffNode tarCombinedDiffNode =
          addrPairToCombinedDiffNodeMap.get(new Pair<>(priTarAddr, secTarAddr));

      final Node srcCombinedYNode = srcCombinedDiffNode.getNode();
      final Node tarCombinedYNode = tarCombinedDiffNode.getNode();

      yCombinedEdge = combinedGraph2D.createEdge(srcCombinedYNode, tarCombinedYNode);

      combinedDiffEdge =
          new CombinedDiffEdge(
              srcCombinedDiffNode,
              tarCombinedDiffNode,
              yCombinedEdge,
              combinedEdgeRealizer,
              combinedJump,
              superDiffEdge);
      CombinedDiffNode.link(srcCombinedDiffNode, tarCombinedDiffNode);

      combinedEdgeMap.put(yCombinedEdge, combinedDiffEdge);
      combinedEdgeRealizer.setUserData(new ZyEdgeData<>(combinedDiffEdge));

      // each edge gets it's representatives of the parallel views
      superDiffEdge.setCombinedDiffEdge(combinedDiffEdge);
      if (primaryDiffEdge != null) {
        primaryDiffEdge.setCombinedDiffEdge(combinedDiffEdge);
      }
      if (secondaryDiffEdge != null) {
        secondaryDiffEdge.setCombinedDiffEdge(combinedDiffEdge);
      }

      // colorize and stylize jumps
      colorizeJumps(combinedJump, primaryEdgeRealizer, secondaryEdgeRealizer);
      stylizeJumps(combinedJump, combinedEdgeRealizer, primaryEdgeRealizer, secondaryEdgeRealizer);
    }
  }

  protected static void buildFlowgraphNodeMaps(
      final FunctionMatchData functionMatch,
      final Graph2D primaryGraph2D,
      final Graph2D secondaryGraph2D,
      final Graph2D superGraph2D,
      final Graph2D combinedGraph2D,
      final Map<Node, SingleDiffNode> primaryNodeMap,
      final Map<Node, SingleDiffNode> secondaryNodeMap,
      final Map<Node, SuperDiffNode> superNodeMap,
      final Map<Node, CombinedDiffNode> combinedNodeMap,
      final Map<IAddress, SingleDiffNode> primaryAddrToSingleDiffNodeMap,
      final Map<IAddress, SingleDiffNode> secondaryAddrToSingleDiffNodeMap,
      final Map<Pair<IAddress, IAddress>, SuperDiffNode> addrPairToSuperDiffNodeMap,
      final Map<Pair<IAddress, IAddress>, CombinedDiffNode> addrPairToCombinedDiffNodeMap,
      final RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
          flowgraph) {
    for (final RawCombinedBasicBlock combinedBasicblock : flowgraph) {
      SingleDiffNode primaryDiffNode = null;
      SingleDiffNode secondaryDiffNode = null;
      SuperDiffNode superDiffNode = null;
      CombinedDiffNode combinedDiffNode = null;

      Node yPrimaryNode = null;
      Node ySecondaryNode = null;
      Node ySuperNode = null;
      Node yCombinedNode = null;

      final IAddress secondaryAddress = combinedBasicblock.getAddress(ESide.SECONDARY);
      final IAddress primaryAddress = combinedBasicblock.getAddress(ESide.PRIMARY);
      final RawBasicBlock primaryBasicblock = combinedBasicblock.getRawNode(ESide.PRIMARY);
      final RawBasicBlock secondaryBasicblock = combinedBasicblock.getRawNode(ESide.SECONDARY);

      // primary basicblocks
      ZyLabelContent primaryNodeContent = null;
      if (primaryBasicblock != null) {
        primaryNodeContent =
            buildSingleBasicblockLabelContent(
                functionMatch, flowgraph, combinedBasicblock, ESide.PRIMARY);
        final ZyNormalNodeRealizer<SingleDiffNode> primaryNodeRealizer =
            new ZyNormalNodeRealizer<>(primaryNodeContent);

        // set updater
        final CodeNodeRealizerUpdater updater = new CodeNodeRealizerUpdater();
        primaryNodeRealizer.setUpdater(updater);
        updater.setRealizer(primaryNodeRealizer);

        yPrimaryNode = primaryGraph2D.createNode();
        primaryDiffNode =
            new SingleDiffBasicBlockNode(
                yPrimaryNode, primaryNodeRealizer, primaryBasicblock, ESide.PRIMARY);

        primaryNodeRealizer.setUserData(new ZyNodeData<>(primaryDiffNode));

        primaryAddrToSingleDiffNodeMap.put(primaryAddress, primaryDiffNode);
        primaryNodeMap.put(yPrimaryNode, primaryDiffNode);
      }

      // secondary basicblocks
      ZyLabelContent secondaryNodeContent = null;
      if (secondaryBasicblock != null) {
        secondaryNodeContent =
            buildSingleBasicblockLabelContent(
                functionMatch, flowgraph, combinedBasicblock, ESide.SECONDARY);
        final ZyNormalNodeRealizer<SingleDiffNode> secondaryNodeRealizer =
            new ZyNormalNodeRealizer<>(secondaryNodeContent);

        // set updater
        final CodeNodeRealizerUpdater updater = new CodeNodeRealizerUpdater();
        secondaryNodeRealizer.setUpdater(updater);
        updater.setRealizer(secondaryNodeRealizer);

        ySecondaryNode = secondaryGraph2D.createNode();
        secondaryDiffNode =
            new SingleDiffBasicBlockNode(
                ySecondaryNode, secondaryNodeRealizer, secondaryBasicblock, ESide.SECONDARY);

        secondaryNodeRealizer.setUserData(new ZyNodeData<>(secondaryDiffNode));

        secondaryAddrToSingleDiffNodeMap.put(secondaryAddress, secondaryDiffNode);
        secondaryNodeMap.put(ySecondaryNode, secondaryDiffNode);
      }

      // super basicblocks
      final ZyLabelContent superNodeContent = new ZyLabelContent(null);
      final ZyNormalNodeRealizer<SuperDiffNode> superNodeRealizer =
          new ZyNormalNodeRealizer<>(superNodeContent);

      ySuperNode = superGraph2D.createNode();

      final SuperViewNode superNode = new SuperViewNode(combinedBasicblock);

      superDiffNode =
          new SuperDiffNode(
              ySuperNode, superNodeRealizer, superNode, primaryDiffNode, secondaryDiffNode);
      superNodeRealizer.setUserData(new ZyNodeData<>(superDiffNode));
      addrPairToSuperDiffNodeMap.put(new Pair<>(primaryAddress, secondaryAddress), superDiffNode);
      superNodeMap.put(ySuperNode, superDiffNode);

      // combined basicblocks
      final CombinedNodeRealizer combinedNodeRealizer =
          new CombinedNodeRealizer(primaryNodeContent, secondaryNodeContent);

      final CodeNodeRealizerUpdater updater = new CodeNodeRealizerUpdater();
      combinedNodeRealizer.setUpdater(updater);
      updater.setRealizer(combinedNodeRealizer);

      yCombinedNode = combinedGraph2D.createNode();
      combinedDiffNode =
          new CombinedDiffNode(
              yCombinedNode, combinedNodeRealizer, combinedBasicblock, superDiffNode);
      combinedNodeRealizer.setUserData(new ZyNodeData<>(combinedDiffNode));

      addrPairToCombinedDiffNodeMap.put(
          new Pair<>(primaryAddress, secondaryAddress), combinedDiffNode);
      combinedNodeMap.put(yCombinedNode, combinedDiffNode);

      // each node gets it's representatives of the parallel views
      superDiffNode.setCombinedDiffNode(combinedDiffNode);
      if (primaryDiffNode != null) {
        primaryDiffNode.setCombinedDiffNode(combinedDiffNode);
      }
      if (secondaryDiffNode != null) {
        secondaryDiffNode.setCombinedDiffNode(combinedDiffNode);
      }

      // colorize Basicblocks
      colorizeBasicblocks(functionMatch, combinedBasicblock);
    }
  }

  public static ZyLabelContent buildSingleBasicblockLabelContent(
      final FunctionMatchData functionMatch,
      final RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
          combinedFlowgraph,
      final RawCombinedBasicBlock combinedBasicblock,
      final ESide side) {
    final BasicBlockLineObject basicblockObject =
        new BasicBlockLineObject(combinedBasicblock.getRawNode(side));

    // TODO(cblichmann): Reenable editability sometime after 4.0 release
    final ZyLabelContent basicblockContent = new DiffLabelContent(basicblockObject, true, false);

    ViewCodeNodeBuilder.buildSingleCodeNodeContent(
        functionMatch, combinedFlowgraph, combinedBasicblock, basicblockContent, side);

    return basicblockContent;
  }

  public static GraphsContainer buildViewFlowgraphs(
      final Diff diff,
      final FunctionMatchData functionMatch,
      final RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
          combinedRawFlowgraph) {
    final GraphSettings settings =
        new GraphSettings(BinDiffConfig.getInstance().getInitialFlowgraphSettings());

    final ZyGraph2DView primaryGraphView = new ZyGraph2DView();
    final ZyGraph2DView secondaryGraphView = new ZyGraph2DView();
    final ZyGraph2DView combinedGraphView = new ZyGraph2DView();
    final ZyGraph2DView superGraphView = new ZyGraph2DView();

    final Graph2D primaryGraph2D = new Graph2D();
    final Graph2D secondaryGraph2D = new Graph2D();
    final Graph2D combinedGraph2D = new Graph2D();
    final Graph2D superGraph2D = new Graph2D();

    primaryGraphView.setGraph2D(primaryGraph2D);
    secondaryGraphView.setGraph2D(secondaryGraph2D);
    superGraphView.setGraph2D(superGraph2D);
    combinedGraphView.setGraph2D(combinedGraph2D);

    final LinkedHashMap<Node, SingleDiffNode> primaryNodeMap = new LinkedHashMap<>();
    final LinkedHashMap<Node, SingleDiffNode> secondaryNodeMap = new LinkedHashMap<>();
    final LinkedHashMap<Node, SuperDiffNode> superNodeMap = new LinkedHashMap<>();
    final LinkedHashMap<Node, CombinedDiffNode> combinedNodeMap = new LinkedHashMap<>();

    final LinkedHashMap<Edge, SingleDiffEdge> primaryEdgeMap = new LinkedHashMap<>();
    final LinkedHashMap<Edge, SingleDiffEdge> secondaryEdgeMap = new LinkedHashMap<>();
    final LinkedHashMap<Edge, SuperDiffEdge> superEdgeMap = new LinkedHashMap<>();
    final LinkedHashMap<Edge, CombinedDiffEdge> combinedEdgeMap = new LinkedHashMap<>();

    final Map<IAddress, SingleDiffNode> primaryAddrToSingleDiffNodeMap = new HashMap<>();
    final Map<IAddress, SingleDiffNode> secondaryAddrToSingleDiffNodeMap = new HashMap<>();
    final Map<Pair<IAddress, IAddress>, CombinedDiffNode> addrPairToCombinedDiffNodeMap =
        new HashMap<>();
    final Map<Pair<IAddress, IAddress>, SuperDiffNode> addrPairToSuperDiffNodeMap = new HashMap<>();

    buildFlowgraphNodeMaps(
        functionMatch,
        primaryGraph2D,
        secondaryGraph2D,
        superGraph2D,
        combinedGraph2D,
        primaryNodeMap,
        secondaryNodeMap,
        superNodeMap,
        combinedNodeMap,
        primaryAddrToSingleDiffNodeMap,
        secondaryAddrToSingleDiffNodeMap,
        addrPairToSuperDiffNodeMap,
        addrPairToCombinedDiffNodeMap,
        combinedRawFlowgraph);

    buildFlowgraphEdgeMaps(
        primaryGraph2D,
        secondaryGraph2D,
        superGraph2D,
        combinedGraph2D,
        primaryEdgeMap,
        secondaryEdgeMap,
        superEdgeMap,
        combinedEdgeMap,
        primaryAddrToSingleDiffNodeMap,
        secondaryAddrToSingleDiffNodeMap,
        addrPairToSuperDiffNodeMap,
        addrPairToCombinedDiffNodeMap,
        settings,
        combinedRawFlowgraph);

    final IAddress primaryAddr = combinedRawFlowgraph.getPrimaryAddress();
    final IAddress secondaryAddr = combinedRawFlowgraph.getSecondaryAddress();

    colorizeSingleNodeLineBorders(primaryNodeMap.values(), primaryAddr);
    colorizeSingleNodeLineBorders(secondaryNodeMap.values(), secondaryAddr);
    colorizeCombinedNodeLineBorders(combinedNodeMap.values(), primaryAddr, secondaryAddr);

    final CombinedGraph combinedGraph =
        new CombinedGraph(
            combinedGraphView, combinedNodeMap, combinedEdgeMap, settings, EGraphType.FLOWGRAPH);
    final SingleGraph primaryGraph =
        new SingleGraph(
            primaryGraphView,
            primaryAddr,
            primaryNodeMap,
            primaryEdgeMap,
            settings,
            ESide.PRIMARY,
            EGraphType.FLOWGRAPH);
    final SingleGraph secondaryGraph =
        new SingleGraph(
            secondaryGraphView,
            secondaryAddr,
            secondaryNodeMap,
            secondaryEdgeMap,
            settings,
            ESide.SECONDARY,
            EGraphType.FLOWGRAPH);
    final SuperGraph superGraph =
        new SuperGraph(
            superGraphView,
            superNodeMap,
            superEdgeMap,
            primaryGraph,
            secondaryGraph,
            settings,
            EGraphType.FLOWGRAPH);

    final GraphsContainer graphs =
        new GraphsContainer(diff, superGraph, combinedGraph, primaryGraph, secondaryGraph);

    superGraph.refreshAllSuperNodesSizes(primaryGraph, secondaryGraph);

    addGraphEditors(functionMatch, graphs);

    return graphs;
  }

  public static void colorizeBasicblocks(
      final FunctionMatchData functionMatch, final RawCombinedBasicBlock combinedBasicblock) {
    final EMatchState matchState = combinedBasicblock.getMatchState();
    if (matchState == EMatchState.PRIMARY_UNMATCHED) {
      combinedBasicblock.setColor(Colors.PRIMARY_BASE);
      combinedBasicblock.getRawNode(ESide.PRIMARY).setColor(Colors.PRIMARY_BASE);
    } else if (matchState == EMatchState.SECONDRAY_UNMATCHED) {
      combinedBasicblock.setColor(Colors.SECONDARY_BASE);
      combinedBasicblock.getRawNode(ESide.SECONDARY).setColor(Colors.SECONDARY_BASE);
    } else {
      final IAddress priBasicblockAddress = combinedBasicblock.getAddress(ESide.PRIMARY);

      final BasicBlockMatchData basicblockMatch =
          functionMatch.getBasicblockMatch(priBasicblockAddress, ESide.PRIMARY);

      final int matchedInstructionCount = basicblockMatch.getSizeOfMatchedInstructions();

      Color color = Colors.MATCHED_BASE;

      if (combinedBasicblock.getRawNode(ESide.PRIMARY).getInstructions().size()
              > matchedInstructionCount
          || combinedBasicblock.getRawNode(ESide.SECONDARY).getInstructions().size()
              > matchedInstructionCount) {
        color = Colors.CHANGED_BASE;
      }

      combinedBasicblock.setColor(color);
      combinedBasicblock.setColor(color);
      combinedBasicblock.getRawNode(ESide.PRIMARY).setColor(color);
      combinedBasicblock.getRawNode(ESide.SECONDARY).setColor(color);
    }
  }

  public static void colorizeCombinedNodeLineBorders(
      final Collection<CombinedDiffNode> nodes,
      final IAddress priFunctionAddr,
      final IAddress secFunctionAddr) {
    for (final CombinedDiffNode node : nodes) {
      if (nodes.size() == 1) {
        node.getRealizer().setLineColor(Colors.MIXED_BASE_COLOR.darker());

        break;
      }

      final SingleViewNode priRawNode = node.getPrimaryRawNode();
      final SingleViewNode secRawNode = node.getSecondaryRawNode();

      if (node.getRawNode().getMatchState() == EMatchState.MATCHED) {
        if (priRawNode.getAddress().equals(priFunctionAddr)
            && secRawNode.getAddress().equals(secFunctionAddr)) {
          node.getRealizer().setLineColor(Colors.JUMP_CONDITIONAL_TRUE.darker());
        } else if (priRawNode.getAddress().equals(priFunctionAddr)
            || secRawNode.getAddress().equals(secFunctionAddr)) {
          node.getRealizer().setLineColor(Colors.MIXED_BASE_COLOR.darker());
        }

        if (priRawNode.getChildren().size() == 0 && secRawNode.getChildren().size() == 0) {
          node.getRealizer().setLineColor(Colors.JUMP_CONDITIONAL_FALSE.darker());
        } else if (priRawNode.getChildren().size() == 0 || secRawNode.getChildren().size() == 0) {
          node.getRealizer().setLineColor(Colors.MIXED_BASE_COLOR.darker());
        }
      } else {
        if (priRawNode != null) {
          if (priRawNode.getAddress().equals(priFunctionAddr)) {
            node.getRealizer().setLineColor(Colors.JUMP_CONDITIONAL_TRUE.darker());
          }

          if (priRawNode.getChildren().size() == 0) {
            node.getRealizer().setLineColor(Colors.JUMP_CONDITIONAL_FALSE.darker());
          }
        } else if (secRawNode != null) {
          if (secRawNode.getAddress().equals(secFunctionAddr)) {
            node.getRealizer().setLineColor(Colors.JUMP_CONDITIONAL_TRUE.darker());
          }

          if (secRawNode.getChildren().size() == 0) {
            node.getRealizer().setLineColor(Colors.JUMP_CONDITIONAL_FALSE.darker());
          }
        }
      }
    }
  }

  public static void colorizeJumps(
      final RawCombinedJump<RawCombinedBasicBlock> combinedJump,
      final ZyEdgeRealizer<SingleDiffEdge> primaryEdgeRealizer,
      final ZyEdgeRealizer<SingleDiffEdge> secondaryEdgeRealizer) {
    final RawJump primaryJump = combinedJump.getPrimaryEdge();
    if (primaryJump != null) {
      switch (primaryJump.getJumpType()) {
        case JUMP_TRUE:
          {
            primaryEdgeRealizer.setLineColor(Colors.JUMP_CONDITIONAL_TRUE);
            combinedJump.setColor(Colors.JUMP_CONDITIONAL_TRUE);
            break;
          }
        case JUMP_FALSE:
          {
            primaryEdgeRealizer.setLineColor(Colors.JUMP_CONDITIONAL_FALSE);
            combinedJump.setColor(Colors.JUMP_CONDITIONAL_FALSE);
            break;
          }
        case SWITCH:
          {
            primaryEdgeRealizer.setLineColor(Colors.JUMP_SWITCH);
            combinedJump.setColor(Colors.JUMP_SWITCH);
            break;
          }
        default:
          {
            primaryEdgeRealizer.setLineColor(Colors.JUMP_UNCONDITIONAL);
            combinedJump.setColor(Colors.JUMP_UNCONDITIONAL);
          }
      }
    }

    final RawJump secondaryJump = combinedJump.getSecondaryEdge();
    if (secondaryJump != null) {
      switch (secondaryJump.getJumpType()) {
        case JUMP_TRUE:
          {
            secondaryEdgeRealizer.setLineColor(Colors.JUMP_CONDITIONAL_TRUE);
            combinedJump.setColor(Colors.JUMP_CONDITIONAL_TRUE);
            break;
          }
        case JUMP_FALSE:
          {
            secondaryEdgeRealizer.setLineColor(Colors.JUMP_CONDITIONAL_FALSE);
            combinedJump.setColor(Colors.JUMP_CONDITIONAL_FALSE);
            break;
          }
        case SWITCH:
          {
            secondaryEdgeRealizer.setLineColor(Colors.JUMP_SWITCH);
            combinedJump.setColor(Colors.JUMP_SWITCH);
            break;
          }
        default:
          {
            secondaryEdgeRealizer.setLineColor(Colors.JUMP_UNCONDITIONAL);
            combinedJump.setColor(Colors.JUMP_UNCONDITIONAL);
          }
      }
    }

    if (combinedJump.getMatchState() == EMatchState.MATCHED) {
      if (combinedJump.getPrimaryEdge().getJumpType()
          != combinedJump.getSecondaryEdge().getJumpType()) {
        primaryEdgeRealizer.setLineColor(Colors.MIXED_BASE_COLOR);
        secondaryEdgeRealizer.setLineColor(Colors.MIXED_BASE_COLOR);
        combinedJump.setColor(Colors.MIXED_BASE_COLOR);
      }
    }
  }

  public static void colorizeSingleNodeLineBorders(
      final Collection<SingleDiffNode> collection, final IAddress functionAddr) {
    for (final SingleDiffNode node : collection) {
      if (collection.size() == 1) {
        node.getRealizer().setLineColor(Colors.MIXED_BASE_COLOR.darker());

        break;
      }

      if (node.getRawNode().getAddress().equals(functionAddr)) {
        node.getRealizer().setLineColor(Colors.JUMP_CONDITIONAL_TRUE.darker());
      } else if (node.getRawNode().getChildren().size() == 0) {
        node.getRealizer().setLineColor(Colors.JUMP_CONDITIONAL_FALSE.darker());
      }
    }
  }

  public static void stylizeJumps(
      final RawCombinedJump<RawCombinedBasicBlock> combinedJump,
      final ZyEdgeRealizer<CombinedDiffEdge> combinedEdgeRealizer,
      final ZyEdgeRealizer<SingleDiffEdge> primaryEdgeRealizer,
      final ZyEdgeRealizer<SingleDiffEdge> secondaryEdgeRealizer) {
    combinedEdgeRealizer.setReversedPathRenderingEnabled(true);

    if (combinedJump.getMatchState() == EMatchState.PRIMARY_UNMATCHED) {
      combinedEdgeRealizer.setLineType(LineType.DASHED_2);
    } else if (combinedJump.getMatchState() == EMatchState.SECONDRAY_UNMATCHED) {
      combinedEdgeRealizer.setLineType(LineType.DOTTED_2);
    } else {
      combinedEdgeRealizer.setLineType(LineType.LINE_2);
    }

    if (primaryEdgeRealizer != null && secondaryEdgeRealizer == null) {
      primaryEdgeRealizer.setLineType(LineType.DASHED_2);
    } else if (secondaryEdgeRealizer != null && primaryEdgeRealizer == null) {
      secondaryEdgeRealizer.setLineType(LineType.DASHED_2);
    } else {
      primaryEdgeRealizer.setLineType(LineType.LINE_2);
      secondaryEdgeRealizer.setLineType(LineType.LINE_2);
    }
  }
}
