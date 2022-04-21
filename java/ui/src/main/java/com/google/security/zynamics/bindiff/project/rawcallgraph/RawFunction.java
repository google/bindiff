// Copyright 2011-2022 Google LLC
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

package com.google.security.zynamics.bindiff.project.rawcallgraph;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.security.zynamics.bindiff.enums.EFunctionType;
import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.nodes.SingleViewNode;
import com.google.security.zynamics.bindiff.project.matches.FunctionMatchData;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// FLAGS 1 (4 Bit)
// Bit 0 = NORMAL
// Bit 1 = LIBRARY
// Bit 2 = IMPORTED
// Bit 3 = THUNK

public class RawFunction extends SingleViewNode {
  private final String name;
  private final EFunctionType type;
  private final ESide side;

  private FunctionMatchData match = null;
  private RawFunction matchedFunction = null;

  private int basicBlocks = -1;
  private int jumps = -1;
  private int instructions = -1;

  private String comment = "";

  public RawFunction(
      final IAddress address, final String name, final EFunctionType type, final ESide side) {
    super(address, -1, 0, 0, Color.WHITE, Color.BLACK, false, true);
    this.name = checkNotNull(name);
    this.type = checkNotNull(type);
    this.side = checkNotNull(side);
  }

  public Set<RawFunction> getCallees() {
    final List<SingleViewNode> children = getChildren();

    final Set<RawFunction> castedChildren = new HashSet<>();

    for (final SingleViewNode node : children) {
      castedChildren.add((RawFunction) node);
    }

    return castedChildren;
  }

  public Set<RawFunction> getCallers() {
    final List<SingleViewNode> parents = getParents();

    final Set<RawFunction> castedParents = new HashSet<>();

    for (final SingleViewNode node : parents) {
      castedParents.add((RawFunction) node);
    }

    return castedParents;
  }

  public String getComment() {
    return comment;
  }

  public FunctionMatchData getFunctionMatch() {
    return match;
  }

  public EFunctionType getFunctionType() {
    return type;
  }

  public FunctionMatchData getMatch() {
    return match;
  }

  public RawFunction getMatchedFunction() {
    return matchedFunction;
  }

  public IAddress getMatchedFunctionAddress() {
    if (getMatchState() == EMatchState.MATCHED) {
      return match.getIAddress(side == ESide.PRIMARY ? ESide.SECONDARY : ESide.PRIMARY);
    }

    return null;
  }

  @Override
  public EMatchState getMatchState() {
    if (match == null) {
      return side == ESide.PRIMARY
          ? EMatchState.PRIMARY_UNMATCHED
          : EMatchState.SECONDRAY_UNMATCHED;
    }

    return EMatchState.MATCHED;
  }

  public String getName() {
    return name;
  }

  @Override
  public ESide getSide() {
    return side;
  }

  public int getSizeOfBasicBlocks() {
    return basicBlocks;
  }

  public int getSizeOfInstructions() {
    return instructions;
  }

  public int getSizeOfJumps() {
    return jumps;
  }

  public int getSizeOfMatchedBasicBlocks() {
    return match == null ? 0 : match.getSizeOfMatchedBasicBlocks();
  }

  public int getSizeOfMatchedInstructions() {
    return match == null ? 0 : match.getSizeOfMatchedInstructions();
  }

  public int getSizeOfMatchedJumps() {
    return match == null ? 0 : match.getSizeOfMatchedJumps();
  }

  public int getSizeOfUnmatchedBasicBlocks() {
    return basicBlocks - match.getSizeOfMatchedBasicBlocks();
  }

  public int getSizeOfUnmatchedInstructions() {
    return instructions - match.getSizeOfMatchedInstructions();
  }

  public int getSizeOfUnmatchedJumps() {
    return jumps - match.getSizeOfMatchedJumps();
  }

  public boolean isChanged() {
    if (getMatchState() == EMatchState.MATCHED) {
      final boolean basicBlocks =
          this.basicBlocks == matchedFunction.getSizeOfBasicBlocks()
              && this.basicBlocks == getSizeOfMatchedBasicBlocks();
      final boolean jumps =
          this.jumps == matchedFunction.getSizeOfJumps() && this.jumps == getSizeOfMatchedJumps();
      final boolean instructions =
          this.instructions == matchedFunction.getSizeOfInstructions()
              && this.instructions == getSizeOfMatchedInstructions();

      return !basicBlocks || !jumps || !instructions;
    }

    return false;
  }

  public boolean isChangedInstructionsOnlyMatch() {
    if (getMatchState() == EMatchState.MATCHED) {
      final boolean basicBlocks =
          this.basicBlocks == matchedFunction.getSizeOfBasicBlocks()
              && this.basicBlocks == getSizeOfMatchedBasicBlocks();
      final boolean jumps =
          this.jumps == matchedFunction.getSizeOfJumps() && this.jumps == getSizeOfMatchedJumps();
      final boolean instructions =
          this.instructions == matchedFunction.getSizeOfInstructions()
              && this.instructions == getSizeOfMatchedInstructions();

      return basicBlocks && jumps && !instructions;
    }

    return false;
  }

  public boolean isChangedStructuralMatch() {
    if (getMatchState() == EMatchState.MATCHED) {
      final boolean basicBlocks =
          this.basicBlocks == matchedFunction.getSizeOfBasicBlocks()
              && this.basicBlocks == getSizeOfMatchedBasicBlocks();
      final boolean jumps =
          this.jumps == matchedFunction.getSizeOfJumps() && this.jumps == getSizeOfMatchedJumps();

      return !basicBlocks || !jumps;
    }

    return false;
  }

  public boolean isIdenticalMatch() {
    if (getMatchState() == EMatchState.MATCHED) {
      final boolean basicBlocks =
          this.basicBlocks == matchedFunction.getSizeOfBasicBlocks()
              && this.basicBlocks == getSizeOfMatchedBasicBlocks();
      final boolean jumps =
          this.jumps == matchedFunction.getSizeOfJumps() && this.jumps == getSizeOfMatchedJumps();
      final boolean instructions =
          this.instructions == matchedFunction.getSizeOfInstructions()
              && this.instructions == getSizeOfMatchedInstructions();

      return basicBlocks && jumps && instructions;
    }

    return false;
  }

  public void setComment(final String comment) {
    this.comment = comment;
  }

  public void setMatch(final RawFunction matchedFunction, final FunctionMatchData match) {
    this.matchedFunction = matchedFunction;
    this.match = match;
  }

  public void setSizeOfBasicBlocks(final int basicBlocks) {
    this.basicBlocks = basicBlocks;
  }

  public void setSizeOfInstructions(final int instructions) {
    this.instructions = instructions;
  }

  public void setSizeOfJumps(final int jumps) {
    this.jumps = jumps;
  }
}
