// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.types.graphs;

import com.google.common.base.Preconditions;

/**
 * Default graph edge implementation.
 * 
 * @param <NodeType> Type of the graph nodes connected by the edge.
 */
public class DefaultEdge<NodeType> implements IGraphEdge<NodeType> {
  /**
   * Source node of the edge.
   */
  private final NodeType m_source;

  /**
   * Target node of the edge.
   */
  private final NodeType m_target;

  /**
   * Creates a new default edge.
   * 
   * @param source Source node of the edge.
   * @param target Target node of the edge.
   */
  public DefaultEdge(final NodeType source, final NodeType target) {
    m_source = Preconditions.checkNotNull(source, "Error: Source argument can not be null");
    m_target = Preconditions.checkNotNull(target, "Error: Target argument can not be null");
  }

  @Override
  public NodeType getSource() {
    return m_source;
  }

  @Override
  public NodeType getTarget() {
    return m_target;
  }
}
