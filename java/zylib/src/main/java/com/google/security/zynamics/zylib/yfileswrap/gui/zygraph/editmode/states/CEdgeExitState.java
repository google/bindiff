// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.CStateChange;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseStateChange;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.CStateFactory;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.transformations.CHitEdgesTransformer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import y.base.Edge;
import y.view.HitInfo;

import java.awt.event.MouseEvent;

public class CEdgeExitState implements IMouseState {
  private final CStateFactory<?, ?> m_factory;

  private final AbstractZyGraph<?, ?> m_graph;

  private final Edge m_edge;

  public CEdgeExitState(final CStateFactory<?, ?> factory, final AbstractZyGraph<?, ?> graph,
      final Edge edge) {
    m_factory = Preconditions.checkNotNull(factory, "Error: factory argument can not be null");
    m_graph = Preconditions.checkNotNull(graph, "Error: graph argument can not be null");
    m_edge = Preconditions.checkNotNull(edge, "Error: edge argument can not be null");
  }

  public Edge getEdge() {
    return m_edge;
  }

  public AbstractZyGraph<?, ?> getGraph() {
    return m_graph;
  }

  @Override
  public CStateFactory<? extends ZyGraphNode<?>, ? extends ZyGraphEdge<?, ?, ?>> getStateFactory() {
    return m_factory;
  }

  @Override
  public IMouseStateChange mouseDragged(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return new CStateChange(m_factory.createDefaultState(), true);
  }

  @Override
  public IMouseStateChange mouseMoved(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    final double x = m_graph.getEditMode().translateX(event.getX());
    final double y = m_graph.getEditMode().translateY(event.getY());

    final HitInfo hitInfo = m_graph.getGraph().getHitInfo(x, y);

    if (hitInfo.hasHitEdges()) {
      return CHitEdgesTransformer.changeEdge(m_factory, event, hitInfo, m_edge);
    } else {
      return CHitEdgesTransformer.exitEdge(m_factory, event, hitInfo, this);
    }
  }

  @Override
  public IMouseStateChange mousePressed(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return new CStateChange(m_factory.createDefaultState(), true);
  }

  @Override
  public IMouseStateChange mouseReleased(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return new CStateChange(m_factory.createDefaultState(), true);
  }
}
