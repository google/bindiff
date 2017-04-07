// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.nodes;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

/**
 * Collects all information that is passed to the user data field of a yfiles node.
 * 
 * @param <NodeType> The type of the node.
 */
public class ZyNodeData<NodeType extends ZyGraphNode<? extends IViewNode<?>>> {
  private final NodeType m_node;

  public ZyNodeData(final NodeType node) {
    Preconditions.checkNotNull(node, "Error: Node argument can't be null");

    m_node = node;
  }

  public NodeType getNode() {
    return m_node;
  }

}
