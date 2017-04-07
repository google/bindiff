package com.google.security.zynamics.bindiff.project.builders;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.exceptions.GraphCreationException;
import com.google.security.zynamics.bindiff.log.Logger;
import com.google.security.zynamics.bindiff.project.matches.BasicBlockMatchData;
import com.google.security.zynamics.bindiff.project.matches.FunctionMatchData;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedFlowGraph;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedJump;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawFlowGraph;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawJump;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.types.graphs.IGraphEdge;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RawCombinedFlowGraphBuilder {
  private static RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
      buildMatchedCombinedFlowgraph(
          final FunctionMatchData functionMatch,
          final RawFlowGraph primaryFlowgraph,
          final RawFlowGraph secondaryFlowgraph) {
    final IAddress priFunctionAddr = primaryFlowgraph.getAddress();
    final IAddress secFunctionAddr = secondaryFlowgraph.getAddress();

    final List<RawCombinedBasicBlock> combinedBasicblocks = new ArrayList<>();
    final List<RawCombinedJump<RawCombinedBasicBlock>> combinedJumps = new ArrayList<>();

    final Map<Pair<IAddress, IAddress>, RawCombinedBasicBlock> helperMap;
    helperMap = new HashMap<>();

    // add primary matched, secondary matched and primary unmatched combined basicblocks
    for (final RawBasicBlock primaryBasicblock : primaryFlowgraph) {
      final IAddress priBasicblockAddr = primaryBasicblock.getAddress();
      final IAddress secBasicblockAddr =
          functionMatch.getSecondaryBasicblockAddr(priBasicblockAddr);
      RawBasicBlock secondaryBasicblock = null;
      if (secBasicblockAddr != null) {
        secondaryBasicblock = secondaryFlowgraph.getBasicblock(secBasicblockAddr);
      }

      final BasicBlockMatchData basicblockMatch =
          functionMatch.getBasicblockMatch(priBasicblockAddr, ESide.PRIMARY);

      final RawCombinedBasicBlock combinedBasicblock =
          new RawCombinedBasicBlock(
              primaryBasicblock,
              secondaryBasicblock,
              basicblockMatch,
              priFunctionAddr,
              secFunctionAddr);
      helperMap.put(new Pair<>(priBasicblockAddr, secBasicblockAddr), combinedBasicblock);

      combinedBasicblocks.add(combinedBasicblock);
    }

    // add secondary unmatched combined basicblocks
    for (final RawBasicBlock secondaryBasicblock : secondaryFlowgraph) {
      final IAddress secBasicblockAddr = secondaryBasicblock.getAddress();
      final IAddress priBasicblockAddr = functionMatch.getPrimaryBasicblockAddr(secBasicblockAddr);
      if (priBasicblockAddr == null) {
        final RawCombinedBasicBlock combinedBasicblock =
            new RawCombinedBasicBlock(
                null, secondaryBasicblock, null, priFunctionAddr, secFunctionAddr);
        helperMap.put(new Pair<>(null, secBasicblockAddr), combinedBasicblock);

        combinedBasicblocks.add(combinedBasicblock);
      }
    }

    // add primary matched, secondary matched and primary unmatched combined jumps
    for (final RawJump primaryJump : primaryFlowgraph.getEdges()) {
      final IAddress priSrcAddr = primaryJump.getSource().getAddress();
      final IAddress priTarAddr = primaryJump.getTarget().getAddress();

      final IAddress secSrcAddr = functionMatch.getSecondaryBasicblockAddr(priSrcAddr);
      final IAddress secTarAddr = functionMatch.getSecondaryBasicblockAddr(priTarAddr);

      final Pair<IAddress, IAddress> srcHelperKey = new Pair<>(priSrcAddr, secSrcAddr);
      final Pair<IAddress, IAddress> tarHelperKey = new Pair<>(priTarAddr, secTarAddr);

      final RawJump secondaryJump = secondaryFlowgraph.getJump(secSrcAddr, secTarAddr);

      final RawCombinedBasicBlock srcCombinedBasicblock = helperMap.get(srcHelperKey);
      final RawCombinedBasicBlock tarCombinedBasicblock = helperMap.get(tarHelperKey);

      final RawCombinedJump<RawCombinedBasicBlock> combinedJump;
      combinedJump =
          new RawCombinedJump<>(
              srcCombinedBasicblock, tarCombinedBasicblock, primaryJump, secondaryJump);

      combinedJumps.add(combinedJump);
    }

    // secondary unmatches combined jumps
    final Set<IGraphEdge<RawBasicBlock>> primaryJumps = new HashSet<>();

    for (final IGraphEdge<RawBasicBlock> jump : primaryFlowgraph.getEdges()) {
      primaryJumps.add(jump);
    }

    for (final RawJump secondaryJump : secondaryFlowgraph.getEdges()) {
      final IAddress secSrcAddr = secondaryJump.getSource().getAddress();
      final IAddress secTarAddr = secondaryJump.getTarget().getAddress();

      final IAddress priSrcAddr = functionMatch.getPrimaryBasicblockAddr(secSrcAddr);
      final IAddress priTarAddr = functionMatch.getPrimaryBasicblockAddr(secTarAddr);

      final Pair<IAddress, IAddress> srcHelperKey = new Pair<>(priSrcAddr, secSrcAddr);
      final Pair<IAddress, IAddress> tarHelperKey = new Pair<>(priTarAddr, secTarAddr);

      boolean add = false;
      if (primaryFlowgraph == null) {
        add = true;
      } else {
        final IGraphEdge<RawBasicBlock> primaryJump =
            primaryFlowgraph.getJump(priSrcAddr, priTarAddr);
        if (!primaryJumps.contains(primaryJump)) {
          add = true;
        }
      }

      if (add) {
        final RawCombinedBasicBlock srcCombinedBasicblock = helperMap.get(srcHelperKey);
        final RawCombinedBasicBlock tarCombinedBasicblock = helperMap.get(tarHelperKey);

        final RawCombinedJump<RawCombinedBasicBlock> combinedJump =
            new RawCombinedJump<>(
                srcCombinedBasicblock, tarCombinedBasicblock, null, secondaryJump);
        combinedJumps.add(combinedJump);
      }
    }

    // create combined flow graph
    return new RawCombinedFlowGraph<>(
        combinedBasicblocks, combinedJumps, primaryFlowgraph, secondaryFlowgraph);
  }

  private static RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
      buildPrimaryUnmatchedCombinedFlowgraph(final RawFlowGraph primaryFlowgraph) {
    final IAddress priFunctionAddr = primaryFlowgraph.getAddress();

    final List<RawCombinedBasicBlock> combinedBasicblocks = new ArrayList<>();
    final List<RawCombinedJump<RawCombinedBasicBlock>> combinedJumps = new ArrayList<>();

    // add primary unmatched combined basicblocks
    final Map<IAddress, RawCombinedBasicBlock> helperMap = new HashMap<>();
    for (final RawBasicBlock primaryBasicblock : primaryFlowgraph) {
      final RawCombinedBasicBlock combinedBasicblock =
          new RawCombinedBasicBlock(primaryBasicblock, null, null, priFunctionAddr, null);
      helperMap.put(primaryBasicblock.getAddress(), combinedBasicblock);

      combinedBasicblocks.add(combinedBasicblock);
    }

    // add primary unmatched combined jumps
    for (final RawJump primaryJump : primaryFlowgraph.getEdges()) {
      final RawCombinedBasicBlock srcCombinedBasicblock =
          helperMap.get(primaryJump.getSource().getAddress());
      final RawCombinedBasicBlock tarCombinedBasicblock =
          helperMap.get(primaryJump.getTarget().getAddress());

      final RawCombinedJump<RawCombinedBasicBlock> combinedJump;
      combinedJump =
          new RawCombinedJump<>(srcCombinedBasicblock, tarCombinedBasicblock, primaryJump, null);

      combinedJumps.add(combinedJump);
    }

    // create combined flow graph
    return new RawCombinedFlowGraph<>(combinedBasicblocks, combinedJumps, primaryFlowgraph, null);
  }

  private static RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
      buildSecondaryUnmatchedCombinedFlowgraph(final RawFlowGraph secondaryFlowgraph) {
    final IAddress secFunctionAddr = secondaryFlowgraph.getAddress();

    final List<RawCombinedBasicBlock> combinedBasicblocks = new ArrayList<>();
    final List<RawCombinedJump<RawCombinedBasicBlock>> combinedJumps = new ArrayList<>();

    // add secondary unmatched combined basicblocks
    final Map<IAddress, RawCombinedBasicBlock> helperMap = new HashMap<>();
    for (final RawBasicBlock secondaryBasicblock : secondaryFlowgraph) {
      final RawCombinedBasicBlock combinedBasicblock =
          new RawCombinedBasicBlock(null, secondaryBasicblock, null, null, secFunctionAddr);
      helperMap.put(secondaryBasicblock.getAddress(), combinedBasicblock);

      combinedBasicblocks.add(combinedBasicblock);
    }

    // add secondary unmatched combined jumps
    for (final RawJump secondaryJump : secondaryFlowgraph.getEdges()) {
      final RawCombinedBasicBlock srcCombinedBasicblock =
          helperMap.get(secondaryJump.getSource().getAddress());
      final RawCombinedBasicBlock tarCombinedBasicblock =
          helperMap.get(secondaryJump.getTarget().getAddress());

      final RawCombinedJump<RawCombinedBasicBlock> combinedJump =
          new RawCombinedJump<>(srcCombinedBasicblock, tarCombinedBasicblock, null, secondaryJump);

      combinedJumps.add(combinedJump);
    }

    // create combined flow graph
    return new RawCombinedFlowGraph<>(combinedBasicblocks, combinedJumps, null, secondaryFlowgraph);
  }

  public static RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
      buildRawCombinedFlowgraph(
          final FunctionMatchData functionMatch,
          final RawFlowGraph primaryFlowgraph,
          final RawFlowGraph secondaryFlowgraph)
          throws GraphCreationException {
    Logger.logInfo(" - Building combined flow graphs");

    try {
      // build matched functions's combined basic blocks and combined jumps
      if (primaryFlowgraph != null && secondaryFlowgraph != null) {
        return buildMatchedCombinedFlowgraph(functionMatch, primaryFlowgraph, secondaryFlowgraph);
      }

      // build unmatched primary flowgraph's combined basicblocks and combined jumps
      if (primaryFlowgraph != null && secondaryFlowgraph == null) {
        return buildPrimaryUnmatchedCombinedFlowgraph(primaryFlowgraph);
      }

      // build secondary unmatched flow graph combined basic blocks and combined jumps
      if (primaryFlowgraph == null && secondaryFlowgraph != null) {
        return buildSecondaryUnmatchedCombinedFlowgraph(secondaryFlowgraph);
      }

      throw new GraphCreationException(
          "Primary and secondary flowgraph cannot both be null. Combined flow graphs creation failed.");
    } catch (final GraphCreationException e) {
      throw e;
    } catch (final Exception e) {
      // FIXME: Never catch all exceptions!
      throw new GraphCreationException("Combined flow graphs creation failed.");
    }
  }
}
