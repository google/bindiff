// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.gui.zygraph.editmode.states;

import java.awt.event.MouseEvent;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.CStateChange;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseStateChange;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.CStateFactory;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CMousePressedHandler;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

public class CBackgroundPressedLeftState implements IMouseState {
  private final CStateFactory<?, ?> m_factory;

  private final AbstractZyGraph<?, ?> m_graph;

  public CBackgroundPressedLeftState(final CStateFactory<?, ?> factory,
      final AbstractZyGraph<?, ?> graph) {
    m_factory = Preconditions.checkNotNull(factory, "Error: factory argument can not be null");
    m_graph = Preconditions.checkNotNull(graph, "Error: graph argument can not be null");
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
    return new CStateChange(m_factory.createBackgroundDraggedLeftState(event), true);
  }

  @Override
  public IMouseStateChange mouseMoved(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return new CStateChange(this, true);
  }

  @Override
  public IMouseStateChange mousePressed(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    return CMousePressedHandler.handleMousePressed(m_factory, this, graph, event);
  }

  @Override
  public IMouseStateChange mouseReleased(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    // a left click is complete

    return new CStateChange(m_factory.createBackgroundClickedLeftState(event), true);
  }
}
