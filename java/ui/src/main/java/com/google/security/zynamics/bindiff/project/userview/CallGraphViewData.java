package com.google.security.zynamics.bindiff.project.userview;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.enums.EViewType;
import com.google.security.zynamics.bindiff.graph.GraphsContainer;
import com.google.security.zynamics.bindiff.project.rawcallgraph.RawCallGraph;
import com.google.security.zynamics.zylib.disassembly.IAddress;

public class CallGraphViewData extends ViewData {
  private final RawCallGraph primaryRawGraph;
  private final RawCallGraph secondaryRawGraph;

  private final String priImageName;
  private final String secImageName;

  public CallGraphViewData(
      final RawCallGraph primaryRawCallgraph,
      final RawCallGraph secondaryRawCallgraph,
      final GraphsContainer graphs,
      final String viewName,
      final String priImageName,
      final String secImageName,
      final EViewType viewType) {
    super(graphs, viewName, viewType);
    this.primaryRawGraph = Preconditions.checkNotNull(primaryRawCallgraph);
    this.secondaryRawGraph = secondaryRawCallgraph;
    this.priImageName = priImageName;
    this.secImageName = secImageName;
  }

  @Override
  public IAddress getAddress(final ESide side) {
    return null;
  }

  public String getImageName(final ESide side) {
    return side == ESide.PRIMARY ? priImageName : secImageName;
  }

  @Override
  public RawCallGraph getRawGraph(final ESide side) {
    return side == ESide.PRIMARY ? primaryRawGraph : secondaryRawGraph;
  }

  @Override
  public boolean isCallgraphView() {
    return true;
  }

  @Override
  public boolean isFlowgraphView() {
    return false;
  }
}
