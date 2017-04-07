// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.types.graphs;

/**
 * Interface for graph edges.
 * 
 * @param <T> The type of the nodes connected by the edge.
 */
public interface IGraphEdge<T> {
  /**
   * Returns the source node of the edge.
   * 
   * @return The source node of the edge.
   */
  T getSource();

  /**
   * Returns the target node of the edge.
   * 
   * @return The target node of the edge.
   */
  T getTarget();
}
