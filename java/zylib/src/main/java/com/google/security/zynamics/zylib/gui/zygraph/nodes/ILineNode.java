// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.nodes;

import com.google.security.zynamics.zylib.gui.zygraph.edges.IViewEdge;


/**
 * Interface that is implemented by all nodes that display line content.
 */
public interface ILineNode<EdgeType extends IViewEdge<? extends IViewNode<?>>> extends
    IViewNode<EdgeType> {
}
