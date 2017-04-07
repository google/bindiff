package com.google.security.zynamics.bindiff.project.matches;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.types.Matches;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import java.util.List;

public class MatchData {
  private final DiffMetaData metaData;

  private Matches<FunctionMatchData> functionMatches;

  private final ListenerProvider<IMatchesChangeListener> listener = new ListenerProvider<>();

  public MatchData(final List<FunctionMatchData> functionMatchData, final DiffMetaData metaData) {
    this.metaData = Preconditions.checkNotNull(metaData);
    functionMatches = new Matches<>(Preconditions.checkNotNull(functionMatchData));
  }

  public void addListener(final IMatchesChangeListener listener) {
    this.listener.addListener(listener);
  }

  public void close() {
    functionMatches.clear();
  }

  public FunctionMatchData getFunctionMatch(final IAddress addr, final ESide side) {
    return functionMatches.get(addr, side);
  }

  public FunctionMatchData[] getFunctionMatches() {
    return functionMatches.getMatches().toArray(new FunctionMatchData[0]);
  }

  public IAddress getPrimaryFunctionAddr(final IAddress secAddr) {
    final FunctionMatchData match = functionMatches.get(secAddr, ESide.SECONDARY);
    return match != null ? match.getIAddress(ESide.PRIMARY) : null;
  }

  public IAddress getSecondaryFunctionAddr(final IAddress priAddr) {
    final FunctionMatchData match = functionMatches.get(priAddr, ESide.PRIMARY);
    return match != null ? match.getIAddress(ESide.SECONDARY) : null;
  }

  public int getSizeOfBasicblocks(final ESide side) {
    return metaData.getSizeOfBasicblocks(side);
  }

  public int getSizeOfCalls(final ESide side) {
    return metaData.getSizeOfCalls(side);
  }

  public int getSizeOfChangedCalls() {
    return metaData.getSizeOfChangedCalls();
  }

  public int getSizeOfChangedFunctions() {
    return metaData.getSizeOfChangedFunctions();
  }

  public int getSizeOfFunctions(final ESide side) {
    return metaData.getSizeOfFunctions(side);
  }

  public int getSizeOfInstructions(final ESide side) {
    return metaData.getSizeOfInstructions(side);
  }

  public int getSizeOfJumps(final ESide side) {
    return metaData.getSizeOfJumps(side);
  }

  public int getSizeOfMatchedBasicblocks() {
    return metaData.getSizeOfMatchedBasicblocks();
  }

  public int getSizeOfMatchedCalls() {
    return metaData.getSizeOfMatchedCalls();
  }

  public int getSizeOfMatchedFunctions() {
    return metaData.getSizeOfMatchedFunctions();
  }

  public int getSizeOfMatchedInstructions() {
    return metaData.getSizeOfMatchedInstructions();
  }

  public int getSizeOfMatchedJumps() {
    return metaData.getSizeOfMatchedJumps();
  }

  public int getSizeOfUnmatchedBasicblocks(final ESide side) {
    return metaData.getSizeOfUnmatchedBasicblocks(side);
  }

  public int getSizeOfUnmatchedCalls(final ESide side) {
    return metaData.getSizeOfUnmatchedCalls(side);
  }

  public int getSizeOfUnmatchedFunctions(final ESide side) {
    return metaData.getSizeOfUnmatchedFunctions(side);
  }

  public int getSizeOfUnmatchedInstructions(final ESide side) {
    return metaData.getSizeOfUnmatchedInstructions(side);
  }

  public int getSizeOfUnmatchedJumps(final ESide side) {
    return metaData.getSizeOfUnmatchedJumps(side);
  }

  public boolean isFunctionMatch(final IAddress priAddr, final IAddress secAddr) {
    final IAddress addr = getSecondaryFunctionAddr(priAddr);

    return addr != null && addr.equals(secAddr);
  }

  public void notifyBasicblockMatchAddedListener(
      final IAddress priFunctionAddr,
      final IAddress secFunctionAddr,
      final IAddress primaryAddr,
      final IAddress secondaryAddr) {
    for (final IMatchesChangeListener listener : listener) {
      listener.addedBasicblockMatch(priFunctionAddr, secFunctionAddr, primaryAddr, secondaryAddr);
    }
  }

  public void notifyBasicblockMatchRemovedListener(
      final IAddress priFunctionAddr,
      final IAddress secFunctionAddr,
      final IAddress primaryAddr,
      final IAddress secondaryAddr) {
    for (final IMatchesChangeListener listener : listener) {
      listener.removedBasicblockMatch(priFunctionAddr, secFunctionAddr, primaryAddr, secondaryAddr);
    }
  }

  public void removeListener(final IMatchesChangeListener listener) {
    this.listener.removeListener(listener);
  }

  public void setSizeOfChangedCalls(final int changedCalls) {
    metaData.setSizeOfChangedCalls(changedCalls);
  }

  public void setSizeOfChangedFunctions(final int changedFunctions) {
    metaData.setSizeOfChangedFunctions(changedFunctions);
  }

  public void setSizeOfMatchedBasicblocks(final int matchedBasicblocks) {
    metaData.setSizeOfMatchedBasicblocks(matchedBasicblocks);
  }

  public void setSizeOfMatchedCalls(final int matchedCalls) {
    metaData.setSizeOfMatchedCalls(matchedCalls);
  }

  public void setSizeOfMatchedInstructions(final int matchedInstructions) {
    metaData.setSizeOfMatchedInstructions(matchedInstructions);
  }

  public void setSizeOfMatchedJumps(final int matchedJumps) {
    metaData.setSizeOfMatchedJumps(matchedJumps);
  }
}
