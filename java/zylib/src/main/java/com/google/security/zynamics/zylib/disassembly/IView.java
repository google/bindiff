// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.disassembly;

import com.google.security.zynamics.zylib.gui.zygraph.edges.IViewEdge;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.types.graphs.IDirectedGraph;

public interface IView<NodeType extends IViewNode<?>, ListenerType extends IViewListener<?>> {
  void addListener(ListenerType listener);

  boolean close();

  int getEdgeCount();

  IDirectedGraph<? extends NodeType, ? extends IViewEdge<? extends NodeType>> getGraph();

  GraphType getGraphType();

  String getName();

  int getNodeCount();

  ViewType getType();

  boolean isLoaded();

  void removeListener(ListenerType listener);
}
