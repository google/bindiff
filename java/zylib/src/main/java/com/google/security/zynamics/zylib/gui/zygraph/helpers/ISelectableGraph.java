// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.helpers;

import java.util.Collection;


/**
 * Graphs that implement this interface unlock {@link GraphHelpers} functions that require the
 * ability to iterate over selected nodes.
 * 
 * @param <NodeType> The type of the nodes in the graph.
 */
public interface ISelectableGraph<NodeType> {
  /**
   * Iterates over all selected nodes in the graph.
   * 
   * @param callback Callback object that is called once for each selected node in the graph.
   */
  void iterateSelected(final INodeCallback<NodeType> callback);

  /**
   * Selects or deselects a collection of nodes in the graph.
   * 
   * @param nodes The nodes to select or deselect.
   * @param selected True, to select the given nodes. False, to deselect the given nodes.
   */
  void selectNodes(final Collection<NodeType> nodes, final boolean selected);

  /**
   * Selects a list of nodes while deselecting another list of nodes.
   * 
   * @param toSelect The nodes to select.
   * @param toDeselect The nodes to deselect.
   */
  void selectNodes(final Collection<NodeType> toSelect, final Collection<NodeType> toDeselect);
}
