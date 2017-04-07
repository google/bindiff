// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.helpers;

import com.google.security.zynamics.zylib.types.common.IItemCallback;

/**
 * Objects that implement this interface can be used as callback objects when iterating over the
 * nodes in a graph.
 * 
 * @param <NodeType> The type of the nodes in the graph.
 */
public interface INodeCallback<NodeType> extends IItemCallback<NodeType> {
}
