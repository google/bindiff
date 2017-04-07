package com.google.security.zynamics.bindiff.graph.edges;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.bindiff.enums.ESide;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedViewNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleViewNode;
import com.google.security.zynamics.bindiff.graph.nodes.SuperViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.edges.CViewEdge;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;
import java.awt.Color;
import java.util.ArrayList;

public final class SuperViewEdge<NodeType extends SuperViewNode> extends CViewEdge<NodeType> {
  private final CombinedViewEdge<? extends CombinedViewNode> combinedEdge;

  public SuperViewEdge(
      final CombinedViewEdge<? extends CombinedViewNode> combinedEdge,
      final NodeType sourceNode,
      final NodeType targetNode) {
    super(
        -1,
        sourceNode,
        targetNode,
        EdgeType.DUMMY,
        0.,
        0.,
        0.,
        0.,
        Color.BLACK,
        false,
        true,
        new ArrayList<>());

    this.combinedEdge = Preconditions.checkNotNull(combinedEdge);

    SuperViewNode.link(sourceNode, targetNode);

    sourceNode.addOutgoingEdge(this);
    targetNode.addIncomingEdge(this);
  }

  public CombinedViewEdge<? extends CombinedViewNode> getCombinedEdge() {
    return combinedEdge;
  }

  public SingleViewEdge<? extends SingleViewNode> getSingleEdge(final ESide side) {
    return side == ESide.PRIMARY ? combinedEdge.getPrimaryEdge() : combinedEdge.getSecondaryEdge();
  }
}
