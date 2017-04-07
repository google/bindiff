// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.helpers;

import com.google.security.zynamics.zylib.types.common.ICollectionFilter;

/**
 * Objects that implement this interface can be used to filter nodes from node lists.
 * 
 * @param <NodeType> The type of the nodes in the list.
 */
public interface INodeFilter<NodeType> extends ICollectionFilter<NodeType> {
  /**
   * Determines whether a node passes the filter check.
   * 
   * @param node The node in question.
   * @return True, if the node passes the filter check. False, if the node does not pass the filter
   *         check.
   */
  @Override
  boolean qualifies(NodeType node);
}
