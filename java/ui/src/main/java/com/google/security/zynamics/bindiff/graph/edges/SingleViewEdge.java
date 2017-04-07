package com.google.security.zynamics.bindiff.graph.edges;

import com.google.security.zynamics.bindiff.graph.nodes.SingleViewNode;
import com.google.security.zynamics.zylib.gui.zygraph.edges.CViewEdge;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;
import java.awt.Color;
import java.util.ArrayList;

/** Edge type for the graphs underlying a single diff view (primary or secondary). */
public abstract class SingleViewEdge<NodeType extends SingleViewNode> extends CViewEdge<NodeType> {
  public SingleViewEdge(final NodeType sourceNode, final NodeType targetNode) {
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

    SingleViewNode.link(sourceNode, targetNode);

    sourceNode.addOutgoingEdge(this);
    targetNode.addIncomingEdge(this);
  }
}
