// Copyright 2011-2023 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.bindiff.project.matches;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.types.Matches;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import java.util.List;

/** Holds diff metadata, function matches and listeners. */
public class MatchData {
  private final DiffMetadata metadata;

  private Matches<FunctionMatchData> functionMatches;

  private final ListenerProvider<IMatchesChangeListener> listener = new ListenerProvider<>();

  public MatchData(final List<FunctionMatchData> functionMatchData, final DiffMetadata metadata) {
    this.metadata = checkNotNull(metadata);
    functionMatches = new Matches<>(checkNotNull(functionMatchData));
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

  public int getSizeOfBasicBlocks(final ESide side) {
    return metadata.getSizeOfBasicBlocks(side);
  }

  public int getSizeOfCalls(final ESide side) {
    return metadata.getSizeOfCalls(side);
  }

  public int getSizeOfChangedCalls() {
    return metadata.getSizeOfChangedCalls();
  }

  public int getSizeOfChangedFunctions() {
    return metadata.getSizeOfChangedFunctions();
  }

  public int getSizeOfFunctions(final ESide side) {
    return metadata.getSizeOfFunctions(side);
  }

  public int getSizeOfInstructions(final ESide side) {
    return metadata.getSizeOfInstructions(side);
  }

  public int getSizeOfJumps(final ESide side) {
    return metadata.getSizeOfJumps(side);
  }

  public int getSizeOfMatchedBasicBlocks() {
    return metadata.getSizeOfMatchedBasicBlocks();
  }

  public int getSizeOfMatchedCalls() {
    return metadata.getSizeOfMatchedCalls();
  }

  public int getSizeOfMatchedFunctions() {
    return metadata.getSizeOfMatchedFunctions();
  }

  public int getSizeOfMatchedInstructions() {
    return metadata.getSizeOfMatchedInstructions();
  }

  public int getSizeOfMatchedJumps() {
    return metadata.getSizeOfMatchedJumps();
  }

  public int getSizeOfUnmatchedBasicBlocks(final ESide side) {
    return metadata.getSizeOfUnmatchedBasicblocks(side);
  }

  public int getSizeOfUnmatchedCalls(final ESide side) {
    return metadata.getSizeOfUnmatchedCalls(side);
  }

  public int getSizeOfUnmatchedFunctions(final ESide side) {
    return metadata.getSizeOfUnmatchedFunctions(side);
  }

  public int getSizeOfUnmatchedInstructions(final ESide side) {
    return metadata.getSizeOfUnmatchedInstructions(side);
  }

  public int getSizeOfUnmatchedJumps(final ESide side) {
    return metadata.getSizeOfUnmatchedJumps(side);
  }

  public boolean isFunctionMatch(final IAddress priAddr, final IAddress secAddr) {
    final IAddress addr = getSecondaryFunctionAddr(priAddr);

    return addr != null && addr.equals(secAddr);
  }

  public void notifyBasicBlockMatchAddedListener(
      final IAddress priFunctionAddr,
      final IAddress secFunctionAddr,
      final IAddress primaryAddr,
      final IAddress secondaryAddr) {
    for (final IMatchesChangeListener listener : listener) {
      listener.addedBasicBlockMatch(priFunctionAddr, secFunctionAddr, primaryAddr, secondaryAddr);
    }
  }

  public void notifyBasicBlockMatchRemovedListener(
      final IAddress priFunctionAddr,
      final IAddress secFunctionAddr,
      final IAddress primaryAddr,
      final IAddress secondaryAddr) {
    for (final IMatchesChangeListener listener : listener) {
      listener.removedBasicBlockMatch(priFunctionAddr, secFunctionAddr, primaryAddr, secondaryAddr);
    }
  }

  public void removeListener(final IMatchesChangeListener listener) {
    this.listener.removeListener(listener);
  }

  public void setSizeOfChangedCalls(final int changedCalls) {
    metadata.setSizeOfChangedCalls(changedCalls);
  }

  public void setSizeOfChangedFunctions(final int changedFunctions) {
    metadata.setSizeOfChangedFunctions(changedFunctions);
  }

  public void setSizeOfMatchedBasicBlocks(final int matchedBasicBlocks) {
    metadata.setSizeOfMatchedBasicBlocks(matchedBasicBlocks);
  }

  public void setSizeOfMatchedCalls(final int matchedCalls) {
    metadata.setSizeOfMatchedCalls(matchedCalls);
  }

  public void setSizeOfMatchedInstructions(final int matchedInstructions) {
    metadata.setSizeOfMatchedInstructions(matchedInstructions);
  }

  public void setSizeOfMatchedJumps(final int matchedJumps) {
    metadata.setSizeOfMatchedJumps(matchedJumps);
  }
}
