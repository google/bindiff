package com.google.security.zynamics.bindiff.project.matches;

import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.edges.CombinedViewEdge;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedViewNode;
import com.google.security.zynamics.bindiff.project.diff.Diff;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedBasicBlock;
import com.google.security.zynamics.bindiff.project.rawflowgraph.RawCombinedJump;
import com.google.security.zynamics.bindiff.types.Matches;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FunctionMatchData implements IAddressPair {
  private final int id;

  private final IAddressPair addrPair;

  private final double similarity;
  private final double confidence;

  private final int flags;
  private final int algoId;

  private Matches<BasicBlockMatchData> basicblockMatches = null;

  private int matchedBasicBlocks;
  private int matchedJumps;
  private int matchedInstructions;

  public FunctionMatchData(
      final int id,
      final long priAddr,
      final long secAddr,
      final double similarity,
      final double confidence,
      final int flags,
      final int algoId,
      final int matchedBasicBlocks,
      final int matchedJumps,
      final int matchedInstructions) {
    this.id = id;

    this.addrPair = new AddressPair(priAddr, secAddr);

    this.similarity = similarity;
    this.confidence = confidence;

    this.flags = flags;
    this.algoId = algoId;

    this.matchedBasicBlocks = matchedBasicBlocks;
    this.matchedJumps = matchedJumps;
    this.matchedInstructions = matchedInstructions;
  }

  protected int getId() {
    return id;
  }

  public void addBasicblockMatch(
      final Diff diff,
      final BasicBlockMatchData newBasicblockMatch,
      final RawCombinedBasicBlock matchedCombinedNode) {
    basicblockMatches.put(newBasicblockMatch);

    final Set<CombinedViewEdge<? extends CombinedViewNode>> newJumps = new HashSet<>();
    newJumps.addAll(matchedCombinedNode.getIncomingEdges());
    newJumps.addAll(matchedCombinedNode.getOutgoingEdges());

    int newMatchedJumpsCounter = 0;
    for (final CombinedViewEdge<? extends CombinedViewNode> combinedJump : newJumps) {
      if (combinedJump.getMatchState() == EMatchState.MATCHED) {
        newMatchedJumpsCounter++;
      }
    }

    final int instructionMatches = newBasicblockMatch.getInstructionMatches().length;

    matchedInstructions += instructionMatches;
    matchedJumps += newMatchedJumpsCounter;
    matchedBasicBlocks++;

    final MatchData matches = diff.getMatches();

    matches.setSizeOfMatchedBasicblocks(matches.getSizeOfMatchedBasicblocks() + 1);
    matches.setSizeOfMatchedJumps(matches.getSizeOfMatchedJumps() + newMatchedJumpsCounter);
    matches.setSizeOfMatchedInstructions(
        matches.getSizeOfMatchedInstructions() + instructionMatches);
  }

  @Override
  public long getAddress(final ESide side) {
    return addrPair.getAddress(side);
  }

  public int getAlgorithmId() {
    return algoId;
  }

  public String getAlgorithmName() {
    return "TODO";
  }

  public BasicBlockMatchData getBasicblockMatch(final IAddress addr, final ESide side) {
    return basicblockMatches != null ? basicblockMatches.get(addr, side) : null;
  }

  public Collection<BasicBlockMatchData> getBasicBlockMatches() {
    return basicblockMatches != null ? basicblockMatches.getMatches() : null;
  }

  public double getConfidence() {
    return confidence;
  }

  public int getFlags() {
    return flags;
  }

  @Override
  public IAddress getIAddress(final ESide side) {
    return new CAddress(addrPair.getAddress(side));
  }

  public Map<IAddress, IAddress> getInstructionsAddressMap(final ESide keySide) {
    if (basicblockMatches != null) {
      final Map<IAddress, IAddress> map = new HashMap<>();

      for (final BasicBlockMatchData basicblockMatch : basicblockMatches.getMatches()) {
        final InstructionMatchData[] instructionMatches = basicblockMatch.getInstructionMatches();

        for (final InstructionMatchData instructionMatch : instructionMatches) {
          final ESide valueSide = keySide == ESide.PRIMARY ? ESide.SECONDARY : ESide.PRIMARY;
          map.put(instructionMatch.getIAddress(keySide), instructionMatch.getIAddress(valueSide));
        }
      }

      return map;
    }

    return null;
  }

  public IAddress getPrimaryBasicblockAddr(final IAddress secAddr) {
    if (basicblockMatches != null) {
      final BasicBlockMatchData basicblockMatch = basicblockMatches.get(secAddr, ESide.SECONDARY);

      return basicblockMatch != null ? basicblockMatch.getIAddress(ESide.PRIMARY) : null;
    }

    return null;
  }

  public IAddress getSecondaryBasicblockAddr(final IAddress priAddr) {
    if (basicblockMatches != null) {
      final BasicBlockMatchData basicblockMatch = basicblockMatches.get(priAddr, ESide.PRIMARY);

      return basicblockMatch != null ? basicblockMatch.getIAddress(ESide.SECONDARY) : null;
    }

    return null;
  }

  public double getSimilarity() {
    return similarity;
  }

  public int getSizeOfMatchedBasicblocks() {
    return matchedBasicBlocks;
  }

  public int getSizeOfMatchedInstructions() {
    return matchedInstructions;
  }

  public int getSizeOfMatchedJumps() {
    return matchedJumps;
  }

  public boolean isBasicblockMatch(final IAddress priAddr, final IAddress secAddr) {
    final IAddress addr = getSecondaryBasicblockAddr(priAddr);

    return addr != null && addr.equals(secAddr);
  }

  public boolean isLoaded() {
    return basicblockMatches != null;
  }

  public void loadBasicBlockMatches(final List<BasicBlockMatchData> matches) {
    basicblockMatches = new Matches<>(matches);
  }

  public void removeBasicblockMatch(
      final Diff diff, final RawCombinedBasicBlock oldMatchedBasicblock) {
    final IAddress priAddress = oldMatchedBasicblock.getAddress(ESide.PRIMARY);
    final IAddress secAddress = oldMatchedBasicblock.getAddress(ESide.SECONDARY);

    final BasicBlockMatchData removedBasicblockMatch =
        basicblockMatches.remove(priAddress, secAddress);

    if (removedBasicblockMatch != null) {
      final int removedMatchedInstructions = removedBasicblockMatch.getSizeOfMatchedInstructions();

      // used a set in order to ensure recursive jumps are counted only once
      final Set<CombinedViewEdge<? extends CombinedViewNode>> combinedJumps = new HashSet<>();
      combinedJumps.addAll(oldMatchedBasicblock.getOutgoingEdges());
      combinedJumps.addAll(oldMatchedBasicblock.getIncomingEdges());

      int matchedJumpsToRemoveCounter = 0;

      for (final CombinedViewEdge<? extends CombinedViewNode> edge : combinedJumps) {
        if (((RawCombinedJump<?>) edge).getMatchState() == EMatchState.MATCHED) {
          ++matchedJumpsToRemoveCounter;
        }
      }

      matchedInstructions -= removedMatchedInstructions;
      matchedJumps -= matchedJumpsToRemoveCounter;
      matchedBasicBlocks--;

      final MatchData matches = diff.getMatches();

      matches.setSizeOfMatchedBasicblocks(matches.getSizeOfMatchedBasicblocks() - 1);
      matches.setSizeOfMatchedJumps(matches.getSizeOfMatchedJumps() - matchedJumpsToRemoveCounter);
      matches.setSizeOfMatchedInstructions(
          matches.getSizeOfMatchedInstructions() - removedMatchedInstructions);
    }
  }

  public void setSizeOfMatchedBasicblocks(final int basicblocks) {
    matchedBasicBlocks = basicblocks;
  }

  public void setSizeOfMatchedInstructions(final int instructions) {
    matchedInstructions = instructions;
  }

  public void setSizeOfMatchedJumps(final int jumps) {
    matchedJumps = jumps;
  }
}
