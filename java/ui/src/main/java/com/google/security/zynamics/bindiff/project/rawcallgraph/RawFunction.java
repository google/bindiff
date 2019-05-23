package com.google.security.zynamics.bindiff.project.rawcallgraph;

import com.google.common.base.Preconditions;
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

  private int basicblocks = -1;
  private int jumps = -1;
  private int instructions = -1;

  private String comment = "";

  public RawFunction(
      final IAddress address, final String name, final EFunctionType type, final ESide side) {
    super(address, -1, 0, 0, Color.WHITE, Color.BLACK, false, true);
    this.name = Preconditions.checkNotNull(name);
    this.type = Preconditions.checkNotNull(type);
    this.side = Preconditions.checkNotNull(side);
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

  public int getSizeOfBasicblocks() {
    return basicblocks;
  }

  public int getSizeOfInstructions() {
    return instructions;
  }

  public int getSizeOfJumps() {
    return jumps;
  }

  public int getSizeOfMatchedBasicblocks() {
    return match == null ? 0 : match.getSizeOfMatchedBasicblocks();
  }

  public int getSizeOfMatchedInstructions() {
    return match == null ? 0 : match.getSizeOfMatchedInstructions();
  }

  public int getSizeOfMatchedJumps() {
    return match == null ? 0 : match.getSizeOfMatchedJumps();
  }

  public int getSizeOfUnmatchedBasicblocks() {
    return basicblocks - match.getSizeOfMatchedBasicblocks();
  }

  public int getSizeOfUnmatchedInstructions() {
    return instructions - match.getSizeOfMatchedInstructions();
  }

  public int getSizeOfUnmatchedJumps() {
    return jumps - match.getSizeOfMatchedJumps();
  }

  public boolean isChanged() {
    if (getMatchState() == EMatchState.MATCHED) {
      final boolean basicblocks =
          this.basicblocks == matchedFunction.getSizeOfBasicblocks()
              && this.basicblocks == getSizeOfMatchedBasicblocks();
      final boolean jumps =
          this.jumps == matchedFunction.getSizeOfJumps() && this.jumps == getSizeOfMatchedJumps();
      final boolean instructions =
          this.instructions == matchedFunction.getSizeOfInstructions()
              && this.instructions == getSizeOfMatchedInstructions();

      return !basicblocks || !jumps || !instructions;
    }

    return false;
  }

  public boolean isChangedInstructionsOnlyMatch() {
    if (getMatchState() == EMatchState.MATCHED) {
      final boolean basicblocks =
          this.basicblocks == matchedFunction.getSizeOfBasicblocks()
              && this.basicblocks == getSizeOfMatchedBasicblocks();
      final boolean jumps =
          this.jumps == matchedFunction.getSizeOfJumps() && this.jumps == getSizeOfMatchedJumps();
      final boolean instructions =
          this.instructions == matchedFunction.getSizeOfInstructions()
              && this.instructions == getSizeOfMatchedInstructions();

      return basicblocks && jumps && !instructions;
    }

    return false;
  }

  public boolean isChangedStructuralMatch() {
    if (getMatchState() == EMatchState.MATCHED) {
      final boolean basicblocks =
          this.basicblocks == matchedFunction.getSizeOfBasicblocks()
              && this.basicblocks == getSizeOfMatchedBasicblocks();
      final boolean jumps =
          this.jumps == matchedFunction.getSizeOfJumps() && this.jumps == getSizeOfMatchedJumps();

      return !basicblocks || !jumps;
    }

    return false;
  }

  public boolean isIdenticalMatch() {
    if (getMatchState() == EMatchState.MATCHED) {
      final boolean basicblocks =
          this.basicblocks == matchedFunction.getSizeOfBasicblocks()
              && this.basicblocks == getSizeOfMatchedBasicblocks();
      final boolean jumps =
          this.jumps == matchedFunction.getSizeOfJumps() && this.jumps == getSizeOfMatchedJumps();
      final boolean instructions =
          this.instructions == matchedFunction.getSizeOfInstructions()
              && this.instructions == getSizeOfMatchedInstructions();

      return basicblocks && jumps && instructions;
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

  public void setSizeOfBasicBlocks(final int basicblocks) {
    this.basicblocks = basicblocks;
  }

  public void setSizeOfInstructions(final int instructions) {
    this.instructions = instructions;
  }

  public void setSizeOfJumps(final int jumps) {
    this.jumps = jumps;
  }
}
