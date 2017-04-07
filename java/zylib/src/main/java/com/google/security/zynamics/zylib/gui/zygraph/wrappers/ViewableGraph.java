// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.wrappers;

import com.google.security.zynamics.zylib.gui.zygraph.functions.IteratorFunctions;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.IViewableGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

public class ViewableGraph<NodeType extends ZyGraphNode<?>> implements IViewableGraph<NodeType> {
  private final AbstractZyGraph<NodeType, ?> m_graph;

  private ViewableGraph(final AbstractZyGraph<NodeType, ?> graph) {
    m_graph = graph;
  }

  public static <NodeType extends ZyGraphNode<?>> ViewableGraph<NodeType> wrap(
      final AbstractZyGraph<NodeType, ?> graph) {
    return new ViewableGraph<NodeType>(graph);
  }

  @Override
  public void iterateInvisible(final INodeCallback<NodeType> callback) {
    IteratorFunctions.iterateInvisible(m_graph, callback);
  }

  @Override
  public void iterateVisible(final INodeCallback<NodeType> callback) {
    IteratorFunctions.iterateVisible(m_graph, callback);
  }
}
