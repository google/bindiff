package com.google.security.zynamics.bindiff.graph.edges;

import com.google.security.zynamics.bindiff.enums.EMatchState;
import com.google.security.zynamics.bindiff.graph.nodes.CombinedViewNode;
import com.google.security.zynamics.bindiff.graph.nodes.SingleViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.edges.CViewEdge;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;
import java.awt.Color;
import java.util.ArrayList;

public abstract class CombinedViewEdge<NodeType extends CombinedViewNode>
    extends CViewEdge<NodeType> {
  public CombinedViewEdge(final NodeType sourceNode, final NodeType targetNode) {
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

    CombinedViewNode.link(sourceNode, targetNode);

    sourceNode.addOutgoingEdge(this);
    targetNode.addIncomingEdge(this);
  }

  public abstract EMatchState getMatchState();

  public abstract SingleViewEdge<? extends SingleViewNode> getPrimaryEdge();

  public abstract SingleViewEdge<? extends SingleViewNode> getSecondaryEdge();
}
