// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.helpers;


/**
 * Graphs that implement this interface unlock {@link GraphHelpers} functions that require the
 * ability to iterate over visible nodes.
 * 
 * @param <NodeType> The type of the nodes in the graph.
 */
public interface IViewableGraph<NodeType> {
  public void iterateInvisible(final INodeCallback<NodeType> callback);

  /**
   * Iterates over all visible nodes in the graph.
   * 
   * @param callback Callback object that is called once for each visible node in the graph.
   */
  public void iterateVisible(final INodeCallback<NodeType> callback);
}
