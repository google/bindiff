// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.types.graphs;

import java.util.List;

/**
 * Interface for directed graphs.
 * 
 * @param <NodeType> Type of the nodes in the graph.
 * @param <EdgeType> Type of the edges in the graph.
 */
public interface IDirectedGraph<NodeType, EdgeType> extends Iterable<NodeType> {
  /**
   * Returns the number of edges in the graph.
   * 
   * @return The number of edges in the graph.
   */
  int edgeCount();

  /**
   * Returns the edges of the graph.
   * 
   * @return The edges of the graph.
   */
  List<EdgeType> getEdges();

  /**
   * Returns the nodes of the graph.
   * 
   * @return The nodes of the graph.
   */
  List<NodeType> getNodes();

  /**
   * Returns the number of nodes in the graph.
   * 
   * @return The number of nodes in the graph.
   */
  int nodeCount();
}
