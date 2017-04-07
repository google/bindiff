// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.helpers;

import java.util.Collection;


/**
 * Graphs that implement this interface unlock {@link GraphHelpers} functions that require the
 * ability to zoom the graph.
 * 
 * @param <NodeType> The type of the nodes in the graph.
 */
public interface IZoomableGraph<NodeType> {
  /**
   * Zooms the graph so far that all nodes in the list are visible.
   * 
   * @param nodes List of nodes that should be displayed as big as possible.
   */
  public void zoomToNodes(final Collection<NodeType> nodes);
}
