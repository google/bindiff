// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.helpers;

import com.google.security.zynamics.zylib.types.common.IIterableCollection;

/**
 * Graphs that implement this interface unlock {@link GraphHelpers} functions that require the
 * ability to iterate over all nodes in a graph.
 * 
 * @param <NodeType> The type of the nodes in the graph.
 */
public interface IIterableGraph<NodeType> extends IIterableCollection<INodeCallback<NodeType>> {
}
