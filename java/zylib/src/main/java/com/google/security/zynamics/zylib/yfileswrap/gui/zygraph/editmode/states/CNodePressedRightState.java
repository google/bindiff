// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.states;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.CStateChange;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseState;
import com.google.security.zynamics.zylib.gui.zygraph.editmode.IMouseStateChange;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.edges.ZyGraphEdge;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.CStateFactory;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.editmode.helpers.CNodeMover;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import y.base.Node;

import java.awt.event.MouseEvent;

public class CNodePressedRightState implements IMouseState {
  private final CStateFactory<?, ?> m_factory;

  private final AbstractZyGraph<?, ?> m_graph;

  private final MouseEvent m_event;

  private final Node m_node;

  public CNodePressedRightState(final CStateFactory<?, ?> factory,
      final AbstractZyGraph<?, ?> graph, final Node node, final MouseEvent event) {
    m_factory = Preconditions.checkNotNull(factory, "Error: factory argument can not be null");
    m_graph = Preconditions.checkNotNull(graph, "Error: graph argument can not be null");
    m_node = Preconditions.checkNotNull(node, "Error: node argument can not be null");
    m_event = Preconditions.checkNotNull(event, "Error: event argument can not be null");
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
    final int oldX = m_event.getX();
    final int oldY = m_event.getY();

    final int newX = event.getX();
    final int newY = event.getY();

    if (CNodeMover.isDraggedFar(oldX, oldY, newX, newY)) {
      return new CStateChange(m_factory.createBackgroundDraggedRightState(event), true);
    } else {
      return new CStateChange(this, true);
    }
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
  public IMouseStateChange mouseReleased(final MouseEvent event, final AbstractZyGraph<?, ?> graph) {
    // A right-click is complete.

    return new CStateChange(m_factory.createNodeClickedRightState(m_node, event), true);
  }
}
