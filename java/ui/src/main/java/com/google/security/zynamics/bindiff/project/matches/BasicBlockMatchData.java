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

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.types.Matches;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;

public class BasicBlockMatchData implements IAddressPair {
  private final IAddressPair addrPair;

  private final int algoId;

  private final Matches<InstructionMatchData> instructionMatches;

  public BasicBlockMatchData(
      final long priAddr,
      final long secAddr,
      final int algoId,
      final Matches<InstructionMatchData> instructionMatches) {
    this.addrPair = new AddressPair(priAddr, secAddr);
    this.algoId = algoId;
    this.instructionMatches = instructionMatches;
  }

  @Override
  public long getAddress(final ESide side) {
    return addrPair.getAddress(side);
  }

  public int getAlgorithmId() {
    return algoId;
  }

  @Override
  public IAddress getIAddress(final ESide side) {
    return new CAddress(getAddress(side));
  }

  public InstructionMatchData getInstructionMatch(
      final IAddress instructionAddr, final ESide side) {
    return instructionMatches.get(instructionAddr, side);
  }

  public InstructionMatchData[] getInstructionMatches() {
    return instructionMatches.getMatches().toArray(new InstructionMatchData[0]);
  }

  public IAddress getPrimaryInstructionAddr(final IAddress secAddress) {
    final InstructionMatchData instructionMatch =
        instructionMatches.get(secAddress, ESide.SECONDARY);

    return instructionMatch != null ? instructionMatch.getIAddress(ESide.PRIMARY) : null;
  }

  public IAddress getSecondaryInstructionAddr(final IAddress priAddress) {
    final InstructionMatchData instructionMatch = instructionMatches.get(priAddress, ESide.PRIMARY);

    return instructionMatch != null ? instructionMatch.getIAddress(ESide.SECONDARY) : null;
  }

  public int getSizeOfMatchedInstructions() {
    return instructionMatches.size();
  }

  public boolean isInstructionMatch(final IAddress priAddr) {
    final IAddress addr = getSecondaryInstructionAddr(priAddr);
    return addr != null && addr.equals(priAddr);
  }
}
