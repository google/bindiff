package com.google.security.zynamics.bindiff.graph.helpers;

import com.google.security.zynamics.bindiff.database.MatchesDatabase;
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
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperViewNode;
import com.google.security.zynamics.bindiff.project.matches.BasicBlockMatchData;
import com.google.security.zynamics.bindiff.project.matches.FunctionMatchData;
import com.google.security.zynamics.bindiff.project.matches.InstructionMatchData;
import com.google.security.zynamics.bindiff.project.matches.MatchData;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedFlowGraph;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedJump;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawFlowGraph;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawJump;
import com.google.security.zynamics.bindiff.project.userview.FlowGraphViewData;
import com.google.security.zynamics.bindiff.project.userview.ViewManager;
import com.google.security.zynamics.bindiff.types.Matches;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class BasicBlockMatchAdder {
  private static BasicBlockMatchData createBasicblockMatchData(
      final RawCombinedBasicBlock oldRawPriUnmatchedCombinedBasicblock,
      final RawCombinedBasicBlock oldRawSecUnmatchedCombinedBasicblock) {
    final long priAddress = oldRawPriUnmatchedCombinedBasicblock.getAddress(ESide.PRIMARY).toLong();
    final long secAddress =
        oldRawSecUnmatchedCombinedBasicblock.getAddress(ESide.SECONDARY).toLong();
    final int algoId = MatchesDatabase.UNSAVED_BASICBLOCKMATCH_ALGORITH_ID;
    final Matches<InstructionMatchData> instructionsMap = new Matches<>(getNewInstructionMatches());

    return new BasicBlockMatchData(priAddress, secAddress, algoId, instructionsMap);
  }

  private static List<InstructionMatchData> getNewInstructionMatches() {
    // TODO: Match instructions: Has to implemented if a 2 way communication with the plugin is
    // established.
    // The plugin has to diff the basicblock instruction.
    return new ArrayList<>();
  }

  // TODO: On cancel, restore CFunctionDiffData (reload from database) in order to update the
  // workspace counts
  // TODO: Do not forget to update the an open callgraph view if a basicblock match has been added
  // (or removed)
  // - change node color if necessary
  // - change calls from unmatched to matched color
  // - change calls from unmatched to matched color (as soon as newly assigned basicblock comments
  // can be diffed)
  // - change proximity nodes of combined
  private static RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
      getRawCombinedFlowgraph(
          final GraphsContainer graphs,
          final CombinedDiffNode priUnmatchedDiffNode,
          final CombinedDiffNode secUnmatchedDiffNode) {
    final IAddress priAddress =
        ((RawCombinedBasicBlock) priUnmatchedDiffNode.getRawNode()).getPrimaryFunctionAddress();
    final IAddress secAddress =
        ((RawCombinedBasicBlock) secUnmatchedDiffNode.getRawNode()).getSecondaryFunctionAddress();

    final ViewManager viewManager = graphs.getDiff().getViewManager();
    final FlowGraphViewData viewData = viewManager.getFlowgraphViewData(priAddress, secAddress);

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
      final SingleDiffEdge primaryDiffEdge = SingleGraph.buildDiffEdge(priGraph, newRawSuperJump);
      final SingleDiffEdge secondaryDiffEdge = SingleGraph.buildDiffEdge(secGraph, newRawSuperJump);
      final SuperDiffEdge superDiffEdge =
          SuperGraph.buildDiffEdge(superGraph, newRawSuperJump, primaryDiffEdge, secondaryDiffEdge);
      final CombinedDiffEdge combinedDiffEdge =
          CombinedGraph.buildDiffEdge(combinedGraph, newRawSuperJump, superDiffEdge);

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

  private static void insertNewDiffNodesAndEdges(
      final GraphsContainer graphs,
      final RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
          combinedRawFlowgraph,
      final SuperViewNode newRawSuperBasicblock)
      throws GraphLayoutException {
    final RawCombinedBasicBlock newRawCombinedBasicblock =
        (RawCombinedBasicBlock) newRawSuperBasicblock.getCombinedNode();

    final CombinedGraph combinedGraph = graphs.getCombinedGraph();
    final SuperGraph superGraph = graphs.getSuperGraph();
    final SingleGraph primaryGraph = graphs.getPrimaryGraph();
    final SingleGraph secondaryGraph = graphs.getSecondaryGraph();

    // create new diff nodes
    final MatchData matches = graphs.getDiff().getMatches();
    final FunctionMatchData functionMatch =
        matches.getFunctionMatch(
            newRawCombinedBasicblock.getPrimaryFunctionAddress(), ESide.PRIMARY);

    final SingleDiffNode newPrimaryDiffNode =
        SingleGraph.buildDiffNode(
            primaryGraph, functionMatch, combinedRawFlowgraph, newRawCombinedBasicblock);
    final SingleDiffNode newSecondaryDiffNode =
        SingleGraph.buildDiffNode(
            secondaryGraph, functionMatch, combinedRawFlowgraph, newRawCombinedBasicblock);
    final SuperDiffNode newSuperDiffNode =
        SuperGraph.buildDiffNode(
            superGraph, newPrimaryDiffNode, newSecondaryDiffNode, newRawSuperBasicblock);
    final CombinedDiffNode newCombinedDiffNode =
        CombinedGraph.buildDiffNode(
            combinedGraph,
            newPrimaryDiffNode,
            newSecondaryDiffNode,
            newSuperDiffNode,
            newRawCombinedBasicblock);

    // each node gets it's representatives of the parallel views
    // update node mappings
    // set node content editors
    newSuperDiffNode.setCombinedDiffNode(newCombinedDiffNode);
    newPrimaryDiffNode.setCombinedDiffNode(newCombinedDiffNode);
    newSecondaryDiffNode.setCombinedDiffNode(newCombinedDiffNode);

    primaryGraph.addNodeToMappings(newPrimaryDiffNode);
    secondaryGraph.addNodeToMappings(newSecondaryDiffNode);
    combinedGraph.addNodeToMappings(newCombinedDiffNode);
    superGraph.addNodeToMappings(newSuperDiffNode);

    final BasicBlockContentEditor priNodeEditor =
        new BasicBlockContentEditor(functionMatch, graphs, ESide.PRIMARY);
    newPrimaryDiffNode.getRealizer().getNodeContent().setLineEditor(priNodeEditor);
    final BasicBlockContentEditor secNodeEditor =
        new BasicBlockContentEditor(functionMatch, graphs, ESide.SECONDARY);
    newSecondaryDiffNode.getRealizer().getNodeContent().setLineEditor(secNodeEditor);

    // colorize new basicblock node's background and border color
    ViewFlowGraphBuilder.colorizeBasicblocks(functionMatch, newRawCombinedBasicblock);
    ViewFlowGraphBuilder.colorizeCombinedNodeLineBorders(
        combinedGraph.getNodes(),
        combinedGraph.getPrimaryGraph().getFunctionAddress(),
        combinedGraph.getSecondaryGraph().getFunctionAddress());
    ViewFlowGraphBuilder.colorizeSingleNodeLineBorders(
        primaryGraph.getNodes(), primaryGraph.getFunctionAddress());
    ViewFlowGraphBuilder.colorizeSingleNodeLineBorders(
        secondaryGraph.getNodes(), secondaryGraph.getFunctionAddress());

    // refresh super node's width and height
    superGraph.refreshSuperNodeSize(
        graphs.getPrimaryGraph(), graphs.getSecondaryGraph(), newSuperDiffNode);

    // create new diff edges and add to graphs
    insertNewDiffEdges(graphs, newSuperDiffNode);
  }

  private static void insertNewRawNodesAndEdges(
      final RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
          combinedRawFlowgraph,
      final Set<SuperViewEdge<? extends SuperViewNode>> oldRawSuperJumps,
      final SuperViewNode oldRawPriUnmatchedSuperBasicblock,
      final SuperViewNode oldRawSecUnmatchedSuperBasicblock,
      final RawBasicBlock newRawPrimaryBasicblock,
      final RawBasicBlock newRawSecondaryBasicblock,
      final RawCombinedBasicBlock newRawCombinedBasicblock,
      final SuperViewNode newRawSuperBasicblock) {
    final RawFlowGraph priFlowgraph = combinedRawFlowgraph.getPrimaryFlowgraph();
    final RawFlowGraph secFlowgraph = combinedRawFlowgraph.getSecondaryFlowgraph();

    // add new raw basicblocks to graphs
    priFlowgraph.addNode(newRawPrimaryBasicblock);
    secFlowgraph.addNode(newRawSecondaryBasicblock);
    combinedRawFlowgraph.addNode(newRawCombinedBasicblock);

    // iterate over all the old edges and create corresponding new ones
    // this edges are already removed out of the graphs
    final Map<SuperViewNode, List<InternalStruct>> edgesToAddMap = new HashMap<>();
    for (final SuperViewEdge<? extends SuperViewNode> oldRawSuperJump : oldRawSuperJumps) {
      SuperViewNode superSource = oldRawSuperJump.getSource();
      RawCombinedBasicBlock combinedSource = (RawCombinedBasicBlock) superSource.getCombinedNode();
      RawBasicBlock priSource = combinedSource.getRawNode(ESide.PRIMARY);
      RawBasicBlock secSource = combinedSource.getRawNode(ESide.SECONDARY);

      SuperViewNode superTarget = oldRawSuperJump.getTarget();
      RawCombinedBasicBlock combinedTarget = (RawCombinedBasicBlock) superTarget.getCombinedNode();
      RawBasicBlock priTarget = combinedTarget.getRawNode(ESide.PRIMARY);
      RawBasicBlock secTarget = combinedTarget.getRawNode(ESide.SECONDARY);

      // get target basicblocks
      if (superTarget == oldRawPriUnmatchedSuperBasicblock
          || superTarget == oldRawSecUnmatchedSuperBasicblock) {
        superTarget = newRawSuperBasicblock;
        combinedTarget = newRawCombinedBasicblock;
        priTarget = newRawPrimaryBasicblock;
        secTarget = newRawSecondaryBasicblock;
      }

      // get source basicblocks - no else in case of recursive basicblocks
      if (superSource == oldRawPriUnmatchedSuperBasicblock
          || superSource == oldRawSecUnmatchedSuperBasicblock) {
        superSource = newRawSuperBasicblock;
        combinedSource = newRawCombinedBasicblock;
        priSource = newRawPrimaryBasicblock;
        secSource = newRawSecondaryBasicblock;
      }

      // fill up edge collected to add
      // in case of two unmatched jumps which may be matched now
      // this if ensures that those jumps are added only once with the correct match state
      InternalStruct struct = null;
      List<InternalStruct> structList = edgesToAddMap.get(superSource);
      if (structList != null) {
        for (final InternalStruct s : structList) {
          if (s.superTarget == superTarget) {
            struct = s;
            break;
          }
        }
      }

      if (struct == null) {
        struct = new InternalStruct();
        struct.superTarget = superTarget;
        struct.combinedSource = combinedSource;
        struct.combinedTarget = combinedTarget;
        struct.priSource = priSource;
        struct.priTarget = priTarget;
        struct.secSource = secSource;
        struct.secTarget = secTarget;

        if (structList == null) {
          structList = new ArrayList<>();
          edgesToAddMap.put(superSource, structList);
        }

        structList.add(struct);
      } else {
        if (priSource != null && priTarget != null) {
          struct.priSource = priSource;
          struct.priTarget = priTarget;
        } else {
          struct.secSource = secSource;
          struct.secTarget = secTarget;
        }
      }
      if (oldRawSuperJump.getSingleEdge(ESide.PRIMARY) != null) {
        struct.priJumpType = ((RawJump) oldRawSuperJump.getSingleEdge(ESide.PRIMARY)).getJumpType();
      }
      if (oldRawSuperJump.getSingleEdge(ESide.SECONDARY) != null) {
        struct.secJumpType =
            ((RawJump) oldRawSuperJump.getSingleEdge(ESide.SECONDARY)).getJumpType();
      }
    }

    // add edges to graphs
    for (final Entry<SuperViewNode, List<InternalStruct>> entry : edgesToAddMap.entrySet()) {
      for (final InternalStruct struct : entry.getValue()) {
        // create and add raw primary jump
        RawJump newPriRawJump = null;
        if (struct.priJumpType != null) {
          newPriRawJump = new RawJump(struct.priSource, struct.priTarget, struct.priJumpType);
          priFlowgraph.addEdge(newPriRawJump);
        }

        // create and add raw secondary jump
        RawJump newSecRawJump = null;
        if (struct.secJumpType != null) {
          newSecRawJump = new RawJump(struct.secSource, struct.secTarget, struct.secJumpType);
          secFlowgraph.addEdge(newSecRawJump);
        }

        // create and add combined raw jump
        final RawCombinedJump<RawCombinedBasicBlock> newRawCombinedJump =
            new RawCombinedJump<RawCombinedBasicBlock>(
                struct.combinedSource, struct.combinedTarget, newPriRawJump, newSecRawJump);
        combinedRawFlowgraph.addEdge(newRawCombinedJump);

        // create and link super raw jump (see super view edge ctor)
        new SuperViewEdge<>(newRawCombinedJump, entry.getKey(), struct.superTarget);
      }
    }
  }

  public static void addBasicblockMatch(
      final GraphsContainer graphs,
      final CombinedDiffNode oldPriUnmatchedCombinedDiffNode,
      final CombinedDiffNode oldSecUnmatchedCombinedDiffNode)
      throws GraphLayoutException {
    graphs.getCombinedGraph().getIntermediateListeners().blockZyLibVisibilityListeners();
    graphs.getCombinedGraph().getIntermediateListeners().blockZyLibSelectionListeners();

    try {
      final SuperDiffNode oldPriUnmatchedSuperDiffNode =
          oldPriUnmatchedCombinedDiffNode.getSuperDiffNode();
      final SuperDiffNode oldSecUnmatchedSuperDiffNode =
          oldSecUnmatchedCombinedDiffNode.getSuperDiffNode();
      final SingleDiffNode oldPriUnmatchedSingleDiffNode =
          oldPriUnmatchedCombinedDiffNode.getPrimaryDiffNode();
      final SingleDiffNode oldSecUnmatchedSingleDiffNode =
          oldSecUnmatchedCombinedDiffNode.getSecondaryDiffNode();

      final RawCombinedBasicBlock oldRawPriUnmatchedCombinedBasicblock =
          (RawCombinedBasicBlock) oldPriUnmatchedCombinedDiffNode.getRawNode();
      final RawCombinedBasicBlock oldRawSecUnmatchedCombinedBasicblock =
          (RawCombinedBasicBlock) oldSecUnmatchedCombinedDiffNode.getRawNode();
      final RawBasicBlock oldRawPriUnmatchedBasicblock =
          (RawBasicBlock) oldPriUnmatchedSingleDiffNode.getRawNode();
      final RawBasicBlock oldRawSecUnmatchedBasicblock =
          (RawBasicBlock) oldSecUnmatchedSingleDiffNode.getRawNode();
      final SuperViewNode oldRawPriUnmatchedSuperBasicblock =
          oldPriUnmatchedSuperDiffNode.getRawNode();
      final SuperViewNode oldRawSecUnmatchedSuperBasicblock =
          oldSecUnmatchedSuperDiffNode.getRawNode();

      final RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
          combinedRawFlowgraph =
              getRawCombinedFlowgraph(
                  graphs, oldPriUnmatchedCombinedDiffNode, oldSecUnmatchedCombinedDiffNode);
      final RawFlowGraph priRawFlowgraph = combinedRawFlowgraph.getPrimaryFlowgraph();
      final RawFlowGraph secRawFlowgraph = combinedRawFlowgraph.getSecondaryFlowgraph();

      // syncs visibility in async view mode
      BasicBlockMatchRemover.syncBasicblockVisibility(graphs, oldPriUnmatchedCombinedDiffNode);
      BasicBlockMatchRemover.syncBasicblockVisibility(graphs, oldSecUnmatchedCombinedDiffNode);

      // store old incoming and outgoing super jumps
      final Set<SuperViewEdge<? extends SuperViewNode>> oldRawSuperJumps = new HashSet<>();
      oldRawSuperJumps.addAll(oldRawPriUnmatchedSuperBasicblock.getIncomingEdges());
      oldRawSuperJumps.addAll(oldRawPriUnmatchedSuperBasicblock.getOutgoingEdges());
      oldRawSuperJumps.addAll(oldRawSecUnmatchedSuperBasicblock.getIncomingEdges());
      oldRawSuperJumps.addAll(oldRawSecUnmatchedSuperBasicblock.getOutgoingEdges());

      // delete raw nodes and edges
      combinedRawFlowgraph.removeNode(oldRawPriUnmatchedCombinedBasicblock);
      combinedRawFlowgraph.removeNode(oldRawSecUnmatchedCombinedBasicblock);
      oldRawPriUnmatchedSuperBasicblock.removeNode();
      oldRawSecUnmatchedSuperBasicblock.removeNode();
      priRawFlowgraph.removeNode(oldRawPriUnmatchedBasicblock);
      secRawFlowgraph.removeNode(oldRawSecUnmatchedBasicblock);

      // delete diff nodes and edges including possibly affected proximity nodes
      graphs.getCombinedGraph().deleteNode(oldPriUnmatchedCombinedDiffNode);
      graphs.getCombinedGraph().deleteNode(oldSecUnmatchedCombinedDiffNode);
      graphs.getSuperGraph().deleteNode(oldPriUnmatchedSuperDiffNode);
      graphs.getSuperGraph().deleteNode(oldSecUnmatchedSuperDiffNode);
      graphs.getPrimaryGraph().deleteNode(oldPriUnmatchedSingleDiffNode);
      graphs.getSecondaryGraph().deleteNode(oldSecUnmatchedSingleDiffNode);

      // creates basicblock match data (but does NOT add the created basicblock match to the
      // function match)
      final BasicBlockMatchData newBasicblockMatch =
          createBasicblockMatchData(
              oldRawPriUnmatchedCombinedBasicblock, oldRawSecUnmatchedCombinedBasicblock);

      // create new raw basicblocks
      final RawBasicBlock newRawPrimaryBasicblock =
          oldRawPriUnmatchedBasicblock.clone(EMatchState.MATCHED);
      final RawBasicBlock newRawSecondaryBasicblock =
          oldRawSecUnmatchedBasicblock.clone(EMatchState.MATCHED);
      final RawCombinedBasicBlock newRawCombinedBasicblock =
          new RawCombinedBasicBlock(
              newRawPrimaryBasicblock,
              newRawSecondaryBasicblock,
              newBasicblockMatch,
              newRawPrimaryBasicblock.getFunctionAddr(),
              newRawSecondaryBasicblock.getFunctionAddr());
      final SuperViewNode newRawSuperBasicblock = new SuperViewNode(newRawCombinedBasicblock);

      // add new raw basicblocks to raw graphs
      // create new raw jumps and add jumps to raw graph
      insertNewRawNodesAndEdges(
          combinedRawFlowgraph,
          oldRawSuperJumps,
          oldRawPriUnmatchedSuperBasicblock,
          oldRawSecUnmatchedSuperBasicblock,
          newRawPrimaryBasicblock,
          newRawSecondaryBasicblock,
          newRawCombinedBasicblock,
          newRawSuperBasicblock);

      // Add new basicblock match to function match (Note: Raw edges have to be added to the new raw
      // combined basicblock before this is called!)
      final FunctionMatchData functionMatch =
          graphs
              .getDiff()
              .getMatches()
              .getFunctionMatch(
                  newRawCombinedBasicblock.getPrimaryFunctionAddress(), ESide.PRIMARY);
      functionMatch.addBasicblockMatch(
          graphs.getDiff(), newBasicblockMatch, newRawCombinedBasicblock);

      // create new diff edges and add to diff graphs
      insertNewDiffNodesAndEdges(graphs, combinedRawFlowgraph, newRawSuperBasicblock);

      // notify intermediate listener to update graph node tree, selection history and main menu of
      // this view
      graphs
          .getDiff()
          .getMatches()
          .notifyBasicblockMatchAddedListener(
              newRawPrimaryBasicblock.getFunctionAddr(),
                  newRawSecondaryBasicblock.getFunctionAddr(),
              newRawPrimaryBasicblock.getAddress(), newRawSecondaryBasicblock.getAddress());
    } finally {
      graphs.getCombinedGraph().getIntermediateListeners().freeZyLibVisibilityListeners();
      graphs.getCombinedGraph().getIntermediateListeners().freeZyLibSelectionListeners();
    }

    // layout graphs
    BasicBlockMatchRemover.doSynchronizedLayout(graphs.getCombinedGraph());
  }

  public static Pair<CombinedDiffNode, CombinedDiffNode> getAffectedCombinedNodes(
      final BinDiffGraph<?, ?> graph) {
    CombinedDiffNode priUnmatchedNode = null;
    CombinedDiffNode secUnmatchedNode = null;

    int unmatchedCounter = 0;

    if (graph instanceof CombinedGraph) {
      for (final CombinedDiffNode node : ((CombinedGraph) graph).getSelectedNodes()) {
        if (node.getRawNode().getMatchState() == EMatchState.PRIMARY_UNMATCHED) {
          ++unmatchedCounter;
          priUnmatchedNode = node;
        } else if (node.getRawNode().getMatchState() == EMatchState.SECONDRAY_UNMATCHED) {
          ++unmatchedCounter;
          secUnmatchedNode = node;
        }
      }

      if (unmatchedCounter == 2
          && priUnmatchedNode != null
          && secUnmatchedNode != null
          && priUnmatchedNode.isVisible()
          && secUnmatchedNode.isVisible()) {
        return new Pair<>(priUnmatchedNode, secUnmatchedNode);
      }
    } else if (graph instanceof SingleGraph) {
      final SingleGraph priGraph = graph.getPrimaryGraph();
      final SingleGraph secGraph = graph.getSecondaryGraph();

      for (final SingleDiffNode node : priGraph.getSelectedNodes()) {
        if (node.getRawNode().getMatchState() == EMatchState.PRIMARY_UNMATCHED) {
          ++unmatchedCounter;
          priUnmatchedNode = node.getCombinedDiffNode();
        }
      }

      if (unmatchedCounter == 1) {
        for (final SingleDiffNode node : secGraph.getSelectedNodes()) {
          if (node.getRawNode().getMatchState() == EMatchState.SECONDRAY_UNMATCHED) {
            ++unmatchedCounter;
            secUnmatchedNode = node.getCombinedDiffNode();
          }
        }
      }
    }

    if (unmatchedCounter == 2
        && priUnmatchedNode != null
        && secUnmatchedNode != null
        && priUnmatchedNode.getPrimaryDiffNode().isVisible()
        && secUnmatchedNode.getSecondaryDiffNode().isVisible()) {
      return new Pair<>(priUnmatchedNode, secUnmatchedNode);
    }

    return null;
  }

  public static Pair<CombinedDiffNode, CombinedDiffNode> getAffectedCombinedNodes(
      final BinDiffGraph<?, ?> graph, final ZyGraphNode<?> clickedNode) {
    CombinedDiffNode priUnmatchedNode = null;
    CombinedDiffNode secUnmatchedNode = null;

    int priCounter = 0;
    int secCounter = 0;

    if (clickedNode instanceof CombinedDiffNode) {
      final CombinedDiffNode combinedClickedNode = (CombinedDiffNode) clickedNode;
      if (combinedClickedNode.getRawNode().getMatchState() == EMatchState.PRIMARY_UNMATCHED) {
        priUnmatchedNode = combinedClickedNode;
        for (final CombinedDiffNode selectedNode : graph.getCombinedGraph().getSelectedNodes()) {
          if (selectedNode == priUnmatchedNode) {
            ++priCounter;
            continue;
          }
          if (selectedNode.getRawNode().getMatchState() == EMatchState.SECONDRAY_UNMATCHED) {
            ++secCounter;
            secUnmatchedNode = selectedNode;
          }
        }
      } else if (combinedClickedNode.getRawNode().getMatchState()
          == EMatchState.SECONDRAY_UNMATCHED) {
        secUnmatchedNode = combinedClickedNode;
        for (final CombinedDiffNode selectedNode : graph.getCombinedGraph().getSelectedNodes()) {
          if (selectedNode.isVisible()) {
            if (selectedNode == secUnmatchedNode) {
              ++secCounter;
              continue;
            }
            if (selectedNode.getRawNode().getMatchState() == EMatchState.PRIMARY_UNMATCHED) {
              ++priCounter;
              priUnmatchedNode = selectedNode;
            }
          }
        }
      }
    } else if (clickedNode instanceof SingleDiffNode) {
      final SingleDiffNode singleClickedNode = (SingleDiffNode) clickedNode;
      if (singleClickedNode.isSelected() && singleClickedNode.isVisible()) {
        if (singleClickedNode.getSide() == ESide.PRIMARY) {
          priUnmatchedNode = singleClickedNode.getCombinedDiffNode();
          priCounter = 1;

          for (final SingleDiffNode selectedNode : graph.getSecondaryGraph().getSelectedNodes()) {
            if (selectedNode.getRawNode().getMatchState() == EMatchState.SECONDRAY_UNMATCHED
                && selectedNode.isVisible()) {
              ++secCounter;
              secUnmatchedNode = selectedNode.getCombinedDiffNode();
            }
          }
        } else {
          secUnmatchedNode = singleClickedNode.getCombinedDiffNode();
          secCounter = 1;
          for (final SingleDiffNode selectedNode : graph.getPrimaryGraph().getSelectedNodes()) {
            if (selectedNode.getRawNode().getMatchState() == EMatchState.PRIMARY_UNMATCHED
                && selectedNode.isVisible()) {
              ++priCounter;
              priUnmatchedNode = selectedNode.getCombinedDiffNode();
            }
          }
        }
      }
    }

    if (priCounter == 1
        && secCounter == 1
        && priUnmatchedNode != null
        && secUnmatchedNode != null) {
      return new Pair<>(priUnmatchedNode, secUnmatchedNode);
    }

    return null;
  }

  private static class InternalStruct {
    SuperViewNode superTarget = null;

    RawBasicBlock priSource = null;
    RawBasicBlock priTarget = null;
    RawBasicBlock secTarget = null;
    RawBasicBlock secSource = null;
    RawCombinedBasicBlock combinedSource = null;
    RawCombinedBasicBlock combinedTarget = null;
    EJumpType priJumpType = null;
    EJumpType secJumpType = null;
  }
}
