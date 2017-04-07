// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.CStateChange;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseStateChange;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.CStateFactory;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.transformations.CHitNodesTransformer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import y.base.Node;
import y.view.HitInfo;

import java.awt.event.MouseEvent;

public class CNodeClickedRightState<NodeType extends ZyGraphNode<?>, EdgeType extends ZyGraphEdge<?, ?, ?>>
    implements IMouseState {
  private final CStateFactory<NodeType, EdgeType> m_factory;

  private final AbstractZyGraph<NodeType, EdgeType> m_graph;

  private final Node m_node;

  public CNodeClickedRightState(final CStateFactory<NodeType, EdgeType> factory,
      final AbstractZyGraph<NodeType, EdgeType> graph, final Node node) {
    m_factory = Preconditions.checkNotNull(factory, "Error: factory argument can not be null");
    m_graph = Preconditions.checkNotNull(graph, "Error: graph argument can not be null");
    m_node = Preconditions.checkNotNull(node, "Error: node argument can not be null");
  }

  public AbstractZyGraph<NodeType, EdgeType> getGraph() {
    return m_graph;
  }

  public Node getNode() {
    return m_node;
  }

  @Override
  public CStateFactory<NodeType, EdgeType> getStateFactory() {
    return m_factory;
  }

  @Override
  public IMouseStateChange mouseDragged(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return new CStateChange(this, true);
  }

  @Override
  public IMouseStateChange mouseMoved(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    final double x = graph.getEditMode().translateX(event.getX());
    final double y = graph.getEditMode().translateY(event.getY());

    final HitInfo hitInfo = graph.getGraph().getHitInfo(x, y);

    if (hitInfo.hasHitNodes()) {
      return CHitNodesTransformer.changeNode(m_factory, event, hitInfo, m_node);
    } else {
      m_factory.createNodeExitState(m_node, event);

      return CHitNodesTransformer.exitNode(m_factory, event, hitInfo, this);
    }
  }

  @Override
  public IMouseStateChange mousePressed(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return new CStateChange(this, true);
  }

  @Override
  public IMouseStateChange mouseReleased(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return new CStateChange(m_factory.createDefaultState(), true);
  }
}
