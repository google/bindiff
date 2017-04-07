// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.CStateChange;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseStateChange;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.CStateFactory;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import y.base.Edge;

import java.awt.event.MouseEvent;

public class CEdgePressedLeftState implements IMouseState {
  private final CStateFactory<?, ?> m_factory;

  private final AbstractZyGraph<?, ?> m_graph;

  private final Edge m_edge;

  public CEdgePressedLeftState(final CStateFactory<?, ?> factory, final AbstractZyGraph<?, ?> graph,
      final Edge edge) {
    m_factory = Preconditions.checkNotNull(factory, "Error: factory argument can not be null");
    m_graph = Preconditions.checkNotNull(graph, "Error: graph argument can not be null");
    m_edge = Preconditions.checkNotNull(edge, "Error: edge argument can not be null");
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
    return new CStateChange(this, true);
  }

  @Override
  public IMouseStateChange mouseMoved(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return new CStateChange(this, true);
  }

  @Override
  public IMouseStateChange mousePressed(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return new CStateChange(this, true);
  }

  @Override
  public IMouseStateChange mouseReleased(final MouseEvent event,
      final AbstractZyGraph<?, ?> graph) {
    // A left-click is complete.
    return new CStateChange(m_factory.createEdgeClickedLeftState(m_edge, event), true);
  }
}
