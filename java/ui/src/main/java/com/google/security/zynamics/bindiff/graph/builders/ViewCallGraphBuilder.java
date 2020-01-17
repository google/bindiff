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

package com.google.security.zynamics.bindiff.graph.builders;

import com.google.security.zynamics.bindiff.config.BinDiffConfig;
import com.google.security.zynamics.bindiff.config.ThemeConfigItem;
import com.google.security.zynamics.bindiff.enums.EFunctionType;
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
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleViewNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperViewNode;
import com.google.security.zynamics.bindiff.graph.realizers.CombinedEdgeRealizer;
import com.google.security.zynamics.bindiff.graph.realizers.CombinedNodeRealizer;
import com.google.security.zynamics.bindiff.graph.realizers.FunctionNodeRealizerUpdater;
import com.google.security.zynamics.bindiff.graph.realizers.SingleEdgeRealizer;
import com.google.security.zynamics.bindiff.graph.settings.GraphSettings;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.helpers.MatchesGetter;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCall;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCombinedCall;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCombinedCallGraph;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCombinedFunction;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawFunction;
import com.google.security.zynamics.bindiff.resources.Colors;
import com.google.security.zynamics.bindiff.resources.Fonts;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.zygraph.edges.ZyEdgeData;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.ZyNodeData;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.CStyleRunData;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.ZyGraph2DView;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyNormalNodeRealizer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import y.base.Edge;
import y.base.Node;
import y.view.Graph2D;
import y.view.LineType;

public class ViewCallGraphBuilder {
  private static void buildCallgraphEdgeMaps(
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
      final RawCombinedCallGraph combinedRawCallgraph) {
    for (final RawCombinedCall combinedCall : combinedRawCallgraph.getEdges()) {
      final RawCombinedFunction combinedSourceNode = combinedCall.getSource();
      final RawCombinedFunction combinedTargetNode = combinedCall.getTarget();

      // primary calls
      SingleDiffEdge primaryDiffEdge = null;
      Edge yPrimaryEdge = null;
      final RawFunction primaryFunctionSourceNode = combinedSourceNode.getRawNode(ESide.PRIMARY);
      final RawFunction primaryFunctionTargetNode = combinedTargetNode.getRawNode(ESide.PRIMARY);
      final IAddress priSrcAddr = combinedCall.getSource().getAddress(ESide.PRIMARY);
      final IAddress priTarAddr = combinedCall.getTarget().getAddress(ESide.PRIMARY);
      final RawCall primaryCall = combinedCall.getPrimaryEdge();

      SingleEdgeRealizer primaryEdgeRealizer = null;

      if (primaryFunctionSourceNode != null
          && primaryFunctionTargetNode != null
          && primaryCall != null) {
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
                primaryCall,
                ESide.PRIMARY);
        SingleDiffNode.link(sourceNode, targetNode);

        primaryEdgeRealizer.setUserData(new ZyEdgeData<>(primaryDiffEdge));
        primaryEdgeRealizer.setLineType(LineType.LINE_2);

        primaryEdgeMap.put(yPrimaryEdge, primaryDiffEdge);
      }

      // secondary calls
      SingleDiffEdge secondaryDiffEdge = null;
      Edge ySecondaryEdge = null;
      final RawFunction secondaryFunctionSourceNode =
          combinedSourceNode.getRawNode(ESide.SECONDARY);
      final RawFunction secondaryFunctionTargetNode =
          combinedTargetNode.getRawNode(ESide.SECONDARY);
      final RawCall secondaryCall = combinedCall.getSecondaryEdge();
      final IAddress secSrcAddr = combinedCall.getSource().getAddress(ESide.SECONDARY);
      final IAddress secTarAddr = combinedCall.getTarget().getAddress(ESide.SECONDARY);

      SingleEdgeRealizer secondaryEdgeRealizer = null;

      if (secondaryFunctionSourceNode != null
          && secondaryFunctionTargetNode != null
          && secondaryCall != null) {
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
                secondaryCall,
                ESide.SECONDARY);
        SingleDiffNode.link(sourceNode, targetNode);

        secondaryEdgeRealizer.setUserData(new ZyEdgeData<>(secondaryDiffEdge));
        secondaryEdgeRealizer.setLineType(LineType.LINE_2);

        secondaryEdgeMap.put(ySecondaryEdge, secondaryDiffEdge);
      }

      // super calls
      final ZyLabelContent superEdgeContent = new ZyLabelContent(null);
      final ZyEdgeRealizer<SuperDiffEdge> superEdgeRealizer =
          new ZyEdgeRealizer<>(superEdgeContent, null);

      final SuperDiffNode srcSuperDiffNode =
          addrPairToSuperDiffNodeMap.get(new Pair<>(priSrcAddr, secSrcAddr));
      final SuperDiffNode tarSuperDiffNode =
          addrPairToSuperDiffNodeMap.get(new Pair<>(priTarAddr, secTarAddr));

      final Node srcSuperYNode = srcSuperDiffNode.getNode();
      final Node tarSuperYNode = tarSuperDiffNode.getNode();

      final Edge ySuperEdge = superGraph2D.createEdge(srcSuperYNode, tarSuperYNode);

      final SuperViewNode superRawSource = srcSuperDiffNode.getRawNode();
      final SuperViewNode superRawTarget = tarSuperDiffNode.getRawNode();

      final SuperViewEdge<SuperViewNode> superEdge =
          new SuperViewEdge<>(combinedCall, superRawSource, superRawTarget);

      SuperDiffEdge superDiffEdge =
          new SuperDiffEdge(
              srcSuperDiffNode,
              tarSuperDiffNode,
              ySuperEdge,
              superEdgeRealizer,
              superEdge,
              primaryDiffEdge,
              secondaryDiffEdge);
      SuperDiffNode.link(srcSuperDiffNode, tarSuperDiffNode);
      superEdgeMap.put(ySuperEdge, superDiffEdge);

      // combined calls
      final ZyLabelContent combinedEdgeContent = new ZyLabelContent(null);
      final CombinedEdgeRealizer combinedEdgeRealizer;

      combinedEdgeRealizer = new CombinedEdgeRealizer(combinedEdgeContent, null, settings);
      combinedEdgeRealizer.setLineType(LineType.LINE_2);

      final CombinedDiffNode srcCombinedDiffNode =
          addrPairToCombinedDiffNodeMap.get(new Pair<>(priSrcAddr, secSrcAddr));
      final CombinedDiffNode tarCombinedDiffNode =
          addrPairToCombinedDiffNodeMap.get(new Pair<>(priTarAddr, secTarAddr));

      final Node srcCombinedYNode = srcCombinedDiffNode.getNode();
      final Node tarCombinedYNode = tarCombinedDiffNode.getNode();

      final Edge yCombinedEdge = combinedGraph2D.createEdge(srcCombinedYNode, tarCombinedYNode);

      final CombinedDiffEdge combinedDiffEdge =
          new CombinedDiffEdge(
              srcCombinedDiffNode,
              tarCombinedDiffNode,
              yCombinedEdge,
              combinedEdgeRealizer,
              combinedCall,
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

      // colorize Calls
      colorizeCalls(combinedCall);
    }
  }

  private static void buildCallgraphNodeMaps(
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
      final RawCombinedCallGraph combinedRawCallgraph) {
    // TODO: set node and edge updater to edge and node realizers!
    for (final RawCombinedFunction combinedFunction : combinedRawCallgraph.getNodes()) {
      SingleDiffNode primaryDiffNode = null;
      SingleDiffNode secondaryDiffNode = null;

      Node yPrimaryNode = null;
      Node ySecondaryNode = null;

      final RawFunction primaryFunction = combinedFunction.getRawNode(ESide.PRIMARY);
      final RawFunction secondaryFunction = combinedFunction.getRawNode(ESide.SECONDARY);
      final IAddress primaryAddress = combinedFunction.getAddress(ESide.PRIMARY);
      final IAddress secondaryAddress = combinedFunction.getAddress(ESide.SECONDARY);

      // Primary functions
      ZyLabelContent primaryNodeContent = null;
      if (primaryFunction != null) {
        primaryNodeContent = buildNormalCallgraphLabelContent(primaryFunction);
        final ZyNormalNodeRealizer<SingleDiffNode> primaryNodeRealizer =
            new ZyNormalNodeRealizer<>(primaryNodeContent);

        // Set updater
        final FunctionNodeRealizerUpdater updater = new FunctionNodeRealizerUpdater();
        primaryNodeRealizer.setUpdater(updater);
        updater.setRealizer(primaryNodeRealizer);

        yPrimaryNode = primaryGraph2D.createNode();
        primaryDiffNode =
            new SingleDiffNode(yPrimaryNode, primaryNodeRealizer, primaryFunction, ESide.PRIMARY);
        primaryAddrToSingleDiffNodeMap.put(primaryAddress, primaryDiffNode);

        primaryNodeRealizer.setUserData(new ZyNodeData<>(primaryDiffNode));

        primaryNodeMap.put(yPrimaryNode, primaryDiffNode);
      }

      // Secondary functions
      ZyLabelContent secondaryNodeContent = null;
      if (secondaryFunction != null) {
        secondaryNodeContent = buildNormalCallgraphLabelContent(secondaryFunction);
        final ZyNormalNodeRealizer<SingleDiffNode> secondaryNodeRealizer =
            new ZyNormalNodeRealizer<>(secondaryNodeContent);

        final FunctionNodeRealizerUpdater updater = new FunctionNodeRealizerUpdater();
        secondaryNodeRealizer.setUpdater(updater);
        updater.setRealizer(secondaryNodeRealizer);

        ySecondaryNode = secondaryGraph2D.createNode();
        secondaryDiffNode =
            new SingleDiffNode(
                ySecondaryNode, secondaryNodeRealizer, secondaryFunction, ESide.SECONDARY);

        secondaryNodeRealizer.setUserData(new ZyNodeData<>(secondaryDiffNode));

        secondaryAddrToSingleDiffNodeMap.put(secondaryAddress, secondaryDiffNode);
        secondaryNodeMap.put(ySecondaryNode, secondaryDiffNode);
      }

      // Super functions
      final ZyLabelContent superNodeContent = new ZyLabelContent(null);
      final ZyNormalNodeRealizer<SuperDiffNode> superNodeRealizer =
          new ZyNormalNodeRealizer<>(superNodeContent);

      final Node ySuperNode = superGraph2D.createNode();
      final SuperViewNode superNode = new SuperViewNode(combinedFunction);

      final SuperDiffNode superDiffNode =
          new SuperDiffNode(
              ySuperNode, superNodeRealizer, superNode, primaryDiffNode, secondaryDiffNode);
      addrPairToSuperDiffNodeMap.put(new Pair<>(primaryAddress, secondaryAddress), superDiffNode);

      superNodeRealizer.setUserData(new ZyNodeData<>(superDiffNode));
      superNodeMap.put(ySuperNode, superDiffNode);

      // combined functions
      final CombinedNodeRealizer combinedNodeRealizer =
          new CombinedNodeRealizer(primaryNodeContent, secondaryNodeContent);

      final Node yCombinedNode = combinedGraph2D.createNode();
      final CombinedDiffNode combinedDiffNode =
          new CombinedDiffNode(
              yCombinedNode, combinedNodeRealizer, combinedFunction, superDiffNode);
      combinedNodeRealizer.setUserData(new ZyNodeData<>(combinedDiffNode));

      addrPairToCombinedDiffNodeMap.put(
          new Pair<>(primaryAddress, secondaryAddress), combinedDiffNode);

      combinedNodeMap.put(yCombinedNode, combinedDiffNode);

      // Each node gets its representatives from the parallel views
      superDiffNode.setCombinedDiffNode(combinedDiffNode);
      if (primaryDiffNode != null) {
        primaryDiffNode.setCombinedDiffNode(combinedDiffNode);
      }
      if (secondaryDiffNode != null) {
        secondaryDiffNode.setCombinedDiffNode(combinedDiffNode);
      }

      // Colorize functions
      colorizeFunctions(combinedFunction);
    }
  }

  private static ZyLabelContent buildNormalCallgraphLabelContent(final SingleViewNode singleNode) {
    final RawFunction rawFunction = (RawFunction) singleNode;

    final ZyLabelContent labelContent = new ZyLabelContent(null);

    if (rawFunction != null) {
      final BinDiffConfig config = BinDiffConfig.getInstance();
      final ThemeConfigItem settings = config.getThemeSettings();
      final Color addressColor = settings.getAddressColor();
      final Color functionColor = settings.getMnemonicColor();

      final String addr = rawFunction.getAddress().toHexString();
      final String functionName = rawFunction.getName();

      final List<CStyleRunData> styleRun = new ArrayList<>();

      styleRun.add(new CStyleRunData(0, addr.length(), addressColor));
      styleRun.add(new CStyleRunData(addr.length() + 1, functionName.length(), functionColor));

      final String content = String.format("%s %s", addr, functionName);

      final ZyLineContent lineContent =
          new ZyLineContent(content, Fonts.NORMAL_FONT, styleRun, null);
      labelContent.addLineContent(lineContent);
    }

    return labelContent;
  }

  private static Color getFunctionBorderColor(
      final RawFunction priFunction, final RawFunction secFunction) {
    final Color[] colors = new Color[6];

    colors[0] = Color.BLACK;
    colors[1] = Colors.FUNCTION_TYPE_LIBRARY.darker();
    colors[2] = Colors.FUNCTION_TYPE_THUNK.darker();
    colors[3] = Colors.FUNCTION_TYPE_IMPORTED.darker();
    colors[4] = Colors.FUNCTION_TYPE_UNKNOWN;
    colors[5] = Colors.MIXED_BASE_COLOR;

    if (priFunction != null && secFunction != null) {
      final EFunctionType priType = priFunction.getFunctionType();
      if (priType != secFunction.getFunctionType()) {
        return colors[5];
      }
    }

    if (priFunction != null) {
      return colors[EFunctionType.getOrdinal(priFunction.getFunctionType())];
    }

    return colors[EFunctionType.getOrdinal(secFunction.getFunctionType())];
  }

  public static GraphsContainer buildDiffCallgraphs(
      final Diff diff, final RawCombinedCallGraph combinedRawCallgraph) {
    final GraphSettings settings =
        new GraphSettings(BinDiffConfig.getInstance().getInitialCallGraphSettings());

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

    buildCallgraphNodeMaps(
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
        combinedRawCallgraph);

    buildCallgraphEdgeMaps(
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
        combinedRawCallgraph);

    final CombinedGraph combinedGraph =
        new CombinedGraph(
            combinedGraphView, combinedNodeMap, combinedEdgeMap, settings, EGraphType.CALLGRAPH);
    final SingleGraph primaryGraph =
        new SingleGraph(
            primaryGraphView,
            null,
            primaryNodeMap,
            primaryEdgeMap,
            settings,
            ESide.PRIMARY,
            EGraphType.CALLGRAPH);
    final SingleGraph secondaryGraph =
        new SingleGraph(
            secondaryGraphView,
            null,
            secondaryNodeMap,
            secondaryEdgeMap,
            settings,
            ESide.SECONDARY,
            EGraphType.CALLGRAPH);
    final SuperGraph superGraph =
        new SuperGraph(
            superGraphView,
            superNodeMap,
            superEdgeMap,
            primaryGraph,
            secondaryGraph,
            settings,
            EGraphType.CALLGRAPH);

    final GraphsContainer graphs =
        new GraphsContainer(diff, superGraph, combinedGraph, primaryGraph, secondaryGraph);

    superGraph.refreshAllSuperNodesSizes(primaryGraph, secondaryGraph);

    return graphs;
  }

  public static void colorizeCalls(final RawCombinedCall combinedCall) {
    // Note: All setColor(Color.BLACK) call are essential. Otherwise all call edges are black
    // if a callgrpah is loaded a second time. Reason model wasn't deleted and as a consequence
    // it does not notify the GUI if the same color is set a second time.

    final EMatchState matchState = combinedCall.getMatchState();

    if (matchState == EMatchState.PRIMARY_UNMATCHED) {
      combinedCall.setColor(
          combinedCall.isChanged() ? Colors.MIXED_BASE_COLOR : Colors.CALL_PRIMARY_UNMATCHED);
      combinedCall.getPrimaryEdge().setColor(Color.BLACK);
      combinedCall
          .getPrimaryEdge()
          .setColor(
              combinedCall.isChanged() ? Colors.MIXED_BASE_COLOR : Colors.CALL_PRIMARY_UNMATCHED);
    } else if (matchState == EMatchState.SECONDRAY_UNMATCHED) {
      combinedCall.setColor(
          combinedCall.isChanged() ? Colors.MIXED_BASE_COLOR : Colors.CALL_SECONDRAY_UNMATCHED);
      combinedCall.getSecondaryEdge().setColor(Color.BLACK);
      combinedCall
          .getSecondaryEdge()
          .setColor(
              combinedCall.isChanged() ? Colors.MIXED_BASE_COLOR : Colors.CALL_SECONDRAY_UNMATCHED);
    } else {
      combinedCall.setColor(Colors.CALL_MATCHED);
      combinedCall.getPrimaryEdge().setColor(Color.BLACK);
      combinedCall.getSecondaryEdge().setColor(Color.BLACK);
      combinedCall.getPrimaryEdge().setColor(Colors.CALL_MATCHED);
      combinedCall.getSecondaryEdge().setColor(Colors.CALL_MATCHED);
    }
  }

  public static void colorizeFunctions(final RawCombinedFunction combinedFunction) {
    final RawFunction priFunction = combinedFunction.getRawNode(ESide.PRIMARY);
    final RawFunction secFunction = combinedFunction.getRawNode(ESide.SECONDARY);

    final Color borderColor = getFunctionBorderColor(priFunction, secFunction);
    combinedFunction.setBorderColor(borderColor);

    final EMatchState matchState = combinedFunction.getMatchState();
    if (matchState == EMatchState.PRIMARY_UNMATCHED) {
      combinedFunction.setColor(Colors.PRIMARY_BASE);

      priFunction.setColor(Colors.PRIMARY_BASE);
      priFunction.setBorderColor(borderColor);
    } else if (matchState == EMatchState.SECONDRAY_UNMATCHED) {
      combinedFunction.setColor(Colors.SECONDARY_BASE);

      secFunction.setColor(Colors.SECONDARY_BASE);
      secFunction.setBorderColor(borderColor);
    } else {
      Color color = Colors.MATCHED_BASE;

      if (combinedFunction.isChanged()) {
        if (MatchesGetter.isStructuralChangedFunctionPair(priFunction, secFunction)) {
          color = Colors.CHANGED_BASE;
        } else {
          color = Colors.INSTRUCTIONS_ONLY_CHANGED_COLOR;
        }
      }

      combinedFunction.setColor(color);
      priFunction.setColor(color);
      secFunction.setColor(color);

      priFunction.setBorderColor(borderColor);
      secFunction.setBorderColor(borderColor);
    }
  }
}
