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

package com.google.security.zynamics.bindiff.project.builders;

import com.google.common.flogger.FluentLogger;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.exceptions.GraphCreationException;
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
import java.util.logging.Level;

public class RawCombinedFlowGraphBuilder {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private static RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
      buildMatchedCombinedFlowGraph(
          final FunctionMatchData functionMatch,
          final RawFlowGraph primaryFlowGraph,
          final RawFlowGraph secondaryFlowGraph) {
    final IAddress priFunctionAddr = primaryFlowGraph.getAddress();
    final IAddress secFunctionAddr = secondaryFlowGraph.getAddress();

    final List<RawCombinedBasicBlock> combinedBasicblocks = new ArrayList<>();
    final List<RawCombinedJump<RawCombinedBasicBlock>> combinedJumps = new ArrayList<>();

    final Map<Pair<IAddress, IAddress>, RawCombinedBasicBlock> helperMap;
    helperMap = new HashMap<>();

    // Add primary matched, secondary matched and primary unmatched combined basic blocks
    for (final RawBasicBlock primaryBasicBlock : primaryFlowGraph) {
      final IAddress priBasicBlockAddr = primaryBasicBlock.getAddress();
      final IAddress secBasicBlockAddr =
          functionMatch.getSecondaryBasicblockAddr(priBasicBlockAddr);
      RawBasicBlock secondaryBasicBlock = null;
      if (secBasicBlockAddr != null) {
        secondaryBasicBlock = secondaryFlowGraph.getBasicblock(secBasicBlockAddr);
      }

      final BasicBlockMatchData basicBlockMatch =
          functionMatch.getBasicBlockMatch(priBasicBlockAddr, ESide.PRIMARY);

      final RawCombinedBasicBlock combinedBasicBlock =
          new RawCombinedBasicBlock(
              primaryBasicBlock,
              secondaryBasicBlock,
              basicBlockMatch,
              priFunctionAddr,
              secFunctionAddr);
      helperMap.put(new Pair<>(priBasicBlockAddr, secBasicBlockAddr), combinedBasicBlock);

      combinedBasicblocks.add(combinedBasicBlock);
    }

    // Add secondary unmatched combined basic blocks
    for (final RawBasicBlock secondaryBasicBlock : secondaryFlowGraph) {
      final IAddress secBasicBlockAddr = secondaryBasicBlock.getAddress();
      final IAddress priBasicBlockAddr = functionMatch.getPrimaryBasicblockAddr(secBasicBlockAddr);
      if (priBasicBlockAddr == null) {
        final RawCombinedBasicBlock combinedBasicBlock =
            new RawCombinedBasicBlock(
                null, secondaryBasicBlock, null, priFunctionAddr, secFunctionAddr);
        helperMap.put(new Pair<>(null, secBasicBlockAddr), combinedBasicBlock);

        combinedBasicblocks.add(combinedBasicBlock);
      }
    }

    // Add primary matched, secondary matched and primary unmatched combined jumps
    for (final RawJump primaryJump : primaryFlowGraph.getEdges()) {
      final IAddress priSrcAddr = primaryJump.getSource().getAddress();
      final IAddress priTarAddr = primaryJump.getTarget().getAddress();

      final IAddress secSrcAddr = functionMatch.getSecondaryBasicblockAddr(priSrcAddr);
      final IAddress secTarAddr = functionMatch.getSecondaryBasicblockAddr(priTarAddr);

      final Pair<IAddress, IAddress> srcHelperKey = new Pair<>(priSrcAddr, secSrcAddr);
      final Pair<IAddress, IAddress> tarHelperKey = new Pair<>(priTarAddr, secTarAddr);

      final RawJump secondaryJump = secondaryFlowGraph.getJump(secSrcAddr, secTarAddr);

      final RawCombinedBasicBlock srcCombinedBasicBlock = helperMap.get(srcHelperKey);
      final RawCombinedBasicBlock tarCombinedBasicBlock = helperMap.get(tarHelperKey);

      final RawCombinedJump<RawCombinedBasicBlock> combinedJump;
      combinedJump =
          new RawCombinedJump<>(
              srcCombinedBasicBlock, tarCombinedBasicBlock, primaryJump, secondaryJump);

      combinedJumps.add(combinedJump);
    }

    // Secondary unmatched combined jumps
    final Set<IGraphEdge<RawBasicBlock>> primaryJumps = new HashSet<>(primaryFlowGraph.getEdges());

    for (final RawJump secondaryJump : secondaryFlowGraph.getEdges()) {
      final IAddress secSrcAddr = secondaryJump.getSource().getAddress();
      final IAddress secTarAddr = secondaryJump.getTarget().getAddress();

      final IAddress priSrcAddr = functionMatch.getPrimaryBasicblockAddr(secSrcAddr);
      final IAddress priTarAddr = functionMatch.getPrimaryBasicblockAddr(secTarAddr);

      final Pair<IAddress, IAddress> srcHelperKey = new Pair<>(priSrcAddr, secSrcAddr);
      final Pair<IAddress, IAddress> tarHelperKey = new Pair<>(priTarAddr, secTarAddr);

      boolean add = false;
      final IGraphEdge<RawBasicBlock> primaryJump =
          primaryFlowGraph.getJump(priSrcAddr, priTarAddr);
      if (!primaryJumps.contains(primaryJump)) {
        add = true;
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

    // Create combined flow graph
    return new RawCombinedFlowGraph<>(
        combinedBasicblocks, combinedJumps, primaryFlowGraph, secondaryFlowGraph);
  }

  private static RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
      buildPrimaryUnmatchedCombinedFlowGraph(final RawFlowGraph primaryFlowGraph) {
    final IAddress priFunctionAddr = primaryFlowGraph.getAddress();

    final List<RawCombinedBasicBlock> combinedBasicBlocks = new ArrayList<>();
    final List<RawCombinedJump<RawCombinedBasicBlock>> combinedJumps = new ArrayList<>();

    // Add primary unmatched combined basic blocks
    final Map<IAddress, RawCombinedBasicBlock> helperMap = new HashMap<>();
    for (final RawBasicBlock primaryBasicBlock : primaryFlowGraph) {
      final RawCombinedBasicBlock combinedBasicBlock =
          new RawCombinedBasicBlock(primaryBasicBlock, null, null, priFunctionAddr, null);
      helperMap.put(primaryBasicBlock.getAddress(), combinedBasicBlock);

      combinedBasicBlocks.add(combinedBasicBlock);
    }

    // Add primary unmatched combined jumps
    for (final RawJump primaryJump : primaryFlowGraph.getEdges()) {
      final RawCombinedBasicBlock srcCombinedBasicblock =
          helperMap.get(primaryJump.getSource().getAddress());
      final RawCombinedBasicBlock tarCombinedBasicblock =
          helperMap.get(primaryJump.getTarget().getAddress());

      final RawCombinedJump<RawCombinedBasicBlock> combinedJump;
      combinedJump =
          new RawCombinedJump<>(srcCombinedBasicblock, tarCombinedBasicblock, primaryJump, null);

      combinedJumps.add(combinedJump);
    }

    // Create combined flow graph
    return new RawCombinedFlowGraph<>(combinedBasicBlocks, combinedJumps, primaryFlowGraph, null);
  }

  private static RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
      buildSecondaryUnmatchedCombinedFlowgraph(final RawFlowGraph secondaryFlowGraph) {
    final IAddress secFunctionAddr = secondaryFlowGraph.getAddress();

    final List<RawCombinedBasicBlock> combinedBasicBlocks = new ArrayList<>();
    final List<RawCombinedJump<RawCombinedBasicBlock>> combinedJumps = new ArrayList<>();

    // Add secondary unmatched combined basic blocks
    final Map<IAddress, RawCombinedBasicBlock> helperMap = new HashMap<>();
    for (final RawBasicBlock secondaryBasicBlock : secondaryFlowGraph) {
      final RawCombinedBasicBlock combinedBasicBlock =
          new RawCombinedBasicBlock(null, secondaryBasicBlock, null, null, secFunctionAddr);
      helperMap.put(secondaryBasicBlock.getAddress(), combinedBasicBlock);

      combinedBasicBlocks.add(combinedBasicBlock);
    }

    // Add secondary unmatched combined jumps
    for (final RawJump secondaryJump : secondaryFlowGraph.getEdges()) {
      final RawCombinedBasicBlock srcCombinedBasicBlock =
          helperMap.get(secondaryJump.getSource().getAddress());
      final RawCombinedBasicBlock tarCombinedBasicBlock =
          helperMap.get(secondaryJump.getTarget().getAddress());

      final RawCombinedJump<RawCombinedBasicBlock> combinedJump =
          new RawCombinedJump<>(srcCombinedBasicBlock, tarCombinedBasicBlock, null, secondaryJump);

      combinedJumps.add(combinedJump);
    }

    // Create combined flow graph
    return new RawCombinedFlowGraph<>(combinedBasicBlocks, combinedJumps, null, secondaryFlowGraph);
  }

  public static RawCombinedFlowGraph<RawCombinedBasicBlock, RawCombinedJump<RawCombinedBasicBlock>>
      buildRawCombinedFlowGraph(
          final FunctionMatchData functionMatch,
          final RawFlowGraph primaryFlowGraph,
          final RawFlowGraph secondaryFlowGraph)
          throws GraphCreationException {
    logger.at(Level.INFO).log(" - Building combined flow graphs");

    // Build matched functions's combined basic blocks and combined jumps
    if (primaryFlowGraph != null && secondaryFlowGraph != null) {
      return buildMatchedCombinedFlowGraph(functionMatch, primaryFlowGraph, secondaryFlowGraph);
    }

    // Build unmatched primary flow graph's combined basic blocks and combined jumps
    if (primaryFlowGraph != null) {
      return buildPrimaryUnmatchedCombinedFlowGraph(primaryFlowGraph);
    }

    // Build secondary unmatched flow graph combined basic blocks and combined jumps
    if (secondaryFlowGraph != null) {
      return buildSecondaryUnmatchedCombinedFlowgraph(secondaryFlowGraph);
    }

    throw new GraphCreationException("Combined flow graphs creation failed.");
  }
}
