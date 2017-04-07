// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.wrappers;

import com.google.security.zynamics.zylib.gui.zygraph.functions.IteratorFunctions;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.ISelectableGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import java.util.Collection;

public class SelectableGraph<NodeType extends ZyGraphNode<?>> implements ISelectableGraph<NodeType> {
  private final AbstractZyGraph<NodeType, ?> m_graph;

  private SelectableGraph(final AbstractZyGraph<NodeType, ?> graph) {
    m_graph = graph;
  }

  public static <NodeType extends ZyGraphNode<?>> SelectableGraph<NodeType> wrap(
      final AbstractZyGraph<NodeType, ?> graph) {
    return new SelectableGraph<NodeType>(graph);
  }

  @Override
  public void iterateSelected(final INodeCallback<NodeType> callback) {
    IteratorFunctions.iterateSelected(m_graph, callback);
  }

  @Override
  public void selectNodes(final Collection<NodeType> nodes, final boolean selected) {
    m_graph.selectNodes(nodes, selected);
  }

  @Override
  public void selectNodes(final Collection<NodeType> toSelect, final Collection<NodeType> toDeselect) {
    m_graph.selectNodes(toSelect, toDeselect);
  }
}
