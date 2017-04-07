// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.types.graphs;

import java.util.List;

/**
 * Interface for nodes in the graph.
 * 
 * @param <T> The concrete graph node type itself.
 */
public interface IGraphNode<T> {
  /**
   * Returns the children of the graph node.
   * 
   * @return The children of the graph node.
   */
  List<? extends T> getChildren();

  /**
   * Returns the parents of the graph node.
   * 
   * @return The parents of the graph node.
   */
  List<? extends T> getParents();
}
