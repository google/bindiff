package com.google.security.zynamics.bindiff.project.rawflowgraph;

import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedViewNode;
import com.google.security.zynamics.bindiff.project.matches.BasicBlockMatchData;
import com.google.security.zynamics.zylib.disassembly.IAddress;

public class RawCombinedBasicBlock extends CombinedViewNode {
  private final BasicBlockMatchData basicblockMatch;

  private final RawBasicBlock primaryBasicblock;

  private final RawBasicBlock secondaryBasicblock;

  private final IAddress primaryFunctionAddr;

  private final IAddress secondaryFunctionAddr;

  public RawCombinedBasicBlock(
      final RawBasicBlock primaryBasicblock,
      final RawBasicBlock secondaryBasicblock,
      final BasicBlockMatchData basicblockMatch,
      final IAddress primaryFunctionAddr,
      final IAddress secondaryFunctionAddr) {
    super();

    if (primaryBasicblock == null && secondaryBasicblock == null) {
      throw new IllegalArgumentException(
          "Primary basic block and seconday basic block cannot both be null.");
    }
    if (primaryFunctionAddr == null && secondaryFunctionAddr == null) {
      throw new IllegalArgumentException(
          "Primary and secondary function address cannot both be null.");
    }
    this.primaryBasicblock = primaryBasicblock;
    this.secondaryBasicblock = secondaryBasicblock;
    this.basicblockMatch = basicblockMatch;
    this.primaryFunctionAddr = primaryFunctionAddr;
    this.secondaryFunctionAddr = secondaryFunctionAddr;
  }

  @Override
  public IAddress getAddress(final ESide side) {
    if (getRawNode(side) == null) {
      return null;
    }

    return getRawNode(side).getAddress();
  }

  public BasicBlockMatchData getBasicblockMatch() {
    return basicblockMatch;
  }

  public IAddress getPrimaryFunctionAddress() {
    return primaryFunctionAddr;
  }

  @Override
  public RawBasicBlock getRawNode(final ESide side) {
    if (side == ESide.PRIMARY) {
      return primaryBasicblock;
    }

    return secondaryBasicblock;
  }

  public IAddress getSecondaryFunctionAddress() {
    return secondaryFunctionAddr;
  }
}
