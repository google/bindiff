package com.google.security.zynamics.bindiff.graph.edges;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.nodes.SingleDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleViewNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;

import y.base.Edge;

public class SingleDiffEdge
    extends ZyGraphEdge<SingleDiffNode, SingleDiffEdge, SingleViewEdge<? extends SingleViewNode>> {
  private SingleDiffEdge otherDiffEdge;
  private SuperDiffEdge superDiffEdge;
  private CombinedDiffEdge combinedDiffEdge;
  private final ESide side;

  public SingleDiffEdge(
      final SingleDiffNode source,
      final SingleDiffNode target,
      final Edge edge,
      final ZyEdgeRealizer<SingleDiffEdge> realizer,
      final SingleViewEdge<? extends SingleViewNode> rawEdge,
      final ESide side) {
    super(source, target, edge, realizer, rawEdge);

    this.side = Preconditions.checkNotNull(side);
  }

  public CombinedDiffEdge getCombinedDiffEdge() {
    return combinedDiffEdge;
  }

  public SingleDiffEdge getOtherSideDiffEdge() {
    return otherDiffEdge;
  }

  public ESide getSide() {
    return side;
  }

  public SuperDiffEdge getSuperDiffEdge() {
    return superDiffEdge;
  }

  public void setCombinedDiffEdge(final CombinedDiffEdge combinedDiffEdge) {
    this.combinedDiffEdge = combinedDiffEdge;
    superDiffEdge = combinedDiffEdge.getSuperDiffEdge();
    otherDiffEdge =
        side == ESide.PRIMARY
            ? combinedDiffEdge.getSecondaryDiffEdge()
            : combinedDiffEdge.getPrimaryDiffEdge();
  }
}
