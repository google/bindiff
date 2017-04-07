package com.google.security.zynamics.bindiff.project.rawcallgraph;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.EFunctionType;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedViewNode;
import com.google.security.zynamics.zylib.disassembly.IAddress;

public class RawCombinedFunction extends CombinedViewNode {
  private final RawFunction primaryFunction;
  private final RawFunction secondaryFunction;

  public RawCombinedFunction(
      final RawFunction primaryFunction, final RawFunction secondaryFunction) {
    super();

    Preconditions.checkArgument(
        primaryFunction != null || secondaryFunction != null,
        "Primary function and seconday function cannot both be null");

    this.primaryFunction = primaryFunction;
    this.secondaryFunction = secondaryFunction;
  }

  @Override
  public IAddress getAddress(final ESide side) {
    if (getRawNode(side) == null) {
      return null;
    }

    return getRawNode(side).getAddress();
  }

  public EFunctionType getFunctionType() {
    if (primaryFunction == null) {
      return secondaryFunction.getFunctionType();
    }

    if (secondaryFunction == null) {
      return primaryFunction.getFunctionType();
    }

    if (primaryFunction.getFunctionType() == secondaryFunction.getFunctionType()) {
      return primaryFunction.getFunctionType();
    }

    return EFunctionType.MIXED;
  }

  @Override
  public RawFunction getRawNode(final ESide side) {
    if (side == ESide.PRIMARY) {
      return primaryFunction;
    }

    return secondaryFunction;
  }

  public boolean isChanged() {
    if (primaryFunction != null && secondaryFunction != null) {
      return primaryFunction.isChanged();
    }

    return false;
  }
}
