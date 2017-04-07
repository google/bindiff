// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.nodes;

import com.google.security.zynamics.zylib.gui.zygraph.edges.IViewEdge;

import java.util.List;


public interface IGroupNode<NodeType extends IViewNode<?>, EdgeType extends IViewEdge<NodeType>>
    extends IViewNode<EdgeType> {
  void addElement(NodeType element);

  List<NodeType> getElements();

  boolean isCollapsed();

  void setCollapsed(boolean collapsed);
}
