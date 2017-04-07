// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.helpers;

import com.google.security.zynamics.zylib.types.common.IterationMode;

/**
 * Objects that implement this interface can be used as callback objects when iterating over the
 * edges in a graph.
 * 
 * @param <EdgeType> The type of the nodes in the graph.
 */
public interface IEdgeCallback<EdgeType> {
  /**
   * This function is called by the iterator object for each edge of a graph that is considered
   * during iteration.
   * 
   * @param edge An edge of the graph.
   * 
   * @return Information that's passed back to the iterator object to help the object to find out
   *         what to do next.
   */
  IterationMode nextEdge(EdgeType edge);
}
