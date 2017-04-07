package com.google.security.zynamics.bindiff.graph.edges;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedDiffNode;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedViewNode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;

import y.base.Edge;

public class CombinedDiffEdge
    extends ZyGraphEdge<
        CombinedDiffNode, CombinedDiffEdge, CombinedViewEdge<? extends CombinedViewNode>> {
  private final SingleDiffEdge primaryDiffEdge;
  private final SingleDiffEdge secondaryDiffEdge;
  private final SuperDiffEdge superDiffEdge;

  public CombinedDiffEdge(
      final CombinedDiffNode source,
      final CombinedDiffNode target,
      final Edge edge,
      final ZyEdgeRealizer<CombinedDiffEdge> realizer,
      final CombinedViewEdge<? extends CombinedViewNode> rawEdge,
      final SuperDiffEdge superDiffEdge) {
    super(source, target, edge, realizer, rawEdge);

    this.superDiffEdge = Preconditions.checkNotNull(superDiffEdge);

    this.primaryDiffEdge = superDiffEdge.getPrimaryDiffEdge();
    this.secondaryDiffEdge = superDiffEdge.getSecondaryDiffEdge();
  }

  public SingleDiffEdge getPrimaryDiffEdge() {
    return primaryDiffEdge;
  }

  public SingleDiffEdge getSecondaryDiffEdge() {
    return secondaryDiffEdge;
  }

  public SuperDiffEdge getSuperDiffEdge() {
    return superDiffEdge;
  }
}
