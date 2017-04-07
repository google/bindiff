package com.google.security.zynamics.bindiff.graph.edges;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.graph.nodes.SuperDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperViewNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;

import y.base.Edge;

public class SuperDiffEdge
    extends ZyGraphEdge<SuperDiffNode, SuperDiffEdge, SuperViewEdge<? extends SuperViewNode>> {
  private final SingleDiffEdge primaryDiffEdge;
  private final SingleDiffEdge secondaryDiffEdge;

  private CombinedDiffEdge combinedDiffEdge;

  public SuperDiffEdge(
      final SuperDiffNode source,
      final SuperDiffNode target,
      final Edge edge,
      final ZyEdgeRealizer<SuperDiffEdge> realizer,
      final SuperViewEdge<? extends SuperViewNode> rawEdge,
      final SingleDiffEdge primaryDiffEdge,
      final SingleDiffEdge secondaryDiffEdge) {
    super(source, target, edge, realizer, rawEdge);

    Preconditions.checkArgument(
        primaryDiffEdge != null || secondaryDiffEdge != null,
        "Primary and secondary edge cannot both be null.");

    this.primaryDiffEdge = primaryDiffEdge;
    this.secondaryDiffEdge = secondaryDiffEdge;
  }

  public CombinedDiffEdge getCombinedDiffEdge() {
    return combinedDiffEdge;
  }

  public SingleDiffEdge getPrimaryDiffEdge() {
    return primaryDiffEdge;
  }

  @Override
  public SuperViewEdge<? extends SuperViewNode> getRawEdge() {
    return super.getRawEdge();
  }

  public SingleDiffEdge getSecondaryDiffEdge() {
    return secondaryDiffEdge;
  }

  public void setCombinedDiffEdge(final CombinedDiffEdge combinedDiffEdge) {
    this.combinedDiffEdge = combinedDiffEdge;
  }
}
